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

    @Query("DELETE FROM contests")
    suspend fun deleteAllContests()
}
