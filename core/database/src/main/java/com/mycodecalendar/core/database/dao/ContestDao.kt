package com.mycodecalendar.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mycodecalendar.core.database.entity.ContestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContestDao {
    @Query("SELECT * FROM contests ORDER BY startTimeUtc ASC")
    fun getAllContests(): Flow<List<ContestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContests(contests: List<ContestEntity>)

    @Query("SELECT * FROM contests ORDER BY startTimeUtc ASC")
    suspend fun getAllContestsList(): List<ContestEntity>

    @Query("DELETE FROM contests WHERE id LIKE '%APG4b%' OR id LIKE '%abs%' OR id LIKE '%adt%' OR durationSeconds > 2592000 OR durationSeconds <= 0 OR name LIKE '%Programming Guide%' OR name LIKE '%Beginners Selection%' OR name LIKE '%Tutorial%' OR name LIKE '%入門%' OR startTimeUtc < '2025-01-01T00:00:00Z'")
    suspend fun purgeInvalidContests()

    @Query("DELETE FROM contests")
    suspend fun deleteAllContests()
}
