package com.mycodecalendar.data.repository

import com.mycodecalendar.core.database.dao.ContestDao
import com.mycodecalendar.core.database.entity.ContestEntity
import com.mycodecalendar.core.network.RemoteDataSource
import com.mycodecalendar.domain.model.Contest
import com.mycodecalendar.domain.model.ContestStatus
import com.mycodecalendar.domain.model.Platform
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant

class ContestRepositoryImpl(
    private val contestDao: ContestDao,
    private val remoteDataSource: RemoteDataSource = RemoteDataSource()
) {
    fun getContestsFlow(): Flow<List<Contest>> {
        return contestDao.getAllContests().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    suspend fun refreshContests(): Result<Unit> {
        val remoteResult = remoteDataSource.getContests()
        return if (remoteResult.isSuccess) {
            val dtos = remoteResult.getOrNull() ?: emptyList()
            val entities = dtos.map { dto ->
                ContestEntity(
                    id = dto.id,
                    providerContestId = dto.providerContestId,
                    platform = dto.platform,
                    name = dto.name,
                    officialUrl = dto.officialUrl,
                    registrationUrl = dto.registrationUrl,
                    startTimeUtc = Instant.parse(dto.startTimeUtc),
                    endTimeUtc = Instant.parse(dto.endTimeUtc),
                    durationSeconds = dto.durationSeconds.toLong(),
                    contestType = dto.contestType,
                    ratingType = dto.ratingType,
                    status = dto.status,
                    lastFetchedAt = Instant.now()
                )
            }
            contestDao.insertContests(entities)
            Result.success(Unit)
        } else {
            Result.failure(remoteResult.exceptionOrNull() ?: Exception("Unknown error"))
        }
    }
}

private fun ContestEntity.toDomainModel(): Contest {
    val platformEnum = try { Platform.valueOf(platform) } catch (e: Exception) { Platform.CODEFORCES }
    val statusEnum = try { ContestStatus.valueOf(status) } catch (e: Exception) { ContestStatus.UPCOMING }
    return Contest(
        id = id,
        providerContestId = providerContestId,
        platform = platformEnum,
        name = name,
        officialUrl = officialUrl,
        registrationUrl = registrationUrl,
        startTimeUtc = startTimeUtc,
        endTimeUtc = endTimeUtc,
        durationSeconds = durationSeconds,
        contestType = contestType,
        ratingType = ratingType,
        status = statusEnum,
        lastFetchedAt = lastFetchedAt
    )
}
