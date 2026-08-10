package com.mycodecalendar.core.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.mycodecalendar.domain.model.Contest
import java.time.Instant

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val contestId = intent.getStringExtra("contest_id") ?: ""
        val platformName = intent.getStringExtra("platform_name") ?: "Contest"
        val contestName = intent.getStringExtra("contest_name") ?: "Coding Contest"
        val notificationId = intent.getIntExtra("notification_id", System.currentTimeMillis().toInt())

        NotificationHelper.showContestReminderNotification(
            context = context,
            notificationId = notificationId,
            contestId = contestId,
            platformName = platformName,
            contestName = contestName,
            minutesBefore = 15
        )
    }
}

class ReminderScheduler(private val context: Context) {

    fun scheduleExactReminder(contest: Contest, offsetMinutes: Long = 15): Boolean {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return false
        val triggerTimeMs = contest.startTimeUtc.minusSeconds(offsetMinutes * 60).toEpochMilli()

        if (triggerTimeMs <= Instant.now().toEpochMilli()) {
            return false // Trigger time is already in the past
        }

        val notificationId = contest.id.hashCode()
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("contest_id", contest.id)
            putExtra("platform_name", contest.platform.name)
            putExtra("contest_name", contest.name)
            putExtra("notification_id", notificationId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMs,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMs,
                    pendingIntent
                )
            }
            return true
        } catch (e: SecurityException) {
            println("Exact alarm permission not granted: ${e.message}")
            return false
        }
    }

    fun cancelReminder(contestId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val notificationId = contestId.hashCode()
        val intent = Intent(context, ReminderReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}
