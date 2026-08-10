package com.mycodecalendar.core.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.mycodecalendar.core.calendar.CalendarContractManager
import com.mycodecalendar.domain.model.Contest
import com.mycodecalendar.domain.model.ContestStatus
import com.mycodecalendar.domain.model.Platform
import java.time.Instant

class CalendarActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val contestId = intent.getStringExtra("contest_id") ?: ""
        val platformName = intent.getStringExtra("platform_name") ?: "CODEFORCES"
        val contestName = intent.getStringExtra("contest_name") ?: "Contest"
        val startTimeMs = intent.getLongExtra("start_time_ms", System.currentTimeMillis() + 900000)

        val platform = try { Platform.valueOf(platformName) } catch (e: Exception) { Platform.CODEFORCES }

        val contest = Contest(
            id = contestId,
            providerContestId = contestId,
            platform = platform,
            name = contestName,
            officialUrl = "https://${platform.name.lowercase()}.com",
            registrationUrl = null,
            startTimeUtc = Instant.ofEpochMilli(startTimeMs),
            endTimeUtc = Instant.ofEpochMilli(startTimeMs + 7200000),
            durationSeconds = 7200L,
            contestType = null,
            ratingType = null,
            status = ContestStatus.UPCOMING,
            lastFetchedAt = Instant.now()
        )

        val calendarManager = CalendarContractManager(context)
        val result = calendarManager.addContestToCalendar(contest)

        if (result.isSuccess) {
            Toast.makeText(context, "✓ Added [${platform.name}] $contestName to your calendar!", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "Failed to add to calendar: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
        }
    }
}
