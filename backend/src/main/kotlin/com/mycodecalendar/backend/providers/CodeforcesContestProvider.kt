package com.mycodecalendar.backend.providers

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.Instant

@Serializable
private data class CodeforcesResponse(
    val status: String,
    val result: List<CodeforcesContest>? = null
)

@Serializable
private data class CodeforcesContest(
    val id: Long,
    val name: String,
    val type: String? = null,
    val phase: String,
    val durationSeconds: Int,
    val startTimeSeconds: Long? = null
)

class CodeforcesContestProvider : ContestProvider {
    override val platformName: String = "CODEFORCES"

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    override suspend fun getUpcomingContests(): Result<List<ContestResult>> {
        return try {
            val response: CodeforcesResponse = client.get("https://codeforces.com/api/contest.list").body()
            if (response.status == "OK" && response.result != null) {
                val contests = response.result
                    .filter { it.phase == "BEFORE" || it.phase == "CODING" }
                    .mapNotNull { cf ->
                        val startSec = cf.startTimeSeconds ?: return@mapNotNull null
                        val startTime = Instant.ofEpochSecond(startSec)
                        val endTime = startTime.plusSeconds(cf.durationSeconds.toLong())
                        val status = if (cf.phase == "CODING") "LIVE" else "UPCOMING"

                        ContestResult(
                            providerContestId = cf.id.toString(),
                            name = cf.name,
                            description = "Official Codeforces Contest (${cf.type ?: "CF"})",
                            startTime = startTime,
                            endTime = endTime,
                            durationSeconds = cf.durationSeconds,
                            url = "https://codeforces.com/contests/${cf.id}",
                            contestType = cf.type ?: "ICPC",
                            ratingType = "Rated",
                            status = status
                        )
                    }
                Result.success(contests)
            } else {
                Result.failure(Exception("Codeforces API returned status ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
