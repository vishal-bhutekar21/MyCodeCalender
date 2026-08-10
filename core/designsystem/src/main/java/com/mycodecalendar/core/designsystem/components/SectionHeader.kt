package com.mycodecalendar.core.designsystem.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mycodecalendar.core.designsystem.Typography

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title.uppercase(),
        style = Typography.titleMedium,
        modifier = modifier.padding(vertical = 8.dp)
    )
}
