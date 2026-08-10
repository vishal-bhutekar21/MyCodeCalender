package com.mycodecalendar.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "platform_accounts",
    indices = [
        Index(value = ["platform", "username"], unique = true)
    ]
)
data class PlatformAccountEntity(
    @PrimaryKey
    val id: String,
    val platform: String,
    val username: String,
    val displayName: String?,
    val isEnabled: Boolean,
    val lastSyncedAt: Instant?,
    val syncStatus: String?
)
