package com.alvarocervantes.fittrackplus.feature.routines

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
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

private val repsPresetOptions = listOf("5", "6-8", "8-12", "10-15")

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
            if (state.editor == null && !state.showArchived) {
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
                onSetActiveRoutine = viewModel::setActiveRoutine,
                onSetShowArchived = viewModel::setShowArchived,
                onRestoreRoutine = viewModel::restoreRoutine,
                onDismissSnapshotInfo = viewModel::dismissSnapshotInfo
            )
        } else {
            if (editor.showCloseConfirmation) {
                AlertDialog(
                    onDismissRequest = viewModel::dismissCloseConfirmation,
                    title = { Text("Descartar cambios") },
                    text = { Text("Los cambios sin guardar se perderan. Quieres cerrar el editor?") },
                    confirmButton = {
                        TextButton(onClick = viewModel::closeEditor) { Text("Descartar") }
                    },
                    dismissButton = {
                        TextButton(onClick = viewModel::dismissCloseConfirmation) { Text("Seguir editando") }
                    }
                )
            }
            RoutineEditorContent(
                state = state,
                editor = editor,
                contentPadding = padding,
                onClose = viewModel::requestCloseEditor,
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
    onSetActiveRoutine: (Long) -> Unit,
    onSetShowArchived: (Boolean) -> Unit,
    onRestoreRoutine: (Long) -> Unit,
    onDismissSnapshotInfo: () -> Unit
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
                subtitle = if (state.showArchived) {
                    "${state.archivedRoutines.size} archivadas"
                } else {
                    "${state.routines.size} guardadas"
                }
            )
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(FitSpacing.sm)
            ) {
                FilterChip(
                    selected = !state.showArchived,
                    onClick = { onSetShowArchived(false) },
                    label = { Text("Activas") }
                )
                FilterChip(
                    selected = state.showArchived,
                    onClick = { onSetShowArchived(true) },
                    label = { Text("Archivadas") }
                )
            }
        }

        if (!state.showArchived) {
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

            if (state.routines.isNotEmpty() && !state.hasSeenSnapshotInfo) {
                item {
                    FitTrackCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = MaterialTheme.colorScheme.surfaceAlt
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "Editar o archivar una rutina no modifica sesiones antiguas: el historial sigue leyendo snapshots.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = onDismissSnapshotInfo,
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Cerrar aviso de snapshots",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
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
        } else {
            item {
                FitTrackSectionLabel(label = "Archivadas")
            }

            if (state.archivedRoutines.isEmpty()) {
                item {
                    FitTrackEmptyState(
                        icon = Icons.Filled.Archive,
                        title = "Sin rutinas archivadas",
                        message = "Las rutinas que archives apareceran aqui. Puedes restaurarlas en cualquier momento."
                    )
                }
            } else {
                items(
                    items = state.archivedRoutines,
                    key = { routine -> "archived_${routine.id}" }
                ) { routine ->
                    ArchivedRoutineListItem(
                        routine = routine,
                        onRestoreRoutine = onRestoreRoutine
                    )
                }
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
            verticalArrangement = Arrangement.spacedBy(FitSpacing.mdLg)
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
private fun ArchivedRoutineListItem(
    routine: RoutineListItemUiState,
    onRestoreRoutine: (Long) -> Unit
) {
    FitTrackCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(FitSpacing.mdLg)
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
                    Text(
                        text = routine.name,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${routine.dayCount} dias · archivada",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                FitTrackBadge(
                    label = "ARCHIVADA",
                    tone = FitTrackBadgeTone.Neutral
                )
            }
            FilledTonalButton(
                onClick = { onRestoreRoutine(routine.id) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.Unarchive,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Restaurar",
                    modifier = Modifier.padding(start = FitSpacing.sm)
                )
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
    val currentSets = exercise.targetSets.toIntOrNull()?.coerceIn(1, 99) ?: 3
    val hasCustomReps = exercise.targetRepsText !in repsPresetOptions
    var showCustomRepsDialog by remember { mutableStateOf(false) }
    var customRepsDraft by remember { mutableStateOf("") }
    var showNotesDialog by remember { mutableStateOf(false) }
    var notesDraft by remember { mutableStateOf("") }

    if (showCustomRepsDialog) {
        AlertDialog(
            onDismissRequest = { showCustomRepsDialog = false },
            title = { Text("Reps personalizadas") },
            text = {
                OutlinedTextField(
                    value = customRepsDraft,
                    onValueChange = { customRepsDraft = it },
                    label = { Text("Valor personalizado") },
                    placeholder = { Text("12-15 o AMRAP") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onExerciseRepsChange(dayIndex, exerciseIndex, customRepsDraft.trim())
                        showCustomRepsDialog = false
                    },
                    enabled = customRepsDraft.trim().isNotEmpty()
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomRepsDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showNotesDialog) {
        AlertDialog(
            onDismissRequest = { showNotesDialog = false },
            title = { Text(if (exercise.notes.isBlank()) "Anadir nota" else "Editar nota") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(FitSpacing.sm)) {
                    OutlinedTextField(
                        value = notesDraft,
                        onValueChange = { notesDraft = it },
                        label = { Text("Notas") },
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (exercise.notes.isNotBlank()) {
                        TextButton(
                            onClick = {
                                onExerciseNotesChange(dayIndex, exerciseIndex, "")
                                showNotesDialog = false
                            }
                        ) {
                            Text("Eliminar nota")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onExerciseNotesChange(dayIndex, exerciseIndex, notesDraft)
                        showNotesDialog = false
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNotesDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceAlt, MaterialTheme.shapes.large)
            .padding(FitSpacing.md),
        verticalArrangement = Arrangement.spacedBy(FitSpacing.smMd)
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

        ExerciseSetsStepper(
            sets = currentSets,
            onDecrease = {
                onExerciseSetsChange(dayIndex, exerciseIndex, (currentSets - 1).coerceAtLeast(1).toString())
            },
            onIncrease = {
                onExerciseSetsChange(dayIndex, exerciseIndex, (currentSets + 1).coerceAtMost(99).toString())
            }
        )

        ExerciseRepsSelector(
            selectedReps = exercise.targetRepsText,
            onPresetSelected = { preset ->
                onExerciseRepsChange(dayIndex, exerciseIndex, preset)
            },
            onCustomSelected = {
                customRepsDraft = if (hasCustomReps) exercise.targetRepsText else ""
                showCustomRepsDialog = true
            }
        )

        NotesActionRow(
            hasNote = exercise.notes.isNotBlank(),
            onClick = {
                notesDraft = exercise.notes
                showNotesDialog = true
            }
        )
    }
}

@Composable
private fun ExerciseSetsStepper(
    sets: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(FitSpacing.xs)
    ) {
        Text(
            text = "Series",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.large)
                    .padding(horizontal = FitSpacing.sm, vertical = FitSpacing.xs)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(FitSpacing.sm)
                ) {
                    IconButton(
                        onClick = onDecrease,
                        enabled = sets > 1,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Remove,
                            contentDescription = "Reducir series"
                        )
                    }
                    Text(
                        text = sets.toString(),
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(
                        onClick = onIncrease,
                        enabled = sets < 99,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Aumentar series"
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExerciseRepsSelector(
    selectedReps: String,
    onPresetSelected: (String) -> Unit,
    onCustomSelected: () -> Unit
) {
    val hasCustomSelection = selectedReps !in repsPresetOptions

    Column(
        verticalArrangement = Arrangement.spacedBy(FitSpacing.xs)
    ) {
        Text(
            text = "Reps objetivo",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(FitSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(FitSpacing.sm)
        ) {
            repsPresetOptions.forEach { preset ->
                FilterChip(
                    selected = selectedReps == preset,
                    onClick = { onPresetSelected(preset) },
                    label = { Text(preset) }
                )
            }
            FilterChip(
                selected = hasCustomSelection,
                onClick = onCustomSelected,
                label = { Text(if (hasCustomSelection) selectedReps else "+") }
            )
        }
    }
}

@Composable
private fun NotesActionRow(
    hasNote: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        TextButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = if (hasNote) "Editar nota" else "Anadir nota",
                modifier = Modifier.padding(start = FitSpacing.sm)
            )
        }
    }
}
