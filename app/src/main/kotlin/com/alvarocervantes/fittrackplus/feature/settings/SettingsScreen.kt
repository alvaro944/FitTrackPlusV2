package com.alvarocervantes.fittrackplus.feature.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alvarocervantes.fittrackplus.BuildConfig
import com.alvarocervantes.fittrackplus.core.design.FitSpacing
import com.alvarocervantes.fittrackplus.core.design.FitTrackCard
import com.alvarocervantes.fittrackplus.core.design.FitTrackConfirmDialog
import com.alvarocervantes.fittrackplus.core.design.FitTrackOutlinedButton
import com.alvarocervantes.fittrackplus.core.design.FitTrackPrimaryButton
import com.alvarocervantes.fittrackplus.core.design.FitTrackSectionLabel
import com.alvarocervantes.fittrackplus.core.design.components.FitTrackSegmentedSelector
import com.alvarocervantes.fittrackplus.core.design.components.FitTrackStepper
import com.alvarocervantes.fittrackplus.core.design.components.FitTrackThemeModeSelector
import com.alvarocervantes.fittrackplus.core.design.textTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val weightUnit by viewModel.weightUnit.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val healthConnectConnected by viewModel.healthConnectConnected.collectAsStateWithLifecycle()
    val dailyStepGoal by viewModel.dailyStepGoal.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDemoDataDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = viewModel.permissionsContract(),
        onResult = viewModel::onPermissionsResult
    )

    if (showDemoDataDialog) {
        FitTrackConfirmDialog(
            title = "Recargar datos demo",
            text = "Esto eliminara todas tus rutinas y sesiones actuales " +
                "y las sustituira por datos de demo. Esta accion no se puede deshacer.",
            confirmLabel = "Recargar",
            dismissLabel = "Cancelar",
            onConfirm = {
                showDemoDataDialog = false
                viewModel.reloadDemoData()
            },
            onDismiss = { showDemoDataDialog = false },
            destructive = true
        )
    }

    LaunchedEffect(message) {
        val currentMessage = message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(currentMessage)
        viewModel.clearMessage()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Ajustes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = FitSpacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(FitSpacing.card)
        ) {

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
                        FitTrackSegmentedSelector(
                            options = listOf("kg", "lb"),
                            selectedIndex = if (weightUnit == "lb") 1 else 0,
                            onSelect = { index -> viewModel.setWeightUnit(if (index == 0) "kg" else "lb") }
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
                        FitTrackThemeModeSelector(
                            selected = themeMode,
                            onSelect = viewModel::setThemeMode
                        )
                    }
                }
            }

            item {
                FitTrackSectionLabel(label = "Salud y actividad")
            }

            item {
                FitTrackCard {
                    Column(verticalArrangement = Arrangement.spacedBy(FitSpacing.md)) {
                        Text(
                            text = "Conecta con Health Connect para leer tus pasos " +
                                "diarios desde apps como Samsung Health, Garmin o Google Fit.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        when {
                            !viewModel.isHealthConnectAvailable && !viewModel.healthConnectNeedsInstallation -> {
                                Text(
                                    text = "Health Connect no esta disponible en este dispositivo.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            viewModel.healthConnectNeedsInstallation -> {
                                Text(
                                    text = "Instala Health Connect desde Play Store para conectar tu app de actividad.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                FitTrackPrimaryButton(
                                    label = "Instalar Health Connect",
                                    onClick = {
                                        context.startActivity(
                                            Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse("market://details?id=com.google.android.apps.healthdata")
                                            ).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            healthConnectConnected -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Objetivo de pasos diarios",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    FitTrackStepper(
                                        value = "%,d".format(dailyStepGoal),
                                        onDecrement = {
                                            if (dailyStepGoal > 1_000) {
                                                viewModel.setDailyStepGoal(dailyStepGoal - 1_000)
                                            }
                                        },
                                        onIncrement = {
                                            viewModel.setDailyStepGoal(dailyStepGoal + 1_000)
                                        },
                                        compact = true,
                                        decrementEnabled = dailyStepGoal > 1_000
                                    )
                                }
                                FitTrackOutlinedButton(
                                    label = "Desconectar Health Connect",
                                    onClick = viewModel::disconnectHealthConnect,
                                    modifier = Modifier.fillMaxWidth(),
                                    destructive = true
                                )
                            }

                            else -> {
                                FitTrackPrimaryButton(
                                    label = "Conectar con Health Connect",
                                    onClick = {
                                        permissionsLauncher.launch(viewModel.requiredPermissions)
                                    },
                                    modifier = Modifier.fillMaxWidth()
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
                                text = "Limpia la base de datos y deja activa tu Rutina Alvaro, " +
                                    "junto con una demo PPL de apoyo con sesiones de ejemplo.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            FitTrackPrimaryButton(
                                label = "Cargar datos demo",
                                onClick = { showDemoDataDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            )
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
