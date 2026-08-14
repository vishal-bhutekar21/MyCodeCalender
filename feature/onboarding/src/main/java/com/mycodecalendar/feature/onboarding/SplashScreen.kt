package com.mycodecalendar.feature.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycodecalendar.core.designsystem.GlassmorphismBackground
import com.mycodecalendar.core.designsystem.Typography
import kotlinx.coroutines.delay

/**
 * Modern, Majestic Animated Splash Screen.
 *
 * Visual Features:
 * - Ambient pulsating neon aura with rotating gradient ring.
 * - Spring-based overshoot scale entrance for the core developer emblem.
 * - Staggered typography and tagline slide-up animation.
 * - Floating platform chips showcasing unified CP hub support.
 * - Smooth exit transition after initialization delay.
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    // ── Animation Controllers ───────────────────────────────────────────────
    val scaleAnim = remember { Animatable(0.4f) }
    val alphaAnim = remember { Animatable(0f) }
    val textAlphaAnim = remember { Animatable(0f) }
    val textSlideAnim = remember { Animatable(40f) }
    val chipsAlphaAnim = remember { Animatable(0f) }
    val progressAnim = remember { Animatable(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "splashLoop")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ringRotation"
    )

    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlow"
    )

    val brandIndigo = Color(0xFF818CF8)
    val brandViolet = Color(0xFFA78BFA)
    val brandCyan = Color(0xFF38BDF8)
    val brandEmerald = Color(0xFF34D399)

    LaunchedEffect(Unit) {
        // Phase 1: Logo zoom & alpha burst
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    LaunchedEffect(Unit) {
        alphaAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(600, easing = FastOutSlowInEasing)
        )
        // Phase 2: Title & tagline reveal
        textAlphaAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(500, easing = FastOutSlowInEasing)
        )
    }

    LaunchedEffect(Unit) {
        textSlideAnim.animateTo(
            targetValue = 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
        // Phase 3: Platform chips & progress line
        chipsAlphaAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(400, easing = FastOutSlowInEasing)
        )
        progressAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(1200, easing = LinearOutSlowInEasing)
        )

        // Hold and trigger exit
        delay(300)
        onSplashFinished()
    }

    GlassmorphismBackground {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Background ambient canvas stars / glow points
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Ambient gradient circle in center
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            brandIndigo.copy(alpha = 0.18f * pulseGlow),
                            brandViolet.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = Offset(w / 2, h / 2),
                        radius = 280.dp.toPx()
                    ),
                    radius = 280.dp.toPx(),
                    center = Offset(w / 2, h / 2)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.weight(1f))

                // ── CORE EMBLEM WITH ROTATING CYBER RING ─────────────────────────
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .scale(scaleAnim.value)
                        .alpha(alphaAnim.value),
                    contentAlignment = Alignment.Center
                ) {
                    // Rotating Gradient Outer Glow Ring
                    Box(
                        modifier = Modifier
                            .size(136.dp)
                            .rotate(rotation)
                            .border(
                                width = 2.dp,
                                brush = Brush.sweepGradient(
                                    listOf(
                                        brandIndigo,
                                        brandCyan,
                                        brandEmerald,
                                        brandViolet,
                                        brandIndigo
                                    )
                                ),
                                shape = CircleShape
                            )
                    )

                    // Outer soft pulsing halo
                    Box(
                        modifier = Modifier
                            .size(116.dp * pulseGlow)
                            .clip(CircleShape)
                            .background(brandIndigo.copy(alpha = 0.12f))
                    )

                    // Inner Glassmorphic Orb
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        Color(0xFF1E1B4B),
                                        Color(0xFF0F172A)
                                    )
                                )
                            )
                            .border(1.5.dp, brandIndigo.copy(alpha = 0.5f), CircleShape)
                            .shadow(24.dp, CircleShape, spotColor = brandIndigo),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Terminal,
                            contentDescription = null,
                            modifier = Modifier.size(46.dp),
                            tint = brandIndigo
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // ── TYPOGRAPHY REVEAL ───────────────────────────────────────────
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .alpha(textAlphaAnim.value)
                        .offset(y = textSlideAnim.value.dp)
                ) {
                    Text(
                        text = "Code Calendar",
                        style = Typography.displayMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 32.sp,
                            brush = Brush.horizontalGradient(
                                listOf(brandIndigo, brandViolet, brandCyan)
                            )
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Unified Developer & Contest Hub",
                        style = Typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // ── PLATFORM ACCENT CHIPS ───────────────────────────────────────
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.alpha(chipsAlphaAnim.value)
                ) {
                    SplashPlatformPill("Codeforces", Color(0xFF3B82F6))
                    SplashPlatformPill("LeetCode", Color(0xFFF59E0B))
                    SplashPlatformPill("GitHub", Color(0xFF10F07B))
                    SplashPlatformPill("CodeChef", Color(0xFF8B5CF6))
                }

                Spacer(modifier = Modifier.weight(1f))

                // ── BOTTOM PROGRESS LINE & VERSION ──────────────────────────────
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(bottom = 32.dp)
                        .alpha(chipsAlphaAnim.value)
                ) {
                    // Sleek progress bar
                    Box(
                        modifier = Modifier
                            .width(180.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progressAnim.value)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(brandIndigo, brandCyan, brandEmerald)
                                    )
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "SYNCING CONTESTS & RATINGS",
                        style = Typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SplashPlatformPill(name: String, accentColor: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = accentColor.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
    ) {
        Text(
            text = name,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = Typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
            color = accentColor
        )
    }
}
