package com.mycodecalendar.feature.resources

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycodecalendar.core.designsystem.GlassmorphismBackground
import com.mycodecalendar.core.designsystem.Typography
import com.mycodecalendar.core.designsystem.components.EmptyState
import com.mycodecalendar.core.designsystem.components.GlassCard
import com.mycodecalendar.core.designsystem.components.GlassChip
import com.mycodecalendar.core.designsystem.components.PlatformBadge
import com.mycodecalendar.core.designsystem.components.ResourcesListSkeleton
import com.mycodecalendar.domain.model.Resource

private fun getCategoryColor(category: String): Color = when {
    category.contains("AI", ignoreCase = true) || category.contains("ML", ignoreCase = true) -> Color(0xFF06B6D4)
    category.contains("YouTube", ignoreCase = true) -> Color(0xFFFF0000)
    category.contains("DSA", ignoreCase = true) || category.contains("CP", ignoreCase = true) -> Color(0xFF818CF8)
    category.contains("System", ignoreCase = true) -> Color(0xFFF59E0B)
    category.contains("Algorithms", ignoreCase = true) -> Color(0xFF8B5CF6)
    category.contains("Graphs", ignoreCase = true) -> Color(0xFF10F07B)
    else -> Color(0xFF818CF8)
}

private fun getCategoryIcon(category: String, isYouTube: Boolean): ImageVector = when {
    isYouTube -> Icons.Rounded.PlayCircle
    category.contains("AI", ignoreCase = true) || category.contains("ML", ignoreCase = true) -> Icons.Rounded.Psychology
    category.contains("DSA", ignoreCase = true) || category.contains("CP", ignoreCase = true) -> Icons.Rounded.Code
    category.contains("System", ignoreCase = true) -> Icons.Rounded.Storage
    else -> Icons.Rounded.Description
}

/**
 * ResourcesScreen — Modern, aesthetic, and minimal learning hub featuring:
 * - AI & Machine Learning Tools (Hugging Face, Google Colab, Kaggle, OpenAI, PyTorch, Ollama, v0)
 * - Masterclass YouTube Playlists (Striver, NeetCode, Andrej Karpathy, 3Blue1Brown, StatQuest, Babbar)
 * - Curated DSA & CP Problem Sheets (Striver SDE 180, CSES 300, USACO Guide, CP-Algorithms)
 * - System Design Primers & Roadmaps
 */
@Composable
fun ResourcesScreen(
    resources: List<Resource>,
    onResourceClick: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedCreator by remember { mutableStateOf<String?>(null) }

    val categories = remember(resources) {
        listOf("All", "AI & ML Tools", "YouTube Playlists", "DSA & CP Sheets", "System Design")
    }

    val creators = remember(resources) {
        resources.mapNotNull { it.creator }.distinct()
    }

    val filteredResources = remember(resources, searchQuery, selectedCategory, selectedCreator) {
        resources.filter { res ->
            val matchesSearch = searchQuery.isBlank() ||
                res.title.contains(searchQuery, ignoreCase = true) ||
                (res.description?.contains(searchQuery, ignoreCase = true) == true) ||
                (res.creator?.contains(searchQuery, ignoreCase = true) == true) ||
                res.category.contains(searchQuery, ignoreCase = true)

            val matchesCategory = when (selectedCategory) {
                null, "All" -> true
                else -> res.category.equals(selectedCategory, ignoreCase = true)
            }

            val matchesCreator = selectedCreator == null || res.creator == selectedCreator

            matchesSearch && matchesCategory && matchesCreator
        }
    }

    GlassmorphismBackground {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // ── TOP HEADER ─────────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 20.dp, bottom = 4.dp)
            ) {
                Text(
                    text = "Developer Hub & Education",
                    style = Typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "AI platforms, YouTube playlists, DSA sheets, and system design",
                    style = Typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── SEARCH BAR ─────────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 14.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.Search,
                            contentDescription = "Search",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    "Search AI tools, YouTube playlists, sheets…",
                                    style = Typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            textStyle = Typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Close,
                                    contentDescription = "Clear",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ── MAIN CATEGORY TABS ─────────────────────────────────────────────────
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = (selectedCategory == null && cat == "All") || (selectedCategory == cat)
                    val catColor = if (cat == "All") MaterialTheme.colorScheme.primary else getCategoryColor(cat)
                    GlassChip(
                        label = cat,
                        selected = isSelected,
                        accentColor = catColor,
                        onClick = {
                            selectedCategory = if (cat == "All") null else cat
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // ── CREATORS FILTER ROW ────────────────────────────────────────────────
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp)
            ) {
                item {
                    GlassChip(
                        label = "All Creators",
                        selected = selectedCreator == null,
                        accentColor = MaterialTheme.colorScheme.primary,
                        onClick = { selectedCreator = null }
                    )
                }
                items(creators) { creator ->
                    GlassChip(
                        label = creator,
                        selected = selectedCreator == creator,
                        accentColor = Color(0xFFA78BFA),
                        onClick = { selectedCreator = if (selectedCreator == creator) null else creator }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── FEED LIST ──────────────────────────────────────────────────────────
            if (resources.isEmpty()) {
                ResourcesListSkeleton()
            } else if (filteredResources.isEmpty()) {
                EmptyState(message = "No learning resources match your search or filter.")
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp)
                ) {
                    items(filteredResources, key = { it.id }) { resource ->
                        ResourceCard(
                            resource = resource,
                            onClick = { onResourceClick(resource.url) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ResourceCard(
    resource: Resource,
    onClick: () -> Unit
) {
    val isYouTube = resource.url.contains("youtube.com") || resource.url.contains("youtu.be")
    val accentColor = getCategoryColor(resource.category)
    val categoryIcon = getCategoryIcon(resource.category, isYouTube)

    val ctaText = when {
        isYouTube -> "Watch Playlist"
        resource.category.contains("AI", ignoreCase = true) -> "Launch Tool"
        resource.category.contains("DSA", ignoreCase = true) || resource.category.contains("CP", ignoreCase = true) -> "Open Sheet"
        else -> "Open Guide"
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        accentColor = if (isYouTube) Color(0xFFFF0000) else accentColor,
        cornerRadius = 18.dp,
        onClick = onClick
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Left Glow Indicator Strip
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(
                        color = if (isYouTube) Color(0xFFFF0000) else accentColor,
                        shape = RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp)
                    )
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(14.dp)
            ) {
                // Top Meta Row
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

                        // Category Badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = (if (isYouTube) Color(0xFFFF0000) else accentColor).copy(alpha = 0.14f),
                            border = BorderStroke(
                                1.dp, (if (isYouTube) Color(0xFFFF0000) else accentColor).copy(alpha = 0.35f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = categoryIcon,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = if (isYouTube) Color(0xFFFF0000) else accentColor
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isYouTube) "YouTube Masterclass" else resource.category,
                                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.5.sp),
                                    color = if (isYouTube) Color(0xFFFF0000) else accentColor
                                )
                            }
                        }
                    }

                    resource.duration?.let { dur ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        ) {
                            Text(
                                text = dur,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = Typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Creator Credits & Action CTA
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        border = BorderStroke(
                            0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = resource.creator ?: "Community",
                                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = ctaText,
                            style = Typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isYouTube) Color(0xFFFF0000) else accentColor
                            )
                        )
                        Icon(
                            imageVector = Icons.Rounded.ArrowOutward,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (isYouTube) Color(0xFFFF0000) else accentColor
                        )
                    }
                }
            }
        }
    }
}
