package com.mycodecalendar.feature.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycodecalendar.core.designsystem.GlassmorphismBackground
import com.mycodecalendar.core.designsystem.Typography
import com.mycodecalendar.core.designsystem.components.GlassCard
import com.mycodecalendar.domain.model.StreakInfo
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

private val ActiveHot    = Color(0xFF00F579)
private val Active3      = Color(0xFF00C962)
private val Active2      = Color(0xFF005E2D)
private val Active1      = Color(0xFF003319)
private val Inactive     = Color(0xFF0D1117)
private val AmberFire    = Color(0xFFF59E0B)
private val VioletAccent = Color(0xFF818CF8)

@Composable
fun StreakScreen(
    streakInfo: StreakInfo,
    onBackClick: () -> Unit,
    onShareStreak: () -> Unit = {}
) {
    val today = remember { LocalDate.now() }
    var displayMonth by remember { mutableStateOf(YearMonth.now()) }

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

    val thisMonthCount = activeDates.count { d -> d.year == displayMonth.year && d.month == displayMonth.month }

    val fireScale by rememberInfiniteTransition(label = "fire").animateFloat(
        initialValue = 0.90f, targetValue = 1.18f,
        animationSpec = infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "fireScale"
    )

    GlassmorphismBackground {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GlassCard(cornerRadius = 20.dp, onClick = onBackClick) {
                        Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back",
                                modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text("Coding Streak", style = Typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                            color = MaterialTheme.colorScheme.onSurface)
                        Text("Daily activity calendar", style = Typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f))
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

            // Hero Badge
            GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                accentColor = AmberFire, cornerRadius = 24.dp) {
                Row(modifier = Modifier.fillMaxWidth().padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    Box(
                        modifier = Modifier.size(72.dp)
                            .background(Brush.radialGradient(listOf(AmberFire.copy(alpha = 0.25f), Color.Transparent)), CircleShape)
                            .border(1.5.dp, AmberFire.copy(alpha = 0.55f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.LocalFireDepartment, null,
                            modifier = Modifier.size(38.dp).scale(fireScale), tint = AmberFire)
                    }
                    Column {
                        Text(
                            text = "${streakInfo.currentStreak}",
                            style = Typography.displayMedium.copy(
                                fontWeight = FontWeight.Black,
                                brush = Brush.horizontalGradient(listOf(AmberFire, Color(0xFFFF6B1A)))
                            )
                        )
                        Text("Day Streak", style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(4.dp))
                        Text("Last active: ${streakInfo.lastOpenDateText}", style = Typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f))
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Stats Row
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StreakStatCard("Total Days", "${activeDates.size}", VioletAccent, Modifier.weight(1f))
                StreakStatCard("Longest", "${longestStreak}d", ActiveHot, Modifier.weight(1f))
                StreakStatCard("This Month", "$thisMonthCount", Color(0xFF38BDF8), Modifier.weight(1f))
            }

            Spacer(Modifier.height(16.dp))

            // Calendar Heatmap
            GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                accentColor = VioletAccent, cornerRadius = 20.dp) {
                Column(modifier = Modifier.padding(16.dp)) {

                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { displayMonth = displayMonth.minusMonths(1) },
                            modifier = Modifier.size(36.dp)) {
                            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, "Prev",
                                tint = MaterialTheme.colorScheme.onSurface)
                        }
                        Text(
                            displayMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                            style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val canGoForward = displayMonth.isBefore(YearMonth.now())
                        IconButton(
                            onClick = { if (canGoForward) displayMonth = displayMonth.plusMonths(1) },
                            modifier = Modifier.size(36.dp), enabled = canGoForward
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, "Next",
                                tint = if (canGoForward) MaterialTheme.colorScheme.onSurface
                                       else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("S","M","T","W","T","F","S").forEach { d ->
                            Text(d, modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.50f),
                                fontSize = 9.sp)
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    val firstDay = displayMonth.atDay(1)
                    val startOffset = firstDay.dayOfWeek.value % 7
                    val cells = buildList<LocalDate?> {
                        repeat(startOffset) { add(null) }
                        for (d in 1..displayMonth.lengthOfMonth()) add(firstDay.plusDays(d - 1L))
                    }

                    cells.chunked(7).forEach { week ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                            repeat(7) { idx ->
                                val date = week.getOrNull(idx)
                                Box(modifier = Modifier.weight(1f).aspectRatio(1f).padding(2.dp),
                                    contentAlignment = Alignment.Center) {
                                    if (date != null) {
                                        CalendarCell(date, date in activeDates, date == today)
                                    }
                                }
                            }
                            if (week.size < 7) repeat(7 - week.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("Less ", style = Typography.labelSmall.copy(fontSize = 9.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f))
                        listOf(Inactive, Active1, Active2, Active3, ActiveHot).forEach { col ->
                            Box(Modifier.padding(horizontal = 2.dp).size(10.dp).clip(RoundedCornerShape(3.dp)).background(col))
                        }
                        Text(" More", style = Typography.labelSmall.copy(fontSize = 9.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                accentColor = ActiveHot.copy(alpha = 0.4f), cornerRadius = 18.dp) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    val motivation = when {
                        streakInfo.currentStreak <= 1  -> "Every journey starts with Day 1. Come back every day to grow your streak!"
                        streakInfo.currentStreak <= 7  -> "One week is just the beginning! Don t break the chain."
                        streakInfo.currentStreak <= 30 -> "${streakInfo.currentStreak} days and counting! You are building an elite habit."
                        else -> "${streakInfo.currentStreak} days! Your consistency is your superpower."
                    }
                    Text(motivation, style = Typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, lineHeight = 22.sp),
                        color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
                }
            }

            Spacer(Modifier.height(110.dp))
        }
    }
}

@Composable
private fun CalendarCell(date: LocalDate, isActive: Boolean, isToday: Boolean) {
    val today = remember { LocalDate.now() }
    val daysSince = if (isActive) (today.toEpochDay() - date.toEpochDay()).toInt() else null
    val cellColor = when {
        !isActive -> Inactive
        daysSince != null && daysSince <= 7  -> ActiveHot
        daysSince != null && daysSince <= 30 -> Active3
        daysSince != null && daysSince <= 90 -> Active2
        else -> Active1
    }
    val isFuture = date.isAfter(today)
    Box(
        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(5.dp))
            .background(if (isFuture) Color.Transparent else cellColor)
            .then(if (isToday) Modifier.border(1.5.dp, Color(0xFFF59E0B), RoundedCornerShape(5.dp)) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${date.dayOfMonth}", fontSize = 9.sp,
            fontWeight = if (isActive || isToday) FontWeight.Bold else FontWeight.Normal,
            color = when {
                isFuture -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                isToday  -> Color(0xFFF59E0B)
                isActive -> Color.White.copy(alpha = 0.92f)
                else     -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.30f)
            }
        )
    }
}

@Composable
private fun StreakStatCard(label: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier, cornerRadius = 16.dp, accentColor = valueColor.copy(alpha = 0.4f)) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = Typography.headlineSmall.copy(fontWeight = FontWeight.Black,
                brush = Brush.verticalGradient(listOf(valueColor, valueColor.copy(alpha = 0.7f)))))
            Spacer(Modifier.height(2.dp))
            Text(label, style = Typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                textAlign = TextAlign.Center, fontSize = 10.sp)
        }
    }
}