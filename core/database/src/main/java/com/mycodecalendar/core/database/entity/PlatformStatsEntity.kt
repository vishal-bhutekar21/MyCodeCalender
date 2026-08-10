package com.mycodecalendar.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import java.time.Instant

@Entity(
    tableName = "platform_stats",
    primaryKeys = ["platform", "username"],
    indices = [
        Index(value = ["platform", "username"], unique = true)
    ]
)
data class PlatformStatsEntity(
    val platform: String,
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
