package com.mycodecalendar.backend.service

import com.mycodecalendar.backend.database.DatabaseFactory
import com.mycodecalendar.backend.database.RedisCache
import com.mycodecalendar.backend.providers.AtCoderProvider
import com.mycodecalendar.backend.providers.CodeChefProvider
import com.mycodecalendar.backend.providers.CodeforcesContestProvider
import com.mycodecalendar.backend.providers.ContestProvider
import com.mycodecalendar.backend.providers.ContestResult
import com.mycodecalendar.backend.providers.GfgProvider
import com.mycodecalendar.backend.providers.LeetCodeProvider

class IngestionWorker(
    private val providers: List<ContestProvider> = listOf(
        CodeforcesContestProvider(),
        AtCoderProvider(),
        CodeChefProvider(),
        LeetCodeProvider(),
        GfgProvider()
    )
) {
    private val circuitBreakers = providers.associate { it.platformName to CircuitBreaker() }

    suspend fun fetchAndIngest() {
        providers.filter { it.isEnabled }.forEach { provider ->
            val breaker = circuitBreakers[provider.platformName]!!
            if (breaker.getState() == CircuitState.OPEN) {
                println("Provider ${provider.platformName} circuit breaker is OPEN. Skipping fetch.")
                return@forEach
            }

            val result = provider.getUpcomingContests()
            if (result.isSuccess) {
                breaker.recordSuccess()
                val contests = result.getOrNull() ?: emptyList()
                val validContests = validateContests(contests)
                val normalized = normalizeAndDedupe(validContests, provider.platformName)

                // Perform Postgres UPSERT
                DatabaseFactory.upsertContests(normalized, provider.platformName)
                // Invalidate Redis cache
                RedisCache.invalidate("contests:")
            } else {
                breaker.recordFailure()
                println("Failed to fetch from ${provider.platformName}: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    private fun validateContests(contests: List<ContestResult>): List<ContestResult> {
        return contests.filter {
            it.name.isNotBlank() && it.durationSeconds > 0 && it.url.isNotBlank()
        }
    }

    private fun normalizeAndDedupe(contests: List<ContestResult>, platform: String): List<ContestResult> {
        return contests.distinctBy { it.providerContestId }
    }
}
