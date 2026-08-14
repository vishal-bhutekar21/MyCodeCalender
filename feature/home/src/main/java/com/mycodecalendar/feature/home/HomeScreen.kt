package com.mycodecalendar.feature.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.rounded.ArrowOutward
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.mycodecalendar.core.designsystem.BrandGitHub
import com.mycodecalendar.core.designsystem.CountdownNormal
import com.mycodecalendar.core.designsystem.CountdownUrgent
import com.mycodecalendar.core.designsystem.GlassmorphismBackground
import com.mycodecalendar.core.designsystem.Typography
import com.mycodecalendar.core.designsystem.components.*
import com.mycodecalendar.domain.model.*
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.Instant
import java.time.LocalTime

/**
 * HomeScreen — Primary dashboard of MyCodeCalendar.
 *
 * Daily App Open Streak System:
 * - Automatically tracks consecutive daily app opens in SharedPreferences.
 * - Increments streak by +1 once a day when opening the app.
 * - Maintains streak count when opened multiple times on the same day.
 * - Resets to 1 if a day is missed.
 * - Shows an animated [StreakCelebrationModal] dialog popup on new day app open or when tapping the streak flame badge.
 */
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onAddPlatformClick: () -> Unit,
    onPlatformClick: (Platform) -> Unit,
    onContestClick: (String) -> Unit,
    onViewAllContestsClick: () -> Unit,
    onResourceClick: (String) -> Unit,
    onStreakClick: () -> Unit = {},
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {}
) {
    var showStreakModal by remember { mutableStateOf(false) }
    // Session-scoped flag: once shown in this process, never auto-show again
    var hasShownStreakModal by remember { mutableStateOf(false) }

    // Auto-show streak modal ONLY on a genuine new-day increment, and only once per session
    LaunchedEffect(uiState.streakInfo?.isNewDayIncrement) {
        if (uiState.streakInfo?.isNewDayIncrement == true && !hasShownStreakModal) {
            hasShownStreakModal = true
            showStreakModal = true
        }
    }

    val refreshAngle by rememberInfiniteTransition(label = "spin").animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(800, easing = LinearEasing), RepeatMode.Restart),
        label = "rotate"
    )

    val greeting = remember {
        val hour = LocalTime.now().hour
        when {
            hour in 5..11 -> "Good Morning"
            hour in 12..16 -> "Good Afternoon"
            hour in 17..21 -> "Good Evening"
            else -> "Happy Coding"
        }
    }

    GlassmorphismBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ── HEADER ───────────────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 20.dp, bottom = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = greeting,
                        style = Typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f)
                    )
                    Text(
                        text = "Code Calendar",
                        style = Typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp,
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF818CF8), // Electric Indigo
                                    Color(0xFFA78BFA), // Lavender Violet
                                    Color(0xFF38BDF8)  // Sky Cyan
                                )
                            )
                        )
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Daily App Login Streak Pill
                    val fireScale by rememberInfiniteTransition(label = "firePulse").animateFloat(
                        initialValue = 0.92f,
                        targetValue = 1.15f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(700, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "fireScale"
                    )

                    GlassCard(
                        cornerRadius = 20.dp,
                        accentColor = Color(0xFFF59E0B),
                        onClick = onStreakClick
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Rounded.LocalFireDepartment,
                                contentDescription = "Daily Streak",
                                modifier = Modifier
                                    .size(17.dp)
                                    .scale(fireScale),
                                tint = Color(0xFFF59E0B)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${uiState.userStreak}d streak",
                                style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Refresh FAB
                    GlassCard(
                        cornerRadius = 20.dp,
                        onClick = onRefresh
                    ) {
                        Box(
                            modifier = Modifier.size(40.dp),
                            contentAlignment = Alignment.Center
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
            }

            AnimatedVisibility(
                visible = isRefreshing,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    cornerRadius = 14.dp,
                    accentColor = MaterialTheme.colorScheme.primary
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CyberLoadingSpinner(
                            size = 20.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Syncing live contest radar & handles…",
                            style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            if (uiState.isOffline) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    accentColor = Color(0xFFF59E0B),
                    cornerRadius = 12.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚡ Offline Mode — Showing saved cache from device",
                            style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFF59E0B)
                        )
                    }
                }
            }

            if (uiState.isLoading || (uiState.connectedStats.isEmpty() && uiState.upcomingContests.isEmpty() && uiState.nextContest == null && isRefreshing)) {
                HomeScreenSkeleton()
            } else {
                // ── HERO SPOTLIGHT: NEXT CONTEST CARD ────────────────────────────────
                uiState.nextContest?.let { contest ->
                    NextContestHeroCard(
                        contest = contest,
                        onClick = { onContestClick(contest.id) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(26.dp))

                // ── PLATFORM RATINGS & ACCOUNTS ──────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader(
                        title = "Connected Ratings",
                        trailingContent = {
                            TextButton(
                                onClick = onAddPlatformClick,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Icon(Icons.Rounded.Add, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "Connect",
                                    style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (uiState.connectedStats.isEmpty()) {
                    EmptyState(
                        message = "No platform handles connected yet. Tap Connect to add Codeforces, LeetCode, GitHub, or CodeChef.",
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

                Spacer(modifier = Modifier.height(26.dp))

                // ── OFFICIAL 2D GITHUB CONTRIBUTION HEATMAP GRID ────────────────────
                uiState.gitHubStats?.let { gh ->
                    SectionHeader(title = "GitHub Activity", modifier = Modifier.padding(horizontal = 20.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    GitHubActivityCard(
                        stats = gh,
                        onClick = { onPlatformClick(Platform.GITHUB) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(26.dp))
                }

                // ── UPCOMING CONTESTS STREAM ─────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader(
                        title = "Upcoming Contests",
                        trailingContent = {
                            SeeAllButton(onClick = onViewAllContestsClick)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (uiState.upcomingContests.isEmpty()) {
                    EmptyState(message = "No upcoming contests found.", modifier = Modifier.padding(horizontal = 16.dp))
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
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

                Spacer(modifier = Modifier.height(26.dp))

                // ── FEATURED STUDY RESOURCE ──────────────────────────────────────────
                uiState.featuredResource?.let { resource ->
                    SectionHeader(title = "Featured Resource", modifier = Modifier.padding(horizontal = 20.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    FeaturedResourceCard(
                        resource = resource,
                        onClick = { onResourceClick(resource.url) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // ── LAST UPDATED TIMESTAMP ────────────────────────────────────────────
                Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp), Alignment.Center) {
                    LastUpdatedLabel(timeAgo = uiState.lastUpdatedText)
                }

                Spacer(modifier = Modifier.height(110.dp))
            }
        }

        // ── ANIMATED DAILY STREAK CELEBRATION MODAL ───────────────────────────
        if (showStreakModal) {
            StreakCelebrationModal(
                streakDays = uiState.userStreak,
                dateText = uiState.streakInfo?.lastOpenDateText ?: "Today",
                onDismiss = { showStreakModal = false }
            )
        }
    }
}

// ── HERO SPOTLIGHT NEXT CONTEST CARD ──────────────────────────────────────────

@Composable
fun NextContestHeroCard(
    contest: Contest,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var remaining by remember(contest.startTimeUtc) {
        mutableStateOf(Duration.between(Instant.now(), contest.startTimeUtc).seconds.coerceAtLeast(0))
    }
    LaunchedEffect(contest.startTimeUtc) {
        while (remaining > 0) {
            delay(1000)
            remaining = Duration.between(Instant.now(), contest.startTimeUtc).seconds.coerceAtLeast(0)
        }
    }
    val h = remaining / 3600
    val m = (remaining % 3600) / 60
    val s = remaining % 60
    val countdown = "%02d:%02d:%02d".format(h, m, s)
    val isUrgent = remaining < 3600 && contest.status == ContestStatus.UPCOMING
    val brandColor = contest.platform.getBrandColor()

    val urgentAlpha by rememberInfiniteTransition(label = "urgentPulse").animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(600, easing = LinearEasing), RepeatMode.Reverse),
        label = "urgentAlpha"
    )

    GlassCard(
        modifier = modifier,
        accentColor = brandColor,
        cornerRadius = 20.dp,
        elevation = 6.dp,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlatformBadge(platform = contest.platform)
                StatusChip(status = contest.status)
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = contest.name,
                style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = if (contest.status == ContestStatus.LIVE) "CONTEST IS LIVE" else "STARTS IN",
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = if (contest.status == ContestStatus.LIVE) "Live Now" else countdown,
                        style = Typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 32.sp
                        ),
                        color = if (isUrgent) CountdownUrgent.copy(alpha = urgentAlpha)
                        else if (contest.status == ContestStatus.LIVE) Color(0xFF22C55E)
                        else CountdownNormal
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = brandColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, brandColor.copy(alpha = 0.40f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "View Details",
                            style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = brandColor
                        )
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = brandColor
                        )
                    }
                }
            }
        }
    }
}

// ── OFFICIAL 2D GITHUB CONTRIBUTION HEATMAP GRID ─────────────────────────────

@Composable
fun GitHubActivityCard(stats: GitHubStats, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val brandColor = BrandGitHub

    val weeks = remember(stats.dailyContributions) {
        stats.dailyContributions.chunked(7)
    }

    GlassCard(
        modifier = modifier,
        accentColor = brandColor,
        cornerRadius = 18.dp,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PlatformBadge(platform = Platform.GITHUB)
                    Spacer(Modifier.width(10.dp))
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
                    color = brandColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, brandColor.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = "${stats.currentContributionStreak}d streak",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = brandColor
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Contribution Graph",
                    style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${stats.totalContributionsThisYear} contributions",
                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = brandColor
                )
            }

            Spacer(Modifier.height(10.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(weeks) { week ->
                    Column(
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        week.forEach { contrib ->
                            val heatColor = when (contrib.level) {
                                4 -> Color(0xFF39D353)
                                3 -> Color(0xFF26A641)
                                2 -> Color(0xFF006D32)
                                1 -> Color(0xFF0E4429)
                                else -> Color(0xFF161B22)
                            }
                            Box(
                                modifier = Modifier
                                    .size(11.dp)
                                    .background(heatColor, RoundedCornerShape(2.dp))
                                    .border(0.5.dp, Color(0x1AFFFFFF), RoundedCornerShape(2.dp))
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Stars: ${stats.totalStars}", style = Typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    Text("Repos: ${stats.publicRepos}", style = Typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text("Less", style = Typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    listOf(
                        Color(0xFF161B22),
                        Color(0xFF0E4429),
                        Color(0xFF006D32),
                        Color(0xFF26A641),
                        Color(0xFF39D353)
                    ).forEach { col ->
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .background(col, RoundedCornerShape(2.dp))
                        )
                    }
                    Text("More", style = Typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                }
            }
        }
    }
}

// ── PLATFORM RATING CARD ─────────────────────────────────────────────────────

@Composable
fun PlatformRatingCard(stat: PlatformStats, onClick: () -> Unit) {
    val brandColor = stat.platform.getBrandColor()

    GlassCard(
        modifier = Modifier.width(154.dp),
        accentColor = brandColor,
        cornerRadius = 16.dp,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            PlatformBadge(platform = stat.platform)
            Spacer(Modifier.height(12.dp))
            Text(
                text = stat.rating?.toString() ?: "—",
                style = Typography.headlineLarge.copy(fontWeight = FontWeight.Black),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stat.rank ?: "Unrated",
                style = Typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "@${stat.username}",
                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = brandColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ── UPCOMING CONTEST ROW ──────────────────────────────────────────────────────

@Composable
fun UpcomingContestRow(contest: Contest, onClick: () -> Unit) {
    val brandColor = contest.platform.getBrandColor()
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

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        accentColor = if (contest.status == ContestStatus.LIVE) Color(0xFF22C55E) else null,
        cornerRadius = 14.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                PlatformBadge(platform = contest.platform)
                Spacer(Modifier.height(6.dp))
                Text(
                    contest.name,
                    style = Typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(horizontalAlignment = Alignment.End) {
                StatusChip(status = contest.status)
                Spacer(Modifier.height(4.dp))
                Text(
                    timeLabel,
                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (contest.status == ContestStatus.LIVE) Color(0xFF22C55E)
                    else brandColor
                )
            }
        }
    }
}

// ── FEATURED RESOURCE CARD ─────────────────────────────────────────────────────

@Composable
fun FeaturedResourceCard(resource: Resource, onClick: () -> Unit, modifier: Modifier = Modifier) {
    GlassCard(
        modifier = modifier,
        cornerRadius = 16.dp,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Text(
                        resource.category,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                resource.duration?.let { Text(it, style = Typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) }
            }
            Spacer(Modifier.height(10.dp))
            Text(resource.title, style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
            resource.description?.let {
                Spacer(Modifier.height(4.dp))
                Text(it, style = Typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "By ${resource.creator ?: "Community"}",
                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Open Link",
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = Icons.Rounded.ArrowOutward,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// ── ANIMATED DAILY STREAK CELEBRATION MODAL ───────────────────────────────────

@Composable
fun StreakCelebrationModal(
    streakDays: Int,
    dateText: String,
    onDismiss: () -> Unit
) {
    val fireScale by rememberInfiniteTransition(label = "streakFire").animateFloat(
        initialValue = 0.95f,
        targetValue = 1.22f,
        animationSpec = infiniteRepeatable(tween(650, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "fireScale"
    )

    Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            accentColor = Color(0xFFF59E0B),
            cornerRadius = 24.dp,
            elevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Animated Glowing Flame Icon
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFF59E0B).copy(alpha = 0.16f),
                    border = BorderStroke(1.5.dp, Color(0xFFF59E0B).copy(alpha = 0.50f)),
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.LocalFireDepartment,
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .scale(fireScale),
                            tint = Color(0xFFF59E0B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Day $streakDays Streak!",
                    style = Typography.headlineMedium.copy(fontWeight = FontWeight.Black, fontSize = 24.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Daily login recorded for $dateText. Keep checking in every day to keep your coding streak alive!",
                    style = Typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
                ) {
                    Text(
                        text = "Keep It Going 🔥",
                        style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = Color.Black)
                    )
                }
            }
        }
    }
}
