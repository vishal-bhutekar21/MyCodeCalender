package com.mycodecalendar.core.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class RemoteDataSource(
    private val baseUrl: String = "http://10.0.2.2:8080/v1"
) {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    suspend fun getContests(platform: String? = null, status: String? = null): Result<List<ContestDto>> {
        return try {
            val response: ApiResponse<List<ContestDto>> = client.get("$baseUrl/contests") {
                parameter("platform", platform)
                parameter("status", status)
            }.body()
            Result.success(response.data ?: emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPlatformStats(platform: String, username: String): Result<PlatformStatsDto> {
        return try {
            val response: ApiResponse<PlatformStatsDto> = client.get("$baseUrl/platforms/$platform/stats/$username").body()
            if (response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.error?.message ?: "Stats not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getResources(category: String? = null): Result<List<ResourceDto>> {
        return try {
            val response: ApiResponse<List<ResourceDto>> = client.get("$baseUrl/resources") {
                parameter("category", category)
            }.body()
            Result.success(response.data ?: emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
