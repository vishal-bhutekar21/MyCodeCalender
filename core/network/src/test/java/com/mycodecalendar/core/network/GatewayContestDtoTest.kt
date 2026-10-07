package com.mycodecalendar.core.network

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GatewayContestDtoTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Test
    fun testDeserializeGatewayContestDto() {
        val sampleJson = """
            {
                "id": "lc-weekly-420",
                "providerContestId": "weekly-contest-420",
                "platform": "LEETCODE",
                "name": "Weekly Contest 420",
                "officialUrl": "https://leetcode.com/contest/weekly-contest-420/",
                "registrationUrl": "https://leetcode.com/contest/weekly-contest-420/",
                "startTimeUtc": "2026-10-18T02:30:00Z",
                "endTimeUtc": "2026-10-18T04:00:00Z",
                "durationSeconds": 5400,
                "contestType": "LeetCode Weekly",
                "ratingType": "Rated",
                "status": "UPCOMING",
                "lastFetchedAt": "2026-10-07T12:00:00Z"
            }
        """.trimIndent()

        val dto = json.decodeFromString<GatewayContestDto>(sampleJson)

        assertEquals("lc-weekly-420", dto.id)
        assertEquals("LEETCODE", dto.platform)
        assertEquals("Weekly Contest 420", dto.name)
        assertEquals(5400L, dto.durationSeconds)
        assertEquals("UPCOMING", dto.status)
        assertEquals("Rated", dto.ratingType)
    }

    @Test
    fun testDeserializeGatewayContestsResponseDto() {
        val responseJson = """
            {
                "status": "success",
                "count": 2,
                "cached": true,
                "data": [
                    {
                        "id": "cf-2040",
                        "providerContestId": "2040",
                        "platform": "CODEFORCES",
                        "name": "Codeforces Round 990 (Div. 2)",
                        "officialUrl": "https://codeforces.com/contest/2040",
                        "startTimeUtc": "2026-10-15T14:35:00Z",
                        "endTimeUtc": "2026-10-15T16:35:00Z",
                        "durationSeconds": 7200,
                        "status": "UPCOMING",
                        "lastFetchedAt": "2026-10-07T12:00:00Z"
                    },
                    {
                        "id": "atc-abc380",
                        "providerContestId": "abc380",
                        "platform": "ATCODER",
                        "name": "AtCoder Beginner Contest 380",
                        "officialUrl": "https://atcoder.jp/contests/abc380",
                        "startTimeUtc": "2026-10-17T12:00:00Z",
                        "endTimeUtc": "2026-10-17T13:40:00Z",
                        "durationSeconds": 6000,
                        "status": "UPCOMING",
                        "lastFetchedAt": "2026-10-07T12:00:00Z"
                    }
                ]
            }
        """.trimIndent()

        val response = json.decodeFromString<GatewayContestsResponseDto>(responseJson)

        assertEquals("success", response.status)
        assertEquals(2, response.count)
        assertTrue(response.cached)
        assertEquals(2, response.data.size)
        assertEquals("CODEFORCES", response.data[0].platform)
        assertEquals("ATCODER", response.data[1].platform)
    }
}
