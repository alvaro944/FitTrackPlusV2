package com.alvarocervantes.fittrackplus.core.design

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppThemeModeTest {

    @Test
    fun systemThemeRespectsSystemDarkValue() {
        assertTrue(resolveDarkTheme(AppThemeMode.System, systemDark = true))
        assertFalse(resolveDarkTheme(AppThemeMode.System, systemDark = false))
    }

    @Test
    fun lightThemeAlwaysResolvesToLight() {
        assertFalse(resolveDarkTheme(AppThemeMode.Light, systemDark = true))
        assertFalse(resolveDarkTheme(AppThemeMode.Light, systemDark = false))
    }

    @Test
    fun darkThemeAlwaysResolvesToDark() {
        assertTrue(resolveDarkTheme(AppThemeMode.Dark, systemDark = true))
        assertTrue(resolveDarkTheme(AppThemeMode.Dark, systemDark = false))
    }

    @Test
    fun parserFallsBackToSystemForMissingOrUnknownValues() {
        assertEquals(AppThemeMode.System, AppThemeMode.fromStorageValue(null))
        assertEquals(AppThemeMode.System, AppThemeMode.fromStorageValue("unknown"))
    }

    @Test
    fun parserReadsKnownStorageValues() {
        assertEquals(AppThemeMode.System, AppThemeMode.fromStorageValue("system"))
        assertEquals(AppThemeMode.Light, AppThemeMode.fromStorageValue("light"))
        assertEquals(AppThemeMode.Dark, AppThemeMode.fromStorageValue("dark"))
    }
}
