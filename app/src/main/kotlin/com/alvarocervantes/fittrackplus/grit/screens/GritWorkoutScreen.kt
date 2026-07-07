package com.alvarocervantes.fittrackplus.grit.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.Canvas
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alvarocervantes.fittrackplus.feature.workout.RestTimerStatus
import com.alvarocervantes.fittrackplus.feature.workout.RestTimerUiState
import com.alvarocervantes.fittrackplus.feature.workout.WorkoutExerciseUiState
import com.alvarocervantes.fittrackplus.feature.workout.WorkoutViewModel
import com.alvarocervantes.fittrackplus.grit.components.GritCard
import com.alvarocervantes.fittrackplus.grit.components.GritEmptyState
import com.alvarocervantes.fittrackplus.grit.components.GritOutlineButton
import com.alvarocervantes.fittrackplus.grit.components.GritPrimaryButton
import com.alvarocervantes.fittrackplus.grit.components.GritSectionLabel
import com.alvarocervantes.fittrackplus.grit.components.GritToast
import com.alvarocervantes.fittrackplus.grit.theme.GritColors
import com.alvarocervantes.fittrackplus.grit.theme.GritShapes
import com.alvarocervantes.fittrackplus.grit.theme.GritType
import kotlinx.coroutines.delay

private const val GRIT_DEFAULT_REST_SECONDS = 90

@Composable
fun GritWorkoutScreen(
    onGoToRoutines: () -> Unit,
    viewModel: WorkoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val session = uiState.activeSession

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            when {
                session != null -> ActiveSessionContent(
                    viewModel = viewModel,
                    session = session,
                    isFinishing = uiState.isFinishing
                )
                uiState.preview != null -> PreviewContent(
                    routineName = uiState.preview?.routineName.orEmpty(),
                    dayName = uiState.preview?.dayName.orEmpty(),
                    weekNumber = uiState.preview?.weekNumber ?: 1,
                    exerciseCount = uiState.preview?.exerciseCount ?: 0,
                    isStarting = uiState.isStarting,
                    onStart = viewModel::startWorkout
                )
                !uiState.isLoading -> GritEmptyState(
                    icon = Icons.Filled.Warning,
                    title = "No hay rutina activa",
                    body = "Ve a Rutinas para crear o seleccionar una rutina de entrenamiento.",
                    actionText = "Ir a Rutinas",
                    onAction = onGoToRoutines
                )
            }
        }

        uiState.celebration?.let { celebration ->
            GritToast(
                title = "¡Nuevo récord detectado!",
                message = "Has conseguido ${celebration.prCount} PR en esta sesión. ¡Espectacular!",
                onDismiss = viewModel::dismissCelebration,
                icon = Icons.Filled.AutoAwesome,
                accent = true,
                autoDismissMillis = 6_000,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }

        uiState.message?.let { message ->
            GritToast(
                title = "Entrenamiento",
                message = message,
                onDismiss = viewModel::clearMessage,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
    }

    if (uiState.restTimer.isActive) {
        GritRestOverlay(
            restTimer = uiState.restTimer,
            onAddSeconds = { extra ->
                viewModel.startRestTimer(uiState.restTimer.remainingSeconds + extra)
            },
            onTogglePause = {
                if (uiState.restTimer.status == RestTimerStatus.Running) {
                    viewModel.pauseRestTimer()
                } else {
                    viewModel.resumeRestTimer()
                }
            },
            onSkip = viewModel::cancelRestTimer
        )
    }
}

@Composable
private fun PreviewContent(
    routineName: String,
    dayName: String,
    weekNumber: Int,
    exerciseCount: Int,
    isStarting: Boolean,
    onStart: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        GritSectionLabel(text = "Próxima sesión", color = GritColors.Lime)
        Text(text = routineName.uppercase(), style = GritType.screenTitle)
        Text(
            text = "${dayName.uppercase()} • SEMANA $weekNumber • $exerciseCount EJERCICIOS",
            style = GritType.monoLabel
        )
    }

    GritCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "TODO LISTO PARA EMPEZAR",
                style = GritType.cardTitle
            )
            Text(
                text = "El cronómetro empieza a contar al iniciar la sesión.",
                style = GritType.monoBody
            )
            GritPrimaryButton(
                text = if (isStarting) "Iniciando…" else "Iniciar Entrenamiento",
                icon = Icons.Filled.PlayArrow,
                onClick = onStart,
                enabled = !isStarting,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ActiveSessionContent(
    viewModel: WorkoutViewModel,
    session: com.alvarocervantes.fittrackplus.feature.workout.ActiveWorkoutSessionUiState,
    isFinishing: Boolean
) {
    var exerciseIndex by rememberSaveable { mutableIntStateOf(0) }
    val exercises = session.exercises
    if (exercises.isNotEmpty() && exerciseIndex > exercises.lastIndex) {
        exerciseIndex = exercises.lastIndex
    }
    val currentExercise = exercises.getOrNull(exerciseIndex)

    // Header with finish action
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        GritSectionLabel(text = "Sesión Actual", color = GritColors.Lime)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = session.routineName.uppercase(), style = GritType.cardTitle)
                Text(text = session.dayName.uppercase(), style = GritType.monoLabelSmall)
            }
            Surface(
                shape = GritShapes.small,
                color = GritColors.Red,
                border = androidx.compose.foundation.BorderStroke(1.dp, GritColors.RedBorder),
                onClick = viewModel::finishWorkout,
                enabled = !isFinishing
            ) {
                Text(
                    text = if (isFinishing) "CERRANDO…" else "FINALIZAR",
                    style = GritType.monoStrong,
                    color = GritColors.TextPrimary,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }
        }
    }

    SessionTimerCard(startedAt = session.startedAt)

    // Exercise cycler
    if (exercises.size > 1) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(GritShapes.small)
                .background(GritColors.SurfaceContainerLow)
                .border(1.dp, GritColors.Border, GritShapes.small)
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Ejercicio anterior",
                tint = GritColors.TextSecondary,
                modifier = Modifier
                    .size(24.dp)
                    .clickable {
                        exerciseIndex = if (exerciseIndex > 0) exerciseIndex - 1 else exercises.lastIndex
                    }
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.FitnessCenter,
                    contentDescription = null,
                    tint = GritColors.Lime,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "EJERCICIO ${exerciseIndex + 1} DE ${exercises.size}",
                    style = GritType.monoStrong
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Ejercicio siguiente",
                tint = GritColors.TextSecondary,
                modifier = Modifier
                    .size(24.dp)
                    .clickable {
                        exerciseIndex = if (exerciseIndex < exercises.lastIndex) exerciseIndex + 1 else 0
                    }
            )
        }
    }

    if (currentExercise != null) {
        CurrentExerciseCard(
            exercise = currentExercise,
            viewModel = viewModel
        )
        CompletedSetsSection(exercise = currentExercise)
    }

    // Session progress footer
    GritSectionLabel(
        text = "Progreso: ${session.completedSetCount} / ${session.totalSetCount} series",
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun SessionTimerCard(startedAt: Long) {
    var nowMillis by rememberSaveable { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(startedAt) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            delay(1_000)
        }
    }
    val elapsedSeconds = ((nowMillis - startedAt) / 1_000).coerceAtLeast(0)
    val hours = elapsedSeconds / 3_600
    val minutes = (elapsedSeconds % 3_600) / 60
    val seconds = elapsedSeconds % 60

    GritCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GritSectionLabel(text = "Tiempo Total")
            Text(
                text = "%02d:%02d:%02d".format(hours, minutes, seconds),
                style = GritType.timer
            )
        }
    }
}

@Composable
private fun CurrentExerciseCard(
    exercise: WorkoutExerciseUiState,
    viewModel: WorkoutViewModel
) {
    val completedCount = exercise.sets.count { it.isCompleted }
    val nextSet = exercise.sets.firstOrNull { !it.isCompleted }

    GritCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GritColors.Background)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = exercise.name.uppercase(), style = GritType.itemTitle)
                Text(
                    text = if (nextSet != null) {
                        "SERIE ${nextSet.setNumber} DE ${exercise.sets.size} (OBJ: ${exercise.targetRepsText.uppercase()})"
                    } else {
                        "EJERCICIO COMPLETADO • $completedCount SERIES"
                    },
                    style = GritType.monoLabelSmall,
                    color = if (nextSet != null) GritColors.TextSecondary else GritColors.Lime
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(GritColors.Border)
            )

            if (nextSet != null) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        SetInputStepper(
                            label = "Peso (kg)",
                            valueText = nextSet.weightText,
                            onValueChange = { viewModel.updateSetWeight(nextSet.id, it) },
                            onDecrement = { viewModel.stepSetWeight(nextSet.id, -2.5) },
                            onIncrement = { viewModel.stepSetWeight(nextSet.id, 2.5) },
                            modifier = Modifier.weight(1f)
                        )
                        SetInputStepper(
                            label = "Reps",
                            valueText = nextSet.repsText,
                            onValueChange = { viewModel.updateSetReps(nextSet.id, it) },
                            onDecrement = { viewModel.stepSetReps(nextSet.id, -1) },
                            onIncrement = { viewModel.stepSetReps(nextSet.id, 1) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (nextSet.previousWeight != null || nextSet.previousReps != null) {
                        Text(
                            text = "ANTERIOR: ${nextSet.previousWeight ?: "—"} KG × ${nextSet.previousReps ?: "—"}",
                            style = GritType.monoLabelSmall
                        )
                    }
                    GritPrimaryButton(
                        text = "Completar Serie",
                        onClick = {
                            val restTimer = viewModel.uiState.value.restTimer
                            viewModel.completeSet(nextSet.id)
                            if (!restTimer.autoStartEnabled) {
                                viewModel.startRestTimer(GRIT_DEFAULT_REST_SECONDS)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun SetInputStepper(
    label: String,
    valueText: String,
    onValueChange: (String) -> Unit,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        GritSectionLabel(text = label)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(width = 40.dp, height = 52.dp)
                    .background(GritColors.SurfaceContainer)
                    .border(1.dp, GritColors.Border)
                    .clickable(onClick = onDecrement),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "−", style = GritType.itemTitle)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .background(GritColors.Background)
                    .border(1.dp, GritColors.Border),
                contentAlignment = Alignment.Center
            ) {
                BasicTextField(
                    value = valueText,
                    onValueChange = onValueChange,
                    textStyle = GritType.monoStrong.copy(
                        color = GritColors.Lime,
                        textAlign = TextAlign.Center
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    cursorBrush = SolidColor(GritColors.Lime),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Box(
                modifier = Modifier
                    .size(width = 40.dp, height = 52.dp)
                    .background(GritColors.SurfaceContainer)
                    .border(1.dp, GritColors.Border)
                    .clickable(onClick = onIncrement),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "+", style = GritType.itemTitle)
            }
        }
    }
}

@Composable
private fun CompletedSetsSection(exercise: WorkoutExerciseUiState) {
    val completedSets = exercise.sets.filter { it.isCompleted }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        GritSectionLabel(text = "Historial de series (${completedSets.size} completadas)")
        if (completedSets.isEmpty()) {
            Text(
                text = "No hay series completadas aún en este ejercicio.",
                style = GritType.monoBody,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GritColors.Border, GritShapes.small)
                    .padding(vertical = 16.dp)
            )
        } else {
            completedSets.forEach { set ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(GritShapes.small)
                        .background(GritColors.Surface)
                        .border(1.dp, GritColors.Border, GritShapes.small)
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(GritShapes.small)
                                .background(GritColors.Lime),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = GritColors.Black,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(text = "SERIE ${set.setNumber}", style = GritType.monoStrong)
                    }
                    Text(
                        text = "${set.weightText}kg × ${set.repsText}",
                        style = GritType.monoStrong,
                        color = GritColors.TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun GritRestOverlay(
    restTimer: RestTimerUiState,
    onAddSeconds: (Int) -> Unit,
    onTogglePause: () -> Unit,
    onSkip: () -> Unit
) {
    Dialog(
        onDismissRequest = onSkip,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.96f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocalFireDepartment,
                        contentDescription = null,
                        tint = GritColors.Lime,
                        modifier = Modifier.size(26.dp)
                    )
                    Text(
                        text = "DESCANSANDO",
                        style = GritType.screenTitle,
                        color = GritColors.Lime
                    )
                }

                Box(
                    modifier = Modifier.size(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 8.dp.toPx()
                        val inset = strokeWidth / 2
                        drawArc(
                            color = GritColors.Neutral900,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = Offset(inset, inset),
                            size = androidx.compose.ui.geometry.Size(
                                size.width - strokeWidth,
                                size.height - strokeWidth
                            ),
                            style = Stroke(width = strokeWidth / 2)
                        )
                        drawArc(
                            color = GritColors.Lime,
                            startAngle = -90f,
                            sweepAngle = 360f * restTimer.progress,
                            useCenter = false,
                            topLeft = Offset(inset, inset),
                            size = androidx.compose.ui.geometry.Size(
                                size.width - strokeWidth,
                                size.height - strokeWidth
                            ),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Square)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "%02d:%02d".format(
                                restTimer.remainingSeconds / 60,
                                restTimer.remainingSeconds % 60
                            ),
                            style = GritType.timer,
                            color = GritColors.TextPrimary
                        )
                        Icon(
                            imageVector = if (restTimer.status == RestTimerStatus.Running) {
                                Icons.Filled.Pause
                            } else {
                                Icons.Filled.PlayArrow
                            },
                            contentDescription = if (restTimer.status == RestTimerStatus.Running) {
                                "Pausar descanso"
                            } else {
                                "Reanudar descanso"
                            },
                            tint = GritColors.TextSecondary,
                            modifier = Modifier
                                .size(28.dp)
                                .clickable(onClick = onTogglePause)
                        )
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        GritOutlineButton(
                            text = "+30s",
                            onClick = { onAddSeconds(30) },
                            modifier = Modifier.weight(1f),
                            accentColor = GritColors.TextSecondary
                        )
                        GritOutlineButton(
                            text = "+1m",
                            onClick = { onAddSeconds(60) },
                            modifier = Modifier.weight(1f),
                            accentColor = GritColors.TextSecondary
                        )
                    }
                    GritOutlineButton(
                        text = "Saltar Descanso",
                        onClick = onSkip,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
