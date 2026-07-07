package com.alvarocervantes.fittrackplus.grit.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Modern Grit design system.
 *
 * Standalone dark-only visual language: near-black surfaces, acid lime accent,
 * monospace labels and heavy uppercase headings. It does not derive from the
 * classic FitTrackPlus theme.
 */
object GritColors {
    val Lime = Color(0xFFD7EF00)
    val LimeLight = Color(0xFFD4EB00)
    val Background = Color(0xFF0A0A0A)
    val Surface = Color(0xFF121212)
    val SurfaceContainer = Color(0xFF201F1F)
    val SurfaceContainerHigh = Color(0xFF2A2A2A)
    val SurfaceContainerLow = Color(0xFF1C1B1B)
    val Border = Color(0xFF1F1F1F)
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFFA1A1A1)
    val TextFaint = Color(0xFF737373)
    val Red = Color(0xFFC5020B)
    val RedBorder = Color(0xFFEF4444)
    val Black = Color(0xFF000000)
    val Neutral800 = Color(0xFF262626)
    val Neutral900 = Color(0xFF171717)
}

object GritType {
    val screenTitle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 26.sp,
        letterSpacing = (-0.5).sp,
        color = GritColors.TextPrimary
    )
    val cardTitle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 20.sp,
        letterSpacing = (-0.25).sp,
        color = GritColors.TextPrimary
    )
    val itemTitle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = GritColors.TextPrimary
    )
    val statValue = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 30.sp,
        color = GritColors.Lime
    )
    val monoLabel = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 1.sp,
        color = GritColors.TextSecondary
    )
    val monoLabelSmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 9.sp,
        letterSpacing = 0.8.sp,
        color = GritColors.TextSecondary
    )
    val monoBody = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        color = GritColors.TextSecondary
    )
    val monoStrong = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        letterSpacing = 1.sp,
        color = GritColors.TextPrimary
    )
    val timer = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 44.sp,
        color = GritColors.Lime
    )
}

object GritShapes {
    val small = RoundedCornerShape(2.dp)
    val medium = RoundedCornerShape(4.dp)
    val large = RoundedCornerShape(6.dp)
}

private val GritColorScheme = darkColorScheme(
    primary = GritColors.Lime,
    onPrimary = GritColors.Black,
    primaryContainer = GritColors.SurfaceContainer,
    onPrimaryContainer = GritColors.Lime,
    secondary = GritColors.Lime,
    onSecondary = GritColors.Black,
    secondaryContainer = GritColors.SurfaceContainerHigh,
    onSecondaryContainer = GritColors.TextPrimary,
    tertiary = GritColors.LimeLight,
    onTertiary = GritColors.Black,
    background = GritColors.Background,
    onBackground = GritColors.TextPrimary,
    surface = GritColors.Surface,
    onSurface = GritColors.TextPrimary,
    surfaceVariant = GritColors.SurfaceContainerLow,
    onSurfaceVariant = GritColors.TextSecondary,
    outline = GritColors.Border,
    outlineVariant = GritColors.Neutral900,
    error = GritColors.Red,
    onError = GritColors.TextPrimary,
    surfaceContainer = GritColors.SurfaceContainer,
    surfaceContainerHigh = GritColors.SurfaceContainerHigh,
    surfaceContainerLow = GritColors.SurfaceContainerLow
)

private val GritMaterialTypography = Typography(
    titleLarge = GritType.cardTitle,
    titleMedium = GritType.itemTitle,
    titleSmall = GritType.monoStrong,
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 15.sp,
        color = GritColors.TextPrimary
    ),
    bodyMedium = GritType.monoBody,
    bodySmall = GritType.monoLabelSmall,
    labelLarge = GritType.monoStrong,
    labelMedium = GritType.monoLabel,
    labelSmall = GritType.monoLabelSmall
)

private val GritMaterialShapes = Shapes(
    extraSmall = GritShapes.small,
    small = GritShapes.small,
    medium = GritShapes.medium,
    large = GritShapes.large,
    extraLarge = RoundedCornerShape(8.dp)
)

@Composable
fun ModernGritTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GritColorScheme,
        typography = GritMaterialTypography,
        shapes = GritMaterialShapes,
        content = content
    )
}
