package com.mycodecalendar.core.common

import android.util.Log

/**
 * AppLogger — Structured, privacy-conscious logging framework for CodeCalendar.
 *
 * Ensures:
 * 1. PII and secret tokens are sanitized from log outputs.
 * 2. Unified log tags and severity levels.
 * 3. Ready for attaching Crashlytics / telemetry breadcrumbs in production.
 */
object AppLogger {

    private const val GLOBAL_PREFIX = "CodeCalendar"

    fun d(tag: String, message: String) {
        Log.d("$GLOBAL_PREFIX:$tag", sanitize(message))
    }

    fun i(tag: String, message: String) {
        Log.i("$GLOBAL_PREFIX:$tag", sanitize(message))
    }

    fun w(tag: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.w("$GLOBAL_PREFIX:$tag", sanitize(message), throwable)
        } else {
            Log.w("$GLOBAL_PREFIX:$tag", sanitize(message))
        }
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e("$GLOBAL_PREFIX:$tag", sanitize(message), throwable)
        } else {
            Log.e("$GLOBAL_PREFIX:$tag", sanitize(message))
        }
    }

    /**
     * Masks potential email addresses, tokens, and authorization headers.
     */
    private fun sanitize(input: String): String {
        return input
            .replace(Regex("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+"), "[REDACTED_EMAIL]")
            .replace(Regex("(?i)(bearer\\s+)[a-zA-Z0-9_.-]+"), "$1[REDACTED_TOKEN]")
            .replace(Regex("(?i)(token\\s*=\\s*)[a-zA-Z0-9_.-]+"), "$1[REDACTED]")
    }
}
