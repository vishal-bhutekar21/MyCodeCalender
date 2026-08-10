package com.mycodecalendar.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "reminders",
    indices = [
        Index(value = ["contestId"])
    ]
)
data class ReminderEntity(
    @PrimaryKey
    val id: String,
    val contestId: String,
    val offsetMinutes: Int,
    val scheduledAt: Instant,
    val enabled: Boolean,
    val notificationId: Int
)
