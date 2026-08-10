package com.mycodecalendar.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycodecalendar.core.designsystem.Typography
import com.mycodecalendar.core.designsystem.components.EmptyState
import com.mycodecalendar.core.designsystem.components.LastUpdatedLabel
import com.mycodecalendar.core.designsystem.components.SectionHeader
import com.mycodecalendar.domain.model.Contest
import com.mycodecalendar.domain.model.GitHubStats
import com.mycodecalendar.domain.model.Platform
import com.mycodecalendar.domain.model.PlatformStats
import com.mycodecalendar.domain.model.Resource
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.Instant

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onAddPlatformClick: () -> Unit,
    onPlatformClick: (Platform) -> Unit,
    onContestClick: (String) -> Unit,
    onViewAllContestsClick: () -> Unit,
    onResourceClick: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // App Header & Streak Counter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "MyCodeCalendar",
                    style = Typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Your coding life, on time",
                    style = Typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Streak Pill
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🔥", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${uiState.userStreak} Days",
                        style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Next Upcoming Contest Hero Banner
        uiState.nextContest?.let { next ->
            HeroContestCountdownCard(
                contest = next,
                onCardClick = { onContestClick(next.id) }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // GitHub Developer Stats Section
        uiState.gitHubStats?.let { gh ->
            SectionHeader(title = "GITHUB DEVELOPER STATS")
            Spacer(modifier = Modifier.height(8.dp))
            GitHubStatsOverviewCard(
                stats = gh,
                onClick = { onPlatformClick(Platform.GITHUB) }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Platform Ratings Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionHeader(title = "PLATFORM RATINGS")
            TextButton(onClick = onAddPlatformClick) {
                Text("+ Connect Platform", style = Typography.labelMedium)
            }
        }

        if (uiState.connectedStats.isEmpty()) {
            EmptyState(
                message = "No platforms connected yet. Tap '+ Connect Platform' to track your ratings!"
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(uiState.connectedStats) { stat ->
                    PlatformRatingSummaryCard(
                        stat = stat,
                        onClick = { onPlatformClick(stat.platform) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Upcoming Contests Quick Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionHeader(title = "UPCOMING CONTESTS")
            TextButton(onClick = onViewAllContestsClick) {
                Text("View All", style = Typography.labelMedium)
            }
        }

        if (uiState.upcomingContests.isEmpty()) {
            EmptyState(message = "No upcoming contests scheduled.")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.upcomingContests.take(3).forEach { contest ->
                    QuickContestRow(
                        contest = contest,
                        onClick = { onContestClick(contest.id) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Featured Resource Slot
        uiState.featuredResource?.let { resource ->
            SectionHeader(title = "FOR YOU — RECOMMENDED")
            Spacer(modifier = Modifier.height(8.dp))
            FeaturedResourceCard(
                resource = resource,
                onClick = { onResourceClick(resource.url) }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Footer Last Updated Indicator
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            LastUpdatedLabel(timeAgo = uiState.lastUpdatedText)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun GitHubStatsOverviewCard(
    stats: GitHubStats,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🐱", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = stats.name ?: stats.username,
                            style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "@${stats.username}",
                            style = Typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF2EA44F).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "🔥 ${stats.currentContributionStreak}d Streak",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF2EA44F)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                GitHubStatTile(label = "Contributions", value = "${stats.totalContributionsThisYear}")
                GitHubStatTile(label = "Stars", value = "★ ${stats.totalStars}")
                GitHubStatTile(label = "Public Repos", value = "${stats.publicRepos}")
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Top Languages Pills
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Top:", style = Typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                stats.topLanguages.take(4).forEach { lang ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = lang,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = Typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GitHubStatTile(label: String, value: String) {
    Column {
        Text(text = label, style = Typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        Text(text = value, style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold))
    }
}

@Composable
fun HeroContestCountdownCard(
    contest: Contest,
    onCardClick: () -> Unit
) {
    var remainingSeconds by remember(contest.startTimeUtc) {
        mutableStateOf(
            Duration.between(Instant.now(), contest.startTimeUtc).seconds.coerceAtLeast(0)
        )
    }

    LaunchedEffect(contest.startTimeUtc) {
        while (remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds = Duration.between(Instant.now(), contest.startTimeUtc).seconds.coerceAtLeast(0)
        }
    }

    val hours = remainingSeconds / 3600
    val minutes = (remainingSeconds % 3600) / 60
    val seconds = remainingSeconds % 60
    val formattedTime = String.format("%02dh %02dm %02ds", hours, minutes, seconds)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = contest.platform.name,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    Text(
                        text = "NEXT CONTEST",
                        style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = contest.name,
                    style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "STARTS IN",
                            style = Typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = formattedTime,
                            style = Typography.headlineLarge.copy(fontWeight = FontWeight.Black),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlatformRatingSummaryCard(
    stat: PlatformStats,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = stat.platform.name,
                style = Typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stat.rating?.toString() ?: "—",
                style = Typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = stat.rank ?: "Rank Unrated",
                style = Typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Global #${stat.globalRank ?: "—"}",
                style = Typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun QuickContestRow(
    contest: Contest,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contest.platform.name,
                    style = Typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = contest.name,
                    style = Typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Duration: ${contest.durationSeconds / 60}m",
                    style = Typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = contest.status.name,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
fun FeaturedResourceCard(
    resource: Resource,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = resource.category,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = Typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Text(
                    text = resource.duration ?: "",
                    style = Typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = resource.title,
                style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = resource.description ?: "",
                style = Typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "By ${resource.creator ?: "Community"}",
                style = Typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
