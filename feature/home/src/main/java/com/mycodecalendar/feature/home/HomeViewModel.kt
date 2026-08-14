package com.mycodecalendar.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mycodecalendar.data.repository.FakeRepository
import com.mycodecalendar.domain.model.Contest
import com.mycodecalendar.domain.model.GitHubStats
import com.mycodecalendar.domain.model.PlatformStats
import com.mycodecalendar.domain.model.Resource
import com.mycodecalendar.domain.model.StreakInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class HomeUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isOffline: Boolean = false,
    val userStreak: Int = 1,
    val streakInfo: StreakInfo? = null,
    val connectedStats: List<PlatformStats> = emptyList(),
    val gitHubStats: GitHubStats? = null,
    val nextContest: Contest? = null,
    val highlightContests: List<Contest> = emptyList(),
    val upcomingContests: List<Contest> = emptyList(),
    val featuredResource: Resource? = null,
    val lastUpdatedText: String = "just now",
    val fetchError: String? = null
)

/**
 * HomeViewModel — drives the Home screen.
 *
 * Daily App Login Streak System:
 * - Driven by persistent daily login streak state ([FakeRepository.getAppStreakInfo]).
 * - 30-second user click throttling and 5-minute background polling loop.
 */
class HomeViewModel(
    private val repository: FakeRepository
) : ViewModel() {

    private var lastManualRefreshTime: Long = 0L
    private val minManualRefreshIntervalMs: Long = 30_000L // 30 seconds throttle

    val isRefreshing: StateFlow<Boolean> = repository.isRefreshing
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val fetchError: StateFlow<String?> = repository.fetchError
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isOffline: StateFlow<Boolean> = repository.isOffline
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        viewModelScope.launch {
            while (true) {
                delay(300_000L) // 5 minutes
                repository.refreshAndAwait(force = false)
            }
        }
    }

    /**
     * Main UI state — combines daily streak info, connected stats, contests, and resources.
     */
    val uiState: StateFlow<HomeUiState> = combine(
        repository.getAppStreakInfo(),
        repository.getAllConnectedStats(),
        repository.getGitHubStats(),
        repository.getContests(),
        repository.getResources(),
        repository.isRefreshing,
        repository.isOffline
    ) { array ->
        @Suppress("UNCHECKED_CAST")
        val streak = array[0] as StreakInfo
        @Suppress("UNCHECKED_CAST")
        val stats = array[1] as List<PlatformStats>
        @Suppress("UNCHECKED_CAST")
        val ghStats = array[2] as GitHubStats?
        @Suppress("UNCHECKED_CAST")
        val contests = array[3] as List<Contest>
        @Suppress("UNCHECKED_CAST")
        val resources = array[4] as List<Resource>
        @Suppress("UNCHECKED_CAST")
        val refreshing = array[5] as Boolean
        @Suppress("UNCHECKED_CAST")
        val offline = array[6] as Boolean

        val sortedContests = contests.sortedWith(
            compareByDescending<Contest> { it.status.name == "LIVE" }
                .thenBy { it.startTimeUtc }
        )

        val nextContest = sortedContests.firstOrNull()
        val liveContests = sortedContests.filter { it.status.name == "LIVE" }
        val highlightContests = if (liveContests.isNotEmpty()) {
            if (liveContests.size >= 2) liveContests
            else liveContests + sortedContests.filter { it.status.name == "UPCOMING" }.take(2)
        } else {
            sortedContests.take(3)
        }
        val featuredRes = resources.firstOrNull()

        HomeUiState(
            isLoading = false,
            isRefreshing = refreshing,
            isOffline = offline,
            userStreak = streak.currentStreak,
            streakInfo = streak,
            connectedStats = stats,
            gitHubStats = ghStats,
            nextContest = nextContest,
            highlightContests = highlightContests,
            upcomingContests = sortedContests,
            featuredResource = featuredRes,
            lastUpdatedText = formatLastUpdated(),
            fetchError = null
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    )

    fun refresh() {
        val now = System.currentTimeMillis()
        if (now - lastManualRefreshTime < minManualRefreshIntervalMs) {
            return
        }
        lastManualRefreshTime = now
        viewModelScope.launch {
            repository.refreshAndAwait(force = true)
        }
    }

    private fun formatLastUpdated(): String {
        val formatter = DateTimeFormatter.ofPattern("hh:mm a")
            .withZone(ZoneId.systemDefault())
        return "at ${formatter.format(Instant.now())}"
    }
}
