package com.mycodecalendar.backend.routes

import com.mycodecalendar.backend.models.ApiResponse
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.healthRouting() {
    get("/health") {
        call.respond(ApiResponse(data = mapOf("status" to "OK")))
    }
}
