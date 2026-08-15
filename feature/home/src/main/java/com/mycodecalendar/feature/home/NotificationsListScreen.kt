package com.mycodecalendar.feature.home

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    BROADCAST, HACKATHON, MATERIAL_ADDED, SYSTEM
}

/**
 * Full-screen Notifications List: broadcasts (hackathons, events, notices) + new CRM materials.
 * Each notification has an individual dismiss button.
 */
@Composable
fun NotificationsListScreen(
    onBackClick: () -> Unit,
    onNotificationClick: (AppNotification) -> Unit
) {
    val context = LocalContext.current
    var notifications by remember { mutableStateOf<List<AppNotification>>(emptyList()) }
    var dismissedIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val result = mutableListOf<AppNotification>()

        db.collection("broadcasts").get().addOnSuccessListener { broadcastSnap ->
            broadcastSnap.documents.forEach { doc ->
                val isActive = doc.getBoolean("isActive") ?: false
                if (!isActive) return@forEach
                val title = doc.getString("title") ?: return@forEach
                val subtitle = doc.getString("message") ?: doc.getString("subtitle") ?: ""
                val badge = doc.getString("badge") ?: "NOTICE"
                val actionUrl = doc.getString("actionUrl") ?: ""
                val bannerImageUrl = doc.getString("bannerImageUrl") ?: ""
                val prizePool = doc.getString("prizePool") ?: ""
                val tags = (doc.get("tags") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                val description = doc.getString("description") ?: subtitle
                val badgeColor = when {
                    badge.contains("HACKATHON", ignoreCase = true) -> BrandPrimaryOrange
                    badge.contains("UPDATE", ignoreCase = true) -> Color(0xFF3B82F6)
                    badge.contains("WARNING", ignoreCase = true) -> Color(0xFFF59E0B)
                    else -> BrandPrimaryOrange
                }
                val kind = when {
                    badge.contains("HACKATHON", ignoreCase = true) || badge.contains("EVENT", ignoreCase = true) -> NotificationKind.HACKATHON
                    else -> NotificationKind.BROADCAST
                }
                result.add(AppNotification(
                    id = doc.id,
                    type = kind,
                    title = title,
                    subtitle = subtitle,
                    badge = badge,
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
                        badge = badge,
                        bannerImageUrl = bannerImageUrl,
                        description = description,
                        prizePool = doc.getString("prizePool") ?: "₹2,00,000",
                        location = doc.getString("location") ?: "Online",
                        teamSize = doc.getString("teamSize") ?: "Individual",
                        timeline = doc.getString("timeline") ?: "Ongoing",
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
                    val redirectUrl = doc.getString("redirectUrl") ?: ""
                    val imageUrl = doc.getString("imageUrl") ?: ""
                    val tags = (doc.get("tags") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                    result.add(AppNotification(
                        id = "mat_${doc.id}",
                        type = NotificationKind.MATERIAL_ADDED,
                        title = title,
                        subtitle = description.take(100),
                        badge = category,
                        badgeColor = Color(0xFF10B981),
                        imageUrl = imageUrl,
                        actionUrl = redirectUrl,
                        tags = tags
                    ))
                }
                notifications = result.sortedBy { it.type.ordinal }
                isLoading = false
            }.addOnFailureListener { isLoading = false }
        }.addOnFailureListener { isLoading = false }
    }

    val visibleNotifications = notifications.filter { it.id !in dismissedIds }

    GlassmorphismBackground {
        Column(modifier = Modifier.fillMaxSize()) {
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
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
                Text("Notifications", style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 17.sp), color = MaterialTheme.colorScheme.onBackground)
                if (visibleNotifications.isNotEmpty()) {
                    GlassCard(cornerRadius = 14.dp, onClick = { dismissedIds = notifications.map { it.id }.toSet() }) {
                        Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                            Text("Clear All", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.width(70.dp))
                }
            }

            if (visibleNotifications.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.size(8.dp).background(BrandPrimaryOrange, CircleShape))
                    Text("${visibleNotifications.size} active notification${if (visibleNotifications.size > 1) "s" else ""}",
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp), color = BrandPrimaryOrange)
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BrandPrimaryOrange, modifier = Modifier.size(36.dp))
                }
            } else if (visibleNotifications.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Rounded.NotificationsOff, contentDescription = null, modifier = Modifier.size(50.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.40f))
                        Text("You are all caught up!", style = Typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.60f))
                        Text("No new notifications from the admin team.", style = Typography.bodySmall.copy(fontSize = 12.sp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f))
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val hackathonItems = visibleNotifications.filter { it.type == NotificationKind.HACKATHON || it.type == NotificationKind.BROADCAST }
                    val materialItems = visibleNotifications.filter { it.type == NotificationKind.MATERIAL_ADDED }

                    if (hackathonItems.isNotEmpty()) {
                        item { NotificationSectionHeader(icon = "🔔", title = "Broadcasts & Events", count = hackathonItems.size) }
                        items(hackathonItems, key = { it.id }) { notif ->
                            AnimatedVisibility(visible = notif.id !in dismissedIds, exit = shrinkVertically() + fadeOut()) {
                                NotificationListCard(notification = notif, onClick = { onNotificationClick(notif) }, onDismiss = { dismissedIds = dismissedIds + notif.id })
                            }
                        }
                    }

                    if (materialItems.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(6.dp))
                            NotificationSectionHeader(icon = "📚", title = "New Materials Added", count = materialItems.size)
                        }
                        items(materialItems, key = { it.id }) { notif ->
                            AnimatedVisibility(visible = notif.id !in dismissedIds, exit = shrinkVertically() + fadeOut()) {
                                MaterialNotificationCard(
                                    notification = notif,
                                    onClick = { if (notif.actionUrl.isNotBlank()) context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(notif.actionUrl))) },
                                    onDismiss = { dismissedIds = dismissedIds + notif.id }
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
private fun NotificationSectionHeader(icon: String, title: String, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = icon, fontSize = 15.sp)
        Text(text = title, style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Surface(shape = CircleShape, color = BrandPrimaryOrange.copy(alpha = 0.18f)) {
            Text(text = count.toString(), modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp), style = Typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 10.sp), color = BrandPrimaryOrange)
        }
    }
}

@Composable
private fun NotificationListCard(notification: AppNotification, onClick: () -> Unit, onDismiss: () -> Unit) {
    val badgeColor = notification.badgeColor
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).border(1.dp, badgeColor.copy(alpha = 0.30f), RoundedCornerShape(18.dp))
    ) {
        GlassCard(modifier = Modifier.fillMaxWidth(), accentColor = badgeColor, cornerRadius = 18.dp, onClick = onClick) {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (notification.imageUrl.isNotBlank()) {
                    Box(modifier = Modifier.fillMaxWidth().height(110.dp).clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))) {
                        AsyncImage(model = notification.imageUrl, contentDescription = notification.title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f)))))
                    }
                }
                Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier.size(40.dp).background(badgeColor.copy(alpha = 0.14f), CircleShape).border(1.dp, badgeColor.copy(alpha = 0.35f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        val icon = when (notification.type) {
                            NotificationKind.HACKATHON -> Icons.Rounded.EmojiEvents
                            NotificationKind.BROADCAST -> Icons.Rounded.Notifications
                            NotificationKind.MATERIAL_ADDED -> Icons.Rounded.MenuBook
                            NotificationKind.SYSTEM -> Icons.Rounded.Info
                        }
                        Icon(icon, contentDescription = null, modifier = Modifier.size(19.dp), tint = badgeColor)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(shape = RoundedCornerShape(4.dp), color = badgeColor.copy(alpha = 0.18f)) {
                                Text(notification.badge.uppercase().take(18), style = Typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Black), color = badgeColor, modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp))
                            }
                            if (notification.isNew) {
                                Surface(shape = CircleShape, color = Color(0xFF10B981).copy(alpha = 0.20f)) {
                                    Text("NEW", style = Typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Black), color = Color(0xFF10B981), modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(notification.title, style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp), color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        if (notification.subtitle.isNotBlank()) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(notification.subtitle, style = Typography.bodySmall.copy(fontSize = 11.5.sp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                        if (notification.prizePool.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFF59E0B).copy(alpha = 0.15f), border = BorderStroke(0.8.dp, Color(0xFFF59E0B).copy(alpha = 0.35f))) {
                                Row(modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Rounded.EmojiEvents, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFFF59E0B))
                                    Text(notification.prizePool, style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.5.sp), color = Color(0xFFF59E0B))
                                }
                            }
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(30.dp).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f), CircleShape)
                    ) {
                        Icon(Icons.Rounded.Close, contentDescription = "Dismiss", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.60f))
                    }
                }
            }
        }
    }
}

@Composable
private fun MaterialNotificationCard(notification: AppNotification, onClick: () -> Unit, onDismiss: () -> Unit) {
    val green = Color(0xFF10B981)
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).border(1.dp, green.copy(alpha = 0.28f), RoundedCornerShape(16.dp))) {
        GlassCard(modifier = Modifier.fillMaxWidth(), accentColor = green, cornerRadius = 16.dp, onClick = onClick) {
            Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (notification.imageUrl.isNotBlank()) {
                    Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(10.dp)).border(1.dp, green.copy(alpha = 0.3f), RoundedCornerShape(10.dp))) {
                        AsyncImage(model = notification.imageUrl, contentDescription = notification.title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    }
                } else {
                    Box(modifier = Modifier.size(52.dp).background(green.copy(alpha = 0.14f), RoundedCornerShape(10.dp)).border(1.dp, green.copy(alpha = 0.30f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.MenuBook, contentDescription = null, modifier = Modifier.size(22.dp), tint = green)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(shape = RoundedCornerShape(4.dp), color = green.copy(alpha = 0.18f)) {
                            Text(notification.badge.uppercase().take(20), style = Typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Black), color = green, modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp))
                        }
                        Surface(shape = RoundedCornerShape(4.dp), color = green.copy(alpha = 0.14f)) {
                            Text("JUST ADDED", style = Typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Black), color = green, modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(notification.title, style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp), color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    if (notification.subtitle.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(notification.subtitle, style = Typography.bodySmall.copy(fontSize = 11.sp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                    if (notification.tags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            notification.tags.take(2).forEach { tag ->
                                Surface(shape = RoundedCornerShape(4.dp), color = green.copy(alpha = 0.10f), border = BorderStroke(0.8.dp, green.copy(alpha = 0.25f))) {
                                    Text(tag, modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp), style = Typography.labelSmall.copy(fontSize = 9.sp), color = green)
                                }
                            }
                        }
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = onClick, modifier = Modifier.size(32.dp).background(green.copy(alpha = 0.14f), CircleShape)) {
                        Icon(Icons.Rounded.OpenInNew, contentDescription = "Open", modifier = Modifier.size(15.dp), tint = green)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), CircleShape)) {
                        Icon(Icons.Rounded.Close, contentDescription = "Dismiss", modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f))
                    }
                }
            }
        }
    }
}
