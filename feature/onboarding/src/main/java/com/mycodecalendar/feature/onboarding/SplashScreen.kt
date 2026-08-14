package com.mycodecalendar.feature.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycodecalendar.core.designsystem.BrandOrangeAccent
import com.mycodecalendar.core.designsystem.BrandPrimaryOrange
import com.mycodecalendar.core.designsystem.GlassmorphismBackground
import com.mycodecalendar.core.designsystem.Typography
import kotlinx.coroutines.delay

/**
 * Modern, High-Performance 2-Second Developer Splash Screen.
 *
 * Design:
 * - Pure Developer/Coder Aesthetic: Deep Obsidian surface with glowing orange radar ring & terminal prompt.
 * - Exact 2.0-second timed animation sequence.
 * - Cohesive Brand Colors: BrandPrimaryOrange (#FF6B00) and Obsidian Slate (#0F121E) — no rainbow clutter.
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val scaleAnim = remember { Animatable(0.6f) }
    val alphaAnim = remember { Animatable(0f) }
    val progressAnim = remember { Animatable(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "splashLoop")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ringRotation"
    )

    val cursorBlink by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursorBlink"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Master 2-Second Animation Controller
    LaunchedEffect(Unit) {
        // Phase 1: Logo & text entrance (0 - 500ms)
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    }

    LaunchedEffect(Unit) {
        alphaAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(400, easing = FastOutSlowInEasing)
        )
    }

    LaunchedEffect(Unit) {
        // Progress runs smoothly for 1800ms
        progressAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(1800, easing = LinearEasing)
        )
        // Brief 200ms hold to reach exactly 2.0 seconds total
        delay(200)
        onSplashFinished()
    }

    GlassmorphismBackground {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Subtle ambient radial glow in signature orange
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            BrandPrimaryOrange.copy(alpha = 0.16f),
                            Color(0xFF6C5CE7).copy(alpha = 0.06f),
                            Color.Transparent
                        ),
                        center = Offset(w / 2, h / 2),
                        radius = 260.dp.toPx()
                    ),
                    radius = 260.dp.toPx(),
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

                // ── SLEEK CODER EMBLEM ──────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .scale(scaleAnim.value * pulseScale)
                        .alpha(alphaAnim.value),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer rotating orange radar ring
                    Box(
                        modifier = Modifier
                            .size(126.dp)
                            .rotate(rotation)
                            .border(
                                width = 1.5.dp,
                                brush = Brush.sweepGradient(
                                    listOf(
                                        BrandPrimaryOrange,
                                        BrandOrangeAccent.copy(alpha = 0.4f),
                                        Color.Transparent,
                                        BrandPrimaryOrange.copy(alpha = 0.8f),
                                        BrandPrimaryOrange
                                    )
                                ),
                                shape = CircleShape
                            )
                    )

                    // Inner slate glass badge
                    Box(
                        modifier = Modifier
                            .size(92.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFF1B2033),
                                        Color(0xFF101422)
                                    )
                                )
                            )
                            .border(1.dp, BrandPrimaryOrange.copy(alpha = 0.5f), RoundedCornerShape(26.dp))
                            .shadow(20.dp, RoundedCornerShape(26.dp), spotColor = BrandPrimaryOrange),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Code,
                            contentDescription = null,
                            modifier = Modifier.size(46.dp),
                            tint = BrandPrimaryOrange
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // ── LOGO & CODER TYPOGRAPHY ─────────────────────────────────────
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.alpha(alphaAnim.value)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "MyCode",
                            style = Typography.displayMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 34.sp,
                                letterSpacing = (-0.5).sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Calendar",
                            style = Typography.displayMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 34.sp,
                                letterSpacing = (-0.5).sp
                            ),
                            color = BrandPrimaryOrange
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Monospace developer prompt tagline
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = ">_ Plan. Code. Conquer.",
                            style = Typography.labelMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                letterSpacing = 1.sp
                            ),
                            color = BrandPrimaryOrange.copy(alpha = 0.9f)
                        )
                        Box(
                            modifier = Modifier
                                .padding(start = 2.dp)
                                .size(width = 6.dp, height = 12.dp)
                                .alpha(cursorBlink)
                                .background(BrandPrimaryOrange)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // ── CLEAN PROGRESS BAR & STATUS ─────────────────────────────────
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(bottom = 36.dp)
                        .alpha(alphaAnim.value)
                ) {
                    Box(
                        modifier = Modifier
                            .width(160.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progressAnim.value)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(BrandPrimaryOrange, BrandOrangeAccent)
                                    )
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "INITIALIZING CONTEST RADAR",
                        style = Typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.8.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                    )
                }
            }
        }
    }
}
