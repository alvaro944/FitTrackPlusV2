package com.alvarocervantes.fittrackplus.feature.history

import org.junit.Assert.assertEquals
import org.junit.Test

class HistoryDetailUiStateTest {

    @Test
    fun exposesDurationVolumeAndBestSetFromHistoricalSnapshot() {
        val detail = HistoryDetailUiState(
            sessionId = 1,
            routineName = "Push",
            dayName = "Day 1",
            startedAt = 1_000,
            finishedAt = 181_000,
            weekNumber = 2,
            notes = "Good session",
            pausedMillis = 0,
            exercises = listOf(
                HistoryExerciseUiState(
                    exerciseId = 11,
                    name = "Bench Press",
                    targetRepsText = "8-12",
                    sets = listOf(
                        HistorySetUiState(
                            setId = 101,
                            setNumber = 1,
                            weightKg = 80.0,
                            reps = 8,
                            notes = "Controlled",
                            isCompleted = true
                        ),
                        HistorySetUiState(
                            setId = 102,
                            setNumber = 2,
                            weightKg = 85.0,
                            reps = 6,
                            notes = null,
                            isCompleted = true
                        )
                    )
                ),
                HistoryExerciseUiState(
                    exerciseId = 12,
                    name = "Dips",
                    targetRepsText = "AMRAP",
                    sets = listOf(
                        HistorySetUiState(
                            setId = 201,
                            setNumber = 1,
                            weightKg = 20.0,
                            reps = 12,
                            notes = null,
                            isCompleted = true
                        )
                    )
                )
            )
        )

        assertEquals(180_000, detail.durationMillis)
        assertEquals(1_390.0, detail.totalVolumeKg, 0.0)
        assertEquals("Bench Press", detail.bestSet?.exerciseName)
        assertEquals(80.0, detail.bestSet?.weightKg ?: -1.0, 0.0)
        assertEquals(8, detail.bestSet?.reps)
        assertEquals(640.0, detail.bestSet?.volumeKg ?: -1.0, 0.0)
    }

    @Test
    fun durationExcludesPausedTimeFromRecoveredSession() {
        val detail = HistoryDetailUiState(
            sessionId = 2,
            routineName = "Pull",
            dayName = "Day 2",
            startedAt = 0,
            finishedAt = 3_600_000, // 60 min of wall-clock between start and final finish
            weekNumber = 1,
            notes = null,
            pausedMillis = 1_800_000, // 30 min spent paused between finish-incomplete and recovery
            exercises = emptyList()
        )

        assertEquals(1_800_000, detail.durationMillis) // only the 30 min actually trained
    }
}
