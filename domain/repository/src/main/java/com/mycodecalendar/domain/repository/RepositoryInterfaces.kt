package com.mycodecalendar.domain.repository

import com.mycodecalendar.domain.model.Contest
import com.mycodecalendar.domain.model.PlatformAccount
import com.mycodecalendar.domain.model.PlatformStats
import kotlinx.coroutines.flow.Flow

interface PlatformRepository {
    fun getAccounts(): Flow<List<PlatformAccount>>
    fun getPlatformStats(platformId: String): Flow<PlatformStats?>
    suspend fun addAccount(account: PlatformAccount)
    suspend fun syncAll()
}

interface ContestRepository {
    fun getUpcomingContests(): Flow<List<Contest>>
    suspend fun refreshContests()
}
