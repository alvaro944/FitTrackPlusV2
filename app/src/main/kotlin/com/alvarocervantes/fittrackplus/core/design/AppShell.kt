package com.alvarocervantes.fittrackplus.core.design

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alvarocervantes.fittrackplus.core.navigation.AppRoute
import com.alvarocervantes.fittrackplus.core.navigation.AppShellViewModel
import com.alvarocervantes.fittrackplus.core.navigation.DrawerItem
import com.alvarocervantes.fittrackplus.core.navigation.DrawerItemKind
import com.alvarocervantes.fittrackplus.core.navigation.NavigationRequestKind
import com.alvarocervantes.fittrackplus.core.navigation.ShellBottomDestination
import com.alvarocervantes.fittrackplus.core.navigation.shellBottomDestinations
import com.alvarocervantes.fittrackplus.core.navigation.shellDrawerItems
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

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
    val bottomDestinations = remember { shellBottomDestinations() }
    val drawerItems = remember { shellDrawerItems() }

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
                        onFutureAction = viewModel::showFutureActionMessage,
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
                                val intercepted = viewModel.requestNavigation(
                                    currentRoute = currentRoute,
                                    targetRoute = targetRoute,
                                    kind = NavigationRequestKind.TopLevel
                                )
                                if (!intercepted) {
                                    onNavigateToTopLevel(targetRoute)
                                }
                            }
                        )
                    }
                }
            ) { innerPadding ->
                content(innerPadding)
            }

            if (currentRoute != AppRoute.Settings) {
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
                            item.title == "Exportar datos" -> "Visible ahora, implementacion futura"
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
            ThemeModeInlineSelector(
                selectedMode = themeMode,
                onSelectMode = onThemeModeChange
            )

            FitTrackSectionLabel(label = "Unidad")
            WeightUnitInlineSelector(
                selectedUnit = weightUnit,
                onSelectUnit = onWeightUnitChange
            )
        }
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
            .clickable(onClick = onClick)
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
                    else MaterialTheme.colorScheme.surfaceAlt
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
private fun ThemeModeInlineSelector(
    selectedMode: AppThemeMode,
    onSelectMode: (AppThemeMode) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(FitSpacing.sm)
    ) {
        AppThemeMode.entries.forEach { mode ->
            val icon = when (mode) {
                AppThemeMode.System -> Icons.Filled.Smartphone
                AppThemeMode.Light -> Icons.Filled.LightMode
                AppThemeMode.Dark -> Icons.Filled.DarkMode
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(MaterialTheme.shapes.large)
                    .background(
                        if (selectedMode == mode) MaterialTheme.colorScheme.primarySoft
                        else MaterialTheme.colorScheme.surfaceAlt
                    )
                    .clickable { onSelectMode(mode) }
                    .padding(FitSpacing.md),
                verticalArrangement = Arrangement.spacedBy(FitSpacing.sm),
                horizontalAlignment = Alignment.Start
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selectedMode == mode) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = mode.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selectedMode == mode) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun WeightUnitInlineSelector(
    selectedUnit: String,
    onSelectUnit: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceAlt)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        listOf("kg", "lb").forEach { unit ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(MaterialTheme.shapes.small)
                    .background(
                        if (selectedUnit == unit) MaterialTheme.colorScheme.surface
                        else MaterialTheme.colorScheme.surfaceAlt
                    )
                    .clickable { onSelectUnit(unit) }
                    .padding(vertical = FitSpacing.sm),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selectedUnit == unit) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
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
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
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

private fun handleDrawerItemClick(
    item: DrawerItem,
    onNavigateToSecondary: (AppRoute) -> Unit,
    onRequestNavigation: (AppRoute) -> Boolean,
    onFutureAction: (String) -> Unit,
    drawerScope: CoroutineScope,
    closeDrawer: suspend () -> Unit
) {
    drawerScope.launch {
        closeDrawer()
        when (item.kind) {
            DrawerItemKind.Navigation -> item.route?.let { route ->
                if (!onRequestNavigation(route)) {
                    onNavigateToSecondary(route)
                }
            }
            DrawerItemKind.FutureAction -> onFutureAction(item.title)
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
