package com.mycodecalendar.domain.model

import java.time.Instant

enum class ContestStatus {
    UPCOMING, LIVE, ENDED
}

data class Contest(
    val id: String,
    val providerContestId: String,
    val platform: Platform,
    val name: String,
    val officialUrl: String,
    val registrationUrl: String?,
    val startTimeUtc: Instant,
    val endTimeUtc: Instant,
    val durationSeconds: Long,
    val contestType: String?,
    val ratingType: String?,
    val status: ContestStatus,
    val lastFetchedAt: Instant
)
