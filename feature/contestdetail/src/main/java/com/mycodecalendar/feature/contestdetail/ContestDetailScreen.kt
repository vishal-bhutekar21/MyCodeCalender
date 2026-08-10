package com.mycodecalendar.feature.contestdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mycodecalendar.core.designsystem.Typography
import com.mycodecalendar.domain.model.Contest
import com.mycodecalendar.domain.model.ContestStatus
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ContestDetailScreen(
    contest: Contest,
    onBackClick: () -> Unit,
    onJoinClick: (String) -> Unit,
    onAddToCalendarClick: (Contest) -> Unit,
    onSetReminderClick: (Contest) -> Unit
) {
    var reminderEnabled by remember { mutableStateOf(false) }
    var calendarAdded by remember { mutableStateOf(false) }

    val formatter = remember {
        DateTimeFormatter.ofPattern("EEE, dd MMM yyyy · hh:mm a")
            .withZone(ZoneId.systemDefault())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Back Button
        TextButton(onClick = onBackClick) {
            Text("← Back", style = Typography.labelLarge)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Platform & Status Badges
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = contest.platform.name,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (contest.status == ContestStatus.LIVE) Color(0xFF4CAF50) else MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = contest.status.name,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (contest.status == ContestStatus.LIVE) Color.White else MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Contest Title
        Text(
            text = contest.name,
            style = Typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Key Details Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                DetailRow(label = "Start Time", value = formatter.format(contest.startTimeUtc))
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                DetailRow(label = "End Time", value = formatter.format(contest.endTimeUtc))
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                DetailRow(
                    label = "Duration",
                    value = "${contest.durationSeconds / 3600} hours ${(contest.durationSeconds % 3600) / 60} mins"
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                DetailRow(label = "Format / Type", value = contest.contestType ?: "Standard")
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                DetailRow(label = "Rating Impact", value = contest.ratingType ?: "Rated")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons
        Button(
            onClick = { onJoinClick(contest.officialUrl) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("🚀 Join / Official Contest Page", style = Typography.labelLarge)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                calendarAdded = !calendarAdded
                onAddToCalendarClick(contest)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                if (calendarAdded) "✓ Added to Calendar" else "📅 Add to Android Calendar",
                style = Typography.labelLarge
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        FilledTonalButton(
            onClick = {
                reminderEnabled = !reminderEnabled
                onSetReminderClick(contest)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                if (reminderEnabled) "🔔 Reminder Set (15m before)" else "🔔 Set Notification Reminder",
                style = Typography.labelLarge
            )
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = Typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
        Text(text = value, style = Typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
