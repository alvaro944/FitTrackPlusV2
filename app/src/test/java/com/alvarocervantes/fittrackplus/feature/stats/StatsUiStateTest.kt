package com.alvarocervantes.fittrackplus.feature.stats

import com.alvarocervantes.fittrackplus.domain.model.WorkoutStatsPeriod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StatsUiStateTest {

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
            selectedExerciseName = "Bench Press",
            selectedProgressPoint = progressPoint(sessionId = 2)
        )

        val updated = state.withStatsPeriod(
            period = WorkoutStatsPeriod.LastFourWeeks,
            stats = sampleStatsUiState()
        )

        assertEquals(WorkoutStatsPeriod.LastFourWeeks, updated.selectedPeriod)
        assertEquals("Bench Press", updated.selectedExerciseName)
        assertNull(updated.selectedProgressPoint)
        assertEquals(listOf(1L, 2L), updated.progressPoints.map { it.sessionId })
    }

    @Test
    fun setStatsPeriod_clearsExerciseWhenItNoLongerExists() {
        val state = sampleState(selectedExerciseName = "Bench Press")

        val updated = state.withStatsPeriod(
            period = WorkoutStatsPeriod.LastFourWeeks,
            stats = sampleStatsUiState(exerciseProgress = emptyList())
        )

        assertNull(updated.selectedExerciseName)
        assertEquals(emptyList<ProgressChartPointUiState>(), updated.progressPoints)
        assertNull(updated.selectedProgressPoint)
    }

    private fun sampleState(
        selectedPeriod: WorkoutStatsPeriod = WorkoutStatsPeriod.All,
        selectedExerciseName: String? = null,
        selectedProgressPoint: ProgressChartPointUiState? = null
    ): StatsUiState = sampleStatsUiState().copy(
        selectedPeriod = selectedPeriod,
        selectedExerciseName = selectedExerciseName,
        selectedProgressPoint = selectedProgressPoint
    ).withProgressPointsForSelection()

    private fun sampleStatsUiState(
        exerciseProgress: List<ExerciseProgressUiState> = listOf(sampleExerciseProgress())
    ): StatsUiState = StatsUiState(
        isLoading = false,
        sessionVolumes = listOf(
            SessionVolumeUiState(
                sessionId = 1,
                routineName = "PPL",
                dayName = "Push",
                finishedAt = 100,
                totalVolumeKg = 500.0
            )
        ),
        exerciseProgress = exerciseProgress,
        exerciseRecords = emptyList()
    )

    private fun sampleExerciseProgress(): ExerciseProgressUiState = ExerciseProgressUiState(
        exerciseKey = "bench press",
        exerciseName = "Bench Press",
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

    private fun progressPoint(sessionId: Long): ProgressChartPointUiState = ProgressChartPointUiState(
        sessionId = sessionId,
        finishedAt = sessionId * 100,
        maxWeightKg = 90.0,
        volumeKg = 500.0,
        totalReps = 8,
        estimatedOneRepMaxKg = 114.0
    )
}
