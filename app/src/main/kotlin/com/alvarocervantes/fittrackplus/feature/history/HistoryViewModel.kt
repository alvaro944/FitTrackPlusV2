package com.alvarocervantes.fittrackplus.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvarocervantes.fittrackplus.domain.model.WorkoutHistoryDetail
import com.alvarocervantes.fittrackplus.domain.model.WorkoutHistoryExercise
import com.alvarocervantes.fittrackplus.domain.model.WorkoutHistorySet
import com.alvarocervantes.fittrackplus.domain.model.WorkoutHistorySummary
import com.alvarocervantes.fittrackplus.domain.usecase.GetWorkoutHistoryDetailUseCase
import com.alvarocervantes.fittrackplus.domain.usecase.ObserveWorkoutHistoryUseCase
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
    private val getWorkoutHistoryDetail: GetWorkoutHistoryDetailUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

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
            _uiState.update { state ->
                state.copy(
                    selectedSessionId = sessionId,
                    isDetailLoading = true,
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
        _uiState.update { state ->
            state.copy(
                selectedSessionId = null,
                selectedDetail = null,
                isDetailLoading = false
            )
        }
    }

    fun clearMessage() {
        _uiState.update { state -> state.copy(message = null) }
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
    val message: String? = null
)

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
    val exercises: List<HistoryExerciseUiState>
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
    val notes: String?
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
        exercises = exercises.map { it.toUiState() }
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
