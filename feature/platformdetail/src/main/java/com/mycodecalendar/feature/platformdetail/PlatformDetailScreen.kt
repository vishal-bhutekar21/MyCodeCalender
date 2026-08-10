package com.mycodecalendar.feature.platformdetail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mycodecalendar.core.designsystem.Typography
import com.mycodecalendar.domain.model.Platform
import com.mycodecalendar.domain.model.PlatformStats
import com.mycodecalendar.domain.model.RatingPoint

@Composable
fun PlatformDetailScreen(
    stats: PlatformStats?,
    ratingHistory: List<RatingPoint>,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        TextButton(onClick = onBackClick) {
            Text("← Back", style = Typography.labelLarge)
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (stats == null) {
            Text("Platform account details not found.", style = Typography.bodyLarge)
            return
        }

        // Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stats.platform.name,
                    style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = stats.username,
                    style = Typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stats.badge ?: stats.rank ?: "Rank Unrated",
                    style = Typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Key Stats Cards Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = "CURRENT RATING",
                value = stats.rating?.toString() ?: "—",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "HIGHEST RATING",
                value = stats.highestRating?.toString() ?: "—",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = "GLOBAL RANK",
                value = stats.globalRank?.let { "#$it" } ?: "—",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "STREAK",
                value = stats.currentStreak?.let { "$it Days 🔥" } ?: "—",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Rating History Canvas Line Chart
        Text(
            text = "RATING PROGRESSION",
            style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.outline
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (ratingHistory.isEmpty()) {
                    Text("No rating history data available.")
                } else {
                    RatingLineChart(
                        points = ratingHistory,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Solved Problems Breakdown
        Text(
            text = "SOLVED PROBLEMS",
            style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.outline
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Total Solved: ${stats.solved ?: "—"}",
                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DifficultyPill(label = "Easy", count = stats.easySolved ?: 0, color = Color(0xFF4CAF50))
                    DifficultyPill(label = "Medium", count = stats.mediumSolved ?: 0, color = Color(0xFFFF9800))
                    DifficultyPill(label = "Hard", count = stats.hardSolved ?: 0, color = Color(0xFFF44336))
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, style = Typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun DifficultyPill(label: String, count: Int, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, style = Typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
            Text(text = count.toString(), style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = color)
        }
    }
}

@Composable
fun RatingLineChart(points: List<RatingPoint>, modifier: Modifier = Modifier) {
    val lineColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        val ratings = points.map { it.rating }
        val minRating = (ratings.minOrNull() ?: 1000) - 100
        val maxRating = (ratings.maxOrNull() ?: 2000) + 100
        val ratingRange = (maxRating - minRating).coerceAtLeast(1)

        val width = size.width
        val height = size.height
        val pointSpacing = width / (points.size - 1).coerceAtLeast(1)

        val path = Path()
        val coordinates = points.mapIndexed { index, point ->
            val x = index * pointSpacing
            val y = height - ((point.rating - minRating).toFloat() / ratingRange * height)
            Offset(x, y)
        }

        coordinates.forEachIndexed { index, offset ->
            if (index == 0) {
                path.moveTo(offset.x, offset.y)
            } else {
                path.lineTo(offset.x, offset.y)
            }
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 4.dp.toPx())
        )

        coordinates.forEach { offset ->
            drawCircle(
                color = lineColor,
                radius = 6.dp.toPx(),
                center = offset
            )
        }
    }
}
