package com.mycodecalendar.core.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Single source of truth for all HTTP calls.
 *
 * Changes from original:
 * 1. Added [HttpTimeout] plugin — each call times out in 12 seconds
 * 2. Replaced broken Heroku LeetCode API with the official LeetCode GraphQL endpoint
 * 3. Added [fetchLeetCodeGraphQL] that POSTs a GraphQL query and parses [LeetCodeStatsSummary]
 * 4. All methods return [Result] — callers handle failures gracefully
 */
class RemoteDataSource(
    private val baseUrl: String = "https://api.mycodecalendar.com/v1"
) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(json)
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 12_000L
            connectTimeoutMillis = 8_000L
            socketTimeoutMillis = 12_000L
        }
        // Don't throw on non-2xx — let callers handle it via Result
        expectSuccess = false
    }

    // ── LIVE CONTESTS ─────────────────────────────────────────────────────────

    /**
     * Fetches all upcoming/live contests from the Kontests.net aggregator API.
     * Returns contests from Codeforces, LeetCode, CodeChef, AtCoder, and more.
     * Endpoint: GET https://kontests.net/api/v1/all
     */
    suspend fun fetchKontestsContests(): Result<List<KontestApiDto>> {
        return runCatching {
            val response: List<KontestApiDto> =
                client.get("https://kontests.net/api/v1/all").body()
            response
        }
    }

    /**
     * Fetches contest list directly from Codeforces official API.
     * Used as a secondary source to supplement Kontests data.
     * Endpoint: GET https://codeforces.com/api/contest.list?gym=false
     */
    suspend fun fetchCodeforcesContests(): Result<List<CodeforcesContestDto>> {
        return runCatching {
            val response: CodeforcesResponseDto<List<CodeforcesContestDto>> =
                client.get("https://codeforces.com/api/contest.list?gym=false").body()
            if (response.status == "OK" && response.result != null) {
                response.result
            } else {
                throw Exception(response.comment ?: "Codeforces API returned non-OK status")
            }
        }
    }

    // ── CODEFORCES USER STATS & RATING ──────────────────────────────────────

    /**
     * Fetches a Codeforces user's handle, current rating, max rating, and rank.
     * Endpoint: GET https://codeforces.com/api/user.info?handles={handle}
     */
    suspend fun fetchCodeforcesUserInfo(handle: String): Result<CodeforcesUserDto> {
        return runCatching {
            val response: CodeforcesResponseDto<List<CodeforcesUserDto>> =
                client.get("https://codeforces.com/api/user.info?handles=$handle").body()
            val user = response.result?.firstOrNull()
                ?: throw Exception(response.comment ?: "Codeforces user '$handle' not found")
            if (response.status == "OK") user
            else throw Exception(response.comment ?: "Codeforces API error for user '$handle'")
        }
    }

    /**
     * Fetches the full rating history for a Codeforces user.
     * Endpoint: GET https://codeforces.com/api/user.rating?handle={handle}
     */
    suspend fun fetchCodeforcesRatingHistory(handle: String): Result<List<CodeforcesRatingPointDto>> {
        return runCatching {
            val response: CodeforcesResponseDto<List<CodeforcesRatingPointDto>> =
                client.get("https://codeforces.com/api/user.rating?handle=$handle").body()
            if (response.status == "OK" && response.result != null) {
                response.result
            } else {
                throw Exception(response.comment ?: "Rating history not found for '$handle'")
            }
        }
    }

    /**
     * Fetches recent submissions for a Codeforces user to compute actual problems solved.
     * Endpoint: GET https://codeforces.com/api/user.status?handle={handle}&from=1&count=1000
     */
    suspend fun fetchCodeforcesUserSubmissions(handle: String): Result<List<CodeforcesSubmissionDto>> {
        return runCatching {
            val response: CodeforcesResponseDto<List<CodeforcesSubmissionDto>> =
                client.get("https://codeforces.com/api/user.status?handle=$handle&from=1&count=1000").body()
            if (response.status == "OK" && response.result != null) {
                response.result
            } else {
                throw Exception(response.comment ?: "Submissions not found for '$handle'")
            }
        }
    }

    // ── CODECHEF STATS ────────────────────────────────────────────────────────

    /**
     * Fetches CodeChef user stats (rating, stars, problems solved, rank) via community API.
     * Endpoint: GET https://codechef-api.vercel.app/handle/{username}
     */
    suspend fun fetchCodeChefStats(username: String): Result<CodeChefApiResponseDto> {
        return runCatching {
            val response: CodeChefApiResponseDto =
                client.get("https://codechef-api.vercel.app/handle/$username").body()
            if (response.success || response.currentRating != null) {
                response
            } else {
                throw Exception("CodeChef user '$username' not found or API error")
            }
        }
    }

    // ── GITHUB USER & DAILY CONTRIBUTIONS ────────────────────────────────────

    /**
     * Fetches a GitHub user's public profile (name, repos, followers, etc.).
     * Endpoint: GET https://api.github.com/users/{username}
     * Note: unauthenticated → 60 req/hour rate limit per IP.
     */
    suspend fun fetchGitHubUser(username: String): Result<GitHubUserDto> {
        return runCatching {
            val response: GitHubUserDto =
                client.get("https://api.github.com/users/$username") {
                    header("User-Agent", "MyCodeCalendar-Android/1.0")
                    header("Accept", "application/vnd.github.v3+json")
                }.body()
            response
        }
    }

    /**
     * Fetches up to 100 public repos for a GitHub user (sorted by last updated).
     * Used to compute: total stars, top languages.
     * Endpoint: GET https://api.github.com/users/{username}/repos
     */
    suspend fun fetchGitHubUserRepos(username: String): Result<List<GitHubRepoDto>> {
        return runCatching {
            val response: List<GitHubRepoDto> =
                client.get("https://api.github.com/users/$username/repos?per_page=100&sort=updated") {
                    header("User-Agent", "MyCodeCalendar-Android/1.0")
                    header("Accept", "application/vnd.github.v3+json")
                }.body()
            response
        }
    }

    /**
     * Fetches daily contribution data for a GitHub user via the Jogruber contributions API.
     * This is a community-maintained service that scrapes GitHub contribution graphs.
     * Endpoint: GET https://github-contributions-api.jogruber.de/v4/{username}
     *
     * Response shape: { "contributions": [{ "date": "2024-01-01", "count": 3, "level": 2 }, ...] }
     */
    suspend fun fetchGitHubDailyContributions(username: String): Result<List<GitHubContributionDayDto>> {
        return runCatching {
            val response: GitHubContributionsResponseDto =
                client.get("https://github-contributions-api.jogruber.de/v4/$username").body()
            response.contributions
        }
    }

    // ── LEETCODE GRAPHQL ─────────────────────────────────────────────────────

    /**
     * Fetches LeetCode user stats via the official LeetCode GraphQL API.
     *
     * This replaces the old unreliable Heroku-based community endpoint.
     * No API key is required; LeetCode accepts unauthenticated GraphQL queries
     * for public profile data.
     *
     * Returns a [LeetCodeStatsSummary] with solved counts, ranking, and contest info.
     */
    suspend fun fetchLeetCodeUserStats(username: String): Result<LeetCodeStatsSummary> {
        return runCatching {
            val query = """
                query getUserProfile(${'$'}username: String!) {
                  matchedUser(username: ${'$'}username) {
                    username
                    submitStats {
                      acSubmissionNum {
                        difficulty
                        count
                        submissions
                      }
                    }
                    profile {
                      ranking
                      reputation
                      realName
                    }
                    badges {
                      name
                    }
                  }
                  userContestRanking(username: ${'$'}username) {
                    attendedContestsCount
                    rating
                    globalRanking
                    totalParticipants
                    topPercentage
                  }
                }
            """.trimIndent()

            val requestBody = LeetCodeGraphQLRequest(
                query = query,
                variables = LeetCodeGraphQLVariables(username = username)
            )

            val response: LeetCodeGraphQLResponse = client.post("https://leetcode.com/graphql") {
                contentType(ContentType.Application.Json)
                header("User-Agent", "Mozilla/5.0 (Android; MyCodeCalendar)")
                header("Referer", "https://leetcode.com/")
                header("Origin", "https://leetcode.com")
                setBody(requestBody)
            }.body()

            // Check for GraphQL errors
            if (!response.errors.isNullOrEmpty()) {
                throw Exception("LeetCode GraphQL error: ${response.errors.first().message}")
            }

            val matchedUser = response.data?.matchedUser
                ?: throw Exception("LeetCode user '$username' not found")

            // Parse submission counts
            val submissionCounts = matchedUser.submitStats?.acSubmissionNum ?: emptyList()
            val totalSolved = submissionCounts.find { it.difficulty == "All" }?.count ?: 0
            val easySolved = submissionCounts.find { it.difficulty == "Easy" }?.count ?: 0
            val mediumSolved = submissionCounts.find { it.difficulty == "Medium" }?.count ?: 0
            val hardSolved = submissionCounts.find { it.difficulty == "Hard" }?.count ?: 0

            val ranking = matchedUser.profile?.ranking ?: Int.MAX_VALUE
            val contestRanking = response.data?.userContestRanking

            LeetCodeStatsSummary(
                totalSolved = totalSolved,
                easySolved = easySolved,
                mediumSolved = mediumSolved,
                hardSolved = hardSolved,
                ranking = ranking,
                contestRating = contestRanking?.rating,
                contestsAttended = contestRanking?.attendedContestsCount ?: 0,
                contestGlobalRank = contestRanking?.globalRanking
            )
        }
    }

    // ── EXISTING COMPATIBILITY METHODS ────────────────────────────────────────
    // These are used by ContestRepositoryImpl (Room-backed) — kept for compatibility.

    suspend fun getContests(platform: String? = null, status: String? = null): Result<List<ContestDto>> {
        return runCatching {
            val response: ApiResponse<List<ContestDto>> = client.get("$baseUrl/contests") {
                parameter("platform", platform)
                parameter("status", status)
            }.body()
            response.data ?: emptyList()
        }
    }

    suspend fun getPlatformStats(platform: String, username: String): Result<PlatformStatsDto> {
        return runCatching {
            val response: ApiResponse<PlatformStatsDto> =
                client.get("$baseUrl/platforms/$platform/stats/$username").body()
            response.data
                ?: throw Exception(response.error?.message ?: "Stats not found for $username on $platform")
        }
    }

    suspend fun getResources(category: String? = null): Result<List<ResourceDto>> {
        return runCatching {
            val response: ApiResponse<List<ResourceDto>> = client.get("$baseUrl/resources") {
                parameter("category", category)
            }.body()
            response.data ?: emptyList()
        }
    }
}
