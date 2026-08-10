package com.mycodecalendar.core.designsystem.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mycodecalendar.core.designsystem.Typography

@Composable
fun LastUpdatedLabel(timeAgo: String, modifier: Modifier = Modifier) {
    Text(
        text = "Updated $timeAgo",
        style = Typography.bodySmall,
        modifier = modifier.padding(vertical = 4.dp)
    )
}
