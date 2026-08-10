package com.mycodecalendar.backend.service

import java.util.concurrent.atomic.AtomicInteger

enum class CircuitState {
    HEALTHY, DEGRADED, OPEN
}

class CircuitBreaker(private val failureThreshold: Int = 3) {
    private var state = CircuitState.HEALTHY
    private val failureCount = AtomicInteger(0)

    fun recordSuccess() {
        failureCount.set(0)
        state = CircuitState.HEALTHY
    }

    fun recordFailure() {
        val count = failureCount.incrementAndGet()
        state = if (count >= failureThreshold) CircuitState.OPEN else CircuitState.DEGRADED
    }

    fun getState(): CircuitState = state
}
