package com.mycodecalendar.core.designsystem.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycodecalendar.core.designsystem.isAppInDarkTheme
import com.mycodecalendar.domain.model.ContestStatus

// ── Premium Status Color Tokens ───────────────────────────────────────────────

// LIVE — Electric Ruby Crimson (Ultra-visible, universally recognized)
private val LiveRubyPrimary = Color(0xFFFF2A55)
private val LiveRubyDarkBg  = Color(0xFF280B12)
private val LiveRubyLightBg = Color(0xFFFFECEF)

// UPCOMING — Electric Indigo-Violet
private val UpcomingPrimary = Color(0xFF818CF8)  // indigo-400
private val UpcomingViolet  = Color(0xFFA78BFA)  // violet-400
private val UpcomingDarkBg  = Color(0xFF0F0F2E)
private val UpcomingLightBg = Color(0xFFEEF2FF)

// ENDED — Muted warm grey
private val EndedColor    = Color(0xFF64748B)
private val EndedDarkBg   = Color(0xFF141B26)
private val EndedLightBg  = Color(0xFFF1F5F9)

/**
 * StatusChip — Premium contest status pill with vivid, high-contrast glass colors.
 *
 * - LIVE:     Ultra-visible Electric Ruby Crimson with animated pulsing beacon and pure-white text
 * - UPCOMING: Deep indigo-violet gradient badge
 * - ENDED:    Subtle warm slate — unobtrusive but readable
 */
@Composable
fun StatusChip(
    status: ContestStatus,
    modifier: Modifier = Modifier
) {
    val isDark = isAppInDarkTheme
    val isLive = status == ContestStatus.LIVE

    // Animate LIVE pulsing glow
    val infiniteTransition = rememberInfiniteTransition(label = "livePulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue  = 1.6f,
        animationSpec = infiniteRepeatable(
            tween(1000, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue  = 0.12f,
        animationSpec = infiniteRepeatable(
            tween(1000, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    if (isLive) {
        // High-contrast, ultra-visible Electric Ruby Crimson pill
        Row(
            modifier = modifier
                .clip(CircleShape)
                .background(
                    if (isDark) Color(0xFFFF2A55).copy(alpha = 0.18f)
                    else Color(0xFFFF2A55).copy(alpha = 0.12f)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier.size(10.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer glowing pulse ring
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .scale(pulseScale)
                        .background(Color(0xFFFF2A55).copy(alpha = pulseAlpha), CircleShape)
                )
                // Center core neon ruby beacon
                Box(
                    modifier = Modifier
                        .size(6.5.dp)
                        .background(Color(0xFFFF2A55), CircleShape)
                )
            }
            Text(
                text = "LIVE",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.4.sp,
                color = if (isDark) Color.White else Color(0xFFDC2626)
            )
        }
    } else {
        // UPCOMING / ENDED status
        val isUpcoming = status == ContestStatus.UPCOMING
        val dotColor = if (isUpcoming) UpcomingPrimary else EndedColor.copy(alpha = 0.55f)
        val textColor = if (isUpcoming) UpcomingViolet else EndedColor.copy(alpha = 0.80f)
        val bgBrush = if (isUpcoming) {
            Brush.linearGradient(
                if (isDark) listOf(UpcomingDarkBg, Color(0xFF1B1057))
                else listOf(UpcomingLightBg, Color(0xFFDDE3FF))
            )
        } else {
            Brush.linearGradient(
                if (isDark) listOf(EndedDarkBg, Color(0xFF1C2432))
                else listOf(EndedLightBg, Color(0xFFE2E8F0))
            )
        }
        val label = if (isUpcoming) "Upcoming" else "Ended"

        Row(
            modifier = modifier
                .clip(CircleShape)
                .background(bgBrush)
                .padding(horizontal = 9.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .background(dotColor, CircleShape)
            )
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        }
    }
}
