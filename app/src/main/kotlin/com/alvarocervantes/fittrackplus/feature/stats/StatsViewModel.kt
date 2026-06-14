package com.alvarocervantes.fittrackplus.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvarocervantes.fittrackplus.data.preferences.UserPreferencesRepository
import com.alvarocervantes.fittrackplus.domain.model.ExerciseProgress
import com.alvarocervantes.fittrackplus.domain.model.ExerciseProgressEntry
import com.alvarocervantes.fittrackplus.domain.model.ExerciseRecords
import com.alvarocervantes.fittrackplus.domain.model.ExerciseSetRecord
import com.alvarocervantes.fittrackplus.domain.model.HeatmapDay
import com.alvarocervantes.fittrackplus.domain.model.StepsData
import com.alvarocervantes.fittrackplus.domain.model.WorkoutSessionVolume
import com.alvarocervantes.fittrackplus.domain.model.WorkoutStats
import com.alvarocervantes.fittrackplus.domain.model.WorkoutStatsPeriod
import com.alvarocervantes.fittrackplus.domain.usecase.GetWorkoutHeatmapUseCase
import com.alvarocervantes.fittrackplus.domain.usecase.ObserveWorkoutStatsUseCase
import com.alvarocervantes.fittrackplus.domain.usecase.ReadDailyStepsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StatsViewModel @Inject constructor(
    observeWorkoutStats: ObserveWorkoutStatsUseCase,
    getWorkoutHeatmap: GetWorkoutHeatmapUseCase,
    private val readDailyStepsUseCase: ReadDailyStepsUseCase,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val selectedPeriod = MutableStateFlow(WorkoutStatsPeriod.All)
    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        selectedPeriod
            .flatMapLatest { period ->
                observeWorkoutStats(period = period).map { stats -> period to stats }
            }
            .onEach { (period, stats) ->
                _uiState.update { currentState ->
                    currentState.withStatsPeriod(
                        period = period,
                        stats = stats.toUiState().copy(isLoading = false)
                    )
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

        getWorkoutHeatmap()
            .onEach { heatmap ->
                _uiState.update { state -> state.copy(heatmapDays = heatmap) }
            }
            .catch { }
            .launchIn(viewModelScope)

        userPreferencesRepository.healthConnectConnected
            .onEach { connected ->
                if (connected) {
                    val steps = readDailyStepsUseCase()
                    val dailyGoal = userPreferencesRepository.dailyStepGoal.first()
                    _uiState.update { state ->
                        state.copy(
                            weeklySteps = steps?.weekDaySteps?.values?.sum(),
                            stepGoalDaysCompleted = countGoalDays(steps, dailyGoal)
                        )
                    }
                } else {
                    _uiState.update { state ->
                        state.copy(weeklySteps = null, stepGoalDaysCompleted = 0)
                    }
                }
            }
            .catch { }
            .launchIn(viewModelScope)
    }

    fun setPeriodFilter(period: WorkoutStatsPeriod) {
        selectedPeriod.value = period
    }

    fun selectExercise(name: String) {
        _uiState.update { state ->
            state.withSelectedExercise(name)
        }
    }

    fun selectProgressPoint(sessionId: Long) {
        _uiState.update { state ->
            state.withSelectedProgressPoint(sessionId)
        }
    }

    fun clearSelectedProgressPoint() {
        _uiState.update { state ->
            state.copy(selectedProgressPoint = null)
        }
    }

    fun onHeatmapDayClick(day: HeatmapDay) {
        if (day.totalVolumeKg <= 0.0) return
        val dateStr = epochDayToDisplayString(day.epochDay)
        val volumeStr = if (day.totalVolumeKg % 1.0 == 0.0) {
            day.totalVolumeKg.toInt().toString()
        } else {
            String.format(java.util.Locale.getDefault(), "%.1f", day.totalVolumeKg)
        }
        _uiState.update { state -> state.copy(message = "$dateStr · $volumeStr kg") }
    }

    fun clearMessage() {
        _uiState.update { state -> state.copy(message = null) }
    }

    private fun countGoalDays(steps: StepsData?, goal: Int): Int {
        if (steps == null || goal <= 0) return 0
        return steps.weekDaySteps.values.count { it >= goal }
    }
}

data class StatsUiState(
    val isLoading: Boolean = true,
    val sessionVolumes: List<SessionVolumeUiState> = emptyList(),
    val exerciseProgress: List<ExerciseProgressUiState> = emptyList(),
    val exerciseRecords: List<ExerciseRecordsUiState> = emptyList(),
    val selectedPeriod: WorkoutStatsPeriod = WorkoutStatsPeriod.All,
    val selectedExerciseName: String? = null,
    val progressPoints: List<ProgressChartPointUiState> = emptyList(),
    val selectedProgressPoint: ProgressChartPointUiState? = null,
    val heatmapDays: List<HeatmapDay> = emptyList(),
    val weeklySteps: Long? = null,
    val stepGoalDaysCompleted: Int = 0,
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

data class ProgressChartPointUiState(
    val sessionId: Long,
    val finishedAt: Long,
    val maxWeightKg: Double,
    val volumeKg: Double,
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

private fun epochDayToDisplayString(epochDay: Long): String {
    val ms = epochDay * 86_400_000L
    return java.text.SimpleDateFormat("d MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date(ms))
}

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

fun StatsUiState.withStatsPeriod(
    period: WorkoutStatsPeriod,
    stats: StatsUiState
): StatsUiState {
    val retainedExerciseName = selectedExerciseName?.takeIf { selectedName ->
        stats.exerciseProgress.any { progress -> progress.exerciseName == selectedName }
    }
    return stats.copy(
        selectedPeriod = period,
        selectedExerciseName = retainedExerciseName,
        selectedProgressPoint = null,
        weeklySteps = weeklySteps,
        stepGoalDaysCompleted = stepGoalDaysCompleted
    ).withProgressPointsForSelection()
}

fun StatsUiState.withSelectedExercise(name: String): StatsUiState {
    val selectedName = exerciseProgress
        .firstOrNull { progress -> progress.exerciseName == name }
        ?.exerciseName
    return copy(
        selectedExerciseName = selectedName,
        selectedProgressPoint = null
    ).withProgressPointsForSelection()
}

fun StatsUiState.withSelectedProgressPoint(sessionId: Long): StatsUiState {
    return copy(
        selectedProgressPoint = progressPoints.firstOrNull { point -> point.sessionId == sessionId }
    )
}

fun StatsUiState.withProgressPointsForSelection(): StatsUiState {
    val name = selectedExerciseName
        ?: return copy(progressPoints = emptyList(), selectedProgressPoint = null)
    val exercise = exerciseProgress.firstOrNull { it.exerciseName == name }
        ?: return copy(
            selectedExerciseName = null,
            progressPoints = emptyList(),
            selectedProgressPoint = null
        )
    val points = exercise.entries
        .sortedBy { it.finishedAt }
        .map { entry ->
            ProgressChartPointUiState(
                sessionId = entry.sessionId,
                finishedAt = entry.finishedAt,
                maxWeightKg = entry.maxWeightKg,
                volumeKg = entry.volumeKg,
                totalReps = entry.totalReps,
                estimatedOneRepMaxKg = entry.estimatedOneRepMaxKg
            )
    }
    val retainedPoint = selectedProgressPoint?.let { selected ->
        points.firstOrNull { point -> point.sessionId == selected.sessionId }
    }
    return copy(
        progressPoints = points,
        selectedProgressPoint = retainedPoint
    )
}
