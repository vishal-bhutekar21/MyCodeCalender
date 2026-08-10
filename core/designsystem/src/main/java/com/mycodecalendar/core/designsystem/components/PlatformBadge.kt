package com.mycodecalendar.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycodecalendar.core.designsystem.*
import com.mycodecalendar.domain.model.Platform

fun Platform.getBrandColor(): Color {
    return when (this) {
        Platform.CODEFORCES -> ColorCodeforces
        Platform.LEETCODE -> ColorLeetCode
        Platform.CODECHEF -> ColorCodeChef
        Platform.ATCODER -> ColorAtCoder
        Platform.GITHUB -> ColorGitHub
        Platform.GEEKSFORGEEKS -> ColorGeeksforGeeks
    }
}

@Composable
fun PlatformBadge(
    platform: Platform,
    modifier: Modifier = Modifier
) {
    val brandColor = platform.getBrandColor()
    Box(
        modifier = modifier
            .background(
                color = brandColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = brandColor.copy(alpha = 0.4f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = platform.name,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = brandColor
        )
    }
}
