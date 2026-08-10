package com.mycodecalendar.feature.contestdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mycodecalendar.core.designsystem.Typography
import com.mycodecalendar.core.designsystem.components.PlatformBadge
import com.mycodecalendar.core.designsystem.components.StatusChip
import com.mycodecalendar.domain.model.Contest
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
    ) {
        // Top navigation bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Header section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PlatformBadge(platform = contest.platform)
                StatusChip(status = contest.status)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = contest.name,
                style = Typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Key details card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(18.dp)
                ),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)) {
                DetailRow(label = "Start Time", value = formatter.format(contest.startTimeUtc))
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 0.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                )
                DetailRow(label = "End Time", value = formatter.format(contest.endTimeUtc))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                )
                DetailRow(
                    label = "Duration",
                    value = "${contest.durationSeconds / 3600}h ${(contest.durationSeconds % 3600) / 60}m"
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                )
                DetailRow(label = "Format", value = contest.contestType ?: "Standard")
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                )
                DetailRow(label = "Rating Impact", value = contest.ratingType ?: "Rated")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Action buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Primary CTA
            Button(
                onClick = { onJoinClick(contest.officialUrl) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = Icons.Rounded.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Open Official Page",
                    style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            // Calendar button
            OutlinedButton(
                onClick = {
                    calendarAdded = !calendarAdded
                    onAddToCalendarClick(contest)
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (calendarAdded) MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.primary
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (calendarAdded) MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (calendarAdded) "Added to Calendar" else "Add to Calendar",
                    style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            // Reminder button
            FilledTonalButton(
                onClick = {
                    reminderEnabled = !reminderEnabled
                    onSetReminderClick(contest)
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (reminderEnabled)
                        MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.NotificationsNone,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (reminderEnabled) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (reminderEnabled) "Reminder Active · 15 min before" else "Set Reminder",
                    style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = if (reminderEnabled) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = Typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = Typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
