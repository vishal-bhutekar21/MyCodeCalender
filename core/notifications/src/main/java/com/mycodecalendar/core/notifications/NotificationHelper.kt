package com.mycodecalendar.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.time.Instant

object NotificationHelper {
    const val CHANNEL_CONTEST_REMINDERS = "contest_reminders_channel"
    const val CHANNEL_SYNC_STATUS = "sync_status_channel"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val reminderChannel = NotificationChannel(
                CHANNEL_CONTEST_REMINDERS,
                "Contest Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for upcoming coding contests"
                enableVibration(true)
            }

            val syncChannel = NotificationChannel(
                CHANNEL_SYNC_STATUS,
                "Sync & Updates",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background sync status updates"
            }

            manager.createNotificationChannel(reminderChannel)
            manager.createNotificationChannel(syncChannel)
        }
    }

    fun showContestReminderNotification(
        context: Context,
        notificationId: Int,
        contestId: String,
        platformName: String,
        contestName: String,
        startTimeMs: Long = System.currentTimeMillis() + 900000,
        minutesBefore: Int = 15
    ) {
        createNotificationChannels(context)

        // Content Intent (Opens Contest Detail screen)
        val deepLinkIntent = Intent(Intent.ACTION_VIEW, Uri.parse("mycodecalendar://contest/$contestId")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            deepLinkIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Notification Action Intent (Direct Add to Calendar button inside notification!)
        val calendarActionIntent = Intent(context, CalendarActionReceiver::class.java).apply {
            putExtra("contest_id", contestId)
            putExtra("platform_name", platformName)
            putExtra("contest_name", contestName)
            putExtra("start_time_ms", startTimeMs)
        }

        val calendarActionPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 1000,
            calendarActionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_CONTEST_REMINDERS)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("[$platformName] Contest Starting Soon!")
            .setContentText("$contestName starts in $minutesBefore minutes. Tap to view details.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(
                android.R.drawable.ic_menu_my_calendar,
                "📅 Add to Calendar",
                calendarActionPendingIntent
            )
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            println("Notification permission missing: ${e.message}")
        }
    }
}
