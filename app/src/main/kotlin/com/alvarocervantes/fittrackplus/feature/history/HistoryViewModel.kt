package com.alvarocervantes.fittrackplus.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvarocervantes.fittrackplus.domain.model.WorkoutHistoryDetail
import com.alvarocervantes.fittrackplus.domain.model.WorkoutHistoryDeltaDirection
import com.alvarocervantes.fittrackplus.domain.model.WorkoutHistoryExercise
import com.alvarocervantes.fittrackplus.domain.model.WorkoutHistoryMetricDelta
import com.alvarocervantes.fittrackplus.domain.model.WorkoutHistorySet
import com.alvarocervantes.fittrackplus.domain.model.WorkoutHistorySummary
import com.alvarocervantes.fittrackplus.domain.usecase.GetWorkoutHistoryDetailUseCase
import com.alvarocervantes.fittrackplus.domain.usecase.ObserveWorkoutHistoryUseCase
import com.alvarocervantes.fittrackplus.domain.usecase.UpdateWorkoutSetUseCase
import com.alvarocervantes.fittrackplus.feature.workout.parseWorkoutWeightInput
import com.alvarocervantes.fittrackplus.feature.workout.sanitizeWorkoutWeightInput
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val DAY_MILLIS: Long = 86_400_000

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val observeWorkoutHistory: ObserveWorkoutHistoryUseCase,
    private val getWorkoutHistoryDetail: GetWorkoutHistoryDetailUseCase,
    private val updateWorkoutSet: UpdateWorkoutSetUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private var editSnapshot: Map<Long, Pair<String, String>> = emptyMap()

    init {
        observeWorkoutHistory()
            .onEach { sessions ->
                val selectedId = _uiState.value.selectedSessionId
                val nextSelectedId = selectedId?.takeIf { sessionId ->
                    sessions.any { it.sessionId == sessionId }
                }

                _uiState.update { state ->
                    val allSessions = sessions.map { it.toUiState() }
                    state.copy(
                        isLoading = false,
                        allSessions = allSessions,
                        sessions = allSessions.applyHistoryFilters(
                            period = state.selectedPeriod,
                            sort = state.selectedSort,
                            nowMillis = System.currentTimeMillis()
                        ),
                        selectedSessionId = nextSelectedId,
                        selectedDetail = if (nextSelectedId == null) null else state.selectedDetail,
                        message = null
                    )
                }
            }
            .catch { throwable ->
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        message = throwable.message ?: "No se pudo cargar el historial."
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun selectSession(sessionId: Long) {
        viewModelScope.launch {
            editSnapshot = emptyMap()
            _uiState.update { state ->
                state.copy(
                    selectedSessionId = sessionId,
                    isDetailLoading = true,
                    isEditMode = false,
                    pendingEditExit = null,
                    message = null
                )
            }

            runCatching {
                getWorkoutHistoryDetail(sessionId)
            }.onSuccess { detail ->
                _uiState.update { state ->
                    state.copy(
                        isDetailLoading = false,
                        selectedDetail = detail?.toUiState(),
                        message = if (detail == null) {
                            "No se encontro la sesion finalizada."
                        } else {
                            null
                        }
                    )
                }
            }.onFailure { throwable ->
                _uiState.update { state ->
                    state.copy(
                        isDetailLoading = false,
                        message = throwable.message ?: "No se pudo cargar el detalle."
                    )
                }
            }
        }
    }

    fun clearSelection() {
        editSnapshot = emptyMap()
        _uiState.update { state ->
            state.copy(
                selectedSessionId = null,
                selectedDetail = null,
                isDetailLoading = false,
                isEditMode = false,
                pendingEditExit = null
            )
        }
    }

    fun clearMessage() {
        _uiState.update { state -> state.copy(message = null) }
    }

    /** Toggles edit mode on, or requests to exit it (with confirmation if there are unsaved changes). */
    fun toggleEditMode() {
        if (!_uiState.value.isEditMode) {
            editSnapshot = snapshotSets(_uiState.value.selectedDetail)
            _uiState.update { state -> state.copy(isEditMode = true) }
        } else {
            requestExitEdit(HistoryEditExitAction.FinishEditing)
        }
    }

    /** Back arrow / system back from the detail screen: confirms first if mid-edit with changes. */
    fun requestBackToList() {
        if (_uiState.value.isEditMode) {
            requestExitEdit(HistoryEditExitAction.BackToList)
        } else {
            clearSelection()
        }
    }

    fun confirmSaveChanges() {
        val action = _uiState.value.pendingEditExit ?: return
        finalizeEditExit(action)
    }

    fun confirmDiscardChanges() {
        val action = _uiState.value.pendingEditExit ?: return
        editSnapshot.forEach { (setId, original) ->
            val (weightText, repsText) = original
            updateSelectedSet(setId) { current ->
                current.copy(
                    weightText = weightText,
                    repsText = repsText,
                    weightKg = parseWorkoutWeightInput(weightText) ?: current.weightKg,
                    reps = repsText.toIntOrNull() ?: current.reps
                )
            }
            persistSetEdit(setId = setId, weightText = weightText, repsText = repsText)
        }
        finalizeEditExit(action)
    }

    fun cancelPendingEditExit() {
        _uiState.update { state -> state.copy(pendingEditExit = null) }
    }

    private fun requestExitEdit(action: HistoryEditExitAction) {
        if (hasUnsavedChanges()) {
            _uiState.update { state -> state.copy(pendingEditExit = action) }
        } else {
            finalizeEditExit(action)
        }
    }

    private fun finalizeEditExit(action: HistoryEditExitAction) {
        editSnapshot = emptyMap()
        _uiState.update { state -> state.copy(isEditMode = false, pendingEditExit = null) }
        if (action == HistoryEditExitAction.BackToList) {
            clearSelection()
        }
    }

    private fun hasUnsavedChanges(): Boolean {
        val currentSets = _uiState.value.selectedDetail?.exercises?.flatMap { it.sets } ?: return false
        return currentSets.any { set ->
            val original = editSnapshot[set.setId]
            original != null && (original.first != set.weightText || original.second != set.repsText)
        }
    }

    private fun snapshotSets(detail: HistoryDetailUiState?): Map<Long, Pair<String, String>> {
        return detail?.exercises
            ?.flatMap { exercise -> exercise.sets }
            ?.associate { set -> set.setId to (set.weightText to set.repsText) }
            ?: emptyMap()
    }

    fun updateSetWeight(setId: Long, weightText: String) {
        val set = findSelectedSet(setId) ?: return
        val sanitizedWeightText = sanitizeWorkoutWeightInput(weightText)
        updateSelectedSet(setId) { current ->
            current.copy(
                weightText = sanitizedWeightText,
                weightKg = parseWorkoutWeightInput(sanitizedWeightText) ?: current.weightKg
            )
        }
        persistSetEdit(setId = setId, weightText = sanitizedWeightText, repsText = set.repsText)
    }

    fun updateSetReps(setId: Long, repsText: String) {
        val set = findSelectedSet(setId) ?: return
        val sanitizedRepsText = repsText.filter { it.isDigit() }
        updateSelectedSet(setId) { current ->
            current.copy(
                repsText = sanitizedRepsText,
                reps = sanitizedRepsText.toIntOrNull() ?: current.reps
            )
        }
        persistSetEdit(setId = setId, weightText = set.weightText, repsText = sanitizedRepsText)
    }

    private fun findSelectedSet(setId: Long): HistorySetUiState? {
        return _uiState.value.selectedDetail?.exercises
            ?.flatMap { exercise -> exercise.sets }
            ?.firstOrNull { set -> set.setId == setId }
    }

    private fun updateSelectedSet(setId: Long, transform: (HistorySetUiState) -> HistorySetUiState) {
        _uiState.update { state ->
            val detail = state.selectedDetail ?: return@update state
            state.copy(
                selectedDetail = detail.copy(
                    exercises = detail.exercises.map { exercise ->
                        if (exercise.sets.none { set -> set.setId == setId }) {
                            exercise
                        } else {
                            exercise.copy(
                                sets = exercise.sets.map { set ->
                                    if (set.setId == setId) transform(set) else set
                                }
                            )
                        }
                    }
                )
            )
        }
    }

    private fun persistSetEdit(setId: Long, weightText: String, repsText: String) {
        viewModelScope.launch {
            runCatching {
                updateWorkoutSet(setId = setId, weightText = weightText, repsText = repsText)
            }.onFailure { throwable ->
                _uiState.update { state ->
                    state.copy(message = throwable.message ?: "No se pudo guardar el cambio.")
                }
            }
        }
    }

    fun setPeriodFilter(period: HistoryPeriodFilter) {
        _uiState.update { state ->
            state.copy(
                selectedPeriod = period,
                sessions = state.allSessions.applyHistoryFilters(
                    period = period,
                    sort = state.selectedSort,
                    nowMillis = System.currentTimeMillis()
                )
            )
        }
    }

    fun setSortOrder(sort: HistorySortOrder) {
        _uiState.update { state ->
            state.copy(
                selectedSort = sort,
                sessions = state.allSessions.applyHistoryFilters(
                    period = state.selectedPeriod,
                    sort = sort,
                    nowMillis = System.currentTimeMillis()
                )
            )
        }
    }
}

data class HistoryUiState(
    val isLoading: Boolean = true,
    val isDetailLoading: Boolean = false,
    val allSessions: List<HistorySessionUiState> = emptyList(),
    val sessions: List<HistorySessionUiState> = emptyList(),
    val selectedPeriod: HistoryPeriodFilter = HistoryPeriodFilter.All,
    val selectedSort: HistorySortOrder = HistorySortOrder.Recent,
    val selectedSessionId: Long? = null,
    val selectedDetail: HistoryDetailUiState? = null,
    val isEditMode: Boolean = false,
    val pendingEditExit: HistoryEditExitAction? = null,
    val message: String? = null
)

enum class HistoryEditExitAction {
    FinishEditing,
    BackToList
}

enum class HistoryPeriodFilter(val label: String) {
    All("Todo"),
    LastFourWeeks("4 semanas"),
    LastTwelveWeeks("12 semanas")
}

enum class HistorySortOrder(val label: String) {
    Recent("Reciente"),
    Oldest("Antiguo"),
    HighestVolume("Mayor volumen")
}

data class HistorySessionUiState(
    val sessionId: Long,
    val routineName: String,
    val dayName: String,
    val startedAt: Long,
    val finishedAt: Long,
    val weekNumber: Int,
    val totalVolumeKg: Double,
    val durationMillis: Long,
    val setCount: Int
)

data class HistoryDetailUiState(
    val sessionId: Long,
    val routineName: String,
    val dayName: String,
    val startedAt: Long,
    val finishedAt: Long,
    val weekNumber: Int,
    val notes: String?,
    val exercises: List<HistoryExerciseUiState>,
    val comparison: HistoryComparisonUiState? = null
) {
    val totalSetCount: Int = exercises.sumOf { it.sets.size }
    val durationMillis: Long = (finishedAt - startedAt).coerceAtLeast(0)
    val totalVolumeKg: Double = exercises.sumOf { exercise ->
        exercise.sets.sumOf { set -> set.weightKg * set.reps }
    }
    val bestSet: HistoryBestSetUiState? = exercises
        .flatMap { exercise ->
            exercise.sets.map { set ->
                HistoryBestSetUiState(
                    exerciseName = exercise.name,
                    weightKg = set.weightKg,
                    reps = set.reps,
                    volumeKg = set.weightKg * set.reps
                )
            }
        }
        .filter { set -> set.volumeKg > 0.0 }
        .maxByOrNull { set -> set.volumeKg }
}

data class HistoryComparisonUiState(
    val previousFinishedAt: Long,
    val totalVolumeDelta: HistoryMetricDeltaUiState,
    val durationMillisDelta: HistoryMetricDeltaUiState,
    val setCountDelta: HistoryMetricDeltaUiState,
    val bestSet: HistoryBestSetComparisonUiState
)

data class HistoryMetricDeltaUiState(
    val currentValue: Double,
    val previousValue: Double,
    val deltaValue: Double,
    val direction: WorkoutHistoryDeltaDirection
)

data class HistoryBestSetComparisonUiState(
    val current: HistoryBestSetUiState?,
    val previous: HistoryBestSetUiState?,
    val delta: HistoryMetricDeltaUiState
)

data class HistoryBestSetUiState(
    val exerciseName: String,
    val weightKg: Double,
    val reps: Int,
    val volumeKg: Double
)

data class HistoryExerciseUiState(
    val exerciseId: Long,
    val name: String,
    val targetRepsText: String,
    val sets: List<HistorySetUiState>
)

data class HistorySetUiState(
    val setId: Long,
    val setNumber: Int,
    val weightKg: Double,
    val reps: Int,
    val notes: String?,
    val weightText: String = weightKg.toHistoryInputText(),
    val repsText: String = reps.toString()
)

private fun WorkoutHistorySummary.toUiState(): HistorySessionUiState {
    return HistorySessionUiState(
        sessionId = sessionId,
        routineName = routineName,
        dayName = dayName,
        startedAt = startedAt,
        finishedAt = finishedAt,
        weekNumber = weekNumber,
        totalVolumeKg = totalVolumeKg,
        durationMillis = durationMillis,
        setCount = setCount
    )
}

fun List<HistorySessionUiState>.applyHistoryFilters(
    period: HistoryPeriodFilter,
    sort: HistorySortOrder,
    nowMillis: Long
): List<HistorySessionUiState> {
    return filterByPeriod(period, nowMillis).sortByOrder(sort)
}

private fun List<HistorySessionUiState>.filterByPeriod(
    period: HistoryPeriodFilter,
    nowMillis: Long
): List<HistorySessionUiState> {
    val cutoff = when (period) {
        HistoryPeriodFilter.All -> return this
        HistoryPeriodFilter.LastFourWeeks -> nowMillis - 4 * 7 * DAY_MILLIS
        HistoryPeriodFilter.LastTwelveWeeks -> nowMillis - 12 * 7 * DAY_MILLIS
    }
    return filter { session -> session.finishedAt >= cutoff }
}

private fun List<HistorySessionUiState>.sortByOrder(sort: HistorySortOrder): List<HistorySessionUiState> {
    return when (sort) {
        HistorySortOrder.Recent -> sortedByDescending { session -> session.finishedAt }
        HistorySortOrder.Oldest -> sortedBy { session -> session.finishedAt }
        HistorySortOrder.HighestVolume -> sortedByDescending { session -> session.totalVolumeKg }
    }
}

private fun WorkoutHistoryDetail.toUiState(): HistoryDetailUiState {
    return HistoryDetailUiState(
        sessionId = sessionId,
        routineName = routineName,
        dayName = dayName,
        startedAt = startedAt,
        finishedAt = finishedAt,
        weekNumber = weekNumber,
        notes = notes,
        exercises = exercises.map { it.toUiState() },
        comparison = comparison?.let { domainComparison ->
            HistoryComparisonUiState(
                previousFinishedAt = domainComparison.previousFinishedAt,
                totalVolumeDelta = domainComparison.totalVolumeDelta.toUiState(),
                durationMillisDelta = domainComparison.durationMillisDelta.toUiState(),
                setCountDelta = domainComparison.setCountDelta.toUiState(),
                bestSet = HistoryBestSetComparisonUiState(
                    current = domainComparison.bestSet.current?.let { bestSet ->
                        HistoryBestSetUiState(
                            exerciseName = bestSet.exerciseName,
                            weightKg = bestSet.weightKg,
                            reps = bestSet.reps,
                            volumeKg = bestSet.volumeKg
                        )
                    },
                    previous = domainComparison.bestSet.previous?.let { bestSet ->
                        HistoryBestSetUiState(
                            exerciseName = bestSet.exerciseName,
                            weightKg = bestSet.weightKg,
                            reps = bestSet.reps,
                            volumeKg = bestSet.volumeKg
                        )
                    },
                    delta = domainComparison.bestSet.delta.toUiState()
                )
            )
        }
    )
}

private fun WorkoutHistoryMetricDelta.toUiState(): HistoryMetricDeltaUiState {
    return HistoryMetricDeltaUiState(
        currentValue = currentValue,
        previousValue = previousValue,
        deltaValue = deltaValue,
        direction = direction
    )
}

private fun WorkoutHistoryExercise.toUiState(): HistoryExerciseUiState {
    return HistoryExerciseUiState(
        exerciseId = exerciseId,
        name = name,
        targetRepsText = targetRepsText,
        sets = sets.map { it.toUiState() }
    )
}

private fun WorkoutHistorySet.toUiState(): HistorySetUiState {
    return HistorySetUiState(
        setId = setId,
        setNumber = setNumber,
        weightKg = weightKg,
        reps = reps,
        notes = notes
    )
}

private fun Double.toHistoryInputText(): String {
    return if (this % 1.0 == 0.0) toInt().toString() else toString().replace('.', ',')
}
