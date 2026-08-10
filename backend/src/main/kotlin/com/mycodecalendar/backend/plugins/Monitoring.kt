package com.mycodecalendar.backend.plugins

import io.ktor.server.application.*

fun Application.configureMonitoring() {
    log.info("Monitoring plugin initialized.")
}
