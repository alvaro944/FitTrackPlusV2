package com.alvarocervantes.fittrackplus.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alvarocervantes.fittrackplus.BuildConfig
import com.alvarocervantes.fittrackplus.core.design.AppThemeMode
import com.alvarocervantes.fittrackplus.core.design.FitSpacing
import com.alvarocervantes.fittrackplus.core.design.FitTrackCard
import com.alvarocervantes.fittrackplus.core.design.FitTrackSectionLabel
import com.alvarocervantes.fittrackplus.core.design.FitTrackScreenHeader
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
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(FitSpacing.sm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = weightUnit == "kg",
                                onClick = { viewModel.setWeightUnit("kg") },
                                label = { Text("kg") }
                            )
                            FilterChip(
                                selected = weightUnit == "lb",
                                onClick = { viewModel.setWeightUnit("lb") },
                                label = { Text("lb") }
                            )
                        }
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
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(FitSpacing.sm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppThemeMode.entries.forEach { mode ->
                                FilterChip(
                                    selected = themeMode == mode,
                                    onClick = { viewModel.setThemeMode(mode) },
                                    label = { Text(mode.label) }
                                )
                            }
                        }
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
                                text = "Rellena la base de datos con una rutina PPL y sesiones de ejemplo. " +
                                    "Elimina todos los datos actuales.",
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
