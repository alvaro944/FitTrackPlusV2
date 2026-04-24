package com.alvarocervantes.fittrackplus.feature.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alvarocervantes.fittrackplus.core.design.FitSpacing
import com.alvarocervantes.fittrackplus.core.design.FitTrackBadge
import com.alvarocervantes.fittrackplus.core.design.FitTrackBadgeTone
import com.alvarocervantes.fittrackplus.core.design.FitTrackCard
import com.alvarocervantes.fittrackplus.core.design.FitTrackEmptyState
import com.alvarocervantes.fittrackplus.core.design.FitTrackLoadingCard
import com.alvarocervantes.fittrackplus.core.design.FitTrackMetric
import com.alvarocervantes.fittrackplus.core.design.FitTrackMetricAccent
import com.alvarocervantes.fittrackplus.core.design.FitTrackProgressBar
import com.alvarocervantes.fittrackplus.core.design.FitTrackScreenHeader
import com.alvarocervantes.fittrackplus.core.design.primaryDark
import com.alvarocervantes.fittrackplus.core.design.primarySoft
import com.alvarocervantes.fittrackplus.core.design.surfaceAlt
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WorkoutScreen(
    onGoToRoutines: () -> Unit,
    viewModel: WorkoutViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showFinishConfirmation by remember { mutableStateOf(false) }

    state.message?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }

    if (showFinishConfirmation) {
        AlertDialog(
            onDismissRequest = { showFinishConfirmation = false },
            title = { Text("Finalizar entrenamiento") },
            text = {
                Text(
                    text = "Se guardara la sesion en el historial con las series registradas hasta ahora."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showFinishConfirmation = false
                        viewModel.finishWorkout()
                    }
                ) {
                    Text("Finalizar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinishConfirmation = false }) {
                    Text("Seguir entrenando")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        WorkoutContent(
            state = state,
            contentPadding = padding,
            onRefresh = viewModel::refresh,
            onStartWorkout = viewModel::startWorkout,
            onFinishWorkout = { showFinishConfirmation = true },
            onSetWeightChange = viewModel::updateSetWeight,
            onSetRepsChange = viewModel::updateSetReps,
            onGoToRoutines = onGoToRoutines
        )
    }
}

@Composable
private fun WorkoutContent(
    state: WorkoutUiState,
    contentPadding: PaddingValues,
    onRefresh: () -> Unit,
    onStartWorkout: () -> Unit,
    onFinishWorkout: () -> Unit,
    onSetWeightChange: (Long, String) -> Unit,
    onSetRepsChange: (Long, String) -> Unit,
    onGoToRoutines: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(
            start = FitSpacing.screenHorizontal,
            top = FitSpacing.screenTop,
            end = FitSpacing.screenHorizontal,
            bottom = FitSpacing.screenBottom
        ),
        verticalArrangement = Arrangement.spacedBy(FitSpacing.section)
    ) {
        item {
            FitTrackScreenHeader(
                title = "Entrenar",
                subtitle = when {
                    state.activeSession != null -> "Sesion en curso"
                    state.preview != null -> "Siguiente entrenamiento listo"
                    state.activeRoutineId == null -> "Necesitas una rutina activa"
                    else -> "Registro de entrenamiento"
                },
                trailing = {
                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Actualizar entrenamiento"
                        )
                    }
                }
            )
        }

        when {
            state.isLoading -> {
                item { FitTrackLoadingCard(text = "Preparando entrenamiento...") }
            }

            state.activeSession != null -> {
                item {
                    ActiveSessionSummary(
                        session = state.activeSession,
                        isFinishing = state.isFinishing,
                        onFinishWorkout = onFinishWorkout
                    )
                }
                items(
                    items = state.activeSession.exercises,
                    key = { exercise -> exercise.id }
                ) { exercise ->
                    WorkoutExerciseCard(
                        exercise = exercise,
                        onSetWeightChange = onSetWeightChange,
                        onSetRepsChange = onSetRepsChange
                    )
                }
            }

            state.activeRoutineId == null -> {
                item {
                    FitTrackEmptyState(
                        icon = Icons.AutoMirrored.Filled.List,
                        title = "No hay rutina activa",
                        message = "Selecciona una rutina en Rutinas para preparar el siguiente entrenamiento.",
                        supporting = "Primero crea o elige una rutina y marcala como activa."
                    ) {
                        Button(onClick = onGoToRoutines) {
                            Text("Ir a Rutinas")
                        }
                    }
                }
            }

            state.preview != null -> {
                item {
                    WorkoutPreviewCard(
                        preview = state.preview,
                        isStarting = state.isStarting,
                        onStartWorkout = onStartWorkout
                    )
                }
            }

            else -> {
                item {
                    FitTrackEmptyState(
                        icon = Icons.Filled.FitnessCenter,
                        title = "No se encontro el siguiente entrenamiento",
                        message = "Puede que la rutina activa no tenga dias o ejercicios disponibles.",
                        supporting = "Revisa la rutina actual antes de volver a intentarlo."
                    ) {
                        FilledTonalButton(onClick = onRefresh) {
                            Text("Revisar de nuevo")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutPreviewCard(
    preview: WorkoutPreviewUiState,
    isStarting: Boolean,
    onStartWorkout: () -> Unit
) {
    androidx.compose.material3.Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryDark),
        border = null
    ) {
        Column(
            modifier = Modifier.padding(FitSpacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(FitSpacing.mdLg)
        ) {
            FitTrackBadge(
                label = "PROXIMO ENTRENAMIENTO",
                tone = FitTrackBadgeTone.Active
            )
            Text(
                text = preview.routineName,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = preview.dayName,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White.copy(alpha = 0.92f)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(FitSpacing.smMd)
            ) {
                HeroTag(text = "Semana ${preview.weekNumber}")
                HeroTag(text = "${preview.exerciseCount} ejercicios")
            }
            Button(
                onClick = onStartWorkout,
                enabled = !isStarting
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = if (isStarting) "Iniciando entrenamiento" else "Iniciar entrenamiento",
                    modifier = Modifier.padding(start = FitSpacing.sm)
                )
            }
        }
    }
}

@Composable
private fun HeroTag(text: String) {
    Box(
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.10f), MaterialTheme.shapes.medium)
            .padding(horizontal = FitSpacing.smMd, vertical = FitSpacing.tiny)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.78f)
        )
    }
}

@Composable
private fun ActiveSessionSummary(
    session: ActiveWorkoutSessionUiState,
    isFinishing: Boolean,
    onFinishWorkout: () -> Unit
) {
    FitTrackCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(FitSpacing.tiny)
            ) {
                FitTrackBadge(
                    label = "SESION ACTIVA",
                    tone = FitTrackBadgeTone.Primary
                )
                Text(
                    text = session.routineName,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${session.dayName} · iniciada ${formatStartedAt(session.startedAt)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FitTrackBadge(
                label = "Semana ${session.weekNumber}",
                tone = FitTrackBadgeTone.Neutral
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(FitSpacing.xl)
        ) {
            FitTrackMetric(
                value = session.completedSetCount.toString(),
                label = "series hechas",
                accent = FitTrackMetricAccent.Primary,
                compact = true
            )
            FitTrackMetric(
                value = session.totalSetCount.toString(),
                label = "series totales",
                compact = true
            )
        }

        FitTrackProgressBar(
            progress = if (session.totalSetCount == 0) {
                0f
            } else {
                session.completedSetCount.toFloat() / session.totalSetCount.toFloat()
            },
            contentDescription = "Progreso de series completadas del entrenamiento actual"
        )

        Button(
            onClick = onFinishWorkout,
            enabled = !isFinishing,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = if (isFinishing) "Finalizando entrenamiento" else "Finalizar entrenamiento",
                modifier = Modifier.padding(start = FitSpacing.sm)
            )
        }
    }
}

@Composable
private fun WorkoutExerciseCard(
    exercise: WorkoutExerciseUiState,
    onSetWeightChange: (Long, String) -> Unit,
    onSetRepsChange: (Long, String) -> Unit
) {
    FitTrackCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(FitSpacing.md)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(FitSpacing.xs)
            ) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Objetivo: ${exercise.targetRepsText} reps",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            exercise.sets.forEach { set ->
                WorkoutSetRow(
                    set = set,
                    onSetWeightChange = onSetWeightChange,
                    onSetRepsChange = onSetRepsChange
                )
            }
        }
    }
}

@Composable
private fun WorkoutSetRow(
    set: WorkoutSetUiState,
    onSetWeightChange: (Long, String) -> Unit,
    onSetRepsChange: (Long, String) -> Unit
) {
    val hasInput = set.weightText.isNotBlank() || set.repsText.isNotBlank()
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceAlt, MaterialTheme.shapes.large)
            .padding(FitSpacing.smMd),
        horizontalArrangement = Arrangement.spacedBy(FitSpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = if (hasInput) MaterialTheme.colorScheme.primarySoft else MaterialTheme.colorScheme.surface,
                    shape = CircleShape
                )
                .semantics {
                    contentDescription = if (hasInput) {
                        "Serie ${set.setNumber} con datos registrados"
                    } else {
                        "Serie ${set.setNumber} pendiente"
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (hasInput) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Text(
                    text = set.setNumber.toString(),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            OutlinedTextField(
                value = set.weightText,
                onValueChange = { value ->
                    if (!hasInput && value.isNotBlank()) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    onSetWeightChange(set.id, value)
                },
                label = { Text("Kg") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
            )
            if (set.previousWeight != null) {
                Text(
                    text = "Ultima vez: ${set.previousWeight} kg",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                )
            }
        }
        OutlinedTextField(
            value = set.repsText,
            onValueChange = { value ->
                if (!hasInput && value.isNotBlank()) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                onSetRepsChange(set.id, value)
            },
            label = { Text("Reps") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 56.dp)
        )
    }
}

private fun formatStartedAt(timestamp: Long): String {
    return SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(timestamp))
}
