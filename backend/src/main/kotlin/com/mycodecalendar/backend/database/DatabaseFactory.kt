package com.mycodecalendar.backend.database

import com.mycodecalendar.backend.models.ContestDto
import com.mycodecalendar.backend.providers.ContestResult
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Timestamp
import java.time.Instant
import java.util.UUID

object DatabaseFactory {
    private val dbUrl = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/mycodecalendar"
    private val dbUser = System.getenv("DB_USER") ?: "postgres"
    private val dbPassword = System.getenv("DB_PASSWORD") ?: "postgrespassword"

    fun getConnection(): Connection? {
        return try {
            Class.forName("org.postgresql.Driver")
            DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        } catch (e: Exception) {
            println("Database connection unavailable (running in memory/mock fallback): ${e.message}")
            null
        }
    }

    fun upsertContests(contests: List<ContestResult>, platform: String) {
        val conn = getConnection() ?: return
        val sql = """
            INSERT INTO contests (id, platform, provider_contest_id, name, description, official_url, registration_url, start_time_utc, end_time_utc, duration_seconds, contest_type, rating_type, status, last_fetched_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (platform, provider_contest_id) DO UPDATE SET
                name = EXCLUDED.name,
                official_url = EXCLUDED.official_url,
                start_time_utc = EXCLUDED.start_time_utc,
                end_time_utc = EXCLUDED.end_time_utc,
                duration_seconds = EXCLUDED.duration_seconds,
                status = EXCLUDED.status,
                last_fetched_at = EXCLUDED.last_fetched_at;
        """.trimIndent()

        try {
            conn.prepareStatement(sql).use { stmt ->
                contests.forEach { c ->
                    stmt.setObject(1, UUID.nameUUIDFromBytes("${platform}:${c.providerContestId}".toByteArray()))
                    stmt.setString(2, platform)
                    stmt.setString(3, c.providerContestId)
                    stmt.setString(4, c.name)
                    stmt.setString(5, c.description ?: "")
                    stmt.setString(6, c.url)
                    stmt.setString(7, c.url)
                    stmt.setTimestamp(8, Timestamp.from(c.startTime))
                    stmt.setTimestamp(9, Timestamp.from(c.endTime))
                    stmt.setInt(10, c.durationSeconds)
                    stmt.setString(11, c.contestType ?: "Standard")
                    stmt.setString(12, c.ratingType ?: "Rated")
                    stmt.setString(13, c.status)
                    stmt.setTimestamp(14, Timestamp.from(Instant.now()))
                    stmt.addBatch()
                }
                stmt.executeBatch()
            }
        } catch (e: Exception) {
            println("Failed to upsert contests to Postgres: ${e.message}")
        } finally {
            conn.close()
        }
    }

    fun getContests(platform: String? = null, status: String? = null): List<ContestDto> {
        val conn = getConnection() ?: return emptyList()
        val list = mutableListOf<ContestDto>()

        var sql = "SELECT id, platform, provider_contest_id, name, description, official_url, registration_url, start_time_utc, end_time_utc, duration_seconds, contest_type, rating_type, status, last_fetched_at FROM contests"
        val params = mutableListOf<String>()

        if (!platform.isNullOrBlank()) {
            params.add("platform = '$platform'")
        }
        if (!status.isNullOrBlank()) {
            params.add("status = '$status'")
        }

        if (params.isNotEmpty()) {
            sql += " WHERE " + params.joinToString(" AND ")
        }
        sql += " ORDER BY start_time_utc ASC;"

        try {
            conn.createStatement().use { stmt ->
                val rs = stmt.executeQuery(sql)
                while (rs.next()) {
                    list.add(
                        ContestDto(
                            id = rs.getString("id"),
                            platform = rs.getString("platform"),
                            providerContestId = rs.getString("provider_contest_id"),
                            name = rs.getString("name"),
                            description = rs.getString("description"),
                            officialUrl = rs.getString("official_url"),
                            registrationUrl = rs.getString("registration_url"),
                            startTimeUtc = rs.getTimestamp("start_time_utc").toInstant().toString(),
                            endTimeUtc = rs.getTimestamp("end_time_utc").toInstant().toString(),
                            durationSeconds = rs.getInt("duration_seconds"),
                            contestType = rs.getString("contest_type"),
                            ratingType = rs.getString("rating_type"),
                            status = rs.getString("status"),
                            lastFetchedAt = rs.getTimestamp("last_fetched_at").toInstant().toString()
                        )
                    )
                }
            }
        } catch (e: Exception) {
            println("Failed to fetch contests from Postgres: ${e.message}")
        } finally {
            conn.close()
        }
        return list
    }
}
