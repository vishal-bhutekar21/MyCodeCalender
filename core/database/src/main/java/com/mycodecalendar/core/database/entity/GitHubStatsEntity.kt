package com.mycodecalendar.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Room entity storing a fully fetched GitHub user profile.
 *
 * Data is serialized to JSON for complex fields (topLanguages, reposJson)
 * and restored on read. This gives us instant cold-start data without
 * waiting for the GitHub REST API (60 req/hr unauthenticated rate limit).
 */
@Entity(tableName = "github_stats")
data class GitHubStatsEntity(
    @PrimaryKey val username: String,
    val name: String?,
    val avatarUrl: String?,
    val bio: String?,
    val publicRepos: Int,
    val totalStars: Int,
    val totalContributionsThisYear: Int,
    val currentContributionStreak: Int,
    val longestContributionStreak: Int,
    val followers: Int,
    val following: Int,
    /** JSON-encoded List<String> of top programming languages */
    val topLanguagesJson: String,
    /** JSON-encoded List<GitHubRepoEntity> items for the repository list */
    val reposJson: String,
    /** JSON-encoded List<DailyContributionEntity> items for the contribution heatmap */
    val dailyContributionsJson: String,
    val lastUpdated: Instant
)
