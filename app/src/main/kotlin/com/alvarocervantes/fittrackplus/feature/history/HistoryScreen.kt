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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.semantics.Role
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
import com.alvarocervantes.fittrackplus.core.design.FitTrackMetric
import com.alvarocervantes.fittrackplus.core.design.components.SkeletonBlock
import com.alvarocervantes.fittrackplus.core.design.components.SkeletonCard
import com.alvarocervantes.fittrackplus.core.design.components.SkeletonText
import com.alvarocervantes.fittrackplus.core.design.FitTrackMetricAccent
import com.alvarocervantes.fittrackplus.core.design.FitTrackScreenHeader
import com.alvarocervantes.fittrackplus.core.design.FitTrackSectionLabel
import com.alvarocervantes.fittrackplus.core.design.surfaceAlt
import com.alvarocervantes.fittrackplus.domain.model.WorkoutHistoryDeltaDirection
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
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
            onToggleEditMode = viewModel::toggleEditMode,
            onSetWeightChange = viewModel::updateSetWeight,
            onSetRepsChange = viewModel::updateSetReps,
            onConfirmSaveChanges = viewModel::confirmSaveChanges,
            onConfirmDiscardChanges = viewModel::confirmDiscardChanges,
            onCancelPendingEditExit = viewModel::cancelPendingEditExit
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
    onToggleEditMode: () -> Unit,
    onSetWeightChange: (Long, String) -> Unit,
    onSetRepsChange: (Long, String) -> Unit,
    onConfirmSaveChanges: () -> Unit,
    onConfirmDiscardChanges: () -> Unit,
    onCancelPendingEditExit: () -> Unit
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
                onCancelPendingEditExit = onCancelPendingEditExit
            )
        } else {
            HistoryListContent(
                state = state,
                contentPadding = contentPadding,
                onSessionClick = onSessionClick,
                onPeriodFilterChange = onPeriodFilterChange,
                onSortOrderChange = onSortOrderChange
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
    onSortOrderChange: (HistorySortOrder) -> Unit
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
                    onPeriodFilterChange = onPeriodFilterChange,
                    onSortOrderChange = onSortOrderChange
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
                        message = "Cambia el periodo o el orden para ver mas sesiones.",
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
    onPeriodFilterChange: (HistoryPeriodFilter) -> Unit,
    onSortOrderChange: (HistorySortOrder) -> Unit
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
        }
    }
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
    onCancelPendingEditExit: () -> Unit
) {
    val listState = rememberLazyListState()
    val allowFieldFocus = !listState.isScrollInProgress

    if (state.pendingEditExit != null) {
        AlertDialog(
            onDismissRequest = onCancelPendingEditExit,
            title = { Text("Cambios sin guardar") },
            text = { Text("Has modificado datos de esta sesion. ¿Quieres guardarlos?") },
            confirmButton = {
                TextButton(onClick = onConfirmSaveChanges) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = onConfirmDiscardChanges) {
                    Text("Descartar")
                }
            }
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
                        allowFieldFocus = allowFieldFocus,
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
    FitTrackCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                role = Role.Button,
                onClickLabel = "Ver detalle de la sesion",
                onClick = onClick
            )
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
                    text = session.routineName,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = session.dayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatDate(session.finishedAt),
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
            FitTrackBadge(
                label = "Semana ${session.weekNumber}",
                tone = FitTrackBadgeTone.Neutral
            )
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
        Text(
            text = "Duracion: ${formatDuration(detail.durationMillis)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Volumen total: ${detail.totalVolumeKg.toDisplayText()} kg",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        detail.bestSet?.let { bestSet ->
            Text(
                text = "Mejor set: ${bestSet.exerciseName} · ${bestSet.weightKg.toDisplayText()} kg x ${bestSet.reps}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        detail.notes?.takeIf { it.isNotBlank() }?.let { notes ->
            Text(
                text = "Notas: $notes",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "Finalizada ${formatDate(detail.finishedAt)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
    allowFieldFocus: Boolean,
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
                    allowFieldFocus = allowFieldFocus,
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
    allowFieldFocus: Boolean,
    onWeightChange: (Long, String) -> Unit,
    onRepsChange: (Long, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceAlt, MaterialTheme.shapes.large)
            .padding(FitSpacing.smMd),
        verticalArrangement = Arrangement.spacedBy(FitSpacing.xs)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(FitSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = set.setNumber.toString(),
                    style = MaterialTheme.typography.labelMedium
                )
            }
            if (isEditMode) {
                OutlinedTextField(
                    value = set.weightText,
                    onValueChange = { onWeightChange(set.setId, it) },
                    label = { Text("kg") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .weight(1f)
                        .focusProperties { canFocus = allowFieldFocus }
                )
                OutlinedTextField(
                    value = set.repsText,
                    onValueChange = { onRepsChange(set.setId, it) },
                    label = { Text("reps") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .focusProperties { canFocus = allowFieldFocus }
                )
            } else {
                Text(
                    text = "${set.weightKg.toDisplayText()} kg",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${set.reps} reps",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        set.notes?.takeIf { it.isNotBlank() }?.let { notes ->
            Text(
                text = notes,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
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
