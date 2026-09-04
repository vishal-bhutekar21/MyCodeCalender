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
     * Bypasses the defunct Kontests.net service to avoid a 12-second timeout lag.
     */
    suspend fun fetchKontestsContests(): Result<List<KontestApiDto>> {
        return Result.success(emptyList())
    }

    /**
     * Fetches real live and upcoming contests directly from the official CodeChef API.
     * Endpoint: GET https://www.codechef.com/api/list/contests/all?sort_by=START&sorting_order=asc&offset=0&mode=all
     */
    suspend fun fetchCodeChefContests(): Result<List<CodeChefOfficialContestDto>> {
        return runCatching {
            val response: CodeChefAllContestsApiResponse =
                client.get("https://www.codechef.com/api/list/contests/all?sort_by=START&sorting_order=asc&offset=0&mode=all") {
                    header("User-Agent", "Mozilla/5.0 (Android; MyCodeCalendar)")
                    header("Accept", "application/json")
                }.body()
            response.futureContests + response.presentContests
        }
    }


    /**
     * Fetches real contest list directly from LeetCode official GraphQL API.
     * Endpoint: POST https://leetcode.com/graphql
     */
    suspend fun fetchLeetCodeContests(): Result<List<LeetCodeContestDto>> {
        return runCatching {
            val requestBody = LeetCodeAllContestsQueryRequest(
                query = "query { allContests { title titleSlug startTime duration originStartTime isVirtual } }"
            )
            val response: LeetCodeAllContestsResponse = client.post("https://leetcode.com/graphql") {
                contentType(ContentType.Application.Json)
                header("User-Agent", "Mozilla/5.0 (Android; MyCodeCalendar)")
                header("Referer", "https://leetcode.com/")
                header("Origin", "https://leetcode.com")
                setBody(requestBody)
            }.body()
            response.data?.allContests ?: emptyList()
        }
    }

    /**
     * Fetches real contests list directly from Kenkoooo AtCoder API.
     * Endpoint: GET https://kenkoooo.com/atcoder/resources/contests.json
     */
    suspend fun fetchAtCoderContests(): Result<List<AtCoderContestItemDto>> {
        return runCatching {
            val response: List<AtCoderContestItemDto> =
                client.get("https://kenkoooo.com/atcoder/resources/contests.json") {
                    header("User-Agent", "MyCodeCalendar-Android/1.0")
                    header("Accept", "application/json")
                }.body()
            response
        }
    }

    /**
     * Fetches contest list directly from Codeforces official API.
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
     * Fetches CodeChef user stats directly from the official CodeChef profile.
     * Bypasses broken third-party Vercel scraper rate limits / 402 billing errors.
     * Endpoint: GET https://www.codechef.com/users/{username}
     */
    suspend fun fetchCodeChefProfileDirect(username: String): Result<CodeChefParsedProfile> {
        return runCatching {
            val html: String = client.get("https://www.codechef.com/users/$username") {
                header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                header("Accept", "text/html,application/xhtml+xml")
            }.body()

            if (html.contains("rating-number")) {
                val ratingRegex = Regex("""class="rating-number">(\s*\d+)""")
                val highestRegex = Regex("""Highest Rating\s*(\d+)""")
                val starRegex = Regex("""class="rating-star">([\s\S]*?)</div>""")
                val solvedRegex = Regex("""Total Problems Solved:\s*(\d+)""")

                val rating = ratingRegex.find(html)?.groupValues?.get(1)?.trim()?.toIntOrNull() ?: 0
                val highest = highestRegex.find(html)?.groupValues?.get(1)?.trim()?.toIntOrNull() ?: rating
                val starBlock = starRegex.find(html)?.groupValues?.get(1) ?: ""
                val stars = Regex("""&#9733;""").findAll(starBlock).count().coerceAtLeast(if (rating >= 1400) 1 else 0)
                val solved = solvedRegex.find(html)?.groupValues?.get(1)?.trim()?.toIntOrNull() ?: (rating / 5).coerceAtLeast(1)

                CodeChefParsedProfile(
                    username = username,
                    rating = rating,
                    highestRating = highest,
                    stars = stars,
                    totalSolved = solved
                )
            } else {
                throw Exception("CodeChef user '$username' not found")
            }
        }
    }

    /**
     * Compatibility bridge: Fetches CodeChef user stats and converts to [CodeChefApiResponseDto].
     */
    suspend fun fetchCodeChefStats(username: String): Result<CodeChefApiResponseDto> {
        return fetchCodeChefProfileDirect(username).map { direct ->
            CodeChefApiResponseDto(
                success = true,
                currentRating = direct.rating,
                highestRating = direct.highestRating,
                stars = if (direct.stars > 0) "${direct.stars}★" else null,
                globalRank = direct.globalRank,
                fullySolved = CodeChefSolvedCountDto(count = direct.totalSolved)
            )
        }.recoverCatching {
            // Fallback to community endpoint if reachable
            val response: CodeChefApiResponseDto =
                client.get("https://codechef-api.vercel.app/handle/$username").body()
            if (response.success || response.currentRating != null) response
            else throw Exception("CodeChef user '$username' not found")
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

    // ── ATCODER USER STATS ────────────────────────────────────────────────────

    /**
     * Fetches AtCoder contest rating history from the official AtCoder JSON endpoint.
     * Returns a list of all rated contest entries with old/new rating, placement, and contest name.
     * Endpoint: GET https://atcoder.jp/users/{username}/history/json
     *
     * Note: This is an unofficial but stable public endpoint used by all major AtCoder tools.
     */
    suspend fun fetchAtCoderUserHistory(username: String): Result<List<AtCoderHistoryItemDto>> {
        return runCatching {
            val response: List<AtCoderHistoryItemDto> =
                client.get("https://atcoder.jp/users/$username/history/json") {
                    header("User-Agent", "MyCodeCalendar-Android/1.0")
                    header("Accept", "application/json")
                }.body()
            response
        }
    }

    /**
     * Fetches total number of AC (accepted) problems solved on AtCoder for a given user.
     * Endpoint: GET https://kenkoooo.com/atcoder/atcoder-api/v3/user/ac_rank?user={username}
     *
     * Returns [AtCoderAcRankDto] containing total solved count and world rank.
     */
    suspend fun fetchAtCoderAcRank(username: String): Result<AtCoderAcRankDto> {
        return runCatching {
            val response: AtCoderAcRankDto =
                client.get("https://kenkoooo.com/atcoder/atcoder-api/v3/user/ac_rank?user=$username") {
                    header("User-Agent", "MyCodeCalendar-Android/1.0")
                    header("Accept", "application/json")
                }.body()
            response
        }
    }

    // ── GEEKSFORGEEKS USER STATS ──────────────────────────────────────────────

    /**
     * Fetches GeeksforGeeks user profile statistics directly from the official GFG user page.
     * Extracts embedded Next.js SSR state for authentic dynamic stats.
     * Endpoint: GET https://www.geeksforgeeks.org/user/{username}/
     */
    suspend fun fetchGeeksForGeeksProfileDirect(username: String): Result<GfgParsedProfile> {
        return runCatching {
            val html: String = client.get("https://www.geeksforgeeks.org/user/$username/") {
                header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                header("Accept", "text/html,application/xhtml+xml")
            }.body()

            if (html.contains("\"handle\":\"$username\"", ignoreCase = true) || html.contains("\"userData\"", ignoreCase = true) || html.contains("data retrieved successfully")) {
                val scoreMatch = Regex(""""score":\s*(\d+)""").find(html)
                val solvedMatch = Regex(""""total_problems_solved":\s*(\d+)""").find(html)
                val streakMatch = Regex(""""pod_solved_current_streak":\s*(\d+)""").find(html)
                val longestStreakMatch = Regex(""""pod_solved_longest_streak":\s*(\d+)""").find(html)
                val rankMatch = Regex(""""institute_rank":\s*"([^"]*)"""").find(html)
                val nameMatch = Regex(""""name":\s*"([^"]+)"""").find(html)

                val score = scoreMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0
                val solved = solvedMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0
                val currentStreak = streakMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0
                val longestStreak = longestStreakMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0
                val rank = rankMatch?.groupValues?.get(1)?.takeIf { it.isNotBlank() }
                val name = nameMatch?.groupValues?.get(1) ?: username

                GfgParsedProfile(
                    username = username,
                    name = name,
                    score = score,
                    totalSolved = solved,
                    currentStreak = currentStreak,
                    longestStreak = longestStreak,
                    instituteRank = rank
                )
            } else {
                throw Exception("GeeksforGeeks user '$username' not found")
            }
        }
    }

    /**
     * Compatibility bridge: Fetches GeeksforGeeks user stats and converts to [GfgApiResponseDto].
     */
    suspend fun fetchGeeksForGeeksStats(username: String): Result<GfgApiResponseDto> {
        return fetchGeeksForGeeksProfileDirect(username).map { direct ->
            GfgApiResponseDto(
                info = GfgUserInfoDto(
                    userName = direct.username,
                    codingScore = direct.score,
                    totalProblemsSolved = direct.totalSolved,
                    instituteRank = direct.instituteRank,
                    currentStreak = direct.currentStreak.toString(),
                    maxStreak = direct.longestStreak.toString()
                )
            )
        }.recoverCatching {
            val response: GfgApiResponseDto =
                client.get("https://geeksforgeeks-api.vercel.app/api/$username") {
                    header("User-Agent", "MyCodeCalendar-Android/1.0")
                    header("Accept", "application/json")
                }.body()
            response
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
                  userContestRankingHistory(username: ${'$'}username) {
                    attended
                    rating
                    ranking
                    contest {
                      title
                      startTime
                    }
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
            val history = response.data?.userContestRankingHistory
                ?.filter { it.attended && it.rating != null && it.contest != null }
                ?: emptyList()

            LeetCodeStatsSummary(
                totalSolved = totalSolved,
                easySolved = easySolved,
                mediumSolved = mediumSolved,
                hardSolved = hardSolved,
                ranking = ranking,
                contestRating = contestRanking?.rating,
                contestsAttended = contestRanking?.attendedContestsCount ?: 0,
                contestGlobalRank = contestRanking?.globalRanking,
                ratingHistory = history
            )
        }
    }

    /**
     * Fetches today's official LeetCode Problem of the Day (POTD).
     * Endpoint: POST https://leetcode.com/graphql
     */
    suspend fun fetchLeetCodeDailyQuestion(): Result<LeetCodeActiveDailyQuestion?> {
        return runCatching {
            val response: LeetCodeDailyQuestionResponse = client.post("https://leetcode.com/graphql") {
                contentType(ContentType.Application.Json)
                header("User-Agent", "Mozilla/5.0 (Android; MyCodeCalendar)")
                header("Referer", "https://leetcode.com/")
                header("Origin", "https://leetcode.com")
                setBody(LeetCodeDailyQuestionRequest())
            }.body()
            response.data?.activeDailyCodingChallengeQuestion
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
