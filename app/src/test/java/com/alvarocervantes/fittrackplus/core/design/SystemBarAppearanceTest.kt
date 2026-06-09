package com.alvarocervantes.fittrackplus.core.design

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SystemBarAppearanceTest {

    @Test
    fun lightThemeUsesLightBackgroundAndDarkIcons() {
        val appearance = systemBarAppearance(darkTheme = false)

        assertEquals(LightSystemBarBackground, appearance.statusBarBackground)
        assertEquals(LightSystemBarBackground, appearance.navigationBarBackground)
        assertTrue(appearance.useDarkStatusBarIcons)
        assertTrue(appearance.useDarkNavigationBarIcons)
    }

    @Test
    fun darkThemeUsesDarkBackgroundAndLightIcons() {
        val appearance = systemBarAppearance(darkTheme = true)

        assertEquals(DarkSystemBarBackground, appearance.statusBarBackground)
        assertEquals(DarkSystemBarBackground, appearance.navigationBarBackground)
        assertFalse(appearance.useDarkStatusBarIcons)
        assertFalse(appearance.useDarkNavigationBarIcons)
    }
}
