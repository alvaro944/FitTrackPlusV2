package com.alvarocervantes.fittrackplus.feature.workout

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.alvarocervantes.fittrackplus.core.design.components.maybeSelectAllOnFocusValue
import com.alvarocervantes.fittrackplus.core.design.components.SelectAllArming
import com.alvarocervantes.fittrackplus.core.design.components.selectAllOnFocusValue
import com.alvarocervantes.fittrackplus.core.design.components.syncTextFieldValue
import com.alvarocervantes.fittrackplus.domain.model.PrType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
    fun isWorkoutSetCompleted_acceptsBodyweightSetsWithPositiveReps() {
        assertTrue(isWorkoutSetCompleted(repsText = "8"))
        assertFalse(isWorkoutSetCompleted(repsText = "0"))
        assertTrue(isWorkoutSetCompleted(repsText = "10"))
    }

    @Test
    fun workoutSetReadyToComplete_requiresPositiveRepsAndNotCompleted() {
        assertTrue(isWorkoutSetReadyToComplete(repsText = "10", isCompleted = false))
        assertFalse(isWorkoutSetReadyToComplete(repsText = "10", isCompleted = true))
        assertFalse(isWorkoutSetReadyToComplete(repsText = "0", isCompleted = false))
    }

    @Test
    fun updateWorkoutSetInput_keepsSetUncompletedWhenInputsBecomeReady() {
        val set = WorkoutSetUiState(
            id = 1,
            setNumber = 1,
            weightText = "",
            repsText = "10",
            isCompleted = false
        )

        val result = updateWorkoutSetWeightInput(set, weightText = "60")

        assertEquals("60", result.weightText)
        assertFalse(result.isCompleted)
        assertTrue(isWorkoutSetReadyToComplete(result.repsText, result.isCompleted))
    }

    @Test
    fun editingCompletedSetClearsItsPersonalRecordMarker() {
        val set = WorkoutSetUiState(
            id = 1,
            setNumber = 1,
            weightText = "60",
            repsText = "10",
            isCompleted = true,
            prType = PrType.MaxWeight
        )

        assertEquals(null, updateWorkoutSetWeightInput(set, "62,5").prType)
        assertEquals(null, updateWorkoutSetRepsInput(set, "11").prType)
    }

    @Test
    fun shouldAutoStartRestTimerOnSetCompletion_onlyTriggersWhenLastFieldCompletesSet() {
        val timer = RestTimerUiState(autoStartEnabled = true)

        assertTrue(
            shouldAutoStartRestTimerOnSetCompletion(
                previousRepsText = "",
                nextRepsText = "10",
                timer = timer
            )
        )
        assertTrue(
            shouldAutoStartRestTimerOnSetCompletion(
                previousRepsText = "",
                nextRepsText = "10",
                timer = timer
            )
        )
        assertFalse(
            shouldAutoStartRestTimerOnSetCompletion(
                previousRepsText = "10",
                nextRepsText = "10",
                timer = timer
            )
        )
    }

    @Test
    fun shouldAutoStartRestTimerOnManualSetCompletion_respectsAutoStartAndTimerStatus() {
        assertTrue(shouldAutoStartRestTimerOnManualSetCompletion(RestTimerUiState(autoStartEnabled = true)))
        assertFalse(shouldAutoStartRestTimerOnManualSetCompletion(RestTimerUiState(autoStartEnabled = false)))
        assertFalse(
            shouldAutoStartRestTimerOnManualSetCompletion(
                RestTimerUiState(autoStartEnabled = true, status = RestTimerStatus.Running)
            )
        )
    }

    @Test
    fun formatPreviousWorkoutLabels_areSeparatedForWeightAndReps() {
        assertEquals(
            "ant. 50 kg",
            formatPreviousWeightLabel("50")
        )
        assertEquals(
            "ant. 12",
            formatPreviousRepsLabel(12)
        )
    }

    @Test
    fun sanitizeWeightInputKeepsOnlyOneDecimalSeparatorAndNormalizesComma() {
        assertEquals("12,5", sanitizeWorkoutWeightInput("12.5"))
        assertEquals("12,5", sanitizeWorkoutWeightInput("12,,5"))
        assertEquals("12,5", sanitizeWorkoutWeightInput("1a2,5x"))
        assertEquals(",5", sanitizeWorkoutWeightInput("..5"))
    }

    @Test
    fun sanitizeWeightInputStopsAtScientificNotationInsteadOfMangling() {
        // Pasted scientific notation ("1.0E7") used to silently become a wrong-but-plausible
        // "1,07" by dropping only the "E" and keeping the trailing exponent digits.
        assertEquals("1,0", sanitizeWorkoutWeightInput("1.0E7"))
        assertEquals("1,0", sanitizeWorkoutWeightInput("1.0e-7"))
    }

    @Test
    fun sanitizeRepsInputKeepsOnlyLeadingDigits() {
        assertEquals("12", sanitizeWorkoutRepsInput("12"))
        assertEquals("", sanitizeWorkoutRepsInput("-5"))
        assertEquals("12", sanitizeWorkoutRepsInput("12x"))
        assertEquals("", sanitizeWorkoutRepsInput("abc"))
    }

    @Test
    fun toInputTextNeverEmitsScientificNotation() {
        // A whole number this large already goes through the toInt() branch, so the case that
        // used to break was a large *fractional* value, where Double.toString() switches to
        // exponential form (e.g. "1.23456785E7") instead of a plain decimal.
        assertEquals("12345678,5", 12_345_678.5.toInputText())
        assertEquals("12,5", 12.5.toInputText())
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
    fun selectAllOnFocusValue_selectsFullText() {
        val result = selectAllOnFocusValue(
            TextFieldValue(
                text = "100",
                selection = TextRange(1, 1)
            )
        )

        assertEquals(TextRange(0, 3), result.selection)
    }

    @Test
    fun maybeSelectAllOnFocusValue_keepsCaretWhenSelectAllIsDisabled() {
        val result = maybeSelectAllOnFocusValue(
            current = TextFieldValue(
                text = "Bench Press",
                selection = TextRange(5, 5)
            ),
            selectAllOnFocus = false
        )

        assertEquals(TextRange(5, 5), result.selection)
    }

    @Test
    fun syncTextFieldValue_updatesTextWhenExternalValueChanges() {
        val result = syncTextFieldValue(
            current = TextFieldValue(
                text = "8",
                selection = TextRange(0, 1)
            ),
            externalText = "12"
        )

        assertEquals("12", result.text)
        assertEquals(TextRange(2, 2), result.selection)
    }

    @Test
    fun repsInputStopsAtThreeDigits() {
        assertEquals("123", sanitizeWorkoutRepsInput("1234"))
        assertEquals("12", sanitizeWorkoutRepsInput("12"))
    }

    @Test
    fun weightInputCapsDigitsOnEachSideOfTheSeparator() {
        assertEquals("1234", sanitizeWorkoutWeightInput("12345"))
        assertEquals("123,45", sanitizeWorkoutWeightInput("123,456"))
        assertEquals("1234,5", sanitizeWorkoutWeightInput("1234,5"))
        // Once the integer cap is reached the rest is dropped, separator included.
        assertEquals("1234", sanitizeWorkoutWeightInput("123456,789"))
    }

    @Test
    fun weightInputKeepsRejectingScientificNotation() {
        assertEquals("1,0", sanitizeWorkoutWeightInput("1.0E7"))
    }

    @Test
    fun selectAllArmingFiresOnceUntilItIsRearmed() {
        val arming = SelectAllArming()

        assertTrue(arming.consume())
        assertFalse(arming.consume())

        arming.rearm()
        assertTrue(arming.consume())
    }


    @Test
    fun clearingRepsOnTheSetBeingEditedIsNotRefilledBySuggestions() {
        val sets = listOf(
            WorkoutSetUiState(id = 1L, setNumber = 1, weightText = "60", repsText = "12", isCompleted = true),
            WorkoutSetUiState(id = 2L, setNumber = 2, weightText = "", repsText = "")
        )

        val result = applyWorkoutSetInputSuggestions(
            sets = sets,
            targetRepsText = "8-12",
            skipSetId = 2L
        )

        // The row the user just cleared stays cleared instead of a suggestion reappearing in it.
        assertEquals("", result[1].repsText)
    }

    @Test
    fun otherSetsStillGetTheirSuggestionWhileOneIsBeingEdited() {
        val sets = listOf(
            WorkoutSetUiState(id = 1L, setNumber = 1, weightText = "60", repsText = "10", isCompleted = true),
            WorkoutSetUiState(id = 2L, setNumber = 2, weightText = "", repsText = ""),
            WorkoutSetUiState(id = 3L, setNumber = 3, weightText = "", repsText = "")
        )

        val result = applyWorkoutSetInputSuggestions(
            sets = sets,
            targetRepsText = "8-12",
            skipSetId = 2L
        )

        assertEquals("", result[1].repsText)
        assertEquals("10", result[2].repsText)
    }


}
