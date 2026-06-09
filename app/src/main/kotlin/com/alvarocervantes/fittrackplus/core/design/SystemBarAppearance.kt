package com.alvarocervantes.fittrackplus.core.design

import androidx.compose.ui.graphics.Color

val LightSystemBarBackground = Color(0xFFF4F4F1)
val DarkSystemBarBackground = Color(0xFF181A18)

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
