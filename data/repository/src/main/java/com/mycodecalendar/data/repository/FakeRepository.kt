package com.mycodecalendar.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.mycodecalendar.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.Instant

/**
 * Repository that persists platform handles to SharedPreferences so users
 * don't need to re-enter them after app restart.
 * Stats are generated based on the saved handle name.
 */
class FakeRepository(context: Context? = null) {

    private val prefs: SharedPreferences? = context?.getSharedPreferences(
        "platform_accounts", Context.MODE_PRIVATE
    )

    // Load persisted accounts on startup, fallback to empty list
    private val connectedPlatforms = MutableStateFlow<List<PlatformAccount>>(
        loadSavedAccounts()
    )

    private val fakeGitHubStatsFlow = MutableStateFlow<GitHubStats?>(
        connectedPlatforms.value
            .firstOrNull { it.platform == Platform.GITHUB }
            ?.let { buildGitHubStats(it.username) }
    )

    // Dynamic stats keyed by platform+username
    private val dynamicStats = MutableStateFlow<Map<String, PlatformStats>>(
        buildStatsMap(connectedPlatforms.value)
    )

    // ── Persistence helpers ───────────────────────────────────────────────────

    private fun accountKey(platform: Platform) = "account_${platform.name}"

    private fun loadSavedAccounts(): List<PlatformAccount> {
        val p = prefs ?: return emptyList()
        return Platform.values().mapNotNull { platform ->
            val handle = p.getString(accountKey(platform), null)
            if (handle.isNullOrBlank()) null
            else PlatformAccount(
                id = platform.name,
                platform = platform,
                username = handle,
                displayName = handle,
                isEnabled = true,
                lastSyncedAt = Instant.now(),
                syncStatus = "SAVED"
            )
        }
    }

    private fun saveAccount(platform: Platform, handle: String) {
        prefs?.edit()?.putString(accountKey(platform), handle)?.apply()
    }

    private fun removeAccount(platform: Platform) {
        prefs?.edit()?.remove(accountKey(platform))?.apply()
    }

    // ── Stats generation from handle ──────────────────────────────────────────

    private fun buildStatsMap(accounts: List<PlatformAccount>): Map<String, PlatformStats> {
        return accounts.associate { acc ->
            val key = statsKey(acc.platform, acc.username)
            key to generateStats(acc.platform, acc.username)
        }
    }

    private fun statsKey(platform: Platform, username: String) = "${platform.name}:$username"

    private fun generateStats(platform: Platform, username: String): PlatformStats {
        // Deterministic fake values based on username hash so they're
        // consistent across restarts for the same handle
        val seed = username.hashCode().toLong().let { if (it < 0) -it else it }
        val baseRating = 1200 + (seed % 1800).toInt()
        val highestRating = baseRating + (seed % 300).toInt()
        val globalRank = (seed % 50000).toInt() + 1
        val solved = 200 + (seed % 2000).toInt()

        return PlatformStats(
            platform = platform,
            username = username,
            rating = baseRating,
            highestRating = highestRating,
            rank = ratingToRank(platform, baseRating),
            globalRank = globalRank,
            solved = solved,
            easySolved = (solved * 0.30).toInt(),
            mediumSolved = (solved * 0.50).toInt(),
            hardSolved = (solved * 0.20).toInt(),
            currentStreak = (seed % 60).toInt(),
            longestStreak = (seed % 120).toInt() + 30,
            contestCount = (seed % 200).toInt() + 10,
            badge = null,
            division = null,
            lastUpdated = Instant.now()
        )
    }

    private fun ratingToRank(platform: Platform, rating: Int): String = when (platform) {
        Platform.CODEFORCES -> when {
            rating >= 3000 -> "Legendary Grandmaster"
            rating >= 2600 -> "International Grandmaster"
            rating >= 2400 -> "Grandmaster"
            rating >= 2300 -> "International Master"
            rating >= 2100 -> "Master"
            rating >= 1900 -> "Candidate Master"
            rating >= 1600 -> "Expert"
            rating >= 1400 -> "Specialist"
            rating >= 1200 -> "Pupil"
            else -> "Newbie"
        }
        Platform.LEETCODE -> when {
            rating >= 3000 -> "Guardian"
            rating >= 2500 -> "Knight"
            rating >= 2000 -> "Silver"
            else -> "Bronze"
        }
        Platform.CODECHEF -> when {
            rating >= 2500 -> "7 Star"
            rating >= 2200 -> "6 Star"
            rating >= 1800 -> "5 Star"
            rating >= 1600 -> "4 Star"
            rating >= 1400 -> "3 Star"
            rating >= 1200 -> "2 Star"
            else -> "1 Star"
        }
        else -> when {
            rating >= 2000 -> "Yellow"
            rating >= 1600 -> "Blue"
            rating >= 1200 -> "Cyan"
            else -> "Gray"
        }
    }

    private fun buildGitHubStats(username: String): GitHubStats {
        val seed = username.hashCode().toLong().let { if (it < 0) -it else it }
        return GitHubStats(
            username = username,
            name = username,
            avatarUrl = "https://github.com/$username.png",
            publicRepos = (seed % 80).toInt() + 5,
            totalStars = (seed % 2000).toInt() + 10,
            totalContributionsThisYear = (seed % 800).toInt() + 50,
            currentContributionStreak = (seed % 50).toInt() + 1,
            longestContributionStreak = (seed % 100).toInt() + 10,
            topLanguages = listOf("Kotlin", "Java", "Python", "C++", "JavaScript")
                .shuffled(java.util.Random(seed)).take(4),
            followers = (seed % 1000).toInt(),
            following = (seed % 200).toInt(),
            lastUpdated = Instant.now()
        )
    }

    // ── Rating history ─────────────────────────────────────────────────────────

    private fun generateRatingHistory(platform: Platform, username: String): List<RatingPoint> {
        val seed = username.hashCode().toLong().let { if (it < 0) -it else it }
        val baseRating = 1200 + (seed % 800).toInt()
        var current = baseRating
        return (6 downTo 0).map { monthsAgo ->
            current += (-100..150).random()
            current = current.coerceIn(800, 3500)
            RatingPoint(
                timestamp = Instant.now().minusSeconds(86400L * 30 * monthsAgo),
                rating = current,
                contestId = "round-${800 + monthsAgo * 15}",
                contestName = "Round ${800 + monthsAgo * 15}"
            )
        }.reversed()
    }

    // ── Public API ─────────────────────────────────────────────────────────────

    fun getConnectedAccounts(): Flow<List<PlatformAccount>> = connectedPlatforms

    fun getGitHubStats(): Flow<GitHubStats?> = fakeGitHubStatsFlow

    fun getPlatformStats(platform: Platform, username: String): Flow<PlatformStats?> {
        val key = statsKey(platform, username)
        return dynamicStats.map { it[key] }
    }

    fun getAllConnectedStats(): Flow<List<PlatformStats>> =
        dynamicStats.map { statsMap ->
            connectedPlatforms.value.mapNotNull { acc ->
                statsMap[statsKey(acc.platform, acc.username)]
            }
        }

    fun getPlatforms(): Flow<List<Platform>> = MutableStateFlow(Platform.values().toList())

    fun addPlatformAccount(platform: Platform, username: String) {
        // Save to SharedPreferences
        saveAccount(platform, username)

        val newAcc = PlatformAccount(
            id = platform.name,
            platform = platform,
            username = username,
            displayName = username,
            isEnabled = true,
            lastSyncedAt = Instant.now(),
            syncStatus = "SAVED"
        )

        // Replace existing entry for this platform or add new
        val current = connectedPlatforms.value.toMutableList()
        val idx = current.indexOfFirst { it.platform == platform }
        if (idx >= 0) current[idx] = newAcc else current.add(newAcc)
        connectedPlatforms.value = current

        // Update stats for this account
        val newStats = dynamicStats.value.toMutableMap()
        newStats[statsKey(platform, username)] = generateStats(platform, username)
        dynamicStats.value = newStats

        // Update GitHub stats if needed
        if (platform == Platform.GITHUB) {
            fakeGitHubStatsFlow.value = buildGitHubStats(username)
        }
    }

    fun removePlatformAccount(platform: Platform) {
        removeAccount(platform)
        val current = connectedPlatforms.value.toMutableList()
        val removed = current.firstOrNull { it.platform == platform }
        current.removeAll { it.platform == platform }
        connectedPlatforms.value = current

        // Remove stats
        if (removed != null) {
            val newStats = dynamicStats.value.toMutableMap()
            newStats.remove(statsKey(platform, removed.username))
            dynamicStats.value = newStats
        }

        if (platform == Platform.GITHUB) {
            val ghAcc = connectedPlatforms.value.firstOrNull { it.platform == Platform.GITHUB }
            fakeGitHubStatsFlow.value = ghAcc?.let { buildGitHubStats(it.username) }
        }
    }

    fun getContests(): Flow<List<Contest>> = MutableStateFlow(fakeContests)

    fun getContestById(id: String): Flow<Contest?> =
        MutableStateFlow(fakeContests.find { it.id == id })

    fun getRatingHistory(platform: Platform, username: String): Flow<List<RatingPoint>> =
        MutableStateFlow(generateRatingHistory(platform, username))

    fun getResources(): Flow<List<Resource>> = MutableStateFlow(fakeResources)
}

// ── Static contest & resource data ────────────────────────────────────────────

private val fakeContests = listOf(
    Contest(
        id = "cf-1950",
        providerContestId = "1950",
        platform = Platform.CODEFORCES,
        name = "Codeforces Round 950 (Div. 2)",
        officialUrl = "https://codeforces.com/contests/1950",
        registrationUrl = "https://codeforces.com/contestRegistration/1950",
        startTimeUtc = Instant.now().plusSeconds(3600 * 2 + 1800),
        endTimeUtc = Instant.now().plusSeconds(3600 * 4 + 1800),
        durationSeconds = 7200L,
        contestType = "ICPC",
        ratingType = "Rated for Div. 2",
        status = ContestStatus.UPCOMING,
        lastFetchedAt = Instant.now()
    ),
    Contest(
        id = "lc-wc390",
        providerContestId = "wc390",
        platform = Platform.LEETCODE,
        name = "Weekly Contest 390",
        officialUrl = "https://leetcode.com/contest/weekly-contest-390",
        registrationUrl = "https://leetcode.com/contest/weekly-contest-390",
        startTimeUtc = Instant.now().minusSeconds(1800),
        endTimeUtc = Instant.now().plusSeconds(3600),
        durationSeconds = 5400L,
        contestType = "LeetCode Format",
        ratingType = "Rated",
        status = ContestStatus.LIVE,
        lastFetchedAt = Instant.now()
    ),
    Contest(
        id = "cc-starters125",
        providerContestId = "START125",
        platform = Platform.CODECHEF,
        name = "Starters 125",
        officialUrl = "https://www.codechef.com/START125",
        registrationUrl = "https://www.codechef.com/START125",
        startTimeUtc = Instant.now().plusSeconds(3600 * 26),
        endTimeUtc = Instant.now().plusSeconds(3600 * 28),
        durationSeconds = 7200L,
        contestType = "Short Contest",
        ratingType = "Rated",
        status = ContestStatus.UPCOMING,
        lastFetchedAt = Instant.now()
    ),
    Contest(
        id = "abc-345",
        providerContestId = "abc345",
        platform = Platform.ATCODER,
        name = "AtCoder Beginner Contest 345",
        officialUrl = "https://atcoder.jp/contests/abc345",
        registrationUrl = "https://atcoder.jp/contests/abc345",
        startTimeUtc = Instant.now().plusSeconds(3600 * 48),
        endTimeUtc = Instant.now().plusSeconds(3600 * 50),
        durationSeconds = 6000L,
        contestType = "ABC",
        ratingType = "Rated (<2000)",
        status = ContestStatus.UPCOMING,
        lastFetchedAt = Instant.now()
    ),
    Contest(
        id = "gfg-weekly55",
        providerContestId = "weekly55",
        platform = Platform.GEEKSFORGEEKS,
        name = "GFG Weekly Contest 55",
        officialUrl = "https://practice.geeksforgeeks.org/contest/gfg-weekly-coding-contest-55",
        registrationUrl = "https://practice.geeksforgeeks.org/contest",
        startTimeUtc = Instant.now().plusSeconds(3600 * 72),
        endTimeUtc = Instant.now().plusSeconds(3600 * 73),
        durationSeconds = 3600L,
        contestType = "Weekly",
        ratingType = "Rated",
        status = ContestStatus.UPCOMING,
        lastFetchedAt = Instant.now()
    )
)

private val fakeResources = listOf(
    Resource(
        id = "res-1",
        title = "Mastering Dynamic Programming Patterns",
        description = "15 DP patterns with code templates and practice problems.",
        creator = "William Fiset",
        url = "https://youtube.com/watch?v=dp_patterns",
        category = "Dynamic Programming",
        platform = Platform.LEETCODE,
        duration = "45 min",
        priority = 1,
        thumbnailUrl = null,
        publishedAt = Instant.now().minusSeconds(86400 * 5)
    ),
    Resource(
        id = "res-2",
        title = "Segment Tree & Lazy Propagation",
        description = "Range query data structures for Div. 1/2 contests.",
        creator = "Errichto",
        url = "https://youtube.com/watch?v=segment_trees",
        category = "Data Structures",
        platform = Platform.CODEFORCES,
        duration = "30 min",
        priority = 2,
        thumbnailUrl = null,
        publishedAt = Instant.now().minusSeconds(86400 * 10)
    ),
    Resource(
        id = "res-3",
        title = "Graph Algorithms Comprehensive Guide",
        description = "BFS, DFS, Dijkstra, and shortest path algorithms explained.",
        creator = "Abdul Bari",
        url = "https://youtube.com/watch?v=graph_algos",
        category = "Graphs",
        platform = null,
        duration = "60 min",
        priority = 3,
        thumbnailUrl = null,
        publishedAt = Instant.now().minusSeconds(86400 * 15)
    ),
    Resource(
        id = "res-4",
        title = "Competitive Programming Handbook",
        description = "Free 300-page textbook covering all major CP topics.",
        creator = "Antti Laaksonen",
        url = "https://cses.fi/book/book.pdf",
        category = "Algorithms",
        platform = null,
        duration = "Book",
        priority = 4,
        thumbnailUrl = null,
        publishedAt = Instant.now().minusSeconds(86400 * 30)
    )
)
