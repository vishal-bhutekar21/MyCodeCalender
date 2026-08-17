package com.mycodecalendar.core.designsystem.components

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Standardized Indian Standard Time (IST / Asia/Kolkata) formatters for contests & schedules.
 */
object IndianDateTimeFormatters {
    val IST_ZONE_ID: ZoneId = ZoneId.of("Asia/Kolkata")

    // Full Date & Time: "19 Aug 2026, 08:00 PM IST"
    val fullDateTimeFormatter: DateTimeFormatter = DateTimeFormatter
        .ofPattern("dd MMM yyyy, hh:mm a 'IST'", Locale.ENGLISH)
        .withZone(IST_ZONE_ID)

    // Short Date & Time: "Wed, 19 Aug · 08:00 PM IST"
    val shortDateTimeFormatter: DateTimeFormatter = DateTimeFormatter
        .ofPattern("EEE, dd MMM · hh:mm a 'IST'", Locale.ENGLISH)
        .withZone(IST_ZONE_ID)

    // Compact Date & Time: "19 Aug · 08:00 PM IST"
    val compactDateTimeFormatter: DateTimeFormatter = DateTimeFormatter
        .ofPattern("dd MMM · hh:mm a 'IST'", Locale.ENGLISH)
        .withZone(IST_ZONE_ID)

    // Time Only: "08:00 PM IST"
    val timeOnlyFormatter: DateTimeFormatter = DateTimeFormatter
        .ofPattern("hh:mm a 'IST'", Locale.ENGLISH)
        .withZone(IST_ZONE_ID)

    // Date Only: "19 Aug 2026"
    val dateOnlyFormatter: DateTimeFormatter = DateTimeFormatter
        .ofPattern("dd MMM yyyy", Locale.ENGLISH)
        .withZone(IST_ZONE_ID)
}

/**
 * Extension helper to format an [Instant] in full Indian Standard Time.
 * Example: "19 Aug 2026, 08:00 PM IST"
 */
fun Instant.formatToIndianDateTime(): String {
    return IndianDateTimeFormatters.fullDateTimeFormatter.format(this)
}

/**
 * Extension helper to format an [Instant] in short Indian Standard Time with Day of Week.
 * Example: "Wed, 19 Aug · 08:00 PM IST"
 */
fun Instant.formatToIndianShortDateTime(): String {
    return IndianDateTimeFormatters.shortDateTimeFormatter.format(this)
}

/**
 * Extension helper to format an [Instant] in compact Indian Standard Time.
 * Example: "19 Aug · 08:00 PM IST"
 */
fun Instant.formatToIndianCompactDateTime(): String {
    return IndianDateTimeFormatters.compactDateTimeFormatter.format(this)
}

/**
 * Extension helper to format an [Instant] into time only in IST.
 * Example: "08:00 PM IST"
 */
fun Instant.formatToIndianTimeOnly(): String {
    return IndianDateTimeFormatters.timeOnlyFormatter.format(this)
}

/**
 * Formats duration in seconds into human readable duration string.
 * Example: 5400s -> "1h 30m", 7200s -> "2h", 1800s -> "30m"
 */
fun formatContestDuration(durationSeconds: Long): String {
    val h = durationSeconds / 3600
    val m = (durationSeconds % 3600) / 60
    return when {
        h > 0 && m > 0 -> "${h}h ${m}m"
        h > 0 -> "${h}h"
        else -> "${m}m"
    }
}
