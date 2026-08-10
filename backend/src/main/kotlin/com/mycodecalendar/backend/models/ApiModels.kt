package com.mycodecalendar.backend.models

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
data class ErrorResponse(
    val code: String,
    val message: String
)

@Serializable
data class ApiResponse<T>(
    val data: T? = null,
    val meta: Map<String, String>? = null,
    val error: ErrorResponse? = null
)
