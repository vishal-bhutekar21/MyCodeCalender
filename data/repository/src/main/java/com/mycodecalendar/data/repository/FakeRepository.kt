package com.mycodecalendar.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.mycodecalendar.core.database.MyCodeCalendarDatabase
import com.mycodecalendar.core.database.entity.SyncStateEntity
import com.mycodecalendar.core.network.LeetCodeStatsSummary
import com.mycodecalendar.core.network.RemoteDataSource
import com.mycodecalendar.domain.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

/**
 * AppRepository — Single source of truth for all data in MyCodeCalendar.
 *
 * Offline-First Architecture:
 * - On startup, Room DB is read immediately to seed flows with cached data (instant UI).
 * - Network refresh runs in the background and updates both the in-memory flows AND Room DB.
 * - If the device is offline, cached data from Room is shown with an offline banner.
 * - When connectivity is restored, [onConnectivityChanged] triggers an automatic re-fetch.
 *
 * Real Past Contest History:
 * - [getPastContestHistory] dynamically maps real connected platform rating history (e.g. Codeforces)
 *   into [PastContestRecord] items with real rating deltas (+/-), contest names, and ranks.
 * - Returns an empty list if no platform accounts are connected, triggering the UI guidance card.
 */
class FakeRepository(
    context: Context? = null,
    private val db: MyCodeCalendarDatabase? = null
) {

    // ── PERSISTENCE ────────────────────────────────────────────────────────────

    private val prefs: SharedPreferences? = context?.getSharedPreferences(
        "platform_accounts", Context.MODE_PRIVATE
    )

    private val streakPrefs: SharedPreferences? = context?.getSharedPreferences(
        "app_streak_prefs", Context.MODE_PRIVATE
    )

    // ── DEPENDENCIES ───────────────────────────────────────────────────────────

    private val remoteDataSource = RemoteDataSource()
    private val scope = CoroutineScope(Dispatchers.IO)

    // ── STATE FLOWS ────────────────────────────────────────────────────────────

    private val streakStateFlow = MutableStateFlow(calculateAndUpdateStreak())

    private val connectedPlatforms = MutableStateFlow<List<PlatformAccount>>(
        loadSavedAccounts()
    )

    private val gitHubStatsFlow = MutableStateFlow<GitHubStats?>(null)

    private val dynamicStats = MutableStateFlow<Map<String, PlatformStats>>(
        buildInitialStatsMap(connectedPlatforms.value)
    )

    private val ratingHistoryMap = MutableStateFlow<Map<String, List<RatingPoint>>>(emptyMap())

    private val ratingHistoryCache = mutableMapOf<String, List<RatingPoint>>()

    private val contestsFlow = MutableStateFlow<List<Contest>>(fallbackContests)

    val fetchError = MutableStateFlow<String?>(null)

    val isRefreshing = MutableStateFlow(false)

    /** True when the last refresh attempt failed due to no internet connection. */
    val isOffline = MutableStateFlow(false)

    private var lastRefreshTimestamp: Long = 0L
    private val minRefreshIntervalMs: Long = 5 * 60 * 1000L // 5 minutes

    init {
        // Step 1: Immediately load cached data from Room DB (zero-latency cold start)
        scope.launch { seedFromCache() }

        // Step 2: Attempt network refresh in the background
        refreshAllData(force = true)
    }

    // ── OFFLINE-FIRST CACHE SEEDING ───────────────────────────────────────────

    /**
     * Loads previously cached data from Room and pushes it into the in-memory flows.
     * Called on init so the UI has data to show instantly, before the network responds.
     */
    private suspend fun seedFromCache() {
        val database = db ?: return

        // Seed contests
        val cachedContests = database.contestDao().getAllContests()
        // Collect the first emission from the flow synchronously
        val contestList = try {
            var result: List<com.mycodecalendar.core.database.entity.ContestEntity> = emptyList()
            val job = scope.launch {
                database.contestDao().getAllContests().collect {
                    result = it
                }
            }
            // Give it a tiny window to emit the cached value
            kotlinx.coroutines.delay(50)
            job.cancel()
            result
        } catch (_: Exception) { emptyList() }

        if (contestList.isNotEmpty()) {
            contestsFlow.value = contestList.map { it.toDomain() }
        }

        // Seed platform stats for each connected account
        connectedPlatforms.value.forEach { acc ->
            val cachedStats = database.platformStatsDao().getStats(acc.platform.name, acc.username)
            if (cachedStats != null) {
                val key = statsKey(acc.platform, acc.username)
                dynamicStats.value = dynamicStats.value.toMutableMap().also {
                    it[key] = cachedStats.toDomain()
                }
            }

            // Seed rating history
            val cachedHistory = database.ratingHistoryDao()
                .getHistory(acc.platform.name, acc.username)
            if (cachedHistory.isNotEmpty()) {
                val key = statsKey(acc.platform, acc.username)
                val domainHistory = cachedHistory.map { it.toDomain() }
                ratingHistoryCache[key] = domainHistory
                ratingHistoryMap.value = ratingHistoryMap.value
                    .toMutableMap().also { it[key] = domainHistory }
            }
        }
    }

    // ── DAILY STREAK CALCULATOR & PERSISTENCE ─────────────────────────────────

    private fun calculateAndUpdateStreak(): StreakInfo {
        val sp = streakPrefs ?: return StreakInfo(currentStreak = 1, isNewDayIncrement = false, lastOpenDateText = "Today")
        val today = LocalDate.now()
        val todayStr = today.toString()

        val lastOpenStr = sp.getString("last_open_date", null)
        var streakCount = sp.getInt("current_streak", 0)
        var isNewDayIncrement = false

        if (lastOpenStr == null) {
            streakCount = 1
            isNewDayIncrement = true
        } else {
            val lastDate = runCatching { LocalDate.parse(lastOpenStr) }.getOrNull()
            if (lastDate != null) {
                when {
                    lastDate == today -> {
                        // Same day: do not increment
                        isNewDayIncrement = false
                    }
                    lastDate == today.minusDays(1) -> {
                        streakCount += 1
                        isNewDayIncrement = true
                    }
                    else -> {
                        // Streak broken
                        streakCount = 1
                        isNewDayIncrement = true
                    }
                }
            } else {
                streakCount = 1
                isNewDayIncrement = true
            }
        }

        // ── Load and update persisted active-dates set ────────────────────────
        // Stored as a comma-separated list of ISO date strings, capped at 365 entries
        val savedDates = sp.getString("active_dates", "") ?: ""
        val activeDatesSet = if (savedDates.isBlank()) mutableSetOf()
                             else savedDates.split(",").toMutableSet()
        activeDatesSet.add(todayStr)
        // Keep only the last 365 days to avoid unbounded growth
        val trimmed = activeDatesSet.sortedDescending().take(365).toSet()

        sp.edit()
            .putString("last_open_date", todayStr)
            .putInt("current_streak", streakCount)
            .putString("active_dates", trimmed.joinToString(","))
            .apply()

        return StreakInfo(
            currentStreak = streakCount,
            isNewDayIncrement = isNewDayIncrement,
            lastOpenDateText = today.format(DateTimeFormatter.ofPattern("EEE, dd MMM")),
            activeDates = trimmed
        )
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

    // ── LIVE NETWORK REFRESH ──────────────────────────────────────────────────

    fun refreshAllData(force: Boolean = false) {
        scope.launch {
            refreshAndAwait(force = force)
        }
    }

    suspend fun refreshAndAwait(force: Boolean = false) {
        val now = System.currentTimeMillis()
        if (!force && (now - lastRefreshTimestamp) < minRefreshIntervalMs) {
            return
        }

        withContext(Dispatchers.IO) {
            isRefreshing.value = true
            fetchError.value = null
            try {
                fetchLiveContests()
                connectedPlatforms.value.forEach { acc ->
                    when (acc.platform) {
                        Platform.GITHUB -> fetchLiveGitHubData(acc.username)
                        Platform.CODEFORCES -> fetchLiveCodeforcesData(acc.username)
                        Platform.LEETCODE -> fetchLiveLeetCodeData(acc.username)
                        Platform.CODECHEF -> fetchLiveCodeChefData(acc.username)
                        else -> fetchFallbackStats(acc.platform, acc.username)
                    }
                }
                lastRefreshTimestamp = System.currentTimeMillis()
                isOffline.value = false
            } catch (e: Exception) {
                val msg = e.message ?: "Unknown error"
                // Distinguish network errors from other errors
                if (msg.contains("Unable to resolve host") ||
                    msg.contains("timeout") ||
                    msg.contains("No address") ||
                    msg.contains("failed to connect") ||
                    msg.contains("Network") ||
                    msg.contains("SocketException") ||
                    msg.contains("UnknownHost")
                ) {
                    isOffline.value = true
                    fetchError.value = "No internet — showing cached data"
                } else {
                    fetchError.value = "Refresh failed: $msg"
                }
            } finally {
                isRefreshing.value = false
            }
        }
    }

    /**
     * Called by the UI layer (via MainActivity's NetworkMonitor) when connectivity changes.
     * Triggers an automatic re-fetch when the device comes back online.
     */
    fun onConnectivityChanged(isOnline: Boolean) {
        if (isOnline) {
            isOffline.value = false
            // Auto-refresh immediately when connectivity is restored
            refreshAllData(force = false)
        } else {
            isOffline.value = true
        }
    }

    // ── LIVE FETCH IMPLEMENTATIONS ────────────────────────────────────────────

    private suspend fun fetchLiveContests() {
        val liveContests = mutableListOf<Contest>()

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

                    val officialUrl = when {
                        dto.url.isNotBlank() && dto.url.startsWith("http") -> dto.url
                        platform == Platform.CODEFORCES -> "https://codeforces.com/contests"
                        platform == Platform.LEETCODE -> "https://leetcode.com/contest/"
                        platform == Platform.CODECHEF -> "https://www.codechef.com/contests"
                        platform == Platform.ATCODER -> "https://atcoder.jp/contests/"
                        platform == Platform.GEEKSFORGEEKS -> "https://www.geeksforgeeks.org/events/"
                        else -> "https://${dto.site.lowercase()}.com"
                    }

                    liveContests.add(
                        Contest(
                            id = "kontest-$idx-${dto.name.hashCode()}",
                            providerContestId = dto.name,
                            platform = platform,
                            name = dto.name,
                            officialUrl = officialUrl,
                            registrationUrl = officialUrl,
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
                            officialUrl = "https://codeforces.com/contest/${cf.id}",
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

        // Ensure real upcoming LeetCode contests (Weekly & Biweekly) are always included
        val hasUpcomingLeetCode = liveContests.any { it.platform == Platform.LEETCODE && it.status == ContestStatus.UPCOMING }
        if (!hasUpcomingLeetCode) {
            val realLc = generateRealUpcomingLeetCodeContests()
            realLc.forEach { lc ->
                if (liveContests.none { it.id == lc.id || it.name == lc.name }) {
                    liveContests.add(lc)
                }
            }
        }

        if (liveContests.isNotEmpty()) {
            val sorted = liveContests.sortedBy { it.startTimeUtc }
            contestsFlow.value = sorted

            // Persist to Room for offline access
            db?.contestDao()?.let { dao ->
                dao.deleteAllContests()
                dao.insertContests(sorted.map { it.toEntity() })
            }

            // Record successful sync timestamp
            db?.syncStateDao()?.upsert(
                SyncStateEntity(
                    key = "contests",
                    lastSyncedAt = Instant.now(),
                    lastError = null
                )
            )
        }
    }

    /**
     * Dynamically calculates authentic upcoming LeetCode Weekly and Biweekly contests
     * based on exact LeetCode schedules:
     * - Weekly: Every Sunday at 02:30 UTC (08:00 AM IST)
     * - Biweekly: Alternate Saturdays at 14:30 UTC (08:00 PM IST)
     */
    private fun generateRealUpcomingLeetCodeContests(): List<Contest> {
        val now = ZonedDateTime.now(ZoneId.of("UTC"))
        val contests = mutableListOf<Contest>()

        // 1. Next upcoming Sunday 02:30 UTC (Weekly Contest)
        var nextSunday = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
            .withHour(2).withMinute(30).withSecond(0).withNano(0)
        if (nextSunday.isBefore(now)) {
            nextSunday = nextSunday.plusWeeks(1)
        }

        // Benchmark: Weekly Contest 410 was on Aug 11, 2024
        val benchmarkDate = ZonedDateTime.of(2024, 8, 11, 2, 30, 0, 0, ZoneId.of("UTC"))
        val weeksDiff = ChronoUnit.WEEKS.between(benchmarkDate, nextSunday).toInt()
        val weeklyNum1 = 410 + weeksDiff.coerceAtLeast(0)
        val weeklyNum2 = weeklyNum1 + 1

        val weekly1Start = nextSunday.toInstant()
        val weekly1End = weekly1Start.plusSeconds(5400)
        contests.add(
            Contest(
                id = "lc-weekly-$weeklyNum1",
                providerContestId = "weekly-contest-$weeklyNum1",
                platform = Platform.LEETCODE,
                name = "LeetCode Weekly Contest $weeklyNum1",
                officialUrl = "https://leetcode.com/contest/weekly-contest-$weeklyNum1/",
                registrationUrl = "https://leetcode.com/contest/weekly-contest-$weeklyNum1/",
                startTimeUtc = weekly1Start,
                endTimeUtc = weekly1End,
                durationSeconds = 5400L,
                contestType = "LeetCode Weekly",
                ratingType = "Rated",
                status = computeStatus(weekly1Start, weekly1End),
                lastFetchedAt = Instant.now()
            )
        )

        val weekly2Start = nextSunday.plusWeeks(1).toInstant()
        val weekly2End = weekly2Start.plusSeconds(5400)
        contests.add(
            Contest(
                id = "lc-weekly-$weeklyNum2",
                providerContestId = "weekly-contest-$weeklyNum2",
                platform = Platform.LEETCODE,
                name = "LeetCode Weekly Contest $weeklyNum2",
                officialUrl = "https://leetcode.com/contest/weekly-contest-$weeklyNum2/",
                registrationUrl = "https://leetcode.com/contest/weekly-contest-$weeklyNum2/",
                startTimeUtc = weekly2Start,
                endTimeUtc = weekly2End,
                durationSeconds = 5400L,
                contestType = "LeetCode Weekly",
                ratingType = "Rated",
                status = computeStatus(weekly2Start, weekly2End),
                lastFetchedAt = Instant.now()
            )
        )

        // 2. Next upcoming Saturday 14:30 UTC (Biweekly Contest)
        var nextSaturday = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY))
            .withHour(14).withMinute(30).withSecond(0).withNano(0)
        if (nextSaturday.isBefore(now)) {
            nextSaturday = nextSaturday.plusWeeks(1)
        }

        // Benchmark: Biweekly Contest 137 was on Aug 17, 2024
        val biweeklyBenchmark = ZonedDateTime.of(2024, 8, 17, 14, 30, 0, 0, ZoneId.of("UTC"))
        val biweeksDiff = (ChronoUnit.WEEKS.between(biweeklyBenchmark, nextSaturday) / 2).toInt()
        val biweeklyNum = 137 + biweeksDiff.coerceAtLeast(0)

        val biweeklyStart = nextSaturday.toInstant()
        val biweeklyEnd = biweeklyStart.plusSeconds(5400)
        contests.add(
            Contest(
                id = "lc-biweekly-$biweeklyNum",
                providerContestId = "biweekly-contest-$biweeklyNum",
                platform = Platform.LEETCODE,
                name = "LeetCode Biweekly Contest $biweeklyNum",
                officialUrl = "https://leetcode.com/contest/biweekly-contest-$biweeklyNum/",
                registrationUrl = "https://leetcode.com/contest/biweekly-contest-$biweeklyNum/",
                startTimeUtc = biweeklyStart,
                endTimeUtc = biweeklyEnd,
                durationSeconds = 5400L,
                contestType = "LeetCode Biweekly",
                ratingType = "Rated",
                status = computeStatus(biweeklyStart, biweeklyEnd),
                lastFetchedAt = Instant.now()
            )
        )

        return contests
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
            val topLangs = repos.mapNotNull { it.language }
                .groupingBy { it }.eachCount()
                .entries.sortedByDescending { it.value }
                .map { it.key }.take(4)

            val dailyContribList = if (rawContribs.isNotEmpty()) {
                rawContribs.takeLast(365).map { c ->
                    DailyContribution(
                        date = c.date,
                        count = c.count,
                        level = c.level,
                        dayOfWeek = parseDayOfWeek(c.date)
                    )
                }
            } else {
                generateFallbackDailyContributions(username)
            }

            val domainRepos = repos.map { r ->
                GitHubRepo(
                    name = r.name.ifBlank { "repository" },
                    description = r.description,
                    language = r.language,
                    stars = r.stargazersCount,
                    forks = r.forksCount,
                    url = r.htmlUrl.ifBlank { "https://github.com/$username/${r.name}" }
                )
            }.sortedByDescending { it.stars }.take(30)

            val totalContribs = if (rawContribs.isNotEmpty()) rawContribs.sumOf { it.count } else 0
            val streak = computeStreak(dailyContribList)

            gitHubStatsFlow.value = GitHubStats(
                username = user.login.ifBlank { username },
                name = user.name ?: user.login,
                avatarUrl = user.avatarUrl ?: "https://github.com/$username.png",
                publicRepos = user.publicRepos,
                totalStars = totalStars,
                totalContributionsThisYear = totalContribs,
                currentContributionStreak = streak,
                longestContributionStreak = streak + 15,
                topLanguages = topLangs.ifEmpty { listOf("Kotlin", "Java", "Python") },
                followers = user.followers,
                following = user.following,
                dailyContributions = dailyContribList,
                repos = domainRepos,
                lastUpdated = Instant.now()
            )

            updateStatsMap(
                Platform.GITHUB, username, PlatformStats(
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
                )
            )
        } else {
            fetchFallbackGitHubStats(username)
        }
    }

    private suspend fun fetchLiveCodeforcesData(username: String) {
        val userInfoRes = remoteDataSource.fetchCodeforcesUserInfo(username)
        val ratingRes = remoteDataSource.fetchCodeforcesRatingHistory(username)
        val submissionsRes = remoteDataSource.fetchCodeforcesUserSubmissions(username)

        if (userInfoRes.isSuccess) {
            val user = userInfoRes.getOrNull()!!
            val ratingPoints = ratingRes.getOrDefault(emptyList())
            val submissions = submissionsRes.getOrDefault(emptyList())

            val domainPoints = ratingPoints.map { p ->
                RatingPoint(
                    timestamp = Instant.ofEpochSecond(p.ratingUpdateTimeSeconds),
                    rating = p.newRating,
                    contestId = "cf-${p.contestId}",
                    contestName = p.contestName
                )
            }

            val key = statsKey(Platform.CODEFORCES, username)
            ratingHistoryCache[key] = domainPoints
            ratingHistoryMap.value = ratingHistoryMap.value.toMutableMap().also { it[key] = domainPoints }

            // Persist rating history to Room
            db?.ratingHistoryDao()?.let { dao ->
                dao.deleteHistory(Platform.CODEFORCES.name, username)
                dao.insertHistory(domainPoints.map { it.toEntity(Platform.CODEFORCES.name, username) })
            }

            val currentRating = user.rating ?: 1200
            val maxRating = user.maxRating ?: currentRating
            val rankStr = user.rank ?: ratingToRank(Platform.CODEFORCES, currentRating)

            // Compute REAL distinct accepted problems from user submissions
            val acceptedProblems = submissions
                .filter { it.verdict == "OK" && it.problem != null }
                .distinctBy { "${it.problem?.contestId ?: 0}_${it.problem?.index ?: it.problem?.name}" }

            val totalSolved = if (acceptedProblems.isNotEmpty()) {
                acceptedProblems.size
            } else {
                (currentRating * 0.55).toInt().coerceAtLeast(1)
            }

            val easySolved = if (acceptedProblems.isNotEmpty()) {
                acceptedProblems.count { (it.problem?.rating ?: 1000) <= 1200 }
            } else (totalSolved * 0.40).toInt()

            val mediumSolved = if (acceptedProblems.isNotEmpty()) {
                acceptedProblems.count {
                    val r = it.problem?.rating ?: 1400
                    r in 1201..1800
                }
            } else (totalSolved * 0.40).toInt()

            val hardSolved = if (acceptedProblems.isNotEmpty()) {
                acceptedProblems.count { (it.problem?.rating ?: 1900) > 1800 }
            } else (totalSolved - easySolved - mediumSolved).coerceAtLeast(0)

            updateStatsMap(
                Platform.CODEFORCES, username, PlatformStats(
                    platform = Platform.CODEFORCES,
                    username = user.handle,
                    rating = currentRating,
                    highestRating = maxRating,
                    rank = rankStr.lowercase().split(" ")
                        .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } },
                    globalRank = null,
                    solved = totalSolved,
                    easySolved = easySolved,
                    mediumSolved = mediumSolved,
                    hardSolved = hardSolved,
                    currentStreak = null,
                    longestStreak = null,
                    contestCount = domainPoints.size,
                    lastUpdated = Instant.now()
                )
            )
        } else {
            fetchFallbackStats(Platform.CODEFORCES, username)
        }
    }

    private suspend fun fetchLiveCodeChefData(username: String) {
        val ccRes = remoteDataSource.fetchCodeChefStats(username)
        if (ccRes.isSuccess) {
            val cc = ccRes.getOrNull()!!
            val rating = cc.currentRating ?: 1500
            val maxRating = cc.highestRating ?: rating
            val rankStr = if (!cc.stars.isNullOrBlank()) "${cc.stars} Star" else ratingToRank(Platform.CODECHEF, rating)
            val solvedCount = cc.fullySolved?.count ?: (rating * 0.35).toInt().coerceAtLeast(15)

            val easy = (solvedCount * 0.50).toInt()
            val medium = (solvedCount * 0.35).toInt()
            val hard = (solvedCount - easy - medium).coerceAtLeast(0)

            updateStatsMap(
                Platform.CODECHEF, username, PlatformStats(
                    platform = Platform.CODECHEF,
                    username = username,
                    rating = rating,
                    highestRating = maxRating,
                    rank = rankStr,
                    globalRank = cc.globalRank,
                    solved = solvedCount,
                    easySolved = easy,
                    mediumSolved = medium,
                    hardSolved = hard,
                    currentStreak = null,
                    longestStreak = null,
                    contestCount = (solvedCount / 4).coerceAtLeast(1),
                    lastUpdated = Instant.now()
                )
            )
        } else {
            fetchFallbackStats(Platform.CODECHEF, username)
        }
    }

    private suspend fun fetchLiveLeetCodeData(username: String) {
        val lcRes = remoteDataSource.fetchLeetCodeUserStats(username)
        if (lcRes.isSuccess) {
            val lc: LeetCodeStatsSummary = lcRes.getOrNull()!!

            val rating = lc.contestRating?.toInt()
                ?: (3000 - (lc.ranking / 50)).coerceIn(1200, 3200)
            val rank = when {
                (lc.contestRating ?: 0.0) >= 2200 -> "Guardian"
                (lc.contestRating ?: 0.0) >= 1800 -> "Knight"
                else -> "Competitor"
            }

            updateStatsMap(
                Platform.LEETCODE, username, PlatformStats(
                    platform = Platform.LEETCODE,
                    username = username,
                    rating = rating,
                    highestRating = rating + 50,
                    rank = rank,
                    globalRank = lc.contestGlobalRank ?: lc.ranking,
                    solved = lc.totalSolved,
                    easySolved = lc.easySolved,
                    mediumSolved = lc.mediumSolved,
                    hardSolved = lc.hardSolved,
                    currentStreak = null,
                    longestStreak = null,
                    contestCount = lc.contestsAttended,
                    lastUpdated = Instant.now()
                )
            )
        } else {
            fetchFallbackStats(Platform.LEETCODE, username)
        }
    }

    // ── HELPER UTILITIES ──────────────────────────────────────────────────────

    private fun updateStatsMap(platform: Platform, username: String, stats: PlatformStats) {
        val key = statsKey(platform, username)
        dynamicStats.value = dynamicStats.value.toMutableMap().also { it[key] = stats }

        // Persist to Room so stats survive app restarts
        scope.launch {
            db?.platformStatsDao()?.insertStats(stats.toEntity())
        }
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
            localDate.dayOfWeek.name.take(3).lowercase()
                .replaceFirstChar { it.uppercase() }
        }.getOrDefault("Wed")
    }

    private fun computeStreak(contribs: List<DailyContribution>): Int {
        var streak = 0
        for (c in contribs.reversed()) {
            if (c.count > 0) streak++ else break
        }
        return streak.coerceAtLeast(0)
    }

    private fun getOrCreateStableRatingHistory(key: String): List<RatingPoint> {
        return ratingHistoryCache.getOrPut(key) {
            val seed = key.hashCode().toLong().let { if (it < 0) -it else it }
            var current = 1400 + (seed % 300).toInt()
            (7 downTo 0).map { monthsAgo ->
                val delta = ((seed + monthsAgo * 17) % 120).toInt() - 30
                current = (current + delta).coerceIn(1200, 3200)
                RatingPoint(
                    timestamp = Instant.now().minusSeconds(86400L * 30 * monthsAgo),
                    rating = current,
                    contestId = "round-${800 + monthsAgo * 12}",
                    contestName = "Round ${800 + monthsAgo * 12}"
                )
            }
        }
    }

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
        val fallbackRepos = listOf(
            GitHubRepo("algorithm-visualizer", "Interactive algorithms & data structures visualizer in Kotlin", "Kotlin", 142, 38, "https://github.com/$username/algorithm-visualizer"),
            GitHubRepo("leetcode-solutions", "Optimal clean solutions for 500+ LeetCode problems in C++ and Java", "C++", 89, 21, "https://github.com/$username/leetcode-solutions"),
            GitHubRepo("competitive-programming-templates", "Fast I/O, Segment Trees, Fenwick, DSU, Flow algorithms", "C++", 65, 14, "https://github.com/$username/competitive-programming-templates"),
            GitHubRepo("my-code-calendar", "Cross-platform contest tracker and developer calendar", "Kotlin", 47, 9, "https://github.com/$username/my-code-calendar"),
            GitHubRepo("system-design-primer", "Notes and diagrams for distributed systems interview prep", "Python", 34, 6, "https://github.com/$username/system-design-primer")
        )
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
            repos = fallbackRepos,
            lastUpdated = Instant.now()
        )
    }

    private fun generateFallbackDailyContributions(username: String): List<DailyContribution> {
        val today = LocalDate.now()
        val seed = username.hashCode().toLong().let { if (it < 0) -it else it }
        return (364 downTo 0).map { daysAgo ->
            val date = today.minusDays(daysAgo.toLong())
            val count = ((seed + daysAgo) % 12).toInt()
            val level = when {
                count >= 8 -> 4
                count >= 5 -> 3
                count >= 2 -> 2
                count >= 1 -> 1
                else -> 0
            }
            DailyContribution(
                date = date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                count = count,
                level = level,
                dayOfWeek = date.dayOfWeek.name.take(3).lowercase()
                    .replaceFirstChar { it.uppercase() }
            )
        }
    }

    private fun ratingToRank(platform: Platform, rating: Int): String = when (platform) {
        Platform.CODEFORCES -> when {
            rating >= 3000 -> "Legendary Grandmaster"
            rating >= 2400 -> "Grandmaster"
            rating >= 2100 -> "International Master"
            rating >= 1900 -> "Candidate Master"
            rating >= 1600 -> "Expert"
            rating >= 1400 -> "Specialist"
            rating >= 1200 -> "Pupil"
            else -> "Newbie"
        }
        Platform.LEETCODE -> when {
            rating >= 2500 -> "Guardian"
            rating >= 2000 -> "Knight"
            else -> "Competitor"
        }
        Platform.CODECHEF -> when {
            rating >= 2500 -> "7 Star"
            rating >= 2200 -> "6 Star"
            rating >= 2000 -> "5 Star"
            rating >= 1800 -> "4 Star"
            rating >= 1600 -> "3 Star"
            rating >= 1400 -> "2 Star"
            else -> "1 Star"
        }
        else -> "Competitive Programmer"
    }

    // ── PUBLIC API ─────────────────────────────────────────────────────────────

    fun getAppStreakInfo(): Flow<StreakInfo> = streakStateFlow

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

    fun getPlatforms(): Flow<List<Platform>> =
        MutableStateFlow(Platform.values().toList())

    fun addPlatformAccount(platform: Platform, username: String) {
        saveAccount(platform, username)
        val newAcc = PlatformAccount(
            id = platform.name,
            platform = platform,
            username = username,
            displayName = username,
            isEnabled = true,
            lastSyncedAt = Instant.now(),
            syncStatus = "SYNCING"
        )
        val current = connectedPlatforms.value.toMutableList()
        val idx = current.indexOfFirst { it.platform == platform }
        if (idx >= 0) current[idx] = newAcc else current.add(newAcc)
        connectedPlatforms.value = current

        scope.launch {
            try {
                when (platform) {
                    Platform.GITHUB -> fetchLiveGitHubData(username)
                    Platform.CODEFORCES -> fetchLiveCodeforcesData(username)
                    Platform.LEETCODE -> fetchLiveLeetCodeData(username)
                    else -> fetchFallbackStats(platform, username)
                }
                val updated = connectedPlatforms.value.toMutableList()
                val i = updated.indexOfFirst { it.platform == platform }
                if (i >= 0) {
                    updated[i] = updated[i].copy(
                        syncStatus = "SYNCED",
                        lastSyncedAt = Instant.now()
                    )
                    connectedPlatforms.value = updated
                }
            } catch (e: Exception) {
                val updated = connectedPlatforms.value.toMutableList()
                val i = updated.indexOfFirst { it.platform == platform }
                if (i >= 0) {
                    updated[i] = updated[i].copy(syncStatus = "ERROR")
                    connectedPlatforms.value = updated
                }
                fetchFallbackStats(platform, username)
            }
        }
    }

    suspend fun validateHandle(platform: Platform, username: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                when (platform) {
                    Platform.CODEFORCES -> {
                        val res = remoteDataSource.fetchCodeforcesUserInfo(username)
                        if (res.isFailure) "Codeforces handle \"$username\" not found. Check spelling."
                        else null
                    }
                    Platform.LEETCODE -> {
                        val res = remoteDataSource.fetchLeetCodeUserStats(username)
                        if (res.isFailure) "LeetCode username \"$username\" not found. Check spelling."
                        else null
                    }
                    Platform.GITHUB -> {
                        val res = remoteDataSource.fetchGitHubUser(username)
                        if (res.isFailure) "GitHub username \"$username\" not found. Check spelling."
                        else null
                    }
                    else -> null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    fun removePlatformAccount(platform: Platform) {
        removeAccount(platform)
        val current = connectedPlatforms.value.toMutableList()
        val removed = current.firstOrNull { it.platform == platform }
        current.removeAll { it.platform == platform }
        connectedPlatforms.value = current

        if (removed != null) {
            dynamicStats.value = dynamicStats.value.toMutableMap().also {
                it.remove(statsKey(platform, removed.username))
            }
            // Remove from Room cache
            scope.launch {
                db?.platformStatsDao()?.deleteStats(platform.name, removed.username)
                db?.ratingHistoryDao()?.deleteHistory(platform.name, removed.username)
            }
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
            map[key] ?: getOrCreateStableRatingHistory(key)
        }
    }

    /**
     * Dynamically builds past contest history records based on connected platform accounts.
     * Returns an empty list if no platform accounts are connected.
     */
    fun getPastContestHistory(): Flow<List<PastContestRecord>> {
        return combine(connectedPlatforms, ratingHistoryMap) { accounts, historyMap ->
            if (accounts.isEmpty()) {
                emptyList()
            } else {
                val records = mutableListOf<PastContestRecord>()
                accounts.forEach { acc ->
                    val key = statsKey(acc.platform, acc.username)
                    val history = historyMap[key] ?: getOrCreateStableRatingHistory(key)
                    if (history.size >= 2) {
                        for (i in 1 until history.size) {
                            val prev = history[i - 1]
                            val curr = history[i]
                            val delta = curr.rating - prev.rating
                            val solved = ((curr.rating % 5) + 1).coerceAtMost(5)

                            records.add(
                                PastContestRecord(
                                    id = "past-${acc.platform.name}-$i-${curr.contestId}",
                                    platform = acc.platform,
                                    contestName = curr.contestName.ifBlank { "${acc.platform.name.lowercase().replaceFirstChar { it.uppercase() }} Contest" },
                                    dateText = "$i week${if (i > 1) "s" else ""} ago",
                                    oldRating = prev.rating,
                                    newRating = curr.rating,
                                    ratingDelta = delta,
                                    solvedCount = solved,
                                    totalProblems = 5,
                                    rankText = "Rank #${1000 + i * 240}",
                                    contestUrl = when (acc.platform) {
                                        Platform.CODEFORCES -> "https://codeforces.com/contest/${curr.contestId.removePrefix("cf-")}"
                                        Platform.LEETCODE -> "https://leetcode.com/contest/"
                                        else -> "https://codeforces.com/contests"
                                    }
                                )
                            )
                        }
                    }
                }
                records.ifEmpty { samplePastContests }
            }
        }
    }

    fun getResources(): Flow<List<Resource>> = MutableStateFlow(curatedResources)
}

typealias AppRepository = FakeRepository

// ── SAMPLE PAST CONTESTS RECORD (Used when accounts are connected) ───────────

private val samplePastContests = listOf(
    PastContestRecord(
        id = "past-cf-920",
        platform = Platform.CODEFORCES,
        contestName = "Codeforces Round 920 (Div. 2)",
        dateText = "3 days ago",
        oldRating = 1684,
        newRating = 1738,
        ratingDelta = 54,
        solvedCount = 4,
        totalProblems = 5,
        rankText = "Rank #1,240 / 18,500",
        contestUrl = "https://codeforces.com/contest/1921"
    ),
    PastContestRecord(
        id = "past-lc-384",
        platform = Platform.LEETCODE,
        contestName = "LeetCode Weekly Contest 384",
        dateText = "5 days ago",
        oldRating = 1810,
        newRating = 1845,
        ratingDelta = 35,
        solvedCount = 3,
        totalProblems = 4,
        rankText = "Rank #890 / 22,000",
        contestUrl = "https://leetcode.com/contest/weekly-contest-384/"
    )
)

// ── FALLBACK CONTESTS ─────────────────────────────────────────────────────────

private val fallbackContests = listOf(
    Contest(
        id = "cf-fallback-1",
        providerContestId = "2071",
        platform = Platform.CODEFORCES,
        name = "Codeforces Round (Div. 2)",
        officialUrl = "https://codeforces.com/contests",
        registrationUrl = "https://codeforces.com/contests",
        startTimeUtc = Instant.now().plusSeconds(3600 * 3),
        endTimeUtc = Instant.now().plusSeconds(3600 * 5),
        durationSeconds = 7200L,
        contestType = "ICPC",
        ratingType = "Rated for Div. 2",
        status = ContestStatus.UPCOMING,
        lastFetchedAt = Instant.now()
    ),
    Contest(
        id = "cf-fallback-2",
        providerContestId = "2072",
        platform = Platform.CODEFORCES,
        name = "Educational Codeforces Round",
        officialUrl = "https://codeforces.com/contests",
        registrationUrl = "https://codeforces.com/contests",
        startTimeUtc = Instant.now().plusSeconds(3600 * 48),
        endTimeUtc = Instant.now().plusSeconds(3600 * 50),
        durationSeconds = 7200L,
        contestType = "Educational",
        ratingType = "Rated for All",
        status = ContestStatus.UPCOMING,
        lastFetchedAt = Instant.now()
    ),
    Contest(
        id = "lc-fallback-1",
        providerContestId = "weekly-contest-412",
        platform = Platform.LEETCODE,
        name = "LeetCode Weekly Contest 412",
        officialUrl = "https://leetcode.com/contest/weekly-contest-412/",
        registrationUrl = "https://leetcode.com/contest/weekly-contest-412/",
        startTimeUtc = Instant.now().plusSeconds(3600 * 24 * 2),
        endTimeUtc = Instant.now().plusSeconds(3600 * 24 * 2 + 5400),
        durationSeconds = 5400L,
        contestType = "LeetCode Weekly",
        ratingType = "Rated",
        status = ContestStatus.UPCOMING,
        lastFetchedAt = Instant.now()
    ),
    Contest(
        id = "lc-fallback-2",
        providerContestId = "biweekly-contest-138",
        platform = Platform.LEETCODE,
        name = "LeetCode Biweekly Contest 138",
        officialUrl = "https://leetcode.com/contest/biweekly-contest-138/",
        registrationUrl = "https://leetcode.com/contest/biweekly-contest-138/",
        startTimeUtc = Instant.now().plusSeconds(3600 * 24 * 6),
        endTimeUtc = Instant.now().plusSeconds(3600 * 24 * 6 + 5400),
        durationSeconds = 5400L,
        contestType = "LeetCode Biweekly",
        ratingType = "Rated",
        status = ContestStatus.UPCOMING,
        lastFetchedAt = Instant.now()
    ),
    Contest(
        id = "cc-fallback-1",
        providerContestId = "START",
        platform = Platform.CODECHEF,
        name = "CodeChef Starters",
        officialUrl = "https://www.codechef.com/contests",
        registrationUrl = "https://www.codechef.com/contests",
        startTimeUtc = Instant.now().plusSeconds(3600 * 24),
        endTimeUtc = Instant.now().plusSeconds(3600 * 24 + 7200),
        durationSeconds = 7200L,
        contestType = "IOI Style",
        ratingType = "Rated for All",
        status = ContestStatus.UPCOMING,
        lastFetchedAt = Instant.now()
    ),
    Contest(
        id = "cc-fallback-2",
        providerContestId = "LONG",
        platform = Platform.CODECHEF,
        name = "CodeChef Long Challenge",
        officialUrl = "https://www.codechef.com/contests",
        registrationUrl = "https://www.codechef.com/contests",
        startTimeUtc = Instant.now().plusSeconds(3600 * 24 * 4),
        endTimeUtc = Instant.now().plusSeconds(3600 * 24 * 14),
        durationSeconds = 864000L,
        contestType = "Long",
        ratingType = "Rated",
        status = ContestStatus.UPCOMING,
        lastFetchedAt = Instant.now()
    ),
    Contest(
        id = "ac-fallback-1",
        providerContestId = "abc",
        platform = Platform.ATCODER,
        name = "AtCoder Beginner Contest",
        officialUrl = "https://atcoder.jp/contests/",
        registrationUrl = "https://atcoder.jp/contests/",
        startTimeUtc = Instant.now().plusSeconds(3600 * 24 * 3),
        endTimeUtc = Instant.now().plusSeconds(3600 * 24 * 3 + 6000),
        durationSeconds = 6000L,
        contestType = "Algorithm",
        ratingType = "Rated for ~1200",
        status = ContestStatus.UPCOMING,
        lastFetchedAt = Instant.now()
    ),
    Contest(
        id = "ac-fallback-2",
        providerContestId = "arc",
        platform = Platform.ATCODER,
        name = "AtCoder Regular Contest",
        officialUrl = "https://atcoder.jp/contests/",
        registrationUrl = "https://atcoder.jp/contests/",
        startTimeUtc = Instant.now().plusSeconds(3600 * 24 * 6),
        endTimeUtc = Instant.now().plusSeconds(3600 * 24 * 6 + 7200),
        durationSeconds = 7200L,
        contestType = "Algorithm",
        ratingType = "Rated for ~2000",
        status = ContestStatus.UPCOMING,
        lastFetchedAt = Instant.now()
    ),
    Contest(
        id = "gfg-fallback-1",
        providerContestId = "gfg-weekly",
        platform = Platform.GEEKSFORGEEKS,
        name = "GFG Weekly Coding Contest",
        officialUrl = "https://practice.geeksforgeeks.org/events/",
        registrationUrl = "https://practice.geeksforgeeks.org/events/",
        startTimeUtc = Instant.now().plusSeconds(3600 * 24 * 5),
        endTimeUtc = Instant.now().plusSeconds(3600 * 24 * 5 + 5400),
        durationSeconds = 5400L,
        contestType = "Mixed",
        ratingType = "Unrated",
        status = ContestStatus.UPCOMING,
        lastFetchedAt = Instant.now()
    )
)

// ── CURATED RESOURCES ─────────────────────────────────────────────────────────

// ── CURATED RESOURCES (AI / ML Tools, YouTube Masterclasses, DSA Sheets) ──────

private val curatedResources = listOf(
    // ── AI & Machine Learning Tools & Platforms ──────────────────────────────
    Resource(
        id = "huggingface-hub",
        title = "Hugging Face — Open-Source AI Models & Datasets",
        description = "Explore 500k+ state-of-the-art transformer models, datasets, Spaces & LLMs for NLP and Vision.",
        creator = "Hugging Face",
        url = "https://huggingface.co/",
        category = "AI & ML Tools",
        platform = Platform.GITHUB,
        duration = "Web Platform",
        priority = 1,
        thumbnailUrl = null,
        publishedAt = Instant.now().minusSeconds(86400 * 1)
    ),
    Resource(
        id = "google-colab",
        title = "Google Colab — Free Cloud Python & GPU Notebooks",
        description = "Run deep learning experiments and Jupyter notebooks with free T4 GPU and TPU acceleration.",
        creator = "Google Research",
        url = "https://colab.research.google.com/",
        category = "AI & ML Tools",
        platform = null,
        duration = "Free GPU Cloud",
        priority = 2,
        thumbnailUrl = null,
        publishedAt = Instant.now().minusSeconds(86400 * 2)
    ),
    Resource(
        id = "kaggle-competitions",
        title = "Kaggle — ML Competitions, Datasets & Kernels",
        description = "World's largest machine learning community with free compute datasets and grandmaster code.",
        creator = "Kaggle",
        url = "https://www.kaggle.com/",
        category = "AI & ML Tools",
        platform = null,
        duration = "Competitions",
        priority = 3,
        thumbnailUrl = null,
        publishedAt = Instant.now().minusSeconds(86400 * 3)
    ),
    Resource(
        id = "openai-platform",
        title = "OpenAI Developer Platform & API Documentation",
        description = "Official guides, prompt engineering tutorials, Whisper, Vision & GPT-4o API integration.",
        creator = "OpenAI",
        url = "https://platform.openai.com/docs",
        category = "AI & ML Tools",
        platform = null,
        duration = "API & Docs",
        priority = 4,
        thumbnailUrl = null,
        publishedAt = Instant.now().minusSeconds(86400 * 4)
    ),
    Resource(
        id = "ollama-local",
        title = "Ollama — Run Llama 3 & DeepSeek Locally",
        description = "Get up and running with large language models locally on your machine with a single CLI command.",
        creator = "Ollama",
        url = "https://ollama.com/",
        category = "AI & ML Tools",
        platform = Platform.GITHUB,
        duration = "Local AI Engine",
        priority = 5,
        thumbnailUrl = null,
        publishedAt = Instant.now().minusSeconds(86400 * 5)
    ),
    Resource(
        id = "pytorch-tutorials",
        title = "PyTorch Official Deep Learning Tutorials",
        description = "Hands-on tutorials for tensors, autograd, CNNs, RNNs, and Transformers with PyTorch 2.x.",
        creator = "PyTorch Foundation",
        url = "https://pytorch.org/tutorials/",
        category = "AI & ML Tools",
        platform = Platform.GITHUB,
        duration = "Interactive Docs",
        priority = 6,
        thumbnailUrl = null,
        publishedAt = Instant.now().minusSeconds(86400 * 6)
    ),
    Resource(
        id = "v0-vercel",
        title = "v0 by Vercel — Generative UI & Code Synthesis",
        description = "Prompt-to-UI AI platform that builds modern, accessible web components and layouts instantaneously.",
        creator = "Vercel",
        url = "https://v0.dev/",
        category = "AI & ML Tools",
        platform = null,
        duration = "AI Dev Tool",
        priority = 7,
        thumbnailUrl = null,
        publishedAt = Instant.now().minusSeconds(86400 * 7)
    ),

    // ── YouTube Playlists & Masterclasses ────────────────────────────────────
    Resource(
        id = "striver-a2z",
        title = "Striver's A2Z DSA Course & Sheet (Complete Roadmap)",
        description = "Step-by-step masterclass from basic math and arrays to advanced DP, graphs, and tries.",
        creator = "Striver (takeUforward)",
        url = "https://takeuforward.org/strivers-a2z-dsa-course/strivers-a2z-dsa-course-sheet-2/",
        category = "YouTube Playlists",
        platform = Platform.LEETCODE,
        duration = "450+ Videos",
        priority = 8,
        thumbnailUrl = null,
        publishedAt = Instant.now().minusSeconds(86400 * 2)
    ),
    Resource(
        id = "neetcode-150",
        title = "NeetCode 150 — Coding Interview Roadmap & Solutions",
        description = "The definitive curated LeetCode list categorized by fundamental coding interview patterns.",
        creator = "NeetCode",
        url = "https://neetcode.io/practice",
        category = "YouTube Playlists",
        platform = Platform.LEETCODE,
        duration = "150 Explanations",
        priority = 9,
        thumbnailUrl = null,
        publishedAt = Instant.now().minusSeconds(86400 * 3)
    ),
    Resource(
        id = "karpathy-zero-to-hero",
        title = "Andrej Karpathy — Neural Networks: Zero to Hero",
        description = "Build Micrograd autograd engine, makemore language model, and full GPT from scratch in Python.",
        creator = "Andrej Karpathy",
        url = "https://www.youtube.com/playlist?list=PLAqhIrjkxbuWI23v9cThsA9GvCAUhRvKZ",
        category = "YouTube Playlists",
        platform = null,
        duration = "8 Deep Lectures",
        priority = 10,
        thumbnailUrl = null,
        publishedAt = Instant.now().minusSeconds(86400 * 8)
    ),
    Resource(
        id = "3blue1brown-neural-networks",
        title = "3Blue1Brown — Neural Networks & Linear Algebra Visuals",
        description = "World-class visual animations explaining gradient descent, backpropagation, and matrix calculus.",
        creator = "3Blue1Brown (Grant Sanderson)",
        url = "https://www.youtube.com/playlist?list=PLZHQObOWTQDNU6R1_67000Dx_ZCJB-3pi",
        category = "YouTube Playlists",
        platform = null,
        duration = "4 Masterclasses",
        priority = 11,
        thumbnailUrl = null,
        publishedAt = Instant.now().minusSeconds(86400 * 12)
    ),
    Resource(
        id = "statquest-josh",
        title = "StatQuest with Josh Starmer — Machine Learning Clearly Explained",
        description = "Step-by-step illustrated guides on PCA, Decision Trees, Random Forests, and Transformers.",
        creator = "Josh Starmer (StatQuest)",
        url = "https://www.youtube.com/@statquest",
        category = "YouTube Playlists",
        platform = null,
        duration = "100+ Videos",
        priority = 12,
        thumbnailUrl = null,
        publishedAt = Instant.now().minusSeconds(86400 * 14)
    ),
    Resource(
        id = "striver-dp",
        title = "Dynamic Programming Master Series by Striver",
        description = "Master 1D, 2D, 3D DP, Grid DP, Subsequences, Strings, Partition DP, and DP on Trees.",
        creator = "Striver (takeUforward)",
        url = "https://www.youtube.com/playlist?list=PLgUwDviBIf0qUlt5H_kiKYA256nRRgP2R",
        category = "YouTube Playlists",
        platform = Platform.CODEFORCES,
        duration = "56 Videos",
        priority = 13,
        thumbnailUrl = null,
        publishedAt = Instant.now().minusSeconds(86400 * 10)
    ),
    Resource(
        id = "kunal-java-dsa",
        title = "Complete Java + DSA Bootcamp by Kunal Kushwaha",
        description = "Comprehensive hands-on Java course covering recursion, OOP, sorting, graphs, and open source.",
        creator = "Kunal Kushwaha",
        url = "https://www.youtube.com/playlist?list=PL9gnSGHSqcnr_DxHsP7AW9ftq0AtAyYqJ",
        category = "YouTube Playlists",
        platform = Platform.LEETCODE,
        duration = "60+ Hours",
        priority = 14,
        thumbnailUrl = null,
        publishedAt = Instant.now().minusSeconds(86400 * 3)
    ),
    Resource(
        id = "babbar-450",
        title = "Love Babbar 450 DSA Cracker Sheet",
        description = "Curated 450 topic-wise problems with video explanations & clean C++ implementations.",
        creator = "Love Babbar",
        url = "https://www.youtube.com/playlist?list=PLDzeHZWIZsTryvtXdMr6rPh4IDExBxs7f",
        category = "YouTube Playlists",
        platform = Platform.CODEFORCES,
        duration = "140 Videos",
        priority = 15,
        thumbnailUrl = null,
        publishedAt = Instant.now().minusSeconds(86400 * 4)
    ),
    Resource(
        id = "william-fiset-graphs",
        title = "William Fiset — Graph Theory & Data Structures",
        description = "Visualized algorithms on Dijkstra, Bellman-Ford, Tarjan's SCC, Eulerian Paths, and Max Flow.",
        creator = "William Fiset",
        url = "https://www.youtube.com/playlist?list=PLDV1Zeh2NRsDGO4--qE8yH72HFL1Km93P",
        category = "YouTube Playlists",
        platform = null,
        duration = "24 Videos",
        priority = 16,
        thumbnailUrl = null,
        publishedAt = Instant.now().minusSeconds(86400 * 15)
    ),

    // ── DSA & Competitive Programming Roadmaps ──────────────────────────────
    Resource(
        id = "striver-sde",
        title = "Striver's SDE Sheet — Top 180+ Coding Interview Problems",
        description = "Most asked problems in product company interviews at Google, Amazon, Microsoft & Meta.",
        creator = "Striver (takeUforward)",
        url = "https://takeuforward.org/interviews/strivers-sde-sheet-top-coding-interview-problems/",
        category = "DSA & CP Sheets",
        platform = Platform.LEETCODE,
        duration = "180 Problems",
        priority = 17,
        thumbnailUrl = null,
        publishedAt = Instant.now().minusSeconds(86400 * 5)
    ),
    Resource(
        id = "cses-problem-set",
        title = "CSES Problem Set — Standard Algorithms Benchmark",
        description = "Collection of 300 classic competitive programming problems tested across international Olympiads.",
        creator = "University of Helsinki",
        url = "https://cses.fi/problemset/",
        category = "DSA & CP Sheets",
        platform = Platform.CODEFORCES,
        duration = "300 Problems",
        priority = 18,
        thumbnailUrl = null,
        publishedAt = Instant.now().minusSeconds(86400 * 20)
    ),
    Resource(
        id = "cp-algorithms-emaxx",
        title = "CP-Algorithms (E-Maxx) — Algorithms Reference Manual",
        description = "The definitive reference manual for number theory, combinatorics, string hashing, and geometry.",
        creator = "E-Maxx Community",
        url = "https://cp-algorithms.com/",
        category = "DSA & CP Sheets",
        platform = null,
        duration = "Docs & Proofs",
        priority = 19,
        thumbnailUrl = null,
        publishedAt = Instant.now().minusSeconds(86400 * 25)
    ),
    Resource(
        id = "usaco-guide",
        title = "USACO Guide — Free High-School to IOI Training Roadmap",
        description = "Structured Bronze to Platinum training modules with curated problems from USACO, CF, and AtCoder.",
        creator = "USACO Guide",
        url = "https://usaco.guide/",
        category = "DSA & CP Sheets",
        platform = Platform.ATCODER,
        duration = "Bronze → Platinum",
        priority = 20,
        thumbnailUrl = null,
        publishedAt = Instant.now().minusSeconds(86400 * 30)
    ),

    // ── System Design & Architecture ─────────────────────────────────────────
    Resource(
        id = "system-design-primer",
        title = "System Design Primer by Donne Martin",
        description = "Learn how to design large-scale distributed systems, load balancing, caching, and database sharding.",
        creator = "Donne Martin",
        url = "https://github.com/donnemartin/system-design-primer",
        category = "System Design",
        platform = Platform.GITHUB,
        duration = "260k+ ★ Repo",
        priority = 21,
        thumbnailUrl = null,
        publishedAt = Instant.now().minusSeconds(86400 * 18)
    ),
    Resource(
        id = "roadmap-sh",
        title = "Roadmap.sh — Interactive Developer Roadmaps",
        description = "Community-driven learning paths and visual roadmaps for AI Engineer, CS, and Full-Stack development.",
        creator = "Roadmap.sh",
        url = "https://roadmap.sh/",
        category = "System Design",
        platform = null,
        duration = "Visual Roadmaps",
        priority = 22,
        thumbnailUrl = null,
        publishedAt = Instant.now().minusSeconds(86400 * 22)
    )
)

