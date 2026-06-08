package com.alvarocervantes.fittrackplus.feature.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alvarocervantes.fittrackplus.core.design.FitSpacing
import com.alvarocervantes.fittrackplus.core.design.FitTrackBadge
import com.alvarocervantes.fittrackplus.core.design.FitTrackBadgeTone
import com.alvarocervantes.fittrackplus.core.design.accentWarm
import com.alvarocervantes.fittrackplus.core.design.components.ConfettiAnimation
import com.alvarocervantes.fittrackplus.domain.model.PrType
import com.alvarocervantes.fittrackplus.core.design.FitTrackCard
import com.alvarocervantes.fittrackplus.core.design.FitTrackEmptyState
import com.alvarocervantes.fittrackplus.core.design.FitTrackMetric
import com.alvarocervantes.fittrackplus.core.design.components.SkeletonBlock
import com.alvarocervantes.fittrackplus.core.design.components.SkeletonCard
import com.alvarocervantes.fittrackplus.core.design.components.SkeletonText
import com.alvarocervantes.fittrackplus.core.design.FitTrackMetricAccent
import com.alvarocervantes.fittrackplus.core.design.FitTrackProgressBar
import com.alvarocervantes.fittrackplus.core.design.FitTrackRadialTimer
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
    val haptic = LocalHapticFeedback.current

    state.message?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.prHapticEvent.collect {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            kotlinx.coroutines.delay(80)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    if (showFinishConfirmation) {
        Dialog(onDismissRequest = { showFinishConfirmation = false }) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(FitSpacing.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(FitSpacing.md)
                ) {
                    Text(
                        text = "Finalizar entrenamiento",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Se guardara la sesion en el historial con las series registradas hasta ahora.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showFinishConfirmation = false }) {
                            Text("Seguir entrenando")
                        }
                        TextButton(
                            onClick = {
                                showFinishConfirmation = false
                                viewModel.finishWorkout()
                            }
                        ) {
                            Text("Finalizar")
                        }
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                onStartRestTimer = viewModel::startRestTimer,
                onPauseRestTimer = viewModel::pauseRestTimer,
                onResumeRestTimer = viewModel::resumeRestTimer,
                onResetRestTimer = viewModel::resetRestTimer,
                onCancelRestTimer = viewModel::cancelRestTimer,
                onAutoStartRestTimerChange = viewModel::setAutoStartRestTimerEnabled,
                onGoToRoutines = onGoToRoutines
            )
        }

        if (state.celebration != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                ConfettiAnimation(
                    modifier = Modifier.fillMaxSize(),
                    onFinished = { viewModel.dismissCelebration() }
                )
                Box(
                    modifier = Modifier
                        .wrapContentSize()
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                            shape = MaterialTheme.shapes.extraLarge
                        )
                    .padding(horizontal = FitSpacing.xl, vertical = FitSpacing.md)
                ) {
                    Text(
                        text = "Nuevo PR",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.accentWarm
                    )
                }
            }
        }
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
    onStartRestTimer: (Int) -> Unit,
    onPauseRestTimer: () -> Unit,
    onResumeRestTimer: () -> Unit,
    onResetRestTimer: () -> Unit,
    onCancelRestTimer: () -> Unit,
    onAutoStartRestTimerChange: (Boolean) -> Unit,
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
                item { WorkoutLoadingSkeleton() }
            }

            state.activeSession != null -> {
                item {
                    ActiveSessionSummary(
                        session = state.activeSession,
                        isFinishing = state.isFinishing,
                        onFinishWorkout = onFinishWorkout
                    )
                }
                item {
                    RestTimerCard(
                        timer = state.restTimer,
                        onStartRestTimer = onStartRestTimer,
                        onPauseRestTimer = onPauseRestTimer,
                        onResumeRestTimer = onResumeRestTimer,
                        onResetRestTimer = onResetRestTimer,
                        onCancelRestTimer = onCancelRestTimer,
                        onAutoStartRestTimerChange = onAutoStartRestTimerChange
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
private fun RestTimerCard(
    timer: RestTimerUiState,
    onStartRestTimer: (Int) -> Unit,
    onPauseRestTimer: () -> Unit,
    onResumeRestTimer: () -> Unit,
    onResetRestTimer: () -> Unit,
    onCancelRestTimer: () -> Unit,
    onAutoStartRestTimerChange: (Boolean) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(timer.status) {
        if (timer.status == RestTimerStatus.Finished) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    FitTrackCard(modifier = Modifier.fillMaxWidth()) {
        RestTimerHeader(
            timer = timer,
            onAutoStartRestTimerChange = onAutoStartRestTimerChange
        )
        RestTimerRadialControls(
            timer = timer,
            onPauseRestTimer = onPauseRestTimer,
            onResumeRestTimer = onResumeRestTimer,
            onResetRestTimer = onResetRestTimer,
            onCancelRestTimer = onCancelRestTimer
        )
        RestTimerQuickDurations(onStartRestTimer = onStartRestTimer)
    }
}

@Composable
private fun RestTimerHeader(
    timer: RestTimerUiState,
    onAutoStartRestTimerChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(FitSpacing.tiny)
        ) {
            FitTrackBadge(label = "DESCANSO", tone = FitTrackBadgeTone.Neutral)
            Text(
                text = restTimerTitle(timer),
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = restTimerSupportText(timer.autoStartEnabled),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(FitSpacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Auto",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Switch(
                checked = timer.autoStartEnabled,
                onCheckedChange = onAutoStartRestTimerChange
            )
        }
    }
}

@Composable
private fun RestTimerRadialControls(
    timer: RestTimerUiState,
    onPauseRestTimer: () -> Unit,
    onResumeRestTimer: () -> Unit,
    onResetRestTimer: () -> Unit,
    onCancelRestTimer: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(FitSpacing.lg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FitTrackRadialTimer(
            remainingSeconds = timer.remainingSeconds,
            durationSeconds = timer.durationSeconds,
            label = restTimerRadialLabel(timer.status),
            isUrgent = isRestTimerUrgent(timer),
            contentDescription = "Tiempo restante del descanso"
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(FitSpacing.sm)
        ) {
            RestTimerPauseResumeButton(
                timer = timer,
                onPauseRestTimer = onPauseRestTimer,
                onResumeRestTimer = onResumeRestTimer
            )
            RestTimerActionButton(
                enabled = timer.durationSeconds > 0,
                icon = Icons.Filled.Refresh,
                label = "Reiniciar",
                onClick = onResetRestTimer
            )
            RestTimerActionButton(
                enabled = timer.isActive,
                icon = Icons.Filled.Close,
                label = "Cancelar",
                onClick = onCancelRestTimer
            )
        }
    }
}

@Composable
private fun RestTimerPauseResumeButton(
    timer: RestTimerUiState,
    onPauseRestTimer: () -> Unit,
    onResumeRestTimer: () -> Unit
) {
    val isRunning = timer.status == RestTimerStatus.Running
    RestTimerActionButton(
        enabled = isRunning || timer.status == RestTimerStatus.Paused,
        icon = if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
        label = if (isRunning) "Pausar" else "Reanudar",
        onClick = if (isRunning) onPauseRestTimer else onResumeRestTimer
    )
}

@Composable
private fun RestTimerQuickDurations(
    onStartRestTimer: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(FitSpacing.sm)
    ) {
        listOf(60, 90, 120).forEach { seconds ->
            FilledTonalButton(
                onClick = { onStartRestTimer(seconds) },
                modifier = Modifier.weight(1f)
            ) {
                Text("${seconds}s")
            }
        }
    }
}

@Composable
private fun RestTimerActionButton(
    enabled: Boolean,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = label,
            modifier = Modifier.padding(start = FitSpacing.sm)
        )
    }
}

private fun restTimerTitle(timer: RestTimerUiState): String {
    return when (timer.status) {
        RestTimerStatus.Finished -> "Descanso terminado"
        RestTimerStatus.Paused -> "Timer pausado"
        RestTimerStatus.Running -> formatRestTimer(timer.remainingSeconds)
        RestTimerStatus.Stopped -> "Timer listo"
    }
}

private fun restTimerSupportText(autoStartEnabled: Boolean): String {
    return if (autoStartEnabled) {
        "Auto al completar una serie"
    } else {
        "Inicia un descanso cuando lo necesites"
    }
}

private fun restTimerRadialLabel(status: RestTimerStatus): String {
    return when (status) {
        RestTimerStatus.Finished -> "fin"
        RestTimerStatus.Paused -> "pausa"
        RestTimerStatus.Running -> "rest"
        RestTimerStatus.Stopped -> "listo"
    }
}

private fun isRestTimerUrgent(timer: RestTimerUiState): Boolean {
    return timer.status == RestTimerStatus.Finished ||
        (timer.status == RestTimerStatus.Running && timer.remainingSeconds <= 10)
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
private fun WeightFieldColumn(
    setId: Long,
    weightText: String,
    previousWeight: String?,
    hasInput: Boolean,
    onSetWeightChange: (Long, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    Column(modifier = modifier) {
        OutlinedTextField(
            value = weightText,
            onValueChange = { value ->
                if (!hasInput && value.isNotBlank()) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onSetWeightChange(setId, value)
            },
            label = { Text("Kg") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .onFocusChanged { focusState ->
                    if (focusState.isFocused && weightText == "0") onSetWeightChange(setId, "")
                }
        )
        if (previousWeight != null) {
            Text(
                text = "Ultima vez: $previousWeight kg",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
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

    Column(modifier = Modifier.fillMaxWidth()) {
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
        WeightFieldColumn(
            setId = set.id,
            weightText = set.weightText,
            previousWeight = set.previousWeight,
            hasInput = hasInput,
            onSetWeightChange = onSetWeightChange,
            modifier = Modifier.weight(1f)
        )
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
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
            ),
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 56.dp)
                .onFocusChanged { focusState ->
                    if (focusState.isFocused && set.repsText == "0") {
                        onSetRepsChange(set.id, "")
                    }
                }
        )
    }
    if (set.prType != null) {
        FitTrackBadge(
            label = if (set.prType == PrType.MaxWeight) "PR PESO" else "PR VOLUMEN",
            tone = FitTrackBadgeTone.Warm,
            modifier = Modifier.padding(start = FitSpacing.smMd, top = 2.dp)
        )
    }
    }
}

@Composable
private fun WorkoutLoadingSkeleton() {
    Column(verticalArrangement = Arrangement.spacedBy(FitSpacing.card)) {
        // Summary card: metrics row + progress bar + finish button
        SkeletonCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(FitSpacing.sm)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(FitSpacing.md)
                ) {
                    repeat(3) {
                        SkeletonBlock(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        )
                    }
                }
                SkeletonBlock(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    shape = MaterialTheme.shapes.small
                )
                SkeletonBlock(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                )
            }
        }
        // 2 exercise cards with 3 set rows each
        repeat(2) {
            SkeletonCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(FitSpacing.sm)) {
                    SkeletonText(widthFraction = 0.5f, lineHeight = 18.dp)
                    repeat(3) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(FitSpacing.md)
                        ) {
                            repeat(3) {
                                SkeletonBlock(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(32.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatStartedAt(timestamp: Long): String {
    return SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(timestamp))
}

private fun formatRestTimer(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
