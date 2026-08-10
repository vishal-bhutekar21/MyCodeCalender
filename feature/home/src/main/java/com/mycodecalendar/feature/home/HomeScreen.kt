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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mycodecalendar.core.designsystem.Typography
import com.mycodecalendar.core.designsystem.components.*
import com.mycodecalendar.domain.model.*
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
    val refreshAngle by rememberInfiniteTransition(label = "spin").animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(800, easing = LinearEasing), RepeatMode.Restart),
        label = "rotate"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // ── HEADER ───────────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "MyCodeCalendar",
                    style = Typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Your competitive dashboard",
                    style = Typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Streak pill
                if (uiState.userStreak > 0) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Rounded.LocalFireDepartment,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${uiState.userStreak}d",
                                style = Typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Refresh
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier
                        .size(38.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                ) {
                    Icon(
                        Icons.Rounded.Refresh,
                        contentDescription = "Refresh",
                        modifier = Modifier
                            .size(18.dp)
                            .rotate(if (isRefreshing) refreshAngle else 0f),
                        tint = if (isRefreshing) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (isRefreshing) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().height(2.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }

        // ── NEXT CONTEST CARD ─────────────────────────────────────────────────
        uiState.nextContest?.let { contest ->
            Spacer(modifier = Modifier.height(4.dp))
            NextContestCard(
                contest = contest,
                onClick = { onContestClick(contest.id) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ── PLATFORM RATINGS ──────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionHeader(title = "Platform Ratings")
            TextButton(onClick = onAddPlatformClick) {
                Icon(Icons.Rounded.Add, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(3.dp))
                Text("Connect", style = Typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.connectedStats.isEmpty()) {
            EmptyState(
                message = "No platforms connected. Tap Connect to add your handles.",
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(uiState.connectedStats) { stat ->
                    PlatformRatingCard(stat = stat, onClick = { onPlatformClick(stat.platform) })
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ── GITHUB ACTIVITY ───────────────────────────────────────────────────
        uiState.gitHubStats?.let { gh ->
            SectionHeader(title = "GitHub Activity", modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(modifier = Modifier.height(8.dp))
            GitHubActivityCard(
                stats = gh,
                onClick = { onPlatformClick(Platform.GITHUB) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(28.dp))
        }

        // ── UPCOMING CONTESTS ─────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionHeader(title = "Upcoming Contests")
            TextButton(onClick = onViewAllContestsClick) {
                Text("View all", style = Typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.primary)
                Icon(Icons.Rounded.ChevronRight, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.upcomingContests.isEmpty()) {
            EmptyState(message = "No upcoming contests.", modifier = Modifier.padding(horizontal = 16.dp))
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                uiState.upcomingContests.take(4).forEach { contest ->
                    UpcomingContestRow(
                        contest = contest,
                        onClick = { onContestClick(contest.id) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ── FEATURED RESOURCE ─────────────────────────────────────────────────
        uiState.featuredResource?.let { resource ->
            SectionHeader(title = "Study Resource", modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(modifier = Modifier.height(8.dp))
            FeaturedResourceCard(
                resource = resource,
                onClick = { onResourceClick(resource.url) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        // ── LAST UPDATED ──────────────────────────────────────────────────────
        Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp), Alignment.Center) {
            LastUpdatedLabel(timeAgo = uiState.lastUpdatedText)
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

// ── NEXT CONTEST CARD — clean surface card, no gradient ──────────────────────

@Composable
fun NextContestCard(contest: Contest, onClick: () -> Unit, modifier: Modifier = Modifier) {
    var remaining by remember(contest.startTimeUtc) {
        mutableStateOf(Duration.between(Instant.now(), contest.startTimeUtc).seconds.coerceAtLeast(0))
    }
    LaunchedEffect(contest.startTimeUtc) {
        while (remaining > 0) { delay(1000); remaining = Duration.between(Instant.now(), contest.startTimeUtc).seconds.coerceAtLeast(0) }
    }
    val h = remaining / 3600
    val m = (remaining % 3600) / 60
    val s = remaining % 60
    val countdown = "%02d:%02d:%02d".format(h, m, s)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlatformBadge(platform = contest.platform)
                StatusChip(status = contest.status)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = contest.name,
                style = Typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(16.dp))
            Text("STARTS IN", style = Typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(countdown, style = Typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
        }
    }
}

// ── GITHUB ACTIVITY CARD ─────────────────────────────────────────────────────

@Composable
fun GitHubActivityCard(stats: GitHubStats, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PlatformBadge(platform = Platform.GITHUB)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(stats.name ?: stats.username, style = Typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
                        Text("@${stats.username}", style = Typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                    Text(
                        "${stats.currentContributionStreak}d streak",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                GHStat("Contributions", stats.totalContributionsThisYear.toString())
                GHStat("Stars", stats.totalStars.toString())
                GHStat("Repos", stats.publicRepos.toString())
            }
        }
    }
}

@Composable
private fun GHStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
        Text(label, style = Typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ── PLATFORM RATING CARD ─────────────────────────────────────────────────────

@Composable
fun PlatformRatingCard(stat: PlatformStats, onClick: () -> Unit) {
    val brandColor = stat.platform.getBrandColor()
    Card(
        modifier = Modifier
            .width(150.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            PlatformBadge(platform = stat.platform)
            Spacer(Modifier.height(12.dp))
            Text(
                text = stat.rating?.toString() ?: "—",
                style = Typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stat.rank ?: "Unrated",
                style = Typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "@${stat.username}",
                style = Typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ── UPCOMING CONTEST ROW ──────────────────────────────────────────────────────

@Composable
fun UpcomingContestRow(contest: Contest, onClick: () -> Unit) {
    val timeUntil = remember(contest.startTimeUtc) {
        Duration.between(Instant.now(), contest.startTimeUtc).seconds.coerceAtLeast(0)
    }
    val timeLabel = when {
        contest.status == ContestStatus.LIVE  -> "Live now"
        contest.status == ContestStatus.ENDED -> "Ended"
        timeUntil < 3600   -> "in ${timeUntil / 60}m"
        timeUntil < 86400  -> "in ${timeUntil / 3600}h ${(timeUntil % 3600) / 60}m"
        else               -> "in ${timeUntil / 86400}d"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                PlatformBadge(platform = contest.platform)
                Spacer(Modifier.height(4.dp))
                Text(
                    contest.name,
                    style = Typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(horizontalAlignment = Alignment.End) {
                StatusChip(status = contest.status)
                Spacer(Modifier.height(3.dp))
                Text(
                    timeLabel,
                    style = Typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = if (contest.status == ContestStatus.LIVE) MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// ── FEATURED RESOURCE CARD ─────────────────────────────────────────────────────

@Composable
fun FeaturedResourceCard(resource: Resource, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                    Text(
                        resource.category,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                resource.duration?.let { Text(it, style = Typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            Spacer(Modifier.height(10.dp))
            Text(resource.title, style = Typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
            resource.description?.let {
                Spacer(Modifier.height(4.dp))
                Text(it, style = Typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.height(10.dp))
            Text(resource.creator ?: "Community", style = Typography.labelSmall.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.primary)
        }
    }
}
