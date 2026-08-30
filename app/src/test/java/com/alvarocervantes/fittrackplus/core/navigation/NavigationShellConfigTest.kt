package com.alvarocervantes.fittrackplus.core.navigation

import com.alvarocervantes.fittrackplus.routeFromOpenTab
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationShellConfigTest {

    @Test
    fun routeFromOpenTabMapsSupportedShortcutTargets() {
        assertEquals(AppRoute.Workout, routeFromOpenTab("workout"))
        assertEquals(AppRoute.Stats, routeFromOpenTab("stats"))
        assertEquals(null, routeFromOpenTab("unknown"))
    }

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
    fun `drawer entries are all real actions with nothing left as coming soon`() {
        val items = shellDrawerItems()

        assertTrue(
            items.any { item ->
                item.kind == DrawerItemKind.Navigation &&
                    item.route == AppRoute.Settings &&
                    !item.isFuture
            }
        )
        // The widget already ships, so this entry explains how to add it instead of
        // claiming the feature is still to come.
        assertTrue(
            items.any { item ->
                item.title == "Widget & atajos" &&
                    item.kind == DrawerItemKind.InfoAction &&
                    !item.message.isNullOrBlank() &&
                    !item.isFuture
            }
        )
        assertTrue(
            items.any { item ->
                item.title == "Exportar datos" &&
                    item.kind == DrawerItemKind.Action &&
                    !item.isFuture
            }
        )
        assertFalse(items.any { item -> item.isFuture })
    }

    @Test
    fun `widget entry no longer claims to be a future action since the widget already exists`() {
        val items = shellDrawerItems()
        val widgetItem = items.first { it.title == "Widget & atajos" }

        assertEquals(DrawerItemKind.InfoAction, widgetItem.kind)
        assertFalse(widgetItem.isFuture)
        assertTrue(widgetItem.message.orEmpty().isNotBlank())
    }
}
