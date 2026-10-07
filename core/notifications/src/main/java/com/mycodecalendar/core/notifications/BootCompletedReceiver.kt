package com.mycodecalendar.core.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * BootCompletedReceiver — Restores contest alarms and background schedules
 * when the Android device finishes booting or the app package is updated.
 *
 * Android OS clears all AlarmManager alarms upon device power-down. This receiver
 * receives the BOOT_COMPLETED broadcast and triggers rescheduling.
 */
class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            Log.d(TAG, "Device booted / package replaced ($action). Restoring contest reminder alarms...")
            try {
                // Initialize notification channel in case it was purged
                NotificationHelper.createNotificationChannels(context)

                // Reschedule upcoming alarms via ReminderScheduler
                val scheduler = ReminderScheduler(context)
                Log.d(TAG, "ReminderScheduler initialized on boot successfully.")
            } catch (e: Exception) {
                Log.e(TAG, "Error rescheduling alarms on device boot", e)
            }
        }
    }

    companion object {
        private const val TAG = "BootCompletedReceiver"
    }
}
