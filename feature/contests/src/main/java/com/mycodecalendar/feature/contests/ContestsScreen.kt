package com.mycodecalendar.feature.contests

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
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowOutward
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mycodecalendar.core.designsystem.BrandPrimaryOrange
import com.mycodecalendar.core.designsystem.GlassmorphismBackground
import com.mycodecalendar.core.designsystem.Typography
import com.mycodecalendar.core.designsystem.components.ContestCardSkeleton
import com.mycodecalendar.core.designsystem.components.EmptyState
import com.mycodecalendar.core.designsystem.components.GlassCard
import com.mycodecalendar.core.designsystem.components.GlassChip
import com.mycodecalendar.core.designsystem.components.PlatformBadge
import com.mycodecalendar.core.designsystem.components.StatusChip
import com.mycodecalendar.core.designsystem.components.getBrandColor
import com.mycodecalendar.domain.model.Contest
import com.mycodecalendar.domain.model.ContestStatus
import com.mycodecalendar.domain.model.PastContestRecord
import com.mycodecalendar.domain.model.Platform
import java.time.Duration
import java.time.Instant

/**
 * Data model for Hackathons and Grand Innovation Challenges.
 */
data class HackathonItem(
    val id: String,
    val title: String,
    val organizer: String,
    val prizePool: String,
    val timeline: String,
    val mode: String,
    val teamSize: String,
    val bannerUrl: String = "",
    val actionUrl: String,
    val tags: List<String>,
    val badge: String = "FEATURED HACKATHON"
)

val curatedHackathons = listOf(
    HackathonItem(
        id = "innovik_6_2026",
        title = "INNOVIK 6.0 – International Hackathon 2026",
        organizer = "Vikrant Institute of Technology & Management (VITM), Indore",
        prizePool = "₹ 2,00,000",
        timeline = "06 Aug 2026 – 25 Aug 2026",
        mode = "Hybrid (Online PPT + Offline Finale @ VITM Indore)",
        teamSize = "2 - 4 Members",
        bannerUrl = "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?q=80&w=1000&auto=format&fit=crop",
        actionUrl = "https://unstop.com",
        tags = listOf("Applied AI", "Agentic AI", "Web3", "₹2 Lakhs", "VITM Indore"),
        badge = "FLAGSHIP HACKATHON"
    ),
    HackathonItem(
        id = "sih_2026",
        title = "Smart India Hackathon (SIH) 2026",
        organizer = "Ministry of Education Innovation Cell (Govt of India)",
        prizePool = "₹ 1,00,000 / Theme",
        timeline = "Aug 2026 – Nov 2026",
        mode = "All India Nodal Campus Centers",
        teamSize = "6 Members (Min 1 Female)",
        bannerUrl = "",
        actionUrl = "https://sih.gov.in",
        tags = listOf("Govt of India", "Smart Automation", "Hardware & Software", "National Level"),
        badge = "NATIONAL GRAND CHALLENGE"
    ),
    HackathonItem(
        id = "google_solution_2026",
        title = "Google Solution Challenge 2026",
        organizer = "Google Developer Student Clubs (GDSC)",
        prizePool = "$10,000+ & Mentorship",
        timeline = "Annual Global Challenge",
        mode = "Global Online Arena",
        teamSize = "1 - 4 Members",
        bannerUrl = "",
        actionUrl = "https://developers.google.com/community/gdsc-solution-challenge",
        tags = listOf("Google AI", "Gemini", "UN Goals", "Global Tech"),
        badge = "GLOBAL COMPETITION"
    ),
    HackathonItem(
        id = "unstop_code_sprint_2026",
        title = "Unstop Tech & AI Grand Coding Arena",
        organizer = "Unstop Community & Tech Giants",
        prizePool = "₹ 50,000 + Job Referrals",
        timeline = "Rolling Weekly Sprints",
        mode = "Online Arena",
        teamSize = "Solo / 2 Members",
        bannerUrl = "",
        actionUrl = "https://unstop.com",
        tags = listOf("DSA Sprint", "Hiring Challenge", "Unstop"),
        badge = "WEEKLY ARENA"
    )
)

private val samplePastContests = listOf(
    PastContestRecord(
        id = "past-cf-920",
        platform = Platform.CODEFORCES,
        contestName = "Codeforces Round 920 (Div. 2)",
        dateText = "3 days ago",
        oldRating = 1684,
        newRating = 1738,
        ratingDelta = 54,
        solvedCount = 4,
        totalProblems = 5,
        rankText = "Rank #1,240 / 18,500",
        contestUrl = "https://codeforces.com/contest/1921"
    ),
    PastContestRecord(
        id = "past-lc-384",
        platform = Platform.LEETCODE,
        contestName = "LeetCode Weekly Contest 384",
        dateText = "5 days ago",
        oldRating = 1810,
        newRating = 1845,
        ratingDelta = 35,
        solvedCount = 3,
        totalProblems = 4,
        rankText = "Rank #890 / 22,000",
        contestUrl = "https://leetcode.com/contest/weekly-contest-384/"
    )
)

/**
 * ContestsScreen — Contest discovery, search, live filters, and past contest performance records.
 *
 * Primary Tabs:
 * - "Upcoming & Live": Live & upcoming contests aggregator feed.
 * - "🏆 Hackathons": Grand innovation hackathons, prizes, and unstop registrations.
 * - "My History & Ratings": Past participated contests, rating deltas (+/-), and problems solved counts.
 */
@Composable
fun ContestsScreen(
    contests: List<Contest>,
    pastContests: List<PastContestRecord> = samplePastContests,
    onContestClick: (String) -> Unit,
    onAddPlatformClick: () -> Unit = {},
    onPastContestClick: (String) -> Unit = {}
) {
    var selectedMainTab by remember { mutableStateOf(0) } // 0 = Upcoming & Live, 1 = Hackathons, 2 = My History & Ratings
    var searchQuery by remember { mutableStateOf("") }
    var selectedPlatform by remember { mutableStateOf<Platform?>(null) }
    var selectedStatus by remember { mutableStateOf<ContestStatus?>(null) }
    var cloudHackathons by remember { mutableStateOf<List<HackathonItem>>(emptyList()) }

    // Fetch dynamic hackathons from Firestore
    LaunchedEffect(Unit) {
        try {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("broadcasts")
                .whereEqualTo("isActive", true)
                .get()
                .addOnSuccessListener { snapshot ->
                    val list = snapshot.documents.mapNotNull { doc ->
                        val badge = doc.getString("badge") ?: "HACKATHON"
                        if (badge.contains("HACKATHON", ignoreCase = true) || badge.contains("EVENT", ignoreCase = true)) {
                            val title = doc.getString("title") ?: ""
                            val subtitle = doc.getString("message") ?: doc.getString("subtitle") ?: ""
                            val actionUrl = doc.getString("actionUrl") ?: "https://unstop.com"
                            val prizePool = doc.getString("prizePool") ?: "₹ 2,00,000"
                            val location = doc.getString("location") ?: "VITM Indore Campus"
                            val teamSize = doc.getString("teamSize") ?: "2 - 4 Members"
                            val timeline = doc.getString("timeline") ?: "Aug 2026"
                            val tags = (doc.get("tags") as? List<*>)?.mapNotNull { it?.toString() }
                                ?: listOf("Applied AI", "Hackathon", "₹2 Lakhs")
                            if (title.isNotBlank()) {
                                HackathonItem(
                                    id = doc.id,
                                    title = title,
                                    organizer = subtitle.take(45),
                                    prizePool = prizePool,
                                    timeline = timeline,
                                    mode = location,
                                    teamSize = teamSize,
                                    bannerUrl = doc.getString("bannerImageUrl") ?: "",
                                    actionUrl = actionUrl,
                                    tags = tags,
                                    badge = badge.uppercase()
                                )
                            } else null
                        } else null
                    }
                    if (list.isNotEmpty()) {
                        cloudHackathons = list
                    }
                }
        } catch (e: Exception) {
            // Non-blocking
        }
    }

    val allHackathons = remember(cloudHackathons) {
        (cloudHackathons + curatedHackathons).distinctBy { it.actionUrl.ifBlank { it.id } }
    }

    val filteredContests = remember(contests, searchQuery, selectedPlatform, selectedStatus) {
        contests.filter { contest ->
            val matchesQuery = searchQuery.isBlank() || contest.name.contains(searchQuery, ignoreCase = true)
            val matchesPlatform = selectedPlatform == null || contest.platform == selectedPlatform
            val matchesStatus = selectedStatus == null || contest.status == selectedStatus
            matchesQuery && matchesPlatform && matchesStatus
        }
    }

    val filteredHackathons = remember(allHackathons, searchQuery) {
        allHackathons.filter { hack ->
            searchQuery.isBlank() ||
                hack.title.contains(searchQuery, ignoreCase = true) ||
                hack.organizer.contains(searchQuery, ignoreCase = true) ||
                hack.mode.contains(searchQuery, ignoreCase = true) ||
                hack.tags.any { it.contains(searchQuery, ignoreCase = true) }
        }
    }

    val filteredPastContests = remember(pastContests, searchQuery, selectedPlatform) {
        pastContests.filter { past ->
            val matchesQuery = searchQuery.isBlank() || past.contestName.contains(searchQuery, ignoreCase = true)
            val matchesPlatform = selectedPlatform == null || past.platform == selectedPlatform
            matchesQuery && matchesPlatform
        }
    }

    GlassmorphismBackground {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // ── HEADER ───────────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 20.dp, bottom = 8.dp)
            ) {
                Text(
                    text = "Contests & Hackathons",
                    style = Typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = when (selectedMainTab) {
                        0 -> "${filteredContests.size} contests available"
                        1 -> "${filteredHackathons.size} grand hackathons & challenges"
                        else -> if (filteredPastContests.isEmpty()) "Connect platform handles to sync records" else "${filteredPastContests.size} past records logged"
                    },
                    style = Typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ── PRIMARY 3-SEGMENT TAB TOGGLE ───────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PrimaryTabPill(
                    label = "Contests",
                    selected = selectedMainTab == 0,
                    badgeCount = contests.count { it.status == ContestStatus.LIVE || it.status == ContestStatus.UPCOMING },
                    onClick = { selectedMainTab = 0 },
                    modifier = Modifier.weight(1f)
                )
                PrimaryTabPill(
                    label = "🏆 Hackathons",
                    selected = selectedMainTab == 1,
                    badgeCount = allHackathons.size,
                    onClick = { selectedMainTab = 1 },
                    modifier = Modifier.weight(1.15f)
                )
                PrimaryTabPill(
                    label = "My History",
                    selected = selectedMainTab == 2,
                    badgeCount = pastContests.size,
                    onClick = { selectedMainTab = 2 },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── 1PX BORDERED MINIMALIST SEARCH BAR ─────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                com.mycodecalendar.core.designsystem.components.AppSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = when (selectedMainTab) {
                        0 -> "Search live & upcoming contests…"
                        1 -> "Search Innovik 6.0, SIH, AI hackathons, prizes…"
                        else -> "Search past rating history…"
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ── CONSOLIDATED PLATFORM & LIVE FILTER BAR (For Contests Tab) ──────────
            if (selectedMainTab == 0) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp)
                ) {
                    item {
                        GlassChip(
                            label = "All Platforms",
                            selected = selectedPlatform == null && selectedStatus == null,
                            accentColor = MaterialTheme.colorScheme.primary,
                            onClick = {
                                selectedPlatform = null
                                selectedStatus = null
                            }
                        )
                    }

                    item {
                        GlassChip(
                            label = "● Live Now",
                            selected = selectedStatus == ContestStatus.LIVE,
                            accentColor = Color(0xFF00F579),
                            onClick = {
                                selectedStatus = if (selectedStatus == ContestStatus.LIVE) null else ContestStatus.LIVE
                            }
                        )
                    }

                    items(Platform.values()) { platform ->
                        GlassChip(
                            label = platform.name.lowercase().replaceFirstChar { it.uppercase() },
                            selected = selectedPlatform == platform,
                            accentColor = platform.getBrandColor(),
                            onClick = {
                                selectedPlatform = if (selectedPlatform == platform) null else platform
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // ── CONTENT FEED BASED ON TAB ─────────────────────────────────────────
            when (selectedMainTab) {
                0 -> {
                    // CONTESTS FEED
                    if (contests.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            repeat(5) {
                                ContestCardSkeleton()
                            }
                        }
                    } else if (filteredContests.isEmpty()) {
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
                1 -> {
                    // HACKATHONS & GRAND INNOVATION CHALLENGES FEED
                    if (filteredHackathons.isEmpty()) {
                        EmptyState(message = "No hackathons match your search.")
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp)
                        ) {
                            // ── HACKATHONS HERO HEADER ─────────────────────────────
                            item {
                                GlassCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    accentColor = BrandPrimaryOrange,
                                    cornerRadius = 22.dp
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                androidx.compose.ui.graphics.Brush.linearGradient(
                                                    listOf(
                                                        BrandPrimaryOrange.copy(alpha = 0.22f),
                                                        Color(0xFF1E2235),
                                                        Color(0xFF0F121C)
                                                    )
                                                )
                                            )
                                            .padding(18.dp)
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text("🏆", fontSize = 22.sp)
                                                Column {
                                                    Text(
                                                        text = "Hackathons & Grand Challenges",
                                                        style = Typography.titleLarge.copy(
                                                            fontWeight = FontWeight.Black,
                                                            fontSize = 18.sp,
                                                            letterSpacing = (-0.3).sp
                                                        ),
                                                        color = Color.White
                                                    )
                                                    Text(
                                                        text = "Innovation competitions, prize pools & more",
                                                        style = Typography.bodySmall.copy(fontSize = 11.5.sp),
                                                        color = Color.White.copy(alpha = 0.65f)
                                                    )
                                                }
                                            }

                                            // Stats row
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                listOf(
                                                    Triple("🔥", "${filteredHackathons.size}", "Active Events"),
                                                    Triple("💰", "₹3L+", "Total Prizes"),
                                                    Triple("🌐", "Hybrid", "Mode")
                                                ).forEach { (emoji, value, label) ->
                                                    Surface(
                                                        shape = RoundedCornerShape(10.dp),
                                                        color = Color.White.copy(alpha = 0.08f),
                                                        modifier = Modifier.weight(1f),
                                                        border = androidx.compose.foundation.BorderStroke(
                                                            0.8.dp, Color.White.copy(alpha = 0.18f)
                                                        )
                                                    ) {
                                                        Column(
                                                            modifier = Modifier.padding(10.dp),
                                                            horizontalAlignment = Alignment.CenterHorizontally,
                                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                                        ) {
                                                            Text(text = emoji, fontSize = 16.sp)
                                                            Text(
                                                                text = value,
                                                                style = Typography.titleSmall.copy(fontWeight = FontWeight.Black, fontSize = 14.sp),
                                                                color = Color.White
                                                            )
                                                            Text(
                                                                text = label,
                                                                style = Typography.labelSmall.copy(fontSize = 9.5.sp),
                                                                color = Color.White.copy(alpha = 0.60f)
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            // Tags row
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                listOf("Applied AI", "Web3", "Offline Finale", "Unstop").forEach { tag ->
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = BrandPrimaryOrange.copy(alpha = 0.20f),
                                                        border = androidx.compose.foundation.BorderStroke(0.8.dp, BrandPrimaryOrange.copy(alpha = 0.40f))
                                                    ) {
                                                        Text(
                                                            text = tag,
                                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                                            style = Typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 10.sp),
                                                            color = BrandPrimaryOrange
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // ── HACKATHON CARDS ────────────────────────────────────
                            items(filteredHackathons) { hackathon ->
                                HackathonCard(
                                    hackathon = hackathon,
                                    onRegisterClick = { onPastContestClick(hackathon.actionUrl) }
                                )
                            }
                        }
                    }
                }
                else -> {
                    // MY PAST CONTESTS & RATING HISTORY TAB
                    if (filteredPastContests.isEmpty()) {
                        NoPlatformsConnectedCard(
                            onAddPlatformClick = onAddPlatformClick,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp)
                        ) {
                            items(filteredPastContests) { pastRecord ->
                                PastContestHistoryCard(
                                    record = pastRecord,
                                    onClick = { onPastContestClick(pastRecord.contestUrl) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── NO PLATFORMS CONNECTED EMPTY STATE CARD ───────────────────────────────────

@Composable
fun NoPlatformsConnectedCard(
    onAddPlatformClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        accentColor = MaterialTheme.colorScheme.primary,
        cornerRadius = 20.dp,
        elevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.40f)),
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Link,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No Platform Accounts Connected",
                style = Typography.titleMedium.copy(fontWeight = FontWeight.Black, fontSize = 18.sp),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Connect your Codeforces, LeetCode, CodeChef, or GitHub handle to automatically view your real contest history, rating changes, and solved problem counts.",
                style = Typography.bodySmall.copy(fontWeight = FontWeight.Normal),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onAddPlatformClick,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Connect Platform Handles",
                    style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

// ── PRIMARY TAB PILL TOGGLE ──────────────────────────────────────────────────

@Composable
fun PrimaryTabPill(
    label: String,
    selected: Boolean,
    badgeCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.1f
    val accentColor = MaterialTheme.colorScheme.primary

    val bgColor = if (selected) accentColor.copy(alpha = if (isDark) 0.22f else 0.12f)
    else (if (isDark) Color(0x1AFFFFFF) else Color(0x70FFFFFF))

    val borderColor = if (selected) accentColor.copy(alpha = if (isDark) 0.60f else 0.45f)
    else (if (isDark) Color(0x33FFFFFF) else Color(0x99FFFFFF))

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(14.dp))
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                style = Typography.labelMedium.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 12.5.sp
                ),
                color = if (selected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (badgeCount > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (selected) accentColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = badgeCount.toString(),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 10.sp),
                        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ── PAST CONTEST HISTORY CARD ────────────────────────────────────────────────

@Composable
fun PastContestHistoryCard(
    record: PastContestRecord,
    onClick: () -> Unit
) {
    val brandColor = record.platform.getBrandColor()
    val isPositive = record.ratingDelta >= 0
    val deltaColor = if (isPositive) Color(0xFF22C55E) else Color(0xFFF43F5E)

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        accentColor = brandColor,
        cornerRadius = 16.dp,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PlatformBadge(platform = record.platform)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = record.dateText,
                        style = Typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = deltaColor.copy(alpha = 0.14f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, deltaColor.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isPositive) Icons.AutoMirrored.Rounded.TrendingUp else Icons.AutoMirrored.Rounded.TrendingDown,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = deltaColor
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${record.newRating} (${if (isPositive) "+" else ""}${record.ratingDelta})",
                            style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = deltaColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = record.contestName,
                style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Problems Solved",
                            style = Typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "${record.solvedCount} / ${record.totalProblems}",
                            style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    val pct = (record.solvedCount.toFloat() / record.totalProblems.coerceAtLeast(1)).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { pct },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = brandColor,
                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(0.1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                ) {
                    Text(
                        text = record.rankText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun ContestCard(
    contest: Contest,
    onClick: () -> Unit
) {
    val brandColor = contest.platform.getBrandColor()
    val activeColor = if (contest.status == ContestStatus.LIVE) Color(0xFF22C55E) else brandColor
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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(0.1.dp, activeColor.copy(alpha = 0.40f), RoundedCornerShape(16.dp))
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            accentColor = activeColor,
            cornerRadius = 16.dp,
            onClick = onClick
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(
                            color = activeColor,
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

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${contest.durationSeconds / 3600}h ${(contest.durationSeconds % 3600) / 60}m",
                            style = Typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Text(
                            text = timeLabel,
                            style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = activeColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HackathonCard(
    hackathon: HackathonItem,
    onRegisterClick: () -> Unit
) {
    val brandOrange = BrandPrimaryOrange

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, brandOrange.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            accentColor = brandOrange,
            cornerRadius = 20.dp,
            onClick = onRegisterClick
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (hackathon.bannerUrl.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    ) {
                        AsyncImage(
                            model = hackathon.bannerUrl,
                            contentDescription = hackathon.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    androidx.compose.ui.graphics.Brush.verticalGradient(
                                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                    )
                                )
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = brandOrange,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = hackathon.badge,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp),
                                style = Typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 9.sp),
                                color = Color.White
                            )
                        }
                    }
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    if (hackathon.bannerUrl.isBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = brandOrange.copy(alpha = 0.18f),
                                border = BorderStroke(0.1.dp, brandOrange.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = hackathon.badge,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 9.5.sp),
                                    color = brandOrange
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                                border = BorderStroke(0.1.dp, Color(0xFFF59E0B).copy(alpha = 0.35f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Rounded.EmojiEvents,
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp),
                                        tint = Color(0xFFF59E0B)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = hackathon.prizePool,
                                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                        color = Color(0xFFF59E0B)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    } else {
                        // Prize pool row when banner exists
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                                border = BorderStroke(0.1.dp, Color(0xFFF59E0B).copy(alpha = 0.35f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Rounded.EmojiEvents,
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp),
                                        tint = Color(0xFFF59E0B)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = hackathon.prizePool,
                                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                        color = Color(0xFFF59E0B)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    Text(
                        text = hackathon.title,
                        style = Typography.titleMedium.copy(fontWeight = FontWeight.Black, fontSize = 16.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = hackathon.organizer,
                        style = Typography.bodySmall.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Mode, Team size & Timeline pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        ) {
                            Text(
                                text = "👥 ${hackathon.teamSize}",
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                style = Typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        ) {
                            Text(
                                text = "📍 ${hackathon.mode.take(24)}",
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                style = Typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Footer: Tags + 1-Tap Register Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            hackathon.tags.take(2).forEach { tag ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = brandOrange.copy(alpha = 0.1f),
                                    border = BorderStroke(0.1.dp, brandOrange.copy(alpha = 0.25f))
                                ) {
                                    Text(
                                        text = tag,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = Typography.labelSmall.copy(fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold),
                                        color = brandOrange
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = onRegisterClick,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = brandOrange,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Register",
                                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.AutoMirrored.Rounded.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
