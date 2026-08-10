package com.mycodecalendar.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "contests",
    indices = [
        Index(value = ["platform", "startTimeUtc"]),
        Index(value = ["status", "startTimeUtc"])
    ]
)
data class ContestEntity(
    @PrimaryKey
    val id: String,
    val providerContestId: String,
    val platform: String,
    val name: String,
    val officialUrl: String,
    val registrationUrl: String?,
    val startTimeUtc: Instant,
    val endTimeUtc: Instant,
    val durationSeconds: Long,
    val contestType: String?,
    val ratingType: String?,
    val status: String,
    val lastFetchedAt: Instant
)
