package com.mycodecalendar.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mycodecalendar.data.repository.FakeRepository
import com.mycodecalendar.domain.model.Contest
import com.mycodecalendar.domain.model.GitHubStats
import com.mycodecalendar.domain.model.PlatformStats
import com.mycodecalendar.domain.model.Resource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class HomeUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val userStreak: Int = 42,
    val connectedStats: List<PlatformStats> = emptyList(),
    val gitHubStats: GitHubStats? = null,
    val nextContest: Contest? = null,
    val upcomingContests: List<Contest> = emptyList(),
    val featuredResource: Resource? = null,
    val lastUpdatedText: String = "just now"
)

class HomeViewModel(
    private val repository: FakeRepository = FakeRepository()
) : ViewModel() {

    private val _refreshTrigger = MutableStateFlow(0)

    val uiState: StateFlow<HomeUiState> = combine(
        repository.getAllConnectedStats(),
        repository.getGitHubStats(),
        repository.getContests(),
        repository.getResources()
    ) { stats, ghStats, contests, resources ->
        val sortedContests = contests.sortedBy { it.startTimeUtc }
        val next = sortedContests.firstOrNull()
        val featuredRes = resources.firstOrNull()
        val maxStreak = stats.maxOfOrNull { it.currentStreak ?: 0 } ?: 0

        HomeUiState(
            isLoading = false,
            isRefreshing = false,
            userStreak = maxStreak.coerceAtLeast(15),
            connectedStats = stats,
            gitHubStats = ghStats,
            nextContest = next,
            upcomingContests = sortedContests,
            featuredResource = featuredRes,
            lastUpdatedText = formatLastUpdated()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    )

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            // Simulate network refresh delay
            delay(1200L)
            // In a real app: trigger re-fetch from remote data sources
            _isRefreshing.value = false
        }
    }

    private fun formatLastUpdated(): String {
        val formatter = DateTimeFormatter.ofPattern("hh:mm a")
            .withZone(ZoneId.systemDefault())
        return "at ${formatter.format(Instant.now())}"
    }
}
