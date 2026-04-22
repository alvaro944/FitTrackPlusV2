package com.alvarocervantes.fittrackplus.core.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF256C5C),
    onPrimary = Color.White,
    secondary = Color(0xFFB5472F),
    onSecondary = Color.White,
    tertiary = Color(0xFF5866A6),
    background = Color(0xFFF7F7F3),
    onBackground = Color(0xFF171A18),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF171A18),
    surfaceVariant = Color(0xFFE1E7E2),
    onSurfaceVariant = Color(0xFF404841),
    outline = Color(0xFF717971)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8DD8C3),
    onPrimary = Color(0xFF00382E),
    secondary = Color(0xFFFFB59F),
    onSecondary = Color(0xFF5A1B0C),
    tertiary = Color(0xFFC3C6FF),
    background = Color(0xFF111412),
    onBackground = Color(0xFFE2E3DE),
    surface = Color(0xFF191C1A),
    onSurface = Color(0xFFE2E3DE),
    surfaceVariant = Color(0xFF404841),
    onSurfaceVariant = Color(0xFFC1C9C1),
    outline = Color(0xFF8B938B)
)

@Composable
fun FitTrackPlusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
