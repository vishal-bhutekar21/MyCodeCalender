package com.mycodecalendar.core.designsystem.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycodecalendar.domain.model.ContestStatus

private val LiveGreen = Color(0xFF22C55E)

@Composable
fun StatusChip(
    status: ContestStatus,
    modifier: Modifier = Modifier
) {
    val isLive = status == ContestStatus.LIVE

    val dotAlpha by rememberInfiniteTransition(label = "livePulse").animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )

    val dotColor = when (status) {
        ContestStatus.LIVE     -> LiveGreen
        ContestStatus.UPCOMING -> MaterialTheme.colorScheme.primary
        ContestStatus.ENDED    -> MaterialTheme.colorScheme.outline
    }
    val label = when (status) {
        ContestStatus.LIVE     -> "Live"
        ContestStatus.UPCOMING -> "Upcoming"
        ContestStatus.ENDED    -> "Ended"
    }
    val textColor = when (status) {
        ContestStatus.LIVE     -> LiveGreen
        ContestStatus.UPCOMING -> MaterialTheme.colorScheme.primary
        ContestStatus.ENDED    -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
    }

    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .alpha(if (isLive) dotAlpha else 1f)
                .background(dotColor, CircleShape)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            letterSpacing = 0.sp
        )
    }
}
