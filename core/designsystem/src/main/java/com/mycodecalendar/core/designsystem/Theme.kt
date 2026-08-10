package com.mycodecalendar.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryIndigo,
    onPrimary = Color.White,
    primaryContainer = PrimaryIndigo.copy(alpha = 0.2f),
    onPrimaryContainer = PrimaryIndigo,
    secondary = SecondaryEmerald,
    onSecondary = Color.White,
    secondaryContainer = SecondaryEmerald.copy(alpha = 0.2f),
    onSecondaryContainer = SecondaryEmerald,
    tertiary = AccentCyan,
    onTertiary = Color.White,
    background = SlateDarkBackground,
    onBackground = TextPrimaryDark,
    surface = SlateDarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = SlateDarkSurfaceVariant,
    onSurfaceVariant = TextPrimaryDark,
    outline = SlateDarkBorder,
    outlineVariant = SlateDarkBorder.copy(alpha = 0.5f),
    error = AccentRose,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryIndigoDark,
    onPrimary = Color.White,
    primaryContainer = PrimaryIndigoDark.copy(alpha = 0.12f),
    onPrimaryContainer = PrimaryIndigoDark,
    secondary = SecondaryEmeraldDark,
    onSecondary = Color.White,
    secondaryContainer = SecondaryEmeraldDark.copy(alpha = 0.12f),
    onSecondaryContainer = SecondaryEmeraldDark,
    tertiary = AccentCyan,
    onTertiary = Color.White,
    background = PorcelainLightBackground,
    onBackground = TextPrimaryLight,
    surface = PorcelainLightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = PorcelainLightSurfaceVariant,
    onSurfaceVariant = TextPrimaryLight,
    outline = PorcelainLightBorder,
    outlineVariant = PorcelainLightBorder.copy(alpha = 0.5f),
    error = AccentRose,
    onError = Color.White
)

@Composable
fun MyCodeCalendarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
