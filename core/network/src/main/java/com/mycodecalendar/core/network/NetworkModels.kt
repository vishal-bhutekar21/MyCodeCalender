package com.mycodecalendar.core.network

import kotlinx.serialization.Serializable

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
