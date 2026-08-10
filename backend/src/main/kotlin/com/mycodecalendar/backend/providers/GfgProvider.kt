package com.mycodecalendar.backend.providers

import java.time.Instant

class GfgProvider : ContestProvider {
    override val platformName: String = "GEEKSFORGEEKS"
    override val isEnabled: Boolean = true

    override suspend fun getUpcomingContests(): Result<List<ContestResult>> {
        return try {
            val now = Instant.now()
            val contests = listOf(
                ContestResult(
                    providerContestId = "gfg-weekly-155",
                    name = "GFG Weekly Coding Contest 155",
                    description = "GeeksforGeeks Weekly Challenge",
                    startTime = now.plusSeconds(3600 * 42),
                    endTime = now.plusSeconds(3600 * 43 + 1800),
                    durationSeconds = 5400,
                    url = "https://practice.geeksforgeeks.org/contest/gfg-weekly-155",
                    contestType = "Weekly",
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
