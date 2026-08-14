package com.mycodecalendar.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp

/**
 * GlassBackButton — Universal frosted glass back navigation button.
 * Provides a tactile, luminous 1px bordered glass pill across all secondary screens.
 */
@Composable
fun GlassBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val baseBg = if (isDark) Color(0xFF0F131C) else Color(0xFFFFFFFF)
    val glassFill = if (isDark) Color(0xFF151B28) else Color(0xFFF8FAFC)
    val glassBorder = if (isDark) Color(0xFF1E2536) else Color(0xFFCBD5E1)

    val borderBrush = Brush.linearGradient(
        colors = listOf(
            if (isDark) Color(0x35FFFFFF) else Color(0xE6FFFFFF),
            glassBorder,
            if (isDark) Color(0x10FFFFFF) else Color(0x40FFFFFF)
        )
    )

    Box(
        modifier = modifier
            .size(42.dp)
            .shadow(
                elevation = 6.dp,
                shape = CircleShape,
                spotColor = Color.Black.copy(alpha = if (isDark) 0.35f else 0.08f),
                ambientColor = Color.Black.copy(alpha = 0.06f)
            )
            .clip(CircleShape)
            .background(baseBg.copy(alpha = if (isDark) 0.85f else 0.90f))
            .background(
                Brush.verticalGradient(
                    listOf(
                        glassFill.copy(alpha = if (isDark) 0.40f else 0.70f),
                        glassFill.copy(alpha = if (isDark) 0.15f else 0.40f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = borderBrush,
                shape = CircleShape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = true),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = "Back",
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}
