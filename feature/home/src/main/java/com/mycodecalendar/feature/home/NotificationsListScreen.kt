package com.mycodecalendar.feature.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mycodecalendar.core.designsystem.BrandPrimaryOrange
import com.mycodecalendar.core.designsystem.GlassmorphismBackground
import com.mycodecalendar.core.designsystem.Typography
import com.mycodecalendar.core.designsystem.components.GlassCard
import com.mycodecalendar.core.designsystem.components.GlassChip
import com.mycodecalendar.core.designsystem.components.NotificationsListSkeleton
import com.mycodecalendar.core.designsystem.components.ScrollRevealContainer
import com.mycodecalendar.core.designsystem.components.formatToIndianCompactDateTime
import java.time.Instant

data class AppNotification(
    val id: String,
    val type: NotificationKind,
    val title: String,
    val subtitle: String,
    val badge: String,
    val badgeColor: Color = BrandPrimaryOrange,
    val imageUrl: String = "",
    val actionUrl: String = "",
    val prizePool: String = "",
    val scheduleInfo: String = "",
    val tags: List<String> = emptyList(),
    val broadcast: CloudBroadcastBanner? = null,
    val isNew: Boolean = true
)

enum class NotificationKind {
    CONTEST_ALERT, HACKATHON, BROADCAST, MATERIAL_ADDED, PLAYLIST, SYSTEM
}

private val defaultNotifications = listOf(
    AppNotification(
        id = "default_contest_1",
        type = NotificationKind.CONTEST_ALERT,
        title = "Codeforces Round 1002 (Div. 2) — Contest Alert",
        subtitle = "Starts at 08:00 PM IST · 2 Hours · 6 Problems · Rating impact active",
        badge = "CONTEST ALERT",
        badgeColor = Color(0xFF00E5FF),
        scheduleInfo = "Starts Today · 08:00 PM IST",
        actionUrl = "codecalendar://contests",
        tags = listOf("Codeforces", "Rated", "Div. 2", "Competitive"),
        isNew = true
    ),
    AppNotification(
        id = "default_hackathon_1",
        type = NotificationKind.HACKATHON,
        title = "Innovik 6.0 — National AI Hackathon",
        subtitle = "Live registrations on Unstop · ₹2,00,000 Prize Pool · Offline Grand Finale at VITM Indore",
        badge = "HACKATHON",
        badgeColor = BrandPrimaryOrange,
        actionUrl = "https://unstop.com",
        prizePool = "₹2,00,000",
        scheduleInfo = "Finale: 25 Aug 2026",
        tags = listOf("Applied AI", "Agentic AI", "Hackathon", "₹2 Lakhs"),
        isNew = true,
        broadcast = CloudBroadcastBanner(
            id = "innovik_6",
            title = "Innovik 6.0 — National AI Hackathon",
            subtitle = "Live on Unstop · ₹2,00,000 Prize Pool",
            actionUrl = "https://unstop.com",
            badge = "HACKATHON",
            description = "Innovik 6.0 brings together top student developers and creators to build bleeding-edge Agentic AI solutions.",
            prizePool = "₹ 2,00,000",
            location = "VITM Indore Campus",
            teamSize = "2 - 4 Members",
            timeline = "06 Aug 2026 – 25 Aug 2026",
            tags = listOf("Applied AI", "Agentic AI", "Hackathon", "₹2 Lakh Prizes", "Unstop")
        )
    ),
    AppNotification(
        id = "default_sheet_1",
        type = NotificationKind.MATERIAL_ADDED,
        title = "Striver's A2Z DSA Sheet",
        subtitle = "Complete topic-wise data structures & algorithms roadmap with 450+ curated problems.",
        badge = "DSA SHEET",
        badgeColor = Color(0xFF10B981),
        actionUrl = "https://takeuforward.org/strivers-a2z-dsa-course/strivers-a2z-dsa-course-sheet-2",
        tags = listOf("Striver", "A2Z DSA", "Top Pick"),
        isNew = false
    ),
    AppNotification(
        id = "default_playlist_1",
        type = NotificationKind.PLAYLIST,
        title = "TakeUForward Graph & DP Masterclass",
        subtitle = "Comprehensive video series covering Dynamic Programming and Graph Algorithms step-by-step.",
        badge = "YOUTUBE PLAYLIST",
        badgeColor = Color(0xFFEF4444),
        actionUrl = "https://www.youtube.com/@takeUforward",
        tags = listOf("YouTube", "DP Series", "Graphs"),
        isNew = false
    ),
    AppNotification(
        id = "default_material_2",
        type = NotificationKind.MATERIAL_ADDED,
        title = "NeetCode 150 & Blind 75 Sheet",
        subtitle = "Core pattern-based coding interview roadmap for LeetCode practice.",
        badge = "DSA SHEET",
        badgeColor = Color(0xFF10B981),
        actionUrl = "https://neetcode.io/practice",
        tags = listOf("NeetCode", "Blind 75", "Interview Prep"),
        isNew = false
    ),
    AppNotification(
        id = "default_radar_2",
        type = NotificationKind.SYSTEM,
        title = "Contest Radar Synchronized",
        subtitle = "Tracking upcoming and active contests across LeetCode, Codeforces, AtCoder, and CodeChef.",
        badge = "RADAR SYNC",
        badgeColor = Color(0xFF3B82F6),
        actionUrl = "codecalendar://contests",
        tags = listOf("Radar", "Live Feeds"),
        isNew = false
    )
)

/**
 * Redesigned, state-of-the-art Notification Center:
 * - Real-time Cloud Broadcasts, Custom Contests, and Study Materials.
 * - Staggered ScrollReveal animations for smooth viewport entry.
 * - High-tech Radar pulse banner with 3/day anti-spam indicator.
 * - Interactive filter chips with category counts and icons.
 * - Bespoke Contest, Broadcast, and Material card architectures.
 */
@Composable
fun NotificationsListScreen(
    onBackClick: () -> Unit,
    onNotificationClick: (AppNotification) -> Unit,
    onOpenResource: () -> Unit = {},
    onViewContests: () -> Unit = {}
) {
    val context = LocalContext.current
    val notifPrefs = remember { context.getSharedPreferences("app_notif_prefs", android.content.Context.MODE_PRIVATE) }

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    // Proactively request notification permission on entering screen so alerts show when screen is off
    LaunchedEffect(Unit) {
        if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    var notifications by remember { mutableStateOf<List<AppNotification>>(defaultNotifications) }
    var dismissedIds by remember {
        mutableStateOf(notifPrefs.getStringSet("dismissed_notif_ids", emptySet()) ?: emptySet())
    }
    var readIds by remember {
        mutableStateOf(notifPrefs.getStringSet("read_notif_ids", emptySet()) ?: emptySet())
    }
    var isLoading by remember { mutableStateOf(true) }
    var selectedCategoryIndex by remember { mutableStateOf(0) }

    // Fetch live notifications from Firestore (broadcasts, featured_materials, custom_contests)
    LaunchedEffect(Unit) {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val result = mutableListOf<AppNotification>()

        try {
            // 1. Fetch broadcasts
            db.collection("broadcasts").get().addOnSuccessListener { broadcastSnap ->
                broadcastSnap.documents.forEach { doc ->
                    val isActive = doc.getBoolean("isActive") ?: false
                    if (!isActive) return@forEach

                    val expiresAtStr = doc.getString("expiresAt")
                    val isExpired = try {
                        if (!expiresAtStr.isNullOrBlank()) {
                            java.time.Instant.parse(expiresAtStr).isBefore(java.time.Instant.now())
                        } else false
                    } catch (_: Exception) {
                        false
                    }
                    if (isExpired) return@forEach

                    val title = doc.getString("title") ?: return@forEach
                    val subtitle = doc.getString("message") ?: doc.getString("subtitle") ?: ""
                    val rawBadge = (doc.getString("badge") ?: "NOTICE").replace(Regex("[\\p{So}\\p{Cn}]"), "").trim()
                    val actionUrl = doc.getString("actionUrl") ?: ""
                    val bannerImageUrl = doc.getString("bannerImageUrl") ?: ""
                    val prizePool = doc.getString("prizePool") ?: ""
                    val tags = (doc.get("tags") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                    val description = doc.getString("description") ?: subtitle

                    val isContestAlert = rawBadge.contains("CONTEST", ignoreCase = true) || actionUrl.contains("contest", ignoreCase = true)
                    val isHackathon = rawBadge.contains("HACKATHON", ignoreCase = true) || rawBadge.contains("EVENT", ignoreCase = true)

                    val kind = when {
                        isContestAlert -> NotificationKind.CONTEST_ALERT
                        isHackathon -> NotificationKind.HACKATHON
                        else -> NotificationKind.BROADCAST
                    }

                    val badgeColor = when {
                        isContestAlert -> Color(0xFF00E5FF)
                        isHackathon -> BrandPrimaryOrange
                        rawBadge.contains("UPDATE", ignoreCase = true) -> Color(0xFF3B82F6)
                        rawBadge.contains("WARNING", ignoreCase = true) -> Color(0xFFF59E0B)
                        else -> BrandPrimaryOrange
                    }

                    result.add(
                        AppNotification(
                            id = doc.id,
                            type = kind,
                            title = title,
                            subtitle = subtitle,
                            badge = rawBadge.ifBlank { "ANNOUNCEMENT" },
                            badgeColor = badgeColor,
                            imageUrl = bannerImageUrl,
                            actionUrl = actionUrl,
                            prizePool = prizePool,
                            tags = tags,
                            isNew = doc.id !in readIds,
                            broadcast = CloudBroadcastBanner(
                                id = doc.id,
                                title = title,
                                subtitle = subtitle,
                                actionUrl = actionUrl,
                                badge = rawBadge.ifBlank { "ANNOUNCEMENT" },
                                bannerImageUrl = bannerImageUrl,
                                description = description,
                                prizePool = prizePool,
                                location = doc.getString("location") ?: "",
                                teamSize = doc.getString("teamSize") ?: "",
                                timeline = doc.getString("timeline") ?: "",
                                tags = tags
                            )
                        )
                    )
                }

                // 2. Fetch custom contests & hackathons
                db.collection("custom_contests").get().addOnSuccessListener { contestSnap ->
                    contestSnap.documents.forEach { doc ->
                        try {
                            val isActive = doc.getBoolean("isActive") ?: true
                            if (!isActive && doc.contains("isActive")) return@forEach

                            val title = doc.getString("title") ?: doc.getString("name") ?: return@forEach
                            val platform = doc.getString("platform") ?: doc.getString("organizer") ?: "Community"
                            val regUrl = doc.getString("registrationUrl") ?: doc.getString("url") ?: "codecalendar://contests"
                            val bannerUrl = doc.getString("bannerImageUrl") ?: doc.getString("bannerUrl") ?: ""
                            val prize = doc.getString("prize") ?: doc.getString("prizePool") ?: ""
                            val tags = (doc.get("tags") as? List<*>)?.mapNotNull { it?.toString() } ?: listOf(platform, "Contest")

                            val startMillis = when (val raw = doc.get("startTime")) {
                                is com.google.firebase.Timestamp -> raw.toDate().time
                                is java.util.Date -> raw.time
                                is Number -> {
                                    val num = raw.toLong()
                                    if (num in 1..99_999_999_999L) num * 1000L else num
                                }
                                is String -> {
                                    raw.toLongOrNull() ?: try {
                                        Instant.parse(raw).toEpochMilli()
                                    } catch (_: Exception) { null }
                                }
                                else -> null
                            }

                            val scheduleInfo = if (startMillis != null && startMillis > 0) {
                                "Starts: ${Instant.ofEpochMilli(startMillis).formatToIndianCompactDateTime()}"
                            } else {
                                "Upcoming Contest · Register Now"
                            }

                            result.add(
                                AppNotification(
                                    id = "custom_${doc.id}",
                                    type = NotificationKind.CONTEST_ALERT,
                                    title = title,
                                    subtitle = "Organized by $platform · Direct registration open",
                                    badge = "CONTEST ALERT",
                                    badgeColor = Color(0xFF00E5FF),
                                    imageUrl = bannerUrl,
                                    actionUrl = regUrl,
                                    prizePool = prize,
                                    scheduleInfo = scheduleInfo,
                                    tags = tags,
                                    isNew = "custom_${doc.id}" !in readIds
                                )
                            )
                        } catch (e: Exception) {
                            android.util.Log.e("NotificationsListScreen", "Error parsing custom_contest doc ${doc.id}", e)
                        }
                    }

                    // 3. Fetch featured study materials
                    db.collection("featured_materials").get().addOnSuccessListener { matSnap ->
                        matSnap.documents.forEach { doc ->
                            val isActive = doc.getBoolean("isActive") ?: false
                            if (!isActive) return@forEach
                            val title = doc.getString("title") ?: return@forEach
                            val description = doc.getString("description") ?: ""
                            val category = doc.getString("category") ?: "Resource"
                            val redirectUrl = doc.getString("redirectUrl") ?: doc.getString("url") ?: ""
                            val imageUrl = doc.getString("imageUrl") ?: doc.getString("thumbnailUrl") ?: ""
                            val tags = (doc.get("tags") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()

                            val isPlaylist = category.contains("YouTube", ignoreCase = true) ||
                                title.contains("Playlist", ignoreCase = true) ||
                                redirectUrl.contains("youtube", ignoreCase = true) ||
                                redirectUrl.contains("youtu.be", ignoreCase = true)

                            val isSheet = category.contains("Sheet", ignoreCase = true) ||
                                title.contains("Sheet", ignoreCase = true) ||
                                title.contains("450", ignoreCase = true) ||
                                title.contains("SDE", ignoreCase = true)

                            val kind = when {
                                isPlaylist -> NotificationKind.PLAYLIST
                                else -> NotificationKind.MATERIAL_ADDED
                            }

                            val badgeLabel = when {
                                isPlaylist -> "YOUTUBE PLAYLIST"
                                isSheet -> "DSA SHEET"
                                category.contains("Roadmap", ignoreCase = true) -> "ROADMAP"
                                else -> category.uppercase().replace(Regex("[\\p{So}\\p{Cn}]"), "").trim()
                            }

                            val badgeColor = when {
                                isPlaylist -> Color(0xFFEF4444)
                                isSheet -> Color(0xFF10B981)
                                category.contains("Roadmap", ignoreCase = true) -> Color(0xFF8B5CF6)
                                else -> Color(0xFF06B6D4)
                            }

                            result.add(
                                AppNotification(
                                    id = "mat_${doc.id}",
                                    type = kind,
                                    title = title,
                                    subtitle = description.take(130),
                                    badge = badgeLabel,
                                    badgeColor = badgeColor,
                                    imageUrl = imageUrl,
                                    actionUrl = redirectUrl,
                                    tags = tags,
                                    isNew = "mat_${doc.id}" !in readIds
                                )
                            )
                        }

                        if (result.isNotEmpty()) {
                            notifications = (result + defaultNotifications).distinctBy { it.id }
                        }
                        isLoading = false
                    }.addOnFailureListener {
                        isLoading = false
                    }
                }.addOnFailureListener {
                    isLoading = false
                }
            }.addOnFailureListener {
                isLoading = false
            }
        } catch (_: Exception) {
            isLoading = false
        }
    }

    val visibleNotifications = notifications.filter { it.id !in dismissedIds }

    val filteredNotifications = remember(visibleNotifications, selectedCategoryIndex) {
        when (selectedCategoryIndex) {
            1 -> visibleNotifications.filter { it.type == NotificationKind.CONTEST_ALERT || it.type == NotificationKind.HACKATHON }
            2 -> visibleNotifications.filter { it.type == NotificationKind.BROADCAST || it.type == NotificationKind.SYSTEM }
            3 -> visibleNotifications.filter { it.type == NotificationKind.MATERIAL_ADDED }
            4 -> visibleNotifications.filter { it.type == NotificationKind.PLAYLIST }
            else -> visibleNotifications
        }
    }

    val contestCount = visibleNotifications.count { it.type == NotificationKind.CONTEST_ALERT || it.type == NotificationKind.HACKATHON }
    val broadcastCount = visibleNotifications.count { it.type == NotificationKind.BROADCAST || it.type == NotificationKind.SYSTEM }
    val sheetCount = visibleNotifications.count { it.type == NotificationKind.MATERIAL_ADDED }
    val playlistCount = visibleNotifications.count { it.type == NotificationKind.PLAYLIST }
    val unreadCount = visibleNotifications.count { it.id !in readIds }

    // Pulsing radar animation
    val radarPulse by rememberInfiniteTransition(label = "radarPulse").animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    GlassmorphismBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── TOP APP BAR ──────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Frosted Back Button
                GlassCard(cornerRadius = 20.dp, onClick = onBackClick) {
                    Box(modifier = Modifier.size(42.dp), contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Title with Glowing Radar Beacon
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .scale(radarPulse)
                                .background(Color(0xFF10B981), CircleShape)
                        )
                        Text(
                            text = "Notification Radar",
                            style = Typography.titleMedium.copy(fontWeight = FontWeight.Black, fontSize = 18.sp),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Text(
                        text = "LIVE CONTEST & COMMUNITY ALERTS",
                        style = Typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                    )
                }

                // Action Menu: Mark all read & Clear all
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (unreadCount > 0) {
                        GlassCard(
                            cornerRadius = 14.dp,
                            onClick = {
                                val allIds = notifications.map { it.id }.toSet()
                                readIds = allIds
                                notifPrefs.edit().putStringSet("read_notif_ids", allIds).apply()
                            }
                        ) {
                            Box(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                                Icon(
                                    Icons.Rounded.DoneAll,
                                    contentDescription = "Mark all read",
                                    modifier = Modifier.size(16.dp),
                                    tint = Color(0xFF10B981)
                                )
                            }
                        }
                    }

                    if (visibleNotifications.isNotEmpty()) {
                        GlassCard(
                            cornerRadius = 14.dp,
                            onClick = {
                                val allIds = notifications.map { it.id }.toSet()
                                dismissedIds = allIds
                                notifPrefs.edit().putStringSet("dismissed_notif_ids", allIds).apply()
                            }
                        ) {
                            Box(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                                Text(
                                    text = "Clear",
                                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // ── NOTIFICATION PERMISSION & LOCKSCREEN STATUS BANNER ────────────
            ScrollRevealContainer(
                delayMillis = 20,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 4.dp)
            ) {
                if (!hasNotificationPermission) {
                    // Elevated Permission Warning & Request Card
                    GlassCard(
                        cornerRadius = 16.dp,
                        accentColor = BrandPrimaryOrange,
                        elevation = 3.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(BrandPrimaryOrange.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Rounded.NotificationsActive,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = BrandPrimaryOrange
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Enable Lock Screen & Contest Alerts",
                                        style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Allow notification permission so contest start alerts appear on your lock screen even when your screen is off.",
                                        style = Typography.labelSmall.copy(fontSize = 11.sp, lineHeight = 15.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BrandPrimaryOrange,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.NotificationsActive,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Allow Notifications",
                                    style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                                )
                            }
                        }
                    }
                } else {
                    // Active Lockscreen Alerts Indicator
                    GlassCard(
                        cornerRadius = 16.dp,
                        accentColor = Color(0xFF10B981),
                        elevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(Color(0xFF10B981).copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Rounded.NotificationsActive,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = Color(0xFF10B981)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Lock Screen & Contest Alerts Active",
                                        style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Alerts dispatch 15 min prior even when screen is locked",
                                        style = Typography.labelSmall.copy(fontSize = 10.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                                    )
                                }
                            }

                            if (unreadCount > 0) {
                                Surface(
                                    shape = CircleShape,
                                    color = BrandPrimaryOrange.copy(alpha = 0.20f),
                                    border = BorderStroke(0.8.dp, BrandPrimaryOrange.copy(alpha = 0.45f))
                                ) {
                                    Text(
                                        text = "$unreadCount NEW",
                                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 9.5.sp),
                                        color = BrandPrimaryOrange,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // ── CATEGORY FILTER ROW ──────────────────────────────────────────
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 4.dp)
            ) {
                item {
                    GlassChip(
                        label = "All (${visibleNotifications.size})",
                        selected = selectedCategoryIndex == 0,
                        accentColor = BrandPrimaryOrange,
                        onClick = { selectedCategoryIndex = 0 }
                    )
                }
                item {
                    GlassChip(
                        label = "Contests ($contestCount)",
                        selected = selectedCategoryIndex == 1,
                        accentColor = Color(0xFF00E5FF),
                        onClick = { selectedCategoryIndex = 1 }
                    )
                }
                item {
                    GlassChip(
                        label = "Announcements ($broadcastCount)",
                        selected = selectedCategoryIndex == 2,
                        accentColor = Color(0xFF3B82F6),
                        onClick = { selectedCategoryIndex = 2 }
                    )
                }
                item {
                    GlassChip(
                        label = "DSA Sheets ($sheetCount)",
                        selected = selectedCategoryIndex == 3,
                        accentColor = Color(0xFF10B981),
                        onClick = { selectedCategoryIndex = 3 }
                    )
                }
                item {
                    GlassChip(
                        label = "Playlists ($playlistCount)",
                        selected = selectedCategoryIndex == 4,
                        accentColor = Color(0xFFEF4444),
                        onClick = { selectedCategoryIndex = 4 }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (isLoading) {
                NotificationsListSkeleton()
            } else if (filteredNotifications.isEmpty()) {
                // High-tech empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(horizontal = 28.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .background(Color(0xFF00E5FF).copy(alpha = 0.10f), CircleShape)
                                .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.30f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.NotificationsNone,
                                contentDescription = null,
                                modifier = Modifier.size(38.dp),
                                tint = Color(0xFF00E5FF)
                            )
                        }
                        Text(
                            text = "Radar Clear — All Caught Up!",
                            style = Typography.titleMedium.copy(fontWeight = FontWeight.Black, fontSize = 17.sp),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "No active broadcasts in this category. Manual contest alerts and new DSA roadmaps will appear here.",
                            style = Typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 18.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        if (selectedCategoryIndex != 0) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = BrandPrimaryOrange.copy(alpha = 0.16f),
                                border = BorderStroke(1.dp, BrandPrimaryOrange.copy(alpha = 0.40f)),
                                modifier = Modifier.clickable { selectedCategoryIndex = 0 }
                            ) {
                                Text(
                                    text = "Show All Notifications",
                                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.5.sp),
                                    color = BrandPrimaryOrange,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredNotifications, key = { it.id }) { notif ->
                        val index = filteredNotifications.indexOf(notif)
                        AnimatedVisibility(
                            visible = notif.id !in dismissedIds,
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            ScrollRevealContainer(
                                delayMillis = (index * 35).coerceAtMost(250),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val isRead = notif.id in readIds

                                when (notif.type) {
                                    NotificationKind.CONTEST_ALERT, NotificationKind.HACKATHON -> {
                                        ContestNotificationCard(
                                            notification = notif,
                                            isRead = isRead,
                                            onClick = {
                                                markAsRead(notif.id, readIds, notifPrefs) { readIds = it }
                                                onNotificationClick(notif)
                                            },
                                            onViewContests = {
                                                markAsRead(notif.id, readIds, notifPrefs) { readIds = it }
                                                onViewContests()
                                            },
                                            onDismiss = {
                                                val updated = dismissedIds + notif.id
                                                dismissedIds = updated
                                                notifPrefs.edit().putStringSet("dismissed_notif_ids", updated).apply()
                                            }
                                        )
                                    }
                                    NotificationKind.BROADCAST, NotificationKind.SYSTEM -> {
                                        BroadcastNotificationCard(
                                            notification = notif,
                                            isRead = isRead,
                                            onClick = {
                                                markAsRead(notif.id, readIds, notifPrefs) { readIds = it }
                                                onNotificationClick(notif)
                                            },
                                            onDismiss = {
                                                val updated = dismissedIds + notif.id
                                                dismissedIds = updated
                                                notifPrefs.edit().putStringSet("dismissed_notif_ids", updated).apply()
                                            }
                                        )
                                    }
                                    NotificationKind.MATERIAL_ADDED, NotificationKind.PLAYLIST -> {
                                        MaterialNotificationCard(
                                            notification = notif,
                                            isRead = isRead,
                                            onOpenInApp = {
                                                markAsRead(notif.id, readIds, notifPrefs) { readIds = it }
                                                onOpenResource()
                                            },
                                            onOpenLink = {
                                                markAsRead(notif.id, readIds, notifPrefs) { readIds = it }
                                                if (notif.actionUrl.isNotBlank()) {
                                                    try {
                                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(notif.actionUrl)))
                                                    } catch (_: Exception) {
                                                        onOpenResource()
                                                    }
                                                } else {
                                                    onOpenResource()
                                                }
                                            },
                                            onDismiss = {
                                                val updated = dismissedIds + notif.id
                                                dismissedIds = updated
                                                notifPrefs.edit().putStringSet("dismissed_notif_ids", updated).apply()
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun markAsRead(
    id: String,
    currentSet: Set<String>,
    prefs: android.content.SharedPreferences,
    onUpdated: (Set<String>) -> Unit
) {
    if (id !in currentSet) {
        val next = currentSet + id
        onUpdated(next)
        prefs.edit().putStringSet("read_notif_ids", next).apply()
    }
}

/**
 * High-voltage Contest & Hackathon Notification Card with glowing neon border.
 */
@Composable
private fun ContestNotificationCard(
    notification: AppNotification,
    isRead: Boolean,
    onClick: () -> Unit,
    onViewContests: () -> Unit,
    onDismiss: () -> Unit
) {
    val accentColor = notification.badgeColor
    val isHackathon = notification.type == NotificationKind.HACKATHON

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        accentColor = accentColor,
        cornerRadius = 18.dp,
        onClick = onClick
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Optional banner image
            if (notification.imageUrl.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(125.dp)
                        .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                ) {
                    AsyncImage(
                        model = notification.imageUrl,
                        contentDescription = notification.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                                )
                            )
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = accentColor.copy(alpha = 0.18f),
                            border = BorderStroke(0.8.dp, accentColor.copy(alpha = 0.40f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    if (isHackathon) Icons.Rounded.EmojiEvents else Icons.Rounded.Code,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = accentColor
                                )
                                Text(
                                    text = notification.badge.uppercase().take(22),
                                    style = Typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Black),
                                    color = accentColor
                                )
                            }
                        }

                        if (!isRead) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF10B981).copy(alpha = 0.20f)
                            ) {
                                Text(
                                    text = "NEW",
                                    style = Typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Black),
                                    color = Color(0xFF10B981),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(26.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f), CircleShape)
                    ) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = "Dismiss",
                            modifier = Modifier.size(13.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f)
                        )
                    }
                }

                // Title and Subtitle
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = notification.title,
                        style = Typography.titleSmall.copy(fontWeight = FontWeight.Black, fontSize = 14.5.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (notification.subtitle.isNotBlank()) {
                        Text(
                            text = notification.subtitle,
                            style = Typography.bodySmall.copy(fontSize = 11.5.sp, lineHeight = 16.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.80f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Specs: Timing, Prize, or Tags
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (notification.scheduleInfo.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF00E5FF).copy(alpha = 0.12f),
                            border = BorderStroke(0.6.dp, Color(0xFF00E5FF).copy(alpha = 0.30f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(11.dp),
                                    tint = Color(0xFF00E5FF)
                                )
                                Text(
                                    text = notification.scheduleInfo,
                                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                    color = Color(0xFF00E5FF)
                                )
                            }
                        }
                    }

                    if (notification.prizePool.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                            border = BorderStroke(0.6.dp, Color(0xFFF59E0B).copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.EmojiEvents,
                                    contentDescription = null,
                                    modifier = Modifier.size(11.dp),
                                    tint = Color(0xFFF59E0B)
                                )
                                Text(
                                    text = notification.prizePool,
                                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                    color = Color(0xFFF59E0B)
                                )
                            }
                        }
                    }
                }

                // Footer Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (notification.tags.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            notification.tags.take(2).forEach { tag ->
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = accentColor.copy(alpha = 0.10f)
                                ) {
                                    Text(
                                        text = tag,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp),
                                        style = Typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.SemiBold),
                                        color = accentColor
                                    )
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = accentColor,
                        onClick = onViewContests
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (isHackathon) "Explore Event" else "View in Contests",
                                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                color = Color.Black
                            )
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Refined Broadcast & Announcement Card.
 */
@Composable
private fun BroadcastNotificationCard(
    notification: AppNotification,
    isRead: Boolean,
    onClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val badgeColor = notification.badgeColor

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        accentColor = badgeColor,
        cornerRadius = 18.dp,
        onClick = onClick
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (notification.imageUrl.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(115.dp)
                        .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                ) {
                    AsyncImage(
                        model = notification.imageUrl,
                        contentDescription = notification.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))))
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header: Badge + NEW Pill + Dismiss button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(5.dp),
                            color = badgeColor.copy(alpha = 0.18f)
                        ) {
                            Text(
                                text = notification.badge.uppercase().take(22),
                                style = Typography.labelSmall.copy(fontSize = 8.5.sp, fontWeight = FontWeight.Black),
                                color = badgeColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        if (!isRead) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF10B981).copy(alpha = 0.20f)
                            ) {
                                Text(
                                    text = "NEW",
                                    style = Typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Black),
                                    color = Color(0xFF10B981),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(26.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f), CircleShape)
                    ) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = "Dismiss",
                            modifier = Modifier.size(13.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f)
                        )
                    }
                }

                // Title and Subtitle
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = notification.title,
                        style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (notification.subtitle.isNotBlank()) {
                        Text(
                            text = notification.subtitle,
                            style = Typography.bodySmall.copy(fontSize = 11.5.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (notification.tags.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            notification.tags.take(2).forEach { tag ->
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = badgeColor.copy(alpha = 0.10f)
                                ) {
                                    Text(
                                        text = tag,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp),
                                        style = Typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.SemiBold),
                                        color = badgeColor
                                    )
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = badgeColor.copy(alpha = 0.15f),
                        border = BorderStroke(0.8.dp, badgeColor.copy(alpha = 0.35f)),
                        onClick = onClick
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "View Details",
                                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                color = badgeColor
                            )
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = badgeColor
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * DSA Sheet & YouTube Playlist Card.
 */
@Composable
private fun MaterialNotificationCard(
    notification: AppNotification,
    isRead: Boolean,
    onOpenInApp: () -> Unit,
    onOpenLink: () -> Unit,
    onDismiss: () -> Unit
) {
    val accentColor = notification.badgeColor
    val isPlaylist = notification.type == NotificationKind.PLAYLIST

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        accentColor = accentColor,
        cornerRadius = 16.dp,
        onClick = onOpenInApp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Category Pill + Dismiss Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(5.dp),
                        color = accentColor.copy(alpha = 0.18f)
                    ) {
                        Text(
                            text = notification.badge.take(24),
                            style = Typography.labelSmall.copy(fontSize = 8.5.sp, fontWeight = FontWeight.Black),
                            color = accentColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (!isRead) {
                        Surface(
                            shape = RoundedCornerShape(5.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = "NEW",
                                style = Typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Black),
                                color = Color(0xFF10B981),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(26.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f), CircleShape)
                ) {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = "Dismiss",
                        modifier = Modifier.size(13.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f)
                    )
                }
            }

            // Body: Media Thumbnail + Title + Subtitle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (notification.imageUrl.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        AsyncImage(
                            model = notification.imageUrl,
                            contentDescription = notification.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(accentColor.copy(alpha = 0.14f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isPlaylist) Icons.Rounded.PlayCircle else Icons.AutoMirrored.Rounded.MenuBook,
                            contentDescription = null,
                            modifier = Modifier.size(26.dp),
                            tint = accentColor
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = notification.title,
                        style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.5.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (notification.subtitle.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = notification.subtitle,
                            style = Typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Footer: Tags + Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (notification.tags.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        notification.tags.take(2).forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = accentColor.copy(alpha = 0.10f),
                                border = BorderStroke(0.8.dp, accentColor.copy(alpha = 0.25f))
                            ) {
                                Text(
                                    text = tag,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp),
                                    style = Typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.SemiBold),
                                    color = accentColor
                                )
                            }
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (notification.actionUrl.isNotBlank()) {
                        IconButton(
                            onClick = onOpenLink,
                            modifier = Modifier
                                .size(30.dp)
                                .background(accentColor.copy(alpha = 0.14f), CircleShape)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Rounded.OpenInNew,
                                contentDescription = "Open Link",
                                modifier = Modifier.size(14.dp),
                                tint = accentColor
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = accentColor,
                        onClick = onOpenInApp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                if (isPlaylist) Icons.Rounded.PlayArrow else Icons.AutoMirrored.Rounded.MenuBook,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = Color.White
                            )
                            Text(
                                text = if (isPlaylist) "Watch Course" else "View in Resources",
                                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
