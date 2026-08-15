package com.mycodecalendar.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mycodecalendar.core.database.entity.RatingHistoryEntity

@Dao
interface RatingHistoryDao {

    @Query(
        "SELECT * FROM rating_history WHERE platform = :platform AND username = :username " +
        "ORDER BY timestamp ASC"
    )
    suspend fun getHistory(platform: String, username: String): List<RatingHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(points: List<RatingHistoryEntity>)

    @Query("DELETE FROM rating_history WHERE platform = :platform AND username = :username")
    suspend fun deleteHistory(platform: String, username: String)

    @Query("DELETE FROM rating_history")
    suspend fun deleteAllHistory()
}
