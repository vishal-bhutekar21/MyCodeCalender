package com.mycodecalendar.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────────────────────
// DARK SCHEME  —  Deep navy/slate neutral with a single indigo accent
// ─────────────────────────────────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary              = Color(0xFF818CF8),   // indigo-400 — softer for dark bg
    onPrimary            = Color(0xFF1E1B4B),
    primaryContainer     = Color(0xFF312E81),   // indigo-900 — very dark container
    onPrimaryContainer   = Color(0xFFC7D2FE),   // indigo-200

    secondary            = Color(0xFF34D399),   // emerald-400
    onSecondary          = Color(0xFF064E3B),
    secondaryContainer   = Color(0xFF065F46),
    onSecondaryContainer = Color(0xFFA7F3D0),

    tertiary             = Color(0xFFFBBF24),   // amber-400 — streak only
    onTertiary           = Color(0xFF78350F),
    tertiaryContainer    = Color(0xFF92400E),
    onTertiaryContainer  = Color(0xFFFDE68A),

    background    = Color(0xFF0D1117),   // GitHub dark — near-black with blue tint
    onBackground  = Color(0xFFE6EDF3),   // very light text on dark

    surface       = Color(0xFF161B22),   // cards — slightly lighter than background
    onSurface     = Color(0xFFE6EDF3),

    surfaceVariant    = Color(0xFF21262D),   // elevated surfaces, chip backgrounds
    onSurfaceVariant  = Color(0xFF8B949E),   // secondary text — medium gray

    outline        = Color(0xFF30363D),   // borders — subtle
    outlineVariant = Color(0xFF21262D),

    error   = Color(0xFFF87171),
    onError = Color(0xFF7F1D1D)
)

// ─────────────────────────────────────────────────────────────────────────────
// LIGHT SCHEME  —  Clean white with clear contrast, single indigo accent
// ─────────────────────────────────────────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary              = Color(0xFF4F46E5),   // indigo-600 — strong on white
    onPrimary            = Color(0xFFFFFFFF),
    primaryContainer     = Color(0xFFE0E7FF),   // indigo-100
    onPrimaryContainer   = Color(0xFF3730A3),   // indigo-700

    secondary            = Color(0xFF059669),   // emerald-600
    onSecondary          = Color(0xFFFFFFFF),
    secondaryContainer   = Color(0xFFD1FAE5),
    onSecondaryContainer = Color(0xFF064E3B),

    tertiary             = Color(0xFFD97706),   // amber-600
    onTertiary           = Color(0xFFFFFFFF),
    tertiaryContainer    = Color(0xFFFEF3C7),
    onTertiaryContainer  = Color(0xFF78350F),

    background    = Color(0xFFF6F8FA),   // GitHub light — off-white, not harsh
    onBackground  = Color(0xFF0D1117),   // near-black — strong contrast

    surface       = Color(0xFFFFFFFF),   // cards — pure white
    onSurface     = Color(0xFF0D1117),   // near-black on white

    surfaceVariant    = Color(0xFFEAECEF),   // chip backgrounds — clear visible
    onSurfaceVariant  = Color(0xFF434A54),   // secondary text — dark enough to read

    outline        = Color(0xFFD0D7DE),   // borders — clear but not harsh
    outlineVariant = Color(0xFFE8ECF0),

    error   = Color(0xFFDC2626),
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
