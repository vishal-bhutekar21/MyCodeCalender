package com.mycodecalendar.feature.contests

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.*
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
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.FormatListBulleted
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import com.mycodecalendar.core.designsystem.components.formatToIndianShortDateTime
import com.mycodecalendar.core.designsystem.components.formatContestDuration
import com.mycodecalendar.domain.model.Contest
import com.mycodecalendar.domain.model.ContestStatus
import com.mycodecalendar.domain.model.PastContestRecord
import com.mycodecalendar.domain.model.Platform
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
    pastContests: List<PastContestRecord> = emptyList(),
    watchedContestIds: Set<String> = emptySet(),
    onToggleWatch: (String) -> Unit = {},
    onShareContest: (Contest) -> Unit = {},
    onSetReminderClick: (Contest) -> Unit = {},
    onContestClick: (String) -> Unit,
    onAddPlatformClick: () -> Unit = {},
    onPastContestClick: (String) -> Unit = {}
) {
    var selectedMainTab by remember { mutableStateOf(0) } // 0 = Upcoming & Live, 1 = Hackathons, 2 = My History & Ratings
    var searchQuery by remember { mutableStateOf("") }
    var selectedPlatform by remember { mutableStateOf<Platform?>(null) }
    var selectedStatus by remember { mutableStateOf<ContestStatus?>(null) }
    var isWatchlistOnly by remember { mutableStateOf(false) }
    var isCalendarView by remember { mutableStateOf(false) }
    var selectedCalendarDate by remember { mutableStateOf<LocalDate?>(null) }
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

    var selectedHackathonTag by remember { mutableStateOf<String?>(null) }
    var selectedQuickFilter by remember { mutableStateOf<String>("all") }

    val filteredContests = remember(contests, searchQuery, selectedPlatform, selectedStatus, isWatchlistOnly, watchedContestIds, selectedCalendarDate, selectedQuickFilter) {
        contests.filter { contest ->
            val matchesQuery = searchQuery.isBlank() || contest.name.contains(searchQuery, ignoreCase = true)
            val matchesPlatform = selectedPlatform == null || contest.platform == selectedPlatform
            val matchesStatus = selectedStatus == null || contest.status == selectedStatus
            val matchesWatchlist = !isWatchlistOnly || watchedContestIds.contains(contest.id)
            val matchesDate = selectedCalendarDate == null ||
                try {
                    contest.startTimeUtc.atZone(ZoneId.systemDefault()).toLocalDate() == selectedCalendarDate
                } catch (_: Exception) { false }
            val matchesQuick = when (selectedQuickFilter) {
                "live" -> contest.status == ContestStatus.LIVE
                "24h" -> {
                    val until = Duration.between(Instant.now(), contest.startTimeUtc).seconds
                    until in 0..86400 || contest.status == ContestStatus.LIVE
                }
                "short" -> contest.durationSeconds in 1..10800
                "long" -> contest.durationSeconds > 10800
                else -> true
            }
            matchesQuery && matchesPlatform && matchesStatus && matchesWatchlist && matchesDate && matchesQuick
        }
    }

    val filteredHackathons = remember(allHackathons, searchQuery, selectedHackathonTag) {
        allHackathons.filter { hack ->
            val matchesQuery = searchQuery.isBlank() ||
                hack.title.contains(searchQuery, ignoreCase = true) ||
                hack.organizer.contains(searchQuery, ignoreCase = true) ||
                hack.mode.contains(searchQuery, ignoreCase = true) ||
                hack.tags.any { it.contains(searchQuery, ignoreCase = true) }

            val matchesTag = selectedHackathonTag == null ||
                hack.title.contains(selectedHackathonTag!!, ignoreCase = true) ||
                hack.organizer.contains(selectedHackathonTag!!, ignoreCase = true) ||
                hack.mode.contains(selectedHackathonTag!!, ignoreCase = true) ||
                hack.prizePool.contains(selectedHackathonTag!!, ignoreCase = true) ||
                hack.tags.any { it.contains(selectedHackathonTag!!, ignoreCase = true) }

            matchesQuery && matchesTag
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
                    style = Typography.headlineMedium.copy(fontWeight = FontWeight.Black, fontSize = 23.sp),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = when (selectedMainTab) {
                        0 -> "${filteredContests.size} contests available across platforms"
                        1 -> "${filteredHackathons.size} active hackathons & coding events"
                        else -> if (filteredPastContests.isEmpty()) "Connect platform handles to sync records" else "${filteredPastContests.size} past records logged"
                    },
                    style = Typography.bodySmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
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
                    label = "Hackathons",
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

            Spacer(modifier = Modifier.height(12.dp))

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
                // View Mode Toggle (List vs Calendar Grid)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isCalendarView) "Calendar Schedule" else "Contests Stream",
                        style = Typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            fontSize = 12.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f)
                    )

                    // View Mode Pill Toggle
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.30f)
                    ) {
                        Row(
                            modifier = Modifier.padding(3.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (!isCalendarView) MaterialTheme.colorScheme.primary else Color.Transparent,
                                modifier = Modifier.clickable {
                                    isCalendarView = false
                                    selectedCalendarDate = null
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.FormatListBulleted,
                                        contentDescription = "List View",
                                        tint = if (!isCalendarView) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "List",
                                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (!isCalendarView) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isCalendarView) MaterialTheme.colorScheme.primary else Color.Transparent,
                                modifier = Modifier.clickable {
                                    isCalendarView = true
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CalendarMonth,
                                        contentDescription = "Calendar Grid",
                                        tint = if (isCalendarView) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "Calendar",
                                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isCalendarView) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp)
                ) {
                    item {
                        GlassChip(
                            label = "All",
                            selected = selectedQuickFilter == "all" && !isWatchlistOnly && selectedStatus == null && selectedPlatform == null,
                            accentColor = MaterialTheme.colorScheme.primary,
                            onClick = {
                                selectedQuickFilter = "all"
                                isWatchlistOnly = false
                                selectedStatus = null
                                selectedPlatform = null
                            }
                        )
                    }

                    item {
                        GlassChip(
                            label = "Saved",
                            icon = Icons.Rounded.Bookmark,
                            selected = isWatchlistOnly,
                            accentColor = BrandPrimaryOrange,
                            onClick = {
                                isWatchlistOnly = !isWatchlistOnly
                            }
                        )
                    }

                    item {
                        GlassChip(
                            label = "Live Now",
                            icon = Icons.Rounded.FiberManualRecord,
                            selected = selectedQuickFilter == "live" || selectedStatus == ContestStatus.LIVE,
                            accentColor = Color(0xFFFF1744),
                            onClick = {
                                selectedQuickFilter = if (selectedQuickFilter == "live") "all" else "live"
                                selectedStatus = if (selectedQuickFilter == "live") ContestStatus.LIVE else null
                            }
                        )
                    }

                    item {
                        GlassChip(
                            label = "Next 24h",
                            icon = Icons.Rounded.FlashOn,
                            selected = selectedQuickFilter == "24h",
                            accentColor = Color(0xFF38BDF8),
                            onClick = {
                                selectedQuickFilter = if (selectedQuickFilter == "24h") "all" else "24h"
                            }
                        )
                    }

                    item {
                        GlassChip(
                            label = "< 3 Hours",
                            icon = Icons.Rounded.Timer,
                            selected = selectedQuickFilter == "short",
                            accentColor = Color(0xFFA855F7),
                            onClick = {
                                selectedQuickFilter = if (selectedQuickFilter == "short") "all" else "short"
                            }
                        )
                    }

                    item {
                        GlassChip(
                            label = "Long Sprints",
                            icon = Icons.Rounded.Flag,
                            selected = selectedQuickFilter == "long",
                            accentColor = Color(0xFFEC4899),
                            onClick = {
                                selectedQuickFilter = if (selectedQuickFilter == "long") "all" else "long"
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
            } else if (selectedMainTab == 1) {
                val hackathonCategories = listOf(
                    "All Tracks" to null,
                    "AI & ML" to "AI",
                    "Lakhs Prizes" to "Lakh",
                    "Web3" to "Web3",
                    "Unstop" to "Unstop",
                    "Offline Finale" to "Offline"
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp)
                ) {
                    items(hackathonCategories) { (label, filterTag) ->
                        GlassChip(
                            label = label,
                            selected = (filterTag == null && selectedHackathonTag == null) || (selectedHackathonTag == filterTag),
                            accentColor = BrandPrimaryOrange,
                            onClick = {
                                selectedHackathonTag = filterTag
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // ── CONTENT FEED BASED ON TAB ─────────────────────────────────────────
            when (selectedMainTab) {
                0 -> {
                    // CONTESTS FEED (List View or Interactive Calendar Grid)
                    if (isCalendarView) {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp)
                        ) {
                            item {
                                ContestCalendarGridView(
                                    contests = contests,
                                    selectedDate = selectedCalendarDate,
                                    onSelectDate = { selectedCalendarDate = it }
                                )
                            }

                            if (filteredContests.isEmpty()) {
                                item {
                                    EmptyState(
                                        title = if (selectedCalendarDate != null) "No Contests On Date" else "No Contests Found",
                                        message = if (selectedCalendarDate != null)
                                            "No contests scheduled on $selectedCalendarDate. Choose another date or reset filters."
                                        else "No contests match your active platform and status filters.",
                                        icon = Icons.Rounded.EventBusy,
                                        actionLabel = if (selectedCalendarDate != null) "Clear Date Filter" else "Reset Filters",
                                        onActionClick = {
                                            selectedCalendarDate = null
                                            searchQuery = ""
                                            selectedPlatform = null
                                            selectedStatus = null
                                        }
                                    )
                                }
                            } else {
                                item {
                                    Text(
                                        text = if (selectedCalendarDate != null)
                                            "Scheduled on Date (${filteredContests.size})"
                                        else "Upcoming Contests (${filteredContests.size})",
                                        style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                                    )
                                }

                                items(filteredContests, key = { it.id }) { contest ->
                                    ContestCard(
                                        contest = contest,
                                        isWatched = watchedContestIds.contains(contest.id),
                                        onToggleWatch = { onToggleWatch(contest.id) },
                                        onShareContest = { onShareContest(contest) },
                                        onSetReminderClick = { onSetReminderClick(contest) },
                                        onClick = { onContestClick(contest.id) }
                                    )
                                }
                            }
                        }
                    } else {
                        // Standard List View
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
                            EmptyState(
                                title = if (isWatchlistOnly) "Watchlist is Empty" else "No Contests Found",
                                message = if (isWatchlistOnly)
                                    "No contests saved to your watchlist yet. Tap the bookmark icon on any contest to keep track of it here!"
                                else "No contests match your current search and platform filters.",
                                icon = if (isWatchlistOnly) Icons.Rounded.BookmarkBorder else Icons.Rounded.SearchOff,
                                actionLabel = if (isWatchlistOnly) "Browse All Contests" else "Reset Filters",
                                onActionClick = {
                                    if (isWatchlistOnly) {
                                        isWatchlistOnly = false
                                    } else {
                                        searchQuery = ""
                                        selectedPlatform = null
                                        selectedStatus = null
                                    }
                                }
                            )
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp)
                            ) {
                                items(filteredContests, key = { it.id }) { contest ->
                                    ContestCard(
                                        contest = contest,
                                        isWatched = watchedContestIds.contains(contest.id),
                                        onToggleWatch = { onToggleWatch(contest.id) },
                                        onShareContest = { onShareContest(contest) },
                                        onSetReminderClick = { onSetReminderClick(contest) },
                                        onClick = { onContestClick(contest.id) }
                                    )
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // HACKATHONS FEED
                    if (filteredHackathons.isEmpty()) {
                        EmptyState(
                            title = "No Hackathons Found",
                            message = "No upcoming hackathons match your search criteria.",
                            icon = Icons.Rounded.EmojiEvents,
                            actionLabel = "Clear Search",
                            onActionClick = { searchQuery = "" }
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 100.dp)
                        ) {
                            items(filteredHackathons, key = { it.id }) { hackathon ->
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
        accentColor = null,
        cornerRadius = 20.dp,
        elevation = 4.dp,
        borderWidth = 0.dp
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

    val bgColor = if (selected) accentColor.copy(alpha = if (isDark) 0.20f else 0.12f)
    else (if (isDark) Color(0x12FFFFFF) else Color(0x55FFFFFF))

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
                Spacer(Modifier.width(5.dp))
            }
            Text(
                text = label,
                style = Typography.labelMedium.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 12.5.sp
                ),
                color = if (selected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.60f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (badgeCount > 0) {
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = badgeCount.toString(),
                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 9.5.sp),
                    color = if (selected) accentColor.copy(alpha = 0.80f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.40f)
                )
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
        accentColor = null,
        cornerRadius = 18.dp,
        elevation = 3.dp,
        borderWidth = 0.dp,
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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (isPositive) Icons.AutoMirrored.Rounded.TrendingUp else Icons.AutoMirrored.Rounded.TrendingDown,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = deltaColor
                    )
                    Text(
                        text = "${record.newRating} (${if (isPositive) "+" else ""}${record.ratingDelta})",
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = deltaColor
                    )
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

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = record.rankText,
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
    isWatched: Boolean = false,
    onToggleWatch: () -> Unit = {},
    onShareContest: () -> Unit = {},
    onSetReminderClick: () -> Unit = {},
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val brandColor = contest.platform.getBrandColor()
    val isLive = contest.status == ContestStatus.LIVE
    val activeColor = if (isLive) Color(0xFFFF1744) else brandColor
    val timeUntilStart = remember(contest.startTimeUtc) {
        Duration.between(Instant.now(), contest.startTimeUtc).seconds.coerceAtLeast(0)
    }
    val timeLabel = when {
        isLive -> "Live now"
        contest.status == ContestStatus.ENDED -> "Ended"
        timeUntilStart < 3600 -> "in ${timeUntilStart / 60}m"
        timeUntilStart < 86400 -> "in ${timeUntilStart / 3600}h ${(timeUntilStart % 3600) / 60}m"
        else -> "in ${timeUntilStart / 86400}d"
    }

    val livePulseAlpha by rememberInfiniteTransition(label = "livePulse").animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "liveAlpha"
    )

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        accentColor = if (isLive) Color(0xFFFF1744) else if (isWatched) BrandPrimaryOrange else null,
        cornerRadius = 18.dp,
        elevation = if (isLive) 6.dp else 2.dp,
        borderWidth = 0.dp,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Platform Badge + Dynamic Status Beacon + Subtle Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PlatformBadge(platform = contest.platform)
                    if (isLive) {
                        // Clean borderless LIVE indicator
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF1744).copy(alpha = livePulseAlpha))
                            )
                            Text(
                                text = "LIVE NOW",
                                style = Typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 9.sp,
                                    letterSpacing = 0.6.sp
                                ),
                                color = Color(0xFFFF1744)
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                            )
                            Text(
                                text = timeLabel,
                                style = Typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                            )
                        }
                    }
                }

                // Sleek, minimal action buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = onShareContest,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onToggleWatch,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = if (isWatched) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                            contentDescription = if (isWatched) "Saved" else "Save",
                            tint = if (isWatched) BrandPrimaryOrange else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Contest Title
            Text(
                text = contest.name,
                style = Typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    lineHeight = 21.sp,
                    letterSpacing = (-0.3).sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Inline Metadata (Date + Duration)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Event,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    tint = brandColor
                )
                Text(
                    text = contest.startTimeUtc.formatToIndianShortDateTime(),
                    style = Typography.labelSmall.copy(
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "·",
                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.5.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.40f)
                )
                Icon(
                    imageVector = Icons.Rounded.Timer,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.60f)
                )
                Text(
                    text = formatContestDuration(contest.durationSeconds),
                    style = Typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer Row: Quick Reminder Button + Clean Primary CTA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.30f))
                        .clickable { onSetReminderClick() }
                        .padding(horizontal = 9.dp, vertical = 5.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.NotificationsNone,
                            contentDescription = "Reminder",
                            modifier = Modifier.size(13.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f)
                        )
                        Text(
                            text = "Reminder",
                            style = Typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.80f)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isLive) Color(0xFFFF1744).copy(alpha = 0.14f)
                            else brandColor.copy(alpha = 0.12f)
                        )
                        .clickable { onClick() }
                        .padding(horizontal = 11.dp, vertical = 5.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (isLive) "Compete Live" else "Details",
                            style = Typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp
                            ),
                            color = if (isLive) Color(0xFFFF1744) else brandColor
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = if (isLive) Color(0xFFFF1744) else brandColor
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

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        accentColor = brandOrange,
        cornerRadius = 20.dp,
        elevation = 4.dp,
        borderWidth = 0.dp,
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

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (hackathon.bannerUrl.isBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Badge — borderless dot+text
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(brandOrange)
                            )
                            Text(
                                text = hackathon.badge,
                                style = Typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 9.5.sp, letterSpacing = 0.4.sp),
                                color = brandOrange
                            )
                        }

                        if (hackathon.prizePool.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.EmojiEvents,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp),
                                    tint = Color(0xFFF59E0B)
                                )
                                Text(
                                    text = hackathon.prizePool,
                                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                    color = Color(0xFFF59E0B)
                                )
                            }
                        }
                    }
                } else if (hackathon.prizePool.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Rounded.EmojiEvents,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = Color(0xFFF59E0B)
                            )
                            Text(
                                text = hackathon.prizePool,
                                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                color = Color(0xFFF59E0B)
                            )
                        }
                    }
                }

                // Title & Organizer
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = hackathon.title,
                        style = Typography.titleMedium.copy(fontWeight = FontWeight.Black, fontSize = 16.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (hackathon.organizer.isNotBlank()) {
                        Text(
                            text = hackathon.organizer,
                            style = Typography.bodySmall.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.80f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Metadata Chips (Vector Icons, No Emojis)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (hackathon.teamSize.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Groups,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = hackathon.teamSize,
                                    style = Typography.labelSmall.copy(fontSize = 10.5.sp, fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (hackathon.mode.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.LocationOn,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = hackathon.mode.take(24),
                                    style = Typography.labelSmall.copy(fontSize = 10.5.sp, fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // Footer: Tags + Gradient Register Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Dot+text tags
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        hackathon.tags.take(2).forEach { tag ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(brandOrange.copy(alpha = 0.70f))
                                )
                                Text(
                                    text = tag,
                                    style = Typography.labelSmall.copy(fontSize = 9.5.sp, fontWeight = FontWeight.Medium),
                                    color = brandOrange.copy(alpha = 0.85f),
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // Gradient Register button — keep the bold gradient, remove the border wrapping
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                androidx.compose.ui.graphics.Brush.horizontalGradient(
                                    listOf(brandOrange, Color(0xFFFF8533))
                                )
                            )
                            .clickable(onClick = onRegisterClick)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                text = "Register Now",
                                style = Typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 11.5.sp),
                                color = Color.White
                            )
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── INTERACTIVE CONTEST CALENDAR GRID VIEW ──────────────────────────────────

@Composable
fun ContestCalendarGridView(
    contests: List<Contest>,
    selectedDate: LocalDate?,
    onSelectDate: (LocalDate?) -> Unit,
    modifier: Modifier = Modifier
) {
    var displayedMonth by remember { mutableStateOf(YearMonth.now()) }
    val today = remember { LocalDate.now() }

    // Map each date to contests occurring on that date
    val contestsByDate = remember(contests, displayedMonth) {
        val map = mutableMapOf<LocalDate, MutableList<Contest>>()
        contests.forEach { contest ->
            try {
                val date = contest.startTimeUtc.atZone(ZoneId.systemDefault()).toLocalDate()
                map.getOrPut(date) { mutableListOf() }.add(contest)
            } catch (_: Exception) {}
        }
        map
    }

    val daysInMonth = displayedMonth.lengthOfMonth()
    val firstDayOfMonth = displayedMonth.atDay(1)
    // Sunday = 0, Monday = 1, ..., Saturday = 6
    val firstDayOfWeek = (firstDayOfMonth.dayOfWeek.value % 7)
    val monthTitle = remember(displayedMonth) {
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy")
        displayedMonth.format(formatter)
    }

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 20.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Month Switcher Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = monthTitle,
                    style = Typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { displayedMonth = displayedMonth.minusMonths(1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ChevronLeft,
                            contentDescription = "Previous Month",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.clickable {
                            displayedMonth = YearMonth.now()
                            onSelectDate(today)
                        }
                    ) {
                        Text(
                            text = "Today",
                            style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(
                        onClick = { displayedMonth = displayedMonth.plusMonths(1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = "Next Month",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Day of Week Header Row: S M T W T F S
            val dayHeaders = listOf("S", "M", "T", "W", "T", "F", "S")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                dayHeaders.forEach { dayName ->
                    Text(
                        text = dayName,
                        style = Typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar Days Matrix
            val totalSlots = firstDayOfWeek + daysInMonth
            val totalRows = (totalSlots + 6) / 7

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (rowIndex in 0 until totalRows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        for (colIndex in 0 until 7) {
                            val slotIndex = rowIndex * 7 + colIndex
                            val dayNumber = slotIndex - firstDayOfWeek + 1

                            if (dayNumber in 1..daysInMonth) {
                                val date = displayedMonth.atDay(dayNumber)
                                val isToday = date == today
                                val isSelected = date == selectedDate
                                val dayContests = contestsByDate[date] ?: emptyList()
                                val hasContests = dayContests.isNotEmpty()

                                val cellBorder = when {
                                    isSelected -> BorderStroke(1.5.dp, BrandPrimaryOrange)
                                    isToday -> BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                                    else -> null
                                }

                                val cellBg = when {
                                    isSelected -> BrandPrimaryOrange.copy(alpha = 0.20f)
                                    isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                                    hasContests -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                                    else -> Color.Transparent
                                }

                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(cellBg)
                                        .then(if (cellBorder != null) Modifier.border(cellBorder, RoundedCornerShape(10.dp)) else Modifier)
                                        .clickable {
                                            if (isSelected) {
                                                onSelectDate(null)
                                            } else {
                                                onSelectDate(date)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "$dayNumber",
                                            style = Typography.labelMedium.copy(
                                                fontWeight = if (isToday || isSelected || hasContests) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 12.sp
                                            ),
                                            color = when {
                                                isSelected -> BrandPrimaryOrange
                                                isToday -> MaterialTheme.colorScheme.primary
                                                hasContests -> MaterialTheme.colorScheme.onSurface
                                                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                                            }
                                        )

                                        // Mini Contest Dots Row (up to 3 platform dots)
                                        if (hasContests) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                val platforms = dayContests.map { it.platform }.distinct().take(3)
                                                platforms.forEach { platform ->
                                                    Box(
                                                        modifier = Modifier
                                                            .size(4.dp)
                                                            .clip(CircleShape)
                                                            .background(platform.getBrandColor())
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                Box(modifier = Modifier.size(38.dp))
                            }
                        }
                    }
                }
            }

            if (selectedDate != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val dateFormatted = remember(selectedDate) {
                        val fmt = DateTimeFormatter.ofPattern("EEEE, MMM d")
                        selectedDate.format(fmt)
                    }
                    Text(
                        text = "Filtered for $dateFormatted",
                        style = Typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = BrandPrimaryOrange
                        )
                    )
                    Text(
                        text = "Clear Filter",
                        style = Typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.clickable { onSelectDate(null) }
                    )
                }
            }
        }
    }
}

