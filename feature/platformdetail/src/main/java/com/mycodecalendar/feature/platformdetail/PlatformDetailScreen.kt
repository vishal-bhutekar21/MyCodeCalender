package com.mycodecalendar.feature.platformdetail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mycodecalendar.core.designsystem.Typography
import com.mycodecalendar.core.designsystem.components.PlatformBadge
import com.mycodecalendar.core.designsystem.components.SectionHeader
import com.mycodecalendar.core.designsystem.components.getBrandColor
import com.mycodecalendar.domain.model.PlatformStats
import com.mycodecalendar.domain.model.RatingPoint

@Composable
fun PlatformDetailScreen(
    stats: PlatformStats?,
    ratingHistory: List<RatingPoint>,
    onBackClick: () -> Unit
) {
    val brandColor = stats?.platform?.getBrandColor() ?: MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Navigation bar
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

        if (stats == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Platform account not found.",
                    style = Typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return
        }

        // Hero header card with brand gradient
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(
                    width = 1.dp,
                    color = brandColor.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(20.dp)
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                brandColor.copy(alpha = 0.12f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PlatformBadge(platform = stats.platform)
                        stats.rank?.let { rank ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = brandColor.copy(alpha = 0.12f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp, brandColor.copy(alpha = 0.35f)
                                )
                            ) {
                                Text(
                                    text = rank,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = brandColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "@${stats.username}",
                        style = Typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    stats.rating?.let { rating ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = rating.toString(),
                            style = Typography.headlineLarge.copy(fontWeight = FontWeight.Black),
                            color = brandColor
                        )
                        Text(
                            text = "current rating",
                            style = Typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stats grid (2x2)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                icon = Icons.Rounded.BarChart,
                title = "Highest Rating",
                value = stats.highestRating?.toString() ?: "—",
                accentColor = brandColor,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Rounded.Language,
                title = "Global Rank",
                value = stats.globalRank?.let { "#$it" } ?: "—",
                accentColor = Color(0xFF06B6D4),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                icon = Icons.Rounded.LocalFireDepartment,
                title = "Streak",
                value = stats.currentStreak?.let { "$it days" } ?: "—",
                accentColor = Color(0xFFF59E0B),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Rounded.EmojiEvents,
                title = "Problems Solved",
                value = stats.solved?.toString() ?: "—",
                accentColor = Color(0xFF10B981),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Rating progression chart
        SectionHeader(
            title = "Rating Progression",
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(18.dp)
                ),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                if (ratingHistory.isEmpty()) {
                    Text(
                        text = "No rating history available.",
                        style = Typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                } else {
                    RatingLineChart(
                        points = ratingHistory,
                        lineColor = brandColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Problem difficulty breakdown
        if (stats.easySolved != null || stats.mediumSolved != null || stats.hardSolved != null) {
            SectionHeader(
                title = "Problems Solved",
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(18.dp)
                    ),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Total: ${stats.solved ?: 0}",
                        style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stacked progress bar
                    val easy = (stats.easySolved ?: 0).toFloat()
                    val medium = (stats.mediumSolved ?: 0).toFloat()
                    val hard = (stats.hardSolved ?: 0).toFloat()
                    val total = (easy + medium + hard).coerceAtLeast(1f)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                            )
                    ) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            if (easy > 0) Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(easy / total)
                                    .background(
                                        color = Color(0xFF10B981),
                                        shape = RoundedCornerShape(
                                            topStart = 4.dp, bottomStart = 4.dp,
                                            topEnd = if (medium == 0f && hard == 0f) 4.dp else 0.dp,
                                            bottomEnd = if (medium == 0f && hard == 0f) 4.dp else 0.dp
                                        )
                                    )
                            )
                            if (medium > 0) Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(medium / total)
                                    .background(color = Color(0xFFF59E0B))
                            )
                            if (hard > 0) Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(hard / total)
                                    .background(
                                        color = Color(0xFFF43F5E),
                                        shape = RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DifficultyLabel("Easy", stats.easySolved ?: 0, Color(0xFF10B981))
                        DifficultyLabel("Medium", stats.mediumSolved ?: 0, Color(0xFFF59E0B))
                        DifficultyLabel("Hard", stats.hardSolved ?: 0, Color(0xFFF43F5E))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun StatCard(
    icon: ImageVector,
    title: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            shape = RoundedCornerShape(14.dp)
        ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = accentColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = Typography.labelSmall,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = Typography.titleMedium.copy(fontWeight = FontWeight.Black),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun DifficultyLabel(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color = color, shape = CircleShape)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = Typography.labelSmall,
            color = color
        )
        Text(
            text = count.toString(),
            style = Typography.titleSmall.copy(fontWeight = FontWeight.Black),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun RatingLineChart(
    points: List<RatingPoint>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    val fillColor = lineColor

    Canvas(modifier = modifier) {
        val ratings = points.map { it.rating }
        val minRating = (ratings.minOrNull() ?: 1000) - 100
        val maxRating = (ratings.maxOrNull() ?: 2000) + 100
        val ratingRange = (maxRating - minRating).coerceAtLeast(1)

        val w = size.width
        val h = size.height
        val spacing = w / (points.size - 1).coerceAtLeast(1)

        val coords = points.mapIndexed { index, point ->
            val x = index * spacing
            val y = h - ((point.rating - minRating).toFloat() / ratingRange * h)
            Offset(x, y)
        }

        // Gradient fill under the line
        val fillPath = Path().apply {
            moveTo(coords.first().x, h)
            coords.forEach { lineTo(it.x, it.y) }
            lineTo(coords.last().x, h)
            close()
        }
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(fillColor.copy(alpha = 0.3f), Color.Transparent),
                startY = 0f,
                endY = h
            )
        )

        // Line stroke
        val linePath = Path().apply {
            coords.forEachIndexed { i, offset ->
                if (i == 0) moveTo(offset.x, offset.y) else lineTo(offset.x, offset.y)
            }
        }
        drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Data point dots
        coords.forEach { offset ->
            drawCircle(color = lineColor, radius = 4.dp.toPx(), center = offset)
            drawCircle(
                color = Color.White.copy(alpha = 0.9f),
                radius = 2.dp.toPx(),
                center = offset
            )
        }
    }
}
