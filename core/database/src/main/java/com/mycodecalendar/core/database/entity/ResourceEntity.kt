package com.mycodecalendar.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "resources",
    indices = [
        Index(value = ["category", "priority"])
    ]
)
data class ResourceEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String?,
    val creator: String?,
    val url: String,
    val category: String,
    val platform: String?,
    val duration: String?,
    val priority: Int,
    val thumbnailUrl: String?,
    val publishedAt: Instant?
)
