package com.alvarocervantes.fittrackplus.feature.routines

import com.alvarocervantes.fittrackplus.domain.model.progression.ExerciseRole
import com.alvarocervantes.fittrackplus.domain.model.progression.ProgressionTuning
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The PRIMARY count is a recommendation, not a gate. The owner decided on 2026-09-18 that blocking
 * their own routine over a product heuristic was paternalistic: the engine has no cross-exercise
 * fatigue, so many primaries make the model less reliable, and that is said in the guidance text
 * instead of being refused.
 */
class RoutineProgressionRoleTest {

    @Test
    fun `marking primary is never refused, however many there already are`() {
        var editor = editorWithFourExercises()

        editor.days[0].exercises.indices.forEach { index ->
            editor = editor.withExerciseProgressionRole(0, index, ExerciseRole.PRIMARY).editor
        }

        assertEquals(4, editor.primaryExerciseCount())
        assertTrue(editor.primaryExerciseCount() > ProgressionTuning.MAX_PRIMARY_EXERCISES)
    }

    @Test
    fun `the count spans every day of the routine`() {
        val editor = editorWithTwoDays()
            .withExerciseProgressionRole(0, 0, ExerciseRole.PRIMARY).editor
            .withExerciseProgressionRole(1, 1, ExerciseRole.PRIMARY).editor

        assertEquals(2, editor.primaryExerciseCount())
    }

    @Test
    fun `demoting frees the exercise from the count`() {
        val atTwo = editorWithFourExercises()
            .withExerciseProgressionRole(0, 0, ExerciseRole.PRIMARY).editor
            .withExerciseProgressionRole(0, 1, ExerciseRole.PRIMARY).editor

        val afterDemote = atTwo.withExerciseProgressionRole(0, 0, ExerciseRole.ACCESSORY).editor

        assertEquals(1, afterDemote.primaryExerciseCount())
    }

    @Test
    fun `promoting seeds the load increment from the movement`() {
        val editor = editorWithFourExercises()

        val upper = editor.withExerciseProgressionRole(0, 0, ExerciseRole.PRIMARY)
        val lower = editor.withExerciseProgressionRole(0, 3, ExerciseRole.PRIMARY)

        assertEquals(
            ProgressionTuning.DEFAULT_INCREMENT_UPPER_KG.toString(),
            upper.editor.days[0].exercises[0].loadIncrementKg
        )
        assertEquals(
            ProgressionTuning.DEFAULT_INCREMENT_LOWER_KG.toString(),
            lower.editor.days[0].exercises[3].loadIncrementKg
        )
    }

    private fun editorWithFourExercises(): RoutineEditorUiState {
        return RoutineEditorUiState(
            routineId = 1L,
            name = "Push/Pull/Legs",
            days = listOf(
                RoutineDayEditorUiState(
                    name = "Push",
                    exercises = listOf(
                        roleExercise(10L, "Press banca"),
                        roleExercise(11L, "Press militar"),
                        roleExercise(12L, "Fondos"),
                        roleExercise(13L, "Sentadilla")
                    )
                )
            )
        )
    }

    private fun editorWithTwoDays(): RoutineEditorUiState {
        return RoutineEditorUiState(
            routineId = 1L,
            name = "Push/Pull",
            days = listOf(
                RoutineDayEditorUiState(
                    name = "Push",
                    exercises = listOf(roleExercise(10L, "Press banca"), roleExercise(11L, "Press militar"))
                ),
                RoutineDayEditorUiState(
                    name = "Pull",
                    exercises = listOf(roleExercise(20L, "Dominadas"), roleExercise(21L, "Remo"))
                )
            )
        )
    }

    private fun roleExercise(id: Long, name: String): RoutineExerciseEditorUiState {
        return RoutineExerciseEditorUiState(
            routineExerciseId = id,
            variantKey = "variant-$id",
            name = name,
            targetSets = "3",
            targetRepsText = "8-12"
        )
    }
}

/**
 * Regression tests for the bug found on 2026-09-18 during the owner manual pass: duplicating an
 * exercise or a day cloned the PRIMARY role, so copies silently became primaries nobody chose.
 *
 * The count is no longer a gate, but the role still carries meaning: an inherited PRIMARY produces
 * an engine-managed exercise the owner never asked for, carrying a goal and an increment copied
 * from a different exercise.
 */
class RoutineDuplicationPrimaryTest {

    @Test
    fun `duplicating a primary exercise does not clone the role`() {
        val editor = editorWithOnePrimary()

        val duplicated = editor.duplicateExercise(dayIndex = 0, exerciseIndex = 0)

        assertEquals(1, duplicated.primaryExerciseCount())
        assertEquals(ExerciseRole.PRIMARY, duplicated.days[0].exercises[0].progressionRole)
        assertEquals(ExerciseRole.SECONDARY, duplicated.days[0].exercises[1].progressionRole)
    }

    @Test
    fun `duplicating a day does not clone its primary roles`() {
        val editor = editorWithOnePrimary()

        val duplicated = editor.duplicateDay(dayIndex = 0)

        assertEquals(1, duplicated.primaryExerciseCount())
        assertEquals(ExerciseRole.SECONDARY, duplicated.days[1].exercises[0].progressionRole)
    }

    @Test
    fun `duplication never adds primaries the owner did not choose`() {
        var editor = editorWithFourExercisesForDuplication()
        repeat(ProgressionTuning.MAX_PRIMARY_EXERCISES) { index ->
            editor = editor.withExerciseProgressionRole(0, index, ExerciseRole.PRIMARY).editor
        }
        assertEquals(ProgressionTuning.MAX_PRIMARY_EXERCISES, editor.primaryExerciseCount())

        val afterExerciseCopy = editor.duplicateExercise(dayIndex = 0, exerciseIndex = 0)
        val afterDayCopy = afterExerciseCopy.duplicateDay(dayIndex = 0)

        // Three chosen primaries, two copy operations, still three primaries.
        assertEquals(ProgressionTuning.MAX_PRIMARY_EXERCISES, afterDayCopy.primaryExerciseCount())
    }

    @Test
    fun `duplicating a primary clears its goal and increment`() {
        val base = editorWithOnePrimary()
        val withGoal = base.days[0].exercises[0].copy(goalWeightKg = "100", loadIncrementKg = "2.5")
        val editor = base.copy(
            days = listOf(
                base.days[0].copy(
                    exercises = listOf(withGoal) + base.days[0].exercises.drop(1)
                )
            )
        )

        val duplicated = editor.duplicateExercise(dayIndex = 0, exerciseIndex = 0)

        assertEquals("", duplicated.days[0].exercises[1].goalWeightKg)
        assertEquals("", duplicated.days[0].exercises[1].loadIncrementKg)
    }

    @Test
    fun `duplicating a non primary keeps its role untouched`() {
        val editor = editorWithFourExercisesForDuplication()
            .withExerciseProgressionRole(0, 1, ExerciseRole.SECONDARY).editor

        val duplicated = editor.duplicateExercise(dayIndex = 0, exerciseIndex = 1)

        assertEquals(ExerciseRole.SECONDARY, duplicated.days[0].exercises[2].progressionRole)
    }

    private fun editorWithOnePrimary(): RoutineEditorUiState {
        return editorWithFourExercisesForDuplication()
            .withExerciseProgressionRole(0, 0, ExerciseRole.PRIMARY).editor
    }

    private fun editorWithFourExercisesForDuplication(): RoutineEditorUiState {
        return RoutineEditorUiState(
            routineId = 1L,
            name = "Push/Pull/Legs",
            days = listOf(
                RoutineDayEditorUiState(
                    name = "Push",
                    exercises = listOf(
                        duplicationExercise(10L, "Press banca"),
                        duplicationExercise(11L, "Press militar"),
                        duplicationExercise(12L, "Fondos"),
                        duplicationExercise(13L, "Sentadilla")
                    )
                )
            )
        )
    }

    private fun duplicationExercise(id: Long, name: String): RoutineExerciseEditorUiState {
        return RoutineExerciseEditorUiState(
            routineExerciseId = id,
            variantKey = "variant-$id",
            name = name,
            targetSets = "3",
            targetRepsText = "8-12"
        )
    }
}
