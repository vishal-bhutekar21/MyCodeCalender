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
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mycodecalendar.core.designsystem.BrandPrimaryOrange
import kotlinx.coroutines.delay

/**
 * Minimalist, Silky-Smooth Instant App Logo Splash Screen.
 *
 * Characteristics:
 * - Ultra-clean: Displays exclusively the premium app icon in the center.
 * - Zero artificial delay: Smooth 320ms spring entrance, followed immediately by navigation.
 * - Tap-to-skip: Instant touch-down dismiss on any interaction.
 * - Pure Daylight / White Theme aesthetic.
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
    val contentScale = remember { Animatable(0.82f) }

    LaunchedEffect(Unit) {
        // Smooth parallel scale & fade-in (240ms)
        contentAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(240, easing = FastOutSlowInEasing)
        )
    }

    LaunchedEffect(Unit) {
        contentScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(280, easing = FastOutSlowInEasing)
        )
        // Brief silky-smooth visual hold, then proceed
        delay(60)
        finishOnce.value()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
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
        // ── SILKY-SMOOTH CENTERED APP LOGO ──
        Box(
            modifier = Modifier
                .size(140.dp)
                .alpha(contentAlpha.value)
                .scale(contentScale.value),
            contentAlignment = Alignment.Center
        ) {
            // Ambient Orange Glow Halo
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                BrandPrimaryOrange.copy(alpha = 0.22f),
                                Color(0xFF6366F1).copy(alpha = 0.08f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Elevated White Glass Icon Card
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(28.dp),
                        ambientColor = Color(0x18FF6B00),
                        spotColor = Color(0x30FF6B00)
                    )
                    .clip(RoundedCornerShape(28.dp))
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
                                BrandPrimaryOrange.copy(alpha = 0.40f),
                                Color(0xFFE2E8F0)
                            )
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Terminal,
                    contentDescription = "Code Calendar",
                    modifier = Modifier.size(46.dp),
                    tint = BrandPrimaryOrange
                )
            }
        }
    }
}
