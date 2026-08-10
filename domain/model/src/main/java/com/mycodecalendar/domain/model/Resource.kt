package com.mycodecalendar.domain.model

import java.time.Instant

data class Resource(
    val id: String,
    val title: String,
    val description: String?,
    val creator: String?,
    val url: String,
    val category: String,
    val platform: Platform?,
    val duration: String?,
    val priority: Int,
    val thumbnailUrl: String?,
    val publishedAt: Instant?
)
