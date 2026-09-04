package com.mycodecalendar.domain.model

/**
 * Domain entity representing a daily coding challenge (e.g. LeetCode Problem of the Day).
 */
data class DailyProblem(
    val title: String,
    val titleSlug: String,
    val difficulty: String,
    val date: String,
    val link: String,
    val platform: Platform = Platform.LEETCODE,
    val topicTags: List<String> = emptyList()
)
