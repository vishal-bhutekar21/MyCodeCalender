package com.mycodecalendar.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mycodecalendar.data.repository.FakeRepository
import com.mycodecalendar.domain.model.Contest
import com.mycodecalendar.domain.model.GitHubStats
import com.mycodecalendar.domain.model.PlatformStats
import com.mycodecalendar.domain.model.Resource
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val isLoading: Boolean = false,
    val userStreak: Int = 42,
    val connectedStats: List<PlatformStats> = emptyList(),
    val gitHubStats: GitHubStats? = null,
    val nextContest: Contest? = null,
    val upcomingContests: List<Contest> = emptyList(),
    val featuredResource: Resource? = null,
    val lastUpdatedText: String = "10 minutes ago"
)

class HomeViewModel(
    private val repository: FakeRepository = FakeRepository()
) : ViewModel() {

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
            userStreak = maxStreak.coerceAtLeast(15),
            connectedStats = stats,
            gitHubStats = ghStats,
            nextContest = next,
            upcomingContests = sortedContests,
            featuredResource = featuredRes,
            lastUpdatedText = "Just now"
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    )
}
