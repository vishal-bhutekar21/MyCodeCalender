package com.mycodecalendar.domain.usecase

import com.mycodecalendar.domain.model.PlatformStats
import com.mycodecalendar.domain.model.Resource

class GetRecommendedResourceUseCase {

    fun execute(stats: List<PlatformStats>, availableResources: List<Resource>): Resource? {
        if (availableResources.isEmpty()) return null

        val lowestRatingStat = stats.minByOrNull { it.rating ?: 9999 }
        if (lowestRatingStat != null && (lowestRatingStat.rating ?: 0) < 1500) {
            val dpResource = availableResources.find { it.category.contains("Dynamic Programming", ignoreCase = true) }
            if (dpResource != null) return dpResource
        }

        val lowStreakStat = stats.find { (it.currentStreak ?: 0) < 5 }
        if (lowStreakStat != null) {
            val dsResource = availableResources.find { it.category.contains("Data Structures", ignoreCase = true) }
            if (dsResource != null) return dsResource
        }

        return availableResources.firstOrNull()
    }
}
