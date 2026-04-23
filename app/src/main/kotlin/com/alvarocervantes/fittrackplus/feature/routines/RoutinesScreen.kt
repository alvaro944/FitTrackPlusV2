package com.alvarocervantes.fittrackplus.feature.routines

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.alvarocervantes.fittrackplus.core.design.FitTrackScreenHeader
import com.alvarocervantes.fittrackplus.core.design.FitTrackSectionLabel
import com.alvarocervantes.fittrackplus.core.design.primarySoft
import com.alvarocervantes.fittrackplus.core.design.surfaceAlt

@Composable
fun RoutinesScreen(
    viewModel: RoutinesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var routinePendingArchive by remember { mutableStateOf<RoutineListItemUiState?>(null) }

    state.message?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }

    routinePendingArchive?.let { routine ->
        AlertDialog(
            onDismissRequest = { routinePendingArchive = null },
            title = { Text("Archivar rutina") },
            text = {
                Text(
                    text = "La rutina \"${routine.name}\" dejara de aparecer en la lista principal. Los entrenamientos antiguos no cambiaran."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        routinePendingArchive = null
                        viewModel.archiveRoutine(routine.id)
                    }
                ) {
                    Text("Archivar")
                }
            },
            dismissButton = {
                TextButton(onClick = { routinePendingArchive = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (state.editor == null) {
                FloatingActionButton(
                    onClick = viewModel::startCreateRoutine
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Crear nueva rutina"
                    )
                }
            }
        }
    ) { padding ->
        val editor = state.editor
        if (editor == null) {
            RoutineListContent(
                state = state,
                contentPadding = padding,
                onCreateRoutine = viewModel::startCreateRoutine,
                onEditRoutine = viewModel::startEditRoutine,
                onArchiveRoutine = { routine -> routinePendingArchive = routine },
                onSetActiveRoutine = viewModel::setActiveRoutine
            )
        } else {
            RoutineEditorContent(
                state = state,
                editor = editor,
                contentPadding = padding,
                onClose = viewModel::closeEditor,
                onSave = viewModel::saveEditor,
                onRoutineNameChange = viewModel::updateRoutineName,
                onAddDay = viewModel::addDay,
                onDayNameChange = viewModel::updateDayName,
                onRemoveDay = viewModel::removeDay,
                onAddExercise = viewModel::addExercise,
                onExerciseNameChange = viewModel::updateExerciseName,
                onExerciseSetsChange = viewModel::updateExerciseSets,
                onExerciseRepsChange = viewModel::updateExerciseReps,
                onExerciseNotesChange = viewModel::updateExerciseNotes,
                onRemoveExercise = viewModel::removeExercise
            )
        }
    }
}

@Composable
private fun RoutineListContent(
    state: RoutinesUiState,
    contentPadding: PaddingValues,
    onCreateRoutine: () -> Unit,
    onEditRoutine: (Long) -> Unit,
    onArchiveRoutine: (RoutineListItemUiState) -> Unit,
    onSetActiveRoutine: (Long) -> Unit
) {
    val activeRoutine = state.routines.firstOrNull { it.isActive }

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
                title = "Rutinas",
                subtitle = "${state.routines.size} guardadas"
            )
        }

        if (activeRoutine != null) {
            item {
                FitTrackCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.primarySoft
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(FitSpacing.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Column(
                            verticalArrangement = Arrangement.spacedBy(FitSpacing.xs)
                        ) {
                            Text(
                                text = "Rutina activa",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = activeRoutine.name,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "Entrenar usara esta rutina para preparar la siguiente sesion.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item {
            FitTrackSectionLabel(label = "Biblioteca")
        }

        if (state.routines.isNotEmpty()) {
            item {
                FitTrackCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.surfaceAlt
                ) {
                    Text(
                        text = "Editar o archivar una rutina no modifica sesiones antiguas: el historial sigue leyendo snapshots.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (state.isLoading) {
            item { FitTrackLoadingCard(text = "Cargando rutinas guardadas...") }
        } else if (state.routines.isEmpty()) {
            item {
                FitTrackEmptyState(
                    icon = Icons.AutoMirrored.Filled.List,
                    title = "Aun no hay rutinas",
                    message = "Crea una rutina con dias y ejercicios. Despues podras marcarla como activa para entrenar.",
                    supporting = "La fase visual cambia el aspecto, no las reglas del flujo."
                ) {
                    Button(onClick = onCreateRoutine) {
                        Text("Crear rutina")
                    }
                }
            }
        } else {
            items(
                items = state.routines,
                key = { routine -> routine.id }
            ) { routine ->
                RoutineListItem(
                    routine = routine,
                    onEditRoutine = onEditRoutine,
                    onArchiveRoutine = onArchiveRoutine,
                    onSetActiveRoutine = onSetActiveRoutine
                )
            }
        }
    }
}

@Composable
private fun RoutineListItem(
    routine: RoutineListItemUiState,
    onEditRoutine: (Long) -> Unit,
    onArchiveRoutine: (RoutineListItemUiState) -> Unit,
    onSetActiveRoutine: (Long) -> Unit
) {
    FitTrackCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(FitSpacing.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = routine.name,
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (routine.isActive) {
                            FitTrackBadge(
                                label = "ACTIVA",
                                tone = FitTrackBadgeTone.Active
                            )
                        }
                    }
                    Text(
                        text = "${routine.dayCount} dias · lista para editar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(FitSpacing.sm)
            ) {
                if (!routine.isActive) {
                    FilledTonalButton(
                        onClick = { onSetActiveRoutine(routine.id) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Activar",
                            modifier = Modifier.padding(start = FitSpacing.sm)
                        )
                    }
                }
                OutlinedButton(
                    onClick = { onEditRoutine(routine.id) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Editar",
                        modifier = Modifier.padding(start = FitSpacing.sm)
                    )
                }
                OutlinedButton(
                    onClick = { onArchiveRoutine(routine) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Archive,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Archivar",
                        modifier = Modifier.padding(start = FitSpacing.sm)
                    )
                }
            }
        }
    }
}

@Composable
private fun RoutineEditorContent(
    state: RoutinesUiState,
    editor: RoutineEditorUiState,
    contentPadding: PaddingValues,
    onClose: () -> Unit,
    onSave: () -> Unit,
    onRoutineNameChange: (String) -> Unit,
    onAddDay: () -> Unit,
    onDayNameChange: (Int, String) -> Unit,
    onRemoveDay: (Int) -> Unit,
    onAddExercise: (Int) -> Unit,
    onExerciseNameChange: (Int, Int, String) -> Unit,
    onExerciseSetsChange: (Int, Int, String) -> Unit,
    onExerciseRepsChange: (Int, Int, String) -> Unit,
    onExerciseNotesChange: (Int, Int, String) -> Unit,
    onRemoveExercise: (Int, Int) -> Unit
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
                title = editor.title,
                subtitle = "Edita la estructura visual sin cambiar el comportamiento historico.",
                trailing = {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Cerrar editor de rutina"
                        )
                    }
                }
            )
        }

        item {
            FitTrackSectionLabel(label = "Identidad")
        }

        item {
            OutlinedTextField(
                value = editor.name,
                onValueChange = onRoutineNameChange,
                label = { Text("Nombre de la rutina") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            FitTrackSectionLabel(label = "Dias")
        }

        itemsIndexed(editor.days) { dayIndex, day ->
            RoutineDayEditor(
                dayIndex = dayIndex,
                day = day,
                canRemove = editor.days.size > 1,
                onDayNameChange = onDayNameChange,
                onRemoveDay = onRemoveDay,
                onAddExercise = onAddExercise,
                onExerciseNameChange = onExerciseNameChange,
                onExerciseSetsChange = onExerciseSetsChange,
                onExerciseRepsChange = onExerciseRepsChange,
                onExerciseNotesChange = onExerciseNotesChange,
                onRemoveExercise = onRemoveExercise
            )
        }

        item {
            OutlinedButton(
                onClick = onAddDay,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Anadir dia",
                    modifier = Modifier.padding(start = FitSpacing.sm)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(FitSpacing.sm)
            ) {
                OutlinedButton(
                    onClick = onClose,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar")
                }
                Button(
                    onClick = onSave,
                    enabled = editor.canSave && !state.isSaving,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (state.isSaving) "Guardando" else "Guardar",
                        modifier = Modifier.padding(start = FitSpacing.sm)
                    )
                }
            }
        }
    }
}

@Composable
private fun RoutineDayEditor(
    dayIndex: Int,
    day: RoutineDayEditorUiState,
    canRemove: Boolean,
    onDayNameChange: (Int, String) -> Unit,
    onRemoveDay: (Int) -> Unit,
    onAddExercise: (Int) -> Unit,
    onExerciseNameChange: (Int, Int, String) -> Unit,
    onExerciseSetsChange: (Int, Int, String) -> Unit,
    onExerciseRepsChange: (Int, Int, String) -> Unit,
    onExerciseNotesChange: (Int, Int, String) -> Unit,
    onRemoveExercise: (Int, Int) -> Unit
) {
    FitTrackCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Dia ${dayIndex + 1}",
                style = MaterialTheme.typography.titleLarge
            )
            if (canRemove) {
                IconButton(
                    onClick = { onRemoveDay(dayIndex) },
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Quitar dia ${dayIndex + 1} del borrador"
                    )
                }
            }
        }

        OutlinedTextField(
            value = day.name,
            onValueChange = { onDayNameChange(dayIndex, it) },
            label = { Text("Nombre del dia") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        day.exercises.forEachIndexed { exerciseIndex, exercise ->
            RoutineExerciseEditor(
                dayIndex = dayIndex,
                exerciseIndex = exerciseIndex,
                exercise = exercise,
                canRemove = day.exercises.size > 1,
                onExerciseNameChange = onExerciseNameChange,
                onExerciseSetsChange = onExerciseSetsChange,
                onExerciseRepsChange = onExerciseRepsChange,
                onExerciseNotesChange = onExerciseNotesChange,
                onRemoveExercise = onRemoveExercise
            )
        }

        OutlinedButton(
            onClick = { onAddExercise(dayIndex) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "Anadir ejercicio",
                modifier = Modifier.padding(start = FitSpacing.sm)
            )
        }
    }
}

@Composable
private fun RoutineExerciseEditor(
    dayIndex: Int,
    exerciseIndex: Int,
    exercise: RoutineExerciseEditorUiState,
    canRemove: Boolean,
    onExerciseNameChange: (Int, Int, String) -> Unit,
    onExerciseSetsChange: (Int, Int, String) -> Unit,
    onExerciseRepsChange: (Int, Int, String) -> Unit,
    onExerciseNotesChange: (Int, Int, String) -> Unit,
    onRemoveExercise: (Int, Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceAlt, MaterialTheme.shapes.large)
            .padding(FitSpacing.md),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ejercicio ${exerciseIndex + 1}",
                style = MaterialTheme.typography.labelLarge
            )
            if (canRemove) {
                IconButton(
                    onClick = { onRemoveExercise(dayIndex, exerciseIndex) },
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Quitar ejercicio ${exerciseIndex + 1} del borrador"
                    )
                }
            }
        }

        OutlinedTextField(
            value = exercise.name,
            onValueChange = { onExerciseNameChange(dayIndex, exerciseIndex, it) },
            label = { Text("Nombre del ejercicio") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(FitSpacing.sm)
        ) {
            OutlinedTextField(
                value = exercise.targetSets,
                onValueChange = { onExerciseSetsChange(dayIndex, exerciseIndex, it) },
                label = { Text("Series") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(0.85f)
            )
            OutlinedTextField(
                value = exercise.targetRepsText,
                onValueChange = { onExerciseRepsChange(dayIndex, exerciseIndex, it) },
                label = { Text("Reps objetivo") },
                placeholder = { Text("8-12") },
                singleLine = true,
                modifier = Modifier.weight(1.15f)
            )
        }

        OutlinedTextField(
            value = exercise.notes,
            onValueChange = { onExerciseNotesChange(dayIndex, exerciseIndex, it) },
            label = { Text("Notas") },
            minLines = 1,
            maxLines = 3,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
