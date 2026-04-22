package com.alvarocervantes.fittrackplus.core.navigation

sealed class AppRoute(val route: String) {
    data object Home : AppRoute("home")
    data object Routines : AppRoute("routines")
    data object Workout : AppRoute("workout")
    data object History : AppRoute("history")
    data object Stats : AppRoute("stats")
    data object Settings : AppRoute("settings")
}
