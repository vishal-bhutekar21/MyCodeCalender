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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.mycodecalendar.core.designsystem.BrandGitHub
import com.mycodecalendar.core.designsystem.BrandPrimaryOrange
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
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    userName: String? = null,
    isLoggedIn: Boolean = false,
    onAddPlatformClick: () -> Unit,
    onPlatformClick: (Platform) -> Unit,
    onContestClick: (String) -> Unit,
    onViewAllContestsClick: () -> Unit,
    onResourceClick: (String) -> Unit,
    onStreakClick: () -> Unit = {},
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showStreakModal by remember { mutableStateOf(false) }

    // Auto-show streak modal ONLY on a genuine new-day increment, and only once per calendar day
    LaunchedEffect(uiState.streakInfo?.isNewDayIncrement) {
        val prefs = context.getSharedPreferences("app_streak_prefs", android.content.Context.MODE_PRIVATE)
        val todayStr = java.time.LocalDate.now().toString()
        val lastShownDate = prefs.getString("last_shown_streak_date", null)

        if (uiState.streakInfo?.isNewDayIncrement == true && lastShownDate != todayStr) {
            prefs.edit().putString("last_shown_streak_date", todayStr).apply()
            showStreakModal = true
        }
    }

    val refreshAngle by rememberInfiniteTransition(label = "spin").animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(800, easing = LinearEasing), RepeatMode.Restart),
        label = "rotate"
    )

    val resolvedUserName = remember(userName, isLoggedIn) {
        if (isLoggedIn) {
            userName?.takeIf { it.isNotBlank() && it != "Guest Developer" && it != "Developer" && it != "Guest" }
        } else {
            null
        }
    }

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
                    .padding(top = 24.dp, bottom = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = greeting,
                        style = Typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp,
                            fontSize = 11.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    if (!resolvedUserName.isNullOrBlank()) {
                        Text(
                            text = resolvedUserName,
                            style = Typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 26.sp,
                                letterSpacing = (-0.5).sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "MyCode",
                                style = Typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 26.sp,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Calendar",
                                style = Typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 26.sp,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = BrandPrimaryOrange
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
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
                        cornerRadius = 24.dp,
                        accentColor = Color(0xFFF59E0B),
                        onClick = onStreakClick
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
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
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "${uiState.userStreak}d streak",
                                style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Refresh FAB
                    GlassCard(
                        cornerRadius = 24.dp,
                        onClick = onRefresh
                    ) {
                        Box(
                            modifier = Modifier.size(42.dp),
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

            // ── DYNAMIC LIVE CLOUD BROADCAST (Web Admin CMS) ───────────────────
            var cloudBroadcast by remember { mutableStateOf<Triple<String, String, String>?>(null) }
            var isBroadcastDismissed by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                try {
                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("broadcasts")
                        .whereEqualTo("isActive", true)
                        .limit(1)
                        .get()
                        .addOnSuccessListener { snapshot ->
                            val doc = snapshot.documents.firstOrNull()
                            if (doc != null) {
                                val title = doc.getString("title") ?: ""
                                val subtitle = doc.getString("subtitle") ?: ""
                                val actionUrl = doc.getString("actionUrl") ?: ""
                                if (title.isNotBlank()) {
                                    cloudBroadcast = Triple(title, subtitle, actionUrl)
                                }
                            }
                        }
                } catch (e: Exception) {
                    // Non-blocking
                }
            }

            if (cloudBroadcast != null && !isBroadcastDismissed) {
                val (bTitle, bSubtitle, bUrl) = cloudBroadcast!!
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    cornerRadius = 18.dp,
                    accentColor = BrandPrimaryOrange,
                    onClick = {
                        if (bUrl.isNotBlank()) onResourceClick(bUrl)
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(BrandPrimaryOrange.copy(alpha = 0.16f), CircleShape)
                                .border(0.1.dp, BrandPrimaryOrange.copy(alpha = 0.40f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.Notifications,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = BrandPrimaryOrange
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = BrandPrimaryOrange.copy(alpha = 0.20f)
                                ) {
                                    Text(
                                        text = "NOTICE",
                                        style = Typography.labelSmall.copy(fontSize = 8.5.sp, fontWeight = FontWeight.Black),
                                        color = BrandPrimaryOrange,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                                Text(
                                    text = bTitle,
                                    style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.5.sp),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (bSubtitle.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = bSubtitle,
                                    style = Typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        IconButton(
                            onClick = { isBroadcastDismissed = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Clear,
                                contentDescription = "Dismiss",
                                modifier = Modifier.size(15.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.50f)
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
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    cornerRadius = 16.dp,
                    accentColor = MaterialTheme.colorScheme.primary
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
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
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    accentColor = Color(0xFFF59E0B),
                    cornerRadius = 14.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 11.dp),
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
                // ── HERO SPOTLIGHT: SWIPEABLE LIVE & ONGOING CONTESTS ────────────────
                val highlightList = remember(uiState.highlightContests, uiState.nextContest) {
                    if (uiState.highlightContests.isNotEmpty()) uiState.highlightContests
                    else listOfNotNull(uiState.nextContest)
                }

                if (highlightList.isNotEmpty()) {
                    if (highlightList.size == 1) {
                        NextContestHeroCard(
                            contest = highlightList.first(),
                            onClick = { onContestClick(highlightList.first().id) },
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    } else {
                        val pagerState = rememberPagerState(pageCount = { highlightList.size })
                        Column(modifier = Modifier.fillMaxWidth()) {
                            HorizontalPager(
                                state = pagerState,
                                contentPadding = PaddingValues(horizontal = 20.dp),
                                pageSpacing = 14.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) { page ->
                                val contest = highlightList[page]
                                NextContestHeroCard(
                                    contest = contest,
                                    onClick = { onContestClick(contest.id) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Animated swipeable dot indicators
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(highlightList.size) { index ->
                                    val isSelected = pagerState.currentPage == index
                                    val dotWidth by animateDpAsState(
                                        targetValue = if (isSelected) 22.dp else 6.dp,
                                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                        label = "dotWidth"
                                    )
                                    val dotColor = if (isSelected) com.mycodecalendar.core.designsystem.BrandPrimaryOrange
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)

                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 3.dp)
                                            .height(5.dp)
                                            .width(dotWidth)
                                            .clip(CircleShape)
                                            .background(dotColor)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

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

                Spacer(modifier = Modifier.height(12.dp))

                if (uiState.connectedStats.isEmpty()) {
                    EmptyState(
                        message = "No platform handles connected yet. Tap Connect to add Codeforces, LeetCode, GitHub, or CodeChef.",
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp)
                    ) {
                        items(uiState.connectedStats) { stat ->
                            PlatformRatingCard(stat = stat, onClick = { onPlatformClick(stat.platform) })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // ── OFFICIAL 2D GITHUB CONTRIBUTION HEATMAP GRID ────────────────────
                uiState.gitHubStats?.let { gh ->
                    SectionHeader(title = "GitHub Activity", modifier = Modifier.padding(horizontal = 20.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    GitHubActivityCard(
                        stats = gh,
                        onClick = { onPlatformClick(Platform.GITHUB) },
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(modifier = Modifier.height(32.dp))
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

                Spacer(modifier = Modifier.height(12.dp))

                if (uiState.upcomingContests.isEmpty()) {
                    EmptyState(message = "No upcoming contests found.", modifier = Modifier.padding(horizontal = 20.dp))
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(horizontal = 20.dp)
                    ) {
                        uiState.upcomingContests.take(4).forEach { contest ->
                            UpcomingContestRow(
                                contest = contest,
                                onClick = { onContestClick(contest.id) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // ── FEATURED STUDY RESOURCE ──────────────────────────────────────────
                uiState.featuredResource?.let { resource ->
                    SectionHeader(title = "Featured Resource", modifier = Modifier.padding(horizontal = 20.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    FeaturedResourceCard(
                        resource = resource,
                        onClick = { onResourceClick(resource.url) },
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(modifier = Modifier.height(28.dp))
                }

                // ── LAST UPDATED TIMESTAMP ────────────────────────────────────────────
                Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp), Alignment.Center) {
                    LastUpdatedLabel(timeAgo = uiState.lastUpdatedText)
                }

                Spacer(modifier = Modifier.height(120.dp))
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
        elevation = 4.dp,
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
                style = Typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    lineHeight = 24.sp,
                    letterSpacing = (-0.2).sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (contest.status == ContestStatus.LIVE) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00F579))
                            )
                        }
                        Text(
                            text = if (contest.status == ContestStatus.LIVE) "ACTIVE ON PLATFORM" else "STARTS IN",
                            style = Typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                fontSize = if (contest.status == ContestStatus.LIVE) 11.5.sp else 10.sp
                            ),
                            color = if (contest.status == ContestStatus.LIVE) Color(0xFF00F579)
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                        )
                    }
                    if (contest.status != ContestStatus.LIVE) {
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = countdown,
                            style = Typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp,
                                letterSpacing = 0.5.sp
                            ),
                            color = if (isUrgent) CountdownUrgent.copy(alpha = urgentAlpha)
                            else CountdownNormal
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = brandColor.copy(alpha = 0.14f),
                    border = BorderStroke(0.1.dp, brandColor.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "View Contest",
                            style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
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

// ── OFFICIAL 2D GITHUB CONTRIBUTION HEATMAP & REPOSITORIES GRID ──────────────

@Composable
fun GitHubActivityCard(stats: GitHubStats, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val brandColor = BrandGitHub

    val weeks = remember(stats.dailyContributions) {
        stats.dailyContributions.chunked(7)
    }

    GlassCard(
        modifier = modifier,
        accentColor = brandColor,
        cornerRadius = 20.dp,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row
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
                            style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "@${stats.username}",
                            style = Typography.bodySmall.copy(fontSize = 11.5.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFF6B00).copy(alpha = 0.15f),
                    border = BorderStroke(0.1.dp, Color(0xFFFF6B00).copy(alpha = 0.40f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Rounded.LocalFireDepartment,
                            null,
                            modifier = Modifier.size(13.dp),
                            tint = Color(0xFFFF6B00)
                        )
                        Text(
                            text = "${stats.currentContributionStreak}d streak",
                            style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                            color = Color(0xFFFF6B00)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Contribution Graph Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Contribution Graph",
                    style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 12.5.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${stats.totalContributionsThisYear} commits this year",
                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                    color = Color(0xFF10B981)
                )
            }

            Spacer(Modifier.height(12.dp))

            // Heatmap Matrix
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(3.5.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                items(weeks) { week ->
                    Column(
                        verticalArrangement = Arrangement.spacedBy(3.5.dp)
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
                                    .border(0.1.dp, Color(0x1AFFFFFF), RoundedCornerShape(2.dp))
                            )
                        }
                    }
                }
            }

            // Top Public Repositories Carousel (if available)
            if (stats.repos.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Top Repositories",
                        style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${stats.repos.size} Repos",
                        style = Typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }

                Spacer(Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    items(stats.repos.take(6)) { repo ->
                        Box(
                            modifier = Modifier
                                .width(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f))
                                .border(0.1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = repo.name,
                                        style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Icon(
                                            Icons.Rounded.Star,
                                            null,
                                            modifier = Modifier.size(12.dp),
                                            tint = Color(0xFFFFB800)
                                        )
                                        Text(
                                            text = "${repo.stars}",
                                            style = Typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                repo.description?.let { desc ->
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = desc,
                                        style = Typography.bodySmall.copy(fontSize = 10.5.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        lineHeight = 14.sp
                                    )
                                }

                                Spacer(Modifier.height(6.dp))

                                repo.language?.let { lang ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .background(
                                                    when (lang.lowercase()) {
                                                        "kotlin" -> Color(0xFFA97BFF)
                                                        "c++" -> Color(0xFFF34B7D)
                                                        "java" -> Color(0xFFB07219)
                                                        "python" -> Color(0xFF3572A5)
                                                        else -> BrandGitHub
                                                    },
                                                    CircleShape
                                                )
                                        )
                                        Text(
                                            text = lang,
                                            style = Typography.labelSmall.copy(fontSize = 10.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Footer Quick Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⭐ ${stats.totalStars} Stars", style = Typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    Text("📁 ${stats.publicRepos} Repos", style = Typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    Text("🔥 Max ${stats.longestContributionStreak}d", style = Typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text("Less", style = Typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f))
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
                    Text("More", style = Typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f))
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
        modifier = Modifier.width(160.dp),
        accentColor = brandColor,
        cornerRadius = 18.dp,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            PlatformBadge(platform = stat.platform)
            Spacer(Modifier.height(14.dp))
            Text(
                text = stat.rating?.toString() ?: "—",
                style = Typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 26.sp,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = stat.rank ?: "Unrated",
                style = Typography.labelSmall.copy(fontWeight = FontWeight.Medium, fontSize = 11.5.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "@${stat.username}",
                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
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

    val activeColor = if (contest.status == ContestStatus.LIVE) Color(0xFF22C55E) else brandColor
    val durationHours = contest.durationSeconds / 3600
    val durationMins = (contest.durationSeconds % 3600) / 60
    val durationText = if (durationHours > 0) "${durationHours}h${if (durationMins > 0) " ${durationMins}m" else ""}" else "${durationMins}m"

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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PlatformBadge(platform = contest.platform)
                        if (contest.durationSeconds > 0) {
                            Text(
                                text = "· $durationText",
                                style = Typography.labelSmall.copy(fontSize = 11.5.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = contest.name,
                        style = Typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            lineHeight = 19.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.width(14.dp))

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center
                ) {
                    StatusChip(status = contest.status)
                    Spacer(Modifier.height(5.dp))
                    Text(
                        text = timeLabel,
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                        color = activeColor
                    )
                }
            }
        }
    }
}

// ── FEATURED RESOURCE CARD ─────────────────────────────────────────────────────

@Composable
fun FeaturedResourceCard(resource: Resource, onClick: () -> Unit, modifier: Modifier = Modifier) {
    GlassCard(
        modifier = modifier,
        cornerRadius = 18.dp,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    border = BorderStroke(0.1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                ) {
                    Text(
                        resource.category,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.5.dp),
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                resource.duration?.let { Text(it, style = Typography.labelSmall.copy(fontSize = 11.sp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                resource.title,
                style = Typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    lineHeight = 22.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            resource.description?.let {
                Spacer(Modifier.height(6.dp))
                Text(
                    it,
                    style = Typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 17.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "By ${resource.creator ?: "Community"}",
                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.5.sp),
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Open Link",
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.5.sp),
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
        initialValue = 0.94f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "fireScale"
    )

    val coderRank = remember(streakDays) {
        com.mycodecalendar.domain.model.BadgeHelper.getCoderRank(streakDays)
    }

    Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            accentColor = BrandPrimaryOrange,
            cornerRadius = 26.dp,
            elevation = 20.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 3D Illuminated Sphere
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .drawBehind {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    listOf(BrandPrimaryOrange.copy(alpha = 0.45f), Color.Transparent),
                                    center = center,
                                    radius = size.width * 0.8f
                                )
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(BrandPrimaryOrange, Color(0xFFFF8C00))
                                )
                            )
                            .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocalFireDepartment,
                            contentDescription = null,
                            modifier = Modifier
                                .size(42.dp)
                                .scale(fireScale),
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Coder Rank Pill
                Surface(
                    shape = CircleShape,
                    color = BrandPrimaryOrange.copy(alpha = 0.15f),
                    border = BorderStroke(0.1.dp, BrandPrimaryOrange.copy(alpha = 0.45f))
                ) {
                    Text(
                        text = coderRank.uppercase(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = Typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            fontSize = 10.sp
                        ),
                        color = BrandPrimaryOrange
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Day $streakDays Streak!",
                    style = Typography.headlineSmall.copy(fontWeight = FontWeight.Black, fontSize = 22.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Daily habit active. Keep the fire burning! 🔥",
                    style = Typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.80f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandPrimaryOrange,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Awesome 🔥",
                        style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }
    }
}
