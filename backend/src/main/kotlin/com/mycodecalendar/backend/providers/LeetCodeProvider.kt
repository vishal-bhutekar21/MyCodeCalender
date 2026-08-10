package com.mycodecalendar.backend.providers

import java.time.Instant

class LeetCodeProvider : ContestProvider {
    override val platformName: String = "LEETCODE"
    override val isEnabled: Boolean = true

    override suspend fun getUpcomingContests(): Result<List<ContestResult>> {
        return try {
            val now = Instant.now()
            val contests = listOf(
                ContestResult(
                    providerContestId = "weekly-contest-395",
                    name = "Weekly Contest 395",
                    description = "Official LeetCode Weekly Contest",
                    startTime = now.plusSeconds(3600 * 18),
                    endTime = now.plusSeconds(3600 * 19 + 1800),
                    durationSeconds = 5400,
                    url = "https://leetcode.com/contest/weekly-contest-395",
                    contestType = "Weekly",
                    ratingType = "Rated",
                    status = "UPCOMING"
                ),
                ContestResult(
                    providerContestId = "biweekly-contest-130",
                    name = "Biweekly Contest 130",
                    description = "Official LeetCode Biweekly Contest",
                    startTime = now.plusSeconds(3600 * 90),
                    endTime = now.plusSeconds(3600 * 91 + 1800),
                    durationSeconds = 5400,
                    url = "https://leetcode.com/contest/biweekly-contest-130",
                    contestType = "Biweekly",
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
