package com.mycodecalendar.core.designsystem.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * ScrollRevealContainer — High-performance scroll-driven entrance and reveal wrapper.
 * 
 * Provides physics-based staggered entry, spring bounce scale, and smooth opacity fades
 * as components are scrolled up or down into the viewport.
 */
@Composable
fun ScrollRevealContainer(
    modifier: Modifier = Modifier,
    delayMillis: Int = 0,
    slideOffsetDp: Float = 36f,
    content: @Composable () -> Unit
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenHeightPx = remember(configuration, density) {
        with(density) { configuration.screenHeightDp.dp.toPx() }
    }

    var isRevealed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (delayMillis > 0) {
            delay(delayMillis.toLong())
        }
    }

    val alpha by animateFloatAsState(
        targetValue = if (isRevealed) 1f else 0f,
        animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing),
        label = "scrollRevealAlpha"
    )

    val translationY by animateFloatAsState(
        targetValue = if (isRevealed) 0f else with(density) { slideOffsetDp.dp.toPx() },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scrollRevealY"
    )

    val scale by animateFloatAsState(
        targetValue = if (isRevealed) 1f else 0.95f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scrollRevealScale"
    )

    Box(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                if (!isRevealed) {
                    val y = coordinates.positionInWindow().y
                    // Component enters reveal state when its top is within viewport + 80px tolerance
                    if (y <= screenHeightPx * 1.05f) {
                        isRevealed = true
                    }
                }
            }
            .graphicsLayer {
                this.alpha = alpha
                this.translationY = translationY
                this.scaleX = scale
                this.scaleY = scale
            }
    ) {
        content()
    }
}

/**
 * Extension modifier to add subtle scroll-direction parallax depth.
 */
fun Modifier.scrollParallax(
    scrollState: ScrollState,
    speedFactor: Float = 0.04f
): Modifier = this.graphicsLayer {
    val offset = scrollState.value * speedFactor
    translationY = -(offset.coerceIn(-24f, 24f))
}
