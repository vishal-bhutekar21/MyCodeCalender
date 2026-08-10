package com.mycodecalendar.backend.providers

import java.time.Instant

class AtCoderProvider : ContestProvider {
    override val platformName: String = "ATCODER"

    override suspend fun getUpcomingContests(): Result<List<ContestResult>> {
        return try {
            val now = Instant.now()
            val dummyContests = listOf(
                ContestResult(
                    providerContestId = "abc350",
                    name = "AtCoder Beginner Contest 350",
                    description = "Official AtCoder Beginner Contest",
                    startTime = now.plusSeconds(3600 * 24),
                    endTime = now.plusSeconds(3600 * 25 + 2400),
                    durationSeconds = 6000,
                    url = "https://atcoder.jp/contests/abc350",
                    contestType = "ABC",
                    ratingType = "Rated (<2000)",
                    status = "UPCOMING"
                ),
                ContestResult(
                    providerContestId = "arc175",
                    name = "AtCoder Regular Contest 175",
                    description = "Official AtCoder Regular Contest",
                    startTime = now.plusSeconds(3600 * 72),
                    endTime = now.plusSeconds(3600 * 74),
                    durationSeconds = 7200,
                    url = "https://atcoder.jp/contests/arc175",
                    contestType = "ARC",
                    ratingType = "Rated (1200-2799)",
                    status = "UPCOMING"
                )
            )
            Result.success(dummyContests)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
