package com.mycodecalendar.core.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.mycodecalendar.core.designsystem.BrandPrimaryOrange
import com.mycodecalendar.core.designsystem.Typography

/**
 * OfflineNetworkDialog — Cyber-styled, modern frosted glass dialog
 * presenting the offline state, local cache capabilities, and quick network actions.
 */
@Composable
fun OfflineNetworkDialog(
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    onOpenSettings: () -> Unit,
    isRetrying: Boolean = false
) {
    val alertAmber = Color(0xFFF59E0B)
    val successGreen = Color(0xFF22C55E)

    val infiniteTransition = rememberInfiniteTransition(label = "halo_pulse")
    val haloScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val haloAlpha by infiniteTransition.animateFloat(
        initialValue = 0.20f,
        targetValue = 0.50f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .clip(RoundedCornerShape(26.dp))
                .border(1.dp, alertAmber.copy(alpha = 0.45f), RoundedCornerShape(26.dp))
        ) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 26.dp,
                accentColor = alertAmber
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Pulsing Cyber Icon Halo
                    Box(
                        modifier = Modifier.size(76.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Outer pulse ring
                        Box(
                            modifier = Modifier
                                .size(72.dp * haloScale)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(alertAmber.copy(alpha = haloAlpha), Color.Transparent)
                                    )
                                )
                                .border(1.dp, alertAmber.copy(alpha = haloAlpha * 0.7f), CircleShape)
                        )
                        // Inner icon core
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(alertAmber.copy(alpha = 0.16f), CircleShape)
                                .border(1.2.dp, alertAmber.copy(alpha = 0.65f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.WifiOff,
                                contentDescription = "Offline",
                                modifier = Modifier.size(24.dp),
                                tint = alertAmber
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Mode Badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = alertAmber.copy(alpha = 0.15f),
                        border = BorderStroke(0.8.dp, alertAmber.copy(alpha = 0.40f))
                    ) {
                        Text(
                            text = "OFFLINE MODE ACTIVE",
                            style = Typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                fontSize = 10.sp
                            ),
                            color = alertAmber,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "No Internet Connection",
                        style = Typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "You are currently browsing cached data. All previously loaded contests and schedules remain fully accessible.",
                        style = Typography.bodySmall.copy(
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Offline Capabilities Checklist Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.45f),
                        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.20f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CapabilityRow(
                                icon = Icons.Rounded.CheckCircle,
                                iconColor = successGreen,
                                title = "Cached Contests & Schedules",
                                status = "Available Offline"
                            )
                            CapabilityRow(
                                icon = Icons.Rounded.CheckCircle,
                                iconColor = successGreen,
                                title = "Saved Watchlist & Reminders",
                                status = "Fully Active"
                            )
                            CapabilityRow(
                                icon = Icons.Rounded.CheckCircle,
                                iconColor = successGreen,
                                title = "Practice Problem of the Day",
                                status = "Available Offline"
                            )
                            CapabilityRow(
                                icon = Icons.Rounded.Info,
                                iconColor = alertAmber,
                                title = "Live Rating & Rank Updates",
                                status = "Paused Until Reconnected"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Primary Action: Retry Connection
                    Button(
                        onClick = onRetry,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = alertAmber),
                        enabled = !isRetrying
                    ) {
                        if (isRetrying) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.Black,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Checking Connection...",
                                style = Typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp
                                ),
                                color = Color.Black
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(17.dp),
                                tint = Color.Black
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Retry Connection",
                                style = Typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp
                                ),
                                color = Color.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Secondary Action: Open Wi-Fi Settings & Dismiss
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onOpenSettings,
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Wi-Fi Settings",
                                style = Typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.5.sp
                                ),
                                maxLines = 1
                            )
                        }

                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text(
                                text = "Stay Offline",
                                style = Typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.5.sp
                                ),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CapabilityRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    status: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = title,
                style = Typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
        Text(
            text = status,
            style = Typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = iconColor
        )
    }
}

/**
 * BackOnlineBanner — Animated snackbar pill dropped when network connectivity is restored.
 */
@Composable
fun BackOnlineBanner(
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    val successGreen = Color(0xFF22C55E)

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = successGreen.copy(alpha = 0.16f),
            border = BorderStroke(1.dp, successGreen.copy(alpha = 0.55f)),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Wifi,
                    contentDescription = null,
                    tint = successGreen,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Back Online · Synchronizing latest contests",
                    style = Typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    ),
                    color = successGreen
                )
            }
        }
    }
}
