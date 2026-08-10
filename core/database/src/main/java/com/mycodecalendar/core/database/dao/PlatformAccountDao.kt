package com.mycodecalendar.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mycodecalendar.core.database.entity.PlatformAccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlatformAccountDao {
    @Query("SELECT * FROM platform_accounts")
    fun getAllAccounts(): Flow<List<PlatformAccountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: PlatformAccountEntity)

    @Query("DELETE FROM platform_accounts WHERE id = :id")
    suspend fun deleteAccount(id: String)
}
