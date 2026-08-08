package com.mycodecalendar.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycodecalendar.core.designsystem.*
import com.mycodecalendar.domain.model.Platform

fun Platform.getBrandColor(): Color = when (this) {
    Platform.CODEFORCES    -> BrandCodeforces
    Platform.LEETCODE      -> BrandLeetCode
    Platform.CODECHEF      -> BrandCodeChef
    Platform.ATCODER       -> BrandAtCoder
    Platform.GITHUB        -> BrandGitHub
    Platform.GEEKSFORGEEKS -> BrandGeeksforGeeks
}

fun Platform.getDisplayName(): String = when (this) {
    Platform.CODEFORCES    -> "Codeforces"
    Platform.LEETCODE      -> "LeetCode"
    Platform.CODECHEF      -> "CodeChef"
    Platform.ATCODER       -> "AtCoder"
    Platform.GITHUB        -> "GitHub"
    Platform.GEEKSFORGEEKS -> "GFG"
}

/**
 * PlatformBadge — Sleek glassmorphism pill badge showing platform name + brand dot.
 */
@Composable
fun PlatformBadge(
    platform: Platform,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val isDark = isSystemInDarkTheme()
    val brandColor = platform.getBrandColor()

    val bgFill = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                brandColor.copy(alpha = 0.15f),
                GlassSurfaceDark.copy(alpha = 0.15f)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                brandColor.copy(alpha = 0.10f),
                GlassSurfaceLight.copy(alpha = 0.70f)
            )
        )
    }

    val borderColor = brandColor.copy(alpha = if (isDark) 0.35f else 0.25f)
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant

    val dotSize: Dp = if (compact) 5.dp else 6.dp
    val fontSize = if (compact) 10.sp else 11.sp
    val hPad = if (compact) 8.dp else 10.dp
    val vPad = if (compact) 3.dp else 4.dp

    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(bgFill)
            .border(1.dp, borderColor, CircleShape)
            .padding(horizontal = hPad, vertical = vPad),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(dotSize)
                .background(brandColor, CircleShape)
        )
        Spacer(modifier = Modifier.width(if (compact) 4.dp else 5.dp))
        Text(
            text = platform.getDisplayName(),
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            color = textColor,
            letterSpacing = 0.sp
        )
    }
}
