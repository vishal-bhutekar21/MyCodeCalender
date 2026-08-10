package com.mycodecalendar.backend.plugins

import com.mycodecalendar.backend.models.ErrorResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.mycodecalendar.backend.routes.healthRouting
import com.mycodecalendar.backend.routes.contestsRouting

fun Application.configureRouting() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(code = "INTERNAL_ERROR", message = cause.localizedMessage ?: "Unknown error")
            )
        }
    }

    routing {
        route("/v1") {
            healthRouting()
            contestsRouting()
        }
    }
}
