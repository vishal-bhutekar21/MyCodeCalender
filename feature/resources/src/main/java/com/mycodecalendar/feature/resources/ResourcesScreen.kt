package com.mycodecalendar.feature.resources

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.automirrored.rounded.MenuBook
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
import com.mycodecalendar.core.designsystem.BrandPrimaryOrange
import com.mycodecalendar.core.designsystem.BrandPurpleAccent
import com.mycodecalendar.core.designsystem.GlassmorphismBackground
import com.mycodecalendar.core.designsystem.Typography
import com.mycodecalendar.core.designsystem.components.AppSearchBar
import com.mycodecalendar.core.designsystem.components.EmptyState
import com.mycodecalendar.core.designsystem.components.GlassCard
import com.mycodecalendar.core.designsystem.components.PlatformBadge
import com.mycodecalendar.core.designsystem.components.ResourcesListSkeleton
import com.mycodecalendar.domain.model.Resource

private fun getResourceCategoryIcon(category: String, isYouTube: Boolean): ImageVector = when {
    isYouTube -> Icons.Rounded.PlayCircle
    category.contains("AI", ignoreCase = true) || category.contains("ML", ignoreCase = true) -> Icons.Rounded.Psychology
    category.contains("DSA", ignoreCase = true) || category.contains("CP", ignoreCase = true) -> Icons.Rounded.Code
    category.contains("System", ignoreCase = true) -> Icons.Rounded.Storage
    else -> Icons.AutoMirrored.Rounded.MenuBook
}

/**
 * ResourcesScreen — Clean, refined, unified developer hub & education center.
 *
 * Design Improvements:
 * - Removed cluttered multiple rows of rainbow chips.
 * - Single, cohesive horizontal category selector in signature Brand Orange / Slate.
 * - 1px crisp bordered minimalist search bar.
 * - Unified card hierarchy with subtle category badges and clean action links.
 */
@Composable
fun ResourcesScreen(
    resources: List<Resource>,
    onResourceClick: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedCreator by remember { mutableStateOf<String?>(null) }

    val categories = remember {
        listOf("All", "AI & ML", "YouTube Playlists", "DSA & CP Sheets", "System Design")
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
                "AI & ML" -> res.category.contains("AI", ignoreCase = true) || res.category.contains("ML", ignoreCase = true)
                "YouTube Playlists" -> res.category.contains("YouTube", ignoreCase = true) || res.url.contains("youtube") || res.url.contains("youtu.be")
                "DSA & CP Sheets" -> res.category.contains("DSA", ignoreCase = true) || res.category.contains("CP", ignoreCase = true)
                "System Design" -> res.category.contains("System", ignoreCase = true)
                else -> res.category.equals(selectedCategory, ignoreCase = true)
            }

            val matchesCreator = selectedCreator == null || res.creator == selectedCreator

            matchesSearch && matchesCategory && matchesCreator
        }
    }

    GlassmorphismBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // ── TOP HEADER ─────────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 16.dp, bottom = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Developer Hub",
                        style = Typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BrandPrimaryOrange.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, BrandPrimaryOrange.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = "${filteredResources.size} Guides",
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                            style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                            color = BrandPrimaryOrange
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Curated AI tools, YouTube masterclasses, DSA sheets & roadmaps",
                    style = Typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── 1PX BORDERED MINIMALIST SEARCH BAR ─────────────────────────────────
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                AppSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search AI tools, playlists, DSA sheets, creators…"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── SINGLE COHESIVE CATEGORY TABS ROW ──────────────────────────────────
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = (selectedCategory == null && cat == "All") || (selectedCategory == cat)
                    CategoryPill(
                        label = cat,
                        selected = isSelected,
                        onClick = {
                            selectedCategory = if (cat == "All") null else cat
                        }
                    )
                }
            }

            // Optional Active Creator Filter Indicator (Clean & Minimal)
            AnimatedVisibility(
                visible = selectedCreator != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Filtered by creator:",
                            style = Typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = BrandPurpleAccent.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, BrandPurpleAccent.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = selectedCreator ?: "",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.5.sp),
                                color = BrandPurpleAccent
                            )
                        }
                    }

                    Text(
                        text = "Clear filter",
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = BrandPrimaryOrange),
                        modifier = Modifier.clickable { selectedCreator = null }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── RESOURCE CARDS LIST ────────────────────────────────────────────────
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
                        RefinedResourceCard(
                            resource = resource,
                            onCreatorFilter = { creator ->
                                selectedCreator = if (selectedCreator == creator) null else creator
                            },
                            onClick = { onResourceClick(resource.url) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Cohesive Category Pill with unified Brand Orange highlight.
 */
@Composable
private fun CategoryPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (selected) BrandPrimaryOrange.copy(alpha = 0.16f)
    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f)

    val borderColor = if (selected) BrandPrimaryOrange.copy(alpha = 0.65f)
    else MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)

    val textColor = if (selected) BrandPrimaryOrange
    else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(bgColor)
            .border(1.dp, borderColor, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(BrandPrimaryOrange, CircleShape)
                )
            }
            Text(
                text = label,
                style = Typography.labelMedium.copy(
                    fontWeight = if (selected) FontWeight.Black else FontWeight.SemiBold,
                    fontSize = 12.5.sp
                ),
                color = textColor
            )
        }
    }
}

/**
 * Refined Resource Card — Elegant, unified styling without rainbow clutter.
 */
@Composable
private fun RefinedResourceCard(
    resource: Resource,
    onCreatorFilter: (String) -> Unit,
    onClick: () -> Unit
) {
    val isYouTube = resource.url.contains("youtube.com") || resource.url.contains("youtu.be")
    val categoryIcon = getResourceCategoryIcon(resource.category, isYouTube)

    val badgeColor = when {
        isYouTube -> Color(0xFFFF5252)
        resource.category.contains("AI", ignoreCase = true) -> Color(0xFF06B6D4)
        resource.category.contains("System", ignoreCase = true) -> BrandPurpleAccent
        else -> BrandPrimaryOrange
    }

    val ctaText = when {
        isYouTube -> "Watch Video ↗"
        resource.category.contains("AI", ignoreCase = true) -> "Launch Tool ↗"
        resource.category.contains("DSA", ignoreCase = true) || resource.category.contains("CP", ignoreCase = true) -> "Open Sheet ↗"
        else -> "Open Guide ↗"
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        accentColor = badgeColor,
        cornerRadius = 18.dp,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Top Meta Row (Platform/Category Badge + Duration)
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
                        color = badgeColor.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = categoryIcon,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = badgeColor
                            )
                            Text(
                                text = if (isYouTube) "YouTube Masterclass" else resource.category,
                                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.5.sp),
                                color = badgeColor
                            )
                        }
                    }
                }

                resource.duration?.let { dur ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
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

            // Title
            Text(
                text = resource.title,
                style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 14.5.sp),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Description
            resource.description?.let { desc ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = desc,
                    style = Typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Action Row (Creator tag & Clean Orange Link)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Creator tag (clickable to filter)
                resource.creator?.let { creator ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onCreatorFilter(creator) }
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Text(
                            text = creator,
                            style = Typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } ?: Spacer(modifier = Modifier.size(1.dp))

                // CTA Link
                Text(
                    text = ctaText,
                    style = Typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp,
                        color = BrandPrimaryOrange
                    )
                )
            }
        }
    }
}
