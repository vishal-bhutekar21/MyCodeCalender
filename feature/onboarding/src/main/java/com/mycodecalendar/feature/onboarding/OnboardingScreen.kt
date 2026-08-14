package com.mycodecalendar.feature.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycodecalendar.core.designsystem.GlassmorphismBackground
import com.mycodecalendar.core.designsystem.Typography
import com.mycodecalendar.core.designsystem.components.GlassBackButton
import com.mycodecalendar.core.designsystem.components.GlassCard

data class OnboardingSlide(
    val badge: String,
    val headlinePrefix: String,
    val headlineHighlight: String,
    val description: String,
    val badgeColor: Color,
    val accentColor: Color
)

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    var currentStep by remember { mutableStateOf(0) }
    val totalSteps = 5

    val brandOrange = Color(0xFFFF6B00)
    val brandPurple = Color(0xFF6C5CE7)
    val brandLavender = Color(0xFF7C4DFF)
    val brandCoral = Color(0xFFFF5722)
    val brandIndigo = Color(0xFF6366F1)

    val slides = remember {
        listOf(
            OnboardingSlide(
                badge = "SMART SCHEDULING",
                headlinePrefix = "Plan your code.\nMaster your goals.\n",
                headlineHighlight = "Build your future.",
                description = "The all-in-one coding calendar for developers to plan schedules, track progress, prepare for interviews, and stay consistent.",
                badgeColor = brandLavender,
                accentColor = brandOrange
            ),
            OnboardingSlide(
                badge = "DAILY HABITS & GOALS",
                headlinePrefix = "Stay consistent.\nCrush daily targets.\n",
                headlineHighlight = "Code every day.",
                description = "Monitor daily progress bars, solve structured problem sets, and build lasting algorithmic problem-solving habits.",
                badgeColor = brandOrange,
                accentColor = brandOrange
            ),
            OnboardingSlide(
                badge = "LIVE CONTEST RADAR",
                headlinePrefix = "Never miss a round.\nReal-time alerts for\n",
                headlineHighlight = "All CP platforms.",
                description = "Live countdowns and instant feeds aggregated from Codeforces, LeetCode, CodeChef, and AtCoder with time-zone synchronization.",
                badgeColor = brandIndigo,
                accentColor = brandIndigo
            ),
            OnboardingSlide(
                badge = "DETAILED ANALYTICS",
                headlinePrefix = "Visualize growth.\nMonitor weekly metrics &\n",
                headlineHighlight = "Rating curves.",
                description = "Track rating progression curves, GitHub contribution heatmaps, study time, and maintain your 7+ day coding streak.",
                badgeColor = brandCoral,
                accentColor = brandCoral
            ),
            OnboardingSlide(
                badge = "DEVELOPER HUB",
                headlinePrefix = "Everything you need.\nTools & roadmaps to\n",
                headlineHighlight = "Level up your skills.",
                description = "Smart calendar, goal tracking, problem practice, and detailed analytics in one unified, sleek developer toolkit.",
                badgeColor = brandLavender,
                accentColor = brandOrange
            )
        )
    }

    val currentSlide = slides[currentStep]

    GlassmorphismBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ── TOP HEADER (Logo Badge, Back, Skip) ─────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Back button or Logo branding
                if (currentStep > 0) {
                    GlassBackButton(onClick = { currentStep-- })
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    Brush.linearGradient(listOf(Color(0xFF1E2235), Color(0xFF141724))),
                                    RoundedCornerShape(8.dp)
                                )
                                .border(1.dp, brandOrange.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.Code,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = brandOrange
                            )
                        }
                        Row {
                            Text(
                                text = "MyCode",
                                style = Typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Calendar",
                                style = Typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = brandOrange
                            )
                        }
                    }
                }

                // Right: Skip Button
                TextButton(
                    onClick = onComplete,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Skip",
                        style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }

            // ── ANIMATED INFOGRAPHIC & SLIDE CONTENT ────────────────────────
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { it } + fadeIn()).togetherWith(
                            slideOutHorizontally { -it } + fadeOut()
                        )
                    } else {
                        (slideInHorizontally { -it } + fadeIn()).togetherWith(
                            slideOutHorizontally { it } + fadeOut()
                        )
                    }
                },
                label = "onboardingSlideContent",
                modifier = Modifier.weight(1f)
            ) { step ->
                val slide = slides[step]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Infographic Illustration Card matching the exact image phones
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        when (step) {
                            0 -> CalendarScheduleInfographic(slide.accentColor)
                            1 -> DailyProgressDashboardInfographic(slide.accentColor)
                            2 -> ContestRadarInfographic(slide.accentColor)
                            3 -> AnalyticsAndStreakInfographic(slide.accentColor)
                            4 -> LevelUpFeatureGridInfographic(slide.accentColor)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Title with colored signature highlight
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = slide.headlinePrefix.trimEnd(),
                            style = Typography.headlineLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp,
                                lineHeight = 30.sp
                            ),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = slide.headlineHighlight,
                            style = Typography.headlineLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp,
                                lineHeight = 30.sp
                            ),
                            textAlign = TextAlign.Center,
                            color = slide.accentColor
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = slide.description,
                        style = Typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )
                }
            }

            // ── BOTTOM CONTROLS (Expanding Indicators & Orange CTA Button) ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Expanding Pill Page Indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until totalSteps) {
                        val isSelected = i == currentStep
                        val pillWidth by animateDpAsState(
                            targetValue = if (isSelected) 34.dp else 8.dp,
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            label = "indicatorWidth_$i"
                        )
                        val pillColor by animateColorAsState(
                            targetValue = if (isSelected) currentSlide.accentColor
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                            label = "indicatorColor_$i"
                        )

                        Box(
                            modifier = Modifier
                                .size(width = pillWidth, height = 7.dp)
                                .clip(CircleShape)
                                .background(pillColor)
                        )
                    }
                }

                // CTA Button in vivid Electric Orange
                Button(
                    onClick = {
                        if (currentStep < totalSteps - 1) {
                            currentStep++
                        } else {
                            onComplete()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .shadow(16.dp, RoundedCornerShape(16.dp), spotColor = brandOrange.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = brandOrange
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (currentStep == totalSteps - 1) "Build Your Future →" else "Continue",
                            style = Typography.labelLarge.copy(fontWeight = FontWeight.Black, fontSize = 15.sp),
                            color = Color.White
                        )
                        if (currentStep < totalSteps - 1) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ANIMATED INFOGRAPHIC ILLUSTRATIONS (Modeled after reference image)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Slide 1 Infographic: Calendar & Schedule Showcase (Phone 1 from image)
 */
@Composable
private fun CalendarScheduleInfographic(accentColor: Color) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(horizontal = 8.dp),
        accentColor = Color(0xFF7C4DFF),
        cornerRadius = 24.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Calendar Month & Days Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF7C4DFF)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Calendar May 2025 >",
                        style = Typography.labelMedium.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF7C4DFF).copy(alpha = 0.15f)
                ) {
                    Text(
                        "15 Active",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = Typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = Color(0xFF7C4DFF)
                    )
                }
            }

            // Days Grid Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("11", "12", "13", "14", "15", "16", "17").forEachIndexed { idx, day ->
                    val isToday = idx == 4
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                if (isToday) Color(0xFF7C4DFF)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day,
                            style = Typography.labelSmall.copy(
                                fontWeight = if (isToday) FontWeight.Black else FontWeight.Normal,
                                fontSize = 11.sp
                            ),
                            color = if (isToday) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // "Today's Schedule" items
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ScheduleItemRow(
                    icon = Icons.Rounded.Code,
                    title = "Two Sum",
                    tag = "LeetCode • Easy",
                    time = "9:00 AM",
                    tagColor = Color(0xFF10B981)
                )
                ScheduleItemRow(
                    icon = Icons.Rounded.AccountTree,
                    title = "Trees in Binary Tree",
                    tag = "LeetCode • Medium",
                    time = "11:30 AM",
                    tagColor = Color(0xFFFF9800)
                )
                ScheduleItemRow(
                    icon = Icons.Rounded.EmojiEvents,
                    title = "Codeforces Round 950",
                    tag = "Practice Round",
                    time = "7:00 PM",
                    tagColor = Color(0xFF3B82F6)
                )
            }
        }
    }
}

@Composable
private fun ScheduleItemRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    tag: String,
    time: String,
    tagColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = tagColor)
            Column {
                Text(title, style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                Text(tag, style = Typography.labelSmall.copy(fontSize = 10.sp), color = tagColor)
            }
        }
        Text(time, style = Typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/**
 * Slide 2 Infographic: Dashboard with Daily Progress 75% & Upcoming Tasks (Phone 2 from image)
 */
@Composable
private fun DailyProgressDashboardInfographic(accentColor: Color) {
    val brandOrange = Color(0xFFFF6B00)

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(horizontal = 8.dp),
        accentColor = brandOrange,
        cornerRadius = 24.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Good Morning, Developer! 👋
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Good Morning,",
                        style = Typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Developer! 👋",
                        style = Typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(brandOrange.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Notifications, null, modifier = Modifier.size(16.dp), tint = brandOrange)
                }
            }

            // Daily Progress 75% Card with Glowing Orange Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1A1F30))
                    .border(1.dp, brandOrange.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Daily Progress",
                            style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            "75%",
                            style = Typography.labelSmall.copy(fontWeight = FontWeight.Black),
                            color = brandOrange
                        )
                    }

                    // Progress Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2B3147))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(0.75f)
                                .clip(CircleShape)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFFF8A00), Color(0xFFFF5200))
                                    )
                                )
                        )
                    }

                    Text(
                        "Great job! Keep it up 🔥",
                        style = Typography.labelSmall.copy(fontSize = 10.sp),
                        color = Color(0xFFFFB74D)
                    )
                }
            }

            // Upcoming Tasks row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UpcomingTaskMiniCard(
                    title = "Dynamic Programming",
                    time = "2:00 PM",
                    color = Color(0xFFFF9800),
                    modifier = Modifier.weight(1f)
                )
                UpcomingTaskMiniCard(
                    title = "System Design",
                    time = "4:00 PM",
                    color = Color(0xFF6C5CE7),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun UpcomingTaskMiniCard(
    title: String,
    time: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .padding(8.dp)
    ) {
        Column {
            Text(title, style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp), maxLines = 1, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(2.dp))
            Text(time, style = Typography.labelSmall.copy(fontSize = 10.sp), color = color)
        }
    }
}

/**
 * Slide 3 Infographic: Multi-Platform Radar
 */
@Composable
private fun ContestRadarInfographic(accentColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    GlassCard(
        modifier = Modifier
            .size(220.dp),
        accentColor = accentColor,
        cornerRadius = 28.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Concentric Glowing Radar Rings
            Box(
                modifier = Modifier
                    .size(160.dp * pulseScale)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.08f))
                    .border(1.dp, accentColor.copy(alpha = 0.25f), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(105.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(1.5.dp, accentColor.copy(alpha = 0.45f), CircleShape)
            )

            // Center Icon
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(
                        Brush.radialGradient(listOf(accentColor, accentColor.copy(alpha = 0.7f))),
                        CircleShape
                    )
                    .shadow(12.dp, CircleShape, spotColor = accentColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.EmojiEvents,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = Color.White
                )
            }

            // Floating Platform Badges
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PlatformTagPill("CF", Color(0xFF3B82F6))
                PlatformTagPill("LC", Color(0xFFFFA116))
            }

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(vertical = 14.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PlatformTagPill("● LIVE", Color(0xFF00F579))
                PlatformTagPill("CC", Color(0xFF8B5CF6))
            }
        }
    }
}

/**
 * Slide 4 Infographic: Analytics & Streak Showcase (Phone 3 from image)
 */
@Composable
private fun AnalyticsAndStreakInfographic(accentColor: Color) {
    val brandOrange = Color(0xFFFF6B00)
    val brandPurple = Color(0xFF7C4DFF)

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(horizontal = 8.dp),
        accentColor = brandOrange,
        cornerRadius = 24.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Progress + Curve Chart
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Progress",
                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Text(
                        "This Week ▾",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = Typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Smooth Rating Line Chart Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    val path = Path().apply {
                        moveTo(0f, h * 0.7f)
                        cubicTo(w * 0.2f, h * 0.85f, w * 0.35f, h * 0.3f, w * 0.55f, h * 0.4f)
                        cubicTo(w * 0.7f, h * 0.5f, w * 0.85f, h * 0.1f, w, h * 0.2f)
                    }

                    // Glow line
                    drawPath(
                        path = path,
                        color = brandOrange,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Target Dot
                    drawCircle(
                        color = brandOrange,
                        radius = 5.dp.toPx(),
                        center = Offset(w * 0.88f, h * 0.14f)
                    )
                }
            }

            // 3 Metric Stat Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MetricStatPill(
                    icon = Icons.Rounded.CheckCircle,
                    value = "86",
                    label = "Solved",
                    color = brandPurple,
                    modifier = Modifier.weight(1f)
                )
                MetricStatPill(
                    icon = Icons.Rounded.Timer,
                    value = "14h 30m",
                    label = "Study Time",
                    color = brandPurple,
                    modifier = Modifier.weight(1.2f)
                )
                MetricStatPill(
                    icon = Icons.Rounded.LocalFireDepartment,
                    value = "7 Days",
                    label = "Streak 🔥",
                    color = brandOrange,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MetricStatPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .padding(horizontal = 6.dp, vertical = 6.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, style = Typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 11.sp), color = MaterialTheme.colorScheme.onSurface)
            Text(label, style = Typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/**
 * Slide 5 Infographic: "Everything You Need to Level Up" 4-Grid Showcase (from bottom of reference image)
 */
@Composable
private fun LevelUpFeatureGridInfographic(accentColor: Color) {
    val brandOrange = Color(0xFFFF6B00)
    val brandPurple = Color(0xFF7C4DFF)
    val brandIndigo = Color(0xFF6366F1)
    val brandCoral = Color(0xFFFF5722)

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(horizontal = 8.dp),
        accentColor = brandOrange,
        cornerRadius = 24.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Everything you need to level up",
                style = Typography.labelMedium.copy(fontWeight = FontWeight.Black),
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FeatureGridCard(
                    icon = Icons.Rounded.CalendarMonth,
                    title = "Smart Calendar",
                    desc = "Plan study & contests",
                    accent = brandPurple,
                    modifier = Modifier.weight(1f)
                )
                FeatureGridCard(
                    icon = Icons.Rounded.TrackChanges,
                    title = "Goal Tracking",
                    desc = "Track daily progress",
                    accent = brandOrange,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FeatureGridCard(
                    icon = Icons.Rounded.Code,
                    title = "Problem Practice",
                    desc = "Curated algorithms",
                    accent = brandIndigo,
                    modifier = Modifier.weight(1f)
                )
                FeatureGridCard(
                    icon = Icons.Rounded.PieChart,
                    title = "Detailed Analytics",
                    desc = "Progress & streaks",
                    accent = brandCoral,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun FeatureGridCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(accent.copy(alpha = 0.18f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = accent)
            }
            Column {
                Text(title, style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp), maxLines = 1, color = MaterialTheme.colorScheme.onSurface)
                Text(desc, style = Typography.labelSmall.copy(fontSize = 9.sp), maxLines = 1, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun PlatformTagPill(name: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.18f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.45f))
    ) {
        Text(
            text = name,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
            color = color
        )
    }
}
