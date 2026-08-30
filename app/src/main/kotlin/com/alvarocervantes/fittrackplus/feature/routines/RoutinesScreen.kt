package com.alvarocervantes.fittrackplus.feature.routines

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
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
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
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
import com.alvarocervantes.fittrackplus.core.design.FitTrackIconBadgeSize
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

private const val MAX_NAME_LENGTH = 60
private const val MAX_NOTES_LENGTH = 500

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
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        appShellViewModel.activeTabReselected.collect { route ->
            if (route == AppRoute.Routines && state.editor == null) {
                listState.animateScrollToItem(0)
            }
        }
    }

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
                listState = listState,
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
    listState: LazyListState,
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
        state = listState,
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
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
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
    var dayPendingRemoval by remember { mutableStateOf<Int?>(null) }

    dayPendingRemoval?.let { dayIndex ->
        FitTrackConfirmDialog(
            title = "Eliminar dia",
            text = "Se eliminara el dia y todos sus ejercicios. Esta accion no se puede deshacer.",
            confirmLabel = "Eliminar",
            dismissLabel = "Cancelar",
            onConfirm = {
                onRemoveDay(dayIndex)
                dayPendingRemoval = null
            },
            onDismiss = { dayPendingRemoval = null },
            destructive = true
        )
    }

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
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done
                    ),
                    maxLength = MAX_NAME_LENGTH,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item {
            FitTrackSectionLabel(label = "Dias")
        }

        itemsIndexed(editor.days, key = { _, day -> day.draftId }) { dayIndex, day ->
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
                onRemoveDay = { dayPendingRemoval = it },
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
                label = "Añadir dia",
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
            // The card is already the day's surface: no nested filled block, so the concentric
            // radius rule holds and expansion is signalled by the chevron alone.
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        onClickLabel = if (isExpanded) {
                            "Colapsar dia ${dayIndex + 1}"
                        } else {
                            "Expandir dia ${dayIndex + 1}"
                        },
                        role = Role.Button,
                        onClick = { onToggleExpanded(dayIndex) }
                    ),
                horizontalArrangement = Arrangement.spacedBy(FitSpacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FitTrackIconBadge(
                    variant = FitTrackIconBadgeVariant.Number("${dayIndex + 1}"),
                    tone = FitTrackIconBadgeTone.Soft
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(FitSpacing.tiny)
                ) {
                    Text(
                        // titleMedium, a step below the routine name that owns this editor.
                        text = day.name.ifBlank { "Dia ${dayIndex + 1}" },
                        style = MaterialTheme.typography.titleMedium,
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
                // Decorative: the whole header row is the control, and it carries the label and
                // role. A nested IconButton here would be a second click target saying the same
                // thing twice to TalkBack.
                Icon(
                    imageVector = if (isExpanded) {
                        Icons.Filled.KeyboardArrowUp
                    } else {
                        Icons.Filled.KeyboardArrowDown
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
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

            var awaitingNewExerciseFocus by remember { mutableStateOf(false) }
            var focusExerciseDraftId by remember { mutableStateOf<String?>(null) }

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
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusExerciseDraftId = day.exercises.firstOrNull()?.draftId }
                ),
                maxLength = MAX_NAME_LENGTH,
                modifier = Modifier.fillMaxWidth()
            )

            LaunchedEffect(day.exercises.size) {
                if (awaitingNewExerciseFocus) {
                    focusExerciseDraftId = day.exercises.lastOrNull()?.draftId
                    awaitingNewExerciseFocus = false
                }
            }

            day.exercises.forEachIndexed { exerciseIndex, exercise ->
                key(exercise.draftId) {
                    RoutineExerciseEditor(
                    dayIndex = dayIndex,
                    exerciseIndex = exerciseIndex,
                    exercise = exercise,
                    canRemove = day.exercises.size > 1,
                    canMoveUp = exerciseIndex > 0,
                    canMoveDown = exerciseIndex < day.exercises.lastIndex,
                    requestNameFocus = exercise.draftId == focusExerciseDraftId,
                    onNameFocusRequested = { focusExerciseDraftId = null },
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
            }

            FitTrackAddButton(
                label = "Añadir ejercicio",
                onClick = {
                    awaitingNewExerciseFocus = true
                    onAddExercise(dayIndex)
                },
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
    requestNameFocus: Boolean,
    onNameFocusRequested: () -> Unit,
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
    var alternativePendingRemoval by remember { mutableStateOf<Int?>(null) }
    var showDeleteNoteConfirm by remember { mutableStateOf(false) }

    alternativePendingRemoval?.let { alternativeIndex ->
        FitTrackConfirmDialog(
            title = "Eliminar alternativa",
            text = "Se eliminara esta alternativa del ejercicio. Esta accion no se puede deshacer.",
            confirmLabel = "Eliminar",
            dismissLabel = "Cancelar",
            onConfirm = {
                onRemoveExerciseAlternative(dayIndex, exerciseIndex, alternativeIndex)
                if (editingAlternativeIndex == alternativeIndex) editingAlternativeIndex = null
                alternativePendingRemoval = null
            },
            onDismiss = { alternativePendingRemoval = null },
            destructive = true
        )
    }

    if (showNotesDialog) {
        FitTrackInputDialog(
            title = if (exercise.notes.isBlank()) "Añadir nota" else "Editar nota",
            value = notesDraft,
            onValueChange = { notesDraft = it },
            label = "Notas",
            singleLine = false,
            minLines = 3,
            maxLines = 5,
            maxLength = MAX_NOTES_LENGTH,
            confirmLabel = "Guardar",
            dismissLabel = "Cancelar",
            onConfirm = {
                onExerciseNotesChange(dayIndex, exerciseIndex, notesDraft)
                showNotesDialog = false
            },
            onDismiss = { showNotesDialog = false },
            extraContent = {
                if (exercise.notes.isNotBlank()) {
                    TextButton(onClick = { showDeleteNoteConfirm = true }) {
                        Text("Eliminar nota")
                    }
                }
            }
        )
    }

    if (showDeleteNoteConfirm) {
        FitTrackConfirmDialog(
            title = "Eliminar nota",
            text = "Se eliminara la nota de este ejercicio. Esta accion no se puede deshacer.",
            confirmLabel = "Eliminar",
            dismissLabel = "Cancelar",
            onConfirm = {
                onExerciseNotesChange(dayIndex, exerciseIndex, "")
                showDeleteNoteConfirm = false
                showNotesDialog = false
            },
            onDismiss = { showDeleteNoteConfirm = false },
            destructive = true
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
                alternativePendingRemoval = alternativeIndex
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

    // Surface + border like every other container in the app, instead of the one borderless
    // filled block it used to be.
    FitTrackCard(modifier = Modifier.fillMaxWidth()) {
      Column(verticalArrangement = Arrangement.spacedBy(FitSpacing.smMd)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // The ordinal as a badge rather than a bold "Ejercicio N" label competing with the
            // name field right below it.
            FitTrackIconBadge(
                variant = FitTrackIconBadgeVariant.Number("${exerciseIndex + 1}"),
                tone = FitTrackIconBadgeTone.Soft,
                size = FitTrackIconBadgeSize.Small
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

        val nameFocusRequester = remember { FocusRequester() }
        LaunchedEffect(requestNameFocus) {
            if (requestNameFocus) {
                nameFocusRequester.requestFocus()
                onNameFocusRequested()
            }
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
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done
            ),
            maxLength = MAX_NAME_LENGTH,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(nameFocusRequester)
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
                val baseIsDefault = exercise.defaultVariantKey == null ||
                    exercise.defaultVariantKey == exercise.variantKey
                FitTrackCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(FitSpacing.xs)) {
                        // Badge rather than a coloured border, matching how the workout tab marks
                        // the default variant.
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = exercise.name.ifBlank { "Ejercicio base" },
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                            if (baseIsDefault) {
                                FitTrackBadge(label = "PREDET.", tone = FitTrackBadgeTone.Active)
                            }
                        }
                        Text(
                            text = "${exercise.targetSets} series · ${exercise.targetRepsText} reps",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        // Only offered when it would change something: a button reading
                        // "Predeterminada" on the item that already is one looks tappable and is not.
                        if (!baseIsDefault) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TextButton(onClick = onSetBaseAsDefault) {
                                    Text("Usar por defecto")
                                }
                            }
                        }
                    }
                }

                exercise.alternatives.forEachIndexed { index, alternative ->
                    val isEditing = editingAlternativeIndex == index
                    val isDefault = exercise.defaultVariantKey == alternative.variantKey
                    FitTrackCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(FitSpacing.xs)) {
                            if (isEditing) {
                                val notesFocusRequester = remember { FocusRequester() }
                                FitTrackSelectAllTextField(
                                    value = alternative.name,
                                    onValueChange = { onAlternativeNameChange(index, it) },
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
                                    keyboardOptions = KeyboardOptions(
                                        capitalization = KeyboardCapitalization.Sentences,
                                        imeAction = ImeAction.Done
                                    ),
                                    maxLength = MAX_NOTES_LENGTH,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(notesFocusRequester)
                                )
                                FitTrackFormDialogActions(
                                    cancelLabel = "Cancelar",
                                    confirmLabel = "Guardar",
                                    onCancel = onCancelEditor,
                                    onConfirm = onCloseEditor
                                )
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = alternative.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (isDefault) {
                                        FitTrackBadge(label = "PREDET.", tone = FitTrackBadgeTone.Active)
                                    }
                                }
                                Text(
                                    text = "${alternative.targetSets} series · ${alternative.targetRepsText} reps",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                // FlowRow so three actions wrap instead of clipping off the
                                // leading edge on a narrow screen.
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = { onEditAlternative(index) }) {
                                        Text("Editar")
                                    }
                                    if (!isDefault) {
                                        TextButton(
                                            onClick = { onSetAlternativeAsDefault(alternative.variantKey) }
                                        ) {
                                            Text("Usar por defecto")
                                        }
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
                text = if (hasNote) "Editar nota" else "Añadir nota",
                modifier = Modifier.padding(start = FitSpacing.sm)
            )
        }
    }
}
