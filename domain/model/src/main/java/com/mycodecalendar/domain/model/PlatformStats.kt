package com.mycodecalendar.domain.model

import java.time.Instant

data class PlatformStats(
    val platform: Platform,
    val username: String,
    val rating: Int?,
    val highestRating: Int?,
    val rank: String?,
    val globalRank: Int?,
    val solved: Int?,
    val easySolved: Int?,
    val mediumSolved: Int?,
    val hardSolved: Int?,
    val currentStreak: Int?,
    val longestStreak: Int?,
    val contestCount: Int?,
    val badge: String?,
    val division: String?,
    val lastUpdated: Instant?
)
