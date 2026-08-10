package com.mycodecalendar.backend.providers

import java.time.Instant

class CodeChefProvider : ContestProvider {
    override val platformName: String = "CODECHEF"

    override suspend fun getUpcomingContests(): Result<List<ContestResult>> {
        return try {
            val now = Instant.now()
            val contests = listOf(
                ContestResult(
                    providerContestId = "START130",
                    name = "Starters 130 (Rated till 6-Star)",
                    description = "CodeChef Starters Contest",
                    startTime = now.plusSeconds(3600 * 30),
                    endTime = now.plusSeconds(3600 * 32),
                    durationSeconds = 7200,
                    url = "https://www.codechef.com/START130",
                    contestType = "Starters",
                    ratingType = "Rated",
                    status = "UPCOMING"
                )
            )
            Result.success(contests)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
