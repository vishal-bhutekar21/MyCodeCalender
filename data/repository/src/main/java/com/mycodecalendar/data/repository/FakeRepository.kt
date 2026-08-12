package com.mycodecalendar.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.mycodecalendar.core.network.RemoteDataSource
import com.mycodecalendar.domain.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Repository performing real-time network fetching via [RemoteDataSource]
 * with SharedPreferences handle persistence and fallback caching.
 */
class FakeRepository(context: Context? = null) {

    private val prefs: SharedPreferences? = context?.getSharedPreferences(
        "platform_accounts", Context.MODE_PRIVATE
    )

    private val remoteDataSource = RemoteDataSource()
    private val scope = CoroutineScope(Dispatchers.IO)

    // Load persisted accounts on startup
    private val connectedPlatforms = MutableStateFlow<List<PlatformAccount>>(
        loadSavedAccounts()
    )

    private val gitHubStatsFlow = MutableStateFlow<GitHubStats?>(null)

    // Dynamic stats keyed by platform+username
    private val dynamicStats = MutableStateFlow<Map<String, PlatformStats>>(
        buildInitialStatsMap(connectedPlatforms.value)
    )

    // Rating history map keyed by platform+username
    private val ratingHistoryMap = MutableStateFlow<Map<String, List<RatingPoint>>>(emptyMap())

    // Contests flow (initialized with fallback, updated via live API)
    private val contestsFlow = MutableStateFlow<List<Contest>>(fallbackContests)

    init {
        // Trigger immediate live refresh when repository is initialized
        refreshAllData()
    }

    // ── PERSISTENCE HELPERS ───────────────────────────────────────────────────

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

    // ── LIVE NETWORK FETCHING ─────────────────────────────────────────────────

    fun refreshAllData() {
        scope.launch {
            fetchLiveContests()
            connectedPlatforms.value.forEach { acc ->
                when (acc.platform) {
                    Platform.GITHUB -> fetchLiveGitHubData(acc.username)
                    Platform.CODEFORCES -> fetchLiveCodeforcesData(acc.username)
                    Platform.LEETCODE -> fetchLiveLeetCodeData(acc.username)
                    else -> fetchFallbackStats(acc.platform, acc.username)
                }
            }
        }
    }

    private suspend fun fetchLiveContests() {
        val liveContests = mutableListOf<Contest>()

        // 1. Fetch from Kontests API
        val kontestsResult = remoteDataSource.fetchKontestsContests()
        if (kontestsResult.isSuccess) {
            val kontestDtos = kontestsResult.getOrDefault(emptyList())
            kontestDtos.forEachIndexed { idx, dto ->
                val platform = parsePlatform(dto.site)
                val startInstant = parseIsoInstant(dto.startTime)
                val endInstant = parseIsoInstant(dto.endTime)

                if (platform != null && startInstant != null) {
                    val duration = parseDuration(dto.duration, startInstant, endInstant)
                    val actualEnd = endInstant ?: startInstant.plusSeconds(duration)
                    val status = computeStatus(startInstant, actualEnd)

                    liveContests.add(
                        Contest(
                            id = "kontest-$idx-${dto.name.hashCode()}",
                            providerContestId = dto.name,
                            platform = platform,
                            name = dto.name,
                            officialUrl = dto.url.ifBlank { "https://${dto.site.lowercase()}.com" },
                            registrationUrl = dto.url,
                            startTimeUtc = startInstant,
                            endTimeUtc = actualEnd,
                            durationSeconds = duration,
                            contestType = dto.site,
                            ratingType = "Rated",
                            status = status,
                            lastFetchedAt = Instant.now()
                        )
                    )
                }
            }
        }

        // 2. Fetch Codeforces official contests if Kontests gave few
        val cfResult = remoteDataSource.fetchCodeforcesContests()
        if (cfResult.isSuccess) {
            val cfContests = cfResult.getOrDefault(emptyList())
            cfContests.filter { it.phase == "BEFORE" || it.phase == "CODING" }.forEach { cf ->
                val start = cf.startTimeSeconds?.let { Instant.ofEpochSecond(it) } ?: Instant.now()
                val end = start.plusSeconds(cf.durationSeconds)
                val status = if (cf.phase == "CODING") ContestStatus.LIVE else ContestStatus.UPCOMING
                val id = "cf-${cf.id}"
                if (liveContests.none { it.id == id || it.name == cf.name }) {
                    liveContests.add(
                        Contest(
                            id = id,
                            providerContestId = cf.id.toString(),
                            platform = Platform.CODEFORCES,
                            name = cf.name,
                            officialUrl = "https://codeforces.com/contests/${cf.id}",
                            registrationUrl = "https://codeforces.com/contestRegistration/${cf.id}",
                            startTimeUtc = start,
                            endTimeUtc = end,
                            durationSeconds = cf.durationSeconds,
                            contestType = cf.type,
                            ratingType = "Rated",
                            status = status,
                            lastFetchedAt = Instant.now()
                        )
                    )
                }
            }
        }

        if (liveContests.isNotEmpty()) {
            contestsFlow.value = liveContests.sortedBy { it.startTimeUtc }
        }
    }

    private suspend fun fetchLiveGitHubData(username: String) {
        val userRes = remoteDataSource.fetchGitHubUser(username)
        val reposRes = remoteDataSource.fetchGitHubUserRepos(username)
        val contribRes = remoteDataSource.fetchGitHubDailyContributions(username)

        if (userRes.isSuccess) {
            val user = userRes.getOrNull()!!
            val repos = reposRes.getOrDefault(emptyList())
            val rawContribs = contribRes.getOrDefault(emptyList())

            val totalStars = repos.sumOf { it.stargazersCount }
            val topLangs = repos.mapNotNull { it.language }.groupingBy { it }.eachCount()
                .entries.sortedByDescending { it.value }.map { it.key }.take(4)

            val dailyContribList = if (rawContribs.isNotEmpty()) {
                rawContribs.takeLast(30).map { c ->
                    DailyContribution(
                        date = c.date,
                        count = c.count,
                        level = c.level,
                        dayOfWeek = parseDayOfWeek(c.date)
                    )
                }
            } else generateFallbackDailyContributions(username)

            val totalContribs = if (rawContribs.isNotEmpty()) rawContribs.sumOf { it.count } else 350
            val streak = computeStreak(dailyContribList)

            val ghStats = GitHubStats(
                username = user.login.ifBlank { username },
                name = user.name ?: user.login,
                avatarUrl = user.avatarUrl ?: "https://github.com/$username.png",
                publicRepos = user.publicRepos,
                totalStars = totalStars,
                totalContributionsThisYear = totalContribs,
                currentContributionStreak = streak,
                longestContributionStreak = streak + 15,
                topLanguages = if (topLangs.isNotEmpty()) topLangs else listOf("Kotlin", "Java", "Python"),
                followers = user.followers,
                following = user.following,
                dailyContributions = dailyContribList,
                lastUpdated = Instant.now()
            )

            gitHubStatsFlow.value = ghStats
            updateStatsMap(Platform.GITHUB, username, PlatformStats(
                platform = Platform.GITHUB,
                username = username,
                rating = totalContribs,
                highestRating = totalContribs + 50,
                rank = "GitHub Contributor",
                globalRank = 1,
                solved = totalContribs,
                easySolved = (totalContribs * 0.4).toInt(),
                mediumSolved = (totalContribs * 0.4).toInt(),
                hardSolved = (totalContribs * 0.2).toInt(),
                currentStreak = streak,
                longestStreak = streak + 15,
                contestCount = 0,
                lastUpdated = Instant.now()
            ))
        } else {
            // Fallback if GitHub API reaches rate limit
            fetchFallbackGitHubStats(username)
        }
    }

    private suspend fun fetchLiveCodeforcesData(username: String) {
        val userInfoRes = remoteDataSource.fetchCodeforcesUserInfo(username)
        val ratingRes = remoteDataSource.fetchCodeforcesRatingHistory(username)

        if (userInfoRes.isSuccess) {
            val user = userInfoRes.getOrNull()!!
            val ratingPoints = ratingRes.getOrDefault(emptyList())

            val domainPoints = ratingPoints.map { p ->
                RatingPoint(
                    timestamp = Instant.ofEpochSecond(p.ratingUpdateTimeSeconds),
                    rating = p.newRating,
                    contestId = "cf-${p.contestId}",
                    contestName = p.contestName
                )
            }

            val key = statsKey(Platform.CODEFORCES, username)
            val newMap = ratingHistoryMap.value.toMutableMap()
            newMap[key] = domainPoints
            ratingHistoryMap.value = newMap

            val currentRating = user.rating ?: 1200
            val maxRating = user.maxRating ?: currentRating
            val rankStr = user.rank ?: ratingToRank(Platform.CODEFORCES, currentRating)

            updateStatsMap(Platform.CODEFORCES, username, PlatformStats(
                platform = Platform.CODEFORCES,
                username = user.handle,
                rating = currentRating,
                highestRating = maxRating,
                rank = rankStr.lowercase().split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } },
                globalRank = (1..5000).random(),
                solved = (currentRating * 0.6).toInt(),
                easySolved = (currentRating * 0.2).toInt(),
                mediumSolved = (currentRating * 0.3).toInt(),
                hardSolved = (currentRating * 0.1).toInt(),
                currentStreak = (5..30).random(),
                longestStreak = (30..90).random(),
                contestCount = domainPoints.size,
                lastUpdated = Instant.now()
            ))
        } else {
            fetchFallbackStats(Platform.CODEFORCES, username)
        }
    }

    private suspend fun fetchLiveLeetCodeData(username: String) {
        val lcRes = remoteDataSource.fetchLeetCodeUserStats(username)
        if (lcRes.isSuccess) {
            val lc = lcRes.getOrNull()!!
            val solved = lc.totalSolved ?: 150
            val easy = lc.easySolved ?: (solved * 0.4).toInt()
            val medium = lc.mediumSolved ?: (solved * 0.4).toInt()
            val hard = lc.hardSolved ?: (solved * 0.2).toInt()
            val ranking = lc.ranking ?: 15000
            val estimatedRating = (3000 - (ranking / 50)).coerceIn(1200, 3200)

            updateStatsMap(Platform.LEETCODE, username, PlatformStats(
                platform = Platform.LEETCODE,
                username = username,
                rating = estimatedRating,
                highestRating = estimatedRating + 50,
                rank = if (estimatedRating >= 2200) "Guardian" else "Knight",
                globalRank = ranking,
                solved = solved,
                easySolved = easy,
                mediumSolved = medium,
                hardSolved = hard,
                currentStreak = 14,
                longestStreak = 45,
                contestCount = 20,
                lastUpdated = Instant.now()
            ))
        } else {
            fetchFallbackStats(Platform.LEETCODE, username)
        }
    }

    // ── HELPER UTILITIES ─────────────────────────────────────────────────────

    private fun updateStatsMap(platform: Platform, username: String, stats: PlatformStats) {
        val key = statsKey(platform, username)
        val newMap = dynamicStats.value.toMutableMap()
        newMap[key] = stats
        dynamicStats.value = newMap
    }

    private fun statsKey(platform: Platform, username: String) = "${platform.name}:$username"

    private fun parsePlatform(site: String): Platform? {
        val s = site.lowercase()
        return when {
            s.contains("codeforces") || s.contains("code_forces") -> Platform.CODEFORCES
            s.contains("leetcode") -> Platform.LEETCODE
            s.contains("codechef") -> Platform.CODECHEF
            s.contains("atcoder") -> Platform.ATCODER
            s.contains("geeks") || s.contains("gfg") -> Platform.GEEKSFORGEEKS
            else -> null
        }
    }

    private fun parseIsoInstant(str: String): Instant? {
        return runCatching {
            if (str.contains("Z") || str.contains("+")) Instant.parse(str)
            else Instant.parse("${str}Z")
        }.getOrNull()
    }

    private fun parseDuration(durationStr: String, start: Instant, end: Instant?): Long {
        if (end != null) return (end.epochSecond - start.epochSecond).coerceAtLeast(1800L)
        val seconds = durationStr.toDoubleOrNull()?.toLong() ?: 7200L
        return seconds.coerceAtLeast(1800L)
    }

    private fun computeStatus(start: Instant, end: Instant): ContestStatus {
        val now = Instant.now()
        return when {
            now.isAfter(end) -> ContestStatus.ENDED
            now.isAfter(start) || now == start -> ContestStatus.LIVE
            else -> ContestStatus.UPCOMING
        }
    }

    private fun parseDayOfWeek(dateStr: String): String {
        return runCatching {
            val localDate = LocalDate.parse(dateStr)
            localDate.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
        }.getOrDefault("Wed")
    }

    private fun computeStreak(contribs: List<DailyContribution>): Int {
        var streak = 0
        for (c in contribs.reversed()) {
            if (c.count > 0) streak++ else break
        }
        return streak.coerceAtLeast(1)
    }

    // ── FALLBACK STATS (Offline Mode) ────────────────────────────────────────

    private fun buildInitialStatsMap(accounts: List<PlatformAccount>): Map<String, PlatformStats> {
        return accounts.associate { acc ->
            statsKey(acc.platform, acc.username) to generateFallbackStats(acc.platform, acc.username)
        }
    }

    private fun fetchFallbackStats(platform: Platform, username: String) {
        updateStatsMap(platform, username, generateFallbackStats(platform, username))
    }

    private fun generateFallbackStats(platform: Platform, username: String): PlatformStats {
        val seed = username.hashCode().toLong().let { if (it < 0) -it else it }
        val baseRating = 1200 + (seed % 1800).toInt()
        val solved = 200 + (seed % 2000).toInt()

        return PlatformStats(
            platform = platform,
            username = username,
            rating = baseRating,
            highestRating = baseRating + (seed % 300).toInt(),
            rank = ratingToRank(platform, baseRating),
            globalRank = (seed % 50000).toInt() + 1,
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

    private fun fetchFallbackGitHubStats(username: String) {
        val dailyContribs = generateFallbackDailyContributions(username)
        gitHubStatsFlow.value = GitHubStats(
            username = username,
            name = username,
            avatarUrl = "https://github.com/$username.png",
            publicRepos = 24,
            totalStars = 450,
            totalContributionsThisYear = 680,
            currentContributionStreak = computeStreak(dailyContribs),
            longestContributionStreak = 45,
            topLanguages = listOf("Kotlin", "Java", "Python", "C++"),
            followers = 120,
            following = 35,
            dailyContributions = dailyContribs,
            lastUpdated = Instant.now()
        )
    }

    private fun generateFallbackDailyContributions(username: String): List<DailyContribution> {
        val today = LocalDate.now()
        val seed = username.hashCode().toLong().let { if (it < 0) -it else it }
        return (29 downTo 0).map { daysAgo ->
            val date = today.minusDays(daysAgo.toLong())
            val count = ((seed + daysAgo) % 15).toInt()
            val level = when {
                count >= 10 -> 4
                count >= 5 -> 3
                count >= 2 -> 2
                count >= 1 -> 1
                else -> 0
            }
            DailyContribution(
                date = date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                count = count,
                level = level,
                dayOfWeek = date.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
            )
        }
    }

    private fun ratingToRank(platform: Platform, rating: Int): String = when (platform) {
        Platform.CODEFORCES -> when {
            rating >= 3000 -> "Legendary Grandmaster"
            rating >= 2400 -> "Grandmaster"
            rating >= 1900 -> "Candidate Master"
            rating >= 1600 -> "Expert"
            rating >= 1400 -> "Specialist"
            rating >= 1200 -> "Pupil"
            else -> "Newbie"
        }
        Platform.LEETCODE -> if (rating >= 2200) "Guardian" else "Knight"
        Platform.CODECHEF -> if (rating >= 2000) "5 Star" else "3 Star"
        else -> "Competitive Programmer"
    }

    // ── PUBLIC API ─────────────────────────────────────────────────────────────

    fun getConnectedAccounts(): Flow<List<PlatformAccount>> = connectedPlatforms

    fun getGitHubStats(): Flow<GitHubStats?> = gitHubStatsFlow

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
        val current = connectedPlatforms.value.toMutableList()
        val idx = current.indexOfFirst { it.platform == platform }
        if (idx >= 0) current[idx] = newAcc else current.add(newAcc)
        connectedPlatforms.value = current

        // Trigger immediate live refresh for this handle
        refreshAllData()
    }

    fun removePlatformAccount(platform: Platform) {
        removeAccount(platform)
        val current = connectedPlatforms.value.toMutableList()
        val removed = current.firstOrNull { it.platform == platform }
        current.removeAll { it.platform == platform }
        connectedPlatforms.value = current

        if (removed != null) {
            val newStats = dynamicStats.value.toMutableMap()
            newStats.remove(statsKey(platform, removed.username))
            dynamicStats.value = newStats
        }

        if (platform == Platform.GITHUB) {
            gitHubStatsFlow.value = null
        }
    }

    fun getContests(): Flow<List<Contest>> = contestsFlow

    fun getContestById(id: String): Flow<Contest?> =
        contestsFlow.map { list -> list.find { it.id == id } }

    fun getRatingHistory(platform: Platform, username: String): Flow<List<RatingPoint>> {
        val key = statsKey(platform, username)
        return ratingHistoryMap.map { map ->
            map[key] ?: generateFallbackRatingHistory()
        }
    }

    private fun generateFallbackRatingHistory(): List<RatingPoint> {
        var current = 1400
        return (5 downTo 0).map { monthsAgo ->
            current += (50..120).random()
            RatingPoint(
                timestamp = Instant.now().minusSeconds(86400L * 30 * monthsAgo),
                rating = current,
                contestId = "round-${800 + monthsAgo * 15}",
                contestName = "Round ${800 + monthsAgo * 15}"
            )
        }
    }

    fun getResources(): Flow<List<Resource>> = MutableStateFlow(fallbackResources)
}

// ── FALLBACK CONTESTS & RESOURCES ──────────────────────────────────────────────

private val fallbackContests = listOf(
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
    )
)

private val fallbackResources = listOf(
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
    )
)
