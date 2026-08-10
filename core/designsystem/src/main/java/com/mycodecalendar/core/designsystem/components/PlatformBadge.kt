package com.mycodecalendar.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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

@Composable
fun PlatformBadge(
    platform: Platform,
    modifier: Modifier = Modifier
) {
    val brandColor = platform.getBrandColor()

    Row(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tiny brand-color dot
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(brandColor, CircleShape)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = platform.getDisplayName(),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.sp
        )
    }
}
