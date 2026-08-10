package com.mycodecalendar.feature.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mycodecalendar.core.designsystem.Typography
import com.mycodecalendar.core.designsystem.components.*
import com.mycodecalendar.domain.model.Contest
import com.mycodecalendar.domain.model.ContestStatus
import com.mycodecalendar.domain.model.GitHubStats
import com.mycodecalendar.domain.model.Platform
import com.mycodecalendar.domain.model.PlatformStats
import com.mycodecalendar.domain.model.Resource
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.Instant

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onAddPlatformClick: () -> Unit,
    onPlatformClick: (Platform) -> Unit,
    onContestClick: (String) -> Unit,
    onViewAllContestsClick: () -> Unit,
    onResourceClick: (String) -> Unit,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    val refreshAngle by rememberInfiniteTransition(label = "refresh").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotateRefresh"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
    ) {
        // Subtle top gradient wash
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(modifier = Modifier.offset(y = (-160).dp)) {
            // ── APP HEADER ─────────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MyCodeCalendar",
                        style = Typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Your competitive coding dashboard",
                        style = Typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Refresh icon button
                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Refresh",
                            modifier = Modifier
                                .size(20.dp)
                                .rotate(if (isRefreshing) refreshAngle else 0f),
                            tint = if (isRefreshing) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }

                    // Streak indicator
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.LocalFireDepartment,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFFF59E0B)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "${uiState.userStreak} days",
                                style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // Refreshing progress bar
            if (isRefreshing) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.Transparent
                )
            }

            // ── NEXT CONTEST HERO ───────────────────────────────────────────────────
            uiState.nextContest?.let { contest ->
                Spacer(modifier = Modifier.height(16.dp))
                HeroContestCard(
                    contest = contest,
                    onClick = { onContestClick(contest.id) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── GITHUB STATS ────────────────────────────────────────────────────────
            uiState.gitHubStats?.let { gh ->
                SectionHeader(
                    title = "Developer Activity",
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                GitHubStatsCard(
                    stats = gh,
                    onClick = { onPlatformClick(Platform.GITHUB) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(28.dp))
            }

            // ── PLATFORM RATINGS ────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader(title = "Platform Ratings")
                TextButton(onClick = onAddPlatformClick) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Connect",
                        style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.connectedStats.isEmpty()) {
                EmptyState(
                    message = "No platforms connected yet. Tap Connect to get started.",
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(uiState.connectedStats) { stat ->
                        PlatformRatingCard(
                            stat = stat,
                            onClick = { onPlatformClick(stat.platform) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── UPCOMING CONTESTS ───────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader(title = "Upcoming Contests")
                TextButton(onClick = onViewAllContestsClick) {
                    Text(
                        text = "View all",
                        style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.upcomingContests.isEmpty()) {
                EmptyState(
                    message = "No upcoming contests scheduled.",
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    uiState.upcomingContests.take(3).forEach { contest ->
                        UpcomingContestRow(
                            contest = contest,
                            onClick = { onContestClick(contest.id) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── FEATURED RESOURCE ───────────────────────────────────────────────────
            uiState.featuredResource?.let { resource ->
                SectionHeader(
                    title = "Study Resource",
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                FeaturedResourceCard(
                    resource = resource,
                    onClick = { onResourceClick(resource.url) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Last updated footer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                LastUpdatedLabel(timeAgo = uiState.lastUpdatedText)
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

// ── HERO CONTEST COUNTDOWN CARD ──────────────────────────────────────────────

@Composable
fun HeroContestCard(
    contest: Contest,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var remainingSeconds by remember(contest.startTimeUtc) {
        mutableStateOf(
            Duration.between(Instant.now(), contest.startTimeUtc).seconds.coerceAtLeast(0)
        )
    }

    LaunchedEffect(contest.startTimeUtc) {
        while (remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds = Duration.between(Instant.now(), contest.startTimeUtc).seconds.coerceAtLeast(0)
        }
    }

    val h = remainingSeconds / 3600
    val m = (remainingSeconds % 3600) / 60
    val s = remainingSeconds % 60
    val countdown = "%02d:%02d:%02d".format(h, m, s)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PlatformBadge(platform = contest.platform)
                    StatusChip(status = contest.status)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = contest.name,
                    style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "STARTS IN",
                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White.copy(alpha = 0.65f)
                )
                Text(
                    text = countdown,
                    style = Typography.headlineLarge.copy(fontWeight = FontWeight.Black),
                    color = Color.White
                )
            }
        }
    }
}

// ── GITHUB STATS CARD ────────────────────────────────────────────────────────

@Composable
fun GitHubStatsCard(
    stats: GitHubStats,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PlatformBadge(platform = Platform.GITHUB)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = stats.name ?: stats.username,
                            style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "@${stats.username}",
                            style = Typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, Color(0xFF10B981).copy(alpha = 0.3f)
                    )
                ) {
                    Text(
                        text = "${stats.currentContributionStreak}d streak",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF10B981)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                GitHubStatItem("Contributions", "${stats.totalContributionsThisYear}")
                GitHubStatItem("Stars", "${stats.totalStars}")
                GitHubStatItem("Repos", "${stats.publicRepos}")
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Top:",
                    style = Typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
                stats.topLanguages.take(4).forEach { lang ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    ) {
                        Text(
                            text = lang,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = Typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GitHubStatItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = Typography.labelSmall,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
        )
        Text(
            text = value,
            style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ── PLATFORM RATING CARD ─────────────────────────────────────────────────────

@Composable
fun PlatformRatingCard(
    stat: PlatformStats,
    onClick: () -> Unit
) {
    val brandColor = stat.platform.getBrandColor()

    Card(
        modifier = Modifier
            .width(155.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Left brand color strip
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(
                        color = brandColor,
                        shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                    )
            )
            Column(modifier = Modifier.padding(12.dp)) {
                PlatformBadge(platform = stat.platform)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stat.rating?.toString() ?: "—",
                    style = Typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stat.rank ?: "Unrated",
                    style = Typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stat.globalRank?.let { "Rank #$it" } ?: "—",
                    style = Typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
                )
            }
        }
    }
}

// ── UPCOMING CONTEST ROW ─────────────────────────────────────────────────────

@Composable
fun UpcomingContestRow(
    contest: Contest,
    onClick: () -> Unit
) {
    val brandColor = contest.platform.getBrandColor()
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(
                        color = brandColor,
                        shape = RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp)
                    )
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    PlatformBadge(platform = contest.platform)
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = contest.name,
                        style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(horizontalAlignment = Alignment.End) {
                    StatusChip(status = contest.status)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = timeLabel,
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = if (contest.status == ContestStatus.LIVE) Color(0xFF10B981)
                        else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// ── FEATURED RESOURCE CARD ───────────────────────────────────────────────────

@Composable
fun FeaturedResourceCard(
    resource: Resource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = resource.category,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                resource.duration?.let { dur ->
                    Text(
                        text = dur,
                        style = Typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = resource.title,
                style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            resource.description?.let { desc ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = desc,
                    style = Typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = resource.creator ?: "Community",
                style = Typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
