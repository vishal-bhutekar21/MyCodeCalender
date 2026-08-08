package com.mycodecalendar.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mycodecalendar.core.database.entity.PlatformStatsEntity

@Dao
interface PlatformStatsDao {

    @Query("SELECT * FROM platform_stats WHERE platform = :platform AND username = :username LIMIT 1")
    suspend fun getStats(platform: String, username: String): PlatformStatsEntity?

    @Query("SELECT * FROM platform_stats")
    suspend fun getAllStats(): List<PlatformStatsEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStats(stats: PlatformStatsEntity)

    @Query("DELETE FROM platform_stats WHERE platform = :platform AND username = :username")
    suspend fun deleteStats(platform: String, username: String)

    @Query("DELETE FROM platform_stats")
    suspend fun deleteAllStats()
}
