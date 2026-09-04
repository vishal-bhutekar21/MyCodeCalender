package com.mycodecalendar.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.mycodecalendar.core.database.MyCodeCalendarDatabase
import com.mycodecalendar.core.database.entity.GitHubStatsEntity
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
import kotlinx.serialization.json.Json
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
    private val context: Context? = null,
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

    /** Lenient JSON for parsing serialized GitHub entity fields */
    private val jsonSerializer = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

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

    private val contestsFlow = MutableStateFlow<List<Contest>>(emptyList())

    private val watchlistPrefs: SharedPreferences? = context?.getSharedPreferences(
        "contest_watchlist_prefs", Context.MODE_PRIVATE
    )

    private val watchedContestIdsFlow = MutableStateFlow<Set<String>>(loadWatchedContestIds())

    private val dailyProblemFlow = MutableStateFlow<DailyProblem?>(getCuratedDailyProblem())

    private val resourcesFlow = MutableStateFlow<List<Resource>>(curatedResources)

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

    /**
     * Checks if a valid user session is authenticated.
     * Prevents unauthenticated/guest sessions from loading or displaying previous user caches.
     */
    private fun isUserLoggedIn(): Boolean {
        val ctx = context ?: return false
        val authPrefs = ctx.getSharedPreferences("app_auth_prefs", Context.MODE_PRIVATE)
        val isLoggedIn = authPrefs.getBoolean("is_logged_in", false)
        return isLoggedIn
    }

    // ── OFFLINE-FIRST CACHE SEEDING ───────────────────────────────────────────

    /**
     * Loads previously cached data from Room and pushes it into the in-memory flows.
     * Contests are public and always seeded. User-specific data is only seeded when a valid user is logged in.
     */
    private suspend fun seedFromCache() {
        val database = db ?: return

        // 1. Purge any stale synthetic or tutorial tracks from Room
        try {
            database.contestDao().purgeInvalidContests()
        } catch (_: Exception) {}

        // 2. Seed contests (public catalog)
        val contestList = try {
            database.contestDao().getAllContestsList()
        } catch (_: Exception) { emptyList() }

        if (contestList.isNotEmpty()) {
            val minValidTime = Instant.parse("2025-01-01T00:00:00Z")
            val cleanList = contestList.filterNot { entity ->
                entity.id.contains("fallback", ignoreCase = true) ||
                entity.id.contains("abc472", ignoreCase = true) ||
                entity.id.contains("starters-252", ignoreCase = true) ||
                entity.id.contains("weekly-515", ignoreCase = true) ||
                entity.id.startsWith("innovik-6", ignoreCase = true) ||
                entity.id.contains("APG4b", ignoreCase = true) ||
                entity.id.contains("abs", ignoreCase = true) ||
                entity.id.contains("adt", ignoreCase = true) ||
                entity.durationSeconds >= 2592000L ||
                entity.durationSeconds <= 0L ||
                entity.startTimeUtc.isBefore(minValidTime) ||
                entity.name.contains("Programming Guide", ignoreCase = true) ||
                entity.name.contains("Beginners Selection", ignoreCase = true) ||
                entity.name.contains("Daily Training", ignoreCase = true) ||
                entity.name.contains("Practice", ignoreCase = true) ||
                entity.name.contains("Tutorial", ignoreCase = true) ||
                entity.name.contains("入門")
            }
            if (cleanList.size != contestList.size) {
                try {
                    database.contestDao().deleteAllContests()
                    if (cleanList.isNotEmpty()) {
                        database.contestDao().insertContests(cleanList)
                    }
                } catch (_: Exception) {}
            }
            contestsFlow.value = cleanList.map { it.toDomain() }
        } else {
            contestsFlow.value = emptyList()
        }

        // 3. Load saved platform accounts from SharedPreferences
        val savedAccounts = loadSavedAccounts()
        if (savedAccounts.isNotEmpty()) {
            connectedPlatforms.value = savedAccounts
        }

        // Only clear user data if completely unauthenticated AND no connected platforms exist
        val loggedIn = isUserLoggedIn()
        if (!loggedIn && savedAccounts.isEmpty()) {
            connectedPlatforms.value = emptyList()
            dynamicStats.value = emptyMap()
            ratingHistoryMap.value = emptyMap()
            ratingHistoryCache.clear()
            gitHubStatsFlow.value = null
            return
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

            // Seed GitHub stats from Room cache for instant offline rendering
            if (acc.platform == Platform.GITHUB) {
                val cachedGh = database.gitHubStatsDao().getGitHubStats(acc.username)
                if (cachedGh != null) {
                    gitHubStatsFlow.value = cachedGh.toDomain(jsonSerializer)
                }
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
        if (!isUserLoggedIn()) return emptyList()
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
                fetchDailyProblemOfTheDay()
                connectedPlatforms.value.forEach { acc ->
                    when (acc.platform) {
                        Platform.GITHUB -> fetchLiveGitHubData(acc.username)
                        Platform.CODEFORCES -> fetchLiveCodeforcesData(acc.username)
                        Platform.LEETCODE -> fetchLiveLeetCodeData(acc.username)
                        Platform.CODECHEF -> fetchLiveCodeChefData(acc.username)
                        Platform.ATCODER -> fetchLiveAtCoderData(acc.username)
                        Platform.GEEKSFORGEEKS -> fetchLiveGeeksForGeeksData(acc.username)
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
        val nowEpoch = Instant.now().epochSecond

        // 1. Fetch Real LeetCode Contests via Official GraphQL
        try {
            val lcResult = remoteDataSource.fetchLeetCodeContests()
            if (lcResult.isSuccess) {
                val lcList = lcResult.getOrDefault(emptyList())
                lcList.filter { !it.isVirtual && (it.startTime + it.duration >= nowEpoch - 7200) }
                    .take(10)
                    .forEach { lc ->
                        val start = Instant.ofEpochSecond(lc.startTime)
                        val end = start.plusSeconds(lc.duration)
                        val status = computeStatus(start, end)
                        val slug = lc.titleSlug.ifBlank { lc.title.lowercase().replace(" ", "-") }
                        val id = "lc-$slug"
                        val contestType = if (lc.title.contains("Biweekly", ignoreCase = true)) "LeetCode Biweekly" else "LeetCode Weekly"
                        if (liveContests.none { it.id == id || it.name.equals(lc.title, ignoreCase = true) }) {
                            liveContests.add(
                                Contest(
                                    id = id,
                                    providerContestId = slug,
                                    platform = Platform.LEETCODE,
                                    name = lc.title,
                                    officialUrl = "https://leetcode.com/contest/$slug/",
                                    registrationUrl = "https://leetcode.com/contest/$slug/",
                                    startTimeUtc = start,
                                    endTimeUtc = end,
                                    durationSeconds = lc.duration,
                                    contestType = contestType,
                                    ratingType = "Rated",
                                    status = status,
                                    lastFetchedAt = Instant.now()
                                )
                            )
                        }
                    }
            }
        } catch (_: Exception) {}

        // 2. Fetch Codeforces Contests via Official API
        try {
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
        } catch (_: Exception) {}

        // 3. Fetch AtCoder Contests via Kenkoooo API
        try {
            val atcResult = remoteDataSource.fetchAtCoderContests()
            if (atcResult.isSuccess) {
                val atcList = atcResult.getOrDefault(emptyList())
                val minValidEpoch = nowEpoch - 86400L * 14 // Must have started at most 14 days ago if currently live
                atcList.filter { 
                    it.durationSecond in 600..2592000 && // real contest duration: between 10 mins and 30 days
                    it.startEpochSecond >= minValidEpoch && // NOT 1970! Must be recent or upcoming
                    (it.startEpochSecond + it.durationSecond >= nowEpoch) && // not ended yet
                    !it.title.contains("Programming Guide", ignoreCase = true) &&
                    !it.title.contains("Beginners Selection", ignoreCase = true) &&
                    !it.title.contains("Daily Training", ignoreCase = true) &&
                    !it.title.contains("Practice", ignoreCase = true) &&
                    !it.title.contains("Tutorial", ignoreCase = true) &&
                    !it.title.contains("Typical", ignoreCase = true) &&
                    !it.id.startsWith("APG4b", ignoreCase = true) &&
                    !it.id.startsWith("abs", ignoreCase = true) &&
                    !it.id.startsWith("adt", ignoreCase = true) &&
                    (it.title.contains("AtCoder", ignoreCase = true) || it.title.startsWith("abc", ignoreCase = true) || it.title.startsWith("arc", ignoreCase = true) || it.title.startsWith("agc", ignoreCase = true) || it.title.startsWith("ahc", ignoreCase = true))
                }
                .sortedBy { it.startEpochSecond }
                .take(15)
                .forEach { atc ->
                    val start = Instant.ofEpochSecond(atc.startEpochSecond)
                    val end = start.plusSeconds(atc.durationSecond)
                    val status = computeStatus(start, end)
                    val id = "atc-${atc.id}"
                    if (liveContests.none { it.id == id || it.name.equals(atc.title, ignoreCase = true) }) {
                        liveContests.add(
                            Contest(
                                id = id,
                                providerContestId = atc.id,
                                platform = Platform.ATCODER,
                                name = atc.title,
                                officialUrl = "https://atcoder.jp/contests/${atc.id}",
                                registrationUrl = "https://atcoder.jp/contests/${atc.id}",
                                startTimeUtc = start,
                                endTimeUtc = end,
                                durationSeconds = atc.durationSecond,
                                contestType = "AtCoder Official",
                                ratingType = if (atc.rateChange.isNotBlank() && atc.rateChange != "-") "Rated (${atc.rateChange})" else "Rated",
                                status = status,
                                lastFetchedAt = Instant.now()
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {}

        // 4. Fetch Real CodeChef Contests via Official CodeChef API
        try {
            val ccResult = remoteDataSource.fetchCodeChefContests()
            if (ccResult.isSuccess) {
                val ccList = ccResult.getOrDefault(emptyList())
                ccList.forEach { cc ->
                    val start = parseIsoInstant(cc.contestStartDateIso)
                    val end = parseIsoInstant(cc.contestEndDateIso)
                    if (start != null) {
                        val durationSec = cc.contestDuration.toLongOrNull()?.times(60) ?: 7200L
                        val actualEnd = end ?: start.plusSeconds(durationSec)
                        val status = computeStatus(start, actualEnd)
                        if (status != ContestStatus.ENDED) {
                            val id = "cc-${cc.contestCode}"
                            if (liveContests.none { it.id == id || it.name.equals(cc.contestName, ignoreCase = true) }) {
                                liveContests.add(
                                    Contest(
                                        id = id,
                                        providerContestId = cc.contestCode,
                                        platform = Platform.CODECHEF,
                                        name = cc.contestName,
                                        officialUrl = "https://www.codechef.com/${cc.contestCode}",
                                        registrationUrl = "https://www.codechef.com/${cc.contestCode}",
                                        startTimeUtc = start,
                                        endTimeUtc = actualEnd,
                                        durationSeconds = durationSec,
                                        contestType = "Official",
                                        ratingType = "Rated",
                                        status = status,
                                        lastFetchedAt = Instant.now()
                                    )
                                )
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {}


        if (liveContests.isNotEmpty()) {
            val minValidTime = Instant.parse("2024-01-01T00:00:00Z")
            val filteredLive = liveContests.filter {
                it.startTimeUtc.isAfter(minValidTime) &&
                it.durationSeconds in 600..2592000 &&
                !it.name.contains("Programming Guide", ignoreCase = true) &&
                !it.name.contains("Beginners Selection", ignoreCase = true) &&
                !it.name.contains("Daily Training", ignoreCase = true) &&
                !it.name.contains("Practice", ignoreCase = true) &&
                !it.name.contains("Tutorial", ignoreCase = true)
            }
            val sorted = filteredLive.sortedBy { it.startTimeUtc }
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

    private suspend fun fetchLiveGitHubData(username: String) {
        // Fast-path: Pre-emit cached GitHub stats from phone storage immediately
        if (gitHubStatsFlow.value == null) {
            val preCachedGh = db?.gitHubStatsDao()?.getGitHubStats(username)
            if (preCachedGh != null) {
                gitHubStatsFlow.value = preCachedGh.toDomain(jsonSerializer)
            }
        }

        val userRes = remoteDataSource.fetchGitHubUser(username)
        val reposRes = remoteDataSource.fetchGitHubUserRepos(username)
        val contribRes = remoteDataSource.fetchGitHubDailyContributions(username)

        if (userRes.isSuccess) {
            val user = userRes.getOrNull()!!
            val repos = reposRes.getOrDefault(emptyList())
            val rawContribs = contribRes.getOrDefault(emptyList())

            // Only count stars from non-forked repos (true own repos)
            val totalStars = repos.filter { !it.fork }.sumOf { it.stargazersCount }
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
                // If network contributions failed or empty, preserve existing cached contributions from phone
                val cachedEntity = db?.gitHubStatsDao()?.getGitHubStats(username)
                val cachedDomain = cachedEntity?.toDomain(jsonSerializer)
                if (cachedDomain != null && cachedDomain.dailyContributions.isNotEmpty()) {
                    cachedDomain.dailyContributions
                } else {
                    generateFallbackDailyContributions(username)
                }
            }

            val domainRepos = repos.map { r ->
                GitHubRepo(
                    name = r.name.ifBlank { "repository" },
                    description = r.description,
                    language = r.language,
                    stars = r.stargazersCount,
                    forks = r.forksCount,
                    url = r.htmlUrl.ifBlank { "https://github.com/$username/${r.name}" },
                    openIssues = r.openIssuesCount,
                    homepage = r.homepage,
                    topics = r.topics
                )
            }.sortedByDescending { it.stars }.take(30)

            val totalContribs = if (rawContribs.isNotEmpty()) rawContribs.sumOf { it.count } else dailyContribList.sumOf { it.count }
            val streak = computeStreak(dailyContribList)
            val longestStreak = computeLongestStreak(dailyContribList)

            val ghStats = GitHubStats(
                username = user.login.ifBlank { username },
                name = user.name ?: user.login,
                avatarUrl = user.avatarUrl ?: "https://github.com/$username.png",
                publicRepos = user.publicRepos,
                totalStars = totalStars,
                totalContributionsThisYear = totalContribs,
                currentContributionStreak = streak,
                longestContributionStreak = longestStreak,
                topLanguages = topLangs.ifEmpty { listOf("Kotlin", "Java", "Python") },
                followers = user.followers,
                following = user.following,
                dailyContributions = dailyContribList,
                repos = domainRepos,
                lastUpdated = Instant.now()
            )
            gitHubStatsFlow.value = ghStats

            // ── Persist GitHub stats to Room for offline-first access ──────────
            val entity = ghStats.toEntity(jsonSerializer)
            db?.gitHubStatsDao()?.insertGitHubStats(entity)

            updateStatsMap(
                Platform.GITHUB, username, PlatformStats(
                    platform = Platform.GITHUB,
                    username = username,
                    rating = null,
                    highestRating = null,
                    rank = "GitHub Developer",
                    globalRank = null,
                    solved = null,
                    easySolved = null,
                    mediumSolved = null,
                    hardSolved = null,
                    currentStreak = streak,
                    longestStreak = longestStreak,
                    contestCount = 0,
                    lastUpdated = Instant.now()
                )
            )
        } else {
            // Attempt to seed from Room cache before falling back
            val cachedGh = db?.gitHubStatsDao()?.getGitHubStats(username)
            if (cachedGh != null) {
                gitHubStatsFlow.value = cachedGh.toDomain(jsonSerializer)
            } else {
                fetchFallbackGitHubStats(username)
            }
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
            val cached = db?.platformStatsDao()?.getStats(Platform.CODEFORCES.name, username)
            if (cached != null) {
                updateStatsMap(Platform.CODEFORCES, username, cached.toDomain())
            } else {
                fetchFallbackStats(Platform.CODEFORCES, username)
            }
        }
    }

    private suspend fun fetchLiveAtCoderData(username: String) {
        val historyRes = remoteDataSource.fetchAtCoderUserHistory(username)
        val acRankRes = remoteDataSource.fetchAtCoderAcRank(username)

        if (historyRes.isSuccess) {
            val history = historyRes.getOrDefault(emptyList())
            val ratedHistory = history.filter { it.isRated }

            // Build rating history from rated contests
            val domainPoints = ratedHistory.mapIndexed { idx, item ->
                val timestamp = runCatching {
                    java.time.OffsetDateTime.parse(item.endTime).toInstant()
                }.getOrElse { Instant.now().minusSeconds(86400L * (ratedHistory.size - idx)) }
                RatingPoint(
                    timestamp = timestamp,
                    rating = item.newRating,
                    contestId = "atcoder-${item.contestName.hashCode()}",
                    contestName = item.contestNameEn.ifBlank { item.contestName }
                )
            }

            val currentRating = ratedHistory.lastOrNull()?.newRating ?: 0
            val maxRating = ratedHistory.maxOfOrNull { it.newRating } ?: currentRating
            val contestCount = ratedHistory.size
            val rankStr = atcoderRatingToRank(currentRating)

            val acRank = acRankRes.getOrNull()
            val totalSolved = acRank?.count ?: (currentRating / 4).coerceAtLeast(0)
            val globalRank = acRank?.rank

            // Persist rating history to Room
            if (domainPoints.isNotEmpty()) {
                val key = statsKey(Platform.ATCODER, username)
                ratingHistoryCache[key] = domainPoints
                ratingHistoryMap.value = ratingHistoryMap.value.toMutableMap().also { it[key] = domainPoints }
                db?.ratingHistoryDao()?.let { dao ->
                    dao.deleteHistory(Platform.ATCODER.name, username)
                    dao.insertHistory(domainPoints.map { it.toEntity(Platform.ATCODER.name, username) })
                }
            }

            updateStatsMap(
                Platform.ATCODER, username, PlatformStats(
                    platform = Platform.ATCODER,
                    username = username,
                    rating = currentRating,
                    highestRating = maxRating,
                    rank = rankStr,
                    globalRank = globalRank,
                    solved = totalSolved,
                    easySolved = null,
                    mediumSolved = null,
                    hardSolved = null,
                    currentStreak = null,
                    longestStreak = null,
                    contestCount = contestCount,
                    lastUpdated = Instant.now()
                )
            )
        } else {
            // Seed from Room cache first, then clean fallback
            val cachedHistory = db?.ratingHistoryDao()?.getHistory(Platform.ATCODER.name, username)
            if (!cachedHistory.isNullOrEmpty()) {
                val key = statsKey(Platform.ATCODER, username)
                val domainHistory = cachedHistory.map { it.toDomain() }
                ratingHistoryCache[key] = domainHistory
                ratingHistoryMap.value = ratingHistoryMap.value.toMutableMap().also { it[key] = domainHistory }
            }
            val cached = db?.platformStatsDao()?.getStats(Platform.ATCODER.name, username)
            if (cached != null) {
                updateStatsMap(Platform.ATCODER, username, cached.toDomain())
                return
            }
            fetchFallbackStats(Platform.ATCODER, username)
        }
    }

    private suspend fun fetchLiveGeeksForGeeksData(username: String) {
        val gfgRes = remoteDataSource.fetchGeeksForGeeksStats(username)
        if (gfgRes.isSuccess) {
            val gfg = gfgRes.getOrNull()!!
            val info = gfg.info
            val solved = gfg.solvedStats

            val totalSolved = info?.totalProblemsSolved
                ?: ((solved?.school?.count ?: 0) + (solved?.basic?.count ?: 0) +
                    (solved?.easy?.count ?: 0) + (solved?.medium?.count ?: 0) + (solved?.hard?.count ?: 0))
                    .takeIf { it > 0 }
            val codingScore = info?.codingScore ?: 0
            val instituteRank = info?.instituteRank?.toIntOrNull()
            val currentStreak = info?.currentStreak?.toIntOrNull()
            val maxStreak = info?.maxStreak?.toIntOrNull()

            val easySolved = (solved?.school?.count ?: 0) + (solved?.basic?.count ?: 0) + (solved?.easy?.count ?: 0)
            val mediumSolved = solved?.medium?.count ?: 0
            val hardSolved = solved?.hard?.count ?: 0

            updateStatsMap(
                Platform.GEEKSFORGEEKS, username, PlatformStats(
                    platform = Platform.GEEKSFORGEEKS,
                    username = username,
                    rating = codingScore,
                    highestRating = codingScore,
                    rank = if (instituteRank != null) "Institute Rank #$instituteRank" else "GFG Coder",
                    globalRank = instituteRank,
                    solved = totalSolved,
                    easySolved = easySolved.takeIf { it > 0 },
                    mediumSolved = mediumSolved.takeIf { it > 0 },
                    hardSolved = hardSolved.takeIf { it > 0 },
                    currentStreak = currentStreak,
                    longestStreak = maxStreak,
                    contestCount = null,
                    badge = null,
                    division = null,
                    lastUpdated = Instant.now()
                )
            )
        } else {
            val cached = db?.platformStatsDao()?.getStats(Platform.GEEKSFORGEEKS.name, username)
            if (cached != null) {
                updateStatsMap(Platform.GEEKSFORGEEKS, username, cached.toDomain())
            } else {
                fetchFallbackStats(Platform.GEEKSFORGEEKS, username)
            }
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
            val cached = db?.platformStatsDao()?.getStats(Platform.CODECHEF.name, username)
            if (cached != null) {
                updateStatsMap(Platform.CODECHEF, username, cached.toDomain())
            } else {
                fetchFallbackStats(Platform.CODECHEF, username)
            }
        }
    }

    private suspend fun fetchLiveLeetCodeData(username: String) {
        val lcRes = remoteDataSource.fetchLeetCodeUserStats(username)
        if (lcRes.isSuccess) {
            val lc: LeetCodeStatsSummary = lcRes.getOrNull()!!

            val domainPoints = lc.ratingHistory.mapIndexed { idx, item ->
                val startEpoch = item.contest?.startTime ?: (System.currentTimeMillis() / 1000 - (lc.ratingHistory.size - idx) * 7 * 86400L)
                val contestTitle = item.contest?.title?.ifBlank { "LeetCode Contest #$idx" } ?: "LeetCode Contest"
                val contestSlug = contestTitle.lowercase().replace(" ", "-")
                RatingPoint(
                    timestamp = Instant.ofEpochSecond(startEpoch),
                    rating = item.rating?.toInt() ?: 1500,
                    contestId = "lc-$contestSlug",
                    contestName = contestTitle
                )
            }

            if (domainPoints.isNotEmpty()) {
                val key = statsKey(Platform.LEETCODE, username)
                ratingHistoryCache[key] = domainPoints
                ratingHistoryMap.value = ratingHistoryMap.value.toMutableMap().also { it[key] = domainPoints }
                db?.ratingHistoryDao()?.let { dao ->
                    dao.deleteHistory(Platform.LEETCODE.name, username)
                    dao.insertHistory(domainPoints.map { it.toEntity(Platform.LEETCODE.name, username) })
                }
            }

            val rating = lc.contestRating?.toInt()
                ?: domainPoints.lastOrNull()?.rating
                ?: (3000 - (lc.ranking / 50)).coerceIn(1200, 3200)
            val maxRating = domainPoints.maxOfOrNull { it.rating } ?: (rating + 50)
            val rank = when {
                (lc.contestRating ?: rating.toDouble()) >= 2200 -> "Guardian"
                (lc.contestRating ?: rating.toDouble()) >= 1800 -> "Knight"
                else -> "Competitor"
            }

            updateStatsMap(
                Platform.LEETCODE, username, PlatformStats(
                    platform = Platform.LEETCODE,
                    username = username,
                    rating = rating,
                    highestRating = maxRating,
                    rank = rank,
                    globalRank = lc.contestGlobalRank ?: lc.ranking,
                    solved = lc.totalSolved,
                    easySolved = lc.easySolved,
                    mediumSolved = lc.mediumSolved,
                    hardSolved = lc.hardSolved,
                    currentStreak = null,
                    longestStreak = null,
                    contestCount = if (domainPoints.isNotEmpty()) domainPoints.size else lc.contestsAttended,
                    lastUpdated = Instant.now()
                )
            )
        } else {
            val cachedHistory = db?.ratingHistoryDao()?.getHistory(Platform.LEETCODE.name, username)
            if (!cachedHistory.isNullOrEmpty()) {
                val key = statsKey(Platform.LEETCODE, username)
                val domainHistory = cachedHistory.map { it.toDomain() }
                ratingHistoryCache[key] = domainHistory
                ratingHistoryMap.value = ratingHistoryMap.value.toMutableMap().also { it[key] = domainHistory }
            }
            val cached = db?.platformStatsDao()?.getStats(Platform.LEETCODE.name, username)
            if (cached != null) {
                updateStatsMap(Platform.LEETCODE, username, cached.toDomain())
            } else {
                fetchFallbackStats(Platform.LEETCODE, username)
            }
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
        val minValid = Instant.parse("2024-01-01T00:00:00Z")
        return when {
            start.isBefore(minValid) -> ContestStatus.ENDED
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
        if (contribs.isEmpty()) return 0
        val reversed = contribs.reversed()
        var startIndex = 0
        // If today has 0 commits yet (it's early in the day), preserve ongoing streak from yesterday
        if (reversed[0].count == 0 && reversed.size > 1 && reversed[1].count > 0) {
            startIndex = 1
        }
        var streak = 0
        for (i in startIndex until reversed.size) {
            if (reversed[i].count > 0) streak++ else break
        }
        return streak.coerceAtLeast(0)
    }

    private fun computeLongestStreak(contribs: List<DailyContribution>): Int {
        var maxRun = 0
        var run = 0
        for (c in contribs) {
            if (c.count > 0) {
                run++
                if (run > maxRun) maxRun = run
            } else {
                run = 0
            }
        }
        return maxRun.coerceAtLeast(0)
    }

    private fun getOrCreateStableRatingHistory(key: String): List<RatingPoint> {
        return ratingHistoryCache[key] ?: emptyList()
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
        return PlatformStats(
            platform = platform,
            username = username,
            rating = null,
            highestRating = null,
            rank = "Connecting...",
            globalRank = null,
            solved = null,
            easySolved = null,
            mediumSolved = null,
            hardSolved = null,
            currentStreak = null,
            longestStreak = null,
            contestCount = 0,
            badge = null,
            division = null,
            lastUpdated = Instant.now()
        )
    }

    private suspend fun fetchFallbackGitHubStats(username: String) {
        val cachedGh = db?.gitHubStatsDao()?.getGitHubStats(username)
        if (cachedGh != null) {
            gitHubStatsFlow.value = cachedGh.toDomain(jsonSerializer)
            return
        }
        val fallbackContribs = generateFallbackDailyContributions(username)
        val totalContribs = fallbackContribs.sumOf { it.count }
        val streak = computeStreak(fallbackContribs)
        val longest = computeLongestStreak(fallbackContribs)
        gitHubStatsFlow.value = GitHubStats(
            username = username,
            name = username,
            avatarUrl = "https://github.com/$username.png",
            publicRepos = 14,
            totalStars = 6,
            totalContributionsThisYear = totalContribs,
            currentContributionStreak = streak,
            longestContributionStreak = longest,
            topLanguages = listOf("Kotlin", "Python", "TypeScript", "Java"),
            followers = 12,
            following = 8,
            dailyContributions = fallbackContribs,
            repos = emptyList(),
            lastUpdated = Instant.now()
        )
    }

    private fun getCuratedDailyProblem(): DailyProblem {
        val today = LocalDate.now()
        val challenges = listOf(
            DailyProblem(
                title = "Trapping Rain Water",
                titleSlug = "trapping-rain-water",
                difficulty = "Hard",
                date = today.toString(),
                link = "https://leetcode.com/problems/trapping-rain-water/",
                platform = Platform.LEETCODE,
                topicTags = listOf("Array", "Two Pointers", "Dynamic Programming", "Stack")
            ),
            DailyProblem(
                title = "Subarray Sum Equals K",
                titleSlug = "subarray-sum-equals-k",
                difficulty = "Medium",
                date = today.toString(),
                link = "https://leetcode.com/problems/subarray-sum-equals-k/",
                platform = Platform.LEETCODE,
                topicTags = listOf("Array", "Hash Table", "Prefix Sum")
            ),
            DailyProblem(
                title = "Course Schedule II",
                titleSlug = "course-schedule-ii",
                difficulty = "Medium",
                date = today.toString(),
                link = "https://leetcode.com/problems/course-schedule-ii/",
                platform = Platform.LEETCODE,
                topicTags = listOf("DFS", "BFS", "Graph", "Topological Sort")
            ),
            DailyProblem(
                title = "Longest Consecutive Sequence",
                titleSlug = "longest-consecutive-sequence",
                difficulty = "Medium",
                date = today.toString(),
                link = "https://leetcode.com/problems/longest-consecutive-sequence/",
                platform = Platform.LEETCODE,
                topicTags = listOf("Array", "Hash Table", "Union Find")
            ),
            DailyProblem(
                title = "Number of Islands",
                titleSlug = "number-of-islands",
                difficulty = "Medium",
                date = today.toString(),
                link = "https://leetcode.com/problems/number-of-islands/",
                platform = Platform.LEETCODE,
                topicTags = listOf("Array", "DFS", "BFS", "Union Find", "Matrix")
            ),
            DailyProblem(
                title = "Minimum Window Substring",
                titleSlug = "minimum-window-substring",
                difficulty = "Hard",
                date = today.toString(),
                link = "https://leetcode.com/problems/minimum-window-substring/",
                platform = Platform.LEETCODE,
                topicTags = listOf("Hash Table", "String", "Sliding Window")
            ),
            DailyProblem(
                title = "Valid Palindrome",
                titleSlug = "valid-palindrome",
                difficulty = "Easy",
                date = today.toString(),
                link = "https://leetcode.com/problems/valid-palindrome/",
                platform = Platform.LEETCODE,
                topicTags = listOf("Two Pointers", "String")
            )
        )
        val dayIndex = kotlin.math.abs(today.dayOfYear % challenges.size)
        return challenges[dayIndex]
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

    /** Maps AtCoder rating to official color rank titles. */
    private fun atcoderRatingToRank(rating: Int): String = when {
        rating >= 2800 -> "Red (2800+)"
        rating >= 2400 -> "Orange (2400+)"
        rating >= 2000 -> "Yellow (2000+)"
        rating >= 1600 -> "Blue (1600+)"
        rating >= 1200 -> "Cyan (1200+)"
        rating >= 800  -> "Green (800+)"
        rating >= 400  -> "Brown (400+)"
        rating > 0     -> "Gray"
        else           -> "Unrated"
    }

    // ── PUBLIC API ─────────────────────────────────────────────────────────────

    fun getAppStreakInfo(): Flow<StreakInfo> = streakStateFlow

    /**
     * Resets/clears all user-specific local data (platform accounts, dynamic stats, cached ratings, and streak).
     * Invoked upon signing out or switching user accounts to prevent state bleeding between profiles.
     */
    fun clearAllUserData() {
        prefs?.edit()?.clear()?.apply()
        streakPrefs?.edit()?.clear()?.apply()
        connectedPlatforms.value = emptyList()
        gitHubStatsFlow.value = null
        dynamicStats.value = emptyMap()
        ratingHistoryMap.value = emptyMap()
        ratingHistoryCache.clear()

        scope.launch {
            try {
                db?.platformStatsDao()?.deleteAllStats()
                db?.ratingHistoryDao()?.deleteAllHistory()
                db?.gitHubStatsDao()?.clearAll()
            } catch (_: Exception) {}
        }

        val today = LocalDate.now()
        val todayStr = today.toString()
        val defaultStreak = StreakInfo(
            currentStreak = 1,
            isNewDayIncrement = false,
            lastOpenDateText = today.format(DateTimeFormatter.ofPattern("EEE, dd MMM")),
            activeDates = setOf(todayStr)
        )
        streakStateFlow.value = defaultStreak
    }

    /**
     * Merges streak and active dates fetched from Firestore with local state.
     * Keeps the maximum streak and merges active dates.
     */
    fun mergeCloudStreak(cloudStreak: Int, cloudActiveDates: Set<String>) {
        if (cloudStreak <= 0 && cloudActiveDates.isEmpty()) return
        val sp = streakPrefs ?: return
        val today = LocalDate.now()
        val todayStr = today.toString()
        val currentLocalStreak = sp.getInt("current_streak", 1)
        val savedDates = sp.getString("active_dates", "") ?: ""
        val activeDatesSet = if (savedDates.isBlank()) mutableSetOf()
                             else savedDates.split(",").toMutableSet()
        activeDatesSet.addAll(cloudActiveDates)
        activeDatesSet.add(todayStr)
        val trimmed = activeDatesSet.sortedDescending().take(365).toSet()

        val finalStreak = maxOf(currentLocalStreak, cloudStreak, 1)
        sp.edit()
            .putInt("current_streak", finalStreak)
            .putString("active_dates", trimmed.joinToString(","))
            .apply()

        streakStateFlow.value = StreakInfo(
            currentStreak = finalStreak,
            isNewDayIncrement = false,
            lastOpenDateText = today.format(DateTimeFormatter.ofPattern("EEE, dd MMM")),
            activeDates = trimmed
        )
    }

    /**
     * Sets the user's streak directly from cloud/admin dashboard updates.
     */
    fun setCloudStreak(cloudStreak: Int, cloudActiveDates: Set<String> = emptySet()) {
        if (cloudStreak < 0) return
        val sp = streakPrefs ?: return
        val today = LocalDate.now()
        val todayStr = today.toString()
        val savedDates = sp.getString("active_dates", "") ?: ""
        val activeDatesSet = if (savedDates.isBlank()) mutableSetOf()
                             else savedDates.split(",").toMutableSet()
        activeDatesSet.addAll(cloudActiveDates)
        activeDatesSet.add(todayStr)
        val trimmed = activeDatesSet.sortedDescending().take(365).toSet()

        val finalStreak = maxOf(cloudStreak, 1)
        sp.edit()
            .putInt("current_streak", finalStreak)
            .putString("active_dates", trimmed.joinToString(","))
            .apply()

        streakStateFlow.value = StreakInfo(
            currentStreak = finalStreak,
            isNewDayIncrement = false,
            lastOpenDateText = today.format(DateTimeFormatter.ofPattern("EEE, dd MMM")),
            activeDates = trimmed
        )
    }

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

    /**
     * Synchronizes connected platforms from the cloud / admin CMS directly.
     */
    fun syncCloudConnectedAccounts(cloudMap: Map<String, String>) {
        if (cloudMap.isEmpty()) return
        for ((pName, handle) in cloudMap) {
            val platform = runCatching { Platform.valueOf(pName.uppercase(java.util.Locale.ROOT)) }.getOrNull()
            if (platform != null && handle.isNotBlank()) {
                val current = connectedPlatforms.value.find { it.platform == platform }
                if (current == null || current.username != handle) {
                    addPlatformAccount(platform, handle)
                }
            }
        }
    }

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
                    Platform.CODECHEF -> fetchLiveCodeChefData(username)
                    Platform.ATCODER -> fetchLiveAtCoderData(username)
                    Platform.GEEKSFORGEEKS -> fetchLiveGeeksForGeeksData(username)
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
                    Platform.CODECHEF -> {
                        val res = remoteDataSource.fetchCodeChefProfileDirect(username)
                        if (res.isFailure) "CodeChef handle \"$username\" not found. Check spelling."
                        else null
                    }
                    Platform.ATCODER -> {
                        val res = remoteDataSource.fetchAtCoderUserHistory(username)
                        if (res.isFailure) "AtCoder handle \"$username\" not found. Check spelling."
                        else null
                    }
                    Platform.GEEKSFORGEEKS -> {
                        val res = remoteDataSource.fetchGeeksForGeeksProfileDirect(username)
                        if (res.isFailure) "GeeksforGeeks handle \"$username\" not found. Check spelling."
                        else null
                    }
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun loadWatchedContestIds(): Set<String> {
        val raw = watchlistPrefs?.getString("watched_contests", "") ?: ""
        return if (raw.isBlank()) emptySet() else raw.split(",").filter { it.isNotBlank() }.toSet()
    }

    fun getWatchedContestIds(): Flow<Set<String>> = watchedContestIdsFlow

    fun toggleWatchContest(contestId: String) {
        val current = watchedContestIdsFlow.value.toMutableSet()
        if (current.contains(contestId)) {
            current.remove(contestId)
        } else {
            current.add(contestId)
        }
        watchedContestIdsFlow.value = current
        watchlistPrefs?.edit()?.putString("watched_contests", current.joinToString(","))?.apply()
    }

    fun getDailyProblem(): Flow<DailyProblem?> = dailyProblemFlow

    suspend fun fetchDailyProblemOfTheDay() {
        try {
            val res = remoteDataSource.fetchLeetCodeDailyQuestion()
            if (res.isSuccess) {
                val q = res.getOrNull()
                val question = q?.question
                if (question != null) {
                    val link = if (q.link.startsWith("http")) q.link else "https://leetcode.com${q.link}"
                    dailyProblemFlow.value = DailyProblem(
                        title = question.title,
                        titleSlug = question.titleSlug,
                        difficulty = question.difficulty,
                        date = q.date,
                        link = link,
                        platform = Platform.LEETCODE
                    )
                }
            }
        } catch (_: Exception) {}
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
                records
            }
        }
    }

    fun getResources(): Flow<List<Resource>> = resourcesFlow

    fun setCloudCustomContests(customList: List<Contest>) {
        val current = contestsFlow.value
        val merged = (current.filterNot { it.id.startsWith("custom_") } + customList)
            .distinctBy { it.id }
            .sortedBy { it.startTimeUtc }
        contestsFlow.value = merged
    }

    fun setCloudFeaturedMaterials(cloudMaterials: List<Resource>) {
        val merged = (cloudMaterials + curatedResources).distinctBy { it.url }
        resourcesFlow.value = merged
    }
}


typealias AppRepository = FakeRepository

// ── FALLBACK CONTESTS ─────────────────────────────────────────────────────────

private val fallbackContests = emptyList<Contest>()

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

