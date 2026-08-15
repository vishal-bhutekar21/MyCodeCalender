package com.mycodecalendar.feature.contests

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowOutward
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycodecalendar.core.designsystem.GlassmorphismBackground
import com.mycodecalendar.core.designsystem.Typography
import com.mycodecalendar.core.designsystem.components.ContestCardSkeleton
import com.mycodecalendar.core.designsystem.components.EmptyState
import com.mycodecalendar.core.designsystem.components.GlassCard
import com.mycodecalendar.core.designsystem.components.GlassChip
import com.mycodecalendar.core.designsystem.components.PlatformBadge
import com.mycodecalendar.core.designsystem.components.StatusChip
import com.mycodecalendar.core.designsystem.components.getBrandColor
import com.mycodecalendar.domain.model.Contest
import com.mycodecalendar.domain.model.ContestStatus
import com.mycodecalendar.domain.model.PastContestRecord
import com.mycodecalendar.domain.model.Platform
import java.time.Duration
import java.time.Instant

/**
 * ContestsScreen — Contest discovery, search, live filters, and past contest performance records.
 *
 * Primary Tabs:
 * - "Upcoming & Live": Live & upcoming contests aggregator feed.
 * - "My History & Ratings": Past participated contests, rating deltas (+/-), and problems solved counts.
 *   Shows interactive "No Platforms Connected — Connect Handles" card when no accounts are connected.
 */
@Composable
fun ContestsScreen(
    contests: List<Contest>,
    pastContests: List<PastContestRecord> = samplePastContests,
    onContestClick: (String) -> Unit,
    onAddPlatformClick: () -> Unit = {},
    onPastContestClick: (String) -> Unit = {}
) {
    var selectedMainTab by remember { mutableStateOf(0) } // 0 = Upcoming & Live, 1 = My History & Ratings
    var searchQuery by remember { mutableStateOf("") }
    var selectedPlatform by remember { mutableStateOf<Platform?>(null) }
    var selectedStatus by remember { mutableStateOf<ContestStatus?>(null) }

    val filteredContests = remember(contests, searchQuery, selectedPlatform, selectedStatus) {
        contests.filter { contest ->
            val matchesQuery = searchQuery.isBlank() || contest.name.contains(searchQuery, ignoreCase = true)
            val matchesPlatform = selectedPlatform == null || contest.platform == selectedPlatform
            val matchesStatus = selectedStatus == null || contest.status == selectedStatus
            matchesQuery && matchesPlatform && matchesStatus
        }
    }

    val filteredPastContests = remember(pastContests, searchQuery, selectedPlatform) {
        pastContests.filter { past ->
            val matchesQuery = searchQuery.isBlank() || past.contestName.contains(searchQuery, ignoreCase = true)
            val matchesPlatform = selectedPlatform == null || past.platform == selectedPlatform
            matchesQuery && matchesPlatform
        }
    }

    GlassmorphismBackground {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // ── HEADER ───────────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 20.dp, bottom = 8.dp)
            ) {
                Text(
                    text = "Contests & Rating History",
                    style = Typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (selectedMainTab == 0) "${filteredContests.size} contests available"
                    else if (filteredPastContests.isEmpty()) "Connect platform handles to sync records"
                    else "${filteredPastContests.size} past records logged",
                    style = Typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ── PRIMARY TAB TOGGLE (Upcoming vs My History) ──────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PrimaryTabPill(
                    label = "Upcoming & Live",
                    selected = selectedMainTab == 0,
                    badgeCount = contests.count { it.status == ContestStatus.LIVE || it.status == ContestStatus.UPCOMING },
                    onClick = { selectedMainTab = 0 },
                    modifier = Modifier.weight(1f)
                )
                PrimaryTabPill(
                    label = "My History & Ratings",
                    selected = selectedMainTab == 1,
                    badgeCount = pastContests.size,
                    onClick = { selectedMainTab = 1 },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── 1PX BORDERED MINIMALIST SEARCH BAR ─────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                com.mycodecalendar.core.designsystem.components.AppSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = if (selectedMainTab == 0) "Search live & upcoming contests…" else "Search past rating history…"
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ── CONSOLIDATED PLATFORM & LIVE FILTER BAR ─────────────────────────────
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp)
            ) {
                item {
                    GlassChip(
                        label = "All Platforms",
                        selected = selectedPlatform == null && selectedStatus == null,
                        accentColor = MaterialTheme.colorScheme.primary,
                        onClick = {
                            selectedPlatform = null
                            selectedStatus = null
                        }
                    )
                }

                if (selectedMainTab == 0) {
                    item {
                        GlassChip(
                            label = "● Live Now",
                            selected = selectedStatus == ContestStatus.LIVE,
                            accentColor = Color(0xFF00F579),
                            onClick = {
                                selectedStatus = if (selectedStatus == ContestStatus.LIVE) null else ContestStatus.LIVE
                            }
                        )
                    }
                }

                items(Platform.values()) { platform ->
                    GlassChip(
                        label = platform.name.lowercase().replaceFirstChar { it.uppercase() },
                        selected = selectedPlatform == platform,
                        accentColor = platform.getBrandColor(),
                        onClick = {
                            selectedPlatform = if (selectedPlatform == platform) null else platform
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── CONTENT FEED BASED ON TAB ─────────────────────────────────────────
            if (selectedMainTab == 0) {
                if (contests.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        repeat(5) {
                            ContestCardSkeleton()
                        }
                    }
                } else if (filteredContests.isEmpty()) {
                    EmptyState(message = "No contests match your filters.")
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp)
                    ) {
                        items(filteredContests) { contest ->
                            ContestCard(
                                contest = contest,
                                onClick = { onContestClick(contest.id) }
                            )
                        }
                    }
                }
            } else {
                // MY PAST CONTESTS & RATING HISTORY TAB
                if (filteredPastContests.isEmpty()) {
                    // INTERACTIVE EMPTY STATE WITH "CONNECT PLATFORM HANDLES" CTA BUTTON
                    NoPlatformsConnectedCard(
                        onAddPlatformClick = onAddPlatformClick,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp)
                    ) {
                        items(filteredPastContests) { pastRecord ->
                            PastContestHistoryCard(
                                record = pastRecord,
                                onClick = { onPastContestClick(pastRecord.contestUrl) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── NO PLATFORMS CONNECTED EMPTY STATE CARD ───────────────────────────────────

@Composable
fun NoPlatformsConnectedCard(
    onAddPlatformClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        accentColor = MaterialTheme.colorScheme.primary,
        cornerRadius = 20.dp,
        elevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.40f)),
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Link,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No Platform Accounts Connected",
                style = Typography.titleMedium.copy(fontWeight = FontWeight.Black, fontSize = 18.sp),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Connect your Codeforces, LeetCode, CodeChef, or GitHub handle to automatically view your real contest history, rating changes, and solved problem counts.",
                style = Typography.bodySmall.copy(fontWeight = FontWeight.Normal),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onAddPlatformClick,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Connect Platform Handles",
                    style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

// ── PRIMARY TAB PILL TOGGLE ──────────────────────────────────────────────────

@Composable
fun PrimaryTabPill(
    label: String,
    selected: Boolean,
    badgeCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.1f
    val accentColor = MaterialTheme.colorScheme.primary

    val bgColor = if (selected) accentColor.copy(alpha = if (isDark) 0.22f else 0.12f)
    else (if (isDark) Color(0x1AFFFFFF) else Color(0x70FFFFFF))

    val borderColor = if (selected) accentColor.copy(alpha = if (isDark) 0.60f else 0.45f)
    else (if (isDark) Color(0x33FFFFFF) else Color(0x99FFFFFF))

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(14.dp))
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                style = Typography.labelMedium.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 12.5.sp
                ),
                color = if (selected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (badgeCount > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (selected) accentColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = badgeCount.toString(),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 10.sp),
                        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ── PAST CONTEST HISTORY CARD ────────────────────────────────────────────────

@Composable
fun PastContestHistoryCard(
    record: PastContestRecord,
    onClick: () -> Unit
) {
    val brandColor = record.platform.getBrandColor()
    val isPositive = record.ratingDelta >= 0
    val deltaColor = if (isPositive) Color(0xFF22C55E) else Color(0xFFF43F5E)

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        accentColor = brandColor,
        cornerRadius = 16.dp,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PlatformBadge(platform = record.platform)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = record.dateText,
                        style = Typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = deltaColor.copy(alpha = 0.14f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, deltaColor.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isPositive) Icons.AutoMirrored.Rounded.TrendingUp else Icons.AutoMirrored.Rounded.TrendingDown,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = deltaColor
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${record.newRating} (${if (isPositive) "+" else ""}${record.ratingDelta})",
                            style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = deltaColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = record.contestName,
                style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Problems Solved",
                            style = Typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "${record.solvedCount} / ${record.totalProblems}",
                            style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    val pct = (record.solvedCount.toFloat() / record.totalProblems.coerceAtLeast(1)).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { pct },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = brandColor,
                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(0.1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                ) {
                    Text(
                        text = record.rankText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun ContestCard(
    contest: Contest,
    onClick: () -> Unit
) {
    val brandColor = contest.platform.getBrandColor()
    val activeColor = if (contest.status == ContestStatus.LIVE) Color(0xFF22C55E) else brandColor
    val timeUntilStart = remember(contest.startTimeUtc) {
        Duration.between(Instant.now(), contest.startTimeUtc).seconds.coerceAtLeast(0)
    }
    val timeLabel = when {
        contest.status == ContestStatus.LIVE -> "Live now"
        contest.status == ContestStatus.ENDED -> "Ended"
        timeUntilStart < 3600 -> "in ${timeUntilStart / 60}m"
        timeUntilStart < 86400 -> "in ${timeUntilStart / 3600}h ${(timeUntilStart % 3600) / 60}m"
        else -> "in ${timeUntilStart / 86400}d"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(0.1.dp, activeColor.copy(alpha = 0.40f), RoundedCornerShape(16.dp))
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            accentColor = activeColor,
            cornerRadius = 16.dp,
            onClick = onClick
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(
                            color = activeColor,
                            shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                        )
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PlatformBadge(platform = contest.platform)
                        StatusChip(status = contest.status)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = contest.name,
                        style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${contest.durationSeconds / 3600}h ${(contest.durationSeconds % 3600) / 60}m",
                            style = Typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Text(
                            text = timeLabel,
                            style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = activeColor
                        )
                    }
                }
            }
        }
    }
}

private val samplePastContests = listOf(
    PastContestRecord(
        id = "past-cf-920",
        platform = Platform.CODEFORCES,
        contestName = "Codeforces Round 920 (Div. 2)",
        dateText = "3 days ago",
        oldRating = 1684,
        newRating = 1738,
        ratingDelta = 54,
        solvedCount = 4,
        totalProblems = 5,
        rankText = "Rank #1,240 / 18,500",
        contestUrl = "https://codeforces.com/contest/1921"
    ),
    PastContestRecord(
        id = "past-lc-384",
        platform = Platform.LEETCODE,
        contestName = "LeetCode Weekly Contest 384",
        dateText = "5 days ago",
        oldRating = 1810,
        newRating = 1845,
        ratingDelta = 35,
        solvedCount = 3,
        totalProblems = 4,
        rankText = "Rank #890 / 22,000",
        contestUrl = "https://leetcode.com/contest/weekly-contest-384/"
    )
)
