package com.mycodecalendar.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "sync_states")
data class SyncStateEntity(
    @PrimaryKey
    val key: String,
    val lastSyncedAt: Instant?,
    val lastError: String?
)
