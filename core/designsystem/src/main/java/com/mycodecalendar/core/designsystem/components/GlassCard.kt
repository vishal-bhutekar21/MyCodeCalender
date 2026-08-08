package com.mycodecalendar.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycodecalendar.core.designsystem.GlassBorderDark
import com.mycodecalendar.core.designsystem.GlassBorderLight
import com.mycodecalendar.core.designsystem.GlassHighlightDark
import com.mycodecalendar.core.designsystem.GlassHighlightLight
import com.mycodecalendar.core.designsystem.GlassSurfaceDark
import com.mycodecalendar.core.designsystem.GlassSurfaceLight

/**
 * GlassCard — Frosted Glassmorphism card container.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    accentColor: Color? = null,
    cornerRadius: Dp = 16.dp,
    elevation: Dp = 4.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()

    val glassFill   = if (isDark) GlassSurfaceDark   else GlassSurfaceLight
    val glassBorder = if (isDark) GlassBorderDark    else GlassBorderLight
    val highlight   = if (isDark) GlassHighlightDark else GlassHighlightLight
    val baseBg      = if (isDark) Color(0xFF111827)   else Color(0xFFFFFFFF)

    val shape = RoundedCornerShape(cornerRadius)

    val borderBrush = if (accentColor != null) {
        Brush.linearGradient(
            colors = listOf(
                accentColor.copy(alpha = if (isDark) 0.50f else 0.40f),
                glassBorder,
                accentColor.copy(alpha = if (isDark) 0.20f else 0.15f)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                if (isDark) Color(0x33FFFFFF) else Color(0xCCFFFFFF),
                glassBorder,
                if (isDark) Color(0x10FFFFFF) else Color(0x40FFFFFF)
            )
        )
    }

    val highlightBrush = Brush.verticalGradient(
        colors = listOf(highlight, Color.Transparent),
        startY = 0f,
        endY = 32f
    )

    val containerModifier = modifier
        .shadow(
            elevation = elevation,
            shape = shape,
            spotColor = accentColor?.copy(alpha = 0.18f)
                ?: (if (isDark) Color.Black.copy(alpha = 0.50f) else Color(0x1A000000)),
            ambientColor = Color.Black.copy(alpha = if (isDark) 0.35f else 0.08f)
        )
        .clip(shape)
        .background(baseBg.copy(alpha = if (isDark) 0.70f else 0.85f))
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    glassFill.copy(alpha = if (isDark) 0.20f else 0.65f),
                    glassFill.copy(alpha = if (isDark) 0.10f else 0.45f)
                )
            )
        )
        .drawBehind {
            drawRect(brush = highlightBrush)
        }
        .border(
            width = 1.dp,
            brush = borderBrush,
            shape = shape
        )
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(
                        bounded = true,
                        color = accentColor ?: MaterialTheme.colorScheme.primary
                    ),
                    onClick = onClick
                )
            } else Modifier
        )

    Column(
        modifier = containerModifier,
        content = content
    )
}

/**
 * GlassChip — Modern, minimalist glassmorphism chip/badge with dot indicators.
 */
@Composable
fun GlassChip(
    label: String,
    selected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    val bgColor = if (selected) accentColor.copy(alpha = if (isDark) 0.22f else 0.12f)
    else (if (isDark) GlassSurfaceDark.copy(alpha = 0.15f) else GlassSurfaceLight.copy(alpha = 0.70f))

    val borderColor = if (selected) accentColor.copy(alpha = if (isDark) 0.60f else 0.45f)
    else if (isDark) GlassBorderDark.copy(alpha = 0.35f)
    else GlassBorderLight.copy(alpha = 0.50f)

    val textColor = if (selected) accentColor
    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(bgColor)
            .border(1.dp, borderColor, CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = true, color = accentColor),
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(accentColor, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = label,
                style = com.mycodecalendar.core.designsystem.Typography.labelMedium.copy(
                    fontWeight = if (selected) androidx.compose.ui.text.font.FontWeight.Bold
                    else androidx.compose.ui.text.font.FontWeight.SemiBold
                ),
                color = textColor
            )
        }
    }
}
