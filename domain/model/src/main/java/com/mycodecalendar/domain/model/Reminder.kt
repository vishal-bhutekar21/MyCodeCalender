package com.mycodecalendar.domain.model

import java.time.Instant

data class Reminder(
    val id: String,
    val contestId: String,
    val offsetMinutes: Int,
    val scheduledAt: Instant,
    val enabled: Boolean,
    val notificationId: Int
)
