package com.alvarocervantes.fittrackplus.core.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationShellConfigTest {

    @Test
    fun `bottom navigation keeps five primary tabs and excludes settings`() {
        val destinations = shellBottomDestinations()

        assertEquals(
            listOf(
                AppRoute.Home,
                AppRoute.Routines,
                AppRoute.Workout,
                AppRoute.History,
                AppRoute.Stats
            ),
            destinations.map { it.route }
        )
        assertFalse(destinations.any { it.route == AppRoute.Settings })
    }

    @Test
    fun `drawer contains real settings entry and future actions`() {
        val items = shellDrawerItems()

        assertTrue(
            items.any { item ->
                item.kind == DrawerItemKind.Navigation &&
                    item.route == AppRoute.Settings &&
                    !item.isFuture
            }
        )
        assertTrue(
            items.any { item ->
                item.title == "Widget & atajos" && item.isFuture
            }
        )
        assertTrue(
            items.any { item ->
                item.title == "Exportar datos" && item.isFuture
            }
        )
    }
}
