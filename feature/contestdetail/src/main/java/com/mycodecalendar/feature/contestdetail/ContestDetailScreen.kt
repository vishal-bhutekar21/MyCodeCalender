package com.mycodecalendar.feature.contestdetail

import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.mycodecalendar.core.designsystem.GlassmorphismBackground
import com.mycodecalendar.core.designsystem.Typography
import com.mycodecalendar.core.designsystem.components.GlassBackButton
import com.mycodecalendar.core.designsystem.components.GlassCard
import com.mycodecalendar.core.designsystem.components.PlatformBadge
import com.mycodecalendar.core.designsystem.components.StatusChip
import com.mycodecalendar.domain.model.Contest
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * ContestDetailScreen — Full contest timing, format, rating impact, and interactive calendar/reminder integration.
 *
 * Enhanced Features:
 * - Full Glassmorphism & OLED Dark Theme compatibility.
 * - [CalendarConfirmationDialog]: Animated glass modal popup when adding to system calendar.
 * - Native Android [CalendarContract.Events] Intent launcher pre-filling event title, start & end time, and description.
 */
@Composable
fun ContestDetailScreen(
    contest: Contest,
    onBackClick: () -> Unit,
    onJoinClick: (String) -> Unit,
    onAddToCalendarClick: (Contest) -> Unit,
    onSetReminderClick: (Contest) -> Unit
) {
    val context = LocalContext.current
    var reminderEnabled by remember { mutableStateOf(false) }
    var calendarAdded by remember { mutableStateOf(false) }
    var showCalendarModal by remember { mutableStateOf(false) }
    var showReminderModal by remember { mutableStateOf(false) }

    val formatter = remember {
        DateTimeFormatter.ofPattern("EEE, dd MMM yyyy · hh:mm a")
            .withZone(ZoneId.systemDefault())
    }

    GlassmorphismBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            // Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassBackButton(onClick = onBackClick)
            }

            // Contest Header Section
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

            // Key Details Glass Card
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                cornerRadius = 20.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)) {
                    DetailRow(label = "Start Time", value = formatter.format(contest.startTimeUtc))
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                    )
                    DetailRow(label = "End Time", value = formatter.format(contest.endTimeUtc))
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                    )
                    DetailRow(
                        label = "Duration",
                        value = "${contest.durationSeconds / 3600}h ${(contest.durationSeconds % 3600) / 60}m"
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                    )
                    DetailRow(label = "Format", value = contest.contestType ?: "Standard")
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                    )
                    DetailRow(label = "Rating Impact", value = contest.ratingType ?: "Rated")
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                    )
                    val displayUrl = if (contest.officialUrl.length > 40) "${contest.officialUrl.take(37)}..." else contest.officialUrl
                    DetailRow(label = "Official Link", value = displayUrl)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Primary Action Button — Open Official Contest Page
                Button(
                    onClick = { onJoinClick(contest.officialUrl) },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Open Official Page",
                        style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    )
                }

                // Add to Calendar Button
                OutlinedButton(
                    onClick = {
                        calendarAdded = true
                        showCalendarModal = true
                        onAddToCalendarClick(contest)

                        // Launch Native Android Calendar Intent directly
                        runCatching {
                            val intent = Intent(Intent.ACTION_INSERT).apply {
                                data = CalendarContract.Events.CONTENT_URI
                                putExtra(CalendarContract.Events.TITLE, contest.name)
                                putExtra(
                                    CalendarContract.Events.DESCRIPTION,
                                    "Contest on ${contest.platform.name}. Official URL: ${contest.officialUrl}"
                                )
                                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, contest.startTimeUtc.toEpochMilli())
                                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, contest.endTimeUtc.toEpochMilli())
                            }
                            context.startActivity(intent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (calendarAdded) Color(0xFF22C55E)
                        else MaterialTheme.colorScheme.primary
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (calendarAdded) Color(0xFF22C55E).copy(alpha = 0.6f)
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    )
                ) {
                    Icon(
                        imageVector = if (calendarAdded) Icons.Rounded.CheckCircle else Icons.Rounded.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = if (calendarAdded) Color(0xFF22C55E) else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (calendarAdded) "✓ Added to Calendar" else "Add to System Calendar",
                        style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    )
                }

                // Set Reminder Button
                FilledTonalButton(
                    onClick = {
                        reminderEnabled = !reminderEnabled
                        if (reminderEnabled) {
                            showReminderModal = true
                        }
                        onSetReminderClick(contest)
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (reminderEnabled)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.60f)
                    )
                ) {
                    Icon(
                        imageVector = if (reminderEnabled) Icons.Rounded.NotificationsActive else Icons.Rounded.NotificationsNone,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = if (reminderEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (reminderEnabled) "✓ Reminder Active · 15m Before" else "Set Event Reminder",
                        style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                        color = if (reminderEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }

        // ── ANIMATED CALENDAR CONFIRMATION DIALOG ─────────────────────────────
        if (showCalendarModal) {
            CalendarConfirmationModal(
                title = "Added to System Calendar",
                subtitle = contest.name,
                timeText = formatter.format(contest.startTimeUtc),
                onDismiss = { showCalendarModal = false }
            )
        }

        // ── ANIMATED REMINDER CONFIRMATION DIALOG ─────────────────────────────
        if (showReminderModal) {
            CalendarConfirmationModal(
                title = "Reminder Activated",
                subtitle = "You will be notified 15 minutes before ${contest.name} starts.",
                timeText = formatter.format(contest.startTimeUtc),
                isReminder = true,
                onDismiss = { showReminderModal = false }
            )
        }
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
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = Typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ── ANIMATED GLASS MODAL CONFIRMATION DIALOG ────────────────────────────────

@Composable
fun CalendarConfirmationModal(
    title: String,
    subtitle: String,
    timeText: String,
    isReminder: Boolean = false,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            accentColor = if (isReminder) MaterialTheme.colorScheme.primary else Color(0xFF22C55E),
            cornerRadius = 24.dp,
            elevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Success Checkmark / Notification Icon with Glow Ring
                Surface(
                    shape = CircleShape,
                    color = (if (isReminder) MaterialTheme.colorScheme.primary else Color(0xFF22C55E)).copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        (if (isReminder) MaterialTheme.colorScheme.primary else Color(0xFF22C55E)).copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isReminder) Icons.Rounded.NotificationsActive else Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = if (isReminder) MaterialTheme.colorScheme.primary else Color(0xFF22C55E)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = title,
                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Black, fontSize = 20.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = subtitle,
                    style = Typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = timeText,
                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isReminder) MaterialTheme.colorScheme.primary else Color(0xFF22C55E)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isReminder) MaterialTheme.colorScheme.primary else Color(0xFF22C55E)
                    )
                ) {
                    Text(
                        text = "Done",
                        style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
