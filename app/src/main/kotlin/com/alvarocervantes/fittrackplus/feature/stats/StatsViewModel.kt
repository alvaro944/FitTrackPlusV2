package com.alvarocervantes.fittrackplus.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvarocervantes.fittrackplus.domain.model.ExerciseProgress
import com.alvarocervantes.fittrackplus.domain.model.ExerciseProgressEntry
import com.alvarocervantes.fittrackplus.domain.model.ExerciseRecords
import com.alvarocervantes.fittrackplus.domain.model.ExerciseSetRecord
import com.alvarocervantes.fittrackplus.domain.model.WorkoutSessionVolume
import com.alvarocervantes.fittrackplus.domain.model.WorkoutStats
import com.alvarocervantes.fittrackplus.domain.usecase.ObserveWorkoutStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

@HiltViewModel
class StatsViewModel @Inject constructor(
    observeWorkoutStats: ObserveWorkoutStatsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        observeWorkoutStats()
            .onEach { stats ->
                _uiState.update {
                    stats.toUiState().copy(isLoading = false)
                }
            }
            .catch { throwable ->
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        message = throwable.message ?: "No se pudieron cargar las estadisticas."
                    )
                }
            }
            .launchIn(viewModelScope)
    }
}

data class StatsUiState(
    val isLoading: Boolean = true,
    val sessionVolumes: List<SessionVolumeUiState> = emptyList(),
    val exerciseProgress: List<ExerciseProgressUiState> = emptyList(),
    val exerciseRecords: List<ExerciseRecordsUiState> = emptyList(),
    val message: String? = null
) {
    val isEmpty: Boolean = sessionVolumes.isEmpty() &&
        exerciseProgress.isEmpty() &&
        exerciseRecords.isEmpty()
}

data class SessionVolumeUiState(
    val sessionId: Long,
    val routineName: String,
    val dayName: String,
    val finishedAt: Long,
    val totalVolumeKg: Double
)

data class ExerciseProgressUiState(
    val exerciseKey: String,
    val exerciseName: String,
    val entries: List<ExerciseProgressEntryUiState>
)

data class ExerciseProgressEntryUiState(
    val sessionId: Long,
    val finishedAt: Long,
    val volumeKg: Double,
    val maxWeightKg: Double,
    val totalReps: Int,
    val estimatedOneRepMaxKg: Double
)

data class ExerciseRecordsUiState(
    val exerciseKey: String,
    val exerciseName: String,
    val maxWeight: ExerciseSetRecordUiState?,
    val maxReps: ExerciseSetRecordUiState?,
    val bestSetVolume: ExerciseSetRecordUiState?,
    val bestEstimatedOneRepMax: ExerciseSetRecordUiState?
)

data class ExerciseSetRecordUiState(
    val sessionId: Long,
    val finishedAt: Long,
    val weightKg: Double,
    val reps: Int,
    val setVolumeKg: Double,
    val estimatedOneRepMaxKg: Double
)

private fun WorkoutStats.toUiState(): StatsUiState {
    return StatsUiState(
        sessionVolumes = sessionVolumes.map { it.toUiState() },
        exerciseProgress = exerciseProgress.map { it.toUiState() },
        exerciseRecords = exerciseRecords.map { it.toUiState() }
    )
}

private fun WorkoutSessionVolume.toUiState(): SessionVolumeUiState {
    return SessionVolumeUiState(
        sessionId = sessionId,
        routineName = routineName,
        dayName = dayName,
        finishedAt = finishedAt,
        totalVolumeKg = totalVolumeKg
    )
}

private fun ExerciseProgress.toUiState(): ExerciseProgressUiState {
    return ExerciseProgressUiState(
        exerciseKey = exerciseKey,
        exerciseName = exerciseName,
        entries = entries.map { it.toUiState() }
    )
}

private fun ExerciseProgressEntry.toUiState(): ExerciseProgressEntryUiState {
    return ExerciseProgressEntryUiState(
        sessionId = sessionId,
        finishedAt = finishedAt,
        volumeKg = volumeKg,
        maxWeightKg = maxWeightKg,
        totalReps = totalReps,
        estimatedOneRepMaxKg = estimatedOneRepMaxKg
    )
}

private fun ExerciseRecords.toUiState(): ExerciseRecordsUiState {
    return ExerciseRecordsUiState(
        exerciseKey = exerciseKey,
        exerciseName = exerciseName,
        maxWeight = maxWeight?.toUiState(),
        maxReps = maxReps?.toUiState(),
        bestSetVolume = bestSetVolume?.toUiState(),
        bestEstimatedOneRepMax = bestEstimatedOneRepMax?.toUiState()
    )
}

private fun ExerciseSetRecord.toUiState(): ExerciseSetRecordUiState {
    return ExerciseSetRecordUiState(
        sessionId = sessionId,
        finishedAt = finishedAt,
        weightKg = weightKg,
        reps = reps,
        setVolumeKg = setVolumeKg,
        estimatedOneRepMaxKg = estimatedOneRepMaxKg
    )
}
