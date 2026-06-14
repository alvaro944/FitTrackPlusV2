package com.alvarocervantes.fittrackplus.core.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.alvarocervantes.fittrackplus.core.design.FitTrackAppShell
import com.alvarocervantes.fittrackplus.feature.history.HistoryScreen
import com.alvarocervantes.fittrackplus.feature.home.HomeScreen
import com.alvarocervantes.fittrackplus.feature.routines.RoutinesScreen
import com.alvarocervantes.fittrackplus.feature.settings.SettingsScreen
import com.alvarocervantes.fittrackplus.feature.stats.StatsScreen
import com.alvarocervantes.fittrackplus.feature.workout.WorkoutScreen
import androidx.compose.runtime.LaunchedEffect

@Composable
fun FitTrackPlusNavHost(initialTab: AppRoute? = null) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val currentRoute = currentDestination.asAppRoute()

    LaunchedEffect(initialTab) {
        if (initialTab != null) {
            navController.navigateToTopLevel(initialTab)
        }
    }

    FitTrackAppShell(
        currentRoute = currentRoute,
        onNavigateToTopLevel = { navController.navigateToTopLevel(it) },
        onNavigateToSecondary = { navController.navigateToSecondary(it) },
        content = { innerPadding ->
            AppNavGraph(navController = navController, contentPadding = innerPadding)
        }
    )
}

@Composable
private fun AppNavGraph(
    navController: NavHostController,
    contentPadding: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = AppRoute.Home.route,
        modifier = Modifier.padding(contentPadding),
        enterTransition = { fadeIn(animationSpec = tween(200)) },
        exitTransition = { fadeOut(animationSpec = tween(200)) },
        popEnterTransition = { fadeIn(animationSpec = tween(200)) },
        popExitTransition = { fadeOut(animationSpec = tween(200)) }
    ) {
        composable(AppRoute.Home.route) {
            HomeScreen(
                onGoToRoutines = { navController.navigateToTopLevel(AppRoute.Routines) },
                onGoToWorkout = { navController.navigateToTopLevel(AppRoute.Workout) },
                onGoToHistory = { navController.navigateToTopLevel(AppRoute.History) },
                onGoToStats = { navController.navigateToTopLevel(AppRoute.Stats) }
            )
        }
        composable(AppRoute.Routines.route) { RoutinesScreen() }
        composable(AppRoute.Workout.route) {
            WorkoutScreen(
                onGoToRoutines = { navController.navigateToTopLevel(AppRoute.Routines) }
            )
        }
        composable(AppRoute.History.route) { HistoryScreen() }
        composable(AppRoute.Stats.route) { StatsScreen() }
        composable(AppRoute.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}

private fun NavHostController.navigateToTopLevel(route: AppRoute) {
    navigate(route.route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

private fun NavHostController.navigateToSecondary(route: AppRoute) {
    navigate(route.route) {
        launchSingleTop = true
    }
}

private fun androidx.navigation.NavDestination?.asAppRoute(): AppRoute? {
    val matchedRoute = this?.hierarchy?.firstNotNullOfOrNull { destination ->
        when (destination.route) {
            AppRoute.Home.route -> AppRoute.Home
            AppRoute.Routines.route -> AppRoute.Routines
            AppRoute.Workout.route -> AppRoute.Workout
            AppRoute.History.route -> AppRoute.History
            AppRoute.Stats.route -> AppRoute.Stats
            AppRoute.Settings.route -> AppRoute.Settings
            else -> null
        }
    }
    return matchedRoute
}
