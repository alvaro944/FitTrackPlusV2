package com.alvarocervantes.fittrackplus.core.design

import androidx.compose.ui.graphics.Color

val LightSystemBarBackground = LightColors.background
val DarkSystemBarBackground = DarkColors.background

data class SystemBarAppearance(
    val statusBarBackground: Color,
    val navigationBarBackground: Color,
    val useDarkStatusBarIcons: Boolean,
    val useDarkNavigationBarIcons: Boolean
)

fun systemBarAppearance(darkTheme: Boolean): SystemBarAppearance {
    return if (darkTheme) {
        SystemBarAppearance(
            statusBarBackground = DarkSystemBarBackground,
            navigationBarBackground = DarkSystemBarBackground,
            useDarkStatusBarIcons = false,
            useDarkNavigationBarIcons = false
        )
    } else {
        SystemBarAppearance(
            statusBarBackground = LightSystemBarBackground,
            navigationBarBackground = LightSystemBarBackground,
            useDarkStatusBarIcons = true,
            useDarkNavigationBarIcons = true
        )
    }
}
