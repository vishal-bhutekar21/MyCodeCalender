package com.mycodecalendar.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mycodecalendar.core.database.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE enabled = 1 ORDER BY scheduledAt ASC")
    fun getAllActiveReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE enabled = 1 AND scheduledAt > :now ORDER BY scheduledAt ASC")
    suspend fun getUpcomingActiveReminders(now: Instant): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE contestId = :contestId LIMIT 1")
    suspend fun getReminderByContestId(contestId: String): ReminderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateReminder(reminder: ReminderEntity)

    @Query("DELETE FROM reminders WHERE contestId = :contestId")
    suspend fun deleteReminderByContestId(contestId: String)

    @Query("DELETE FROM reminders WHERE scheduledAt < :cutoff")
    suspend fun purgeOldReminders(cutoff: Instant)
}
