package com.alvarocervantes.fittrackplus.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
