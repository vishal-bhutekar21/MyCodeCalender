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

/**
 * PastContestRecord — Represents a user's past contest performance record.
 */
data class PastContestRecord(
    val id: String,
    val platform: Platform,
    val contestName: String,
    val dateText: String,
    val oldRating: Int,
    val newRating: Int,
    val ratingDelta: Int,
    val solvedCount: Int,
    val totalProblems: Int,
    val rankText: String,
    val contestUrl: String
)

/**
 * StreakInfo — Represents persistent daily app login streak state.
 */
data class StreakInfo(
    val currentStreak: Int,
    val isNewDayIncrement: Boolean,
    val lastOpenDateText: String,
    /** Set of date strings ("yyyy-MM-dd") when the app was opened */
    val activeDates: Set<String> = emptySet()
)
