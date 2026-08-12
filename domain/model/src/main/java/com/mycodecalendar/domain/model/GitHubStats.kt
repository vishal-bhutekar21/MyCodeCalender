package com.mycodecalendar.domain.model

import java.time.Instant

data class DailyContribution(
    val date: String,       // e.g. "2026-08-12"
    val count: Int,        // e.g. 5
    val level: Int,        // 0 to 4 (heat level)
    val dayOfWeek: String  // e.g. "Wed"
)

data class GitHubStats(
    val username: String,
    val name: String?,
    val avatarUrl: String?,
    val publicRepos: Int,
    val totalStars: Int,
    val totalContributionsThisYear: Int,
    val currentContributionStreak: Int,
    val longestContributionStreak: Int,
    val topLanguages: List<String>,
    val followers: Int,
    val following: Int,
    val dailyContributions: List<DailyContribution> = emptyList(),
    val lastUpdated: Instant
)
