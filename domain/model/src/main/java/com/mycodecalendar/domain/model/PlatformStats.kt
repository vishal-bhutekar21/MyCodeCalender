package com.mycodecalendar.domain.model

import java.time.Instant

data class PlatformStats(
    val platform: Platform,
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
    val lastUpdated: Instant? = null
)
