package com.alvarocervantes.fittrackplus.feature.routines

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.ViewModelStoreOwner
import com.alvarocervantes.fittrackplus.core.design.FitSpacing
import com.alvarocervantes.fittrackplus.core.design.FitTrackAddButton
import com.alvarocervantes.fittrackplus.core.design.FitTrackBadge
import com.alvarocervantes.fittrackplus.core.design.FitTrackBadgeTone
import com.alvarocervantes.fittrackplus.core.design.FitTrackCard
import com.alvarocervantes.fittrackplus.core.design.FitTrackConfirmDialog
import com.alvarocervantes.fittrackplus.core.design.FitTrackDialog
import com.alvarocervantes.fittrackplus.core.design.FitTrackEmptyState
import com.alvarocervantes.fittrackplus.core.design.FitTrackEntityListCard
import com.alvarocervantes.fittrackplus.core.design.FitTrackEntityListCardBadge
import com.alvarocervantes.fittrackplus.core.design.FitTrackFormDialogActions
import com.alvarocervantes.fittrackplus.core.design.FitTrackInputDialog
import com.alvarocervantes.fittrackplus.core.design.FitTrackIconBadge
import com.alvarocervantes.fittrackplus.core.design.FitTrackIconBadgeTone
import com.alvarocervantes.fittrackplus.core.design.FitTrackIconBadgeVariant
import com.alvarocervantes.fittrackplus.core.design.FitTrackOutlinedButton
import com.alvarocervantes.fittrackplus.core.design.FitTrackPrimaryButton
import com.alvarocervantes.fittrackplus.core.design.FitTrackReorderActions
import com.alvarocervantes.fittrackplus.core.design.FitTrackScreenHeader
import com.alvarocervantes.fittrackplus.core.design.FitTrackTonalButton
import com.alvarocervantes.fittrackplus.core.design.FitTrackTargetPrescriptionFields
import com.alvarocervantes.fittrackplus.core.design.components.FitTrackSelectAllTextField
import com.alvarocervantes.fittrackplus.core.design.components.SkeletonBlock
import com.alvarocervantes.fittrackplus.core.design.components.SkeletonCard
import com.alvarocervantes.fittrackplus.core.design.components.SkeletonText
import com.alvarocervantes.fittrackplus.core.design.FitTrackSectionLabel
import com.alvarocervantes.fittrackplus.core.navigation.AppRoute
import com.alvarocervantes.fittrackplus.core.navigation.AppShellViewModel
import com.alvarocervantes.fittrackplus.core.design.borderLight
import com.alvarocervantes.fittrackplus.core.design.primarySoft
import com.alvarocervantes.fittrackplus.core.design.surfaceAlt


@Composable
fun RoutinesScreen(
    viewModel: RoutinesViewModel = hiltViewModel()
) {
    val activity = LocalActivity.current
    val appShellOwner = requireNotNull(activity) as ViewModelStoreOwner
    val appShellViewModel: AppShellViewModel = hiltViewModel(appShellOwner)
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val pendingNavigation by appShellViewModel.pendingNavigation.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var routinePendingArchive by remember { mutableStateOf<RoutineListItemUiState?>(null) }

    LaunchedEffect(state.editor?.hasUnsavedChanges) {
        appShellViewModel.setNavigationBlocker(
            route = AppRoute.Routines,
            isBlocked = state.editor?.hasUnsavedChanges == true
        )
    }

    BackHandler(enabled = state.editor != null) {
        viewModel.requestCloseEditor()
    }

    state.message?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }

    routinePendingArchive?.let { routine ->
        FitTrackConfirmDialog(
            title = "Archivar rutina",
            text = "La rutina \"${routine.name}\" dejara de aparecer en la lista principal. Los entrenamientos antiguos no cambiaran.",
            confirmLabel = "Archivar",
            dismissLabel = "Cancelar",
            onConfirm = {
                routinePendingArchive = null
                viewModel.archiveRoutine(routine.id)
            },
            onDismiss = { routinePendingArchive = null },
            destructive = true
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            when {
                state.editor?.hasUnsavedChanges == true -> {
                    ExtendedFloatingActionButton(
                        onClick = {
                            if (!state.isSaving) viewModel.saveEditor()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null
                        )
                        Text(
                            text = "Guardar",
                            modifier = Modifier.padding(start = FitSpacing.xs)
                        )
                    }
                }
                state.editor == null && !state.showArchived -> {
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
        }
    ) { padding ->
        val editor = state.editor
        if (editor == null) {
            RoutineListContent(
                state = state,
                contentPadding = padding,
                onCreateRoutine = viewModel::startCreateRoutine,
                onUseTemplate = viewModel::startCreateRoutineFromTemplate,
                onEditRoutine = viewModel::startEditRoutine,
                onArchiveRoutine = { routine -> routinePendingArchive = routine },
                onSetActiveRoutine = viewModel::setActiveRoutine,
                onSetShowArchived = viewModel::setShowArchived,
                onRestoreRoutine = viewModel::restoreRoutine,
                onDismissSnapshotInfo = viewModel::dismissSnapshotInfo
            )
        } else {
            if (editor.showCloseConfirmation || pendingNavigation != null) {
                FitTrackConfirmDialog(
                    title = "Cambios sin guardar",
                    text = "Tienes cambios sin guardar. ¿Quieres descartarlos?",
                    confirmLabel = "Descartar",
                    dismissLabel = "Seguir editando",
                    onConfirm = {
                        if (pendingNavigation != null) {
                            viewModel.discardEditorChanges()
                            appShellViewModel.confirmPendingNavigation()
                        } else {
                            viewModel.resolveCloseConfirmation(discard = true)
                        }
                    },
                    onDismiss = {
                        if (pendingNavigation != null) {
                            appShellViewModel.dismissPendingNavigation()
                        } else {
                            viewModel.resolveCloseConfirmation(discard = false)
                        }
                    },
                    destructive = true
                )
            }
            RoutineEditorContent(
                state = state,
                editor = editor,
                contentPadding = padding,
                onClose = viewModel::requestCloseEditor,
                onSave = viewModel::saveEditor,
                onToggleDayExpansion = viewModel::toggleDayExpansion,
                onRoutineNameChange = viewModel::updateRoutineName,
                onAddDay = viewModel::addDay,
                onDayNameChange = viewModel::updateDayName,
                onDuplicateDay = { dayIndex ->
                    viewModel.applyEditorOperation(RoutineEditorOperation.DuplicateDay(dayIndex))
                },
                onMoveDay = { dayIndex, direction ->
                    viewModel.applyEditorOperation(RoutineEditorOperation.MoveDay(dayIndex, direction))
                },
                onRemoveDay = viewModel::removeDay,
                onAddExercise = viewModel::addExercise,
                onExerciseNameChange = viewModel::updateExerciseName,
                onExerciseSetsChange = viewModel::updateExerciseSets,
                onExerciseRepsChange = viewModel::updateExerciseReps,
                onExerciseNotesChange = viewModel::updateExerciseNotes,
                onAddExerciseAlternative = viewModel::addExerciseAlternative,
                onBeginExerciseAlternativeEdit = viewModel::beginExerciseAlternativeEdit,
                onCancelExerciseAlternativeEdit = viewModel::cancelExerciseAlternativeEdit,
                onFinishExerciseAlternativeEdit = viewModel::finishExerciseAlternativeEdit,
                onExerciseAlternativeNameChange = viewModel::updateExerciseAlternativeName,
                onExerciseAlternativeSetsChange = viewModel::updateExerciseAlternativeSets,
                onExerciseAlternativeRepsChange = viewModel::updateExerciseAlternativeReps,
                onExerciseAlternativeNotesChange = viewModel::updateExerciseAlternativeNotes,
                onRemoveExerciseAlternative = viewModel::removeExerciseAlternative,
                onSetExerciseDefaultVariant = viewModel::setExerciseDefaultVariant,
                onDuplicateExercise = { dayIndex, exerciseIndex ->
                    viewModel.applyEditorOperation(
                        RoutineEditorOperation.DuplicateExercise(dayIndex, exerciseIndex)
                    )
                },
                onMoveExercise = { dayIndex, exerciseIndex, direction ->
                    viewModel.applyEditorOperation(
                        RoutineEditorOperation.MoveExercise(dayIndex, exerciseIndex, direction)
                    )
                },
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
    onUseTemplate: (String) -> Unit,
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
                            FitTrackIconBadge(
                                variant = FitTrackIconBadgeVariant.Icon(Icons.Filled.Check),
                                tone = FitTrackIconBadgeTone.Filled
                            )
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
                items(3) { RoutineListItemSkeleton() }
            } else if (state.routines.isEmpty()) {
                item {
                    FitTrackEmptyState(
                        icon = Icons.AutoMirrored.Filled.List,
                        title = "Aun no hay rutinas",
                        message = "Crea una rutina desde cero o usa una plantilla para tener una base editable.",
                        supporting = "Revisa la plantilla antes de guardar; no se toca el historial hasta entrenar."
                    ) {
                        FitTrackPrimaryButton(
                            label = "Crear rutina",
                            onClick = onCreateRoutine
                        )
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

            item {
                FitTrackSectionLabel(label = "Plantillas")
            }

            items(
                items = routineTemplates,
                key = { template -> template.id }
            ) { template ->
                RoutineTemplateCard(
                    template = template,
                    onUseTemplate = onUseTemplate
                )
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
private fun RoutineTemplateCard(
    template: RoutineTemplateUiState,
    onUseTemplate: (String) -> Unit
) {
    FitTrackCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(FitSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(FitSpacing.xs)
            ) {
                Text(
                    text = template.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = template.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${template.days.size} dias - ${template.days.sumOf { it.exercises.size }} ejercicios",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FitTrackTonalButton(
                label = "Usar",
                onClick = { onUseTemplate(template.id) }
            )
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
    FitTrackEntityListCard(
        title = routine.name,
        modifier = Modifier.fillMaxWidth(),
        badge = if (routine.isActive) {
            FitTrackEntityListCardBadge("ACTIVA", FitTrackBadgeTone.Active)
        } else {
            null
        },
        meta = "${routine.dayCount} dias · lista para editar",
        actions = if (routine.isActive) {
            listOf({
                FitTrackOutlinedButton(
                    label = "Editar",
                    onClick = { onEditRoutine(routine.id) },
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Edit
                )
                FitTrackOutlinedButton(
                    label = "Archivar",
                    onClick = { onArchiveRoutine(routine) },
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Archive
                )
            })
        } else {
            listOf(
                {
                    FitTrackTonalButton(
                        label = "Activar",
                        onClick = { onSetActiveRoutine(routine.id) },
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Filled.Check
                    )
                },
                {
                    FitTrackOutlinedButton(
                        label = "Editar",
                        onClick = { onEditRoutine(routine.id) },
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.Edit
                    )
                    FitTrackOutlinedButton(
                        label = "Archivar",
                        onClick = { onArchiveRoutine(routine) },
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.Archive
                    )
                }
            )
        }
    )
}

@Composable
private fun ArchivedRoutineListItem(
    routine: RoutineListItemUiState,
    onRestoreRoutine: (Long) -> Unit
) {
    FitTrackEntityListCard(
        title = routine.name,
        modifier = Modifier.fillMaxWidth(),
        badge = FitTrackEntityListCardBadge("ARCHIVADA", FitTrackBadgeTone.Neutral),
        meta = "${routine.dayCount} dias · archivada",
        actions = listOf({
            FitTrackTonalButton(
                label = "Restaurar",
                onClick = { onRestoreRoutine(routine.id) },
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Filled.Unarchive
            )
        })
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RoutineEditorContent(
    state: RoutinesUiState,
    editor: RoutineEditorUiState,
    contentPadding: PaddingValues,
    onClose: () -> Unit,
    onSave: () -> Unit,
    onToggleDayExpansion: (Int) -> Unit,
    onRoutineNameChange: (String) -> Unit,
    onAddDay: () -> Unit,
    onDayNameChange: (Int, String) -> Unit,
    onDuplicateDay: (Int) -> Unit,
    onMoveDay: (Int, MoveDirection) -> Unit,
    onRemoveDay: (Int) -> Unit,
    onAddExercise: (Int) -> Unit,
    onExerciseNameChange: (Int, Int, String) -> Unit,
    onExerciseSetsChange: (Int, Int, String) -> Unit,
    onExerciseRepsChange: (Int, Int, String) -> Unit,
    onExerciseNotesChange: (Int, Int, String) -> Unit,
    onAddExerciseAlternative: (Int, Int) -> Unit,
    onBeginExerciseAlternativeEdit: (Int, Int, Int) -> Unit,
    onCancelExerciseAlternativeEdit: (Int, Int, Int) -> Unit,
    onFinishExerciseAlternativeEdit: (Int, Int, Int) -> Unit,
    onExerciseAlternativeNameChange: (Int, Int, Int, String) -> Unit,
    onExerciseAlternativeSetsChange: (Int, Int, Int, String) -> Unit,
    onExerciseAlternativeRepsChange: (Int, Int, Int, String) -> Unit,
    onExerciseAlternativeNotesChange: (Int, Int, Int, String) -> Unit,
    onRemoveExerciseAlternative: (Int, Int, Int) -> Unit,
    onSetExerciseDefaultVariant: (Int, Int, String?) -> Unit,
    onDuplicateExercise: (Int, Int) -> Unit,
    onMoveExercise: (Int, Int, MoveDirection) -> Unit,
    onRemoveExercise: (Int, Int) -> Unit
) {
    val listState = rememberLazyListState()
    val imeBottom = with(LocalDensity.current) {
        WindowInsets.ime.getBottom(this).toDp()
    }
    var exercisePendingRemoval by remember { mutableStateOf<PendingExerciseRemoval?>(null) }

    exercisePendingRemoval?.let { pendingRemoval ->
        FitTrackConfirmDialog(
            title = "Eliminar ejercicio",
            text = exerciseRemovalMessage(
                exerciseIndex = pendingRemoval.exerciseIndex,
                exerciseName = pendingRemoval.exerciseName
            ),
            confirmLabel = "Eliminar",
            dismissLabel = "Cancelar",
            onConfirm = {
                onRemoveExercise(pendingRemoval.dayIndex, pendingRemoval.exerciseIndex)
                exercisePendingRemoval = null
            },
            onDismiss = { exercisePendingRemoval = null },
            destructive = true
        )
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
            FitTrackCard(modifier = Modifier.fillMaxWidth()) {
                FitTrackSelectAllTextField(
                    value = editor.name,
                    onValueChange = onRoutineNameChange,
                    label = { Text("Nombre de la rutina") },
                    isError = editor.routineNameError != null,
                    supportingText = editor.routineNameError?.let { error ->
                        { Text(error) }
                    },
                    singleLine = true,
                    selectAllOnFocus = false,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item {
            FitTrackSectionLabel(label = "Dias")
        }

        itemsIndexed(editor.days) { dayIndex, day ->
            RoutineDayEditor(
                dayIndex = dayIndex,
                day = day,
                isExpanded = editor.expandedDayIndex == dayIndex,
                canRemove = editor.days.size > 1,
                canMoveUp = dayIndex > 0,
                canMoveDown = dayIndex < editor.days.lastIndex,
                onToggleExpanded = onToggleDayExpansion,
                onDayNameChange = onDayNameChange,
                onDuplicateDay = onDuplicateDay,
                onMoveDay = onMoveDay,
                onRemoveDay = onRemoveDay,
                onAddExercise = onAddExercise,
                onExerciseNameChange = onExerciseNameChange,
                onExerciseSetsChange = onExerciseSetsChange,
                onExerciseRepsChange = onExerciseRepsChange,
                onExerciseNotesChange = onExerciseNotesChange,
                onAddExerciseAlternative = onAddExerciseAlternative,
                onBeginExerciseAlternativeEdit = onBeginExerciseAlternativeEdit,
                onCancelExerciseAlternativeEdit = onCancelExerciseAlternativeEdit,
                onFinishExerciseAlternativeEdit = onFinishExerciseAlternativeEdit,
                onExerciseAlternativeNameChange = onExerciseAlternativeNameChange,
                onExerciseAlternativeSetsChange = onExerciseAlternativeSetsChange,
                onExerciseAlternativeRepsChange = onExerciseAlternativeRepsChange,
                onExerciseAlternativeNotesChange = onExerciseAlternativeNotesChange,
                onRemoveExerciseAlternative = onRemoveExerciseAlternative,
                onSetExerciseDefaultVariant = onSetExerciseDefaultVariant,
                onDuplicateExercise = onDuplicateExercise,
                onMoveExercise = onMoveExercise,
                onRemoveExercise = { selectedDayIndex, selectedExerciseIndex, exerciseName ->
                    exercisePendingRemoval = PendingExerciseRemoval(
                        dayIndex = selectedDayIndex,
                        exerciseIndex = selectedExerciseIndex,
                        exerciseName = exerciseName
                    )
                }
            )
        }

        item {
            FitTrackAddButton(
                label = "Anadir dia",
                onClick = onAddDay,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(FitSpacing.sm)
            ) {
                editor.validationMessage?.let { validationMessage ->
                    Text(
                        text = validationMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(FitSpacing.sm)
                ) {
                    FitTrackOutlinedButton(
                        label = "Cancelar",
                        onClick = onClose,
                        modifier = Modifier.weight(1f)
                    )
                    FitTrackPrimaryButton(
                        label = if (state.isSaving) "Guardando" else "Guardar",
                        onClick = onSave,
                        enabled = editor.canSave && !state.isSaving,
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.Check
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
    isExpanded: Boolean,
    canRemove: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onToggleExpanded: (Int) -> Unit,
    onDayNameChange: (Int, String) -> Unit,
    onDuplicateDay: (Int) -> Unit,
    onMoveDay: (Int, MoveDirection) -> Unit,
    onRemoveDay: (Int) -> Unit,
    onAddExercise: (Int) -> Unit,
    onExerciseNameChange: (Int, Int, String) -> Unit,
    onExerciseSetsChange: (Int, Int, String) -> Unit,
    onExerciseRepsChange: (Int, Int, String) -> Unit,
    onExerciseNotesChange: (Int, Int, String) -> Unit,
    onAddExerciseAlternative: (Int, Int) -> Unit,
    onBeginExerciseAlternativeEdit: (Int, Int, Int) -> Unit,
    onCancelExerciseAlternativeEdit: (Int, Int, Int) -> Unit,
    onFinishExerciseAlternativeEdit: (Int, Int, Int) -> Unit,
    onExerciseAlternativeNameChange: (Int, Int, Int, String) -> Unit,
    onExerciseAlternativeSetsChange: (Int, Int, Int, String) -> Unit,
    onExerciseAlternativeRepsChange: (Int, Int, Int, String) -> Unit,
    onExerciseAlternativeNotesChange: (Int, Int, Int, String) -> Unit,
    onRemoveExerciseAlternative: (Int, Int, Int) -> Unit,
    onSetExerciseDefaultVariant: (Int, Int, String?) -> Unit,
    onDuplicateExercise: (Int, Int) -> Unit,
    onMoveExercise: (Int, Int, MoveDirection) -> Unit,
    onRemoveExercise: (Int, Int, String) -> Unit
) {
    FitTrackCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        color = if (isExpanded) {
                            MaterialTheme.colorScheme.primarySoft
                        } else {
                            MaterialTheme.colorScheme.surfaceAlt
                        },
                        shape = MaterialTheme.shapes.large
                    )
                    .clickable { onToggleExpanded(dayIndex) }
                    .padding(FitSpacing.md),
                horizontalArrangement = Arrangement.spacedBy(FitSpacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 6.dp, height = 40.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = MaterialTheme.shapes.small
                        )
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(FitSpacing.tiny)
                ) {
                    Text(
                        text = day.name.ifBlank { "Dia ${dayIndex + 1}" },
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (day.exercises.size == 1) {
                            "1 ejercicio"
                        } else {
                            "${day.exercises.size} ejercicios"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = { onToggleExpanded(dayIndex) },
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = if (isExpanded) {
                            Icons.Filled.KeyboardArrowUp
                        } else {
                            Icons.Filled.KeyboardArrowDown
                        },
                        contentDescription = if (isExpanded) {
                            "Colapsar dia ${dayIndex + 1}"
                        } else {
                            "Expandir dia ${dayIndex + 1}"
                        }
                    )
                }
            }
        }

        if (isExpanded) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                FitTrackReorderActions(
                    canMoveUp = canMoveUp,
                    canMoveDown = canMoveDown,
                    canRemove = canRemove,
                    onMoveUp = { onMoveDay(dayIndex, MoveDirection.Up) },
                    onMoveDown = { onMoveDay(dayIndex, MoveDirection.Down) },
                    onDuplicate = { onDuplicateDay(dayIndex) },
                    onRemove = { onRemoveDay(dayIndex) },
                    moveUpContentDescription = "Subir dia ${dayIndex + 1}",
                    moveDownContentDescription = "Bajar dia ${dayIndex + 1}",
                    duplicateContentDescription = "Duplicar dia ${dayIndex + 1}",
                    removeContentDescription = "Quitar dia ${dayIndex + 1} del borrador"
                )
            }

            FitTrackSelectAllTextField(
                value = day.name,
                onValueChange = { onDayNameChange(dayIndex, it) },
                label = { Text("Nombre del dia") },
                isError = day.nameError != null,
                supportingText = day.nameError?.let { error ->
                    { Text(error) }
                },
                singleLine = true,
                selectAllOnFocus = false,
                modifier = Modifier.fillMaxWidth()
            )

            day.exercises.forEachIndexed { exerciseIndex, exercise ->
                RoutineExerciseEditor(
                    dayIndex = dayIndex,
                    exerciseIndex = exerciseIndex,
                    exercise = exercise,
                    canRemove = day.exercises.size > 1,
                    canMoveUp = exerciseIndex > 0,
                    canMoveDown = exerciseIndex < day.exercises.lastIndex,
                    onExerciseNameChange = onExerciseNameChange,
                    onExerciseSetsChange = onExerciseSetsChange,
                    onExerciseRepsChange = onExerciseRepsChange,
                    onExerciseNotesChange = onExerciseNotesChange,
                    onAddExerciseAlternative = onAddExerciseAlternative,
                    onBeginExerciseAlternativeEdit = onBeginExerciseAlternativeEdit,
                    onCancelExerciseAlternativeEdit = onCancelExerciseAlternativeEdit,
                    onFinishExerciseAlternativeEdit = onFinishExerciseAlternativeEdit,
                    onExerciseAlternativeNameChange = onExerciseAlternativeNameChange,
                    onExerciseAlternativeSetsChange = onExerciseAlternativeSetsChange,
                    onExerciseAlternativeRepsChange = onExerciseAlternativeRepsChange,
                    onExerciseAlternativeNotesChange = onExerciseAlternativeNotesChange,
                    onRemoveExerciseAlternative = onRemoveExerciseAlternative,
                    onSetExerciseDefaultVariant = onSetExerciseDefaultVariant,
                    onDuplicateExercise = onDuplicateExercise,
                    onMoveExercise = onMoveExercise,
                    onRemoveExercise = onRemoveExercise
                )
            }

            FitTrackAddButton(
                label = "Anadir ejercicio",
                onClick = { onAddExercise(dayIndex) },
                modifier = Modifier.fillMaxWidth()
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
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onExerciseNameChange: (Int, Int, String) -> Unit,
    onExerciseSetsChange: (Int, Int, String) -> Unit,
    onExerciseRepsChange: (Int, Int, String) -> Unit,
    onExerciseNotesChange: (Int, Int, String) -> Unit,
    onAddExerciseAlternative: (Int, Int) -> Unit,
    onBeginExerciseAlternativeEdit: (Int, Int, Int) -> Unit,
    onCancelExerciseAlternativeEdit: (Int, Int, Int) -> Unit,
    onFinishExerciseAlternativeEdit: (Int, Int, Int) -> Unit,
    onExerciseAlternativeNameChange: (Int, Int, Int, String) -> Unit,
    onExerciseAlternativeSetsChange: (Int, Int, Int, String) -> Unit,
    onExerciseAlternativeRepsChange: (Int, Int, Int, String) -> Unit,
    onExerciseAlternativeNotesChange: (Int, Int, Int, String) -> Unit,
    onRemoveExerciseAlternative: (Int, Int, Int) -> Unit,
    onSetExerciseDefaultVariant: (Int, Int, String?) -> Unit,
    onDuplicateExercise: (Int, Int) -> Unit,
    onMoveExercise: (Int, Int, MoveDirection) -> Unit,
    onRemoveExercise: (Int, Int, String) -> Unit
) {
    var showNotesDialog by remember { mutableStateOf(false) }
    var notesDraft by remember { mutableStateOf("") }
    var showAlternativesDialog by remember { mutableStateOf(false) }
    var editingAlternativeIndex by remember { mutableStateOf<Int?>(null) }

    if (showNotesDialog) {
        FitTrackInputDialog(
            title = if (exercise.notes.isBlank()) "Anadir nota" else "Editar nota",
            value = notesDraft,
            onValueChange = { notesDraft = it },
            label = "Notas",
            singleLine = false,
            minLines = 3,
            maxLines = 5,
            confirmLabel = "Guardar",
            dismissLabel = "Cancelar",
            onConfirm = {
                onExerciseNotesChange(dayIndex, exerciseIndex, notesDraft)
                showNotesDialog = false
            },
            onDismiss = { showNotesDialog = false },
            extraContent = {
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
        )
    }

    if (showAlternativesDialog) {
        ExerciseAlternativesEditorDialog(
            exercise = exercise,
            editingAlternativeIndex = editingAlternativeIndex,
            onDismiss = {
                cancelInlineAlternativeEdit(
                    editingAlternativeIndex,
                    dayIndex,
                    exerciseIndex,
                    onCancelExerciseAlternativeEdit
                )
                showAlternativesDialog = false
                editingAlternativeIndex = null
            },
            onSetBaseAsDefault = { onSetExerciseDefaultVariant(dayIndex, exerciseIndex, null) },
            onSetAlternativeAsDefault = { variantKey ->
                onSetExerciseDefaultVariant(dayIndex, exerciseIndex, variantKey)
            },
            onStartCreateAlternative = {
                val nextIndex = exercise.alternatives.size
                onBeginExerciseAlternativeEdit(dayIndex, exerciseIndex, nextIndex)
                onAddExerciseAlternative(dayIndex, exerciseIndex)
                editingAlternativeIndex = nextIndex
            },
            onEditAlternative = { alternativeIndex ->
                onBeginExerciseAlternativeEdit(dayIndex, exerciseIndex, alternativeIndex)
                editingAlternativeIndex = alternativeIndex
            },
            onRemoveAlternative = { alternativeIndex ->
                onRemoveExerciseAlternative(dayIndex, exerciseIndex, alternativeIndex)
                if (editingAlternativeIndex == alternativeIndex) editingAlternativeIndex = null
            },
            onAlternativeNameChange = { alternativeIndex, value ->
                onExerciseAlternativeNameChange(dayIndex, exerciseIndex, alternativeIndex, value)
            },
            onAlternativeSetsChange = { alternativeIndex, value ->
                onExerciseAlternativeSetsChange(dayIndex, exerciseIndex, alternativeIndex, value)
            },
            onAlternativeRepsChange = { alternativeIndex, value ->
                onExerciseAlternativeRepsChange(dayIndex, exerciseIndex, alternativeIndex, value)
            },
            onAlternativeNotesChange = { alternativeIndex, value ->
                onExerciseAlternativeNotesChange(dayIndex, exerciseIndex, alternativeIndex, value)
            },
            onCancelEditor = {
                cancelInlineAlternativeEdit(
                    editingAlternativeIndex,
                    dayIndex,
                    exerciseIndex,
                    onCancelExerciseAlternativeEdit
                )
                editingAlternativeIndex = null
            },
            onCloseEditor = {
                finishInlineAlternativeEdit(
                    editingAlternativeIndex,
                    dayIndex,
                    exerciseIndex,
                    onFinishExerciseAlternativeEdit
                )
                editingAlternativeIndex = null
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
            FitTrackReorderActions(
                canMoveUp = canMoveUp,
                canMoveDown = canMoveDown,
                canRemove = canRemove,
                onMoveUp = { onMoveExercise(dayIndex, exerciseIndex, MoveDirection.Up) },
                onMoveDown = { onMoveExercise(dayIndex, exerciseIndex, MoveDirection.Down) },
                onDuplicate = { onDuplicateExercise(dayIndex, exerciseIndex) },
                onRemove = { onRemoveExercise(dayIndex, exerciseIndex, exercise.name) },
                moveUpContentDescription = "Subir ejercicio ${exerciseIndex + 1}",
                moveDownContentDescription = "Bajar ejercicio ${exerciseIndex + 1}",
                duplicateContentDescription = "Duplicar ejercicio ${exerciseIndex + 1}",
                removeContentDescription = "Quitar ejercicio ${exerciseIndex + 1} del borrador",
                extraAction = {
                    IconButton(
                        onClick = { showAlternativesDialog = true },
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Ver ejercicios alternativos para ${exercise.name.ifBlank { "este ejercicio" }}"
                        )
                    }
                }
            )
        }

        FitTrackSelectAllTextField(
            value = exercise.name,
            onValueChange = { onExerciseNameChange(dayIndex, exerciseIndex, it) },
            label = { Text("Nombre del ejercicio") },
            isError = exercise.nameError != null,
            supportingText = exercise.nameError?.let { error ->
                { Text(error) }
            },
            singleLine = true,
            selectAllOnFocus = false,
            modifier = Modifier.fillMaxWidth()
        )

        FitTrackTargetPrescriptionFields(
            targetSets = exercise.targetSets,
            targetRepsText = exercise.targetRepsText,
            onTargetSetsChange = { value ->
                onExerciseSetsChange(dayIndex, exerciseIndex, value)
            },
            onTargetRepsChange = { value ->
                onExerciseRepsChange(dayIndex, exerciseIndex, value)
            },
            isValidTargetReps = ::isValidTargetReps,
            targetSetsError = exercise.targetSetsError,
            targetRepsError = exercise.targetRepsError
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

private data class PendingExerciseRemoval(
    val dayIndex: Int,
    val exerciseIndex: Int,
    val exerciseName: String
)

private fun finishInlineAlternativeEdit(
    alternativeIndex: Int?,
    dayIndex: Int,
    exerciseIndex: Int,
    onFinish: (Int, Int, Int) -> Unit
) {
    val index = alternativeIndex ?: return
    onFinish(dayIndex, exerciseIndex, index)
}

private fun cancelInlineAlternativeEdit(
    alternativeIndex: Int?,
    dayIndex: Int,
    exerciseIndex: Int,
    onCancel: (Int, Int, Int) -> Unit
) {
    val index = alternativeIndex ?: return
    onCancel(dayIndex, exerciseIndex, index)
}

internal fun exerciseRemovalMessage(
    exerciseIndex: Int,
    exerciseName: String
): String {
    val trimmedName = exerciseName.trim()
    return if (trimmedName.isNotEmpty()) {
        "Se eliminara \"$trimmedName\" de la rutina. Esta accion no se puede deshacer."
    } else {
        "Se eliminara el ejercicio ${exerciseIndex + 1} de la rutina. Esta accion no se puede deshacer."
    }
}

@Composable
private fun ExerciseAlternativesEditorDialog(
    exercise: RoutineExerciseEditorUiState,
    editingAlternativeIndex: Int?,
    onDismiss: () -> Unit,
    onSetBaseAsDefault: () -> Unit,
    onSetAlternativeAsDefault: (String?) -> Unit,
    onStartCreateAlternative: () -> Unit,
    onEditAlternative: (Int) -> Unit,
    onRemoveAlternative: (Int) -> Unit,
    onAlternativeNameChange: (Int, String) -> Unit,
    onAlternativeSetsChange: (Int, String) -> Unit,
    onAlternativeRepsChange: (Int, String) -> Unit,
    onAlternativeNotesChange: (Int, String) -> Unit,
    onCancelEditor: () -> Unit,
    onCloseEditor: () -> Unit
) {
    FitTrackDialog(
        title = "Ejercicios alternativos",
        onDismissRequest = onDismiss,
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(FitSpacing.sm)) {
                FitTrackCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = if (exercise.defaultVariantKey == exercise.variantKey) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.borderLight
                    }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(FitSpacing.xs)) {
                        Text(
                            text = exercise.name.ifBlank { "Ejercicio base" },
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${exercise.targetSets} series · ${exercise.targetRepsText} reps",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = onSetBaseAsDefault) {
                                Text(
                                    if (exercise.defaultVariantKey == null || exercise.defaultVariantKey == exercise.variantKey) {
                                        "Predeterminada"
                                    } else {
                                        "Usar por defecto"
                                    }
                                )
                            }
                        }
                    }
                }

                exercise.alternatives.forEachIndexed { index, alternative ->
                    val isEditing = editingAlternativeIndex == index
                    FitTrackCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = if (exercise.defaultVariantKey == alternative.variantKey) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.borderLight
                        }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(FitSpacing.xs)) {
                            if (isEditing) {
                                FitTrackSelectAllTextField(
                                    value = alternative.name,
                                    onValueChange = { onAlternativeNameChange(index, it) },
                                    label = { Text("Nombre") },
                                    singleLine = true,
                                    selectAllOnFocus = false,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                FitTrackTargetPrescriptionFields(
                                    targetSets = alternative.targetSets,
                                    targetRepsText = alternative.targetRepsText,
                                    onTargetSetsChange = { value ->
                                        onAlternativeSetsChange(index, value)
                                    },
                                    onTargetRepsChange = { value ->
                                        onAlternativeRepsChange(index, value)
                                    },
                                    isValidTargetReps = ::isValidTargetReps,
                                    targetSetsError = alternative.targetSetsError,
                                    targetRepsError = alternative.targetRepsError
                                )
                                FitTrackSelectAllTextField(
                                    value = alternative.notes,
                                    onValueChange = { onAlternativeNotesChange(index, it) },
                                    label = { Text("Notas") },
                                    singleLine = false,
                                    minLines = 2,
                                    selectAllOnFocus = false,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                FitTrackFormDialogActions(
                                    cancelLabel = "Cancelar",
                                    confirmLabel = "Guardar",
                                    onCancel = onCancelEditor,
                                    onConfirm = onCloseEditor
                                )
                            } else {
                                Text(text = alternative.name, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = "${alternative.targetSets} series · ${alternative.targetRepsText} reps",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    TextButton(onClick = { onEditAlternative(index) }) {
                                        Text("Editar")
                                    }
                                    TextButton(onClick = { onSetAlternativeAsDefault(alternative.variantKey) }) {
                                        Text(
                                            if (exercise.defaultVariantKey == alternative.variantKey) {
                                                "Predeterminada"
                                            } else {
                                                "Usar por defecto"
                                            }
                                        )
                                    }
                                    TextButton(onClick = { onRemoveAlternative(index) }) {
                                        Text("Eliminar")
                                    }
                                }
                            }
                        }
                    }
                }

                FitTrackTonalButton(
                    label = "Crear alternativa",
                    onClick = onStartCreateAlternative,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        actions = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
private fun RoutineListItemSkeleton() {
    SkeletonCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(FitSpacing.sm)) {
            SkeletonText(widthFraction = 0.6f, lineHeight = 20.dp)
            SkeletonText(widthFraction = 0.4f)
            SkeletonBlock(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                shape = MaterialTheme.shapes.medium
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
