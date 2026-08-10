package com.mycodecalendar.core.calendar

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.CalendarContract
import com.mycodecalendar.domain.model.Contest
import java.util.TimeZone

class CalendarContractManager(private val context: Context) {

    fun addContestToCalendar(contest: Contest): Result<Long> {
        return try {
            val cr: ContentResolver = context.contentResolver
            val calendarId = getPrimaryWritableCalendarId(cr) ?: return Result.failure(Exception("No writable calendar found"))

            // Check if already added to prevent duplicates
            val existingEventId = findExistingEventId(cr, contest.id)
            if (existingEventId != null) {
                return Result.success(existingEventId)
            }

            val values = ContentValues().apply {
                put(CalendarContract.Events.DTSTART, contest.startTimeUtc.toEpochMilli())
                put(CalendarContract.Events.DTEND, contest.endTimeUtc.toEpochMilli())
                put(CalendarContract.Events.TITLE, "[${contest.platform.name}] ${contest.name}")
                put(
                    CalendarContract.Events.DESCRIPTION,
                    "Contest Link: ${contest.officialUrl}\n\nMyCodeCalendarContestId=${contest.id}"
                )
                put(CalendarContract.Events.CALENDAR_ID, calendarId)
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
                put(CalendarContract.Events.HAS_ALARM, 1)
            }

            val uri: Uri? = cr.insert(CalendarContract.Events.CONTENT_URI, values)
            val eventId = uri?.lastPathSegment?.toLongOrNull()

            if (eventId != null) {
                // Add 15m default reminder alarm
                val reminderValues = ContentValues().apply {
                    put(CalendarContract.Reminders.EVENT_ID, eventId)
                    put(CalendarContract.Reminders.MINUTES, 15)
                    put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
                }
                cr.insert(CalendarContract.Reminders.CONTENT_URI, reminderValues)
                Result.success(eventId)
            } else {
                Result.failure(Exception("Failed to insert event to Calendar"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun removeContestFromCalendar(contestId: String): Result<Boolean> {
        return try {
            val cr: ContentResolver = context.contentResolver
            val eventId = findExistingEventId(cr, contestId) ?: return Result.success(false)

            val deleteUri = Uri.withAppendedPath(CalendarContract.Events.CONTENT_URI, eventId.toString())
            val rows = cr.delete(deleteUri, null, null)
            Result.success(rows > 0)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun findExistingEventId(cr: ContentResolver, contestId: String): Long? {
        val projection = arrayOf(CalendarContract.Events._ID, CalendarContract.Events.DESCRIPTION)
        val selection = "${CalendarContract.Events.DESCRIPTION} LIKE ?"
        val selectionArgs = arrayOf("%MyCodeCalendarContestId=$contestId%")

        cr.query(CalendarContract.Events.CONTENT_URI, projection, selection, selectionArgs, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getLong(0)
            }
        }
        return null
    }

    private fun getPrimaryWritableCalendarId(cr: ContentResolver): Long? {
        val projection = arrayOf(CalendarContract.Calendars._ID, CalendarContract.Calendars.IS_PRIMARY)
        cr.query(CalendarContract.Calendars.CONTENT_URI, projection, null, null, null)?.use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getLong(0)
                val isPrimary = cursor.getInt(1)
                if (isPrimary == 1) return id
            }
        }
        return 1L // Fallback to calendar ID 1 if primary flag not set
    }
}
