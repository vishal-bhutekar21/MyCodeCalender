package com.mycodecalendar.data.repository

import com.mycodecalendar.core.database.entity.ContestEntity
import com.mycodecalendar.core.database.entity.PlatformStatsEntity
import com.mycodecalendar.core.database.entity.RatingHistoryEntity
import com.mycodecalendar.domain.model.Contest
import com.mycodecalendar.domain.model.ContestStatus
import com.mycodecalendar.domain.model.Platform
import com.mycodecalendar.domain.model.PlatformStats
import com.mycodecalendar.domain.model.RatingPoint

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
