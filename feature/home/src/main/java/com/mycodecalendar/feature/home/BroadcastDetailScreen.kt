package com.mycodecalendar.feature.home

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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

/**
 * Dedicated, adaptive Announcement & Broadcast Detail Screen.
 * Automatically tailors its layout depending on whether the item is:
 * - General Announcement / Message from Admin
 * - Welcome / Thank You for Installing Notice
 * - System & Feature Release Update
 * - Hackathon / Coding Contest with Stages & Deadlines
 */
@Composable
fun BroadcastDetailScreen(
    broadcast: CloudBroadcastBanner?,
    onBackClick: () -> Unit,
    onOpenUrl: (String) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val item = broadcast ?: CloudBroadcastBanner(
        id = "default_broadcast",
        title = "Welcome to MyCodeCalendar!",
        subtitle = "Thank you for installing the ultimate competitive programming companion.",
        badge = "WELCOME",
        description = "MyCodeCalendar empowers you to track live and upcoming contests across LeetCode, Codeforces, AtCoder, CodeChef, and more.\n\nMaintain your daily coding streak, explore top-tier curated DSA sheets, and receive timely alerts before your favorite contests begin."
    )

    val isHackathon = (item.badge.contains("HACKATHON", ignoreCase = true) ||
            item.badge.contains("CONTEST", ignoreCase = true) ||
            item.title.contains("Hackathon", ignoreCase = true)) &&
            item.prizePool.isNotBlank()

    val isWelcome = item.badge.contains("WELCOME", ignoreCase = true) ||
            item.title.contains("Welcome", ignoreCase = true) ||
            item.title.contains("Thank you", ignoreCase = true) ||
            item.title.contains("Thanks", ignoreCase = true)

    val isUpdate = item.badge.contains("UPDATE", ignoreCase = true) ||
            item.badge.contains("RELEASE", ignoreCase = true) ||
            item.badge.contains("FEATURE", ignoreCase = true) ||
            item.badge.contains("VERSION", ignoreCase = true)

    val isWarning = item.badge.contains("WARNING", ignoreCase = true) ||
            item.badge.contains("ALERT", ignoreCase = true) ||
            item.badge.contains("URGENT", ignoreCase = true)

    val themeColor: Color = when {
        isHackathon -> BrandPrimaryOrange
        isWelcome -> Color(0xFF10B981) // Emerald Green
        isUpdate -> Color(0xFF3B82F6)  // Electric Blue
        isWarning -> Color(0xFFF59E0B) // Amber
        item.badge.contains("TIPS", ignoreCase = true) -> Color(0xFF8B5CF6) // Purple
        else -> BrandPrimaryOrange
    }

    val heroIcon: ImageVector = when {
        isHackathon -> Icons.Rounded.EmojiEvents
        isWelcome -> Icons.Rounded.Celebration
        isUpdate -> Icons.Rounded.NewReleases
        isWarning -> Icons.Rounded.Warning
        item.badge.contains("TIPS", ignoreCase = true) -> Icons.Rounded.Lightbulb
        else -> Icons.Rounded.Campaign
    }

    val screenTitle: String = when {
        isHackathon -> "Event & Hackathon"
        isWelcome -> "Welcome to MyCodeCalendar"
        isUpdate -> "Release & What's New"
        isWarning -> "Important Notice"
        else -> "Announcement & News"
    }

    val onShareClick = {
        val shareBody = buildString {
            append("📢 ${item.title}\n\n")
            if (item.subtitle.isNotBlank()) {
                append("${item.subtitle}\n\n")
            }
            if (item.description.isNotBlank() && item.description != item.subtitle) {
                append("${item.description}\n\n")
            }
            if (isHackathon && item.prizePool.isNotBlank()) {
                append("🏆 Prizes: ${item.prizePool}\n")
            }
            if (item.location.isNotBlank()) {
                append("📍 Location: ${item.location}\n")
            }
            if (item.timeline.isNotBlank()) {
                append("📅 Timeline: ${item.timeline}\n")
            }
            if (item.actionUrl.isNotBlank() && item.actionUrl != "#") {
                append("\n🔗 Learn more: ${item.actionUrl}")
            }
        }
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, item.title)
            putExtra(Intent.EXTRA_TEXT, shareBody)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Announcement"))
    }

    GlassmorphismBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(bottom = 96.dp)
            ) {
                // ── TOP APP BAR ──────────────────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlassCard(
                        cornerRadius = 20.dp,
                        onClick = onBackClick
                    ) {
                        Box(
                            modifier = Modifier.size(42.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Text(
                        text = screenTitle,
                        style = Typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    GlassCard(
                        cornerRadius = 20.dp,
                        onClick = onShareClick
                    ) {
                        Box(
                            modifier = Modifier.size(42.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.Share,
                                contentDescription = "Share",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // ── HERO POSTER / BANNER IMAGE ───────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 6.dp)
                ) {
                    if (item.bannerImageUrl.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(210.dp)
                                .clip(RoundedCornerShape(26.dp))
                                .border(
                                    1.2.dp,
                                    themeColor.copy(alpha = 0.35f),
                                    RoundedCornerShape(26.dp)
                                )
                                .shadow(16.dp, RoundedCornerShape(26.dp), spotColor = themeColor.copy(alpha = 0.25f))
                        ) {
                            AsyncImage(
                                model = item.bannerImageUrl,
                                contentDescription = item.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            // Subtle gradient overlay for readability
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                                        )
                                    )
                            )
                            // Badge in Banner
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = themeColor
                                ) {
                                    Text(
                                        text = item.badge.uppercase(),
                                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 10.sp),
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.Black.copy(alpha = 0.65f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(themeColor, CircleShape)
                                        )
                                        Text(
                                            text = "OFFICIAL BROADCAST",
                                            style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.5.sp),
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Neon Frosted Hero Card with Ambient Icon
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 24.dp,
                            accentColor = themeColor
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                themeColor.copy(alpha = 0.22f),
                                                themeColor.copy(alpha = 0.05f),
                                                Color.Transparent
                                            ),
                                            radius = 500f
                                        )
                                    )
                                    .padding(22.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = themeColor.copy(alpha = 0.20f),
                                            border = BorderStroke(1.dp, themeColor.copy(alpha = 0.40f))
                                        ) {
                                            Text(
                                                text = item.badge.uppercase(),
                                                style = Typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 10.sp),
                                                color = themeColor,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .background(themeColor.copy(alpha = 0.15f), CircleShape)
                                                .border(1.dp, themeColor.copy(alpha = 0.35f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = heroIcon,
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp),
                                                tint = themeColor
                                            )
                                        }
                                    }

                                    Text(
                                        text = item.title,
                                        style = Typography.headlineSmall.copy(
                                            fontWeight = FontWeight.Black,
                                            fontSize = 21.sp,
                                            lineHeight = 27.sp,
                                            letterSpacing = (-0.3).sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ── TITLE & SUBTITLE SECTION (IF BANNER IMAGE USED) ──────────────────
                if (item.bannerImageUrl.isNotBlank()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    ) {
                        Text(
                            text = item.title,
                            style = Typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp,
                                letterSpacing = (-0.4).sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        if (item.subtitle.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = item.subtitle,
                                style = Typography.bodyMedium.copy(
                                    fontSize = 13.5.sp,
                                    lineHeight = 19.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                } else if (item.subtitle.isNotBlank()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    ) {
                        Text(
                            text = item.subtitle,
                            style = Typography.bodyMedium.copy(
                                fontSize = 13.5.sp,
                                lineHeight = 19.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // ══════════════════════════════════════════════════════════════════════
                // ── CONDITIONAL LAYOUT: HACKATHON VS GENERAL ANNOUNCEMENT ─────────────
                // ══════════════════════════════════════════════════════════════════════

                if (isHackathon) {
                    // ── HACKATHON METRICS GRID ─────────────────────────────────────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (item.prizePool.isNotBlank()) {
                            GlassCard(
                                modifier = Modifier.weight(1f),
                                cornerRadius = 18.dp,
                                accentColor = Color(0xFFF59E0B)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.EmojiEvents,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = Color(0xFFF59E0B)
                                    )
                                    Text(
                                        text = "Prizes Worth",
                                        style = Typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = item.prizePool,
                                        style = Typography.titleSmall.copy(fontWeight = FontWeight.Black, fontSize = 14.sp),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        if (item.teamSize.isNotBlank()) {
                            GlassCard(
                                modifier = Modifier.weight(1f),
                                cornerRadius = 18.dp,
                                accentColor = Color(0xFF3B82F6)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.Groups,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = Color(0xFF3B82F6)
                                    )
                                    Text(
                                        text = "Team Size",
                                        style = Typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = item.teamSize,
                                        style = Typography.titleSmall.copy(fontWeight = FontWeight.Black, fontSize = 14.sp),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    if (item.timeline.isNotBlank() || item.location.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (item.timeline.isNotBlank()) {
                                GlassCard(
                                    modifier = Modifier.weight(1f),
                                    cornerRadius = 18.dp,
                                    accentColor = BrandPrimaryOrange
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            Icons.Rounded.DateRange,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = BrandPrimaryOrange
                                        )
                                        Text(
                                            text = "Timeline",
                                            style = Typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                        Text(
                                            text = item.timeline,
                                            style = Typography.titleSmall.copy(fontWeight = FontWeight.Black, fontSize = 12.5.sp),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            if (item.location.isNotBlank()) {
                                GlassCard(
                                    modifier = Modifier.weight(1f),
                                    cornerRadius = 18.dp,
                                    accentColor = Color(0xFF10B981)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            Icons.Rounded.LocationOn,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = Color(0xFF10B981)
                                        )
                                        Text(
                                            text = "Venue / Mode",
                                            style = Typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                        Text(
                                            text = item.location,
                                            style = Typography.titleSmall.copy(fontWeight = FontWeight.Black, fontSize = 12.5.sp),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // ── HACKATHON ABOUT & GUIDELINES ───────────────────────────────────
                    val descriptionText = item.description.ifBlank { item.subtitle }
                    if (descriptionText.isNotBlank()) {
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp),
                            cornerRadius = 22.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.Info,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = BrandPrimaryOrange
                                    )
                                    Text(
                                        text = "About & Guidelines",
                                        style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Text(
                                    text = descriptionText,
                                    style = Typography.bodyMedium.copy(fontSize = 13.5.sp, lineHeight = 21.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                } else {
                    // ══════════════════════════════════════════════════════════════════
                    // ── GENERAL ANNOUNCEMENT / NEWS / WELCOME / NOTICE LAYOUT ─────────
                    // ══════════════════════════════════════════════════════════════════

                    val messageBody = item.description.ifBlank { item.subtitle }

                    // ── MAIN MESSAGE CONTENT CARD ──────────────────────────────────────
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp),
                        cornerRadius = 22.dp,
                        accentColor = themeColor
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(themeColor.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = heroIcon,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = themeColor
                                    )
                                }

                                Text(
                                    text = when {
                                        isWelcome -> "Welcome Message"
                                        isUpdate -> "Release Highlights"
                                        isWarning -> "Important Notification"
                                        else -> "Announcement Details"
                                    },
                                    style = Typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                            )

                            // Render message with paragraphs
                            val paragraphs = messageBody.split("\n\n").filter { it.isNotBlank() }
                            if (paragraphs.isNotEmpty()) {
                                paragraphs.forEach { para ->
                                    Text(
                                        text = para.trim(),
                                        style = Typography.bodyMedium.copy(
                                            fontSize = 14.sp,
                                            lineHeight = 22.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.90f)
                                    )
                                }
                            } else {
                                Text(
                                    text = messageBody,
                                    style = Typography.bodyMedium.copy(
                                        fontSize = 14.sp,
                                        lineHeight = 22.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.90f)
                                )
                            }
                        }
                    }

                    // ── WELCOME ONBOARDING HIGHLIGHTS BOX ──────────────────────────────
                    if (isWelcome) {
                        Spacer(modifier = Modifier.height(14.dp))
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp),
                            cornerRadius = 22.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Key Features at a Glance",
                                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                FeatureHighlightRow(
                                    icon = Icons.Rounded.Radar,
                                    iconColor = BrandPrimaryOrange,
                                    title = "Live Contest Radar",
                                    desc = "Track upcoming and ongoing challenges from LeetCode, Codeforces, AtCoder & CodeChef."
                                )

                                FeatureHighlightRow(
                                    icon = Icons.Rounded.LocalFireDepartment,
                                    iconColor = Color(0xFFF59E0B),
                                    title = "Daily Coding Streak",
                                    desc = "Stay disciplined and build daily problem-solving momentum with real-time sync."
                                )

                                FeatureHighlightRow(
                                    icon = Icons.Rounded.MenuBook,
                                    iconColor = Color(0xFF10B981),
                                    title = "Curated DSA Materials",
                                    desc = "Explore blind 75/150 patterns, Striver sheets, and interview roadmaps."
                                )

                                FeatureHighlightRow(
                                    icon = Icons.Rounded.NotificationsActive,
                                    iconColor = Color(0xFF3B82F6),
                                    title = "Smart Calendar Reminders",
                                    desc = "Add contests to system calendar and set 15-minute start reminders."
                                )
                            }
                        }
                    }

                    // ── CONDITIONAL METADATA (TIMELINE / LOCATION) ────────────────────
                    if (item.timeline.isNotBlank() || item.location.isNotBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (item.timeline.isNotBlank()) {
                                GlassCard(
                                    modifier = Modifier.weight(1f),
                                    cornerRadius = 18.dp
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            Icons.Rounded.DateRange,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = themeColor
                                        )
                                        Text(
                                            text = "Date / Timeline",
                                            style = Typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                        Text(
                                            text = item.timeline,
                                            style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            if (item.location.isNotBlank()) {
                                GlassCard(
                                    modifier = Modifier.weight(1f),
                                    cornerRadius = 18.dp
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            Icons.Rounded.LocationOn,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = themeColor
                                        )
                                        Text(
                                            text = "Location / Mode",
                                            style = Typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                        Text(
                                            text = item.location,
                                            style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ── TAGS / TOPIC PILLS (ONLY IF PRESENT) ─────────────────────────────
                if (item.tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp),
                        cornerRadius = 22.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = if (isHackathon) "Eligible Tracks & Themes" else "Topic Tags",
                                style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.5.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            OptInFlowRow(tags = item.tags, accentColor = themeColor)
                        }
                    }
                }
            }

            // ── FLOATING ACTION BAR AT BOTTOM ────────────────────────────────────────
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                if (item.actionUrl.isNotBlank() && item.actionUrl != "#") {
                    Button(
                        onClick = {
                            onOpenUrl(item.actionUrl)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = themeColor.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = themeColor,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isHackathon) "Register / Open Official Portal" else "Explore Link / Learn More",
                            style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        )
                    }
                } else {
                    Button(
                        onClick = onBackClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = themeColor.copy(alpha = 0.35f)),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = themeColor,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Got It",
                            style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureHighlightRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(iconColor.copy(alpha = 0.16f), CircleShape)
                .border(1.dp, iconColor.copy(alpha = 0.35f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = iconColor
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.5.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = desc,
                style = Typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
            )
        }
    }
}

@Composable
private fun OptInFlowRow(tags: List<String>, accentColor: Color = BrandPrimaryOrange) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                tags.take(3).forEach { tag ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = accentColor.copy(alpha = 0.12f),
                        border = BorderStroke(0.8.dp, accentColor.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = tag,
                            style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                            color = accentColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            if (tags.size > 3) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    tags.drop(3).forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        ) {
                            Text(
                                text = tag,
                                style = Typography.labelSmall.copy(fontWeight = FontWeight.Medium, fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
