package com.alvarocervantes.fittrackplus.feature.workout

import org.junit.Assert.assertEquals
import org.junit.Test

class WorkoutInputDefaultsTest {

    @Test
    fun suggestedRepsUsesPreviousCompletedSetWhenAvailable() {
        val result = suggestWorkoutSetRepsInput(
            previousCompletedReps = 10,
            targetRepsText = "8-12"
        )

        assertEquals("10", result)
    }

    @Test
    fun suggestedRepsFallsBackToRangeMinimum() {
        val result = suggestWorkoutSetRepsInput(
            previousCompletedReps = null,
            targetRepsText = "8-12"
        )

        assertEquals("8", result)
    }

    @Test
    fun suggestedRepsFallsBackToBlankWhenRangeIsNotParseable() {
        val result = suggestWorkoutSetRepsInput(
            previousCompletedReps = null,
            targetRepsText = "AMRAP"
        )

        assertEquals("", result)
    }

    @Test
    fun adjustRepsTextStepsByOneAndNeverBelowZero() {
        assertEquals("9", adjustWorkoutRepsInput(currentValue = "8", delta = 1))
        assertEquals("0", adjustWorkoutRepsInput(currentValue = "0", delta = -1))
        assertEquals("1", adjustWorkoutRepsInput(currentValue = "", delta = 1))
    }

    @Test
    fun adjustWeightTextSupportsDefaultAndLongPressSteps() {
        assertEquals("2.5", adjustWorkoutWeightInput(currentValue = "", deltaKg = 2.5))
        assertEquals("7.5", adjustWorkoutWeightInput(currentValue = "2.5", deltaKg = 5.0))
        assertEquals("0", adjustWorkoutWeightInput(currentValue = "2.5", deltaKg = -5.0))
    }
}
