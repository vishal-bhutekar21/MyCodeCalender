package com.mycodecalendar.feature.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycodecalendar.core.designsystem.BrandPrimaryOrange
import com.mycodecalendar.core.designsystem.GlassmorphismBackground
import com.mycodecalendar.core.designsystem.Typography
import com.mycodecalendar.core.designsystem.components.GlassBackButton
import com.mycodecalendar.core.designsystem.components.GlassCard
import com.mycodecalendar.domain.model.BadgeCategory
import com.mycodecalendar.domain.model.BadgeHelper
import com.mycodecalendar.domain.model.StreakBadge
import com.mycodecalendar.domain.model.StreakInfo
import com.mycodecalendar.feature.home.components.BadgeDetailBottomSheet
import com.mycodecalendar.feature.home.components.Streak3DBadgeCard
import com.mycodecalendar.feature.home.util.StreakAudioChime
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

private val ActiveHot    = Color(0xFF00F579)
private val Active3      = Color(0xFF00C962)
private val Active2      = Color(0xFF005E2D)
private val Active1      = Color(0xFF003319)
private val Inactive     = Color(0xFF0D1117)
private val AmberFire    = Color(0xFFFF6B00)
private val VioletAccent = Color(0xFF818CF8)

@Composable
fun StreakScreen(
    streakInfo: StreakInfo,
    onBackClick: () -> Unit,
    onShareStreak: () -> Unit = {},
    onShareBadge: (StreakBadge) -> Unit = {}
) {
    val today = remember { LocalDate.now() }
    var displayMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedBadgeCategoryTab by remember { mutableStateOf(0) } // 0 = All, 1 = Milestones, 2 = 12-Month Badges
    var activeDetailBadge by remember { mutableStateOf<StreakBadge?>(null) }

    val allBadges = remember(streakInfo) {
        BadgeHelper.computeAllBadges(streakInfo)
    }

    val unlockedBadgesCount = remember(allBadges) {
        allBadges.count { it.isUnlocked }
    }

    val filteredBadges = remember(allBadges, selectedBadgeCategoryTab) {
        when (selectedBadgeCategoryTab) {
            1 -> allBadges.filter { it.category == BadgeCategory.MILESTONE }
            2 -> allBadges.filter { it.category == BadgeCategory.MONTHLY }
            else -> allBadges
        }
    }

    val coderRank = remember(streakInfo.currentStreak) {
        BadgeHelper.getCoderRank(streakInfo.currentStreak)
    }

    val activeDates = remember(streakInfo.activeDates) {
        streakInfo.activeDates.mapNotNull {
            runCatching { LocalDate.parse(it) }.getOrNull()
        }.toSet()
    }

    val longestStreak = remember(activeDates) {
        var maxRun = 0; var run = 0
        var prev: LocalDate? = null
        for (date in activeDates.sorted()) {
            if (prev != null && date == prev!!.plusDays(1)) { run++ } else { run = 1 }
            if (run > maxRun) maxRun = run
            prev = date
        }
        maxRun.coerceAtLeast(streakInfo.currentStreak)
    }

    val weeks = remember(activeDates) {
        val totalDays = 140 // 20 weeks of GitHub style history
        val startDate = today.minusDays(totalDays.toLong() - 1)
        val dayOfWeekVal = startDate.dayOfWeek.value // 1 = Mon, 7 = Sun
        val alignedStart = startDate.minusDays((dayOfWeekVal - 1).toLong())
        val list = mutableListOf<LocalDate>()
        var curr = alignedStart
        while (!curr.isAfter(today)) {
            list.add(curr)
            curr = curr.plusDays(1)
        }
        list.chunked(7)
    }

    val fireScale by rememberInfiniteTransition(label = "fire").animateFloat(
        initialValue = 0.92f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(750, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "fireScale"
    )

    // ── ANIMATED STREAK COUNTER (counts up on load) ────────────────────────
    val haptic = LocalHapticFeedback.current
    var streakCountTarget by remember { mutableStateOf(0) }
    val animatedStreakCount by animateIntAsState(
        targetValue = streakCountTarget,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "streakCount"
    )
    LaunchedEffect(streakInfo.currentStreak) {
        kotlinx.coroutines.delay(250L)
        streakCountTarget = streakInfo.currentStreak
        kotlinx.coroutines.delay(950L) // Wait for count-up to land
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        StreakAudioChime.playCelebrationChime()
    }

    GlassmorphismBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {

            // ── TOP HEADER ─────────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GlassBackButton(onClick = onBackClick)
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            "Streak & Trophies",
                            style = Typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "LeetCode-style 3D milestone badges",
                            style = Typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                        )
                    }
                }

                GlassCard(
                    cornerRadius = 20.dp,
                    accentColor = AmberFire,
                    onClick = onShareStreak
                ) {
                    Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = "Share Streak",
                            modifier = Modifier.size(18.dp),
                            tint = AmberFire
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // ── 3D HERO STREAK GLASS CARD ──────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                AmberFire.copy(alpha = 0.18f),
                                Color(0xFF6C5CE7).copy(alpha = 0.08f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                            )
                        )
                    )
                    .border(
                        BorderStroke(
                            1.2.dp,
                            Brush.verticalGradient(
                                listOf(AmberFire.copy(alpha = 0.65f), VioletAccent.copy(alpha = 0.3f))
                            )
                        ),
                        RoundedCornerShape(26.dp)
                    )
                    .padding(22.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // 3D Animated Flame Sphere
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .drawBehind {
                                        drawCircle(
                                            brush = Brush.radialGradient(
                                                listOf(AmberFire.copy(alpha = 0.50f), Color.Transparent),
                                                center = center,
                                                radius = size.width * 0.75f
                                            )
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(62.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(0xFFFF8C00), Color(0xFFFF4500))
                                            )
                                        )
                                        .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Rounded.LocalFireDepartment,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(38.dp)
                                            .scale(fireScale),
                                        tint = Color.White
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "$animatedStreakCount",
                                    style = Typography.displayMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        brush = Brush.horizontalGradient(
                                            listOf(AmberFire, Color(0xFFFF8F00), Color(0xFFFFB300))
                                        )
                                    )
                                )
                                Text(
                                    "Day Coding Streak",
                                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Last active: ${streakInfo.lastOpenDateText}",
                            style = Typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                        )
                        Text(
                            text = "$unlockedBadgesCount / ${allBadges.size} Trophies Unlocked",
                            style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF10B981)
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── QUICK STAT COUNTERS ────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StreakStatCard("Total Days", "${activeDates.size}", VioletAccent, Modifier.weight(1f))
                StreakStatCard("Longest", "${longestStreak}d", ActiveHot, Modifier.weight(1f))
                StreakStatCard("Trophies", "$unlockedBadgesCount", AmberFire, Modifier.weight(1f))
            }

            Spacer(Modifier.height(20.dp))

            // ── GITHUB-STYLE CONTRIBUTION ACTIVITY MATRIX ──────────────────────────
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                accentColor = Color(0xFF26A641),
                cornerRadius = 22.dp
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Habit Heatmap",
                                style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "GitHub-style daily check-in activity",
                                style = Typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF26A641).copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Color(0xFF26A641).copy(alpha = 0.40f))
                        ) {
                            Text(
                                text = "${activeDates.size} Active Days",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                color = Color(0xFF39D353)
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Horizontal Scrollable GitHub Matrix
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Day Labels
                        Column(
                            modifier = Modifier.padding(end = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text("Mon", style = Typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                            Spacer(Modifier.height(3.dp))
                            Text("Wed", style = Typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                            Spacer(Modifier.height(3.dp))
                            Text("Fri", style = Typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(3.5.dp),
                            contentPadding = PaddingValues(vertical = 2.dp)
                        ) {
                            items(weeks) { week ->
                                Column(verticalArrangement = Arrangement.spacedBy(3.5.dp)) {
                                    week.forEach { date ->
                                        val isActive = date in activeDates
                                        val isToday = date == today
                                        val heatColor = when {
                                            isActive && isToday -> Color(0xFF39D353)
                                            isActive            -> Color(0xFF26A641)
                                            else                -> Color(0xFF161B22)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(11.dp)
                                                .clip(RoundedCornerShape(2.5.dp))
                                                .background(heatColor)
                                                .border(
                                                    0.5.dp,
                                                    if (isToday) Color(0xFF39D353).copy(alpha = 0.8f) else Color(0x1AFFFFFF),
                                                    RoundedCornerShape(2.5.dp)
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Less ",
                            style = Typography.labelSmall.copy(fontSize = 9.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                        )
                        listOf(
                            Color(0xFF161B22),
                            Color(0xFF0E4429),
                            Color(0xFF006D32),
                            Color(0xFF26A641),
                            Color(0xFF39D353)
                        ).forEach { col ->
                            Box(
                                Modifier
                                    .padding(horizontal = 2.dp)
                                    .size(10.dp)
                                    .clip(RoundedCornerShape(2.5.dp))
                                    .background(col)
                                    .border(0.5.dp, Color(0x1AFFFFFF), RoundedCornerShape(2.5.dp))
                            )
                        }
                        Text(
                            " More",
                            style = Typography.labelSmall.copy(fontSize = 9.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── TROPHIES & 12-MONTH BADGES SECTION ──────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Badges & Trophies",
                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Text(
                        text = "Tap to Inspect",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = Typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Badges Category Selector Pills (All / Milestones / 12 Months)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BadgeTabPill(
                    label = "All (${allBadges.size})",
                    selected = selectedBadgeCategoryTab == 0,
                    onClick = { selectedBadgeCategoryTab = 0 },
                    modifier = Modifier.weight(1f)
                )
                BadgeTabPill(
                    label = "Milestones (5)",
                    selected = selectedBadgeCategoryTab == 1,
                    onClick = { selectedBadgeCategoryTab = 1 },
                    modifier = Modifier.weight(1.1f)
                )
                BadgeTabPill(
                    label = "12 Months (12)",
                    selected = selectedBadgeCategoryTab == 2,
                    onClick = { selectedBadgeCategoryTab = 2 },
                    modifier = Modifier.weight(1.2f)
                )
            }

            Spacer(Modifier.height(14.dp))

            // 2-Column Responsive Grid of 3D Glassmorphic Badge Cards
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                filteredBadges.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        pair.forEach { badge ->
                            Streak3DBadgeCard(
                                badge = badge,
                                onClick = { activeDetailBadge = badge },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (pair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── MOTIVATION BANNER ──────────────────────────────────────────────────
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                accentColor = ActiveHot.copy(alpha = 0.4f),
                cornerRadius = 18.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val motivation = when {
                        streakInfo.currentStreak <= 1  -> "Every journey starts with Day 1. Check in daily to unlock legendary trophies!"
                        streakInfo.currentStreak <= 7  -> "One week sprint achieved! Keep pushing for the 14-Day Velocity badge."
                        streakInfo.currentStreak <= 30 -> "${streakInfo.currentStreak} days and counting! You are building an elite habit."
                        else -> "${streakInfo.currentStreak} days! Your relentless consistency is your ultimate superpower."
                    }
                    Text(
                        motivation,
                        style = Typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, lineHeight = 22.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(110.dp))
        }

        // ── IN-APP 3D BADGE DETAIL SHEET ───────────────────────────────────────────
        activeDetailBadge?.let { badge ->
            BadgeDetailBottomSheet(
                badge = badge,
                onDismiss = { activeDetailBadge = null },
                onShareBadge = { b ->
                    activeDetailBadge = null
                    onShareBadge(b)
                }
            )
        }
    }
}

@Composable
private fun BadgeTabPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (selected) AmberFire.copy(alpha = 0.16f)
    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f)

    val borderColor = if (selected) AmberFire.copy(alpha = 0.60f)
    else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)

    val textColor = if (selected) AmberFire
    else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(bgColor)
            .border(1.dp, borderColor, CircleShape)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = Typography.labelSmall.copy(
                fontWeight = if (selected) FontWeight.Black else FontWeight.SemiBold,
                fontSize = 11.5.sp
            ),
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun StreakStatCard(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier, cornerRadius = 16.dp, accentColor = valueColor.copy(alpha = 0.4f)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                style = Typography.headlineSmall.copy(
                    fontWeight = FontWeight.Black,
                    brush = Brush.verticalGradient(listOf(valueColor, valueColor.copy(alpha = 0.7f)))
                )
            )
            Spacer(Modifier.height(2.dp))
            Text(
                label,
                style = Typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                textAlign = TextAlign.Center,
                fontSize = 10.5.sp
            )
        }
    }
}