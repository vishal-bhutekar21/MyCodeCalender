package com.mycodecalendar.core.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── KONTESTS API DTO ─────────────────────────────────────────────────────────

@Serializable
data class KontestApiDto(
    val name: String = "",
    val url: String = "",
    @SerialName("start_time") val startTime: String = "",
    @SerialName("end_time") val endTime: String = "",
    val duration: String = "0",
    val site: String = "",
    @SerialName("in_24_hours") val in24Hours: String = "No",
    val status: String = ""
)

// ── CODEFORCES API DTOS ───────────────────────────────────────────────────────

@Serializable
data class CodeforcesResponseDto<T>(
    val status: String = "",
    val result: T? = null,
    val comment: String? = null
)

@Serializable
data class CodeforcesUserDto(
    val handle: String = "",
    val rating: Int? = null,
    val maxRating: Int? = null,
    val rank: String? = null,
    val maxRank: String? = null,
    val avatar: String? = null,
    val titlePhoto: String? = null
)

@Serializable
data class CodeforcesContestDto(
    val id: Int = 0,
    val name: String = "",
    val type: String = "CF",
    val phase: String = "",
    val frozen: Boolean = false,
    val durationSeconds: Long = 0L,
    val startTimeSeconds: Long? = null
)

@Serializable
data class CodeforcesRatingPointDto(
    val contestId: Int = 0,
    val contestName: String = "",
    val rank: Int = 0,
    val ratingUpdateTimeSeconds: Long = 0L,
    val oldRating: Int = 0,
    val newRating: Int = 0
)

@Serializable
data class CodeforcesSubmissionDto(
    val id: Long = 0L,
    val contestId: Int? = null,
    val creationTimeSeconds: Long = 0L,
    val problem: CodeforcesProblemDto? = null,
    val verdict: String? = null
)

@Serializable
data class CodeforcesProblemDto(
    val contestId: Int? = null,
    val index: String = "",
    val name: String = "",
    val rating: Int? = null,
    val tags: List<String> = emptyList()
)

// ── CODECHEF API DTOS ─────────────────────────────────────────────────────────

@Serializable
data class CodeChefApiResponseDto(
    val success: Boolean = false,
    val profile: String? = null,
    val name: String? = null,
    val currentRating: Int? = null,
    val highestRating: Int? = null,
    val stars: String? = null,
    val globalRank: Int? = null,
    val countryRank: Int? = null,
    val countryName: String? = null,
    val fullySolved: CodeChefSolvedCountDto? = null,
    val partiallySolved: CodeChefSolvedCountDto? = null
)

@Serializable
data class CodeChefSolvedCountDto(
    val count: Int = 0
)

// ── GITHUB API DTOS ───────────────────────────────────────────────────────────

@Serializable
data class GitHubUserDto(
    val login: String = "",
    val name: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("public_repos") val publicRepos: Int = 0,
    val followers: Int = 0,
    val following: Int = 0
)

@Serializable
data class GitHubRepoDto(
    val name: String = "",
    val description: String? = null,
    val language: String? = null,
    @SerialName("stargazers_count") val stargazersCount: Int = 0,
    @SerialName("forks_count") val forksCount: Int = 0,
    @SerialName("html_url") val htmlUrl: String = ""
)

@Serializable
data class GitHubContributionDayDto(
    val date: String = "",
    val count: Int = 0,
    val level: Int = 0
)

@Serializable
data class GitHubContributionsResponseDto(
    val contributions: List<GitHubContributionDayDto> = emptyList()
)

// ── LEETCODE GRAPHQL DTOS ────────────────────────────────────────────────────
// Uses the official LeetCode GraphQL endpoint at https://leetcode.com/graphql
// No API key required; standard browser headers are sufficient.

@Serializable
data class LeetCodeGraphQLRequest(
    val query: String,
    val variables: LeetCodeGraphQLVariables
)

@Serializable
data class LeetCodeGraphQLVariables(
    val username: String
)

@Serializable
data class LeetCodeGraphQLResponse(
    val data: LeetCodeGraphQLData? = null,
    val errors: List<LeetCodeGraphQLError>? = null
)

@Serializable
data class LeetCodeGraphQLData(
    val matchedUser: LeetCodeMatchedUser? = null,
    val userContestRanking: LeetCodeContestRanking? = null
)

@Serializable
data class LeetCodeMatchedUser(
    val username: String? = null,
    val submitStats: LeetCodeSubmitStats? = null,
    val profile: LeetCodeProfile? = null,
    val badges: List<LeetCodeBadge>? = null
)

@Serializable
data class LeetCodeSubmitStats(
    val acSubmissionNum: List<LeetCodeSubmissionCount>? = null
)

@Serializable
data class LeetCodeSubmissionCount(
    val difficulty: String = "",   // "All", "Easy", "Medium", "Hard"
    val count: Int = 0,
    val submissions: Int = 0
)

@Serializable
data class LeetCodeProfile(
    val ranking: Int? = null,
    val reputation: Int? = null,
    val starRating: Double? = null,
    val realName: String? = null,
    val userAvatar: String? = null
)

@Serializable
data class LeetCodeBadge(
    val name: String? = null
)

@Serializable
data class LeetCodeContestRanking(
    val attendedContestsCount: Int? = null,
    val rating: Double? = null,
    val globalRanking: Int? = null,
    val totalParticipants: Int? = null,
    val topPercentage: Double? = null
)

@Serializable
data class LeetCodeGraphQLError(
    val message: String = ""
)

// Parsed summary model for convenience
data class LeetCodeStatsSummary(
    val totalSolved: Int,
    val easySolved: Int,
    val mediumSolved: Int,
    val hardSolved: Int,
    val ranking: Int,
    val contestRating: Double?,
    val contestsAttended: Int,
    val contestGlobalRank: Int?
)

// ── EXISTING DTOS (for backward compatibility with ContestRepositoryImpl) ─────

@Serializable
data class ContestDto(
    val id: String,
    val platform: String,
    val providerContestId: String,
    val name: String,
    val description: String? = null,
    val officialUrl: String,
    val registrationUrl: String? = null,
    val startTimeUtc: String,
    val endTimeUtc: String,
    val durationSeconds: Int,
    val contestType: String? = null,
    val ratingType: String? = null,
    val status: String,
    val lastFetchedAt: String
)

@Serializable
data class PlatformStatsDto(
    val platform: String,
    val username: String,
    val rating: Int? = null,
    val highestRating: Int? = null,
    val rank: String? = null,
    val globalRank: Int? = null,
    val solved: Int? = null,
    val easySolved: Int? = null,
    val mediumSolved: Int? = null,
    val hardSolved: Int? = null,
    val currentStreak: Int? = null,
    val longestStreak: Int? = null,
    val contestCount: Int? = null,
    val badge: String? = null,
    val division: String? = null,
    val lastUpdated: String
)

@Serializable
data class ResourceDto(
    val id: String,
    val title: String,
    val description: String,
    val creator: String,
    val url: String,
    val category: String,
    val platform: String,
    val duration: String,
    val priority: Int,
    val thumbnailUrl: String? = null,
    val publishedAt: String
)

@Serializable
data class ApiResponse<T>(
    val data: T? = null,
    val meta: Map<String, String> = emptyMap(),
    val error: ApiError? = null
)

@Serializable
data class ApiError(
    val code: String,
    val message: String
)
