package com.mycodecalendar.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mycodecalendar.core.designsystem.Typography
import com.mycodecalendar.core.designsystem.components.SectionHeader
import com.mycodecalendar.domain.model.PlatformAccount

@Composable
fun SettingsScreen(
    connectedAccounts: List<PlatformAccount>,
    onAddPlatformClick: () -> Unit,
    onManageAccountClick: (PlatformAccount) -> Unit
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var calendarSyncEnabled by remember { mutableStateOf(true) }
    var selectedTheme by remember { mutableStateOf("System Default") }
    var selectedOffset by remember { mutableStateOf("15 Minutes Before") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            style = Typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Connected Accounts Section
        SectionHeader("CONNECTED PLATFORMS")
        Spacer(modifier = Modifier.height(8.dp))

        if (connectedAccounts.isEmpty()) {
            Text("No platforms connected.", style = Typography.bodyMedium)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                connectedAccounts.forEach { acc ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = acc.platform.name,
                                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "@${acc.username}",
                                    style = Typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            TextButton(onClick = { onManageAccountClick(acc) }) {
                                Text("Manage", style = Typography.labelMedium)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onAddPlatformClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("+ Add Another Platform", style = Typography.labelLarge)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Preferences Section
        SectionHeader("NOTIFICATION & REMINDERS")
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingSwitchRow(
                    title = "Contest Reminders",
                    subtitle = "Receive notifications before contests start",
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                SettingSwitchRow(
                    title = "Android Calendar Sync",
                    subtitle = "Allow adding contests directly to device calendar",
                    checked = calendarSyncEnabled,
                    onCheckedChange = { calendarSyncEnabled = it }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Appearance
        SectionHeader("APPEARANCE & THEME")
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("App Theme", style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("System Default", "Dark", "Light").forEach { theme ->
                        FilterChip(
                            selected = selectedTheme == theme,
                            onClick = { selectedTheme = theme },
                            label = { Text(theme) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // App Footer Info
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("MyCodeCalendar v1.0.0", style = Typography.labelMedium, color = MaterialTheme.colorScheme.outline)
            Text("Built for competitive programmers worldwide", style = Typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = Typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
            Text(text = subtitle, style = Typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
