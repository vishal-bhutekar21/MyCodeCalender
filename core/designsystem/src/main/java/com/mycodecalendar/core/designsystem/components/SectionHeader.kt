package com.mycodecalendar.core.designsystem.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.mycodecalendar.core.designsystem.Typography

/**
 * SectionHeader — a styled section label with an optional trailing action slot.
 *
 * The title text has a subtle left-edge gradient underline (2dp tall, 24dp wide)
 * in the primary color to add visual interest without being intrusive.
 *
 * @param title The section title (will be uppercased automatically)
 * @param modifier Modifier for outer layout positioning
 * @param trailingContent Optional composable slot for a "See All" link, badge, etc.
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title.uppercase(),
            style = Typography.labelMedium.copy(fontWeight = FontWeight.Black),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = TextUnit(1.5f, TextUnitType.Sp),
            modifier = Modifier.drawBehind {
                // Gradient underline accent — a 2.5dp line with the primary electric color
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            primaryColor,
                            primaryColor.copy(alpha = 0.3f),
                            Color.Transparent
                        ),
                        startX = 0f,
                        endX = 36.dp.toPx()
                    ),
                    start = Offset(0f, size.height + 3.dp.toPx()),
                    end = Offset(36.dp.toPx(), size.height + 3.dp.toPx()),
                    strokeWidth = 2.5.dp.toPx()
                )
            }
        )

        trailingContent?.invoke()
    }
}

/**
 * SeeAllButton — lightweight "See all →" action link for section headers.
 */
@Composable
fun SeeAllButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.TextButton(
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
    ) {
        Text(
            text = "See all →",
            style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
    }
}
