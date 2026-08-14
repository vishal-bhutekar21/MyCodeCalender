package com.mycodecalendar.feature.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycodecalendar.core.designsystem.BrandPrimaryOrange
import com.mycodecalendar.core.designsystem.GlassmorphismBackground
import com.mycodecalendar.core.designsystem.Typography
import kotlinx.coroutines.delay

/**
 * Ultra-Clean Minimalist 2-Second Typing Splash Screen.
 *
 * Design:
 * - Clean terminal typing animation (types "Code Calendar" character-by-character with blinking cursor).
 * - Deep Obsidian background with subtle electric orange ambient glow.
 * - Exact 2.0-second timed progression sequence.
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val fullText = "Code Calendar"
    var displayedCharsCount by remember { mutableIntStateOf(0) }
    val progressAnim = remember { Animatable(0f) }
    val contentAlpha = remember { Animatable(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "cursorTransition")
    val cursorBlink by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursorBlink"
    )

    // Smooth breathing circle animation (small to big)
    val circleScale by infiniteTransition.animateFloat(
        initialValue = 0.82f,
        targetValue = 1.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "circleScale"
    )

    // Slow blinking white light aura
    val whiteLightGlow by infiniteTransition.animateFloat(
        initialValue = 0.10f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "whiteLightGlow"
    )

    // Master 550ms Sequence Controller
    LaunchedEffect(Unit) {
        // Fade in container smoothly (0-120ms)
        contentAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(120, easing = FastOutSlowInEasing)
        )
    }

    LaunchedEffect(Unit) {
        // Snappy Typing Effect: 13 characters over ~280ms (starts after 50ms delay)
        delay(50)
        for (i in 1..fullText.length) {
            displayedCharsCount = i
            delay(20)
        }
    }

    LaunchedEffect(Unit) {
        // Smooth progress bar from 0 -> 100% over 480ms
        progressAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(480, easing = FastOutSlowInEasing)
        )
        // Hold for final 70ms to total exactly 550ms
        delay(70)
        onSplashFinished()
    }

    GlassmorphismBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .alpha(contentAlpha.value)
            ) {
                Spacer(modifier = Modifier.weight(1f))

                // ── ICON WITH CENTERED GLOWING CIRCLE & GLOWING WHITE 0.5.DP BORDER ──
                Box(
                    modifier = Modifier.size(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer dynamic gradient halo centered behind icon
                    Box(
                        modifier = Modifier
                            .size((220 * circleScale).dp)
                            .drawBehind {
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            BrandPrimaryOrange.copy(alpha = 0.28f),
                                            Color(0xFF818CF8).copy(alpha = 0.12f),
                                            Color.Transparent
                                        ),
                                        center = center,
                                        radius = size.width * 0.75f
                                    )
                                )
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = whiteLightGlow * 0.40f),
                                            Color.White.copy(alpha = whiteLightGlow * 0.10f),
                                            Color.Transparent
                                        ),
                                        center = center,
                                        radius = size.width * 0.45f
                                    )
                                )
                            }
                    )

                    // Soft animated glowing white light aura ring
                    Box(
                        modifier = Modifier
                            .size((96 * circleScale).dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        Color.White.copy(alpha = whiteLightGlow * 0.30f),
                                        BrandPrimaryOrange.copy(alpha = 0.25f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Premium Icon container with 0.5.dp glowing white border
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFF151B28),
                                        Color(0xFF0F131C)
                                    )
                                )
                            )
                            .border(
                                0.5.dp,
                                Brush.verticalGradient(
                                    listOf(
                                        Color.White.copy(alpha = (whiteLightGlow * 0.75f + 0.25f).coerceIn(0f, 1f)),
                                        BrandPrimaryOrange.copy(alpha = 0.60f),
                                        Color.White.copy(alpha = (whiteLightGlow * 0.50f + 0.15f).coerceIn(0f, 1f))
                                    )
                                ),
                                RoundedCornerShape(22.dp)
                            )
                            .shadow(16.dp, RoundedCornerShape(22.dp), spotColor = Color.White.copy(alpha = whiteLightGlow * 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Terminal,
                            contentDescription = "Code Calendar",
                            modifier = Modifier.size(36.dp),
                            tint = BrandPrimaryOrange
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ── LIVE CODE TYPING PROMPT ────────────────────────────────────────
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = fullText.substring(0, displayedCharsCount),
                        style = Typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 28.sp,
                            letterSpacing = (-0.5).sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    // Electric Orange Blinking Terminal Cursor
                    Box(
                        modifier = Modifier
                            .padding(start = 3.dp)
                            .width(3.dp)
                            .height(28.dp)
                            .alpha(cursorBlink)
                            .background(BrandPrimaryOrange, RoundedCornerShape(1.dp))
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Clean Subtitle
                Text(
                    text = "Live Contests · Ratings · Coding Streaks",
                    style = Typography.bodySmall.copy(
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.weight(1f))

                // ── MINIMAL 1PX LINE PROGRESS BAR ───────────────────────────────────
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(0.50f)
                ) {
                    LinearProgressIndicator(
                        progress = { progressAnim.value },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.5.dp)
                            .clip(CircleShape),
                        color = BrandPrimaryOrange,
                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "READY",
                        style = Typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.40f)
                    )
                }

                Spacer(modifier = Modifier.height(36.dp))
            }
        }
    }
}
