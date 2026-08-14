package com.mycodecalendar.data.repository

import com.mycodecalendar.core.database.entity.ContestEntity
import com.mycodecalendar.core.database.entity.GitHubStatsEntity
import com.mycodecalendar.core.database.entity.PlatformStatsEntity
import com.mycodecalendar.core.database.entity.RatingHistoryEntity
import com.mycodecalendar.domain.model.Contest
import com.mycodecalendar.domain.model.ContestStatus
import com.mycodecalendar.domain.model.DailyContribution
import com.mycodecalendar.domain.model.GitHubRepo
import com.mycodecalendar.domain.model.GitHubStats
import com.mycodecalendar.domain.model.Platform
import com.mycodecalendar.domain.model.PlatformStats
import com.mycodecalendar.domain.model.RatingPoint
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// ── CONTEST ───────────────────────────────────────────────────────────────────

fun Contest.toEntity(): ContestEntity = ContestEntity(
    id = id,
    providerContestId = providerContestId,
    platform = platform.name,
    name = name,
    officialUrl = officialUrl,
    registrationUrl = registrationUrl,
    startTimeUtc = startTimeUtc,
    endTimeUtc = endTimeUtc,
    durationSeconds = durationSeconds,
    contestType = contestType,
    ratingType = ratingType,
    status = status.name,
    lastFetchedAt = lastFetchedAt
)

fun ContestEntity.toDomain(): Contest = Contest(
    id = id,
    providerContestId = providerContestId,
    platform = runCatching { Platform.valueOf(platform) }.getOrDefault(Platform.CODEFORCES),
    name = name,
    officialUrl = officialUrl,
    registrationUrl = registrationUrl,
    startTimeUtc = startTimeUtc,
    endTimeUtc = endTimeUtc,
    durationSeconds = durationSeconds,
    contestType = contestType,
    ratingType = ratingType,
    status = runCatching { ContestStatus.valueOf(status) }.getOrDefault(ContestStatus.UPCOMING),
    lastFetchedAt = lastFetchedAt
)

// ── PLATFORM STATS ────────────────────────────────────────────────────────────

fun PlatformStats.toEntity(): PlatformStatsEntity = PlatformStatsEntity(
    platform = platform.name,
    username = username,
    rating = rating,
    highestRating = highestRating,
    rank = rank,
    globalRank = globalRank,
    solved = solved,
    easySolved = easySolved,
    mediumSolved = mediumSolved,
    hardSolved = hardSolved,
    currentStreak = currentStreak,
    longestStreak = longestStreak,
    contestCount = contestCount,
    badge = badge,
    division = division,
    lastUpdated = lastUpdated
)

fun PlatformStatsEntity.toDomain(): PlatformStats = PlatformStats(
    platform = runCatching { Platform.valueOf(platform) }.getOrDefault(Platform.CODEFORCES),
    username = username,
    rating = rating,
    highestRating = highestRating,
    rank = rank,
    globalRank = globalRank,
    solved = solved,
    easySolved = easySolved,
    mediumSolved = mediumSolved,
    hardSolved = hardSolved,
    currentStreak = currentStreak,
    longestStreak = longestStreak,
    contestCount = contestCount,
    badge = badge,
    division = division,
    lastUpdated = lastUpdated
)

// ── RATING HISTORY ────────────────────────────────────────────────────────────

fun RatingPoint.toEntity(platformName: String, username: String): RatingHistoryEntity =
    RatingHistoryEntity(
        platform = platformName,
        username = username,
        timestamp = timestamp,
        rating = rating,
        contestId = contestId,
        contestName = contestName
    )

fun RatingHistoryEntity.toDomain(): RatingPoint = RatingPoint(
    timestamp = timestamp,
    rating = rating,
    contestId = contestId,
    contestName = contestName
)

// ── GITHUB STATS ──────────────────────────────────────────────────────────────

/**
 * Lightweight JSON-serializable representation of [GitHubRepo] for Room storage.
 * Only stores the core fields needed to reconstruct the UI cards.
 */
@Serializable
data class GitHubRepoJson(
    val name: String,
    val description: String? = null,
    val language: String? = null,
    val stars: Int = 0,
    val forks: Int = 0,
    val url: String = "",
    val openIssues: Int = 0,
    val homepage: String? = null,
    val topics: List<String> = emptyList()
)

/**
 * Lightweight JSON-serializable representation of [DailyContribution] for Room storage.
 */
@Serializable
data class DailyContributionJson(
    val date: String,
    val count: Int,
    val level: Int,
    val dayOfWeek: String
)

/**
 * Converts a live [GitHubStats] domain object to its Room [GitHubStatsEntity] representation.
 * Complex list fields are JSON-encoded for flat storage.
 */
fun GitHubStats.toEntity(json: Json): GitHubStatsEntity = GitHubStatsEntity(
    username = username,
    name = name,
    avatarUrl = avatarUrl,
    bio = null,
    publicRepos = publicRepos,
    totalStars = totalStars,
    totalContributionsThisYear = totalContributionsThisYear,
    currentContributionStreak = currentContributionStreak,
    longestContributionStreak = longestContributionStreak,
    followers = followers,
    following = following,
    topLanguagesJson = json.encodeToString(topLanguages),
    reposJson = json.encodeToString(repos.map { r ->
        GitHubRepoJson(
            name = r.name,
            description = r.description,
            language = r.language,
            stars = r.stars,
            forks = r.forks,
            url = r.url,
            openIssues = r.openIssues,
            homepage = r.homepage,
            topics = r.topics
        )
    }),
    dailyContributionsJson = json.encodeToString(dailyContributions.map { c ->
        DailyContributionJson(date = c.date, count = c.count, level = c.level, dayOfWeek = c.dayOfWeek)
    }),
    lastUpdated = lastUpdated
)

/**
 * Converts a cached [GitHubStatsEntity] back to the domain [GitHubStats] model.
 * JSON fields are decoded back to their typed list representations.
 */
fun GitHubStatsEntity.toDomain(json: Json): GitHubStats {
    val langs: List<String> = runCatching { json.decodeFromString<List<String>>(topLanguagesJson) }.getOrElse { emptyList() }
    val repoList: List<GitHubRepoJson> = runCatching { json.decodeFromString<List<GitHubRepoJson>>(reposJson) }.getOrElse { emptyList() }
    val contribs: List<DailyContributionJson> = runCatching { json.decodeFromString<List<DailyContributionJson>>(dailyContributionsJson) }.getOrElse { emptyList() }

    return GitHubStats(
        username = username,
        name = name,
        avatarUrl = avatarUrl,
        publicRepos = publicRepos,
        totalStars = totalStars,
        totalContributionsThisYear = totalContributionsThisYear,
        currentContributionStreak = currentContributionStreak,
        longestContributionStreak = longestContributionStreak,
        topLanguages = langs,
        followers = followers,
        following = following,
        dailyContributions = contribs.map { c: DailyContributionJson ->
            DailyContribution(date = c.date, count = c.count, level = c.level, dayOfWeek = c.dayOfWeek)
        },
        repos = repoList.map { r: GitHubRepoJson ->
            GitHubRepo(
                name = r.name,
                description = r.description,
                language = r.language,
                stars = r.stars,
                forks = r.forks,
                url = r.url,
                openIssues = r.openIssues,
                homepage = r.homepage,
                topics = r.topics
            )
        },
        lastUpdated = lastUpdated
    )
}

