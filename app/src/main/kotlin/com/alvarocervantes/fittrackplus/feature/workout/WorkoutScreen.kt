@file:Suppress("TooManyFunctions")

package com.alvarocervantes.fittrackplus.feature.workout

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
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
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalDensity
import androidx.activity.compose.LocalActivity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.ViewModelStoreOwner
import com.alvarocervantes.fittrackplus.core.design.FitSpacing
import com.alvarocervantes.fittrackplus.core.design.onHero
import com.alvarocervantes.fittrackplus.core.design.onHeroMuted
import com.alvarocervantes.fittrackplus.core.design.success
import com.alvarocervantes.fittrackplus.core.design.primaryMid
import com.alvarocervantes.fittrackplus.core.design.accentSoft
import com.alvarocervantes.fittrackplus.core.design.FitTrackBadge
import com.alvarocervantes.fittrackplus.core.design.FitTrackBadgeTone
import com.alvarocervantes.fittrackplus.core.design.FitTrackHeroTag
import com.alvarocervantes.fittrackplus.core.design.FitTrackHeroCard
import com.alvarocervantes.fittrackplus.core.design.FitTrackIconBadge
import com.alvarocervantes.fittrackplus.core.design.FitTrackIconBadgeTone
import com.alvarocervantes.fittrackplus.core.design.FitTrackIconBadgeVariant
import com.alvarocervantes.fittrackplus.core.design.accentWarm
import com.alvarocervantes.fittrackplus.core.design.components.ConfettiAnimation
import com.alvarocervantes.fittrackplus.core.design.components.FitTrackSelectAllTextField
import com.alvarocervantes.fittrackplus.domain.model.PrType
import com.alvarocervantes.fittrackplus.domain.model.ProgressionHint
import com.alvarocervantes.fittrackplus.feature.routines.isValidTargetReps
import com.alvarocervantes.fittrackplus.core.design.FitTrackCard
import com.alvarocervantes.fittrackplus.core.design.FitTrackConfirmDialog
import com.alvarocervantes.fittrackplus.core.design.FitTrackDialog
import com.alvarocervantes.fittrackplus.core.design.FitTrackEmptyState
import com.alvarocervantes.fittrackplus.core.design.FitTrackFormDialogActions
import com.alvarocervantes.fittrackplus.core.design.FitTrackMetric
import com.alvarocervantes.fittrackplus.core.design.FitTrackPrimaryButton
import com.alvarocervantes.fittrackplus.core.design.components.SkeletonBlock
import com.alvarocervantes.fittrackplus.core.design.components.SkeletonCard
import com.alvarocervantes.fittrackplus.core.design.components.SkeletonText
import com.alvarocervantes.fittrackplus.core.design.FitTrackMetricAccent
import com.alvarocervantes.fittrackplus.core.design.FitTrackProgressBar
import com.alvarocervantes.fittrackplus.core.design.FitTrackRadialTimer
import com.alvarocervantes.fittrackplus.core.design.FitTrackScreenHeader
import com.alvarocervantes.fittrackplus.core.design.FitTrackSetRow
import com.alvarocervantes.fittrackplus.core.design.FitTrackSetRowMode
import com.alvarocervantes.fittrackplus.core.design.FitTrackTonalButton
import com.alvarocervantes.fittrackplus.core.design.FitTrackTargetPrescriptionFields
import com.alvarocervantes.fittrackplus.core.design.primarySoft
import com.alvarocervantes.fittrackplus.core.navigation.AppRoute
import com.alvarocervantes.fittrackplus.core.navigation.AppShellViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val MAX_NAME_LENGTH = 60
private const val MAX_NOTES_LENGTH = 500

@Composable
fun WorkoutScreen(
    onGoToRoutines: () -> Unit,
    viewModel: WorkoutViewModel = hiltViewModel()
) {
    val activity = LocalActivity.current
    val appShellOwner = requireNotNull(activity) as ViewModelStoreOwner
    val appShellViewModel: AppShellViewModel = hiltViewModel(appShellOwner)
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val pendingNavigation by appShellViewModel.pendingNavigation.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showFinishConfirmation by remember { mutableStateOf(false) }
    var finishNotes by remember { mutableStateOf("") }
    val haptic = LocalHapticFeedback.current
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        appShellViewModel.activeTabReselected.collect { route ->
            if (route == AppRoute.Workout) {
                listState.animateScrollToItem(0)
            }
        }
    }

    LaunchedEffect(state.activeSession?.sessionId) {
        appShellViewModel.setNavigationBlocker(
            route = AppRoute.Workout,
            isBlocked = state.activeSession != null
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            appShellViewModel.setNavigationBlocker(AppRoute.Workout, isBlocked = false)
        }
    }

    state.message?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }

    if (pendingNavigation != null) {
        FitTrackConfirmDialog(
            title = "Entrenamiento en curso",
            text = "Tienes una sesion activa. ¿Quieres salir sin finalizarla?",
            confirmLabel = "Salir",
            dismissLabel = "Seguir entrenando",
            onConfirm = appShellViewModel::confirmPendingNavigation,
            onDismiss = appShellViewModel::dismissPendingNavigation,
            destructive = true
        )
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

    LaunchedEffect(Unit) {
        viewModel.restTimerFinishedHapticEvent.collect {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    LaunchedEffect(state.activeSession?.sessionId) {
        finishNotes = ""
    }

    // Pick up a session reopened from History when returning to this tab (only when idle).
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshIfIdle()
    }

    if (showFinishConfirmation) {
        val completedSetCount = state.activeSession?.completedSetCount ?: 0
        val totalSetCount = state.activeSession?.totalSetCount ?: 0
        val finishDialogText = when {
            completedSetCount == 0 ->
                "No hay series completadas. Si finalizas ahora, la sesion se descartara."
            completedSetCount < totalSetCount ->
                "Quedan series sin completar. Se guardara como incompleto y podras recuperarlo desde el historial."
            else ->
                "Se guardara la sesion en el historial con las series registradas hasta ahora."
        }
        FinishWorkoutDialog(
            title = "Finalizar entrenamiento",
            text = finishDialogText,
            notes = finishNotes,
            onNotesChange = { finishNotes = it },
            onConfirm = {
                showFinishConfirmation = false
                viewModel.finishWorkout(finishNotes)
            },
            onDismiss = { showFinishConfirmation = false },
            confirmEnabled = !state.isFinishing,
        )
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
                listState = listState,
                onRefresh = viewModel::refresh,
                onStartWorkout = viewModel::startWorkout,
                onFinishWorkout = { showFinishConfirmation = true },
                onSetWeightChange = viewModel::updateSetWeight,
                onSetRepsChange = viewModel::updateSetReps,
                onSetNotesChange = viewModel::updateSetNotes,
                onCompleteSet = viewModel::completeSet,
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
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primaryMid,
                        MaterialTheme.colorScheme.accentWarm,
                        MaterialTheme.colorScheme.tertiary,
                        MaterialTheme.colorScheme.success,
                        MaterialTheme.colorScheme.accentSoft
                    ),
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
private fun FinishWorkoutDialog(
    title: String,
    text: String,
    notes: String,
    onNotesChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmEnabled: Boolean
) {
    FitTrackDialog(
        title = title,
        onDismissRequest = onDismiss,
        content = {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FitTrackSelectAllTextField(
                value = notes,
                onValueChange = onNotesChange,
                label = { Text("Notas de la sesion") },
                singleLine = false,
                minLines = 3,
                selectAllOnFocus = false,
                modifier = Modifier.fillMaxWidth()
            )
        },
        actions = {
            FitTrackFormDialogActions(
                cancelLabel = "Seguir entrenando",
                confirmLabel = "Finalizar",
                onCancel = onDismiss,
                onConfirm = onConfirm,
                confirmEnabled = confirmEnabled
            )
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WorkoutContent(
    state: WorkoutUiState,
    contentPadding: PaddingValues,
    listState: LazyListState,
    onRefresh: () -> Unit,
    onStartWorkout: () -> Unit,
    onFinishWorkout: () -> Unit,
    onSetWeightChange: (Long, String) -> Unit,
    onSetRepsChange: (Long, String) -> Unit,
    onSetNotesChange: (Long, String) -> Unit,
    onCompleteSet: (Long) -> Unit,
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
    val imeBottom = with(LocalDensity.current) {
        WindowInsets.ime.getBottom(this).toDp()
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(
            start = FitSpacing.screenHorizontal,
            top = FitSpacing.screenTop,
            end = FitSpacing.screenHorizontal,
            bottom = FitSpacing.screenBottom + imeBottom
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
                        weightUnitLabel = state.weightUnit.label,
                        hint = state.hints[exercise.id] ?: ProgressionHint.NONE,
                        isExpanded = state.expandedExerciseId == exercise.id,
                        onOpenAlternatives = onOpenExerciseAlternatives,
                        onToggleExpanded = onToggleExerciseExpanded,
                        onSetWeightChange = onSetWeightChange,
                        onSetRepsChange = onSetRepsChange,
                        onSetNotesChange = onSetNotesChange,
                        onCompleteSet = onCompleteSet,
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
                        FitTrackPrimaryButton(
                            label = "Ir a Rutinas",
                            onClick = onGoToRoutines
                        )
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
                        FitTrackTonalButton(
                            label = "Revisar de nuevo",
                            onClick = onRefresh
                        )
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
    FitTrackHeroCard(
        badge = "PROXIMO ENTRENAMIENTO",
        title = {
            Text(
                text = preview.routineName,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onHero,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        },
        cta = if (isStarting) "Iniciando entrenamiento" else "Iniciar entrenamiento",
        onCtaClick = onStartWorkout,
        ctaEnabled = !isStarting,
        ctaIcon = Icons.Filled.PlayArrow,
        content = {
            Text(
                text = preview.dayName,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onHeroMuted
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(FitSpacing.smMd)
            ) {
                FitTrackHeroTag(text = "Semana ${preview.weekNumber}")
                FitTrackHeroTag(text = "${preview.exerciseCount} ejercicios")
            }
        }
    )
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

        FitTrackPrimaryButton(
            label = if (isFinishing) "Finalizando entrenamiento" else "Finalizar entrenamiento",
            onClick = onFinishWorkout,
            enabled = !isFinishing,
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Filled.Check
        )
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
    weightUnitLabel: String,
    hint: ProgressionHint,
    isExpanded: Boolean,
    onOpenAlternatives: (Long) -> Unit,
    onToggleExpanded: (Long) -> Unit,
    onSetWeightChange: (Long, String) -> Unit,
    onSetRepsChange: (Long, String) -> Unit,
    onSetNotesChange: (Long, String) -> Unit,
    onCompleteSet: (Long) -> Unit,
    onStepWeight: (Long, Double) -> Unit,
    onStepReps: (Long, Int) -> Unit
) {
    val showProgressionHint = hint != ProgressionHint.NONE && exercise.sets.none { it.isCompleted }
    val completedSetCount = exercise.sets.count { it.isCompleted }
    val isExerciseCompleted = completedSetCount == exercise.sets.size && exercise.sets.isNotEmpty()
    var showSetNotes by remember(exercise.id) { mutableStateOf(false) }

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
                        exercise.notes?.takeIf { showSetNotes && it.isNotBlank() }?.let { notes ->
                            Text(
                                text = "Notas: $notes",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
                        IconButton(
                            onClick = { showSetNotes = !showSetNotes },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                imageVector = if (showSetNotes) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (showSetNotes) {
                                    "Ocultar notas de las series de ${exercise.name}"
                                } else {
                                    "Mostrar notas de las series de ${exercise.name}"
                                }
                            )
                        }
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
                            weightUnitLabel = weightUnitLabel,
                            showNotes = showSetNotes,
                            onSetWeightChange = onSetWeightChange,
                            onSetRepsChange = onSetRepsChange,
                            onSetNotesChange = onSetNotesChange,
                            onCompleteSet = onCompleteSet,
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
                tint = MaterialTheme.colorScheme.success,
                modifier = Modifier.size(16.dp)
            )
        }
        Text(
            text = if (isExpanded && !isCompleted) "$label abiertas" else label,
            style = MaterialTheme.typography.labelMedium,
            color = if (isCompleted) MaterialTheme.colorScheme.success else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ExerciseVariantOptionCard(
    option: ExerciseVariantOptionUiState,
    enabled: Boolean,
    onClick: () -> Unit
) {
    FitTrackCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled || option.isCurrent, onClick = onClick)
    ) {
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
                    FitTrackBadge(label = "PREDET.", tone = FitTrackBadgeTone.Active)
                }
            }
            Text(
                text = "${option.targetSets} series · ${option.targetRepsText} reps",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            // Only label the row as actionable when tapping it would actually do something.
            if (option.isCurrent || enabled) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = if (option.isCurrent) "Usando ahora" else "Usar ahora",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

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
    var pendingVariantKey by remember { mutableStateOf<String?>(null) }

    pendingVariantKey?.let { variantKey ->
        FitTrackConfirmDialog(
            title = "Cambiar variante",
            text = "Cambiaras el ejercicio activo de esta sesion. Las series objetivo se actualizaran " +
                "segun la nueva variante.",
            confirmLabel = "Cambiar",
            dismissLabel = "Cancelar",
            onConfirm = {
                onApplyVariant(variantKey)
                pendingVariantKey = null
            },
            onDismiss = { pendingVariantKey = null },
            destructive = false
        )
    }

    FitTrackDialog(
        title = "Ejercicios alternativos",
        onDismissRequest = onDismiss,
        content = {
            Text(
                text = picker.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (picker.draft == null) {
                if (!picker.canSwapVariant) {
                    Text(
                        text = "Ya has registrado series en este ejercicio, asi que no se puede " +
                            "cambiar la variante en esta sesion. Puedes editarlas desde Rutinas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                val optionsEnabled = picker.canSwapVariant && !picker.isSaving
                picker.options.forEach { option ->
                    ExerciseVariantOptionCard(
                        option = option,
                        enabled = optionsEnabled,
                        onClick = {
                            if (option.isCurrent) onDismiss() else pendingVariantKey = option.variantKey
                        }
                    )
                }
                FitTrackTonalButton(
                    label = "Crear alternativa",
                    onClick = onStartCreating,
                    enabled = picker.canSwapVariant && !picker.isSaving,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                val notesFocusRequester = remember { FocusRequester() }
                FitTrackSelectAllTextField(
                    value = picker.draft.name,
                    onValueChange = onDraftNameChange,
                    label = { Text("Nombre") },
                    singleLine = true,
                    selectAllOnFocus = false,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { notesFocusRequester.requestFocus() }
                    ),
                    maxLength = MAX_NAME_LENGTH,
                    modifier = Modifier.fillMaxWidth()
                )
                FitTrackTargetPrescriptionFields(
                    targetSets = picker.draft.targetSets,
                    targetRepsText = picker.draft.targetRepsText,
                    onTargetSetsChange = onDraftSetsChange,
                    onTargetRepsChange = onDraftRepsChange,
                    isValidTargetReps = ::isValidTargetReps
                )
                FitTrackSelectAllTextField(
                    value = picker.draft.notes,
                    onValueChange = onDraftNotesChange,
                    label = { Text("Notas") },
                    singleLine = false,
                    minLines = 2,
                    selectAllOnFocus = false,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    maxLength = MAX_NOTES_LENGTH,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(notesFocusRequester)
                )
            }
        },
        actions = if (picker.draft != null) {
            {
                FitTrackFormDialogActions(
                    cancelLabel = "Cancelar",
                    confirmLabel = "Guardar y usar",
                    onCancel = onCancelCreating,
                    onConfirm = onSaveAlternative,
                    confirmEnabled = picker.draft.canSave && !picker.isSaving
                )
            }
        } else null
    )
}

@Composable
private fun WorkoutSetRow(
    set: WorkoutSetUiState,
    weightUnitLabel: String,
    showNotes: Boolean,
    onSetWeightChange: (Long, String) -> Unit,
    onSetRepsChange: (Long, String) -> Unit,
    onSetNotesChange: (Long, String) -> Unit,
    onCompleteSet: (Long) -> Unit,
    onStepWeight: (Long, Double) -> Unit,
    onStepReps: (Long, Int) -> Unit
) {
    FitTrackSetRow(
        setId = set.id,
        setNumber = set.setNumber,
        weightText = set.weightText,
        repsText = set.repsText,
        notes = set.notes,
        showNotes = showNotes,
        mode = FitTrackSetRowMode.Edit,
        isCompleted = set.isCompleted,
        isReadyToComplete = isWorkoutSetReadyToComplete(
            set.repsText,
            set.isCompleted
        ),
        showCompletionControl = true,
        previousWeight = set.previousWeight,
        weightUnitLabel = weightUnitLabel,
        previousReps = set.previousReps,
        onWeightChange = { onSetWeightChange(set.id, it) },
        onRepsChange = { onSetRepsChange(set.id, it) },
        onNotesChange = { onSetNotesChange(set.id, it) },
        onComplete = { onCompleteSet(set.id) },
        onStepWeight = { onStepWeight(set.id, it) },
        onStepReps = { onStepReps(set.id, it) },
        footer = if (set.prType != null) {
            {
                FitTrackBadge(
                    label = if (set.prType == PrType.MaxWeight) "PR PESO" else "PR VOLUMEN",
                    tone = FitTrackBadgeTone.Warm,
                    modifier = Modifier.padding(start = FitSpacing.smMd, top = 2.dp)
                )
            }
        } else {
            null
        }
    )
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
                    MaterialTheme.colorScheme.success
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

internal fun formatPreviousWeightLabel(previousWeight: String): String = "ant. $previousWeight kg"

internal fun formatPreviousRepsLabel(previousReps: Int): String = "ant. $previousReps"

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
