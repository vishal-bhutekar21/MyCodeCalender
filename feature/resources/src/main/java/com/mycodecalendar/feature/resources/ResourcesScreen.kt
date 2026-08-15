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
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mycodecalendar.core.designsystem.BrandPrimaryOrange
import com.mycodecalendar.core.designsystem.BrandPurpleAccent
import com.mycodecalendar.core.designsystem.GlassmorphismBackground
import com.mycodecalendar.core.designsystem.Typography
import com.mycodecalendar.core.designsystem.components.AppSearchBar
import com.mycodecalendar.core.designsystem.components.EmptyState
import com.mycodecalendar.core.designsystem.components.GlassCard
import com.mycodecalendar.core.designsystem.components.PlatformBadge
import com.mycodecalendar.core.designsystem.components.ResourcesListSkeleton
import com.mycodecalendar.core.designsystem.components.getBrandColor
import com.mycodecalendar.domain.model.Platform
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
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourcesScreen(
    resources: List<Resource>,
    onResourceClick: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedCreator by remember { mutableStateOf<String?>(null) }
    var activeSheetResource by remember { mutableStateOf<Resource?>(null) }
    var cloudMaterials by remember { mutableStateOf<List<Resource>>(emptyList()) }

    // Fetch dynamic live materials from Cloud Firestore (Web Admin CMS)
    LaunchedEffect(Unit) {
        try {
            FirebaseFirestore.getInstance()
                .collection("featured_materials")
                .whereEqualTo("isActive", true)
                .orderBy("priority", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener { snapshot ->
                    val cloudItems = snapshot.documents.mapNotNull { doc ->
                        try {
                            Resource(
                                id = "cloud_${doc.id}",
                                title = doc.getString("title") ?: "",
                                description = doc.getString("description"),
                                creator = "Featured CMS",
                                url = doc.getString("redirectUrl") ?: "",
                                category = doc.getString("category") ?: "DSA & CP Sheets",
                                platform = null,
                                duration = null,
                                priority = doc.getLong("priority")?.toInt() ?: 1,
                                thumbnailUrl = doc.getString("imageUrl"),
                                publishedAt = null
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    if (cloudItems.isNotEmpty()) {
                        cloudMaterials = cloudItems
                    }
                }
        } catch (e: Exception) {
            // Non-blocking fallback
        }
    }

    val combinedResources = remember(resources, cloudMaterials) {
        (cloudMaterials + resources).distinctBy { it.url.ifBlank { it.id } }
    }

    val categories = remember {
        listOf("All", "AI & ML", "YouTube Playlists", "DSA & CP Sheets", "System Design")
    }

    val creators = remember(combinedResources) {
        combinedResources.mapNotNull { it.creator }.distinct()
    }

    val filteredResources = remember(combinedResources, searchQuery, selectedCategory, selectedCreator) {
        combinedResources.filter { res ->
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

    var selectedMainTab by remember { mutableStateOf(0) } // 0 = Guides & Roadmaps, 1 = Practice Problem Sets
    var selectedProblemPlatform by remember { mutableStateOf("All") }

    val filteredPracticeSheets = remember(searchQuery, selectedProblemPlatform) {
        curatedPracticeSheets.filter { sheet ->
            val matchesQuery = searchQuery.isBlank() ||
                sheet.title.contains(searchQuery, ignoreCase = true) ||
                sheet.description.contains(searchQuery, ignoreCase = true) ||
                sheet.platformName.contains(searchQuery, ignoreCase = true) ||
                sheet.tags.any { it.contains(searchQuery, ignoreCase = true) }

            val matchesPlatform = when (selectedProblemPlatform) {
                "All" -> true
                "LeetCode & NeetCode" -> sheet.platform == Platform.LEETCODE
                "Codeforces" -> sheet.platform == Platform.CODEFORCES
                "AtCoder" -> sheet.platform == Platform.ATCODER
                "HackerRank & Other" -> sheet.platform == Platform.GEEKSFORGEEKS || sheet.platform == Platform.CODECHEF
                else -> true
            }

            matchesQuery && matchesPlatform
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
                        border = BorderStroke(0.1.dp, BrandPrimaryOrange.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = if (selectedMainTab == 0) "${filteredResources.size} Guides" else "${filteredPracticeSheets.size} Sheets",
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                            style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                            color = BrandPrimaryOrange
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Curated AI tools, YouTube masterclasses, top 300 problem sheets & roadmaps",
                    style = Typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── PRIMARY HUB SEGMENTED TOGGLE (Guides vs Practice Problem Sheets) ───
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HubSegmentPill(
                    label = "Guides & Roadmaps",
                    selected = selectedMainTab == 0,
                    badgeCount = filteredResources.size,
                    onClick = { selectedMainTab = 0 },
                    modifier = Modifier.weight(1f)
                )

                HubSegmentPill(
                    label = "⚡ Top Practice Sets",
                    selected = selectedMainTab == 1,
                    badgeCount = curatedPracticeSheets.size,
                    onClick = { selectedMainTab = 1 },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── 1PX BORDERED MINIMALIST SEARCH BAR ─────────────────────────────────
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                AppSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = if (selectedMainTab == 0) "Search AI tools, playlists, DSA sheets, creators…"
                    else "Search NeetCode 150, CSES, Blind 75, Ladders, DP…"
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (selectedMainTab == 0) {
                // ── SINGLE COHESIVE CATEGORY TABS ROW (Guides) ─────────────────────
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
                                border = BorderStroke(0.1.dp, BrandPurpleAccent.copy(alpha = 0.4f))
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

                // ── RESOURCE CARDS LIST ────────────────────────────────────────────
                if (resources.isEmpty()) {
                    ResourcesListSkeleton()
                } else if (filteredResources.isEmpty()) {
                    EmptyState(message = "No learning resources match your search or filter.")
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 100.dp)
                    ) {
                        items(filteredResources, key = { it.id }) { resource ->
                            RefinedResourceCard(
                                resource = resource,
                                onCreatorFilter = { creator ->
                                    selectedCreator = if (selectedCreator == creator) null else creator
                                },
                                onClick = { activeSheetResource = resource }
                            )
                        }
                    }
                }
            } else {
                // ── PRACTICE PROBLEM SETS TAB ──────────────────────────────────────
                val problemPlatforms = listOf(
                    "All",
                    "LeetCode & NeetCode",
                    "Codeforces",
                    "AtCoder",
                    "HackerRank & Other"
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp)
                ) {
                    items(problemPlatforms) { plat ->
                        val isSelected = selectedProblemPlatform == plat
                        CategoryPill(
                            label = plat,
                            selected = isSelected,
                            onClick = { selectedProblemPlatform = plat }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (filteredPracticeSheets.isEmpty()) {
                    EmptyState(message = "No practice sheets match your search query.")
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 100.dp)
                    ) {
                        items(filteredPracticeSheets, key = { it.id }) { sheet ->
                            PracticeSheetCard(
                                sheet = sheet,
                                onOpen = { onResourceClick(sheet.url) }
                            )
                        }
                    }
                }
            }
        }

        // ── IN-APP DETAIL BOTTOM SHEET ─────────────────────────────────────────
        activeSheetResource?.let { res ->
            ResourceDetailBottomSheet(
                resource = res,
                onDismiss = { activeSheetResource = null },
                onOpenLink = { url ->
                    activeSheetResource = null
                    onResourceClick(url)
                }
            )
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
            .border(0.1.dp, borderColor, CircleShape)
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
 * Refined Resource Card — Ultra-minimal, modern layout with 0.1.dp card border.
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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(0.1.dp, badgeColor.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 18.dp,
            accentColor = badgeColor,
            onClick = onClick
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Minimal Category / Type Icon or Cloud Remote Image Container
                if (!resource.thumbnailUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = resource.thumbnailUrl,
                        contentDescription = resource.title,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(0.1.dp, badgeColor.copy(alpha = 0.35f), RoundedCornerShape(14.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(badgeColor.copy(alpha = 0.12f))
                            .border(0.1.dp, badgeColor.copy(alpha = 0.35f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = badgeColor
                        )
                    }
                }

            // Text Content Column (Clean & uncluttered)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                if (resource.priority <= 2 || resource.creator == "Featured CMS") {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = BrandPrimaryOrange.copy(alpha = 0.18f),
                        modifier = Modifier.padding(bottom = 3.dp)
                    ) {
                        Text(
                            text = if (resource.priority == 1) "⭐ TOP PICK" else "⚡ FEATURED",
                            style = Typography.labelSmall.copy(fontSize = 8.5.sp, fontWeight = FontWeight.Black),
                            color = BrandPrimaryOrange,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }

                Text(
                    text = resource.title,
                    style = Typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp,
                        lineHeight = 19.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                resource.description?.let { desc ->
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = desc,
                        style = Typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Single clean footer line: "By [Creator] · [Duration]"
                val metadataParts = buildList {
                    resource.creator?.let { add("By $it") }
                    resource.duration?.let { add(it) }
                    if (isEmpty()) add(resource.category)
                }

                Text(
                    text = metadataParts.joinToString(" · "),
                    style = Typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Minimal Trailing Arrow Indicator
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = "Open",
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
            )
        }
    }
}
}

/**
 * In-App Developer Hub Resource Detail Bottom Sheet.
 * Allows developers to preview the full curriculum, author, and description before opening externally.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResourceDetailBottomSheet(
    resource: Resource,
    onDismiss: () -> Unit,
    onOpenLink: (String) -> Unit
) {
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    val isYouTube = resource.url.contains("youtube.com") || resource.url.contains("youtu.be")
    val categoryIcon = getResourceCategoryIcon(resource.category, isYouTube)

    val badgeColor = when {
        isYouTube -> Color(0xFFFF5252)
        resource.category.contains("AI", ignoreCase = true) -> Color(0xFF06B6D4)
        resource.category.contains("System", ignoreCase = true) -> BrandPurpleAccent
        else -> BrandPrimaryOrange
    }

    val actionButtonText = when {
        isYouTube -> "Watch Masterclass on YouTube"
        resource.category.contains("DSA", ignoreCase = true) || resource.category.contains("CP", ignoreCase = true) -> "Open DSA Sheet"
        resource.category.contains("AI", ignoreCase = true) -> "Launch AI Tool"
        else -> "Open Resource"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
        ) {
            // Badges Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    resource.platform?.let { platform ->
                        PlatformBadge(platform = platform)
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = badgeColor.copy(alpha = 0.15f),
                        border = BorderStroke(0.1.dp, badgeColor.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = categoryIcon,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = badgeColor
                            )
                            Text(
                                text = if (isYouTube) "YouTube Masterclass" else resource.category,
                                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                color = badgeColor
                            )
                        }
                    }
                }

                resource.duration?.let { dur ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        border = BorderStroke(0.1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    ) {
                        Text(
                            text = dur,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = Typography.labelSmall.copy(fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Title
            Text(
                text = resource.title,
                style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp),
                color = MaterialTheme.colorScheme.onSurface
            )

            // Creator / Author Tag
            resource.creator?.let { creator ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Verified,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = BrandPrimaryOrange
                    )
                    Text(
                        text = "Curated by $creator",
                        style = Typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Description
            resource.description?.let { desc ->
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = desc,
                    style = Typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                    lineHeight = 22.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Copy Link Button
                OutlinedButton(
                    onClick = {
                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(resource.url))
                        copied = true
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(0.1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Icon(
                        if (copied) Icons.Rounded.Check else Icons.Rounded.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (copied) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (copied) "Copied!" else "Copy Link",
                        style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                // Primary Launch / Open Button
                Button(
                    onClick = { onOpenLink(resource.url) },
                    modifier = Modifier
                        .weight(1.5f)
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandPrimaryOrange,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        Icons.AutoMirrored.Rounded.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = actionButtonText,
                        style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Top Segmented Navigation Pill between Guides and Practice Problem Sets.
 */
@Composable
private fun HubSegmentPill(
    label: String,
    selected: Boolean,
    badgeCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (selected) BrandPrimaryOrange.copy(alpha = 0.18f)
    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)

    val borderColor = if (selected) BrandPrimaryOrange.copy(alpha = 0.60f)
    else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = bgColor,
        border = BorderStroke(0.1.dp, borderColor),
        modifier = modifier.height(44.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                style = Typography.labelMedium.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 12.sp
                ),
                color = if (selected) BrandPrimaryOrange else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                maxLines = 1
            )
            if (badgeCount > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (selected) BrandPrimaryOrange else MaterialTheme.colorScheme.outline.copy(alpha = 0.20f)
                ) {
                    Text(
                        text = badgeCount.toString(),
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 9.5.sp),
                        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Data model for famous curated problem sheets (NeetCode 150, Blind 75, CSES, Ladders, etc.).
 */
data class TopPracticeSheet(
    val id: String,
    val title: String,
    val description: String,
    val problemCount: String,
    val platform: Platform,
    val platformName: String,
    val url: String,
    val tags: List<String>,
    val difficulty: String = "All Levels"
)

/**
 * Dedicated Card for Top Platform Practice Problem Sets (0.3px brand border, 1-tap open).
 */
@Composable
private fun PracticeSheetCard(
    sheet: TopPracticeSheet,
    onOpen: () -> Unit
) {
    val brandColor = sheet.platform.getBrandColor()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(0.1.dp, brandColor.copy(alpha = 0.40f), RoundedCornerShape(18.dp))
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 18.dp,
            accentColor = brandColor,
            onClick = onOpen
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Top Row: Platform Badge + Problem Count Chip (0.1.dp border)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PlatformBadge(platform = sheet.platform)

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = brandColor.copy(alpha = 0.15f),
                        border = BorderStroke(0.1.dp, brandColor.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = sheet.problemCount,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.5.sp),
                            color = brandColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Title
                Text(
                    text = sheet.title,
                    style = Typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        lineHeight = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Description
                Text(
                    text = sheet.description,
                    style = Typography.bodySmall.copy(fontSize = 11.5.sp, lineHeight = 16.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Footer Row: Tags + 1-Tap Open Button (0.1.dp borders)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        sheet.tags.take(2).forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f),
                                border = BorderStroke(0.1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.20f))
                            ) {
                                Text(
                                    text = tag,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = Typography.labelSmall.copy(fontSize = 9.5.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                                )
                            }
                        }
                    }

                    Surface(
                        onClick = onOpen,
                        shape = RoundedCornerShape(10.dp),
                        color = brandColor.copy(alpha = 0.16f),
                        border = BorderStroke(0.1.dp, brandColor.copy(alpha = 0.40f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Practice Now",
                                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                color = brandColor
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = brandColor
                            )
                        }
                    }
                }
            }
        }
    }
}

val curatedPracticeSheets = listOf(
    // ── LEETCODE & NEETCODE ──────────────────────────────────────────────────
    TopPracticeSheet(
        id = "neetcode_150",
        title = "NeetCode 150 — Comprehensive DSA Practice",
        description = "Curated 150 must-solve LeetCode problems covering all 18 core DSA patterns with full video walk-throughs.",
        problemCount = "150 Problems",
        platform = Platform.LEETCODE,
        platformName = "NeetCode / LeetCode",
        url = "https://neetcode.io/practice",
        tags = listOf("NeetCode", "Blind 75", "Core DSA", "FAANG"),
        difficulty = "Easy to Hard"
    ),
    TopPracticeSheet(
        id = "blind_75",
        title = "Blind 75 — Essential Tech Interview Questions",
        description = "The definitive 75 LeetCode questions by Yangshun Tay covering Arrays, DP, Graphs, and Trees for rapid interview prep.",
        problemCount = "75 Problems",
        platform = Platform.LEETCODE,
        platformName = "LeetCode",
        url = "https://leetcode.com/discuss/general-discussion/460599/blind-75-leetcode-questions",
        tags = listOf("Blind 75", "Interview Prep", "Must-Solve"),
        difficulty = "Medium Focused"
    ),
    TopPracticeSheet(
        id = "lc_top_150",
        title = "LeetCode Top Interview 150",
        description = "Official LeetCode curated collection of the top 150 interview problems asked at Google, Amazon, Meta, and Microsoft.",
        problemCount = "150 Problems",
        platform = Platform.LEETCODE,
        platformName = "LeetCode Official",
        url = "https://leetcode.com/studyplan/top-interview-150/",
        tags = listOf("Top Interview", "Official", "Big Tech"),
        difficulty = "All Levels"
    ),
    TopPracticeSheet(
        id = "grind_75",
        title = "Grind 75 — Ranked Coding Interview Roadmap",
        description = "Next-generation evolution of Blind 75 with customizable time schedules, topic ordering, and difficulty filters.",
        problemCount = "75 Problems",
        platform = Platform.LEETCODE,
        platformName = "Tech Interview Handbook",
        url = "https://www.techinterviewhandbook.org/grind75",
        tags = listOf("Grind 75", "FAANG", "Timed Study"),
        difficulty = "Customizable"
    ),
    TopPracticeSheet(
        id = "lc_sql_50",
        title = "LeetCode SQL 50 — Database Query Mastery",
        description = "Top 50 curated SQL problems for software developers and data engineers covering JOINS, subqueries, and window functions.",
        problemCount = "50 Problems",
        platform = Platform.LEETCODE,
        platformName = "LeetCode SQL",
        url = "https://leetcode.com/studyplan/top-sql-50/",
        tags = listOf("SQL", "Databases", "Queries"),
        difficulty = "Basic to Advanced"
    ),

    // ── CODEFORCES ──────────────────────────────────────────────────────────
    TopPracticeSheet(
        id = "cses_300",
        title = "CSES Problem Set — 300 Classic CP Problems",
        description = "The gold standard 300 CP problems by University of Helsinki covering DP, Trees, Range Queries, Graphs, and Math.",
        problemCount = "300 Problems",
        platform = Platform.CODEFORCES,
        platformName = "CSES / Codeforces",
        url = "https://cses.fi/problemset/",
        tags = listOf("CSES", "Competitive Programming", "Algorithms"),
        difficulty = "Beginner to Grandmaster"
    ),
    TopPracticeSheet(
        id = "a2oj_ladders",
        title = "A2OJ / C2 Ladders (Rating 800 to 2400)",
        description = "Rating-specific Codeforces practice ladders (Div 2 A to E) designed to systematically increase CF contest rank.",
        problemCount = "300+ Problems",
        platform = Platform.CODEFORCES,
        platformName = "Codeforces Ladders",
        url = "https://c2-ladders.com/",
        tags = listOf("A2OJ", "Rating Ladder", "Div2 Practice"),
        difficulty = "Rating 800 - 2400"
    ),
    TopPracticeSheet(
        id = "striver_cp_sheet",
        title = "Striver CP Sheet — Top 250 CP Problems",
        description = "Curated 250 competitive programming problems by Raj Vikramaditya sorted by topic, intuition, and difficulty.",
        problemCount = "250 Problems",
        platform = Platform.CODEFORCES,
        platformName = "TakeUForward",
        url = "https://takeuforward.org/interview-sheets/strivers-cp-sheet",
        tags = listOf("Striver CP", "Topic-wise", "Codeforces"),
        difficulty = "Intermediate to Advanced"
    ),
    TopPracticeSheet(
        id = "cf_top_solved",
        title = "Codeforces Top Solved Problemset",
        description = "Most solved algorithmic problems across Codeforces history with extensive community solution discussions.",
        problemCount = "Top 300+",
        platform = Platform.CODEFORCES,
        platformName = "Codeforces Official",
        url = "https://codeforces.com/problemset?order=BY_SOLVED_DESC",
        tags = listOf("Codeforces", "Most Solved", "Div2/Div3"),
        difficulty = "All Levels"
    ),

    // ── ATCODER ─────────────────────────────────────────────────────────────
    TopPracticeSheet(
        id = "atcoder_dp_26",
        title = "AtCoder Educational DP Contest (Tasks A - Z)",
        description = "26 iconic DP challenges from Frog A to Deque and Subtree DP, universally acclaimed as the best DP learning track.",
        problemCount = "26 DP Tasks",
        platform = Platform.ATCODER,
        platformName = "AtCoder Official",
        url = "https://atcoder.jp/contests/dp/tasks",
        tags = listOf("Educational DP", "Dynamic Programming", "AtCoder"),
        difficulty = "Medium to Expert"
    ),
    TopPracticeSheet(
        id = "atcoder_100",
        title = "AtCoder Beginner 100 Selected Problems",
        description = "100 curated problems from past AtCoder Beginner Contests (ABC) for building rock-solid problem solving foundations.",
        problemCount = "100 Problems",
        platform = Platform.ATCODER,
        platformName = "AtCoder / Kenkoooo",
        url = "https://kenkoooo.com/atcoder/",
        tags = listOf("ABC", "Fundamentals", "Kenkoooo"),
        difficulty = "Beginner to Intermediate"
    ),
    TopPracticeSheet(
        id = "atcoder_typical_90",
        title = "AtCoder Typical 90 (Kyopro 90)",
        description = "90 high-caliber problems curated by E869120 teaching 90 standard competitive programming problem-solving patterns.",
        problemCount = "90 Problems",
        platform = Platform.ATCODER,
        platformName = "AtCoder Typical 90",
        url = "https://atcoder.jp/contests/typical90/tasks",
        tags = listOf("Typical 90", "CP Patterns", "Must-Solve"),
        difficulty = "★1 to ★7 Difficulty"
    ),

    // ── HACKERRANK & TOP PRACTICE HUBS ───────────────────────────────────────
    TopPracticeSheet(
        id = "hr_30_days",
        title = "HackerRank 30 Days of Code",
        description = "Structured 30-day coding bootcamp covering fundamentals: conditional logic, OOP, recursion, linked lists, and sorting.",
        problemCount = "30 Challenges",
        platform = Platform.GEEKSFORGEEKS,
        platformName = "HackerRank Official",
        url = "https://www.hackerrank.com/domains/tutorials/30-days-of-code",
        tags = listOf("30 Days", "Fundamentals", "Beginner Friendly"),
        difficulty = "Beginner"
    ),
    TopPracticeSheet(
        id = "hr_algorithms",
        title = "HackerRank Problem Solving & Algorithms Track",
        description = "Comprehensive problem track covering Warmup, Implementation, Strings, Sorting, Search, Graph Theory, and Bit Manipulation.",
        problemCount = "120+ Problems",
        platform = Platform.GEEKSFORGEEKS,
        platformName = "HackerRank",
        url = "https://www.hackerrank.com/domains/algorithms",
        tags = listOf("Problem Solving", "Gold Badges", "Data Structures"),
        difficulty = "Easy to Advanced"
    ),
    TopPracticeSheet(
        id = "ib_300",
        title = "InterviewBit Top 300 Programming Track",
        description = "Timed coding challenges with automated test cases covering FAANG interview patterns and system design fundamentals.",
        problemCount = "300 Problems",
        platform = Platform.LEETCODE,
        platformName = "InterviewBit",
        url = "https://www.interviewbit.com/practice/",
        tags = listOf("InterviewBit", "Timed Prep", "SDE"),
        difficulty = "Intermediate to Hard"
    ),
    TopPracticeSheet(
        id = "project_euler",
        title = "Project Euler Mathematical & Algorithmic Archive",
        description = "World-famous mathematical and computational challenges requiring mathematical insight and efficient algorithms.",
        problemCount = "800+ Challenges",
        platform = Platform.CODECHEF,
        platformName = "Project Euler",
        url = "https://projecteuler.net/archives",
        tags = listOf("Project Euler", "Math", "Number Theory"),
        difficulty = "Challenging"
    )
)
