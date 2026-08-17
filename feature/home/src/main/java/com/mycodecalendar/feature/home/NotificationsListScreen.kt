package com.mycodecalendar.feature.home

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
    val tags: List<String> = emptyList(),
    val broadcast: CloudBroadcastBanner? = null,
    val isNew: Boolean = true
)

enum class NotificationKind {
    BROADCAST, HACKATHON, MATERIAL_ADDED, PLAYLIST, SYSTEM
}

private val defaultNotifications = listOf(
    AppNotification(
        id = "default_hackathon_1",
        type = NotificationKind.HACKATHON,
        title = "Innovik 6.0 — National AI Hackathon",
        subtitle = "Live registrations on Unstop · ₹2,00,000 Prize Pool · Offline Grand Finale at VITM Indore",
        badge = "HACKATHON",
        badgeColor = BrandPrimaryOrange,
        actionUrl = "https://unstop.com",
        prizePool = "₹2,00,000",
        tags = listOf("Applied AI", "Agentic AI", "Hackathon", "₹2 Lakhs"),
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
        tags = listOf("Striver", "A2Z DSA", "Top Pick")
    ),
    AppNotification(
        id = "default_playlist_1",
        type = NotificationKind.PLAYLIST,
        title = "TakeUForward Graph & DP Masterclass",
        subtitle = "Comprehensive video series covering Dynamic Programming and Graph Algorithms step-by-step.",
        badge = "YOUTUBE PLAYLIST",
        badgeColor = Color(0xFFEF4444),
        actionUrl = "https://www.youtube.com/@takeUforward",
        tags = listOf("YouTube", "DP Series", "Graphs")
    ),
    AppNotification(
        id = "default_material_2",
        type = NotificationKind.MATERIAL_ADDED,
        title = "NeetCode 150 & Blind 75 Sheet",
        subtitle = "Core pattern-based coding interview roadmap for LeetCode practice.",
        badge = "DSA SHEET",
        badgeColor = Color(0xFF10B981),
        actionUrl = "https://neetcode.io/practice",
        tags = listOf("NeetCode", "Blind 75", "Interview Prep")
    ),
    AppNotification(
        id = "default_radar_2",
        type = NotificationKind.SYSTEM,
        title = "Contest Radar Synchronized",
        subtitle = "Tracking upcoming and active contests across LeetCode, Codeforces, AtCoder, and CodeChef.",
        badge = "SYSTEM",
        badgeColor = Color(0xFF3B82F6),
        actionUrl = "https://codeforces.com/contests",
        tags = listOf("Radar", "Live Sync")
    )
)

/**
 * Clean, modern Notification Center:
 * - Broadcasts & Announcements
 * - Hackathons
 * - Newly added Sheets, Resources, and YouTube Playlists
 * - 100% Emoji-free with crisp vector icons & modern frosted glass design
 * - Smart In-App routing to Resources tab
 */
@Composable
fun NotificationsListScreen(
    onBackClick: () -> Unit,
    onNotificationClick: (AppNotification) -> Unit,
    onOpenResource: () -> Unit = {}
) {
    val context = LocalContext.current
    val notifPrefs = remember { context.getSharedPreferences("app_notif_prefs", android.content.Context.MODE_PRIVATE) }

    var notifications by remember { mutableStateOf<List<AppNotification>>(defaultNotifications) }
    var dismissedIds by remember {
        mutableStateOf(notifPrefs.getStringSet("dismissed_notif_ids", emptySet()) ?: emptySet())
    }
    var isLoading by remember { mutableStateOf(true) }
    var selectedCategoryIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val result = mutableListOf<AppNotification>()

        try {
            db.collection("broadcasts").get().addOnSuccessListener { broadcastSnap ->
                broadcastSnap.documents.forEach { doc ->
                    val isActive = doc.getBoolean("isActive") ?: false
                    if (!isActive) return@forEach

                    // Check expiry date
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
                    val badgeColor = when {
                        rawBadge.contains("HACKATHON", ignoreCase = true) -> BrandPrimaryOrange
                        rawBadge.contains("UPDATE", ignoreCase = true) -> Color(0xFF3B82F6)
                        rawBadge.contains("WARNING", ignoreCase = true) -> Color(0xFFF59E0B)
                        else -> BrandPrimaryOrange
                    }
                    val kind = when {
                        rawBadge.contains("HACKATHON", ignoreCase = true) || rawBadge.contains("EVENT", ignoreCase = true) -> NotificationKind.HACKATHON
                        else -> NotificationKind.BROADCAST
                    }
                    result.add(AppNotification(
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
                    ))
                }

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

                        result.add(AppNotification(
                            id = "mat_${doc.id}",
                            type = kind,
                            title = title,
                            subtitle = description.take(120),
                            badge = badgeLabel,
                            badgeColor = badgeColor,
                            imageUrl = imageUrl,
                            actionUrl = redirectUrl,
                            tags = tags
                        ))
                    }
                    if (result.isNotEmpty()) {
                        notifications = (result + defaultNotifications).distinctBy { it.id }.sortedBy { it.type.ordinal }
                    }
                    isLoading = false
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
            1 -> visibleNotifications.filter { it.type == NotificationKind.BROADCAST || it.type == NotificationKind.SYSTEM }
            2 -> visibleNotifications.filter { it.type == NotificationKind.HACKATHON }
            3 -> visibleNotifications.filter { it.type == NotificationKind.MATERIAL_ADDED || it.type == NotificationKind.PLAYLIST }
            else -> visibleNotifications
        }
    }

    val broadcastCount = visibleNotifications.count { it.type == NotificationKind.BROADCAST || it.type == NotificationKind.SYSTEM }
    val hackathonCount = visibleNotifications.count { it.type == NotificationKind.HACKATHON }
    val materialCount = visibleNotifications.count { it.type == NotificationKind.MATERIAL_ADDED || it.type == NotificationKind.PLAYLIST }

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

                Text(
                    text = "Notification Center",
                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Black, fontSize = 18.sp),
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (visibleNotifications.isNotEmpty()) {
                    GlassCard(
                        cornerRadius = 14.dp,
                        onClick = {
                            val allIds = notifications.map { it.id }.toSet()
                            dismissedIds = allIds
                            notifPrefs.edit().putStringSet("dismissed_notif_ids", allIds).apply()
                        }
                    ) {
                        Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                            Text(
                                text = "Clear All",
                                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.5.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.width(60.dp))
                }
            }

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
                        label = "Broadcasts ($broadcastCount)",
                        selected = selectedCategoryIndex == 1,
                        accentColor = Color(0xFF3B82F6),
                        onClick = { selectedCategoryIndex = 1 }
                    )
                }
                item {
                    GlassChip(
                        label = "Hackathons ($hackathonCount)",
                        selected = selectedCategoryIndex == 2,
                        accentColor = BrandPrimaryOrange,
                        onClick = { selectedCategoryIndex = 2 }
                    )
                }
                item {
                    GlassChip(
                        label = "Sheets & Playlists ($materialCount)",
                        selected = selectedCategoryIndex == 3,
                        accentColor = Color(0xFF10B981),
                        onClick = { selectedCategoryIndex = 3 }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (isLoading) {
                NotificationsListSkeleton()
            } else if (filteredNotifications.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.Rounded.NotificationsOff,
                            contentDescription = null,
                            modifier = Modifier.size(54.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.40f)
                        )
                        Text(
                            text = "You're all caught up!",
                            style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f)
                        )
                        Text(
                            text = "No active notifications in this category.",
                            style = Typography.bodySmall.copy(fontSize = 12.5.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.50f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val hackathonItems = filteredNotifications.filter { it.type == NotificationKind.HACKATHON || it.type == NotificationKind.BROADCAST || it.type == NotificationKind.SYSTEM }
                    val materialItems = filteredNotifications.filter { it.type == NotificationKind.MATERIAL_ADDED || it.type == NotificationKind.PLAYLIST }

                    if (hackathonItems.isNotEmpty()) {
                        item {
                            NotificationSectionHeader(
                                icon = Icons.Rounded.Campaign,
                                title = "Broadcasts & Hackathons",
                                count = hackathonItems.size
                            )
                        }
                        items(hackathonItems, key = { it.id }) { notif ->
                            AnimatedVisibility(
                                visible = notif.id !in dismissedIds,
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                NotificationListCard(
                                    notification = notif,
                                    onClick = { onNotificationClick(notif) },
                                    onDismiss = {
                                        val updated = dismissedIds + notif.id
                                        dismissedIds = updated
                                        notifPrefs.edit().putStringSet("dismissed_notif_ids", updated).apply()
                                    }
                                )
                            }
                        }
                    }

                    if (materialItems.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(6.dp))
                            NotificationSectionHeader(
                                icon = Icons.AutoMirrored.Rounded.MenuBook,
                                title = "DSA Sheets & Video Playlists",
                                count = materialItems.size
                            )
                        }
                        items(materialItems, key = { it.id }) { notif ->
                            AnimatedVisibility(
                                visible = notif.id !in dismissedIds,
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                MaterialNotificationCard(
                                    notification = notif,
                                    onOpenInApp = onOpenResource,
                                    onOpenLink = {
                                        if (notif.actionUrl.isNotBlank()) {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(notif.actionUrl)))
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

@Composable
private fun NotificationSectionHeader(
    icon: ImageVector,
    title: String,
    count: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(17.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = title,
            style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Surface(
            shape = CircleShape,
            color = BrandPrimaryOrange.copy(alpha = 0.18f)
        ) {
            Text(
                text = count.toString(),
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                style = Typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 10.sp),
                color = BrandPrimaryOrange
            )
        }
    }
}

@Composable
private fun NotificationListCard(
    notification: AppNotification,
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
                        if (notification.isNew) {
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

                    // Subtle Cancel / Dismiss Button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(28.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f), CircleShape)
                    ) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = "Dismiss Notification",
                            modifier = Modifier.size(14.dp),
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

                // Footer: Prize / Tags + Action Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (notification.prizePool.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                            border = BorderStroke(0.8.dp, Color(0xFFF59E0B).copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.EmojiEvents,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = Color(0xFFF59E0B)
                                )
                                Text(
                                    text = notification.prizePool,
                                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.5.sp),
                                    color = Color(0xFFF59E0B)
                                )
                            }
                        }
                    } else if (notification.tags.isNotEmpty()) {
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

                    // Sleek Action Button
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
                                text = if (notification.type == NotificationKind.HACKATHON) "View Event" else "View Details",
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

@Composable
private fun MaterialNotificationCard(
    notification: AppNotification,
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
                    Surface(
                        shape = RoundedCornerShape(5.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = "NEW RESOURCE",
                            style = Typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Black),
                            color = Color(0xFF10B981),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Cancel / Dismiss button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(28.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f), CircleShape)
                ) {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = "Dismiss",
                        modifier = Modifier.size(13.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f)
                    )
                }
            }

            // Body: Icon/Image + Title + Subtitle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (notification.imageUrl.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(10.dp))
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
                            .size(48.dp)
                            .background(accentColor.copy(alpha = 0.14f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isPlaylist) Icons.Rounded.PlayCircle else Icons.AutoMirrored.Rounded.MenuBook,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
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

            // Footer: Tags + Direct "View in Resources" Button
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
                                Icons.AutoMirrored.Rounded.MenuBook,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = Color.White
                            )
                            Text(
                                text = "View in Resources",
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
