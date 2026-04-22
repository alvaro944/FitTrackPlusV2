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
                    state.copy(
                        isLoading = false,
                        sessions = sessions.map { it.toUiState() },
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
}

data class HistoryUiState(
    val isLoading: Boolean = true,
    val isDetailLoading: Boolean = false,
    val sessions: List<HistorySessionUiState> = emptyList(),
    val selectedSessionId: Long? = null,
    val selectedDetail: HistoryDetailUiState? = null,
    val message: String? = null
)

data class HistorySessionUiState(
    val sessionId: Long,
    val routineName: String,
    val dayName: String,
    val startedAt: Long,
    val finishedAt: Long,
    val weekNumber: Int
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
}

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
        weekNumber = weekNumber
    )
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
