package com.mycodecalendar.feature.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycodecalendar.core.designsystem.BrandPrimaryOrange
import com.mycodecalendar.core.designsystem.GlassmorphismBackground
import com.mycodecalendar.core.designsystem.Typography

/**
 * Ultra-Fast, Fluid, Modern Splash Screen.
 *
 * Characteristics:
 * - Instant responsive launch: ~280ms total animation time.
 * - Tap-to-skip: User can tap anywhere to immediately enter the app.
 * - Pure Daylight / White Theme aesthetic: Crisp white elevated icon card, soft orange glow,
 *   clean modern typography with signature orange accent dot, and sleek progress indicator.
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    var hasFinished by remember { mutableStateOf(false) }
    val finishOnce = rememberUpdatedState {
        if (!hasFinished) {
            hasFinished = true
            onSplashFinished()
        }
    }

    val contentAlpha = remember { Animatable(0f) }
    val contentScale = remember { Animatable(0.92f) }
    val progressAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Snappy, fluid entrance animation (~180ms)
        contentAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(180, easing = FastOutSlowInEasing)
        )
    }

    LaunchedEffect(Unit) {
        contentScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(220, easing = FastOutSlowInEasing)
        )
    }

    LaunchedEffect(Unit) {
        // Fast, satisfying progress fill (280ms)
        progressAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(280, easing = FastOutSlowInEasing)
        )
        finishOnce.value()
    }

    GlassmorphismBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    finishOnce.value()
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .alpha(contentAlpha.value)
                    .scale(contentScale.value)
            ) {
                Spacer(modifier = Modifier.weight(1f))

                // ── ELEVATED PURE WHITE APP ICON CARD WITH ORANGE GLOW ──
                Box(
                    modifier = Modifier.size(110.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Soft ambient orange halo
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        BrandPrimaryOrange.copy(alpha = 0.18f),
                                        Color(0xFF818CF8).copy(alpha = 0.08f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Elevated White Icon Card
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .shadow(
                                elevation = 16.dp,
                                shape = RoundedCornerShape(24.dp),
                                ambientColor = Color(0x18FF6B00),
                                spotColor = Color(0x28FF6B00)
                            )
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color.White,
                                        Color(0xFFFFF7ED)
                                    )
                                )
                            )
                            .border(
                                width = 1.2.dp,
                                brush = Brush.linearGradient(
                                    listOf(
                                        Color.White,
                                        BrandPrimaryOrange.copy(alpha = 0.35f),
                                        Color(0xFFE2E8F0)
                                    )
                                ),
                                shape = RoundedCornerShape(24.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Terminal,
                            contentDescription = "Code Calendar",
                            modifier = Modifier.size(40.dp),
                            tint = BrandPrimaryOrange
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ── BRAND TITLE WITH ORANGE ACCENT DOT ──
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Code Calendar",
                        style = Typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 28.sp,
                            letterSpacing = (-0.5).sp
                        ),
                        color = Color(0xFF0F172A)
                    )
                    Box(
                        modifier = Modifier
                            .padding(start = 3.dp, top = 6.dp)
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(BrandPrimaryOrange)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Subtitle Badge
                Text(
                    text = "Live Contests · Ratings · Coding Streaks",
                    style = Typography.bodySmall.copy(
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.4.sp
                    ),
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.weight(1f))

                // ── MINIMAL FAST PROGRESS BAR ──
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(0.48f)
                ) {
                    LinearProgressIndicator(
                        progress = { progressAnim.value },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(CircleShape),
                        color = BrandPrimaryOrange,
                        trackColor = Color(0xFFE2E8F0)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "STARTING",
                        style = Typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        ),
                        color = Color(0xFF94A3B8)
                    )
                }

                Spacer(modifier = Modifier.height(36.dp))
            }
        }
    }
}
