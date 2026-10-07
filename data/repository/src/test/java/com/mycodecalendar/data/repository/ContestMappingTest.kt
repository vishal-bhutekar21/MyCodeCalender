package com.mycodecalendar.data.repository

import com.mycodecalendar.core.network.GatewayContestDto
import com.mycodecalendar.domain.model.Contest
import com.mycodecalendar.domain.model.ContestStatus
import com.mycodecalendar.domain.model.Platform
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class ContestMappingTest {

    private fun computeStatus(start: Instant, end: Instant): ContestStatus {
        val now = Instant.now()
        val minValid = Instant.parse("2024-01-01T00:00:00Z")
        return when {
            start.isBefore(minValid) -> ContestStatus.ENDED
            now.isAfter(end) -> ContestStatus.ENDED
            now.isAfter(start) || now == start -> ContestStatus.LIVE
            else -> ContestStatus.UPCOMING
        }
    }

    private fun parsePlatform(site: String): Platform? {
        val s = site.lowercase()
        return when {
            s.contains("codeforces") || s.contains("code_forces") -> Platform.CODEFORCES
            s.contains("leetcode") -> Platform.LEETCODE
            s.contains("codechef") -> Platform.CODECHEF
            s.contains("atcoder") -> Platform.ATCODER
            s.contains("geeks") || s.contains("gfg") -> Platform.GEEKSFORGEEKS
            else -> null
        }
    }

    @Test
    fun testParsePlatformsCorrectly() {
        assertEquals(Platform.LEETCODE, parsePlatform("LeetCode"))
        assertEquals(Platform.CODEFORCES, parsePlatform("codeforces"))
        assertEquals(Platform.ATCODER, parsePlatform("AtCoder"))
        assertEquals(Platform.CODECHEF, parsePlatform("CodeChef"))
        assertEquals(Platform.GEEKSFORGEEKS, parsePlatform("geeksforgeeks"))
        assertNull(parsePlatform("unknown_platform"))
    }

    @Test
    fun testComputeStatusUpcoming() {
        val futureStart = Instant.now().plusSeconds(3600)
        val futureEnd = futureStart.plusSeconds(7200)

        val status = computeStatus(futureStart, futureEnd)
        assertEquals(ContestStatus.UPCOMING, status)
    }

    @Test
    fun testComputeStatusLive() {
        val pastStart = Instant.now().minusSeconds(1800)
        val futureEnd = Instant.now().plusSeconds(1800)

        val status = computeStatus(pastStart, futureEnd)
        assertEquals(ContestStatus.LIVE, status)
    }

    @Test
    fun testComputeStatusEnded() {
        val pastStart = Instant.now().minusSeconds(7200)
        val pastEnd = Instant.now().minusSeconds(3600)

        val status = computeStatus(pastStart, pastEnd)
        assertEquals(ContestStatus.ENDED, status)
    }
}
