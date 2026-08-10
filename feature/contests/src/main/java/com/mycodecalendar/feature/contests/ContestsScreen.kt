package com.mycodecalendar.feature.contests

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mycodecalendar.core.designsystem.Typography
import com.mycodecalendar.core.designsystem.components.EmptyState
import com.mycodecalendar.core.designsystem.components.PlatformBadge
import com.mycodecalendar.core.designsystem.components.StatusChip
import com.mycodecalendar.core.designsystem.components.getBrandColor
import com.mycodecalendar.domain.model.Contest
import com.mycodecalendar.domain.model.ContestStatus
import com.mycodecalendar.domain.model.Platform
import java.time.Duration
import java.time.Instant

@Composable
fun ContestsScreen(
    contests: List<Contest>,
    onContestClick: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedPlatform by remember { mutableStateOf<Platform?>(null) }
    var selectedStatus by remember { mutableStateOf<ContestStatus?>(null) }

    val filteredContests = remember(contests, searchQuery, selectedPlatform, selectedStatus) {
        contests.filter { contest ->
            val matchesQuery = searchQuery.isBlank() || contest.name.contains(searchQuery, ignoreCase = true)
            val matchesPlatform = selectedPlatform == null || contest.platform == selectedPlatform
            val matchesStatus = selectedStatus == null || contest.status == selectedStatus
            matchesQuery && matchesPlatform && matchesStatus
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 20.dp, bottom = 4.dp)
        ) {
            Text(
                text = "Contests",
                style = Typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "${filteredContests.size} contests available",
                style = Typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Pill Search Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(14.dp)
                ),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Search",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.width(10.dp))
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            "Search contests...",
                            style = Typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    textStyle = Typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Platform chips (brand-colored when selected)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
        ) {
            item {
                FilterChipStyled(
                    label = "All",
                    selected = selectedPlatform == null,
                    selectedColor = MaterialTheme.colorScheme.primary,
                    onClick = { selectedPlatform = null }
                )
            }
            items(Platform.values()) { platform ->
                FilterChipStyled(
                    label = platform.name.lowercase().replaceFirstChar { it.uppercase() },
                    selected = selectedPlatform == platform,
                    selectedColor = platform.getBrandColor(),
                    onClick = {
                        selectedPlatform = if (selectedPlatform == platform) null else platform
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Status chips (color-coded)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp)
        ) {
            item {
                FilterChipStyled(
                    label = "All Status",
                    selected = selectedStatus == null,
                    selectedColor = MaterialTheme.colorScheme.primary,
                    onClick = { selectedStatus = null }
                )
            }
            items(ContestStatus.values()) { status ->
                val color = when (status) {
                    ContestStatus.LIVE -> Color(0xFF10B981)
                    ContestStatus.UPCOMING -> Color(0xFF6366F1)
                    ContestStatus.ENDED -> Color(0xFF64748B)
                }
                FilterChipStyled(
                    label = status.name.lowercase().replaceFirstChar { it.uppercase() },
                    selected = selectedStatus == status,
                    selectedColor = color,
                    onClick = { selectedStatus = if (selectedStatus == status) null else status }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredContests.isEmpty()) {
            EmptyState(message = "No contests match your filters.")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp)
            ) {
                items(filteredContests) { contest ->
                    ContestCard(
                        contest = contest,
                        onClick = { onContestClick(contest.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun FilterChipStyled(
    label: String,
    selected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = if (selected) selectedColor.copy(alpha = 0.15f)
        else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (selected) selectedColor.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        )
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = Typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = if (selected) selectedColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun ContestCard(
    contest: Contest,
    onClick: () -> Unit
) {
    val brandColor = contest.platform.getBrandColor()
    val timeUntilStart = remember(contest.startTimeUtc) {
        Duration.between(Instant.now(), contest.startTimeUtc).seconds.coerceAtLeast(0)
    }
    val timeLabel = when {
        contest.status == ContestStatus.LIVE -> "Live now"
        contest.status == ContestStatus.ENDED -> "Ended"
        timeUntilStart < 3600 -> "in ${timeUntilStart / 60}m"
        timeUntilStart < 86400 -> "in ${timeUntilStart / 3600}h ${(timeUntilStart % 3600) / 60}m"
        else -> "in ${timeUntilStart / 86400}d"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Left brand color accent bar
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(
                        color = brandColor,
                        shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                    )
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PlatformBadge(platform = contest.platform)
                    StatusChip(status = contest.status)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = contest.name,
                    style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${contest.durationSeconds / 3600}h ${(contest.durationSeconds % 3600) / 60}m",
                        style = Typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                    )
                    Text(
                        text = timeLabel,
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = if (contest.status == ContestStatus.LIVE) Color(0xFF10B981)
                        else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
