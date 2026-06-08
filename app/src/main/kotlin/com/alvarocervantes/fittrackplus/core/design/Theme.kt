package com.alvarocervantes.fittrackplus.core.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LightColors = lightColorScheme(
    primary = Color(0xFF1F6B57),
    onPrimary = Color.White,
    secondary = Color(0xFFC47A49),
    onSecondary = Color.White,
    tertiary = Color(0xFF3A8870),
    background = Color(0xFFF4F4F1),
    onBackground = Color(0xFF161816),
    surface = Color(0xFFFCFBF7),
    onSurface = Color(0xFF161816),
    surfaceVariant = Color(0xFFE8E5DD),
    onSurfaceVariant = Color(0xFF5E655F),
    outline = Color(0xFFD5D0C5),
    error = Color(0xFFB15249),
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF2E8B6E),
    onPrimary = Color(0xFFEEF0EE),
    secondary = Color(0xFFD4895A),
    onSecondary = Color(0xFF181A18),
    tertiary = Color(0xFF3AA882),
    background = Color(0xFF181A18),
    onBackground = Color(0xFFEEF0EE),
    surface = Color(0xFF1F221F),
    onSurface = Color(0xFFEEF0EE),
    surfaceVariant = Color(0xFF272A27),
    onSurfaceVariant = Color(0xFF8A928B),
    outline = Color(0xFF353835),
    error = Color(0xFFC96260),
    onError = Color(0xFF181A18)
)

@Immutable
data class FitTrackPlusExtraColors(
    val surfaceAlt: Color,
    val surfaceCard: Color,
    val borderLight: Color,
    val textTertiary: Color,
    val primaryDark: Color,
    val primarySoft: Color,
    val primaryMid: Color,
    val accentWarm: Color,
    val accentSoft: Color,
    val errorSoft: Color
)

private val LightExtraColors = FitTrackPlusExtraColors(
    surfaceAlt = Color(0xFFE8E5DD),
    surfaceCard = Color(0xFFFCFBF7),
    borderLight = Color(0xFFD5D0C5),
    textTertiary = Color(0xFF9AA09B),
    primaryDark = Color(0xFF174D40),
    primarySoft = Color(0xFFD9E8E1),
    primaryMid = Color(0xFF2E8B6E),
    accentWarm = Color(0xFFC47A49),
    accentSoft = Color(0xFFF0E0D3),
    errorSoft = Color(0xFFF2DED9)
)

private val DarkExtraColors = FitTrackPlusExtraColors(
    surfaceAlt = Color(0xFF272A27),
    surfaceCard = Color(0xFF1F221F),
    borderLight = Color(0xFF353835),
    textTertiary = Color(0xFF5E655F),
    primaryDark = Color(0xFF3AA882),
    primarySoft = Color(0xFF1A3830),
    primaryMid = Color(0xFF2E8B6E),
    accentWarm = Color(0xFFD4895A),
    accentSoft = Color(0xFF2D2018),
    errorSoft = Color(0xFF2D1919)
)

private val LocalFitTrackPlusExtraColors = staticCompositionLocalOf { LightExtraColors }

private val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.2.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.6.sp
    )
)

private val AppShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
)

object FitTrackPlusStyle {
    val extraColors: FitTrackPlusExtraColors
        @Composable
        get() = LocalFitTrackPlusExtraColors.current
}

@Composable
fun FitTrackPlusTheme(
    themeMode: AppThemeMode = AppThemeMode.System,
    content: @Composable () -> Unit
) {
    val darkTheme = resolveDarkTheme(themeMode, isSystemInDarkTheme())
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val extraColors = if (darkTheme) DarkExtraColors else LightExtraColors

    CompositionLocalProvider(
        LocalFitTrackPlusExtraColors provides extraColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }
}

val ColorScheme.primarySoft: Color
    @Composable
    get() = FitTrackPlusStyle.extraColors.primarySoft

val ColorScheme.primaryDark: Color
    @Composable
    get() = FitTrackPlusStyle.extraColors.primaryDark

val ColorScheme.surfaceAlt: Color
    @Composable
    get() = FitTrackPlusStyle.extraColors.surfaceAlt

val ColorScheme.surfaceCard: Color
    @Composable
    get() = FitTrackPlusStyle.extraColors.surfaceCard

val ColorScheme.borderLight: Color
    @Composable
    get() = FitTrackPlusStyle.extraColors.borderLight

val ColorScheme.textTertiary: Color
    @Composable
    get() = FitTrackPlusStyle.extraColors.textTertiary

val ColorScheme.accentWarm: Color
    @Composable
    get() = FitTrackPlusStyle.extraColors.accentWarm

val ColorScheme.accentSoft: Color
    @Composable
    get() = FitTrackPlusStyle.extraColors.accentSoft

val ColorScheme.errorSoft: Color
    @Composable
    get() = FitTrackPlusStyle.extraColors.errorSoft

val ColorScheme.primaryMid: Color
    @Composable
    get() = FitTrackPlusStyle.extraColors.primaryMid
