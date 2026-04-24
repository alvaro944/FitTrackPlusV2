package com.alvarocervantes.fittrackplus.core.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.alvarocervantes.fittrackplus.core.design.borderLight
import com.alvarocervantes.fittrackplus.feature.history.HistoryScreen
import com.alvarocervantes.fittrackplus.feature.home.HomeScreen
import com.alvarocervantes.fittrackplus.feature.routines.RoutinesScreen
import com.alvarocervantes.fittrackplus.feature.settings.SettingsScreen
import com.alvarocervantes.fittrackplus.feature.stats.StatsScreen
import com.alvarocervantes.fittrackplus.feature.workout.WorkoutScreen
import androidx.compose.material3.MaterialTheme

private data class BottomDestination(
    val route: AppRoute,
    val label: String,
    val icon: ImageVector
)

private val bottomDestinations = listOf(
    BottomDestination(AppRoute.Home, "Inicio", Icons.Filled.Home),
    BottomDestination(AppRoute.Routines, "Rutinas", Icons.Filled.List),
    BottomDestination(AppRoute.Workout, "Entrenar", Icons.Filled.FitnessCenter),
    BottomDestination(AppRoute.History, "Historial", Icons.Filled.History),
    BottomDestination(AppRoute.Stats, "Datos", Icons.Filled.BarChart),
    BottomDestination(AppRoute.Settings, "Ajustes", Icons.Filled.Settings)
)

@Composable
fun FitTrackPlusNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    Scaffold(
        bottomBar = {
            Surface(
                shadowElevation = 10.dp,
                tonalElevation = 0.dp,
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.borderLight
                )
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    bottomDestinations.forEach { destination ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.route == destination.route.route
                        } == true

                        NavigationBarItem(
                            selected = selected,
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            onClick = {
                                navController.navigateToTopLevel(destination.route)
                            },
                            icon = {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = destination.label
                                )
                            },
                            label = { Text(destination.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        AppNavGraph(
            navController = navController,
            contentPadding = innerPadding
        )
    }
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
        composable(AppRoute.Settings.route) { SettingsScreen() }
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
