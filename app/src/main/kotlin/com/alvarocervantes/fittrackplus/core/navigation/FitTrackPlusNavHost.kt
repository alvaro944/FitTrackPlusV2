package com.alvarocervantes.fittrackplus.core.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.alvarocervantes.fittrackplus.feature.history.HistoryScreen
import com.alvarocervantes.fittrackplus.feature.home.HomeScreen
import com.alvarocervantes.fittrackplus.feature.routines.RoutinesScreen
import com.alvarocervantes.fittrackplus.feature.stats.StatsScreen
import com.alvarocervantes.fittrackplus.feature.workout.WorkoutScreen

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
    BottomDestination(AppRoute.Stats, "Datos", Icons.Filled.BarChart)
)

@Composable
fun FitTrackPlusNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomDestinations.forEach { destination ->
                    val selected = currentDestination?.hierarchy?.any {
                        it.route == destination.route.route
                    } == true

                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(destination.route.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
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
        modifier = Modifier.padding(contentPadding)
    ) {
        composable(AppRoute.Home.route) { HomeScreen() }
        composable(AppRoute.Routines.route) { RoutinesScreen() }
        composable(AppRoute.Workout.route) { WorkoutScreen() }
        composable(AppRoute.History.route) { HistoryScreen() }
        composable(AppRoute.Stats.route) { StatsScreen() }
    }
}
