package com.mycodecalendar.backend.routes

import com.mycodecalendar.backend.database.DatabaseFactory
import com.mycodecalendar.backend.models.ApiResponse
import com.mycodecalendar.backend.models.ContestDto
import com.mycodecalendar.backend.models.ErrorResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.Instant

private val fallbackContests = listOf(
    ContestDto(
        id = "cf-999",
        platform = "CODEFORCES",
        providerContestId = "999",
        name = "Codeforces Round 999 (Div. 2)",
        description = "Official Codeforces Round",
        officialUrl = "https://codeforces.com/contest/999",
        registrationUrl = "https://codeforces.com/contest/999",
        startTimeUtc = Instant.now().plusSeconds(3600).toString(),
        endTimeUtc = Instant.now().plusSeconds(10800).toString(),
        durationSeconds = 7200,
        contestType = "ICPC",
        ratingType = "Rated for Div. 2",
        status = "UPCOMING",
        lastFetchedAt = Instant.now().toString()
    ),
    ContestDto(
        id = "abc-350",
        platform = "ATCODER",
        providerContestId = "350",
        name = "AtCoder Beginner Contest 350",
        description = "Official AtCoder Beginner Contest",
        officialUrl = "https://atcoder.jp/contests/abc350",
        registrationUrl = "https://atcoder.jp/contests/abc350",
        startTimeUtc = Instant.now().plusSeconds(86400).toString(),
        endTimeUtc = Instant.now().plusSeconds(92400).toString(),
        durationSeconds = 6000,
        contestType = "ABC",
        ratingType = "Rated (<2000)",
        status = "UPCOMING",
        lastFetchedAt = Instant.now().toString()
    )
)

fun Route.contestsRouting() {
    get("/v1/contests") {
        val platform = call.request.queryParameters["platform"]
        val status = call.request.queryParameters["status"]

        val dbContests = DatabaseFactory.getContests(platform, status)
        val contests = if (dbContests.isNotEmpty()) dbContests else fallbackContests

        call.respond(ApiResponse(data = contests))
    }

    get("/v1/contests/{id}") {
        val id = call.parameters["id"] ?: return@get call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse<Nothing>(error = ErrorResponse("INVALID_ID", "Missing id parameter"))
        )

        val dbContests = DatabaseFactory.getContests()
        val contest = dbContests.find { it.id == id || it.providerContestId == id }
            ?: fallbackContests.find { it.id == id || it.providerContestId == id }

        if (contest != null) {
            call.respond(ApiResponse(data = contest))
        } else {
            call.respond(
                HttpStatusCode.NotFound,
                ApiResponse<Nothing>(error = ErrorResponse("NOT_FOUND", "Contest not found"))
            )
        }
    }
}
