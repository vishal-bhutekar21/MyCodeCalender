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
// DARK SCHEME — Deep OLED Obsidian Navy with Electric Neon Accents
// ─────────────────────────────────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary              = Color(0xFF818CF8),   // electric indigo-400
    onPrimary            = Color(0xFF070A11),
    primaryContainer     = Color(0xFF1E1B4B),
    onPrimaryContainer   = Color(0xFFE0E7FF),

    secondary            = Color(0xFF34D399),   // neon emerald-400
    onSecondary          = Color(0xFF022C22),
    secondaryContainer   = Color(0xFF064E3B),
    onSecondaryContainer = Color(0xFFA7F3D0),

    tertiary             = Color(0xFF38BDF8),   // neon cyan-400
    onTertiary           = Color(0xFF082F49),
    tertiaryContainer    = Color(0xFF075985),
    onTertiaryContainer  = Color(0xFFBAE6FD),

    background    = Color(0xFF06090F),   // deep OLED obsidian black
    onBackground  = Color(0xFFFFFFFF),   // pure snow white heading

    surface       = Color(0xFF0E131F),   // midnight slate
    onSurface     = Color(0xFFF8FAFC),   // crisp bright snow slate

    surfaceVariant    = Color(0xFF182234),   // slate card container
    onSurfaceVariant  = Color(0xFFCBD5E1),   // luminous slate-300 body text

    outline        = Color(0xFF334155),
    outlineVariant = Color(0xFF1E293B),

    error   = Color(0xFFFB7185),
    onError = Color(0xFF4C0519)
)

// ─────────────────────────────────────────────────────────────────────────────
// LIGHT SCHEME — Minimal, crisp off-white with high-contrast slate text
// ─────────────────────────────────────────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary              = Color(0xFF4F46E5),   // indigo-600
    onPrimary            = Color(0xFFFFFFFF),
    primaryContainer     = Color(0xFFEEF2FF),
    onPrimaryContainer   = Color(0xFF312E81),

    secondary            = Color(0xFF059669),   // emerald-600
    onSecondary          = Color(0xFFFFFFFF),
    secondaryContainer   = Color(0xFFECFDF5),
    onSecondaryContainer = Color(0xFF064E3B),

    tertiary             = Color(0xFF0284C7),   // sky-600
    onTertiary           = Color(0xFFFFFFFF),
    tertiaryContainer    = Color(0xFFF0F9FF),
    onTertiaryContainer  = Color(0xFF0C4A6E),

    background    = Color(0xFFF8FAFC),   // clean off-white (#F8FAFC)
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
 * Dark Mode: Deep OLED obsidian base (#070A11) with radiant indigo, cyan, and emerald radial glows.
 * Light Mode: Clean off-white (#F8FAFC) with soft pastel radial glows.
 */
@Composable
fun GlassmorphismBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()

    val node1 = if (isDark) Color(0x3D6366F1) else Color(0x126366F1)  // electric indigo
    val node2 = if (isDark) Color(0x3006B6D4) else Color(0x1038BDF8)  // neon cyan
    val node3 = if (isDark) Color(0x2410B981) else Color(0x0D10B981)  // neon emerald
    val baseBg = if (isDark) Color(0xFF070A11) else Color(0xFFF8FAFC)

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                val w = size.width
                val h = size.height

                // Base canvas fill
                drawRect(color = baseBg)

                // Node 1: Top-left radial glow (Indigo)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(node1, Color.Transparent),
                        center = Offset(w * 0.12f, h * 0.04f),
                        radius = h * 0.52f
                    ),
                    radius = h * 0.52f,
                    center = Offset(w * 0.12f, h * 0.04f)
                )

                // Node 2: Top-right radial glow (Cyan)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(node2, Color.Transparent),
                        center = Offset(w * 0.88f, h * 0.08f),
                        radius = h * 0.44f
                    ),
                    radius = h * 0.44f,
                    center = Offset(w * 0.88f, h * 0.08f)
                )

                // Node 3: Bottom-center emerald glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(node3, Color.Transparent),
                        center = Offset(w * 0.5f, h * 0.92f),
                        radius = h * 0.38f
                    ),
                    radius = h * 0.38f,
                    center = Offset(w * 0.5f, h * 0.92f)
                )
            },
        content = content
    )
}
