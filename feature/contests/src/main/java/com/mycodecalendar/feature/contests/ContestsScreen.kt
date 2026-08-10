package com.mycodecalendar.feature.contests

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.mycodecalendar.domain.model.Contest
import com.mycodecalendar.domain.model.ContestStatus
import com.mycodecalendar.domain.model.Platform

@OptIn(ExperimentalMaterial3Api::class)
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
            .padding(16.dp)
    ) {
        Text(
            text = "Explore Contests",
            style = Typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search contest name...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Platform Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedPlatform == null,
                    onClick = { selectedPlatform = null },
                    label = { Text("All Platforms") }
                )
            }
            items(Platform.values()) { platform ->
                FilterChip(
                    selected = selectedPlatform == platform,
                    onClick = { selectedPlatform = if (selectedPlatform == platform) null else platform },
                    label = { Text(platform.name) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Status Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedStatus == null,
                    onClick = { selectedStatus = null },
                    label = { Text("All Status") }
                )
            }
            items(ContestStatus.values()) { status ->
                FilterChip(
                    selected = selectedStatus == status,
                    onClick = { selectedStatus = if (selectedStatus == status) null else status },
                    label = { Text(status.name) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredContests.isEmpty()) {
            EmptyState(message = "No contests match your filters.")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredContests) { contest ->
                    DetailedContestCard(
                        contest = contest,
                        onClick = { onContestClick(contest.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun DetailedContestCard(
    contest: Contest,
    onClick: () -> Unit
) {
    val statusColor = when (contest.status) {
        ContestStatus.LIVE -> Color(0xFF4CAF50)
        ContestStatus.UPCOMING -> MaterialTheme.colorScheme.primary
        ContestStatus.ENDED -> MaterialTheme.colorScheme.outline
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = contest.platform.name,
                    style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = contest.status.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = contest.name,
                style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Duration: ${contest.durationSeconds / 3600}h ${(contest.durationSeconds % 3600) / 60}m",
                    style = Typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = contest.ratingType ?: "Rated",
                    style = Typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
