package com.mycodecalendar.core.designsystem.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mycodecalendar.core.designsystem.Typography

@Composable
fun StatValue(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = value, style = Typography.headlineMedium)
        Text(text = label, style = Typography.bodySmall)
    }
}
