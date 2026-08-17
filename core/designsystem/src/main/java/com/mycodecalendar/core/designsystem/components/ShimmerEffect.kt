package com.mycodecalendar.core.designsystem.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

import androidx.compose.ui.graphics.luminance

/**
 * Creates a high-end diagonal 45° sweeping shimmer gradient brush with
 * micro-pulsing luminance tailored for dark OLED and crisp light themes.
 */
@Composable
fun rememberShimmerBrush(
    isDarkTheme: Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f
): Brush {
    val transition = rememberInfiniteTransition(label = "shimmerTransition")
    val translateAnim by transition.animateFloat(
        initialValue = -500f,
        targetValue = 1500f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1300,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val colors = if (isDarkTheme) {
        listOf(
            Color(0xFF0F131C),
            Color(0xFF151B28),
            Color(0xFF1E2536),
            Color(0xFF2D3748),
            Color(0xFF1E2536),
            Color(0xFF151B28),
            Color(0xFF0F131C)
        )
    } else {
        listOf(
            Color(0xFFE2E8F0),
            Color(0xFFF1F5F9),
            Color(0xFFFFFFFF),
            Color(0xFFF1F5F9),
            Color(0xFFE2E8F0)
        )
    }

    return Brush.linearGradient(
        colors = colors,
        start = Offset(translateAnim, translateAnim / 2f),
        end = Offset(translateAnim + 400f, translateAnim / 2f + 400f)
    )
}

/**
 * ShimmerBox — a single animated glassmorphic shimmer skeleton element.
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 10.dp,
    accentGlow: Color? = null
) {
    val brush = rememberShimmerBrush()
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(brush)
            .then(
                if (accentGlow != null) {
                    Modifier.border(
                        1.dp,
                        accentGlow.copy(alpha = 0.20f),
                        RoundedCornerShape(cornerRadius)
                    )
                } else Modifier
            )
    )
}

/**
 * CyberLoadingSpinner — Futuristic dual-orbiting neon spinner.
 */
@Composable
fun CyberLoadingSpinner(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = Color(0xFF38BDF8)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cyberSpinner")
    val rotateClockwise by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotateCW"
    )
    val rotateCounter by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotateCCW"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeW = (size.toPx() * 0.08f).coerceAtLeast(3f)

            // Outer primary arc
            drawArc(
                brush = Brush.sweepGradient(listOf(color.copy(alpha = 0.1f), color)),
                startAngle = rotateClockwise,
                sweepAngle = 240f,
                useCenter = false,
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )

            // Inner secondary arc
            val innerInset = strokeW * 2f
            drawArc(
                brush = Brush.sweepGradient(listOf(secondaryColor.copy(alpha = 0.1f), secondaryColor)),
                startAngle = rotateCounter,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(innerInset, innerInset),
                size = androidx.compose.ui.geometry.Size(
                    this.size.width - innerInset * 2,
                    this.size.height - innerInset * 2
                ),
                style = Stroke(width = strokeW * 0.8f, cap = StrokeCap.Round)
            )

            // Glowing center node
            drawCircle(
                color = color,
                radius = strokeW * 1.2f,
                center = center
            )
        }
    }
}

/**
 * PulsingLiveDot — An animated glowing radar dot indicator.
 */
@Composable
fun PulsingLiveDot(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF10F07B),
    size: Dp = 10.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulsingDot")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotScale"
    )

    Box(modifier = modifier.size(size * 2), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(size * scale)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.25f))
        )
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(color)
        )
    }
}

/**
 * HomeScreenSkeleton — Full state-of-the-art loading skeleton for the Home dashboard.
 */
@Composable
fun HomeScreenSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(18.dp))

        // Header Greeting & Title row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ShimmerBox(modifier = Modifier.width(110.dp).height(14.dp))
                ShimmerBox(modifier = Modifier.width(200.dp).height(28.dp), cornerRadius = 12.dp)
            }
            ShimmerBox(modifier = Modifier.size(width = 84.dp, height = 36.dp), cornerRadius = 18.dp)
        }

        Spacer(Modifier.height(6.dp))

        // Next Contest Hero Banner
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            cornerRadius = 24.dp,
            accentGlow = MaterialTheme.colorScheme.primary
        )

        // Platform Stat Cards Grid (2 columns)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ShimmerBox(modifier = Modifier.weight(1f).height(115.dp), cornerRadius = 20.dp)
            ShimmerBox(modifier = Modifier.weight(1f).height(115.dp), cornerRadius = 20.dp)
        }

        // GitHub Contribution Heatmap Card
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            cornerRadius = 20.dp,
            accentGlow = Color(0xFF34D399)
        )

        // Section header
        ShimmerBox(modifier = Modifier.width(160.dp).height(16.dp))

        // Upcoming Contest Rows
        repeat(3) {
            ContestCardSkeleton()
        }

        Spacer(Modifier.height(40.dp))
    }
}

/**
 * ContestCardSkeleton — Modern glassmorphic contest row placeholder.
 */
@Composable
fun ContestCardSkeleton() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Platform icon box
            ShimmerBox(modifier = Modifier.size(46.dp), cornerRadius = 14.dp)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShimmerBox(modifier = Modifier.fillMaxWidth(0.85f).height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ShimmerBox(modifier = Modifier.width(80.dp).height(12.dp))
                    ShimmerBox(modifier = Modifier.width(60.dp).height(12.dp))
                }
            }

            // Action / Status Pill
            ShimmerBox(modifier = Modifier.size(width = 62.dp, height = 28.dp), cornerRadius = 10.dp)
        }
    }
}

/**
 * PlatformDetailSkeleton — Loading skeleton for platform profile & rating analytics.
 */
@Composable
fun PlatformDetailSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(10.dp))
        // Top profile card
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(140.dp), cornerRadius = 24.dp)

        // Rating progression chart area
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(220.dp), cornerRadius = 24.dp)

        // Solved problems breakdown
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(180.dp), cornerRadius = 20.dp)
    }
}

/**
 * ResourceCardSkeleton — Modern glassmorphic educational card placeholder.
 */
@Composable
fun ResourceCardSkeleton() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerBox(modifier = Modifier.width(90.dp).height(22.dp), cornerRadius = 8.dp)
                ShimmerBox(modifier = Modifier.width(60.dp).height(18.dp), cornerRadius = 6.dp)
            }
            ShimmerBox(modifier = Modifier.fillMaxWidth(0.9f).height(18.dp))
            ShimmerBox(modifier = Modifier.fillMaxWidth(0.6f).height(14.dp))
        }
    }
}

/**
 * ResourcesListSkeleton — Full screen skeleton for education & playlists.
 */
@Composable
fun ResourcesListSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Category chips skeleton
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(4) {
                ShimmerBox(modifier = Modifier.width(80.dp).height(32.dp), cornerRadius = 10.dp)
            }
        }
        Spacer(Modifier.height(4.dp))
        repeat(4) {
            ResourceCardSkeleton()
        }
    }
}

/**
 * SettingsScreenSkeleton — Full screen skeleton for settings and preferences.
 */
@Composable
fun SettingsScreenSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        ShimmerBox(modifier = Modifier.width(160.dp).height(28.dp), cornerRadius = 8.dp)
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(120.dp), cornerRadius = 20.dp)
        ShimmerBox(modifier = Modifier.width(140.dp).height(20.dp), cornerRadius = 6.dp)
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(140.dp), cornerRadius = 18.dp)
        ShimmerBox(modifier = Modifier.width(120.dp).height(20.dp), cornerRadius = 6.dp)
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(160.dp), cornerRadius = 18.dp)
    }
}

/**
 * BroadcastBannerSkeleton — Glassmorphic shimmer placeholder for Home Screen announcement banner.
 */
@Composable
fun BroadcastBannerSkeleton() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.45f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ShimmerBox(modifier = Modifier.size(42.dp), cornerRadius = 12.dp)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ShimmerBox(modifier = Modifier.width(72.dp).height(16.dp), cornerRadius = 4.dp)
                ShimmerBox(modifier = Modifier.fillMaxWidth(0.85f).height(16.dp), cornerRadius = 4.dp)
                ShimmerBox(modifier = Modifier.fillMaxWidth(0.6f).height(12.dp), cornerRadius = 4.dp)
            }
        }
    }
}

/**
 * NotificationsListSkeleton — Full screen shimmering notification cards placeholder.
 */
@Composable
fun NotificationsListSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(5) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.45f))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), RoundedCornerShape(18.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ShimmerBox(modifier = Modifier.size(44.dp), cornerRadius = 12.dp)
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ShimmerBox(modifier = Modifier.width(80.dp).height(18.dp), cornerRadius = 6.dp)
                            ShimmerBox(modifier = Modifier.width(50.dp).height(14.dp), cornerRadius = 4.dp)
                        }
                        ShimmerBox(modifier = Modifier.fillMaxWidth(0.9f).height(16.dp), cornerRadius = 4.dp)
                        ShimmerBox(modifier = Modifier.fillMaxWidth(0.7f).height(12.dp), cornerRadius = 4.dp)
                    }
                }
            }
        }
    }
}


