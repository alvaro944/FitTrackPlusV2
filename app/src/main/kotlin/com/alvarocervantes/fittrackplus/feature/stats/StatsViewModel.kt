package com.alvarocervantes.fittrackplus.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvarocervantes.fittrackplus.data.preferences.UserPreferencesRepository
import com.alvarocervantes.fittrackplus.domain.model.ExerciseProgress
import com.alvarocervantes.fittrackplus.domain.model.ExerciseProgressEntry
import com.alvarocervantes.fittrackplus.domain.model.ExerciseRecords
import com.alvarocervantes.fittrackplus.domain.model.ExerciseSetRecord
import com.alvarocervantes.fittrackplus.domain.model.HeatmapDay
import com.alvarocervantes.fittrackplus.domain.model.WorkoutSessionVolume
import com.alvarocervantes.fittrackplus.domain.model.WorkoutStats
import com.alvarocervantes.fittrackplus.domain.model.WorkoutStatsPeriod
import com.alvarocervantes.fittrackplus.domain.usecase.GetWorkoutHeatmapUseCase
import com.alvarocervantes.fittrackplus.domain.usecase.ObserveWorkoutStatsUseCase
import com.alvarocervantes.fittrackplus.domain.usecase.ReadDailyStepsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.Collator
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
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

    private val selectedPeriod = MutableStateFlow(WorkoutStatsPeriod.LastFourWeeks)
    private val activeRoutineId = MutableStateFlow<Long?>(null)
    private val _selectedWeekStart = MutableStateFlow(currentWeekMonday())
    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        userPreferencesRepository.activeRoutineId
            .onEach { id ->
                activeRoutineId.value = id
                _uiState.update { state -> state.copy(activeRoutineId = id).withValidFocusSelection() }
            }
            .launchIn(viewModelScope)

        combine(selectedPeriod, activeRoutineId) { period, routineId -> period to routineId }
            .flatMapLatest { (period, routineId) ->
                observeWorkoutStats(period = period).map { stats -> Triple(period, routineId, stats) }
            }
            .onEach { (period, routineId, stats) ->
                _uiState.update { currentState ->
                    currentState.withStatsPeriod(
                        period = period,
                        stats = stats.toUiState().copy(
                            isLoading = false,
                            activeRoutineId = routineId
                        )
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

        combine(
            userPreferencesRepository.healthConnectConnected,
            _selectedWeekStart
        ) { connected, weekStart -> connected to weekStart }
            .onEach { (connected, weekStart) ->
                if (connected) {
                    val dailyGoal = userPreferencesRepository.dailyStepGoal.first()
                    val daySteps = readDailyStepsUseCase.readForWeekStart(weekStart) ?: emptyMap()
                    val isCurrentWeek = weekStart == currentWeekMonday()
                    val stepsData = WeeklyStepsData(
                        weekStart = weekStart,
                        dailySteps = daySteps,
                        dailyGoal = dailyGoal,
                        isCurrentWeek = isCurrentWeek,
                        daysElapsedInWeek = if (isCurrentWeek) LocalDate.now().dayOfWeek.value else 7
                    )
                    _uiState.update { state ->
                        state.copy(
                            weeklyStepsData = stepsData,
                            canGoToNextWeek = !isCurrentWeek
                        )
                    }
                } else {
                    _uiState.update { state ->
                        state.copy(weeklyStepsData = null, canGoToNextWeek = false)
                    }
                }
            }
            .catch { }
            .launchIn(viewModelScope)
    }

    fun previousWeek() {
        _selectedWeekStart.update { it.minusWeeks(1) }
    }

    fun nextWeek() {
        val nextWeek = _selectedWeekStart.value.plusWeeks(1)
        if (!nextWeek.isAfter(currentWeekMonday())) {
            _selectedWeekStart.value = nextWeek
        }
    }

    fun setPeriodFilter(period: WorkoutStatsPeriod) {
        selectedPeriod.value = period
    }

    fun selectExercise(name: String) {
        _uiState.update { state ->
            state.withSelectedExercise(name)
        }
    }

    fun selectRoutine(routineName: String) {
        _uiState.update { state ->
            state.copy(
                selectedRoutineName = routineName,
                selectedDayName = null,
                selectedExerciseScopeKey = null,
                selectedExerciseName = null,
                selectedProgressPoint = null
            ).withValidFocusSelection()
        }
    }

    fun selectDay(dayName: String) {
        _uiState.update { state ->
            state.copy(
                selectedDayName = dayName,
                selectedExerciseScopeKey = null,
                selectedExerciseName = null,
                selectedProgressPoint = null
            ).withValidFocusSelection()
        }
    }

    fun selectExerciseScope(scopeKey: String) {
        _uiState.update { state ->
            state.withSelectedExerciseScope(scopeKey)
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

    fun clearMessage() {
        _uiState.update { state -> state.copy(message = null) }
    }

    private fun currentWeekMonday(): LocalDate = LocalDate.now()
        .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
}

data class WeeklyStepsData(
    val weekStart: LocalDate,
    val dailySteps: Map<Int, Long>,  // 0=Mon..6=Sun
    val dailyGoal: Int,
    val isCurrentWeek: Boolean,
    val daysElapsedInWeek: Int       // 1=Mon..7=Sun, used only when isCurrentWeek
) {
    val weekEnd: LocalDate = weekStart.plusDays(6)
    val totalSteps: Long = dailySteps.values.sum()
    val daysGoalMet: Int = dailySteps.count { (_, steps) -> steps >= dailyGoal }
}

data class StatsUiState(
    val isLoading: Boolean = true,
    val sessionVolumes: List<SessionVolumeUiState> = emptyList(),
    val exerciseProgress: List<ExerciseProgressUiState> = emptyList(),
    val exerciseRecords: List<ExerciseRecordsUiState> = emptyList(),
    val selectedPeriod: WorkoutStatsPeriod = WorkoutStatsPeriod.LastFourWeeks,
    val activeRoutineId: Long? = null,
    val selectedRoutineName: String? = null,
    val selectedDayName: String? = null,
    val selectedExerciseScopeKey: String? = null,
    val selectedExerciseName: String? = null,
    val progressPoints: List<ProgressChartPointUiState> = emptyList(),
    val selectedProgressPoint: ProgressChartPointUiState? = null,
    val heatmapDays: List<HeatmapDay> = emptyList(),
    val weeklyStepsData: WeeklyStepsData? = null,
    val canGoToNextWeek: Boolean = false,
    val message: String? = null
) {
    val isEmpty: Boolean = sessionVolumes.isEmpty() &&
        exerciseProgress.isEmpty() &&
        exerciseRecords.isEmpty()
    val sessionCount: Int = sessionVolumes.size
    val bestSessionVolumeKg: Double = sessionVolumes.maxOfOrNull { session -> session.totalVolumeKg } ?: 0.0
    val availableRoutineNames: List<String> = sessionVolumes
        .map { session -> session.routineName.trim() }
        .filter { name -> name.isNotBlank() }
        .distinct()
        .sortedWith(spanishCollator())
    val availableDayNames: List<String> = sessionVolumes
        .filter { session -> selectedRoutineName == null || session.routineName == selectedRoutineName }
        .map { session -> session.dayName.trim() to session.dayId }
        .filter { (name, _) -> name.isNotBlank() }
        .groupBy(keySelector = { (name, _) -> name }, valueTransform = { (_, dayId) -> dayId })
        .entries
        .sortedWith(
            compareBy<Map.Entry<String, List<Long?>>> { (_, dayIds) ->
                dayIds.filterNotNull().minOrNull() ?: Long.MAX_VALUE
            }.thenBy(spanishCollator()) { (name, _) -> name }
        )
        .map { (name, _) -> name }
    val focusedSessionVolumes: List<SessionVolumeUiState> = sessionVolumes
        .filter { session -> selectedRoutineName == null || session.routineName == selectedRoutineName }
        .filter { session -> selectedDayName == null || session.dayName == selectedDayName }
    val focusedExerciseProgress: List<ExerciseProgressUiState> = exerciseProgress
        .filter { progress -> selectedRoutineName == null || progress.routineName == selectedRoutineName }
        .filter { progress -> selectedDayName == null || progress.dayName == selectedDayName }
        .sortedWith(compareBy<ExerciseProgressUiState> { it.exercisePosition }.thenBy { it.exerciseName })
    val focusedExerciseRecords: List<ExerciseRecordsUiState> = exerciseRecords
        .filter { records -> selectedRoutineName == null || records.routineName == selectedRoutineName }
        .filter { records -> selectedDayName == null || records.dayName == selectedDayName }
        .sortedWith(compareBy<ExerciseRecordsUiState> { it.exercisePosition }.thenBy { it.exerciseName })
}

data class SessionVolumeUiState(
    val sessionId: Long,
    val routineId: Long? = null,
    val routineName: String,
    val dayId: Long? = null,
    val dayName: String,
    val finishedAt: Long,
    val totalVolumeKg: Double
)

data class ExerciseProgressUiState(
    val exerciseKey: String,
    val scopeKey: String = exerciseKey,
    val routineName: String = "",
    val dayName: String = "",
    val exerciseName: String,
    val exercisePosition: Int = Int.MAX_VALUE,
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
    val scopeKey: String = exerciseKey,
    val routineName: String = "",
    val dayName: String = "",
    val exerciseName: String,
    val exercisePosition: Int = Int.MAX_VALUE,
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
        routineId = routineId,
        routineName = routineName,
        dayId = dayId,
        dayName = dayName,
        finishedAt = finishedAt,
        totalVolumeKg = totalVolumeKg
    )
}

private fun ExerciseProgress.toUiState(): ExerciseProgressUiState {
    return ExerciseProgressUiState(
        exerciseKey = exerciseKey,
        scopeKey = scopeKey,
        routineName = routineName,
        dayName = dayName,
        exerciseName = exerciseName,
        exercisePosition = exercisePosition,
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
        scopeKey = scopeKey,
        routineName = routineName,
        dayName = dayName,
        exerciseName = exerciseName,
        exercisePosition = exercisePosition,
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
    return stats.copy(
        selectedPeriod = period,
        selectedRoutineName = selectedRoutineName,
        selectedDayName = selectedDayName,
        selectedExerciseScopeKey = selectedExerciseScopeKey,
        selectedExerciseName = selectedExerciseName,
        selectedProgressPoint = null,
        heatmapDays = heatmapDays,
        weeklyStepsData = weeklyStepsData,
        canGoToNextWeek = canGoToNextWeek
    ).withValidFocusSelection()
}

fun StatsUiState.withSelectedExercise(name: String): StatsUiState {
    val selectedName = exerciseProgress
        .firstOrNull { progress -> progress.exerciseName == name }
        ?.exerciseName
    return copy(
        selectedExerciseName = selectedName,
        selectedExerciseScopeKey = exerciseProgress.firstOrNull { it.exerciseName == selectedName }?.scopeKey,
        selectedProgressPoint = null
    ).withProgressPointsForSelection()
}

fun StatsUiState.withSelectedExerciseScope(scopeKey: String): StatsUiState {
    val selected = focusedExerciseProgress.firstOrNull { progress -> progress.scopeKey == scopeKey }
    return copy(
        selectedExerciseScopeKey = selected?.scopeKey,
        selectedExerciseName = selected?.exerciseName,
        selectedProgressPoint = null
    ).withProgressPointsForSelection()
}

fun StatsUiState.withValidFocusSelection(): StatsUiState {
    val routine = selectedRoutineName?.takeIf { routineName -> routineName in availableRoutineNames }
        ?: activeRoutineId?.let { activeId ->
            sessionVolumes.firstOrNull { session -> session.routineId == activeId }?.routineName
        }
        ?: availableRoutineNames.firstOrNull()
    val stateWithRoutine = copy(selectedRoutineName = routine)
    val day = stateWithRoutine.selectedDayName?.takeIf { dayName -> dayName in stateWithRoutine.availableDayNames }
        ?: stateWithRoutine.availableDayNames.firstOrNull()
    val stateWithDay = stateWithRoutine.copy(selectedDayName = day)
    val selectedExercise = stateWithDay.selectedExerciseScopeKey
        ?.let { scopeKey ->
            stateWithDay.focusedExerciseProgress.firstOrNull { progress -> progress.scopeKey == scopeKey }
        }
        ?: stateWithDay.focusedExerciseProgress.firstOrNull()

    return stateWithDay.copy(
        selectedExerciseScopeKey = selectedExercise?.scopeKey,
        selectedExerciseName = selectedExercise?.exerciseName,
        selectedProgressPoint = null
    ).withProgressPointsForSelection()
}

fun StatsUiState.withSelectedProgressPoint(sessionId: Long): StatsUiState {
    return copy(
        selectedProgressPoint = progressPoints.firstOrNull { point -> point.sessionId == sessionId }
    )
}

fun StatsUiState.withProgressPointsForSelection(): StatsUiState {
    val scopeKey = selectedExerciseScopeKey
        ?: return copy(progressPoints = emptyList(), selectedProgressPoint = null)
    val exercise = exerciseProgress.firstOrNull { it.scopeKey == scopeKey }
        ?: return copy(
            selectedExerciseScopeKey = null,
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

private fun spanishCollator(): Collator = Collator.getInstance(Locale("es", "ES"))
