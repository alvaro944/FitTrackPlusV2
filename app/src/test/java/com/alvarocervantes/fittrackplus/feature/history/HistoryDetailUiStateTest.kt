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
                            notes = "Controlled"
                        ),
                        HistorySetUiState(
                            setId = 102,
                            setNumber = 2,
                            weightKg = 85.0,
                            reps = 6,
                            notes = null
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
                            notes = null
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
}
