package com.mycodecalendar.core.designsystem.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycodecalendar.core.designsystem.Typography

/**
 * EmptyState — Animated glassmorphic empty state container.
 *
 * Supports optional halo icon, bold title, message, and action CTA button.
 * Fully backwards-compatible with standard message-only invocations.
 */
@Composable
fun EmptyState(
    message: String,
    modifier: Modifier = Modifier,
    title: String? = null,
    icon: ImageVector? = null,
    accentColor: Color? = null,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    val themeColor = accentColor ?: MaterialTheme.colorScheme.primary
    val pulseScale by rememberInfiniteTransition(label = "emptyPulse").animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        cornerRadius = 22.dp,
        accentColor = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 28.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Glowing Ambient Icon Halo
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .scale(pulseScale),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        themeColor.copy(alpha = 0.20f),
                                        themeColor.copy(alpha = 0.05f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(themeColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = themeColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (!title.isNullOrBlank()) {
                Text(
                    text = title,
                    style = Typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 16.5.sp,
                        letterSpacing = (-0.2).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            Text(
                text = message,
                style = Typography.bodyMedium.copy(
                    fontSize = 12.5.sp,
                    lineHeight = 18.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(0.92f)
            )

            if (!actionLabel.isNullOrBlank() && onActionClick != null) {
                Spacer(modifier = Modifier.height(18.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = themeColor,
                    shadowElevation = 2.dp,
                    modifier = Modifier.clickable { onActionClick() }
                ) {
                    Text(
                        text = actionLabel,
                        style = Typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            letterSpacing = 0.2.sp
                        ),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 9.dp)
                    )
                }
            }
        }
    }
}
