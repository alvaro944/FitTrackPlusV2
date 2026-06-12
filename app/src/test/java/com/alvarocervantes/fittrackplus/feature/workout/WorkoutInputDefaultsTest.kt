package com.alvarocervantes.fittrackplus.feature.workout

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
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
        assertEquals("2,5", adjustWorkoutWeightInput(currentValue = "", deltaKg = 2.5))
        assertEquals("7,5", adjustWorkoutWeightInput(currentValue = "2,5", deltaKg = 5.0))
        assertEquals("0", adjustWorkoutWeightInput(currentValue = "2.5", deltaKg = -5.0))
    }

    @Test
    fun sanitizeWeightInputKeepsOnlyOneDecimalSeparatorAndNormalizesComma() {
        assertEquals("12,5", sanitizeWorkoutWeightInput("12.5"))
        assertEquals("12,5", sanitizeWorkoutWeightInput("12,,5"))
        assertEquals("12,5", sanitizeWorkoutWeightInput("1a2,5x"))
        assertEquals(",5", sanitizeWorkoutWeightInput("..5"))
    }

    @Test
    fun parseWeightInputAcceptsCommaDecimalSeparator() {
        assertEquals(12.5, parseWorkoutWeightInput("12,5") ?: -1.0, 0.0)
        assertEquals(12.5, parseWorkoutWeightInput("12.5") ?: -1.0, 0.0)
        assertEquals(null, parseWorkoutWeightInput("abc"))
    }

    @Test
    fun applyWorkoutSetInputSuggestions_preservesExistingIncompleteReps() {
        val sets = listOf(
            WorkoutSetUiState(
                id = 1,
                setNumber = 1,
                weightText = "",
                repsText = "9",
                isCompleted = true
            ),
            WorkoutSetUiState(
                id = 2,
                setNumber = 2,
                weightText = "",
                repsText = "8",
                isCompleted = false
            ),
            WorkoutSetUiState(
                id = 3,
                setNumber = 3,
                weightText = "",
                repsText = "",
                isCompleted = false
            )
        )

        val result = applyWorkoutSetInputSuggestions(
            sets = sets,
            targetRepsText = "8-10"
        )

        assertEquals("9", result[0].repsText)
        assertEquals("8", result[1].repsText)
        assertEquals("9", result[2].repsText)
    }

    @Test
    fun updateWorkoutExercisesForSet_onlyMutatesMatchingSetId() {
        val exercises = listOf(
            WorkoutExerciseUiState(
                id = 10,
                exerciseTemplateId = null,
                variantKey = "row-1",
                name = "Remo",
                targetRepsText = "8-10",
                sets = listOf(
                    WorkoutSetUiState(
                        id = 101,
                        setNumber = 1,
                        weightText = "",
                        repsText = "8"
                    ),
                    WorkoutSetUiState(
                        id = 102,
                        setNumber = 2,
                        weightText = "",
                        repsText = "8"
                    )
                )
            ),
            WorkoutExerciseUiState(
                id = 20,
                exerciseTemplateId = null,
                variantKey = "curl-1",
                name = "Curl",
                targetRepsText = "10-12",
                sets = listOf(
                    WorkoutSetUiState(
                        id = 201,
                        setNumber = 1,
                        weightText = "",
                        repsText = "10"
                    )
                )
            )
        )

        val result = updateWorkoutExercisesForSet(exercises, setId = 101) {
            it.copy(repsText = "9", isCompleted = true)
        }

        assertEquals("9", result[0].sets[0].repsText)
        assertEquals("8", result[0].sets[1].repsText)
        assertEquals("10", result[1].sets[0].repsText)
    }

    @Test
    fun selectAllWorkoutFieldValue_selectsFullText() {
        val result = selectAllWorkoutFieldValue(
            TextFieldValue(
                text = "100",
                selection = TextRange(1, 1)
            )
        )

        assertEquals(TextRange(0, 3), result.selection)
    }

    @Test
    fun syncWorkoutFieldValue_updatesTextWhenExternalValueChanges() {
        val result = syncWorkoutFieldValue(
            current = TextFieldValue(
                text = "8",
                selection = TextRange(0, 1)
            ),
            externalText = "12"
        )

        assertEquals("12", result.text)
        assertEquals(TextRange(2, 2), result.selection)
    }
}
