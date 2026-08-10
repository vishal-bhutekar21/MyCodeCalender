package com.mycodecalendar.domain.model

import java.time.Instant

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
    val lastUpdated: Instant
)
