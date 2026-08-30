package com.alvarocervantes.fittrackplus.core.navigation

data class ShellBottomDestination(
    val route: AppRoute,
    val label: String
)

enum class DrawerItemKind {
    Navigation,

    /** Runs a shell action identified by the item title (e.g. exporting data). */
    Action,
    FutureAction,

    /** Shows [DrawerItem.message] as a snackbar, for items that only explain something. */
    InfoAction
}

data class DrawerItem(
    val title: String,
    val kind: DrawerItemKind,
    val route: AppRoute? = null,
    val isFuture: Boolean = false,
    val message: String? = null
)

fun shellBottomDestinations(): List<ShellBottomDestination> {
    return listOf(
        ShellBottomDestination(AppRoute.Home, "Inicio"),
        ShellBottomDestination(AppRoute.Routines, "Rutinas"),
        ShellBottomDestination(AppRoute.Workout, "Entrenar"),
        ShellBottomDestination(AppRoute.History, "Historial"),
        ShellBottomDestination(AppRoute.Stats, "Datos")
    )
}

fun shellDrawerItems(): List<DrawerItem> {
    return listOf(
        DrawerItem(
            title = "Ajustes avanzados",
            kind = DrawerItemKind.Navigation,
            route = AppRoute.Settings
        ),
        DrawerItem(
            title = "Widget & atajos",
            kind = DrawerItemKind.InfoAction,
            message = "El widget ya esta disponible: mantén pulsada la pantalla de inicio " +
                "de tu movil y busca FitTrackPlus."
        ),
        DrawerItem(
            title = "Exportar datos",
            kind = DrawerItemKind.Action
        )
    )
}
