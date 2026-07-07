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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alvarocervantes.fittrackplus.feature.routines.RoutineEditorUiState
import com.alvarocervantes.fittrackplus.feature.routines.RoutineListItemUiState
import com.alvarocervantes.fittrackplus.feature.routines.RoutinesViewModel
import com.alvarocervantes.fittrackplus.grit.components.GritBadge
import com.alvarocervantes.fittrackplus.grit.components.GritCard
import com.alvarocervantes.fittrackplus.grit.components.GritOutlineButton
import com.alvarocervantes.fittrackplus.grit.components.GritPrimaryButton
import com.alvarocervantes.fittrackplus.grit.components.GritScreenHeader
import com.alvarocervantes.fittrackplus.grit.components.GritSectionLabel
import com.alvarocervantes.fittrackplus.grit.components.GritToast
import com.alvarocervantes.fittrackplus.grit.theme.GritColors
import com.alvarocervantes.fittrackplus.grit.theme.GritShapes
import com.alvarocervantes.fittrackplus.grit.theme.GritType

@Composable
fun GritRoutinesScreen(
    viewModel: RoutinesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activeRoutine = uiState.routines.firstOrNull { it.isActive }
    val otherRoutines = uiState.routines.filterNot { it.isActive }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            GritScreenHeader(title = "Mis Rutinas")

            CreateRoutineButton(onClick = viewModel::startCreateRoutine)

            if (activeRoutine != null) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    GritSectionLabel(text = "Fase Actual", color = GritColors.Lime)
                    ActiveRoutineCard(
                        routine = activeRoutine,
                        onEdit = { viewModel.startEditRoutine(activeRoutine.id) }
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    GritSectionLabel(text = "Otras Configuraciones")
                    GritSectionLabel(text = "${otherRoutines.size} TOTAL")
                }
                if (otherRoutines.isEmpty()) {
                    Text(
                        text = "No hay otras rutinas configuradas. ¡Crea una nueva arriba!",
                        style = GritType.monoBody,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    otherRoutines.forEach { routine ->
                        SecondaryRoutineCard(
                            routine = routine,
                            onActivate = { viewModel.setActiveRoutine(routine.id) },
                            onArchive = { viewModel.archiveRoutine(routine.id) },
                            onEdit = { viewModel.startEditRoutine(routine.id) }
                        )
                    }
                }
            }

            ArchivedSection(
                archivedRoutines = uiState.archivedRoutines,
                showArchived = uiState.showArchived,
                onToggleShow = { viewModel.setShowArchived(!uiState.showArchived) },
                onRestore = viewModel::restoreRoutine
            )
        }

        uiState.message?.let { message ->
            GritToast(
                title = "Rutinas",
                message = message,
                onDismiss = viewModel::clearMessage,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
    }

    uiState.editor?.let { editor ->
        GritRoutineEditorDialog(
            editor = editor,
            isSaving = uiState.isSaving,
            viewModel = viewModel
        )
    }
}

@Composable
private fun CreateRoutineButton(onClick: () -> Unit) {
    GritCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                GritSectionLabel(text = "Crear Sistema")
                Text(text = "NUEVA RUTINA", style = GritType.cardTitle)
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(GritShapes.small)
                    .background(GritColors.Lime),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Crear rutina",
                    tint = GritColors.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun ActiveRoutineCard(
    routine: RoutineListItemUiState,
    onEdit: () -> Unit
) {
    GritCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    GritBadge(text = "Activa", filled = true)
                    Text(text = routine.name.uppercase(), style = GritType.cardTitle)
                }
                Icon(
                    imageVector = Icons.Filled.Bolt,
                    contentDescription = null,
                    tint = GritColors.Lime.copy(alpha = 0.4f),
                    modifier = Modifier.size(32.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(GritShapes.small)
                        .background(GritColors.Background)
                        .border(1.dp, GritColors.Border, GritShapes.small)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    GritSectionLabel(text = "Días de entreno")
                    Text(text = "${routine.dayCount}", style = GritType.itemTitle)
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(GritShapes.small)
                        .background(GritColors.Background)
                        .border(1.dp, GritColors.Border, GritShapes.small)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    GritSectionLabel(text = "Estado")
                    Text(text = "EN USO", style = GritType.itemTitle, color = GritColors.Lime)
                }
            }

            GritOutlineButton(
                text = "Detalles de carga",
                onClick = onEdit,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SecondaryRoutineCard(
    routine: RoutineListItemUiState,
    onActivate: () -> Unit,
    onArchive: () -> Unit,
    onEdit: () -> Unit
) {
    GritCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = routine.name.uppercase(), style = GritType.itemTitle)
                GritSectionLabel(text = "${routine.dayCount} DÍAS")
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GritOutlineButton(text = "Activar", onClick = onActivate)
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Editar rutina",
                    tint = GritColors.TextSecondary,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable(onClick = onEdit)
                )
                Icon(
                    imageVector = Icons.Filled.Archive,
                    contentDescription = "Archivar rutina",
                    tint = GritColors.TextFaint,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable(onClick = onArchive)
                )
            }
        }
    }
}

@Composable
private fun ArchivedSection(
    archivedRoutines: List<RoutineListItemUiState>,
    showArchived: Boolean,
    onToggleShow: () -> Unit,
    onRestore: (Long) -> Unit
) {
    if (archivedRoutines.isEmpty() && !showArchived) return
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggleShow),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            GritSectionLabel(text = "Archivadas")
            GritSectionLabel(
                text = if (showArchived) "Ocultar" else "Mostrar",
                color = GritColors.Lime
            )
        }
        if (showArchived) {
            archivedRoutines.forEach { routine ->
                GritCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = routine.name.uppercase(),
                            style = GritType.itemTitle,
                            color = GritColors.TextSecondary,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Filled.Unarchive,
                            contentDescription = "Restaurar rutina",
                            tint = GritColors.Lime,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { onRestore(routine.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GritRoutineEditorDialog(
    editor: RoutineEditorUiState,
    isSaving: Boolean,
    viewModel: RoutinesViewModel
) {
    Dialog(
        onDismissRequest = viewModel::requestCloseEditor,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = GritShapes.medium,
            color = GritColors.Surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, GritColors.Border)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = editor.title.uppercase(), style = GritType.cardTitle)
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Cerrar editor",
                        tint = GritColors.TextSecondary,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable(onClick = viewModel::requestCloseEditor)
                    )
                }

                GritTextField(
                    label = "Nombre de rutina",
                    value = editor.name,
                    onValueChange = viewModel::updateRoutineName,
                    placeholder = "Ej: EMPUJE / TIRÓN / PIERNA"
                )

                editor.days.forEachIndexed { dayIndex, day ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(GritShapes.small)
                            .background(GritColors.Background)
                            .border(1.dp, GritColors.Border, GritShapes.small)
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GritSectionLabel(text = "Día ${dayIndex + 1}", color = GritColors.Lime)
                            if (editor.days.size > 1) {
                                GritSectionLabel(
                                    text = "Eliminar día",
                                    color = GritColors.TextFaint,
                                    modifier = Modifier.clickable { viewModel.removeDay(dayIndex) }
                                )
                            }
                        }
                        GritTextField(
                            label = "Nombre del día",
                            value = day.name,
                            onValueChange = { viewModel.updateDayName(dayIndex, it) }
                        )

                        day.exercises.forEachIndexed { exerciseIndex, exercise ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(GritShapes.small)
                                    .background(GritColors.Surface)
                                    .border(1.dp, GritColors.Border, GritShapes.small)
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
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
                                        GritSectionLabel(text = "Ejercicio ${exerciseIndex + 1}")
                                    }
                                    if (day.exercises.size > 1) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = "Eliminar ejercicio",
                                            tint = GritColors.TextFaint,
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clickable {
                                                    viewModel.removeExercise(dayIndex, exerciseIndex)
                                                }
                                        )
                                    }
                                }
                                GritTextField(
                                    label = "Nombre",
                                    value = exercise.name,
                                    onValueChange = {
                                        viewModel.updateExerciseName(dayIndex, exerciseIndex, it)
                                    },
                                    placeholder = "Ej: SENTADILLA BÚLGARA"
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    GritTextField(
                                        label = "Series",
                                        value = exercise.targetSets,
                                        onValueChange = {
                                            viewModel.updateExerciseSets(dayIndex, exerciseIndex, it)
                                        },
                                        modifier = Modifier.weight(1f),
                                        keyboardType = KeyboardType.Number
                                    )
                                    GritTextField(
                                        label = "Reps objetivo",
                                        value = exercise.targetRepsText,
                                        onValueChange = {
                                            viewModel.updateExerciseReps(dayIndex, exerciseIndex, it)
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        GritOutlineButton(
                            text = "+ Añadir ejercicio",
                            onClick = { viewModel.addExercise(dayIndex) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                GritOutlineButton(
                    text = "+ Añadir día",
                    onClick = viewModel::addDay,
                    modifier = Modifier.fillMaxWidth(),
                    accentColor = GritColors.TextSecondary
                )

                editor.validationMessage?.let { validation ->
                    Text(
                        text = validation,
                        style = GritType.monoLabelSmall,
                        color = GritColors.RedBorder
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GritOutlineButton(
                        text = "Cancelar",
                        onClick = viewModel::requestCloseEditor,
                        modifier = Modifier.weight(1f),
                        accentColor = GritColors.TextSecondary
                    )
                    GritPrimaryButton(
                        text = if (isSaving) "Guardando…" else "Guardar rutina",
                        onClick = viewModel::saveEditor,
                        modifier = Modifier.weight(1f),
                        enabled = editor.canSave && !isSaving
                    )
                }
            }
        }
    }

    if (editor.showCloseConfirmation) {
        Dialog(onDismissRequest = { viewModel.resolveCloseConfirmation(false) }) {
            Surface(
                shape = GritShapes.medium,
                color = GritColors.Surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, GritColors.Border)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(text = "¿DESCARTAR CAMBIOS?", style = GritType.cardTitle)
                    Text(
                        text = "Hay cambios sin guardar en la rutina.",
                        style = GritType.monoBody
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        GritOutlineButton(
                            text = "Seguir editando",
                            onClick = { viewModel.resolveCloseConfirmation(false) },
                            modifier = Modifier.weight(1f),
                            accentColor = GritColors.TextSecondary
                        )
                        GritOutlineButton(
                            text = "Descartar",
                            onClick = { viewModel.resolveCloseConfirmation(true) },
                            modifier = Modifier.weight(1f),
                            accentColor = GritColors.RedBorder
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun GritTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        GritSectionLabel(text = label)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = GritType.monoStrong,
            singleLine = true,
            shape = GritShapes.small,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            placeholder = placeholder?.let {
                { Text(text = it, style = GritType.monoBody, color = GritColors.TextFaint) }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GritColors.Lime,
                unfocusedBorderColor = GritColors.Border,
                focusedTextColor = GritColors.TextPrimary,
                unfocusedTextColor = GritColors.TextPrimary,
                cursorColor = GritColors.Lime,
                focusedContainerColor = GritColors.Background,
                unfocusedContainerColor = GritColors.Background
            )
        )
    }
}
