package com.mycodecalendar.core.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class RemoteDataSource(
    private val baseUrl: String = "https://api.mycodecalendar.com/v1"
) {
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                coerceInputValues = true
            })
        }
    }

    // ── LIVE CONTESTS ─────────────────────────────────────────────────────────

    suspend fun fetchKontestsContests(): Result<List<KontestApiDto>> {
        return try {
            val response: List<KontestApiDto> = client.get("https://kontests.net/api/v1/all").body()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchCodeforcesContests(): Result<List<CodeforcesContestDto>> {
        return try {
            val response: CodeforcesResponseDto<List<CodeforcesContestDto>> =
                client.get("https://codeforces.com/api/contest.list?gym=false").body()
            if (response.status == "OK" && response.result != null) {
                Result.success(response.result)
            } else {
                Result.failure(Exception(response.comment ?: "Codeforces API error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── CODEFORCES USER STATS & RATING ─────────────────────────────────────────

    suspend fun fetchCodeforcesUserInfo(handle: String): Result<CodeforcesUserDto> {
        return try {
            val response: CodeforcesResponseDto<List<CodeforcesUserDto>> =
                client.get("https://codeforces.com/api/user.info?handles=$handle").body()
            val user = response.result?.firstOrNull()
            if (response.status == "OK" && user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception(response.comment ?: "User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchCodeforcesRatingHistory(handle: String): Result<List<CodeforcesRatingPointDto>> {
        return try {
            val response: CodeforcesResponseDto<List<CodeforcesRatingPointDto>> =
                client.get("https://codeforces.com/api/user.rating?handle=$handle").body()
            if (response.status == "OK" && response.result != null) {
                Result.success(response.result)
            } else {
                Result.failure(Exception(response.comment ?: "Rating history not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── GITHUB USER & DAILY CONTRIBUTIONS (DAY-WISE) ──────────────────────────

    suspend fun fetchGitHubUser(username: String): Result<GitHubUserDto> {
        return try {
            val response: GitHubUserDto = client.get("https://api.github.com/users/$username") {
                header("User-Agent", "MyCodeCalendar-Android")
            }.body()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchGitHubUserRepos(username: String): Result<List<GitHubRepoDto>> {
        return try {
            val response: List<GitHubRepoDto> = client.get("https://api.github.com/users/$username/repos?per_page=100&sort=updated") {
                header("User-Agent", "MyCodeCalendar-Android")
            }.body()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchGitHubDailyContributions(username: String): Result<List<GitHubContributionDayDto>> {
        return try {
            val response: GitHubContributionsResponseDto =
                client.get("https://github-contributions-api.jogruber.de/v4/$username").body()
            Result.success(response.contributions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── LEETCODE USER STATS ────────────────────────────────────────────────────

    suspend fun fetchLeetCodeUserStats(username: String): Result<LeetCodeStatsDto> {
        return try {
            val response: LeetCodeStatsDto =
                client.get("https://leetcode-stats-api.herokuapp.com/$username").body()
            if (response.status == "success") {
                Result.success(response)
            } else {
                Result.failure(Exception(response.message ?: "LeetCode user stats error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── EXISTING COMPATIBILITY METHODS ─────────────────────────────────────────

    suspend fun getContests(platform: String? = null, status: String? = null): Result<List<ContestDto>> {
        return try {
            val response: ApiResponse<List<ContestDto>> = client.get("$baseUrl/contests") {
                parameter("platform", platform)
                parameter("status", status)
            }.body()
            Result.success(response.data ?: emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPlatformStats(platform: String, username: String): Result<PlatformStatsDto> {
        return try {
            val response: ApiResponse<PlatformStatsDto> = client.get("$baseUrl/platforms/$platform/stats/$username").body()
            if (response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.error?.message ?: "Stats not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getResources(category: String? = null): Result<List<ResourceDto>> {
        return try {
            val response: ApiResponse<List<ResourceDto>> = client.get("$baseUrl/resources") {
                parameter("category", category)
            }.body()
            Result.success(response.data ?: emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
