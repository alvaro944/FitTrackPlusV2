@file:Suppress("TooManyFunctions")

package com.alvarocervantes.fittrackplus.feature.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
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
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
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
import com.alvarocervantes.fittrackplus.domain.model.ProgressionHint
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

    LaunchedEffect(Unit) {
        viewModel.setCompletionHapticEvent.collect {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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
                        text = if (state.activeSession?.completedSetCount == 0) {
                            "No hay series completadas. Si finalizas ahora, la sesion se descartara."
                        } else {
                            "Se guardara la sesion en el historial con las series registradas hasta ahora."
                        },
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

    state.alternativePicker?.let { picker ->
        ExerciseAlternativesDialog(
            picker = picker,
            onDismiss = viewModel::dismissExerciseAlternatives,
            onApplyVariant = viewModel::applyExerciseVariant,
            onStartCreating = viewModel::startCreatingExerciseAlternative,
            onCancelCreating = viewModel::cancelCreatingExerciseAlternative,
            onDraftNameChange = viewModel::updateAlternativeDraftName,
            onDraftSetsChange = viewModel::updateAlternativeDraftSets,
            onDraftRepsChange = viewModel::updateAlternativeDraftReps,
            onDraftNotesChange = viewModel::updateAlternativeDraftNotes,
            onSaveAlternative = viewModel::saveExerciseAlternative
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
                onStepWeight = viewModel::stepSetWeight,
                onStepReps = viewModel::stepSetReps,
                onStartRestTimer = viewModel::startRestTimer,
                onPauseRestTimer = viewModel::pauseRestTimer,
                onResumeRestTimer = viewModel::resumeRestTimer,
                onResetRestTimer = viewModel::resetRestTimer,
                onCancelRestTimer = viewModel::cancelRestTimer,
                onAutoStartRestTimerChange = viewModel::setAutoStartRestTimerEnabled,
                onOpenExerciseAlternatives = viewModel::openExerciseAlternatives,
                onToggleExerciseExpanded = viewModel::toggleExerciseExpanded,
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WorkoutContent(
    state: WorkoutUiState,
    contentPadding: PaddingValues,
    onRefresh: () -> Unit,
    onStartWorkout: () -> Unit,
    onFinishWorkout: () -> Unit,
    onSetWeightChange: (Long, String) -> Unit,
    onSetRepsChange: (Long, String) -> Unit,
    onStepWeight: (Long, Double) -> Unit,
    onStepReps: (Long, Int) -> Unit,
    onStartRestTimer: (Int) -> Unit,
    onPauseRestTimer: () -> Unit,
    onResumeRestTimer: () -> Unit,
    onResetRestTimer: () -> Unit,
    onCancelRestTimer: () -> Unit,
    onAutoStartRestTimerChange: (Boolean) -> Unit,
    onOpenExerciseAlternatives: (Long) -> Unit,
    onToggleExerciseExpanded: (Long) -> Unit,
    onGoToRoutines: () -> Unit
) {
    val listState = rememberLazyListState()
    val allowFieldFocus = !listState.isScrollInProgress

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .imeNestedScroll(),
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
                        hint = state.hints[exercise.id] ?: ProgressionHint.NONE,
                        isExpanded = state.expandedExerciseId == exercise.id,
                        allowFieldFocus = allowFieldFocus,
                        onOpenAlternatives = onOpenExerciseAlternatives,
                        onToggleExpanded = onToggleExerciseExpanded,
                        onSetWeightChange = onSetWeightChange,
                        onSetRepsChange = onSetRepsChange,
                        onStepWeight = onStepWeight,
                        onStepReps = onStepReps
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
    hint: ProgressionHint,
    isExpanded: Boolean,
    allowFieldFocus: Boolean,
    onOpenAlternatives: (Long) -> Unit,
    onToggleExpanded: (Long) -> Unit,
    onSetWeightChange: (Long, String) -> Unit,
    onSetRepsChange: (Long, String) -> Unit,
    onStepWeight: (Long, Double) -> Unit,
    onStepReps: (Long, Int) -> Unit
) {
    val showProgressionHint = hint != ProgressionHint.NONE && exercise.sets.none { it.isCompleted }
    val completedSetCount = exercise.sets.count { it.isCompleted }
    val isExerciseCompleted = completedSetCount == exercise.sets.size && exercise.sets.isNotEmpty()

    FitTrackCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(FitSpacing.md)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(FitSpacing.xs)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleExpanded(exercise.id) },
                    horizontalArrangement = Arrangement.spacedBy(FitSpacing.sm),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(FitSpacing.xs)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(FitSpacing.xs),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = exercise.name,
                                style = MaterialTheme.typography.titleLarge,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (showProgressionHint) {
                                ProgressionHintButton(hint = hint)
                            }
                        }
                        Text(
                            text = "Objetivo: ${exercise.targetRepsText} reps",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        ExerciseCompletionLabel(
                            isExpanded = isExpanded,
                            isCompleted = isExerciseCompleted,
                            completedSetCount = completedSetCount,
                            totalSetCount = exercise.sets.size
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(FitSpacing.xs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isExpanded) {
                                Icons.Filled.KeyboardArrowUp
                            } else {
                                Icons.Filled.KeyboardArrowDown
                            },
                            contentDescription = if (isExpanded) {
                                "Contraer ${exercise.name}"
                            } else {
                                "Expandir ${exercise.name}"
                            }
                        )
                        IconButton(
                            onClick = { onOpenAlternatives(exercise.id) },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Ver ejercicios alternativos para ${exercise.name}"
                            )
                        }
                    }
                }
            }

            if (isExpanded) {
                if (exercise.sets.isEmpty()) {
                    Text(
                        text = "Sin series configuradas",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    exercise.sets.forEach { set ->
                        WorkoutSetRow(
                            set = set,
                            allowFieldFocus = allowFieldFocus,
                            onSetWeightChange = onSetWeightChange,
                            onSetRepsChange = onSetRepsChange,
                            onStepWeight = onStepWeight,
                            onStepReps = onStepReps
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseCompletionLabel(
    isExpanded: Boolean,
    isCompleted: Boolean,
    completedSetCount: Int,
    totalSetCount: Int
) {
    val label = if (isCompleted) {
        "Completado"
    } else {
        "$completedSetCount/$totalSetCount series"
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(FitSpacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isCompleted) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = Color(0xFF2E7D32),
                modifier = Modifier.size(16.dp)
            )
        }
        Text(
            text = if (isExpanded && !isCompleted) "$label abiertas" else label,
            style = MaterialTheme.typography.labelMedium,
            color = if (isCompleted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExerciseAlternativesDialog(
    picker: ExerciseAlternativesUiState,
    onDismiss: () -> Unit,
    onApplyVariant: (String) -> Unit,
    onStartCreating: () -> Unit,
    onCancelCreating: () -> Unit,
    onDraftNameChange: (String) -> Unit,
    onDraftSetsChange: (String) -> Unit,
    onDraftRepsChange: (String) -> Unit,
    onDraftNotesChange: (String) -> Unit,
    onSaveAlternative: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .imeNestedScroll()
                    .padding(FitSpacing.cardPadding),
                verticalArrangement = Arrangement.spacedBy(FitSpacing.md)
            ) {
                Text(
                    text = "Ejercicios alternativos",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = picker.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (picker.draft == null) {
                    picker.options.forEach { option ->
                        FitTrackCard(modifier = Modifier.fillMaxWidth()) {
                            Column(verticalArrangement = Arrangement.spacedBy(FitSpacing.xs)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = option.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (option.isDefault) {
                                        FitTrackBadge(
                                            label = "PREDET.",
                                            tone = FitTrackBadgeTone.Active
                                        )
                                    }
                                }
                                Text(
                                    text = "${option.targetSets} series · ${option.targetRepsText} reps",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = { onApplyVariant(option.variantKey) },
                                        enabled = !option.isCurrent
                                    ) {
                                        Text(if (option.isCurrent) "Usando ahora" else "Usar hoy")
                                    }
                                }
                            }
                        }
                    }
                    FilledTonalButton(
                        onClick = onStartCreating,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Crear alternativa")
                    }
                } else {
                    OutlinedTextField(
                        value = picker.draft.name,
                        onValueChange = onDraftNameChange,
                        label = { Text("Nombre") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(FitSpacing.sm)) {
                        OutlinedTextField(
                            value = picker.draft.targetSets,
                            onValueChange = onDraftSetsChange,
                            label = { Text("Series") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = picker.draft.targetRepsText,
                            onValueChange = onDraftRepsChange,
                            label = { Text("Reps") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = picker.draft.notes,
                        onValueChange = onDraftNotesChange,
                        label = { Text("Notas") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onCancelCreating) {
                            Text("Cancelar")
                        }
                        TextButton(
                            onClick = onSaveAlternative,
                            enabled = picker.draft.canSave && !picker.isSaving
                        ) {
                            Text("Guardar y usar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeightFieldColumn(
    setId: Long,
    weightText: String,
    previousWeight: String?,
    previousReps: Int?,
    allowFieldFocus: Boolean,
    isCompleted: Boolean,
    onSetWeightChange: (Long, String) -> Unit,
    onStepWeight: (Long, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var fieldValue by remember(setId) { mutableStateOf(TextFieldValue(weightText)) }
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(weightText) {
        fieldValue = syncWorkoutFieldValue(
            current = fieldValue,
            externalText = weightText
        )
    }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) {
                fieldValue = selectAllWorkoutFieldValue(fieldValue)
            }
        }
    }

    Column(modifier = modifier) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(FitSpacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SetStepperButton(
                icon = Icons.Filled.Remove,
                contentDescription = "Bajar peso de la serie ${setId}",
                onClick = { onStepWeight(setId, -2.5) },
                onLongClick = { onStepWeight(setId, -5.0) },
                minButtonSize = REPS_STEPPER_BUTTON_SIZE,
                iconSize = REPS_STEPPER_ICON_SIZE
            )
            OutlinedTextField(
                value = fieldValue,
                onValueChange = { value ->
                    fieldValue = value
                    onSetWeightChange(setId, value.text)
                },
                placeholder = { Text("Kg") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = workoutSetFieldColors(isCompleted = isCompleted),
                interactionSource = interactionSource,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 56.dp)
                    .focusProperties { canFocus = allowFieldFocus }
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            fieldValue = selectAllWorkoutFieldValue(fieldValue)
                        }
                    }
            )
            SetStepperButton(
                icon = Icons.Filled.Add,
                contentDescription = "Subir peso de la serie ${setId}",
                onClick = { onStepWeight(setId, 2.5) },
                onLongClick = { onStepWeight(setId, 5.0) },
                minButtonSize = REPS_STEPPER_BUTTON_SIZE,
                iconSize = REPS_STEPPER_ICON_SIZE
            )
        }
        val previousPerformance = formatPreviousWorkoutSetPerformance(
            previousWeight = previousWeight,
            previousReps = previousReps
        )
        if (previousPerformance != null) {
            Text(
                text = previousPerformance,
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
    allowFieldFocus: Boolean,
    onSetWeightChange: (Long, String) -> Unit,
    onSetRepsChange: (Long, String) -> Unit,
    onStepWeight: (Long, Double) -> Unit,
    onStepReps: (Long, Int) -> Unit
) {
    val rowBackground = if (set.isCompleted) {
        MaterialTheme.colorScheme.primarySoft
    } else {
        MaterialTheme.colorScheme.surfaceAlt
    }
    var repsFieldValue by remember(set.id) { mutableStateOf(TextFieldValue(set.repsText)) }
    val repsInteractionSource = remember { MutableInteractionSource() }

    LaunchedEffect(set.repsText) {
        repsFieldValue = syncWorkoutFieldValue(
            current = repsFieldValue,
            externalText = set.repsText
        )
    }

    LaunchedEffect(repsInteractionSource) {
        repsInteractionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) {
                repsFieldValue = selectAllWorkoutFieldValue(repsFieldValue)
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(rowBackground, MaterialTheme.shapes.large)
                .padding(FitSpacing.smMd),
            horizontalArrangement = Arrangement.spacedBy(FitSpacing.sm),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .size(WORKOUT_SET_INDEX_SIZE)
                    .background(
                        color = if (set.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        shape = CircleShape
                    )
                    .semantics {
                        contentDescription = if (set.isCompleted) {
                            "Serie ${set.setNumber} completada"
                        } else {
                            "Serie ${set.setNumber} pendiente"
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (set.isCompleted) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
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
                previousReps = set.previousReps,
                allowFieldFocus = allowFieldFocus,
                isCompleted = set.isCompleted,
                onSetWeightChange = onSetWeightChange,
                onStepWeight = onStepWeight,
                modifier = Modifier.weight(WORKOUT_WEIGHT_COLUMN_WEIGHT)
            )
            Row(
                modifier = Modifier.weight(WORKOUT_REPS_COLUMN_WEIGHT),
                horizontalArrangement = Arrangement.spacedBy(FitSpacing.tiny),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SetStepperButton(
                    icon = Icons.Filled.Remove,
                    contentDescription = "Bajar repeticiones de la serie ${set.setNumber}",
                    onClick = { onStepReps(set.id, -1) },
                    minButtonSize = REPS_STEPPER_BUTTON_SIZE,
                    iconSize = REPS_STEPPER_ICON_SIZE
                )
                OutlinedTextField(
                    value = repsFieldValue,
                    onValueChange = { value ->
                        repsFieldValue = value
                        onSetRepsChange(set.id, value.text)
                    },
                    placeholder = { Text("Reps") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = workoutSetFieldColors(isCompleted = set.isCompleted),
                    interactionSource = repsInteractionSource,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 56.dp)
                        .focusProperties { canFocus = allowFieldFocus }
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) {
                                repsFieldValue = selectAllWorkoutFieldValue(repsFieldValue)
                            }
                        }
                )
                SetStepperButton(
                    icon = Icons.Filled.Add,
                    contentDescription = "Subir repeticiones de la serie ${set.setNumber}",
                    onClick = { onStepReps(set.id, 1) },
                    minButtonSize = REPS_STEPPER_BUTTON_SIZE,
                    iconSize = REPS_STEPPER_ICON_SIZE
                )
            }
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

internal fun formatPreviousWorkoutSetPerformance(
    previousWeight: String?,
    previousReps: Int?
): String? {
    val weightPart = previousWeight?.let { "$it kg" }
    val repsPart = previousReps?.takeIf { it > 0 }?.let { "$it reps" }
    val parts = listOfNotNull(weightPart, repsPart)
    if (parts.isEmpty()) return null
    return "Ultima vez: ${parts.joinToString(separator = " · ")}"
}

@Composable
private fun ProgressionHintButton(hint: ProgressionHint) {
    var showHintMessage by remember { mutableStateOf(false) }

    Box {
        IconButton(
            onClick = { showHintMessage = true },
            modifier = Modifier.minimumInteractiveComponentSize()
        ) {
            Icon(
                imageVector = if (hint == ProgressionHint.UP) {
                    Icons.Filled.KeyboardArrowUp
                } else {
                    Icons.Filled.KeyboardArrowDown
                },
                contentDescription = if (hint == ProgressionHint.UP) {
                    "Sugerencia de subir peso"
                } else {
                    "Sugerencia de bajar peso"
                },
                tint = if (hint == ProgressionHint.UP) {
                    Color(0xFF2E7D32)
                } else {
                    MaterialTheme.colorScheme.accentWarm
                }
            )
        }
        DropdownMenu(
            expanded = showHintMessage,
            onDismissRequest = { showHintMessage = false }
        ) {
            DropdownMenuItem(
                text = { Text(progressionHintSupportText(hint)) },
                onClick = { showHintMessage = false }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SetStepperButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    minButtonSize: androidx.compose.ui.unit.Dp = 36.dp,
    iconSize: androidx.compose.ui.unit.Dp = 18.dp
) {
    Box(
        modifier = Modifier
            .sizeIn(minWidth = minButtonSize, minHeight = minButtonSize)
            .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(iconSize)
        )
    }
}

internal fun selectAllWorkoutFieldValue(current: TextFieldValue): TextFieldValue {
    return current.copy(selection = TextRange(0, current.text.length))
}

internal fun syncWorkoutFieldValue(
    current: TextFieldValue,
    externalText: String
): TextFieldValue {
    return if (current.text == externalText) {
        current
    } else {
        TextFieldValue(
            text = externalText,
            selection = TextRange(externalText.length, externalText.length)
        )
    }
}

@Composable
private fun workoutSetFieldColors(isCompleted: Boolean) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    focusedTextColor = if (isCompleted) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onSurface
    },
    unfocusedTextColor = if (isCompleted) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onSurface
    },
    cursorColor = MaterialTheme.colorScheme.primary,
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent
)

private val WORKOUT_SET_INDEX_SIZE = 40.dp
private val REPS_STEPPER_BUTTON_SIZE = 28.dp
private val REPS_STEPPER_ICON_SIZE = 16.dp
private const val WORKOUT_WEIGHT_COLUMN_WEIGHT = 1.0f
private const val WORKOUT_REPS_COLUMN_WEIGHT = 1.1f

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

private fun progressionHintSupportText(hint: ProgressionHint): String {
    return when (hint) {
        ProgressionHint.UP -> "Has superado el rango las ultimas sesiones. Considera subir peso."
        ProgressionHint.DOWN -> "No has alcanzado el rango las ultimas sesiones. Considera bajar peso."
        ProgressionHint.NONE -> ""
    }
}
