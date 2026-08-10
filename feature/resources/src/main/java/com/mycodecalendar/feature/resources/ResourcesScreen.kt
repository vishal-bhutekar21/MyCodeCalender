package com.mycodecalendar.feature.resources

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowOutward
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
import com.mycodecalendar.domain.model.Resource

// Category-to-accent color mapping
private fun categoryColor(category: String): Color = when (category.lowercase()) {
    "algorithms" -> Color(0xFF6366F1)
    "data structures" -> Color(0xFF06B6D4)
    "dynamic programming" -> Color(0xFF8B5CF6)
    "graphs" -> Color(0xFF10B981)
    "math" -> Color(0xFFF59E0B)
    "competitive" -> Color(0xFFF43F5E)
    "system design" -> Color(0xFF64748B)
    else -> Color(0xFF6366F1)
}

@Composable
fun ResourcesScreen(
    resources: List<Resource>,
    onResourceClick: (String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    val categories = remember(resources) {
        resources.map { it.category }.distinct()
    }

    val filteredResources = remember(resources, selectedCategory) {
        if (selectedCategory == null) resources else resources.filter { it.category == selectedCategory }
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
                text = "Resources",
                style = Typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "${filteredResources.size} curated guides and tutorials",
                style = Typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Category filter chips with accent colors
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
        ) {
            item {
                CategoryChip(
                    label = "All",
                    selected = selectedCategory == null,
                    color = MaterialTheme.colorScheme.primary,
                    onClick = { selectedCategory = null }
                )
            }
            items(categories) { cat ->
                CategoryChip(
                    label = cat,
                    selected = selectedCategory == cat,
                    color = categoryColor(cat),
                    onClick = { selectedCategory = if (selectedCategory == cat) null else cat }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredResources.isEmpty()) {
            EmptyState(message = "No resources found in this category.")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp)
            ) {
                items(filteredResources) { resource ->
                    ResourceCard(
                        resource = resource,
                        onClick = { onResourceClick(resource.url) }
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryChip(
    label: String,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = if (selected) color.copy(alpha = 0.12f)
        else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (selected) color.copy(alpha = 0.45f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        )
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = Typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun ResourceCard(
    resource: Resource,
    onClick: () -> Unit
) {
    val accentColor = categoryColor(resource.category)

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
            // Left category color accent
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(
                        color = accentColor,
                        shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                    )
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(14.dp)
            ) {
                // Top meta row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        resource.platform?.let { platform ->
                            PlatformBadge(platform = platform)
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = accentColor.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp, accentColor.copy(alpha = 0.3f)
                            )
                        ) {
                            Text(
                                text = resource.category,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = accentColor
                            )
                        }
                    }
                    resource.duration?.let { dur ->
                        Text(
                            text = dur,
                            style = Typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = resource.title,
                    style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                resource.description?.let { desc ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = desc,
                        style = Typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = resource.creator ?: "Community",
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Open",
                            style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = Icons.Rounded.ArrowOutward,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
