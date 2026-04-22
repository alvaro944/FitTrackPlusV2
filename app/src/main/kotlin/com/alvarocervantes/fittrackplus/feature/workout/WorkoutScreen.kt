package com.alvarocervantes.fittrackplus.feature.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WorkoutScreen(
    viewModel: WorkoutViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    state.message?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        WorkoutContent(
            state = state,
            contentPadding = padding,
            onRefresh = viewModel::refresh,
            onStartWorkout = viewModel::startWorkout,
            onFinishWorkout = viewModel::finishWorkout,
            onSetWeightChange = viewModel::updateSetWeight,
            onSetRepsChange = viewModel::updateSetReps
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
    onSetRepsChange: (Long, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            WorkoutHeader(
                state = state,
                onRefresh = onRefresh
            )
        }

        when {
            state.isLoading -> {
                item {
                    CircularProgressIndicator()
                }
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
                    NoActiveRoutineState()
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
                    EmptyWorkoutState(onRefresh = onRefresh)
                }
            }
        }
    }
}

@Composable
private fun WorkoutHeader(
    state: WorkoutUiState,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Entrenar",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = when {
                    state.activeSession != null -> "Sesion en curso"
                    state.preview != null -> "Siguiente entrenamiento listo"
                    else -> "Registro de entrenamiento"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onRefresh) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = "Actualizar"
            )
        }
    }
}

@Composable
private fun NoActiveRoutineState() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "No hay rutina activa",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Selecciona una rutina activa en Rutinas para iniciar un entrenamiento.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WorkoutPreviewCard(
    preview: WorkoutPreviewUiState,
    isStarting: Boolean,
    onStartWorkout: () -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = preview.routineName,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = preview.dayName,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                AssistChip(
                    onClick = {},
                    label = { Text("Semana ${preview.weekNumber}") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.FitnessCenter,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }

            Text(
                text = "${preview.exerciseCount} ejercicios preparados",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = onStartWorkout,
                enabled = !isStarting,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Iniciar entrenamiento",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun ActiveSessionSummary(
    session: ActiveWorkoutSessionUiState,
    isFinishing: Boolean,
    onFinishWorkout: () -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = session.routineName,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = session.dayName,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Iniciada ${formatStartedAt(session.startedAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                AssistChip(
                    onClick = {},
                    label = { Text("Semana ${session.weekNumber}") }
                )
            }

            Text(
                text = "${session.completedSetCount}/${session.totalSetCount} series con reps",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    text = "Finalizar entrenamiento",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun WorkoutExerciseCard(
    exercise: WorkoutExerciseUiState,
    onSetWeightChange: (Long, String) -> Unit,
    onSetRepsChange: (Long, String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Objetivo: ${exercise.targetRepsText} reps",
                    style = MaterialTheme.typography.bodySmall,
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = set.setNumber.toString(),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(0.35f)
        )
        OutlinedTextField(
            value = set.weightText,
            onValueChange = { onSetWeightChange(set.id, it) },
            label = { Text("Kg") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = set.repsText,
            onValueChange = { onSetRepsChange(set.id, it) },
            label = { Text("Reps") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun EmptyWorkoutState(
    onRefresh: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "No se encontro el siguiente entrenamiento",
                style = MaterialTheme.typography.titleMedium
            )
            Button(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Revisar de nuevo",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

private fun formatStartedAt(timestamp: Long): String {
    return SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(timestamp))
}
