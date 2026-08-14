package com.mycodecalendar.domain.usecase

import com.mycodecalendar.domain.model.Platform
import com.mycodecalendar.domain.model.PlatformStats
import com.mycodecalendar.domain.model.Resource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.time.Instant

class GetRecommendedResourceUseCaseTest {

    private val useCase = GetRecommendedResourceUseCase()

    private val dummyResources = listOf(
        Resource(
            id = "1",
            title = "DP Masterclass",
            description = "DP Patterns",
            creator = "Alex",
            url = "https://example.com/dp",
            category = "Dynamic Programming",
            platform = Platform.CODEFORCES,
            duration = "30m",
            priority = 1,
            thumbnailUrl = null,
            publishedAt = Instant.now()
        ),
        Resource(
            id = "2",
            title = "Data Structures 101",
            description = "Trees and Graphs",
            creator = "Bob",
            url = "https://example.com/ds",
            category = "Data Structures",
            platform = Platform.LEETCODE,
            duration = "20m",
            priority = 2,
            thumbnailUrl = null,
            publishedAt = Instant.now()
        )
    )

    @Test
    fun testRecommendsDpResourceForLowerRatings() {
        val stats = listOf(
            PlatformStats(
                platform = Platform.CODEFORCES,
                username = "coder",
                rating = 1200,
                highestRating = 1300,
                lastUpdated = Instant.now()
            )
        )

        val result = useCase.execute(stats, dummyResources)
        assertNotNull(result)
        assertEquals("Dynamic Programming", result?.category)
    }
}
