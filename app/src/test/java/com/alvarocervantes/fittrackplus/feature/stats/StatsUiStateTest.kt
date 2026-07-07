package com.alvarocervantes.fittrackplus.feature.stats

import com.alvarocervantes.fittrackplus.domain.model.WorkoutStatsPeriod
import com.alvarocervantes.fittrackplus.domain.model.HeatmapDay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StatsUiStateTest {

    @Test
    fun defaultStatsPeriodStartsAtLastFourWeeks() {
        assertEquals(WorkoutStatsPeriod.LastFourWeeks, StatsUiState().selectedPeriod)
    }

    @Test
    fun selectExercise_keepsChronologicalProgressPointsAndClearsSelectedPoint() {
        val state = sampleState(selectedProgressPoint = progressPoint(sessionId = 99))

        val updated = state.withSelectedExercise("Bench Press")

        assertEquals("Bench Press", updated.selectedExerciseName)
        assertEquals(listOf(1L, 2L), updated.progressPoints.map { it.sessionId })
        assertNull(updated.selectedProgressPoint)
    }

    @Test
    fun selectExercise_clearsSelectionWhenExerciseDoesNotExist() {
        val state = sampleState(selectedExerciseName = "Bench Press")

        val updated = state.withSelectedExercise("Squat")

        assertNull(updated.selectedExerciseName)
        assertEquals(emptyList<ProgressChartPointUiState>(), updated.progressPoints)
        assertNull(updated.selectedProgressPoint)
    }

    @Test
    fun selectProgressPoint_setsPointFromCurrentProgressPoints() {
        val state = sampleState().withSelectedExercise("Bench Press")

        val updated = state.withSelectedProgressPoint(sessionId = 2L)

        assertEquals(2L, updated.selectedProgressPoint?.sessionId)
        assertEquals(95.0, updated.selectedProgressPoint?.maxWeightKg ?: -1.0, 0.0)
    }

    @Test
    fun setStatsPeriod_preservesExistingExerciseWhenStillPresentAndClearsPoint() {
        val state = sampleState(
            selectedPeriod = WorkoutStatsPeriod.All,
            selectedExerciseScopeKey = "ppl|push|bench press",
            selectedProgressPoint = progressPoint(sessionId = 2)
        )

        val updated = state.withStatsPeriod(
            period = WorkoutStatsPeriod.LastFourWeeks,
            stats = sampleStatsUiState()
        )

        assertEquals(WorkoutStatsPeriod.LastFourWeeks, updated.selectedPeriod)
        assertEquals("Bench Press", updated.selectedExerciseName)
        assertEquals("ppl|push|bench press", updated.selectedExerciseScopeKey)
        assertNull(updated.selectedProgressPoint)
        assertEquals(listOf(1L, 2L), updated.progressPoints.map { it.sessionId })
    }

    @Test
    fun setStatsPeriod_clearsExerciseWhenItNoLongerExists() {
        val state = sampleState(selectedExerciseScopeKey = "ppl|push|bench press")

        val updated = state.withStatsPeriod(
            period = WorkoutStatsPeriod.LastFourWeeks,
            stats = sampleStatsUiState(exerciseProgress = emptyList())
        )

        assertNull(updated.selectedExerciseName)
        assertEquals(emptyList<ProgressChartPointUiState>(), updated.progressPoints)
        assertNull(updated.selectedProgressPoint)
    }

    @Test
    fun setStatsPeriodPreservesHeatmapDays() {
        val heatmap = listOf(HeatmapDay(epochDay = 1, totalVolumeKg = 500.0, intensityLevel = 2))
        val state = sampleState().copy(heatmapDays = heatmap)

        val updated = state.withStatsPeriod(
            period = WorkoutStatsPeriod.LastFourWeeks,
            stats = sampleStatsUiState()
        )

        assertEquals(heatmap, updated.heatmapDays)
    }

    @Test
    fun focusedDataUsesSelectedRoutineAndDay() {
        val state = sampleStatsUiState(
            sessionVolumes = listOf(
                sessionVolume(1, routineName = "Rutina Álvaro", dayName = "Pierna"),
                sessionVolume(2, routineName = "Rutina prueba", dayName = "Push"),
                sessionVolume(3, routineName = "Rutina Álvaro", dayName = "Push")
            ),
            exerciseProgress = listOf(
                sampleExerciseProgress(
                    scopeKey = "alvaro|pierna|squat",
                    routineName = "Rutina Álvaro",
                    dayName = "Pierna",
                    exerciseName = "Sentadilla",
                    exercisePosition = 0
                ),
                sampleExerciseProgress(
                    scopeKey = "alvaro|pierna|rdl",
                    routineName = "Rutina Álvaro",
                    dayName = "Pierna",
                    exerciseName = "Peso muerto rumano",
                    exercisePosition = 1
                ),
                sampleExerciseProgress(
                    scopeKey = "test|push|bench",
                    routineName = "Rutina prueba",
                    dayName = "Push",
                    exerciseName = "Press banca",
                    exercisePosition = 0
                )
            ),
            exerciseRecords = listOf(
                sampleExerciseRecords("alvaro|pierna|squat", "Rutina Álvaro", "Pierna", "Sentadilla", 0),
                sampleExerciseRecords("alvaro|pierna|rdl", "Rutina Álvaro", "Pierna", "Peso muerto rumano", 1),
                sampleExerciseRecords("test|push|bench", "Rutina prueba", "Push", "Press banca", 0)
            )
        ).copy(
            selectedRoutineName = "Rutina Álvaro",
            selectedDayName = "Pierna"
        ).withValidFocusSelection()

        assertEquals(listOf("Rutina Álvaro", "Rutina prueba"), state.availableRoutineNames)
        assertEquals(listOf("Pierna", "Push"), state.availableDayNames)
        assertEquals(listOf(1L), state.focusedSessionVolumes.map { it.sessionId })
        assertEquals(
            listOf("Sentadilla", "Peso muerto rumano"),
            state.focusedExerciseProgress.map { it.exerciseName }
        )
        assertEquals(
            listOf("Sentadilla", "Peso muerto rumano"),
            state.focusedExerciseRecords.map { it.exerciseName }
        )
    }

    @Test
    fun activeRoutineIsPreferredWhenAvailable() {
        val state = sampleStatsUiState(
            sessionVolumes = listOf(
                sessionVolume(1, routineId = 10, routineName = "Rutina prueba", dayId = 100, dayName = "Push"),
                sessionVolume(2, routineId = 20, routineName = "Rutina Álvaro", dayId = 200, dayName = "Pierna")
            )
        ).copy(activeRoutineId = 20)
            .withValidFocusSelection()

        assertEquals("Rutina Álvaro", state.selectedRoutineName)
        assertEquals("Pierna", state.selectedDayName)
    }

    @Test
    fun summaryUsesExerciseAppearancesInsteadOfBestSessionVolume() {
        val state = sampleStatsUiState(
            sessionVolumes = listOf(
                sessionVolume(1, totalVolumeKg = 500.0),
                sessionVolume(2, totalVolumeKg = 750.0),
                sessionVolume(3, totalVolumeKg = 650.0)
            )
        ).copy(selectedPeriod = WorkoutStatsPeriod.All)

        assertEquals(3, state.sessionCount)
        assertEquals(2, state.exerciseCount)
    }

    @Test
    fun selectedExerciseLimitsProgressAndRecordsDetail() {
        val state = sampleStatsUiState(
            exerciseProgress = listOf(
                sampleExerciseProgress(
                    scopeKey = "alvaro|pierna|squat",
                    routineName = "Rutina Álvaro",
                    dayName = "Pierna",
                    exerciseName = "Sentadilla",
                    exercisePosition = 0
                ),
                sampleExerciseProgress(
                    scopeKey = "alvaro|pierna|rdl",
                    routineName = "Rutina Álvaro",
                    dayName = "Pierna",
                    exerciseName = "Peso muerto rumano",
                    exercisePosition = 1
                )
            ),
            exerciseRecords = listOf(
                sampleExerciseRecords("alvaro|pierna|squat", "Rutina Álvaro", "Pierna", "Sentadilla", 0),
                sampleExerciseRecords("alvaro|pierna|rdl", "Rutina Álvaro", "Pierna", "Peso muerto rumano", 1)
            )
        ).copy(
            selectedRoutineName = "Rutina Álvaro",
            selectedDayName = "Pierna",
            selectedExerciseScopeKey = "alvaro|pierna|rdl",
            selectedExerciseName = "Peso muerto rumano"
        )

        assertEquals("Peso muerto rumano", state.selectedExerciseProgress?.exerciseName)
        assertEquals("Peso muerto rumano", state.selectedExerciseRecords?.exerciseName)
    }

    @Test
    fun focusedSessionVolumesAreChronologicalForTrendCharts() {
        val state = sampleStatsUiState(
            sessionVolumes = listOf(
                sessionVolume(3),
                sessionVolume(1),
                sessionVolume(2)
            )
        )

        assertEquals(listOf(1L, 2L, 3L), state.focusedSessionVolumesChronological.map { it.sessionId })
    }

    private fun sampleState(
        selectedPeriod: WorkoutStatsPeriod = WorkoutStatsPeriod.All,
        selectedExerciseName: String? = null,
        selectedExerciseScopeKey: String? = null,
        selectedProgressPoint: ProgressChartPointUiState? = null
    ): StatsUiState = sampleStatsUiState().copy(
        selectedPeriod = selectedPeriod,
        selectedExerciseName = selectedExerciseName,
        selectedExerciseScopeKey = selectedExerciseScopeKey,
        selectedProgressPoint = selectedProgressPoint
    ).withProgressPointsForSelection()

    private fun sampleStatsUiState(
        sessionVolumes: List<SessionVolumeUiState> = listOf(sessionVolume()),
        exerciseProgress: List<ExerciseProgressUiState> = listOf(sampleExerciseProgress()),
        exerciseRecords: List<ExerciseRecordsUiState> = emptyList()
    ): StatsUiState = StatsUiState(
        isLoading = false,
        sessionVolumes = sessionVolumes,
        exerciseProgress = exerciseProgress,
        exerciseRecords = exerciseRecords
    )

    private fun sessionVolume(
        sessionId: Long = 1,
        routineId: Long? = 1,
        routineName: String = "PPL",
        dayId: Long? = 10,
        dayName: String = "Push",
        totalVolumeKg: Double = 500.0
    ): SessionVolumeUiState = SessionVolumeUiState(
        sessionId = sessionId,
        routineId = routineId,
        routineName = routineName,
        dayId = dayId,
        dayName = dayName,
        finishedAt = sessionId * 100,
        totalVolumeKg = totalVolumeKg
    )

    private fun sampleExerciseProgress(
        scopeKey: String = "ppl|push|bench press",
        routineName: String = "PPL",
        dayName: String = "Push",
        exerciseName: String = "Bench Press",
        exercisePosition: Int = 0
    ): ExerciseProgressUiState = ExerciseProgressUiState(
        scopeKey = scopeKey,
        exerciseKey = "bench press",
        routineName = routineName,
        dayName = dayName,
        exerciseName = exerciseName,
        exercisePosition = exercisePosition,
        entries = listOf(
            ExerciseProgressEntryUiState(
                sessionId = 2,
                finishedAt = 200,
                volumeKg = 800.0,
                maxWeightKg = 95.0,
                totalReps = 10,
                estimatedOneRepMaxKg = 126.6
            ),
            ExerciseProgressEntryUiState(
                sessionId = 1,
                finishedAt = 100,
                volumeKg = 500.0,
                maxWeightKg = 90.0,
                totalReps = 8,
                estimatedOneRepMaxKg = 114.0
            )
        )
    )

    private fun sampleExerciseRecords(
        scopeKey: String,
        routineName: String,
        dayName: String,
        exerciseName: String,
        exercisePosition: Int
    ): ExerciseRecordsUiState = ExerciseRecordsUiState(
        scopeKey = scopeKey,
        exerciseKey = scopeKey.substringAfterLast("|"),
        routineName = routineName,
        dayName = dayName,
        exerciseName = exerciseName,
        exercisePosition = exercisePosition,
        maxWeight = null,
        maxReps = null,
        bestSetVolume = null,
        bestEstimatedOneRepMax = null
    )

    private fun progressPoint(sessionId: Long): ProgressChartPointUiState = ProgressChartPointUiState(
        sessionId = sessionId,
        finishedAt = sessionId * 100,
        maxWeightKg = 90.0,
        volumeKg = 500.0,
        totalReps = 8,
        estimatedOneRepMaxKg = 114.0
    )
}
