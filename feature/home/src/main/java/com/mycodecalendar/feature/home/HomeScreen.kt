package com.mycodecalendar.feature.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
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
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.luminance
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

data class CloudBroadcastBanner(
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val actionUrl: String = "",
    val badge: String = "NOTICE",
    val bannerImageUrl: String = "",
    val description: String = "",
    val prizePool: String = "",
    val location: String = "",
    val teamSize: String = "",
    val timeline: String = "",
    val tags: List<String> = emptyList()
)

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
    onNotificationClick: (CloudBroadcastBanner) -> Unit = {},
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    onViewAllNotificationsClick: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showStreakModal by remember { mutableStateOf(false) }
    var isBroadcastLoading by remember { mutableStateOf(true) }
    var cloudBroadcast by remember {
        mutableStateOf<CloudBroadcastBanner?>(null)
    }

    // Persisted dismissed broadcast IDs specifically for HomeScreen
    val homePrefs = remember { context.getSharedPreferences("app_home_prefs", android.content.Context.MODE_PRIVATE) }
    var dismissedHomeBroadcastIds by remember {
        mutableStateOf(
            homePrefs.getStringSet("dismissed_broadcast_ids", emptySet()) ?: emptySet()
        )
    }

    val isCurrentBroadcastDismissedOnHome = cloudBroadcast != null && dismissedHomeBroadcastIds.contains(cloudBroadcast?.id)
    val hasUnreadBroadcast = cloudBroadcast != null && !isCurrentBroadcastDismissedOnHome

    // Real-time Firestore snapshot listener for cloud broadcasts
    DisposableEffect(Unit) {
        val listener = try {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("broadcasts")
                .whereEqualTo("isActive", true)
                .limit(1)
                .addSnapshotListener { snapshot, error ->
                    isBroadcastLoading = false
                    if (error == null && snapshot != null) {
                        val doc = snapshot.documents.firstOrNull()
                        if (doc != null) {
                            val expiresAtStr = doc.getString("expiresAt")
                            val isExpired = try {
                                if (!expiresAtStr.isNullOrBlank()) {
                                    java.time.Instant.parse(expiresAtStr).isBefore(java.time.Instant.now())
                                } else false
                            } catch (_: Exception) {
                                false
                            }

                            if (isExpired) {
                                cloudBroadcast = null
                                return@addSnapshotListener
                            }

                            val title = doc.getString("title") ?: ""
                            val subtitle = doc.getString("message") ?: doc.getString("subtitle") ?: ""
                            val actionUrl = doc.getString("actionUrl") ?: ""
                            val badge = doc.getString("badge") ?: "NOTICE"
                            val bannerImageUrl = doc.getString("bannerImageUrl") ?: ""
                            val description = doc.getString("description") ?: subtitle
                            val prizePool = doc.getString("prizePool") ?: ""
                            val location = doc.getString("location") ?: ""
                            val teamSize = doc.getString("teamSize") ?: ""
                            val timeline = doc.getString("timeline") ?: ""
                            val tags = (doc.get("tags") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                            if (title.isNotBlank()) {
                                cloudBroadcast = CloudBroadcastBanner(
                                    id = doc.id,
                                    title = title,
                                    subtitle = subtitle,
                                    actionUrl = actionUrl,
                                    badge = badge,
                                    bannerImageUrl = bannerImageUrl,
                                    description = description,
                                    prizePool = prizePool,
                                    location = location,
                                    teamSize = teamSize,
                                    timeline = timeline,
                                    tags = tags
                                )
                            }
                        } else {
                            cloudBroadcast = null
                        }
                    }
                }
        } catch (_: Exception) {
            isBroadcastLoading = false
            null
        }

        onDispose {
            listener?.remove()
        }
    }

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

    var showOfflineDialog by remember { mutableStateOf(false) }
    var showBackOnlineBanner by remember { mutableStateOf(false) }
    var wasOffline by remember { mutableStateOf(uiState.isOffline) }

    LaunchedEffect(uiState.isOffline) {
        if (wasOffline && !uiState.isOffline) {
            showBackOnlineBanner = true
            delay(2800)
            showBackOnlineBanner = false
        }
        wasOffline = uiState.isOffline
    }

    GlassmorphismBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            // Show shimmer skeleton while data is still loading
            androidx.compose.animation.AnimatedVisibility(
                visible = uiState.isLoading,
                enter = androidx.compose.animation.fadeIn(),
                exit = androidx.compose.animation.fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .statusBarsPadding()
                ) {
                    HomeScreenSkeleton()
                }
            }
            androidx.compose.animation.AnimatedVisibility(
                visible = !uiState.isLoading,
                enter = androidx.compose.animation.fadeIn(
                    animationSpec = androidx.compose.animation.core.tween(300)
                ),
                exit = androidx.compose.animation.fadeOut()
            ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
            // ── HEADER ───────────────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .padding(top = 22.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .padding(end = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = greeting,
                            style = Typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.5.sp,
                                fontSize = 11.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (uiState.isOffline) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFF59E0B).copy(alpha = 0.12f),
                                border = BorderStroke(0.8.dp, Color(0xFFF59E0B).copy(alpha = 0.35f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .background(Color(0xFFF59E0B), CircleShape)
                                    )
                                    Text(
                                        text = "OFFLINE",
                                        style = Typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.4.sp
                                        ),
                                        color = Color(0xFFF59E0B)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    if (!resolvedUserName.isNullOrBlank()) {
                        Text(
                            text = resolvedUserName,
                            style = Typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp,
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
                                    fontSize = 24.sp,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1
                            )
                            Text(
                                text = "Calendar",
                                style = Typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 24.sp,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = BrandPrimaryOrange,
                                maxLines = 1
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
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
                            modifier = Modifier
                                .height(36.dp)
                                .padding(horizontal = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Rounded.LocalFireDepartment,
                                contentDescription = "Daily Streak",
                                modifier = Modifier
                                    .size(15.dp)
                                    .scale(fireScale),
                                tint = Color(0xFFF59E0B)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${uiState.userStreak}d",
                                style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // ── GLASS NOTIFICATION BELL (BETWEEN STREAK AND REFRESH) ──
                    GlassCard(
                        cornerRadius = 20.dp,
                        accentColor = if (hasUnreadBroadcast) BrandPrimaryOrange else null,
                        onClick = {
                            onViewAllNotificationsClick()
                        }
                    ) {
                        Box(
                            modifier = Modifier.size(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.Notifications,
                                contentDescription = "Notifications & Broadcasts",
                                modifier = Modifier.size(18.dp),
                                tint = if (hasUnreadBroadcast) BrandPrimaryOrange
                                else MaterialTheme.colorScheme.onSurface
                            )

                            // Glowing notification dot
                            if (hasUnreadBroadcast) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(top = 7.dp, end = 7.dp)
                                        .size(7.dp)
                                        .background(BrandPrimaryOrange, CircleShape)
                                        .border(1.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                        .clip(CircleShape)
                                )
                            }
                        }
                    }

                    // Refresh FAB
                    GlassCard(
                        cornerRadius = 20.dp,
                        onClick = onRefresh
                    ) {
                        Box(
                            modifier = Modifier.size(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.Refresh,
                                contentDescription = "Refresh",
                                modifier = Modifier
                                    .size(17.dp)
                                    .rotate(if (isRefreshing) refreshAngle else 0f),
                                tint = if (isRefreshing) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            if (isBroadcastLoading) {
                BroadcastBannerSkeleton()
            } else if (cloudBroadcast != null && !isCurrentBroadcastDismissedOnHome) {
                val currentBroadcast = cloudBroadcast!!
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 6.dp),
                    cornerRadius = 18.dp,
                    accentColor = null,
                    borderWidth = 0.dp,
                    onClick = {
                        onNotificationClick(currentBroadcast)
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
                                .background(BrandPrimaryOrange.copy(alpha = 0.16f), CircleShape),
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
                                        text = currentBroadcast.badge.uppercase(),
                                        style = Typography.labelSmall.copy(fontSize = 8.5.sp, fontWeight = FontWeight.Black),
                                        color = BrandPrimaryOrange,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                                Text(
                                    text = currentBroadcast.title,
                                    style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.5.sp),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (currentBroadcast.subtitle.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = currentBroadcast.subtitle,
                                    style = Typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                val bId = currentBroadcast.id
                                val updated = dismissedHomeBroadcastIds + bId
                                dismissedHomeBroadcastIds = updated
                                homePrefs.edit().putStringSet("dismissed_broadcast_ids", updated).apply()
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Clear,
                                contentDescription = "Dismiss from Home",
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
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .clickable { showOfflineDialog = true },
                    accentColor = Color(0xFFF59E0B),
                    cornerRadius = 14.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.WifiOff,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(17.dp)
                            )
                            Column {
                                Text(
                                    text = "Offline Mode — Cached data active",
                                    style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFF59E0B),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Tap to view offline capabilities & network settings",
                                    style = Typography.labelSmall.copy(fontSize = 10.5.sp),
                                    color = Color(0xFFF59E0B).copy(alpha = 0.85f)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF59E0B).copy(alpha = 0.20f),
                            border = BorderStroke(0.8.dp, Color(0xFFF59E0B).copy(alpha = 0.50f)),
                            modifier = Modifier.clickable { onRefresh() }
                        ) {
                            Text(
                                text = "Retry",
                                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFF59E0B),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
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

                Spacer(modifier = Modifier.height(24.dp))

                // ── QUICK ACCESS GRID ─────────────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    SectionHeader(title = "Quick Access")
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickAccessTile(
                            icon = Icons.Rounded.EmojiEvents,
                            label = "Contests",
                            sublabel = "${uiState.upcomingContests.size} upcoming",
                            accentColor = BrandPrimaryOrange,
                            onClick = onViewAllContestsClick,
                            modifier = Modifier.weight(1f)
                        )
                        QuickAccessTile(
                            icon = Icons.Rounded.Code,
                            label = "Problem\nof the Day",
                            sublabel = if (uiState.dailyProblem != null) uiState.dailyProblem.difficulty else "LeetCode",
                            accentColor = Color(0xFF38BDF8),
                            onClick = {
                                val link = uiState.dailyProblem?.link
                                if (!link.isNullOrBlank()) {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                                        context.startActivity(intent)
                                    } catch (_: Exception) {}
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickAccessTile(
                            icon = Icons.AutoMirrored.Rounded.MenuBook,
                            label = "Dev Hub",
                            sublabel = "Resources & sheets",
                            accentColor = Color(0xFFA855F7),
                            onClick = { onResourceClick("https://neetcode.io") },
                            modifier = Modifier.weight(1f)
                        )
                        QuickAccessTile(
                            icon = Icons.Rounded.BarChart,
                            label = "My Ratings",
                            sublabel = if (uiState.connectedStats.isNotEmpty())
                                "${uiState.connectedStats.size} platforms"
                            else "Connect now",
                            accentColor = Color(0xFF22C55E),
                            onClick = onAddPlatformClick,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

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
                        title = "No Platforms Connected",
                        message = "Link your Codeforces, LeetCode, GitHub, or CodeChef handles to track ratings, streaks, and charts.",
                        icon = Icons.Rounded.AddLink,
                        actionLabel = "Connect Platforms",
                        onActionClick = onAddPlatformClick,
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

                // ── DAILY CODING CHALLENGE (POTD) ───────────────────────────────────
                if (uiState.dailyProblem != null) {
                    val potd = uiState.dailyProblem
                    SectionHeader(
                        title = "Problem of the Day",
                        modifier = Modifier.padding(horizontal = 20.dp),
                        trailingContent = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(Color(0xFFFFA116), CircleShape)
                                )
                                Text(
                                    text = "LeetCode",
                                    style = Typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp
                                    ),
                                    color = Color(0xFFFFA116)
                                )
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    DailyProblemCard(
                        dailyProblem = potd,
                        onClick = {
                            if (potd.link.isNotBlank()) {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(potd.link))
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            }
                        },
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                } else if (isRefreshing) {
                    SectionHeader(
                        title = "Problem of the Day",
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    DailyProblemCardSkeleton(modifier = Modifier.padding(horizontal = 20.dp))
                    Spacer(modifier = Modifier.height(32.dp))
                }

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
                    EmptyState(
                        title = "Radar Clear",
                        message = "No upcoming contests found. Sync with the live radar to check for upcoming rounds across all platforms.",
                        icon = Icons.Rounded.CloudSync,
                        actionLabel = "Refresh Radar",
                        onActionClick = onRefresh,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
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
                } // closes else
            } // closes Column

            // Top Animated Banner for Back Online
            BackOnlineBanner(
                visible = showBackOnlineBanner,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp)
            )
        } // closes Box

        // ── ANIMATED DAILY STREAK CELEBRATION MODAL ───────────────────────────
        if (showStreakModal) {
            StreakCelebrationModal(
                streakDays = uiState.userStreak,
                dateText = uiState.streakInfo?.lastOpenDateText ?: "Today",
                onDismiss = { showStreakModal = false }
            )
        }

        // ── OFFLINE NETWORK CAPABILITY DIALOG ────────────────────────────────
        if (showOfflineDialog) {
            val context = LocalContext.current
            OfflineNetworkDialog(
                onDismiss = { showOfflineDialog = false },
                onRetry = {
                    onRefresh()
                    if (!uiState.isOffline) {
                        showOfflineDialog = false
                    }
                },
                onOpenSettings = {
                    runCatching {
                        val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    }.onFailure {
                        runCatching {
                            context.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            })
                        }
                    }
                },
                isRetrying = isRefreshing
            )
        }
        } // close AnimatedVisibility (non-loading)
    } // close Box
} // close GlassmorphismBackground

// ── HERO SPOTLIGHT NEXT CONTEST CARD ──────────────────────────────────────────

@Composable
fun NextContestHeroCard(
    contest: Contest,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
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
    val isUrgent = remaining < 3600 && contest.status == ContestStatus.UPCOMING
    val isLive = contest.status == ContestStatus.LIVE

    val urgentAlpha by rememberInfiniteTransition(label = "urgentPulse").animateFloat(
        initialValue = 0.65f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(600, easing = LinearEasing), RepeatMode.Reverse),
        label = "urgentAlpha"
    )

    val liveBeaconAlpha by rememberInfiniteTransition(label = "heroBeaconPulse").animateFloat(
        initialValue = 0.40f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "heroBeaconAlpha"
    )

    GlassCard(
        modifier = modifier,
        accentColor = null,
        cornerRadius = 24.dp,
        elevation = 6.dp,
        borderWidth = 0.dp,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header Row: Platform Name + Status (Clean, Unboxed, Zero Chips)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .background(contest.platform.getBrandColor(), CircleShape)
                    )
                    Text(
                        text = contest.platform.getDisplayName(),
                        style = Typography.labelLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.5.sp,
                            letterSpacing = 0.3.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (isLive) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981).copy(alpha = liveBeaconAlpha))
                        )
                        Text(
                            text = "Live",
                            style = Typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp
                            ),
                            color = Color(0xFF10B981)
                        )
                    }
                } else if (isUrgent) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AlarmOn,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = CountdownUrgent
                        )
                        Text(
                            text = "Starting Soon",
                            style = Typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp
                            ),
                            color = CountdownUrgent
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Contest Headline
            Text(
                text = contest.name,
                style = Typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.5.sp,
                    lineHeight = 25.sp,
                    letterSpacing = (-0.3).sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(8.dp))

            // Inline Metadata (Date + Duration)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Event,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = contest.startTimeUtc.formatToIndianShortDateTime(),
                    style = Typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "·",
                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.40f)
                )
                Icon(
                    imageVector = Icons.Rounded.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                )
                Text(
                    text = formatContestDuration(contest.durationSeconds),
                    style = Typography.labelSmall.copy(fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.80f)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Middle Section: 3-Box High-Contrast Countdown (No Borders)
            Column {
                Text(
                    text = if (isLive) "ENDS IN" else "STARTS IN",
                    style = Typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontSize = 10.sp
                    ),
                    color = if (isLive) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val countdownTint = when {
                        isLive -> Color(0xFF10B981)
                        isUrgent -> CountdownUrgent.copy(alpha = urgentAlpha)
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                    CyberCountdownBlock(
                        value = "%02d".format(h),
                        unit = "HOURS",
                        tintColor = countdownTint,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = ":",
                        style = Typography.titleLarge.copy(fontWeight = FontWeight.Black, fontSize = 18.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                    )
                    CyberCountdownBlock(
                        value = "%02d".format(m),
                        unit = "MINS",
                        tintColor = countdownTint,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = ":",
                        style = Typography.titleLarge.copy(fontWeight = FontWeight.Black, fontSize = 18.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                    )
                    CyberCountdownBlock(
                        value = "%02d".format(s),
                        unit = "SECS",
                        tintColor = countdownTint,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Action Hub: High-Impact Button + Share & Copy Icons (Zero Borders)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Micro Action Cluster (Share + Copy)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f),
                        modifier = Modifier.clickable {
                            runCatching {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Contest Link", contest.officialUrl)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Contest link copied!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Box(modifier = Modifier.size(38.dp), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.ContentCopy,
                                contentDescription = "Copy Link",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f),
                        modifier = Modifier.clickable {
                            try {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "⚔️ Code Contest Alert: ${contest.name} on ${contest.platform.name}\nStarts: ${contest.startTimeUtc.formatToIndianShortDateTime()}\n🔗 Link: ${contest.officialUrl}")
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Contest"))
                            } catch (_: Exception) {}
                        }
                    ) {
                        Box(modifier = Modifier.size(38.dp), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Share,
                                contentDescription = "Share Contest",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Primary Gradient CTA
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.Transparent,
                    shadowElevation = 2.dp,
                    modifier = Modifier.clickable { onClick() }
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        BrandPrimaryOrange,
                                        Color(0xFFFF8C00)
                                    )
                                )
                            )
                            .padding(horizontal = 18.dp, vertical = 9.5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (isLive) "Enter Arena" else "Contest Arena",
                                style = Typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    letterSpacing = 0.2.sp
                                ),
                                color = Color.White
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * CyberCountdownBlock — Seamless high-contrast digital timer tile without borders.
 */
@Composable
private fun CyberCountdownBlock(
    value: String,
    unit: String,
    tintColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.30f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                style = Typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 19.sp,
                    letterSpacing = 0.5.sp
                ),
                color = tintColor
            )
            Text(
                text = unit,
                style = Typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 8.5.sp,
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
            )
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
        accentColor = null,
        cornerRadius = 20.dp,
        borderWidth = 0.dp,
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
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .background(brandColor, CircleShape)
                    )
                    Spacer(Modifier.width(8.dp))
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
                    color = Color(0xFFFF6B00).copy(alpha = 0.12f)
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
                                else -> Color.White.copy(alpha = 0.20f)
                            }
                            Box(
                                modifier = Modifier
                                    .size(11.dp)
                                    .background(heatColor, RoundedCornerShape(2.dp))
                                    .border(0.5.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(2.dp))
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
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f), RoundedCornerShape(12.dp))
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
                        Color.White.copy(alpha = 0.20f),
                        Color(0xFF0E4429),
                        Color(0xFF006D32),
                        Color(0xFF26A641),
                        Color(0xFF39D353)
                    ).forEach { col ->
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .background(col, RoundedCornerShape(2.dp))
                                .border(0.5.dp, Color.White.copy(alpha = 0.30f), RoundedCornerShape(2.dp))
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
        modifier = Modifier.width(168.dp),
        accentColor = null,
        cornerRadius = 20.dp,
        elevation = 4.dp,
        borderWidth = 0.dp,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(brandColor, CircleShape)
                    )
                    Text(
                        text = stat.platform.getDisplayName(),
                        style = Typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        color = brandColor
                    )
                }
            }

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

            Spacer(Modifier.height(4.dp))

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = brandColor.copy(alpha = 0.12f)
            ) {
                Text(
                    text = stat.rank ?: "Active Coder",
                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.5.sp),
                    color = brandColor,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = "@${stat.username}",
                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.5.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
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
    val isLive = contest.status == ContestStatus.LIVE
    val timeUntil = remember(contest.startTimeUtc) {
        Duration.between(Instant.now(), contest.startTimeUtc).seconds.coerceAtLeast(0)
    }
    val timeLabel = when {
        isLive  -> "Live now"
        contest.status == ContestStatus.ENDED -> "Ended"
        timeUntil < 3600   -> "in ${timeUntil / 60}m"
        timeUntil < 86400  -> "in ${timeUntil / 3600}h ${(timeUntil % 3600) / 60}m"
        else               -> "in ${timeUntil / 86400}d"
    }

    val safeDuration = contest.durationSeconds.coerceIn(0L, 2592000L)
    val durationText = formatContestDuration(safeDuration)

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        accentColor = null,
        cornerRadius = 18.dp,
        elevation = 3.dp,
        borderWidth = 0.dp,
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
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(brandColor, CircleShape)
                    )
                    Text(
                        text = contest.platform.getDisplayName(),
                        style = Typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp
                        ),
                        color = brandColor
                    )
                    if (contest.durationSeconds > 0) {
                        Text(
                            text = "· $durationText",
                            style = Typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
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
                Spacer(Modifier.height(3.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Event,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = contest.startTimeUtc.formatToIndianShortDateTime(),
                        style = Typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            if (isLive) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Text(
                        text = "Live",
                        style = Typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        ),
                        color = Color(0xFF10B981)
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = timeLabel,
                        style = Typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
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
        borderWidth = 0.dp,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    )
                    Text(
                        text = resource.category.uppercase(),
                        style = Typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp
                        ),
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
                    color = BrandPrimaryOrange.copy(alpha = 0.15f)
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

// ── PROBLEM OF THE DAY (LEETCODE POTD) CARD ───────────────────────────────────

@Composable
fun DailyProblemCard(
    dailyProblem: DailyProblem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val brandColor = Color(0xFFFFA116) // LeetCode signature orange

    val diffColor = when (dailyProblem.difficulty.uppercase()) {
        "EASY" -> Color(0xFF06B6D4) // Electric Cyber Cyan
        "HARD" -> Color(0xFFFF334B) // Electric Rose
        else -> Color(0xFFFFC01E)   // Warm Amber for Medium
    }

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 22.dp,
        accentColor = null,
        elevation = 6.dp,
        borderWidth = 0.dp,
        onClick = {
            if (dailyProblem.link.isNotBlank()) {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(dailyProblem.link))
                    context.startActivity(intent)
                } catch (_: Exception) {}
            } else {
                onClick()
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Top Row: Unboxed Platform Label + Difficulty Tag + Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(brandColor, CircleShape)
                    )
                    Text(
                        text = "LeetCode POTD",
                        style = Typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        color = brandColor
                    )
                    Text(
                        text = "·",
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.40f)
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = diffColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = dailyProblem.difficulty.uppercase(),
                            style = Typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = diffColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp)
                        )
                    }
                }

                if (dailyProblem.date.isNotBlank()) {
                    Text(
                        text = dailyProblem.date,
                        style = Typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title
            Text(
                text = dailyProblem.title,
                style = Typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    lineHeight = 23.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Topic Tags (if present)
            if (dailyProblem.topicTags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (tag in dailyProblem.topicTags.take(3)) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        ) {
                            Text(
                                text = tag,
                                style = Typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Solve Button CTA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daily Habit Active 🔥",
                    style = Typography.bodySmall.copy(
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f),
                        modifier = Modifier.clickable {
                            try {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "🧩 LeetCode Daily Challenge: ${dailyProblem.title} [${dailyProblem.difficulty}]\n🔗 Solve here: ${dailyProblem.link}")
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Problem of the Day"))
                            } catch (_: Exception) {}
                        }
                    ) {
                        Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Share,
                                contentDescription = "Share Problem",
                                tint = brandColor,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Transparent,
                        modifier = Modifier.clickable {
                            if (dailyProblem.link.isNotBlank()) {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(dailyProblem.link))
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            } else {
                                onClick()
                            }
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(brandColor, Color(0xFFFF8533))
                                    )
                                )
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(
                                    text = "Solve Challenge",
                                    style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
                                    color = Color.White
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp),
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
// ── QUICK ACCESS TILE ─────────────────────────────────────────────────────────

@Composable
fun QuickAccessTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    sublabel: String,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
        label = "tileScale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (isDark) Color(0xFF131A26).copy(alpha = 0.90f)
                else Color(0xFFF1F5F9).copy(alpha = 0.90f)
            )
            .background(
                Brush.verticalGradient(
                    listOf(
                        accentColor.copy(alpha = if (isDark) 0.08f else 0.05f),
                        Color.Transparent
                    )
                )
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Icon with glow circle
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = if (isDark) 0.18f else 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = label,
                    style = Typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                        lineHeight = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2
                )
                Text(
                    text = sublabel,
                    style = Typography.labelSmall.copy(fontSize = 10.5.sp),
                    color = accentColor.copy(alpha = 0.75f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
