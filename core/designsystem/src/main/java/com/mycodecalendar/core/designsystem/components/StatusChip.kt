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
import com.mycodecalendar.domain.model.ContestStatus

// ── Premium Status Color Tokens ───────────────────────────────────────────────

// LIVE — Electric Neon Green
private val LiveGreen   = Color(0xFF00F579)
private val LiveGreen2  = Color(0xFF00D166)
private val LiveDarkBg  = Color(0xFF00180C)
private val LiveLightBg = Color(0xFFE0FFF0)

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
 * - LIVE:     Neon electric green with animated pulsing glow
 * - UPCOMING: Deep indigo-violet gradient badge
 * - ENDED:    Subtle warm slate — unobtrusive but readable
 */
@Composable
fun StatusChip(
    status: ContestStatus,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val isLive = status == ContestStatus.LIVE

    // Animate LIVE dot
    val dotScale by rememberInfiniteTransition(label = "livePulse").animateFloat(
        initialValue = 0.6f,
        targetValue  = 1.4f,
        animationSpec = infiniteRepeatable(
            tween(700, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "dotScale"
    )

    // ── Per-status chip config ─────────────────────────────────────────────────
    val chipShape = when (status) {
        ContestStatus.LIVE     -> RoundedCornerShape(10.dp)
        ContestStatus.UPCOMING -> CircleShape
        ContestStatus.ENDED    -> CircleShape
    }

    val dotColor = when (status) {
        ContestStatus.LIVE     -> LiveGreen
        ContestStatus.UPCOMING -> UpcomingPrimary
        ContestStatus.ENDED    -> EndedColor.copy(alpha = 0.55f)
    }

    val textColor = when (status) {
        ContestStatus.LIVE     -> LiveGreen
        ContestStatus.UPCOMING -> UpcomingViolet
        ContestStatus.ENDED    -> EndedColor.copy(alpha = 0.80f)
    }

    val bgBrush: Brush = when (status) {
        ContestStatus.LIVE -> Brush.linearGradient(
            if (isDark) listOf(LiveDarkBg, Color(0xFF003A1A))
            else        listOf(LiveLightBg, Color(0xFFB6FFDA))
        )
        ContestStatus.UPCOMING -> Brush.linearGradient(
            if (isDark) listOf(UpcomingDarkBg, Color(0xFF1B1057))
            else        listOf(UpcomingLightBg, Color(0xFFDDE3FF))
        )
        ContestStatus.ENDED -> Brush.linearGradient(
            if (isDark) listOf(EndedDarkBg, Color(0xFF1C2432))
            else        listOf(EndedLightBg, Color(0xFFE2E8F0))
        )
    }

    val borderColor = when (status) {
        ContestStatus.LIVE     -> LiveGreen2.copy(alpha = if (isDark) 0.80f else 0.55f)
        ContestStatus.UPCOMING -> UpcomingPrimary.copy(alpha = if (isDark) 0.55f else 0.35f)
        ContestStatus.ENDED    -> EndedColor.copy(alpha = if (isDark) 0.22f else 0.28f)
    }

    val label = when (status) {
        ContestStatus.LIVE     -> "● LIVE"
        ContestStatus.UPCOMING -> "Upcoming"
        ContestStatus.ENDED    -> "Ended"
    }

    val borderWidth = if (isLive) 1.5.dp else 1.dp

    // ── Chip ──────────────────────────────────────────────────────────────────
    Row(
        modifier = modifier
            .clip(chipShape)
            .background(bgBrush)
            .border(borderWidth, borderColor, chipShape)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (!isLive) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .scale(if (status == ContestStatus.UPCOMING) dotScale else 1f)
                    .background(dotColor, CircleShape)
            )
        }

        Text(
            text = label,
            fontSize = if (isLive) 10.sp else 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = textColor,
            letterSpacing = if (isLive) 1.sp else 0.2.sp
        )
    }
}
