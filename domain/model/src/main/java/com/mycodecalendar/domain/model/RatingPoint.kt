package com.mycodecalendar.domain.model

import java.time.Instant

data class RatingPoint(
    val timestamp: Instant,
    val rating: Int,
    val contestId: String,
    val contestName: String
)
