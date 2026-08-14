package com.mycodecalendar.feature.home.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycodecalendar.core.designsystem.BrandPrimaryOrange
import com.mycodecalendar.core.designsystem.Typography
import com.mycodecalendar.core.designsystem.components.GlassCard
import com.mycodecalendar.domain.model.BadgeCategory
import com.mycodecalendar.domain.model.StreakBadge

/**
 * Returns a fitting ImageVector for the badge based on its id and category.
 */
fun getBadgeIcon(badge: StreakBadge): ImageVector = when (badge.id) {
    "badge_7d"   -> Icons.Rounded.LocalFireDepartment
    "badge_14d"  -> Icons.Rounded.ElectricBolt
    "badge_30d"  -> Icons.Rounded.EmojiEvents
    "badge_50d"  -> Icons.Rounded.MilitaryTech
    "badge_100d" -> Icons.Rounded.WorkspacePremium
    else         -> if (badge.category == BadgeCategory.MONTHLY) Icons.Rounded.CalendarMonth else Icons.Rounded.Star
}

/**
 * Streak3DBadgeCard — A futuristic 3D Glassmorphism trophy card.
 *
 * Features:
 * - 3D illuminated layered glow for unlocked badges with metallic gradient ring.
 * - Minimal frosted dark glass for locked badges with live progress bar.
 * - Interactive tap to open [BadgeDetailBottomSheet].
 */
@Composable
fun Streak3DBadgeCard(
    badge: StreakBadge,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val primaryColor = Color(badge.colorHexes.firstOrNull() ?: 0xFFFF6B00)
    val secondaryColor = Color(badge.colorHexes.getOrNull(1) ?: 0xFFFF8C00)
    val badgeIcon = getBadgeIcon(badge)

    val infiniteTransition = rememberInfiniteTransition(label = "badgeShimmer")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue  = 1.04f,
        animationSpec = infiniteRepeatable(
            tween(1400, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "badgePulse"
    )

    if (badge.isUnlocked) {
        // ── UNLOCKED 3D GLASS TROPHY (24dp rounded & dreamy faded glow) ────────
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            primaryColor.copy(alpha = if (isDark) 0.16f else 0.10f),
                            secondaryColor.copy(alpha = if (isDark) 0.04f else 0.02f)
                        )
                    )
                )
                .border(
                    BorderStroke(
                        1.dp,
                        Brush.verticalGradient(
                            listOf(
                                primaryColor.copy(alpha = if (isDark) 0.60f else 0.45f),
                                secondaryColor.copy(alpha = if (isDark) 0.15f else 0.10f)
                            )
                        )
                    ),
                    RoundedCornerShape(24.dp)
                )
                .clickable(onClick = onClick)
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 3D Illuminated Medal Emblem
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .scale(pulseScale)
                        .drawBehind {
                            // Ambient radial glow behind emblem
                            drawCircle(
                                brush = Brush.radialGradient(
                                    listOf(primaryColor.copy(alpha = 0.45f), Color.Transparent),
                                    center = center,
                                    radius = size.width * 0.75f
                                )
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Outer 3D metallic gradient ring
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(primaryColor, secondaryColor)
                                )
                            )
                            .border(1.5.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        // Inner glossy glass dome
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            Color.White.copy(alpha = 0.35f),
                                            Color.Black.copy(alpha = 0.25f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = badgeIcon,
                                contentDescription = badge.title,
                                modifier = Modifier.size(24.dp),
                                tint = Color.White
                            )
                        }
                    }

                    // Verified unlock mini checkmark pill
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                            .border(1.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Check,
                            contentDescription = "Unlocked",
                            modifier = Modifier.size(11.dp),
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Title
                Text(
                    text = badge.title,
                    style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.5.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Subtitle
                Text(
                    text = badge.subtitle,
                    style = Typography.labelSmall.copy(fontSize = 11.sp),
                    color = primaryColor,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Unlocked status pill
                Surface(
                    shape = CircleShape,
                    color = primaryColor.copy(alpha = 0.14f),
                    border = BorderStroke(0.8.dp, primaryColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "Unlocked",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = Typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 9.5.sp
                        ),
                        color = primaryColor
                    )
                }
            }
        }
    } else {
        // ── LOCKED 3D FROSTED GLASS CARD (24dp rounded & faded) ──────────────
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(24.dp))
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.35f else 0.50f)
                )
                .border(
                    BorderStroke(
                        0.8.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = if (isDark) 0.15f else 0.18f)
                    ),
                    RoundedCornerShape(24.dp)
                )
                .clickable(onClick = onClick)
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Locked Medal Icon (Frosted with Lock)
                Box(
                    modifier = Modifier.size(62.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = badgeIcon,
                            contentDescription = badge.title,
                            modifier = Modifier.size(22.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.30f)
                        )
                    }

                    // Lock Icon overlay
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Lock,
                            contentDescription = "Locked",
                            modifier = Modifier.size(10.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Title
                Text(
                    text = badge.title,
                    style = Typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.70f),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Subtitle
                Text(
                    text = badge.subtitle,
                    style = Typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.50f),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Progress Bar (e.g. 3/7 Days)
                val progressFraction = (badge.currentProgress.toFloat() / badge.maxProgress.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(4.dp)
                            .clip(CircleShape),
                        color = primaryColor.copy(alpha = 0.75f),
                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${badge.currentProgress}/${badge.maxProgress} Days",
                        style = Typography.labelSmall.copy(
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                    )
                }
            }
        }
    }
}

/**
 * In-App 3D Badge Detail Bottom Sheet.
 * Displays full 3D medal illustration, unlock requirements, progress, and 1-tap share action.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgeDetailBottomSheet(
    badge: StreakBadge,
    onDismiss: () -> Unit,
    onShareBadge: (StreakBadge) -> Unit
) {
    val primaryColor = Color(badge.colorHexes.firstOrNull() ?: 0xFFFF6B00)
    val secondaryColor = Color(badge.colorHexes.getOrNull(1) ?: 0xFFFF8C00)
    val badgeIcon = getBadgeIcon(badge)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Big 3D Badge Medal Display
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(
                                    if (badge.isUnlocked) primaryColor.copy(alpha = 0.40f) else Color.Transparent,
                                    Color.Transparent
                                ),
                                center = center,
                                radius = size.width * 0.8f
                            )
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .clip(CircleShape)
                        .background(
                            if (badge.isUnlocked) Brush.linearGradient(listOf(primaryColor, secondaryColor))
                            else Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        )
                        .border(
                            2.dp,
                            if (badge.isUnlocked) Color.White.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        Color.White.copy(alpha = if (badge.isUnlocked) 0.35f else 0.10f),
                                        Color.Black.copy(alpha = 0.25f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = badgeIcon,
                            contentDescription = badge.title,
                            modifier = Modifier.size(38.dp),
                            tint = if (badge.isUnlocked) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tier Pill
            Surface(
                shape = CircleShape,
                color = if (badge.isUnlocked) primaryColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                border = BorderStroke(
                    1.dp,
                    if (badge.isUnlocked) primaryColor.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
            ) {
                Text(
                    text = badge.tierTitle.uppercase(),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                    style = Typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        fontSize = 10.sp
                    ),
                    color = if (badge.isUnlocked) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Badge Title
            Text(
                text = badge.title,
                style = Typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Text(
                text = badge.subtitle,
                style = Typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = if (badge.isUnlocked) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Description Box
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 16.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Achievement Details",
                        style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = badge.description,
                        style = Typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.10f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Status",
                            style = Typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (badge.isUnlocked) "● Unlocked (${badge.unlockedDateText ?: "Earned"})" else "○ Locked (${badge.currentProgress}/${badge.maxProgress} Days)",
                            style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (badge.isUnlocked) Color(0xFF10B981) else BrandPrimaryOrange
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Action Button (Share Trophy or Close)
            if (badge.isUnlocked) {
                Button(
                    onClick = {
                        onShareBadge(badge)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandPrimaryOrange,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        Icons.Rounded.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Share Trophy",
                        style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            } else {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "Keep Coding to Unlock",
                        style = Typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }
    }
}
