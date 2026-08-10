package com.mycodecalendar.feature.platforms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mycodecalendar.core.designsystem.Typography
import com.mycodecalendar.domain.model.Platform

@Composable
fun AddPlatformScreen(
    onAddPlatform: (Platform, String) -> Unit,
    onBackClick: () -> Unit
) {
    var selectedPlatform by remember { mutableStateOf(Platform.CODEFORCES) }
    var username by remember { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        TextButton(onClick = onBackClick) {
            Text("← Back", style = Typography.labelLarge)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Connect Platform",
            style = Typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Enter your username/handle to sync ratings & contest stats",
            style = Typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Platform Selection Options
        Text(
            text = "SELECT PLATFORM",
            style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.outline
        )

        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Platform.values().forEach { platform ->
                val isSelected = platform == selectedPlatform
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedPlatform = platform },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = platform.name,
                            style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedPlatform = platform }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Handle Input
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username / Handle") },
            placeholder = { Text("e.g. tourist, neal_wu") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Save Button
        Button(
            onClick = {
                if (username.isNotBlank()) {
                    isVerifying = true
                    onAddPlatform(selectedPlatform, username.trim())
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = username.isNotBlank() && !isVerifying,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                if (isVerifying) "Verifying & Syncing..." else "Save Platform Account",
                style = Typography.labelLarge
            )
        }
    }
}
