package com.alvarocervantes.fittrackplus.grit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alvarocervantes.fittrackplus.core.app.AppVersion
import com.alvarocervantes.fittrackplus.core.design.AppDesignStyle
import com.alvarocervantes.fittrackplus.core.navigation.AppRoute
import com.alvarocervantes.fittrackplus.core.navigation.AppShellViewModel
import com.alvarocervantes.fittrackplus.core.navigation.NavigationRequestKind
import com.alvarocervantes.fittrackplus.grit.components.GritSectionLabel
import com.alvarocervantes.fittrackplus.grit.theme.GritColors
import com.alvarocervantes.fittrackplus.grit.theme.GritShapes
import com.alvarocervantes.fittrackplus.grit.theme.GritType
import kotlinx.coroutines.launch

private data class GritBottomDestination(
    val route: AppRoute,
    val label: String,
    val icon: ImageVector
)

private fun gritBottomDestinations(): List<GritBottomDestination> = listOf(
    GritBottomDestination(AppRoute.Home, "Inicio", Icons.Filled.Home),
    GritBottomDestination(AppRoute.Routines, "Rutinas", Icons.AutoMirrored.Filled.List),
    GritBottomDestination(AppRoute.Workout, "Entrenar", Icons.Filled.PlayArrow),
    GritBottomDestination(AppRoute.History, "Historial", Icons.Filled.History),
    GritBottomDestination(AppRoute.Stats, "Datos", Icons.Filled.BarChart)
)

@Composable
fun GritAppShell(
    currentRoute: AppRoute?,
    onNavigateToTopLevel: (AppRoute) -> Unit,
    onNavigateToSecondary: (AppRoute) -> Unit,
    content: @Composable (PaddingValues) -> Unit,
    viewModel: AppShellViewModel = hiltViewModel()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val designStyle by viewModel.designStyle.collectAsStateWithLifecycle()
    val weightUnit by viewModel.weightUnit.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val destinations = remember { gritBottomDestinations() }

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
            GritDrawerContent(
                designStyle = designStyle,
                weightUnit = weightUnit,
                onDesignStyleChange = viewModel::setDesignStyle,
                onWeightUnitChange = viewModel::setWeightUnit,
                onOpenSettings = {
                    coroutineScope.launch {
                        drawerState.close()
                        val intercepted = viewModel.requestNavigation(
                            currentRoute = currentRoute,
                            targetRoute = AppRoute.Settings,
                            kind = NavigationRequestKind.Secondary
                        )
                        if (!intercepted) {
                            onNavigateToSecondary(AppRoute.Settings)
                        }
                    }
                }
            )
        }
    ) {
        Scaffold(
            containerColor = GritColors.Background,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                if (currentRoute != AppRoute.Settings) {
                    GritTopBar(
                        onLogoClick = { onNavigateToTopLevel(AppRoute.Home) },
                        onMenuClick = {
                            coroutineScope.launch { drawerState.open() }
                        }
                    )
                }
            },
            bottomBar = {
                if (currentRoute != AppRoute.Settings) {
                    GritBottomBar(
                        currentRoute = currentRoute,
                        destinations = destinations,
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
    }
}

@Composable
private fun GritTopBar(
    onLogoClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    Column(modifier = Modifier.background(GritColors.Background)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.clickable(onClick = onLogoClick),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Bolt,
                    contentDescription = null,
                    tint = GritColors.Lime,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "FitTrackPlus",
                    style = GritType.cardTitle,
                    color = GritColors.Lime
                )
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .border(1.dp, GritColors.Border, CircleShape)
                    .background(GritColors.Neutral900)
                    .clickable(onClick = onMenuClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "Abrir menú",
                    tint = GritColors.TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(GritColors.Border)
        )
    }
}

@Composable
private fun GritBottomBar(
    currentRoute: AppRoute?,
    destinations: List<GritBottomDestination>,
    onNavigate: (AppRoute) -> Unit
) {
    Surface(
        color = GritColors.Background,
        border = BorderStroke(1.dp, GritColors.Border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 24.dp, start = 8.dp, end = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.Bottom
        ) {
            destinations.forEach { destination ->
                val selected = currentRoute == destination.route
                if (destination.route == AppRoute.Workout) {
                    GritCenterAction(
                        destination = destination,
                        selected = selected,
                        onClick = { onNavigate(destination.route) }
                    )
                } else {
                    GritBottomItem(
                        destination = destination,
                        selected = selected,
                        onClick = { onNavigate(destination.route) }
                    )
                }
            }
        }
    }
}

@Composable
private fun GritBottomItem(
    destination: GritBottomDestination,
    selected: Boolean,
    onClick: () -> Unit
) {
    val tint = if (selected) GritColors.Lime else GritColors.TextSecondary
    Column(
        modifier = Modifier
            .clip(GritShapes.small)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = destination.icon,
            contentDescription = destination.label,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = destination.label.uppercase(),
            style = GritType.monoLabelSmall,
            color = tint
        )
    }
}

@Composable
private fun GritCenterAction(
    destination: GritBottomDestination,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(GritShapes.small)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .offset(y = (-16).dp)
                .size(48.dp)
                .clip(CircleShape)
                .background(GritColors.Lime)
                .border(1.dp, GritColors.Border, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = destination.icon,
                contentDescription = destination.label,
                tint = GritColors.Black,
                modifier = Modifier.size(28.dp)
            )
        }
        Text(
            text = destination.label.uppercase(),
            style = GritType.monoLabelSmall,
            color = if (selected) GritColors.Lime else GritColors.TextSecondary,
            modifier = Modifier.offset(y = (-12).dp)
        )
    }
}

@Composable
private fun GritDrawerContent(
    designStyle: AppDesignStyle,
    weightUnit: String,
    onDesignStyleChange: (AppDesignStyle) -> Unit,
    onWeightUnitChange: (String) -> Unit,
    onOpenSettings: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier
            .fillMaxHeight()
            .widthIn(max = 320.dp),
        drawerContainerColor = GritColors.Background,
        drawerContentColor = GritColors.TextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(top = 32.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                GritSectionLabel(text = "Menú")
                Text(text = "FitTrackPlus", style = GritType.screenTitle, color = GritColors.Lime)
            }

            GritSectionLabel(text = "Diseño")
            GritDrawerSelector(
                options = AppDesignStyle.entries.map { style -> style.label },
                selectedIndex = AppDesignStyle.entries.indexOf(designStyle),
                onSelect = { index -> onDesignStyleChange(AppDesignStyle.entries[index]) }
            )
            Text(
                text = "El diseño clásico incluye tema claro y oscuro.",
                style = GritType.monoLabelSmall,
                color = GritColors.TextFaint
            )

            GritSectionLabel(text = "Unidad")
            GritDrawerSelector(
                options = listOf("kg", "lb"),
                selectedIndex = if (weightUnit == "lb") 1 else 0,
                onSelect = { index -> onWeightUnitChange(if (index == 0) "kg" else "lb") }
            )

            GritSectionLabel(text = "General")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(GritShapes.small)
                    .border(1.dp, GritColors.Border, GritShapes.small)
                    .background(GritColors.Surface)
                    .clickable(onClick = onOpenSettings)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = null,
                    tint = GritColors.Lime,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "AJUSTES AVANZADOS",
                    style = GritType.monoStrong
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                GritSectionLabel(text = "Versión instalada")
                Text(text = AppVersion.displayName, style = GritType.monoBody)
            }
        }
    }
}

@Composable
private fun GritDrawerSelector(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(GritShapes.small)
            .border(1.dp, GritColors.Border, GritShapes.small)
            .background(GritColors.Surface)
    ) {
        options.forEachIndexed { index, option ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(if (selected) GritColors.Lime else GritColors.Surface)
                    .clickable { onSelect(index) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option.uppercase(),
                    style = GritType.monoStrong,
                    color = if (selected) GritColors.Black else GritColors.TextSecondary
                )
            }
        }
    }
}
