package com.mycodecalendar.backend.providers

import java.time.Instant

data class ContestResult(
    val providerContestId: String,
    val name: String,
    val description: String? = null,
    val startTime: Instant,
    val endTime: Instant,
    val durationSeconds: Int,
    val url: String,
    val contestType: String? = "Standard",
    val ratingType: String? = "Rated",
    val status: String = "UPCOMING"
)

interface ContestProvider {
    val platformName: String
    val isEnabled: Boolean get() = true
    suspend fun getUpcomingContests(): Result<List<ContestResult>>
}
