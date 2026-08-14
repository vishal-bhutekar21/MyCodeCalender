package com.mycodecalendar.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mycodecalendar.core.database.entity.GitHubStatsEntity

/**
 * Room DAO for reading and writing cached GitHub user profile data.
 *
 * Insert uses REPLACE strategy — each successful GitHub API fetch
 * overwrites the previous cache entry for the same username.
 */
@Dao
interface GitHubStatsDao {

    @Query("SELECT * FROM github_stats WHERE username = :username LIMIT 1")
    suspend fun getGitHubStats(username: String): GitHubStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGitHubStats(stats: GitHubStatsEntity)

    @Query("DELETE FROM github_stats WHERE username = :username")
    suspend fun deleteGitHubStats(username: String)

    @Query("DELETE FROM github_stats")
    suspend fun clearAll()
}
