package com.mycodecalendar.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import java.time.Instant

@Entity(
    tableName = "rating_history",
    primaryKeys = ["platform", "username", "timestamp"],
    indices = [
        Index(value = ["platform", "username", "timestamp"], unique = true)
    ]
)
data class RatingHistoryEntity(
    val platform: String,
    val username: String,
    val timestamp: Instant,
    val rating: Int,
    val contestId: String,
    val contestName: String
)
