package com.mycodecalendar.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "saved_contests")
data class SavedContestEntity(
    @PrimaryKey
    val contestId: String,
    val savedAt: Instant
)
