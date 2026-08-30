package com.alvarocervantes.fittrackplus.core.design

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alvarocervantes.fittrackplus.core.app.AppVersion
import com.alvarocervantes.fittrackplus.core.design.components.FitTrackSegmentedSelector
import com.alvarocervantes.fittrackplus.core.design.components.FitTrackThemeModeSelector
import com.alvarocervantes.fittrackplus.core.navigation.AppRoute
import com.alvarocervantes.fittrackplus.core.navigation.AppShellViewModel
import com.alvarocervantes.fittrackplus.core.navigation.DrawerItem
import com.alvarocervantes.fittrackplus.core.navigation.DrawerItemKind
import com.alvarocervantes.fittrackplus.core.navigation.NavigationRequestKind
import com.alvarocervantes.fittrackplus.core.navigation.ShellBottomDestination
import com.alvarocervantes.fittrackplus.core.navigation.shellBottomDestinations
import com.alvarocervantes.fittrackplus.core.navigation.shellDrawerItems
import com.alvarocervantes.fittrackplus.domain.usecase.UserDataExport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

@Composable
fun FitTrackAppShell(
    currentRoute: AppRoute?,
    onNavigateToTopLevel: (AppRoute) -> Unit,
    onNavigateToSecondary: (AppRoute) -> Unit,
    content: @Composable (PaddingValues) -> Unit,
    viewModel: AppShellViewModel = hiltViewModel()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val weightUnit by viewModel.weightUnit.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val menuHiddenForRoute by viewModel.menuHiddenForRoute.collectAsStateWithLifecycle()
    val bottomDestinations = remember { shellBottomDestinations() }
    val drawerItems = remember { shellDrawerItems() }
    var pendingExport by remember { mutableStateOf<UserDataExport?>(null) }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        pendingExport?.let { export -> uri?.let { viewModel.saveDataExport(it, export) } }
        pendingExport = null
    }

    LaunchedEffect(message) {
        val currentMessage = message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(currentMessage)
        viewModel.clearMessage()
    }

    LaunchedEffect(Unit) {
        viewModel.approvedNavigation.collect { navigation ->
            when (navigation.kind) {
                NavigationRequestKind.TopLevel -> onNavigateToTopLevel(navigation.route)
                NavigationRequestKind.Secondary -> onNavigateToSecondary(navigation.route)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.exportRequests.collect { export ->
            pendingExport = export
            exportLauncher.launch(export.fileName)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                drawerItems = drawerItems,
                selectedRoute = currentRoute,
                themeMode = themeMode,
                weightUnit = weightUnit,
                onThemeModeChange = viewModel::setThemeMode,
                onWeightUnitChange = viewModel::setWeightUnit,
                onItemClick = { item ->
                    handleDrawerItemClick(
                        item = item,
                        onNavigateToSecondary = onNavigateToSecondary,
                        onRequestNavigation = { targetRoute ->
                            viewModel.requestNavigation(
                                currentRoute = currentRoute,
                                targetRoute = targetRoute,
                                kind = NavigationRequestKind.Secondary
                            )
                        },
                        onNonNavigationItem = { drawerItem ->
                            viewModel.handleNonNavigationDrawerItem(drawerItem)
                        },
                        drawerScope = coroutineScope,
                        closeDrawer = { drawerState.close() }
                    )
                }
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    if (currentRoute != AppRoute.Settings) {
                        BottomNavigationBar(
                            currentRoute = currentRoute,
                            destinations = bottomDestinations,
                            onNavigate = { targetRoute ->
                                if (targetRoute == currentRoute) {
                                    viewModel.notifyActiveTabReselected(targetRoute)
                                } else {
                                    val intercepted = viewModel.requestNavigation(
                                        currentRoute = currentRoute,
                                        targetRoute = targetRoute,
                                        kind = NavigationRequestKind.TopLevel
                                    )
                                    if (!intercepted) {
                                        onNavigateToTopLevel(targetRoute)
                                    }
                                }
                            }
                        )
                    }
                }
            ) { innerPadding ->
                content(innerPadding)
            }

            // Floats over the content at the top end. It stays on the end side because the start
            // side is where every screen header puts its title, and a floating button there would
            // sit on top of it. Screens whose header shows trailing actions in this same corner
            // hide it while those are up.
            if (currentRoute != AppRoute.Settings && menuHiddenForRoute != currentRoute) {
                ShellMenuButton(
                    onClick = {
                        coroutineScope.launch {
                            drawerState.open()
                        }
                    },
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }
        }
    }
}

@Composable
private fun BottomNavigationBar(
    currentRoute: AppRoute?,
    destinations: List<ShellBottomDestination>,
    onNavigate: (AppRoute) -> Unit
) {
    Surface(
        shadowElevation = FitElevation.navBar,
        tonalElevation = FitElevation.none,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.borderLight
        )
    ) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            destinations.forEach { destination ->
                val selected = currentRoute == destination.route
                NavigationBarItem(
                    selected = selected,
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    onClick = { onNavigate(destination.route) },
                    icon = {
                        Icon(
                            imageVector = destination.route.bottomBarIcon(),
                            contentDescription = destination.label
                        )
                    },
                    label = { Text(destination.label) }
                )
            }
        }
    }
}

@Composable
private fun DrawerContent(
    drawerItems: List<DrawerItem>,
    selectedRoute: AppRoute?,
    themeMode: AppThemeMode,
    weightUnit: String,
    onThemeModeChange: (AppThemeMode) -> Unit,
    onWeightUnitChange: (String) -> Unit,
    onItemClick: (DrawerItem) -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier
            .fillMaxHeight()
            .widthIn(max = 340.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = FitSpacing.lg)
                .padding(top = 28.dp, bottom = FitSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(FitSpacing.lg)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(FitSpacing.xs)) {
                Text(
                    text = "MENÚ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.textTertiary
                )
                Text(
                    text = "FitTrackPlus",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Navegacion secundaria y preferencias.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            FitTrackSectionLabel(label = "General")
            Column(verticalArrangement = Arrangement.spacedBy(FitSpacing.sm)) {
                drawerItems.forEach { item ->
                    DrawerActionRow(
                        title = item.title,
                        supporting = when {
                            item.route == AppRoute.Settings -> "Preferencias y datos de la aplicacion"
                            item.title == "Widget & atajos" -> "Visible ahora, implementacion futura"
                            item.title == "Exportar datos" -> "Guarda una copia JSON de tus rutinas e historial"
                            else -> null
                        },
                        icon = item.icon(),
                        selected = item.route != null && item.route == selectedRoute,
                        future = item.isFuture,
                        onClick = { onItemClick(item) }
                    )
                }
            }

            FitTrackSectionLabel(label = "Tema")
            FitTrackThemeModeSelector(
                selected = themeMode,
                onSelect = onThemeModeChange,
                showRadio = false
            )

            FitTrackSectionLabel(label = "Unidad")
            FitTrackSegmentedSelector(
                options = listOf("kg", "lb"),
                selectedIndex = if (weightUnit == "lb") 1 else 0,
                onSelect = { index -> onWeightUnitChange(if (index == 0) "kg" else "lb") }
            )

            Spacer(modifier = Modifier.weight(1f))
            AppVersionFooter()
        }
    }
}

@Composable
private fun AppVersionFooter() {
    Column(verticalArrangement = Arrangement.spacedBy(FitSpacing.xs)) {
        Text(
            text = "Versión instalada",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.textTertiary
        )
        Text(
            text = AppVersion.displayName,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DrawerActionRow(
    title: String,
    supporting: String?,
    icon: ImageVector,
    selected: Boolean,
    future: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(
                if (selected) MaterialTheme.colorScheme.primarySoft else MaterialTheme.colorScheme.surface
            )
            .clickable(
                role = Role.Button,
                onClickLabel = "Abrir $title",
                onClick = onClick
            )
            .padding(FitSpacing.lg),
        horizontalArrangement = Arrangement.spacedBy(FitSpacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(
                    if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(FitSpacing.xs)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(FitSpacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                if (future) {
                    FitTrackBadge(
                        label = "Próximamente",
                        tone = FitTrackBadgeTone.Neutral
                    )
                }
            }
            if (supporting != null) {
                Text(
                    text = supporting,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ShellMenuButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    Surface(
        modifier = modifier
            .padding(top = topInset + FitSpacing.sm, end = FitSpacing.md)
            .size(40.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable(
                role = Role.Button,
                onClickLabel = "Abrir menú",
                onClick = onClick
            ),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = FitElevation.menuButton,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.borderLight)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Filled.Menu,
                contentDescription = "Abrir menú",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/** Dispatches the drawer entries that do not navigate anywhere. */
private fun AppShellViewModel.handleNonNavigationDrawerItem(item: DrawerItem) {
    when (item.kind) {
        DrawerItemKind.FutureAction -> showFutureActionMessage(item.title)
        DrawerItemKind.InfoAction -> showMessage(item.message ?: item.title)
        DrawerItemKind.Action -> if (item.title == EXPORT_DATA_ITEM_TITLE) requestDataExport()
        DrawerItemKind.Navigation -> Unit
    }
}

private const val EXPORT_DATA_ITEM_TITLE = "Exportar datos"

private fun handleDrawerItemClick(
    item: DrawerItem,
    onNavigateToSecondary: (AppRoute) -> Unit,
    onRequestNavigation: (AppRoute) -> Boolean,
    onNonNavigationItem: (DrawerItem) -> Unit,
    drawerScope: CoroutineScope,
    closeDrawer: suspend () -> Unit
) {
    drawerScope.launch {
        closeDrawer()
        val route = item.route
        if (item.kind == DrawerItemKind.Navigation && route != null) {
            if (!onRequestNavigation(route)) {
                onNavigateToSecondary(route)
            }
        } else {
            onNonNavigationItem(item)
        }
    }
}

private fun AppRoute.bottomBarIcon(): ImageVector {
    return when (this) {
        AppRoute.Home -> Icons.Filled.Home
        AppRoute.Routines -> Icons.AutoMirrored.Filled.List
        AppRoute.Workout -> Icons.Filled.FitnessCenter
        AppRoute.History -> Icons.Filled.History
        AppRoute.Stats -> Icons.Filled.BarChart
        AppRoute.Settings -> Icons.Filled.Settings
    }
}

private fun DrawerItem.icon(): ImageVector {
    return when {
        route == AppRoute.Settings -> Icons.Filled.Settings
        title == "Widget & atajos" -> Icons.Filled.Widgets
        title == "Exportar datos" -> Icons.Filled.Download
        else -> Icons.Filled.Settings
    }
}
