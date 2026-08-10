package com.mycodecalendar.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mycodecalendar.core.database.dao.ContestDao
import com.mycodecalendar.core.database.dao.PlatformAccountDao
import com.mycodecalendar.core.database.entity.ContestEntity
import com.mycodecalendar.core.database.entity.PlatformAccountEntity
import com.mycodecalendar.core.database.entity.PlatformStatsEntity
import com.mycodecalendar.core.database.entity.RatingHistoryEntity
import com.mycodecalendar.core.database.entity.ReminderEntity
import com.mycodecalendar.core.database.entity.ResourceEntity
import com.mycodecalendar.core.database.entity.SavedContestEntity
import com.mycodecalendar.core.database.entity.SyncStateEntity

@Database(
    entities = [
        PlatformAccountEntity::class,
        PlatformStatsEntity::class,
        RatingHistoryEntity::class,
        ContestEntity::class,
        SavedContestEntity::class,
        ReminderEntity::class,
        ResourceEntity::class,
        SyncStateEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(InstantConverter::class)
abstract class MyCodeCalendarDatabase : RoomDatabase() {
    abstract fun platformAccountDao(): PlatformAccountDao
    abstract fun contestDao(): ContestDao
    // Add other DAOs as needed
}
