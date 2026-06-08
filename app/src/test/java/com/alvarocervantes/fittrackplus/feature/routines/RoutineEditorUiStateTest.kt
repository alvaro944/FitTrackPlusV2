package com.alvarocervantes.fittrackplus.feature.routines

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutineEditorUiStateTest {

    @Test
    fun canSaveAcceptsReasonableTargetRepsFormats() {
        listOf("8", "8-12", "AMRAP", "RPE 8").forEach { targetReps ->
            val editor = validEditor(targetReps = targetReps)

            assertTrue("Expected $targetReps to be valid", editor.canSave)
        }
    }

    @Test
    fun canSaveRejectsBlankOrAbsurdTargetReps() {
        listOf("", "abc xyz", "8 to 12 maybe", "RPE hard", "8-").forEach { targetReps ->
            val editor = validEditor(targetReps = targetReps)

            assertFalse("Expected $targetReps to be invalid", editor.canSave)
        }
    }

    @Test
    fun canSaveRejectsMissingRequiredEditorFields() {
        assertFalse(validEditor(routineName = "").canSave)
        assertFalse(validEditor(dayName = "").canSave)
        assertFalse(validEditor(exerciseName = "").canSave)
        assertFalse(validEditor(targetSets = "0").canSave)
        assertFalse(validEditor(targetSets = "abc").canSave)
    }

    private fun validEditor(
        routineName: String = "Push Pull Legs",
        dayName: String = "Push",
        exerciseName: String = "Bench Press",
        targetSets: String = "3",
        targetReps: String = "8-12"
    ): RoutineEditorUiState {
        return RoutineEditorUiState(
            name = routineName,
            days = listOf(
                RoutineDayEditorUiState(
                    name = dayName,
                    exercises = listOf(
                        RoutineExerciseEditorUiState(
                            name = exerciseName,
                            targetSets = targetSets,
                            targetRepsText = targetReps
                        )
                    )
                )
            )
        )
    }
}
