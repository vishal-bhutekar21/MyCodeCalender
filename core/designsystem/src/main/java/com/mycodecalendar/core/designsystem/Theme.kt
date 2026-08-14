package com.mycodecalendar.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────────────────────
// DARK SCHEME — Deep Obsidian Midnight with Electric Orange & Purple Accents
// ─────────────────────────────────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary              = Color(0xFFFF6B00),   // signature electric orange (#FF6B00)
    onPrimary            = Color(0xFFFFFFFF),
    primaryContainer     = Color(0x33FF6B00),   // glowing translucent orange
    onPrimaryContainer   = Color(0xFFFFD8BF),

    secondary            = Color(0xFF6C5CE7),   // slate lavender / purple (#6C5CE7)
    onSecondary          = Color(0xFFFFFFFF),
    secondaryContainer   = Color(0xFF2E236C),
    onSecondaryContainer = Color(0xFFDCD6FD),

    tertiary             = Color(0xFF38BDF8),   // sky cyan
    onTertiary           = Color(0xFF082F49),
    tertiaryContainer    = Color(0xFF075985),
    onTertiaryContainer  = Color(0xFFBAE6FD),

    background    = Color(0xFF090C15),   // deep obsidian dark
    onBackground  = Color(0xFFFFFFFF),   // pure snow white heading

    surface       = Color(0xFF121624),   // elevated midnight slate
    onSurface     = Color(0xFFF8FAFC),   // crisp bright snow slate

    surfaceVariant    = Color(0xFF1B2033),   // slate card container
    onSurfaceVariant  = Color(0xFFCBD5E1),   // luminous slate-300 body text

    outline        = Color(0xFF2A314A),
    outlineVariant = Color(0xFF1E2438),

    error   = Color(0xFFFB7185),
    onError = Color(0xFF4C0519)
)

// ─────────────────────────────────────────────────────────────────────────────
// LIGHT SCHEME — Crisp off-white with vibrant orange highlights
// ─────────────────────────────────────────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary              = Color(0xFFFF6B00),   // signature electric orange
    onPrimary            = Color(0xFFFFFFFF),
    primaryContainer     = Color(0xFFFFF0E6),
    onPrimaryContainer   = Color(0xFF7A2E00),

    secondary            = Color(0xFF6C5CE7),   // slate purple
    onSecondary          = Color(0xFFFFFFFF),
    secondaryContainer   = Color(0xFFF0EDFD),
    onSecondaryContainer = Color(0xFF291E6A),

    tertiary             = Color(0xFF0284C7),   // sky-600
    onTertiary           = Color(0xFFFFFFFF),
    tertiaryContainer    = Color(0xFFF0F9FF),
    onTertiaryContainer  = Color(0xFF0C4A6E),

    background    = Color(0xFFF9FAFB),   // clean off-white
    onBackground  = Color(0xFF0F172A),   // deep charcoal heading

    surface       = Color(0xFFFFFFFF),   // pure white card surface
    onSurface     = Color(0xFF0F172A),   // charcoal slate

    surfaceVariant    = Color(0xFFF1F5F9),   // slate-100
    onSurfaceVariant  = Color(0xFF334155),   // dark slate-700 body text

    outline        = Color(0xFFCBD5E1),   // slate-300
    outlineVariant = Color(0xFFE2E8F0),

    error   = Color(0xFFE11D48),
    onError = Color(0xFFFFFFFF)
)

// ─────────────────────────────────────────────────────────────────────────────
// Theme entry point
// ─────────────────────────────────────────────────────────────────────────────
enum class AppTheme { LIGHT, DARK, SYSTEM }

@Composable
fun MyCodeCalendarTheme(
    appTheme: AppTheme = AppTheme.SYSTEM,
    content: @Composable () -> Unit
) {
    val dark = when (appTheme) {
        AppTheme.DARK   -> true
        AppTheme.LIGHT  -> false
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }

    MaterialTheme(
        colorScheme = if (dark) DarkColorScheme else LightColorScheme,
        typography  = Typography,
        content     = content
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// GlassmorphismBackground — full-screen background wrapper
// ─────────────────────────────────────────────────────────────────────────────

/**
 * GlassmorphismBackground — full-screen background container with ambient mesh nodes.
 *
 * Dark Mode: Deep obsidian base (#090C15) with vibrant orange (#FF6B00) and lavender (#6C5CE7) ambient radial glows.
 * Light Mode: Clean off-white (#F9FAFB) with soft warm ambient glows.
 */
@Composable
fun GlassmorphismBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == DarkColorScheme.background || 
                 MaterialTheme.colorScheme.surface == DarkColorScheme.surface

    val node1 = if (isDark) Color(0x30FF6B00) else Color(0x14FF6B00)  // vibrant orange glow
    val node2 = if (isDark) Color(0x2E6C5CE7) else Color(0x126C5CE7)  // slate purple glow
    val node3 = if (isDark) Color(0x2038BDF8) else Color(0x0C38BDF8)  // cyan glow
    val baseBg = if (isDark) Color(0xFF090C15) else Color(0xFFF9FAFB)

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                val w = size.width
                val h = size.height

                // Base canvas fill
                drawRect(color = baseBg)

                // Node 1: Top-right radial glow (Brand Orange)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(node1, Color.Transparent),
                        center = Offset(w * 0.85f, h * 0.08f),
                        radius = h * 0.48f
                    ),
                    radius = h * 0.48f,
                    center = Offset(w * 0.85f, h * 0.08f)
                )

                // Node 2: Top-left radial glow (Slate Purple)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(node2, Color.Transparent),
                        center = Offset(w * 0.12f, h * 0.22f),
                        radius = h * 0.46f
                    ),
                    radius = h * 0.46f,
                    center = Offset(w * 0.12f, h * 0.22f)
                )

                // Node 3: Bottom-center ambient glow (Orange/Cyan mix)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(node3, Color.Transparent),
                        center = Offset(w * 0.5f, h * 0.90f),
                        radius = h * 0.40f
                    ),
                    radius = h * 0.40f,
                    center = Offset(w * 0.5f, h * 0.90f)
                )
            },
        content = content
    )
}
