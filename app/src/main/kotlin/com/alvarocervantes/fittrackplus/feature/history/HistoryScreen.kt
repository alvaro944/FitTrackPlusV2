package com.alvarocervantes.fittrackplus.feature.history

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alvarocervantes.fittrackplus.core.design.FitSpacing
import com.alvarocervantes.fittrackplus.core.design.FitTrackBadge
import com.alvarocervantes.fittrackplus.core.design.FitTrackBadgeTone
import com.alvarocervantes.fittrackplus.core.design.FitTrackCard
import com.alvarocervantes.fittrackplus.core.design.FitTrackConfirmDialog
import com.alvarocervantes.fittrackplus.core.design.FitTrackEmptyState
import com.alvarocervantes.fittrackplus.core.design.FitTrackDropdownField
import com.alvarocervantes.fittrackplus.core.design.FitTrackEntityListCard
import com.alvarocervantes.fittrackplus.core.design.FitTrackEntityListCardBadge
import com.alvarocervantes.fittrackplus.core.design.FitTrackMetric
import com.alvarocervantes.fittrackplus.core.design.components.SkeletonBlock
import com.alvarocervantes.fittrackplus.core.design.components.SkeletonCard
import com.alvarocervantes.fittrackplus.core.design.components.SkeletonText
import com.alvarocervantes.fittrackplus.core.design.FitTrackMetricAccent
import com.alvarocervantes.fittrackplus.core.design.FitTrackKeyValueRow
import com.alvarocervantes.fittrackplus.core.design.FitTrackKeyValueRowStyle
import com.alvarocervantes.fittrackplus.core.design.FitTrackScreenHeader
import com.alvarocervantes.fittrackplus.core.design.FitTrackSectionLabel
import com.alvarocervantes.fittrackplus.core.design.FitTrackSetRow
import com.alvarocervantes.fittrackplus.core.design.FitTrackSetRowEditFieldStyle
import com.alvarocervantes.fittrackplus.core.design.FitTrackSetRowMode
import com.alvarocervantes.fittrackplus.core.design.surfaceAlt
import com.alvarocervantes.fittrackplus.domain.model.WorkoutHistoryDeltaDirection
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    onGoToWorkout: () -> Unit = {},
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    state.message?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.recoveredSessionEvent.collect { onGoToWorkout() }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        HistoryContent(
            state = state,
            contentPadding = padding,
            onSessionClick = viewModel::selectSession,
            onBackToList = viewModel::requestBackToList,
            onPeriodFilterChange = viewModel::setPeriodFilter,
            onSortOrderChange = viewModel::setSortOrder,
            onRoutineFilterChange = viewModel::setRoutineFilter,
            onToggleEditMode = viewModel::toggleEditMode,
            onSetWeightChange = viewModel::updateSetWeight,
            onSetRepsChange = viewModel::updateSetReps,
            onConfirmSaveChanges = viewModel::confirmSaveChanges,
            onConfirmDiscardChanges = viewModel::confirmDiscardChanges,
            onCancelPendingEditExit = viewModel::cancelPendingEditExit,
            onRecoverSession = viewModel::recoverSession
        )
    }
}

@Composable
private fun HistoryContent(
    state: HistoryUiState,
    contentPadding: PaddingValues,
    onSessionClick: (Long) -> Unit,
    onBackToList: () -> Unit,
    onPeriodFilterChange: (HistoryPeriodFilter) -> Unit,
    onSortOrderChange: (HistorySortOrder) -> Unit,
    onRoutineFilterChange: (String?) -> Unit,
    onToggleEditMode: () -> Unit,
    onSetWeightChange: (Long, String) -> Unit,
    onSetRepsChange: (Long, String) -> Unit,
    onConfirmSaveChanges: () -> Unit,
    onConfirmDiscardChanges: () -> Unit,
    onCancelPendingEditExit: () -> Unit,
    onRecoverSession: () -> Unit
) {
    val showingDetail = state.selectedSessionId != null || state.isDetailLoading

    BackHandler(enabled = showingDetail) {
        onBackToList()
    }

    AnimatedContent(
        targetState = showingDetail,
        transitionSpec = {
            fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200))
        },
        label = "history_content"
    ) { isDetailVisible ->
        if (isDetailVisible) {
            HistoryDetailContent(
                state = state,
                contentPadding = contentPadding,
                onBackToList = onBackToList,
                onToggleEditMode = onToggleEditMode,
                onSetWeightChange = onSetWeightChange,
                onSetRepsChange = onSetRepsChange,
                onConfirmSaveChanges = onConfirmSaveChanges,
                onConfirmDiscardChanges = onConfirmDiscardChanges,
                onCancelPendingEditExit = onCancelPendingEditExit,
                onRecoverSession = onRecoverSession
            )
        } else {
            HistoryListContent(
                state = state,
                contentPadding = contentPadding,
                onSessionClick = onSessionClick,
                onPeriodFilterChange = onPeriodFilterChange,
                onSortOrderChange = onSortOrderChange,
                onRoutineFilterChange = onRoutineFilterChange
            )
        }
    }
}

@Composable
private fun HistoryListContent(
    state: HistoryUiState,
    contentPadding: PaddingValues,
    onSessionClick: (Long) -> Unit,
    onPeriodFilterChange: (HistoryPeriodFilter) -> Unit,
    onSortOrderChange: (HistorySortOrder) -> Unit,
    onRoutineFilterChange: (String?) -> Unit
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
        verticalArrangement = Arrangement.spacedBy(FitSpacing.lg)
    ) {
        item {
            FitTrackScreenHeader(
                title = "Historial",
                subtitle = "Sesiones finalizadas"
            )
        }

        if (state.allSessions.isNotEmpty()) {
            item {
                HistoryFilterControls(
                    selectedPeriod = state.selectedPeriod,
                    selectedSort = state.selectedSort,
                    selectedRoutineName = state.selectedRoutineName,
                    availableRoutineNames = state.availableRoutineNames,
                    onPeriodFilterChange = onPeriodFilterChange,
                    onSortOrderChange = onSortOrderChange,
                    onRoutineFilterChange = onRoutineFilterChange
                )
            }
        }

        when {
            state.isLoading -> {
                items(5) { HistorySessionCardSkeleton() }
            }

            state.allSessions.isEmpty() -> {
                item {
                    FitTrackEmptyState(
                        icon = Icons.Filled.History,
                        title = "Sin sesiones finalizadas",
                        message = "Finaliza un entrenamiento para verlo aqui.",
                        supporting = "El historial usa snapshots, asi que los cambios futuros en rutinas no modificaran estas sesiones."
                    )
                }
            }

            state.sessions.isEmpty() -> {
                item {
                    FitTrackEmptyState(
                        icon = Icons.Filled.History,
                        title = "Sin sesiones para este filtro",
                        message = "Cambia el periodo o la rutina para ver mas sesiones.",
                        supporting = "El historial completo sigue guardado."
                    )
                }
            }

            else -> {
                item {
                    FitTrackSectionLabel(label = "Sesiones")
                }
                items(
                    items = state.sessions,
                    key = { session -> session.sessionId }
                ) { session ->
                    HistorySessionCard(
                        session = session,
                        onClick = { onSessionClick(session.sessionId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryFilterControls(
    selectedPeriod: HistoryPeriodFilter,
    selectedSort: HistorySortOrder,
    selectedRoutineName: String?,
    availableRoutineNames: List<String>,
    onPeriodFilterChange: (HistoryPeriodFilter) -> Unit,
    onSortOrderChange: (HistorySortOrder) -> Unit,
    onRoutineFilterChange: (String?) -> Unit
) {
    FitTrackCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(FitSpacing.sm)) {
            FitTrackSectionLabel(label = "Periodo")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(FitSpacing.sm)
            ) {
                HistoryPeriodFilter.entries.forEach { period ->
                    FilterChip(
                        selected = selectedPeriod == period,
                        onClick = { onPeriodFilterChange(period) },
                        label = { Text(period.label) }
                    )
                }
            }
            FitTrackSectionLabel(label = "Orden")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(FitSpacing.sm)
            ) {
                HistorySortOrder.entries.forEach { sort ->
                    FilterChip(
                        selected = selectedSort == sort,
                        onClick = { onSortOrderChange(sort) },
                        label = { Text(sort.label) }
                    )
                }
            }
            FitTrackSectionLabel(label = "Rutina")
            RoutineFilterDropdown(
                selectedRoutineName = selectedRoutineName,
                availableRoutineNames = availableRoutineNames,
                onRoutineFilterChange = onRoutineFilterChange
            )
        }
    }
}

@Composable
private fun RoutineFilterDropdown(
    selectedRoutineName: String?,
    availableRoutineNames: List<String>,
    onRoutineFilterChange: (String?) -> Unit
) {
    val allRoutinesLabel = "Todas las rutinas"

    FitTrackDropdownField(
        label = "Rutina",
        value = selectedRoutineName ?: allRoutinesLabel,
        options = listOf<String?>(null) + availableRoutineNames,
        onSelect = onRoutineFilterChange,
        optionLabel = { it ?: allRoutinesLabel }
    )
}

@Composable
private fun HistoryDetailContent(
    state: HistoryUiState,
    contentPadding: PaddingValues,
    onBackToList: () -> Unit,
    onToggleEditMode: () -> Unit,
    onSetWeightChange: (Long, String) -> Unit,
    onSetRepsChange: (Long, String) -> Unit,
    onConfirmSaveChanges: () -> Unit,
    onConfirmDiscardChanges: () -> Unit,
    onCancelPendingEditExit: () -> Unit,
    onRecoverSession: () -> Unit
) {
    val listState = rememberLazyListState()

    if (state.pendingEditExit != null) {
        FitTrackConfirmDialog(
            title = "Cambios sin guardar",
            text = "Has modificado datos de esta sesion. ¿Quieres guardarlos?",
            confirmLabel = "Guardar",
            dismissLabel = "Descartar",
            onConfirm = onConfirmSaveChanges,
            onDismiss = onConfirmDiscardChanges,
            onDismissRequest = onCancelPendingEditExit,
            destructive = false
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
            bottom = FitSpacing.screenBottom
        ),
        verticalArrangement = Arrangement.spacedBy(FitSpacing.lg)
    ) {
        item {
            FitTrackScreenHeader(
                title = "Historial",
                subtitle = if (state.isEditMode) "Editando series" else "Detalle historico",
                trailing = {
                    Row {
                        if (state.selectedDetail != null) {
                            IconButton(onClick = onToggleEditMode) {
                                Icon(
                                    imageVector = if (state.isEditMode) Icons.Filled.Check else Icons.Filled.Edit,
                                    contentDescription = if (state.isEditMode) {
                                        "Terminar edicion de series"
                                    } else {
                                        "Editar series"
                                    }
                                )
                            }
                        }
                        IconButton(onClick = onBackToList) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Volver al listado de historial"
                            )
                        }
                    }
                }
            )
        }

        when {
            state.isDetailLoading -> {
                item { HistoryDetailSummarySkeleton() }
                item { HistoryComparisonSkeleton() }
            }

            state.selectedDetail != null -> {
                item {
                    HistoryDetailSummary(detail = state.selectedDetail)
                }
                if (!state.selectedDetail.isComplete) {
                    item {
                        HistoryIncompleteCard(onRecoverSession = onRecoverSession)
                    }
                }
                item {
                    HistoryComparisonCard(comparison = state.selectedDetail.comparison)
                }
                item {
                    FitTrackSectionLabel(label = "Ejercicios")
                }
                items(
                    items = state.selectedDetail.exercises,
                    key = { exercise -> exercise.exerciseId }
                ) { exercise ->
                    HistoryExerciseCard(
                        exercise = exercise,
                        isEditMode = state.isEditMode,
                        onSetWeightChange = onSetWeightChange,
                        onSetRepsChange = onSetRepsChange
                    )
                }
            }

            else -> {
                item { HistoryDetailSummarySkeleton() }
                item { HistoryComparisonSkeleton() }
            }
        }
    }
}

@Composable
private fun HistorySessionCard(
    session: HistorySessionUiState,
    onClick: () -> Unit
) {
    FitTrackEntityListCard(
        title = session.routineName,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                role = Role.Button,
                onClickLabel = "Ver detalle de la sesion",
                onClick = onClick
            ),
        leadingDot = if (!session.isComplete) MaterialTheme.colorScheme.error else null,
        leadingDotContentDescription = if (!session.isComplete) "Entrenamiento incompleto" else null,
        badge = FitTrackEntityListCardBadge(
            text = "Semana ${session.weekNumber}",
            tone = FitTrackBadgeTone.Neutral
        ),
        metaContent = {
            Text(
                text = session.dayName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formatDate(session.startedAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${session.totalVolumeKg.toDisplayText()} kg - " +
                    "${session.setCount} series - " +
                    formatDuration(session.durationMillis),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}

@Composable
private fun HistoryIncompleteCard(onRecoverSession: () -> Unit) {
    FitTrackCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(FitSpacing.sm)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(FitSpacing.sm)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(MaterialTheme.colorScheme.error, CircleShape)
                )
                Text(
                    text = "Entrenamiento incompleto",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Text(
                text = "Quedaron series sin completar. Puedes recuperarlo y seguir donde lo dejaste.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onRecoverSession) {
                Text("Recuperar entrenamiento")
            }
        }
    }
}

@Composable
private fun HistoryDetailSummary(detail: HistoryDetailUiState) {
    FitTrackCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = detail.routineName,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = detail.dayName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(FitSpacing.xl)
        ) {
            FitTrackMetric(
                value = detail.exercises.size.toString(),
                label = "ejercicios",
                accent = FitTrackMetricAccent.Primary,
                compact = true
            )
            FitTrackMetric(
                value = detail.totalSetCount.toString(),
                label = "series",
                compact = true
            )
        }
        FitTrackKeyValueRow(
            label = "Duracion",
            value = formatDuration(detail.durationMillis),
            style = FitTrackKeyValueRowStyle.Flat
        )
        FitTrackKeyValueRow(
            label = "Volumen total",
            value = "${detail.totalVolumeKg.toDisplayText()} kg",
            style = FitTrackKeyValueRowStyle.Flat
        )
        detail.bestSet?.let { bestSet ->
            FitTrackKeyValueRow(
                label = "Mejor set",
                value = "${bestSet.exerciseName} · ${bestSet.weightKg.toDisplayText()} kg x ${bestSet.reps}",
                style = FitTrackKeyValueRowStyle.Flat
            )
        }
        detail.notes?.takeIf { it.isNotBlank() }?.let { notes ->
            FitTrackKeyValueRow(
                label = "Notas",
                value = notes,
                style = FitTrackKeyValueRowStyle.Flat
            )
        }
        FitTrackKeyValueRow(
            label = "Finalizada",
            value = formatDate(detail.finishedAt),
            style = FitTrackKeyValueRowStyle.Flat,
            labelTextStyle = MaterialTheme.typography.bodySmall,
            valueTextStyle = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun HistoryComparisonCard(comparison: HistoryComparisonUiState?) {
    FitTrackCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(FitSpacing.sm)) {
            Text(
                text = "Comparado con la anterior",
                style = MaterialTheme.typography.titleMedium
            )
            if (comparison == null) {
                Text(
                    text = "Primera sesion comparable",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "Anterior: ${formatDate(comparison.previousFinishedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HistoryDeltaRow(
                    label = "Volumen",
                    currentText = "${comparison.totalVolumeDelta.currentValue.toDisplayText()} kg",
                    delta = comparison.totalVolumeDelta,
                    deltaText = "${comparison.totalVolumeDelta.deltaValue.toSignedDisplayText()} kg"
                )
                HistoryDeltaRow(
                    label = "Duracion",
                    currentText = formatDuration(comparison.durationMillisDelta.currentValue.toLong()),
                    delta = comparison.durationMillisDelta,
                    deltaText = comparison.durationMillisDelta.deltaValue.toDurationDeltaText()
                )
                HistoryDeltaRow(
                    label = "Series",
                    currentText = comparison.setCountDelta.currentValue.toInt().toString(),
                    delta = comparison.setCountDelta,
                    deltaText = comparison.setCountDelta.deltaValue.toSignedIntText()
                )
                HistoryDeltaRow(
                    label = "Mejor set",
                    currentText = comparison.bestSet.current?.let { bestSet ->
                        "${bestSet.exerciseName}: ${bestSet.weightKg.toDisplayText()} kg x ${bestSet.reps}"
                    } ?: "Sin datos",
                    delta = comparison.bestSet.delta,
                    deltaText = "${comparison.bestSet.delta.deltaValue.toSignedDisplayText()} kg"
                )
            }
        }
    }
}

@Composable
private fun HistoryDeltaRow(
    label: String,
    currentText: String,
    delta: HistoryMetricDeltaUiState,
    deltaText: String
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
                text = label,
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = currentText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        FitTrackBadge(
            label = delta.direction.toDeltaLabel(deltaText),
            tone = when (delta.direction) {
                WorkoutHistoryDeltaDirection.Up -> FitTrackBadgeTone.Active
                WorkoutHistoryDeltaDirection.Down -> FitTrackBadgeTone.Warm
                WorkoutHistoryDeltaDirection.Same,
                WorkoutHistoryDeltaDirection.Unavailable -> FitTrackBadgeTone.Neutral
            }
        )
    }
}

@Composable
private fun HistoryExerciseCard(
    exercise: HistoryExerciseUiState,
    isEditMode: Boolean,
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
                HistorySetRow(
                    set = set,
                    isEditMode = isEditMode,
                    onWeightChange = onSetWeightChange,
                    onRepsChange = onSetRepsChange
                )
            }
        }
    }
}

@Composable
private fun HistorySetRow(
    set: HistorySetUiState,
    isEditMode: Boolean,
    onWeightChange: (Long, String) -> Unit,
    onRepsChange: (Long, String) -> Unit
) {
    FitTrackSetRow(
        setId = set.setId,
        setNumber = set.setNumber,
        weightText = if (isEditMode) set.weightText else "${set.weightKg.toDisplayText()} kg",
        repsText = if (isEditMode) set.repsText else "${set.reps} reps",
        mode = if (isEditMode) FitTrackSetRowMode.Edit else FitTrackSetRowMode.ReadOnly,
        notes = set.notes,
        editFieldStyle = FitTrackSetRowEditFieldStyle.TextField,
        onWeightChange = { onWeightChange(set.setId, it) },
        onRepsChange = { onRepsChange(set.setId, it) }
    )
}

private fun formatDate(timestamp: Long): String {
    return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))
}

private fun formatDuration(durationMillis: Long): String {
    val totalMinutes = durationMillis / 60_000
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) {
        "${hours}h ${minutes}min"
    } else {
        "${minutes}min"
    }
}

private fun WorkoutHistoryDeltaDirection.toDeltaLabel(deltaText: String): String {
    return when (this) {
        WorkoutHistoryDeltaDirection.Up -> if (deltaText.startsWith("+")) deltaText else "+$deltaText"
        WorkoutHistoryDeltaDirection.Down -> deltaText
        WorkoutHistoryDeltaDirection.Same -> "Igual"
        WorkoutHistoryDeltaDirection.Unavailable -> "Sin datos"
    }
}

private fun Double.toSignedDisplayText(): String {
    val absolute = kotlin.math.abs(this).toDisplayText()
    return if (this < 0.0) {
        "-$absolute"
    } else {
        absolute
    }
}

private fun Double.toSignedIntText(): String {
    val rounded = toInt()
    return when {
        rounded > 0 -> "+$rounded"
        rounded < 0 -> rounded.toString()
        else -> "0"
    }
}

@Composable
private fun HistorySessionCardSkeleton() {
    SkeletonCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(FitSpacing.sm)) {
            SkeletonText(widthFraction = 0.55f, lineHeight = 18.dp)
            SkeletonText(widthFraction = 0.35f)
            SkeletonBlock(
                modifier = Modifier
                    .fillMaxWidth(0.3f)
                    .height(20.dp),
                shape = MaterialTheme.shapes.small
            )
        }
    }
}

@Composable
private fun HistoryDetailSummarySkeleton() {
    SkeletonCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(FitSpacing.sm)) {
            SkeletonText(widthFraction = 0.6f, lineHeight = 18.dp)
            SkeletonText(widthFraction = 0.45f)
            SkeletonText(widthFraction = 0.55f)
            SkeletonText(widthFraction = 0.3f)
        }
    }
}

@Composable
private fun HistoryComparisonSkeleton() {
    SkeletonCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(FitSpacing.sm)) {
            repeat(4) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SkeletonBlock(
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .height(14.dp),
                        shape = MaterialTheme.shapes.small
                    )
                    SkeletonBlock(
                        modifier = Modifier
                            .fillMaxWidth(0.3f)
                            .height(14.dp),
                        shape = MaterialTheme.shapes.small
                    )
                }
            }
        }
    }
}

private fun Double.toDurationDeltaText(): String {
    val sign = if (this < 0.0) "-" else "+"
    val text = formatDuration(kotlin.math.abs(this).toLong())
    return "$sign$text"
}

private fun Double.toDisplayText(): String {
    return if (this % 1.0 == 0.0) {
        toInt().toString()
    } else {
        String.format(Locale.getDefault(), "%.1f", this)
    }
}
