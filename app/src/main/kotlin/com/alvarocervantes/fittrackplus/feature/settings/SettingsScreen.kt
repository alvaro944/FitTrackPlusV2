package com.alvarocervantes.fittrackplus.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alvarocervantes.fittrackplus.BuildConfig
import com.alvarocervantes.fittrackplus.core.design.AppThemeMode
import com.alvarocervantes.fittrackplus.core.design.FitSpacing
import com.alvarocervantes.fittrackplus.core.design.FitTrackCard
import com.alvarocervantes.fittrackplus.core.design.FitTrackSectionLabel
import com.alvarocervantes.fittrackplus.core.design.FitTrackScreenHeader
import com.alvarocervantes.fittrackplus.core.design.primarySoft
import com.alvarocervantes.fittrackplus.core.design.surfaceAlt
import com.alvarocervantes.fittrackplus.core.design.textTertiary

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val weightUnit by viewModel.weightUnit.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDemoDataDialog by remember { mutableStateOf(false) }

    if (showDemoDataDialog) {
        AlertDialog(
            onDismissRequest = { showDemoDataDialog = false },
            title = { Text("Recargar datos demo") },
            text = {
                Text(
                    "Esto eliminara todas tus rutinas y sesiones actuales " +
                        "y las sustituira por datos de demo. Esta accion no se puede deshacer."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDemoDataDialog = false
                        viewModel.reloadDemoData()
                    }
                ) {
                    Text("Recargar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDemoDataDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    LaunchedEffect(message) {
        val currentMessage = message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(currentMessage)
        viewModel.clearMessage()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = FitSpacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(FitSpacing.card)
        ) {
            item {
                FitTrackScreenHeader(
                    title = "Ajustes",
                    subtitle = "Preferencias de la aplicacion.",
                    modifier = Modifier.padding(top = FitSpacing.screenTop)
                )
            }

            item {
                FitTrackSectionLabel(label = "Unidad de peso")
            }

            item {
                FitTrackCard {
                    Column(verticalArrangement = Arrangement.spacedBy(FitSpacing.sm)) {
                        Text(
                            text = "Unidad utilizada para registrar el peso en cada serie.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        UnitSelector(
                            selectedUnit = weightUnit,
                            onSelectUnit = viewModel::setWeightUnit
                        )
                    }
                }
            }

            item {
                FitTrackSectionLabel(label = "Tema")
            }

            item {
                FitTrackCard {
                    Column(verticalArrangement = Arrangement.spacedBy(FitSpacing.sm)) {
                        Text(
                            text = "Apariencia utilizada por la aplicacion.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        ThemeModeSelector(
                            selectedMode = themeMode,
                            onSelectMode = viewModel::setThemeMode
                        )
                    }
                }
            }

            if (viewModel.isDebugBuild) {
                item {
                    FitTrackSectionLabel(label = "Datos de demostracion")
                }
                item {
                    FitTrackCard {
                        Column(verticalArrangement = Arrangement.spacedBy(FitSpacing.sm)) {
                            Text(
                                text = "Limpia la base de datos y deja activa tu Rutina Álvaro, " +
                                    "junto con una demo PPL de apoyo con sesiones de ejemplo.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(
                                onClick = { showDemoDataDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Cargar datos demo")
                            }
                        }
                    }
                }
            }

            item {
                FitTrackSectionLabel(label = "Acerca de")
            }

            item {
                FitTrackCard {
                    Column(verticalArrangement = Arrangement.spacedBy(FitSpacing.sm)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "FitTrackPlus",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "v${BuildConfig.VERSION_NAME}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.textTertiary
                            )
                        }
                        Text(
                            text = "App de entrenamiento local-first. Rutinas, registro de sesiones e historial sin depender de la nube.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UnitSelector(
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
        UnitSegment(
            label = "kg",
            selected = selectedUnit == "kg",
            onClick = { onSelectUnit("kg") },
            modifier = Modifier.weight(1f)
        )
        UnitSegment(
            label = "lb",
            selected = selectedUnit == "lb",
            onClick = { onSelectUnit("lb") },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun UnitSegment(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.surface
                } else {
                    MaterialTheme.colorScheme.surfaceAlt
                }
            )
            .clickable(onClick = onClick)
            .padding(vertical = FitSpacing.sm),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@Composable
private fun ThemeModeSelector(
    selectedMode: AppThemeMode,
    onSelectMode: (AppThemeMode) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(FitSpacing.sm)
    ) {
        AppThemeMode.entries.forEach { mode ->
            ThemeModeOption(
                mode = mode,
                selected = selectedMode == mode,
                onClick = { onSelectMode(mode) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ThemeModeOption(
    mode: AppThemeMode,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.primarySoft
                } else {
                    MaterialTheme.colorScheme.surfaceAlt
                }
            )
            .border(
                width = 1.dp,
                color = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline
                },
                shape = MaterialTheme.shapes.large
            )
            .clickable(onClick = onClick)
            .padding(FitSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(FitSpacing.sm),
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(
                    if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onPrimary)
                )
            }
        }
        Text(
            text = mode.label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
        Text(
            text = when (mode) {
                AppThemeMode.System -> "Auto"
                AppThemeMode.Light -> "Claro"
                AppThemeMode.Dark -> "Oscuro"
            }.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.textTertiary
        )
    }
}
