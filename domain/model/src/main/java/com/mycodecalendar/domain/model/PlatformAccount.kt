package com.mycodecalendar.domain.model

import java.time.Instant

data class PlatformAccount(
    val id: String,
    val platform: Platform,
    val username: String,
    val displayName: String?,
    val isEnabled: Boolean,
    val lastSyncedAt: Instant?,
    val syncStatus: String?
)
