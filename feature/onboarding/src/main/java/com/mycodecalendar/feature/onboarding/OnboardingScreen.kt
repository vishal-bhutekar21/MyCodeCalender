package com.mycodecalendar.feature.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycodecalendar.core.designsystem.GlassmorphismBackground
import com.mycodecalendar.core.designsystem.Typography
import com.mycodecalendar.core.designsystem.components.GlassCard

data class OnboardingSlide(
    val badge: String,
    val title: String,
    val description: String,
    val accentColor: Color
)

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    var currentStep by remember { mutableStateOf(0) }
    val totalSteps = 5

    val slides = remember {
        listOf(
            OnboardingSlide(
                badge = "REAL-TIME AGGREGATOR",
                title = "Never Miss a Contest",
                description = "Live countdowns and instant contest feeds aggregated from Codeforces, LeetCode, CodeChef, and AtCoder with time-zone synchronization.",
                accentColor = Color(0xFF818CF8)
            ),
            OnboardingSlide(
                badge = "UNIFIED RATINGS",
                title = "Track Ranks & Performance",
                description = "Connect your handles across all competitive programming platforms to view unified rating progressions, global rankings, and problem breakdown.",
                accentColor = Color(0xFF38BDF8)
            ),
            OnboardingSlide(
                badge = "ACTIVITY & STREAKS",
                title = "Daily Heatmaps & Streak",
                description = "Visualize your GitHub contributions and maintain your daily problem-solving streak with motivating milestones and monthly heatmaps.",
                accentColor = Color(0xFF34D399)
            ),
            OnboardingSlide(
                badge = "SMART CALENDAR",
                title = "1-Tap Calendar & Alerts",
                description = "Export upcoming contests directly to your device calendar and configure customizable 15-minute advance notifications.",
                accentColor = Color(0xFFF59E0B)
            ),
            OnboardingSlide(
                badge = "CURATED RESOURCES",
                title = "Master Algorithms & CP",
                description = "Explore handpicked roadmaps, cheat sheets, dynamic programming patterns, and curated interview problem sets to level up.",
                accentColor = Color(0xFFA78BFA)
            )
        )
    }

    val currentSlide = slides[currentStep]

    GlassmorphismBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ── TOP BAR (Back / Skip) ───────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep > 0) {
                    IconButton(
                        onClick = { currentStep-- },
                        modifier = Modifier
                            .size(38.dp)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), CircleShape)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Previous",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(38.dp))
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = currentSlide.accentColor.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, currentSlide.accentColor.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = currentSlide.badge,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 10.sp),
                        color = currentSlide.accentColor
                    )
                }

                TextButton(
                    onClick = onComplete,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
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
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Infographic Illustration Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        when (step) {
                            0 -> ContestRadarInfographic(slide.accentColor)
                            1 -> RatingProgressInfographic(slide.accentColor)
                            2 -> HeatmapStreakInfographic(slide.accentColor)
                            3 -> CalendarAlertsInfographic(slide.accentColor)
                            4 -> ResourcesCodeInfographic(slide.accentColor)
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Text(
                        text = slide.title,
                        style = Typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 26.sp
                        ),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = slide.description,
                        style = Typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }

            // ── BOTTOM CONTROLS (Pill Indicators & CTA Button) ──────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Expanding Pill Page Indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until totalSteps) {
                        val isSelected = i == currentStep
                        val pillWidth by animateDpAsState(
                            targetValue = if (isSelected) 32.dp else 8.dp,
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
                                .size(width = pillWidth, height = 8.dp)
                                .clip(CircleShape)
                                .background(pillColor)
                        )
                    }
                }

                // CTA Button
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
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = currentSlide.accentColor
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (currentStep == totalSteps - 1) "Get Started →" else "Continue",
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
// ANIMATED INFOGRAPHIC ILLUSTRATIONS
// ─────────────────────────────────────────────────────────────────────────────

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
                PlatformTagPill("LC", Color(0xFFF59E0B))
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

@Composable
private fun RatingProgressInfographic(accentColor: Color) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(horizontal = 16.dp),
        accentColor = accentColor,
        cornerRadius = 24.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "RATING: 2140",
                        style = Typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = accentColor
                    )
                    Text(
                        "Global Rank #412",
                        style = Typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = accentColor.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.40f))
                ) {
                    Text(
                        "Master ★",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = accentColor
                    )
                }
            }

            // Mini visual rating line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                listOf(40.dp, 65.dp, 55.dp, 90.dp, 80.dp, 115.dp, 130.dp).forEachIndexed { idx, h ->
                    val isLast = idx == 6
                    Box(
                        modifier = Modifier
                            .width(18.dp)
                            .height(h)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(
                                if (isLast) Brush.verticalGradient(listOf(accentColor, accentColor.copy(alpha = 0.4f)))
                                else Brush.verticalGradient(listOf(accentColor.copy(alpha = 0.35f), accentColor.copy(alpha = 0.1f)))
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun HeatmapStreakInfographic(accentColor: Color) {
    GlassCard(
        modifier = Modifier
            .size(220.dp),
        accentColor = accentColor,
        cornerRadius = 28.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Fire badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .background(Color(0xFFF59E0B).copy(alpha = 0.15f), CircleShape)
                    .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.40f), CircleShape)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    Icons.Rounded.LocalFireDepartment,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color(0xFFF59E0B)
                )
                Text(
                    "14 DAYS ACTIVE",
                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Black),
                    color = Color(0xFFF59E0B)
                )
            }

            // Activity Grid (5x4)
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                repeat(4) { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        repeat(6) { col ->
                            val level = (row + col) % 5
                            val cellColor = when (level) {
                                4 -> Color(0xFF00F579)
                                3 -> Color(0xFF00C962)
                                2 -> Color(0xFF006D35)
                                1 -> Color(0xFF00381B)
                                else -> Color(0xFF1E293B)
                            }
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(cellColor)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarAlertsInfographic(accentColor: Color) {
    GlassCard(
        modifier = Modifier
            .size(220.dp),
        accentColor = accentColor,
        cornerRadius = 28.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(accentColor.copy(alpha = 0.15f), CircleShape)
                    .border(1.5.dp, accentColor.copy(alpha = 0.45f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.NotificationsActive,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = accentColor
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Codeforces Round 950",
                    style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Starts in 15 mins",
                    style = Typography.labelSmall,
                    color = accentColor
                )
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Text(
                    "✓ Synced with Google Calendar",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = Typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ResourcesCodeInfographic(accentColor: Color) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(horizontal = 16.dp),
        accentColor = accentColor,
        cornerRadius = 24.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(10.dp).background(Color(0xFFF43F5E), CircleShape))
                    Box(modifier = Modifier.size(10.dp).background(Color(0xFFF59E0B), CircleShape))
                    Box(modifier = Modifier.size(10.dp).background(Color(0xFF10B981), CircleShape))
                }
                Text(
                    "roadmap.cpp",
                    style = Typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "// Core CP Topics & Templates",
                    style = Typography.bodySmall.copy(fontSize = 12.sp),
                    color = accentColor.copy(alpha = 0.8f)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TopicChip("Dynamic Programming", accentColor)
                    TopicChip("Segment Trees", Color(0xFF38BDF8))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TopicChip("Graph Flow", Color(0xFF34D399))
                    TopicChip("Binary Search", Color(0xFFF59E0B))
                }
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

@Composable
private fun TopicChip(name: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.30f))
    ) {
        Text(
            text = name,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = Typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
            color = color
        )
    }
}

