package com.alvarocervantes.fittrackplus.feature.history

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.lifecycle.ViewModelStoreOwner
import com.alvarocervantes.fittrackplus.core.navigation.AppRoute
import com.alvarocervantes.fittrackplus.core.navigation.AppShellViewModel
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.runtime.DisposableEffect
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
import com.alvarocervantes.fittrackplus.core.design.FitTrackDeltaDirection
import com.alvarocervantes.fittrackplus.core.design.FitTrackDeltaMeaning
import com.alvarocervantes.fittrackplus.core.design.fitTrackDeltaTone
import com.alvarocervantes.fittrackplus.core.design.FitTrackConfirmDialog
import com.alvarocervantes.fittrackplus.core.design.FitTrackEmptyState
import com.alvarocervantes.fittrackplus.core.design.FitTrackErrorState
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
import com.alvarocervantes.fittrackplus.domain.model.WeightUnit
import com.alvarocervantes.fittrackplus.domain.model.WorkoutStatsPeriod
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    onGoToWorkout: () -> Unit = {},
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val activity = LocalActivity.current
    val appShellOwner = requireNotNull(activity) as ViewModelStoreOwner
    val appShellViewModel: AppShellViewModel = hiltViewModel(appShellOwner)
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    state.message?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.recoveredSessionEvent.collect { onGoToWorkout() }
    }

    // Re-tapping the History tab while already on it should pop back to the list and scroll
    // it to the top, matching the standard re-tap pattern used by the other tabs.
    LaunchedEffect(Unit) {
        appShellViewModel.activeTabReselected.collect { route ->
            if (route == AppRoute.History) {
                viewModel.requestBackToList()
                listState.animateScrollToItem(0)
            }
        }
    }

    // The detail view puts its edit and delete actions in the header's trailing slot, the same
    // top-end corner the floating shell menu button occupies, so hide the menu while it is open.
    LaunchedEffect(state.selectedSessionId) {
        appShellViewModel.setMenuButtonHidden(
            route = AppRoute.History,
            hidden = state.selectedSessionId != null
        )
    }
    DisposableEffect(Unit) {
        onDispose {
            appShellViewModel.setMenuButtonHidden(AppRoute.History, hidden = false)
        }
    }

    Scaffold(
        // The app shell already applies the system bar insets; without this the
        // status bar padding lands twice and leaves a dead band above the content.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        HistoryContent(
            state = state,
            contentPadding = padding,
            listState = listState,
            onSessionClick = viewModel::selectSession,
            onBackToList = viewModel::requestBackToList,
            onPeriodFilterChange = viewModel::setPeriodFilter,
            onSortOrderChange = viewModel::setSortOrder,
            onRoutineFilterChange = viewModel::setRoutineFilter,
            onClearFilters = viewModel::clearFilters,
            onToggleEditMode = viewModel::toggleEditMode,
            onSetWeightChange = viewModel::updateSetWeight,
            onSetRepsChange = viewModel::updateSetReps,
            onConfirmSaveChanges = viewModel::confirmSaveChanges,
            onConfirmDiscardChanges = viewModel::confirmDiscardChanges,
            onCancelPendingEditExit = viewModel::cancelPendingEditExit,
            onRecoverSession = viewModel::recoverSession,
            onDeleteSession = viewModel::deleteSession
        )
    }
}

@Composable
private fun HistoryContent(
    state: HistoryUiState,
    contentPadding: PaddingValues,
    listState: LazyListState,
    onSessionClick: (Long) -> Unit,
    onBackToList: () -> Unit,
    onPeriodFilterChange: (WorkoutStatsPeriod) -> Unit,
    onSortOrderChange: (HistorySortOrder) -> Unit,
    onRoutineFilterChange: (String?) -> Unit,
    onClearFilters: () -> Unit,
    onToggleEditMode: () -> Unit,
    onSetWeightChange: (Long, String) -> Unit,
    onSetRepsChange: (Long, String) -> Unit,
    onConfirmSaveChanges: () -> Unit,
    onConfirmDiscardChanges: () -> Unit,
    onCancelPendingEditExit: () -> Unit,
    onRecoverSession: () -> Unit,
    onDeleteSession: () -> Unit
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
                onRecoverSession = onRecoverSession,
                onDeleteSession = onDeleteSession
            )
        } else {
            HistoryListContent(
                state = state,
                contentPadding = contentPadding,
                listState = listState,
                onSessionClick = onSessionClick,
                onPeriodFilterChange = onPeriodFilterChange,
                onSortOrderChange = onSortOrderChange,
                onRoutineFilterChange = onRoutineFilterChange,
                onClearFilters = onClearFilters
            )
        }
    }
}

@Composable
private fun HistoryListContent(
    state: HistoryUiState,
    contentPadding: PaddingValues,
    listState: LazyListState,
    onSessionClick: (Long) -> Unit,
    onPeriodFilterChange: (WorkoutStatsPeriod) -> Unit,
    onSortOrderChange: (HistorySortOrder) -> Unit,
    onRoutineFilterChange: (String?) -> Unit,
    onClearFilters: () -> Unit
) {
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
                    onRoutineFilterChange = onRoutineFilterChange,
                    onClearFilters = onClearFilters
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
                        weightUnit = state.weightUnit,
                        onClick = { onSessionClick(session.sessionId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryFilterControls(
    selectedPeriod: WorkoutStatsPeriod,
    selectedSort: HistorySortOrder,
    selectedRoutineName: String?,
    availableRoutineNames: List<String>,
    onPeriodFilterChange: (WorkoutStatsPeriod) -> Unit,
    onSortOrderChange: (HistorySortOrder) -> Unit,
    onRoutineFilterChange: (String?) -> Unit,
    onClearFilters: () -> Unit
) {
    val defaults = remember { HistoryUiState() }
    val hasActiveFilters = selectedPeriod != defaults.selectedPeriod ||
        selectedSort != defaults.selectedSort ||
        selectedRoutineName != null

    FitTrackCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(FitSpacing.sm)) {
            FitTrackSectionLabel(
                label = "Filtros",
                actionLabel = if (hasActiveFilters) "Limpiar" else null,
                onAction = if (hasActiveFilters) onClearFilters else null
            )
            FitTrackSectionLabel(label = "Periodo")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(FitSpacing.sm)
            ) {
                WorkoutStatsPeriod.entries.forEach { period ->
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
    onRecoverSession: () -> Unit,
    onDeleteSession: () -> Unit
) {
    val listState = rememberLazyListState()
    var showRecoverConfirm by remember { mutableStateOf(false) }
    var showDeleteSessionConfirm by remember { mutableStateOf(false) }

    if (showRecoverConfirm) {
        FitTrackConfirmDialog(
            title = "Recuperar entrenamiento",
            text = "Se reabrira esta sesion en la pestana Entrenar para que sigas donde lo dejaste.",
            confirmLabel = "Recuperar",
            dismissLabel = "Cancelar",
            onConfirm = {
                showRecoverConfirm = false
                onRecoverSession()
            },
            onDismiss = { showRecoverConfirm = false },
            destructive = false
        )
    }

    if (showDeleteSessionConfirm) {
        FitTrackConfirmDialog(
            title = "Eliminar sesion",
            text = "Se eliminara esta sesion del historial de forma permanente. Esta accion no se puede deshacer.",
            confirmLabel = "Eliminar",
            dismissLabel = "Cancelar",
            onConfirm = {
                showDeleteSessionConfirm = false
                onDeleteSession()
            },
            onDismiss = { showDeleteSessionConfirm = false },
            destructive = true
        )
    }

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
                leading = {
                    IconButton(onClick = onBackToList) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver al listado de historial"
                        )
                    }
                },
                trailing = if (state.selectedDetail != null) {
                    {
                        Row {
                            if (!state.isEditMode) {
                                IconButton(onClick = { showDeleteSessionConfirm = true }) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "Eliminar sesion"
                                    )
                                }
                            }
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
                    }
                } else {
                    null
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
                    HistoryDetailSummary(
                        detail = state.selectedDetail,
                        weightUnit = state.weightUnit
                    )
                }
                if (!state.selectedDetail.isComplete) {
                    item {
                        HistoryIncompleteCard(onRecoverSession = { showRecoverConfirm = true })
                    }
                }
                item {
                    HistoryComparisonCard(
                        comparison = state.selectedDetail.comparison,
                        weightUnit = state.weightUnit
                    )
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
                        weightUnit = state.weightUnit,
                        isEditMode = state.isEditMode,
                        onSetWeightChange = onSetWeightChange,
                        onSetRepsChange = onSetRepsChange
                    )
                }
            }

            // A session is selected, nothing is loading, and no detail arrived: the read failed
            // or the session is gone. Showing skeletons here left them shimmering forever with
            // the back button as the only way out.
            else -> {
                item {
                    FitTrackErrorState(
                        title = "No se pudo abrir la sesion",
                        message = "No hemos podido cargar los detalles de este entrenamiento. " +
                            "Puede que ya no exista.",
                        onRetry = onBackToList,
                        retryLabel = "Volver al historial"
                    )
                }
            }
        }
    }
}

@Composable
private fun HistorySessionCard(
    session: HistorySessionUiState,
    weightUnit: WeightUnit,
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
                text = "${session.totalVolumeKg.toDisplayText(weightUnit)} ${weightUnit.label} - " +
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
private fun HistoryDetailSummary(detail: HistoryDetailUiState, weightUnit: WeightUnit) {
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
            value = "${detail.totalVolumeKg.toDisplayText(weightUnit)} ${weightUnit.label}",
            style = FitTrackKeyValueRowStyle.Flat
        )
        detail.bestSet?.let { bestSet ->
            FitTrackKeyValueRow(
                label = "Mejor set",
                value = "${bestSet.exerciseName} · ${bestSet.weightKg.toDisplayText(weightUnit)} ${weightUnit.label} x ${bestSet.reps}",
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
private fun HistoryComparisonCard(comparison: HistoryComparisonUiState?, weightUnit: WeightUnit) {
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
                    currentText = "${comparison.totalVolumeDelta.currentValue.toDisplayText(weightUnit)} ${weightUnit.label}",
                    delta = comparison.totalVolumeDelta,
                    deltaText = "${comparison.totalVolumeDelta.deltaValue.toSignedDisplayText(weightUnit)} ${weightUnit.label}"
                )
                HistoryDeltaRow(
                    label = "Duracion",
                    currentText = formatDuration(comparison.durationMillisDelta.currentValue.toLong()),
                    delta = comparison.durationMillisDelta,
                    deltaText = comparison.durationMillisDelta.deltaValue.toDurationDeltaText(),
                    // A longer session is not an improvement, so it stays neutral.
                    meaning = FitTrackDeltaMeaning.Neutral
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
                        "${bestSet.exerciseName}: ${bestSet.weightKg.toDisplayText(weightUnit)} ${weightUnit.label} x ${bestSet.reps}"
                    } ?: "Sin datos",
                    delta = comparison.bestSet.delta,
                    deltaText = "${comparison.bestSet.delta.deltaValue.toSignedDisplayText(weightUnit)} ${weightUnit.label}"
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
    deltaText: String,
    meaning: FitTrackDeltaMeaning = FitTrackDeltaMeaning.HigherIsBetter
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
            tone = fitTrackDeltaTone(
                direction = when (delta.direction) {
                    WorkoutHistoryDeltaDirection.Up -> FitTrackDeltaDirection.Up
                    WorkoutHistoryDeltaDirection.Down -> FitTrackDeltaDirection.Down
                    WorkoutHistoryDeltaDirection.Same,
                    WorkoutHistoryDeltaDirection.Unavailable -> FitTrackDeltaDirection.Flat
                },
                meaning = meaning
            )
        )
    }
}

@Composable
private fun HistoryExerciseCard(
    exercise: HistoryExerciseUiState,
    weightUnit: WeightUnit,
    isEditMode: Boolean,
    onSetWeightChange: (Long, String) -> Unit,
    onSetRepsChange: (Long, String) -> Unit
) {
    val hasNotes = !exercise.notes.isNullOrBlank() || exercise.sets.any { !it.notes.isNullOrBlank() }
    var showNotes by remember(exercise.exerciseId) { mutableStateOf(false) }

    FitTrackCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(FitSpacing.md)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(FitSpacing.xs)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = exercise.name,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (hasNotes) {
                        IconButton(onClick = { showNotes = !showNotes }) {
                            Icon(
                                imageVector = if (showNotes) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (showNotes) {
                                    "Ocultar notas de ${exercise.name}"
                                } else {
                                    "Mostrar notas de ${exercise.name}"
                                }
                            )
                        }
                    }
                }
                Text(
                    text = "Objetivo: ${exercise.targetRepsText} reps",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                exercise.notes?.takeIf { showNotes && it.isNotBlank() }?.let { notes ->
                    Text(
                        text = "Notas: $notes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            exercise.sets.forEach { set ->
                HistorySetRow(
                    set = set,
                    weightUnit = weightUnit,
                    isEditMode = isEditMode,
                    showNotes = showNotes,
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
    weightUnit: WeightUnit,
    isEditMode: Boolean,
    showNotes: Boolean,
    onWeightChange: (Long, String) -> Unit,
    onRepsChange: (Long, String) -> Unit
) {
    FitTrackSetRow(
        setId = set.setId,
        setNumber = set.setNumber,
        weightText = if (isEditMode) set.weightText else "${set.weightKg.toDisplayText(weightUnit)} ${weightUnit.label}",
        repsText = if (isEditMode) set.repsText else "${set.reps} reps",
        mode = if (isEditMode) FitTrackSetRowMode.Edit else FitTrackSetRowMode.ReadOnly,
        notes = set.notes,
        showNotes = showNotes,
        editFieldStyle = FitTrackSetRowEditFieldStyle.TextField,
        weightUnitLabel = weightUnit.label,
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

private fun Double.toSignedDisplayText(weightUnit: WeightUnit): String {
    val absolute = kotlin.math.abs(this).toDisplayText(weightUnit)
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

private fun Double.toDisplayText(weightUnit: WeightUnit): String =
    weightUnit.fromKilograms(this).toDisplayText()
