package com.mycodecalendar.data.repository

import com.mycodecalendar.domain.model.Contest
import com.mycodecalendar.domain.model.ContestStatus
import com.mycodecalendar.domain.model.GitHubStats
import com.mycodecalendar.domain.model.Platform
import com.mycodecalendar.domain.model.PlatformAccount
import com.mycodecalendar.domain.model.PlatformStats
import com.mycodecalendar.domain.model.RatingPoint
import com.mycodecalendar.domain.model.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.Instant

class FakeRepository {

    private val connectedPlatforms = MutableStateFlow<List<PlatformAccount>>(
        listOf(
            PlatformAccount(
                id = "1",
                platform = Platform.CODEFORCES,
                username = "tourist",
                displayName = "Gennady Korotkevich",
                isEnabled = true,
                lastSyncedAt = Instant.now().minusSeconds(600)
            ),
            PlatformAccount(
                id = "2",
                platform = Platform.LEETCODE,
                username = "neal_wu",
                displayName = "Neal Wu",
                isEnabled = true,
                lastSyncedAt = Instant.now().minusSeconds(1200)
            ),
            PlatformAccount(
                id = "3",
                platform = Platform.GITHUB,
                username = "torvalds",
                displayName = "Linus Torvalds",
                isEnabled = true,
                lastSyncedAt = Instant.now().minusSeconds(300)
            )
        )
    )

    private val fakeGitHubStats = MutableStateFlow(
        GitHubStats(
            username = "torvalds",
            name = "Linus Torvalds",
            avatarUrl = "https://avatars.githubusercontent.com/u/1024025",
            publicRepos = 48,
            totalStars = 185000,
            totalContributionsThisYear = 1420,
            currentContributionStreak = 24,
            longestContributionStreak = 150,
            topLanguages = listOf("C", "C++", "Kotlin", "Python", "Shell"),
            followers = 210000,
            following = 0,
            lastUpdated = Instant.now().minusSeconds(300)
        )
    )

    private val fakeStats = mapOf(
        Platform.CODEFORCES to PlatformStats(
            platform = Platform.CODEFORCES,
            username = "tourist",
            rating = 3842,
            highestRating = 3979,
            rank = "Legendary Grandmaster",
            globalRank = 1,
            solved = 2450,
            easySolved = 500,
            mediumSolved = 1100,
            hardSolved = 850,
            currentStreak = 42,
            longestStreak = 180,
            contestCount = 210,
            badge = "🔴 International Target",
            division = "Div. 1",
            lastUpdated = Instant.now().minusSeconds(600)
        ),
        Platform.LEETCODE to PlatformStats(
            platform = Platform.LEETCODE,
            username = "neal_wu",
            rating = 3320,
            highestRating = 3400,
            rank = "Guardian",
            globalRank = 4,
            solved = 1820,
            easySolved = 450,
            mediumSolved = 920,
            hardSolved = 450,
            currentStreak = 15,
            longestStreak = 90,
            contestCount = 145,
            badge = "🛡️ Guardian",
            division = "Knight",
            lastUpdated = Instant.now().minusSeconds(1200)
        ),
        Platform.GITHUB to PlatformStats(
            platform = Platform.GITHUB,
            username = "torvalds",
            rating = 1420,
            highestRating = 1850,
            rank = "Octocat Master",
            globalRank = 1,
            solved = 1420,
            easySolved = 500,
            mediumSolved = 600,
            hardSolved = 320,
            currentStreak = 24,
            longestStreak = 150,
            contestCount = 0,
            badge = "🐱 GitHub Contributor",
            division = "Open Source",
            lastUpdated = Instant.now().minusSeconds(300)
        )
    )

    private val fakeContests = listOf(
        Contest(
            id = "cf-1950",
            platform = Platform.CODEFORCES,
            name = "Codeforces Round 950 (Div. 2)",
            officialUrl = "https://codeforces.com/contests/1950",
            registrationUrl = "https://codeforces.com/contestRegistration/1950",
            startTimeUtc = Instant.now().plusSeconds(3600 * 2 + 1800),
            endTimeUtc = Instant.now().plusSeconds(3600 * 4 + 1800),
            durationSeconds = 7200,
            contestType = "ICPC",
            ratingType = "Rated for Div. 2",
            status = ContestStatus.UPCOMING
        ),
        Contest(
            id = "lc-wc390",
            platform = Platform.LEETCODE,
            name = "Weekly Contest 390",
            officialUrl = "https://leetcode.com/contest/weekly-contest-390",
            registrationUrl = "https://leetcode.com/contest/weekly-contest-390",
            startTimeUtc = Instant.now().minusSeconds(1800),
            endTimeUtc = Instant.now().plusSeconds(3600),
            durationSeconds = 5400,
            contestType = "LeetCode Format",
            ratingType = "Rated",
            status = ContestStatus.LIVE
        ),
        Contest(
            id = "cc-starters125",
            platform = Platform.CODECHEF,
            name = "Starters 125 (Rated till 6-Star)",
            officialUrl = "https://www.codechef.com/START125",
            registrationUrl = "https://www.codechef.com/START125",
            startTimeUtc = Instant.now().plusSeconds(3600 * 26),
            endTimeUtc = Instant.now().plusSeconds(3600 * 28),
            durationSeconds = 7200,
            contestType = "Short Contest",
            ratingType = "Rated",
            status = ContestStatus.UPCOMING
        ),
        Contest(
            id = "abc-345",
            platform = Platform.ATCODER,
            name = "AtCoder Beginner Contest 345",
            officialUrl = "https://atcoder.jp/contests/abc345",
            registrationUrl = "https://atcoder.jp/contests/abc345",
            startTimeUtc = Instant.now().plusSeconds(3600 * 48),
            endTimeUtc = Instant.now().plusSeconds(3600 * 50),
            durationSeconds = 6000,
            contestType = "ABC",
            ratingType = "Rated (<2000)",
            status = ContestStatus.UPCOMING
        )
    )

    private val fakeResources = listOf(
        Resource(
            id = "res-1",
            title = "Mastering Dynamic Programming Patterns",
            description = "Comprehensive guide covering top 15 DP patterns with code templates and practice problems.",
            creator = "William Fiset",
            url = "https://youtube.com/watch?v=dp_patterns",
            category = "Dynamic Programming",
            platform = "YouTube",
            duration = "45 mins",
            priority = 1,
            thumbnailUrl = null,
            publishedAt = Instant.now().minusSeconds(86400 * 5)
        ),
        Resource(
            id = "res-2",
            title = "Segment Tree & Lazy Propagation Deep Dive",
            description = "Step-by-step tutorial on range query data structures used in Div. 1/Div. 2 contests.",
            creator = "Errichto",
            url = "https://youtube.com/watch?v=segment_trees",
            category = "Data Structures",
            platform = "YouTube",
            duration = "30 mins",
            priority = 2,
            thumbnailUrl = null,
            publishedAt = Instant.now().minusSeconds(86400 * 10)
        )
    )

    fun getPlatforms(): Flow<List<Platform>> = MutableStateFlow(Platform.values().toList())

    fun getConnectedAccounts(): Flow<List<PlatformAccount>> = connectedPlatforms

    fun getGitHubStats(): Flow<GitHubStats?> = fakeGitHubStats

    fun getPlatformStats(platform: Platform, username: String): Flow<PlatformStats?> =
        MutableStateFlow(fakeStats[platform])

    fun getAllConnectedStats(): Flow<List<PlatformStats>> =
        connectedPlatforms.map { accounts ->
            accounts.mapNotNull { fakeStats[it.platform] }
        }

    fun getContests(): Flow<List<Contest>> = MutableStateFlow(fakeContests)

    fun getContestById(id: String): Flow<Contest?> =
        MutableStateFlow(fakeContests.find { it.id == id } ?: fakeContests.first())

    fun getRatingHistory(platform: Platform, username: String): Flow<List<RatingPoint>> =
        MutableStateFlow(
            listOf(
                RatingPoint(Instant.now().minusSeconds(86400 * 120), 1200, "Round 800", "Div. 3 Round"),
                RatingPoint(Instant.now().minusSeconds(86400 * 90), 1350, "Round 820", "Div. 3 Round"),
                RatingPoint(Instant.now().minusSeconds(86400 * 60), 1500, "Round 850", "Div. 2 Round"),
                RatingPoint(Instant.now().minusSeconds(86400 * 30), 1680, "Round 900", "Div. 2 Round"),
                RatingPoint(Instant.now().minusSeconds(86400 * 10), 1840, "Round 940", "Div. 2 Round"),
                RatingPoint(Instant.now(), 1920, "Round 950", "Div. 1/Div. 2")
            )
        )

    fun getResources(): Flow<List<Resource>> = MutableStateFlow(fakeResources)

    fun addPlatformAccount(platform: Platform, username: String) {
        val newAcc = PlatformAccount(
            id = (connectedPlatforms.value.size + 1).toString(),
            platform = platform,
            username = username,
            displayName = username,
            isEnabled = true,
            lastSyncedAt = Instant.now()
        )
        connectedPlatforms.value = connectedPlatforms.value + newAcc
        if (platform == Platform.GITHUB) {
            fakeGitHubStats.value = GitHubStats(
                username = username,
                name = username,
                avatarUrl = "https://github.com/$username.png",
                publicRepos = 24,
                totalStars = 450,
                totalContributionsThisYear = 680,
                currentContributionStreak = 12,
                longestContributionStreak = 45,
                topLanguages = listOf("Kotlin", "Java", "Python"),
                followers = 120,
                following = 35,
                lastUpdated = Instant.now()
            )
        }
    }
}
