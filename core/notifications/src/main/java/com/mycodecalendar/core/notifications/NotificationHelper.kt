package com.mycodecalendar.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {

    // ── Channel IDs ────────────────────────────────────────────────────────────
    const val CHANNEL_CONTEST_REMINDERS = "contest_reminders_channel"
    const val CHANNEL_SYNC_STATUS       = "sync_status_channel"
    const val CHANNEL_FCM_BROADCASTS    = "fcm_broadcasts_channel"   // Cloud push / Cloudflare worker
    const val CHANNEL_POTD              = "potd_channel"             // Problem of the day

    // ── Notification Group Keys ────────────────────────────────────────────────
    private const val GROUP_CONTESTS    = "group_contests"
    private const val GROUP_BROADCASTS  = "group_broadcasts"

    // ── App launch class (resolved at runtime via package manager) ─────────────
    private const val MAIN_ACTIVITY_CLASS = "com.vishal.mycodecalendar.MainActivity"

    // ─────────────────────────────────────────────────────────────────────────
    // Channel Creation
    // ─────────────────────────────────────────────────────────────────────────

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Contest Reminders — HIGH importance (heads-up)
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_CONTEST_REMINDERS,
                    "Contest Reminders",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Upcoming coding contest start alerts"
                    enableVibration(true)
                    setShowBadge(true)
                }
            )

            // FCM / Cloud Broadcasts — HIGH importance
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_FCM_BROADCASTS,
                    "App Announcements",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Hackathons, contests, and app updates from the admin"
                    enableVibration(true)
                    setShowBadge(true)
                }
            )

            // Problem of the Day — DEFAULT importance
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_POTD,
                    "Daily Problem",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Daily coding problem reminder"
                    setShowBadge(true)
                }
            )

            // Sync & Status — LOW (silent)
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_SYNC_STATUS,
                    "Sync & Updates",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Background sync status"
                    setShowBadge(false)
                }
            )
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Contest Reminder Notification
    // ─────────────────────────────────────────────────────────────────────────

    fun showContestReminderNotification(
        context: Context,
        notificationId: Int,
        contestId: String,
        platformName: String,
        contestName: String,
        startTimeMs: Long = System.currentTimeMillis() + 900_000L,
        minutesBefore: Int = 15
    ) {
        createNotificationChannels(context)

        // Tap → open contest detail via deep link
        val deepLinkIntent = Intent(Intent.ACTION_VIEW, Uri.parse("codecalendar://contest/$contestId")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            setPackage(context.packageName)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context, notificationId, deepLinkIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Add to Calendar
        val calendarActionIntent = Intent(context, CalendarActionReceiver::class.java).apply {
            putExtra("contest_id", contestId)
            putExtra("platform_name", platformName)
            putExtra("contest_name", contestName)
            putExtra("start_time_ms", startTimeMs)
        }
        val calendarPendingIntent = PendingIntent.getBroadcast(
            context, notificationId + 1000, calendarActionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title   = "🏆 $platformName Contest Starts in $minutesBefore min"
        val bodyMsg = "$contestName is starting soon! Tap to view details and compete."

        val appIcon = getAppIconBitmap(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_CONTEST_REMINDERS)
            .setSmallIcon(context.applicationInfo.icon)
            .apply { if (appIcon != null) setLargeIcon(appIcon) }
            .setContentTitle(title)
            .setContentText(bodyMsg)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(bodyMsg)
                    .setSummaryText(platformName)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .setGroup(GROUP_CONTESTS)
            .setColor(0xFFFF7A00.toInt())   // brand orange accent
            .addAction(
                android.R.drawable.ic_menu_my_calendar,
                "Add to Calendar",
                calendarPendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_view,
                "View Contest",
                contentPendingIntent
            )
            .build()

        safeNotify(context, notificationId, notification)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FCM / Cloudflare Push Notification
    // Shown when a cloud push (Firebase / Cloudflare Worker) arrives.
    // Supports:
    //   - "codecalendar://..."
    //   - "contest:<id>"
    //   - "broadcast:<id>"
    //   - "tab:<name>"
    //   - "https://..."
    // ─────────────────────────────────────────────────────────────────────────

    fun showFcmPushNotification(
        context: Context,
        notificationId: Int = System.currentTimeMillis().toInt(),
        title: String,
        body: String,
        actionUrl: String = "",
        badge: String = ""
    ) {
        createNotificationChannels(context)

        // Build the tap intent based on actionUrl
        val tapIntent: Intent = when {
            actionUrl.startsWith("http://") || actionUrl.startsWith("https://") -> {
                Intent(Intent.ACTION_VIEW, Uri.parse(actionUrl)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            }
            actionUrl.startsWith("codecalendar://") -> {
                Intent(Intent.ACTION_VIEW, Uri.parse(actionUrl)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    setPackage(context.packageName)
                }
            }
            actionUrl.startsWith("contest:") -> {
                val contestId = actionUrl.removePrefix("contest:").trim()
                Intent(Intent.ACTION_VIEW, Uri.parse("codecalendar://contest/$contestId")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    setPackage(context.packageName)
                }
            }
            actionUrl.startsWith("broadcast:") -> {
                val broadcastId = actionUrl.removePrefix("broadcast:").trim()
                Intent(Intent.ACTION_VIEW, Uri.parse("codecalendar://broadcast/$broadcastId")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    setPackage(context.packageName)
                }
            }
            actionUrl.startsWith("tab:") -> {
                val tab = actionUrl.removePrefix("tab:").trim()
                Intent(Intent.ACTION_VIEW, Uri.parse("codecalendar://tab/$tab")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    setPackage(context.packageName)
                }
            }
            actionUrl.isNotBlank() && (actionUrl in listOf("contests", "resources", "settings", "home", "notifications_list", "streak")) -> {
                Intent(Intent.ACTION_VIEW, Uri.parse("codecalendar://tab/$actionUrl")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    setPackage(context.packageName)
                }
            }
            else -> {
                context.packageManager
                    .getLaunchIntentForPackage(context.packageName)
                    ?.apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP }
                    ?: Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_LAUNCHER)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        setPackage(context.packageName)
                    }
            }
        }

        val contentPendingIntent = PendingIntent.getActivity(
            context, notificationId, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val appIcon = getAppIconBitmap(context)
        val subText = badge.takeIf { it.isNotBlank() } ?: "MyCodeCalendar"

        val builder = NotificationCompat.Builder(context, CHANNEL_FCM_BROADCASTS)
            .setSmallIcon(context.applicationInfo.icon)
            .apply { if (appIcon != null) setLargeIcon(appIcon) }
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(body)
                    .setSummaryText(subText)
            )
            .setSubText(subText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .setGroup(GROUP_BROADCASTS)
            .setColor(0xFFFF7A00.toInt())

        if (actionUrl.isNotBlank()) {
            val actionLabel = if (actionUrl.startsWith("http://") || actionUrl.startsWith("https://")) {
                "Open Link"
            } else {
                "View Details"
            }
            builder.addAction(android.R.drawable.ic_menu_view, actionLabel, contentPendingIntent)
        }

        val notification = builder.build()
        safeNotify(context, notificationId, notification)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Problem of the Day Notification
    // ─────────────────────────────────────────────────────────────────────────

    fun showPotdNotification(
        context: Context,
        notificationId: Int = 9901,
        problemTitle: String,
        difficulty: String,
        problemUrl: String
    ) {
        createNotificationChannels(context)

        val tapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(problemUrl)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, notificationId, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val body = "Today's $difficulty problem: $problemTitle. Tap to solve it now."
        val appIcon = getAppIconBitmap(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_POTD)
            .setSmallIcon(context.applicationInfo.icon)
            .apply { if (appIcon != null) setLargeIcon(appIcon) }
            .setContentTitle("🧠 Daily Problem of the Day")
            .setContentText(body)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(body)
                    .setSummaryText("LeetCode · $difficulty")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setColor(0xFFFFA116.toInt())   // LeetCode yellow
            .addAction(android.R.drawable.ic_menu_view, "Solve Challenge", pendingIntent)
            .build()

        safeNotify(context, notificationId, notification)
    }

    private fun getAppIconBitmap(context: Context): Bitmap? {
        return try {
            val drawable = context.packageManager.getApplicationIcon(context.packageName)
            if (drawable is BitmapDrawable) {
                drawable.bitmap
            } else {
                val width = drawable.intrinsicWidth.coerceAtLeast(1)
                val height = drawable.intrinsicHeight.coerceAtLeast(1)
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bitmap
            }
        } catch (_: Exception) {
            try {
                BitmapFactory.decodeResource(context.resources, context.applicationInfo.icon)
            } catch (_: Exception) {
                null
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private fun safeNotify(context: Context, id: Int, notification: android.app.Notification) {
        try {
            NotificationManagerCompat.from(context).notify(id, notification)
        } catch (e: SecurityException) {
            // Permission not granted — permission dialog is shown separately in the UI
        }
    }
}
