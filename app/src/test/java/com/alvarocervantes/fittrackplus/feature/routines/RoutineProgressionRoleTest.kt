package com.alvarocervantes.fittrackplus.feature.routines

import com.alvarocervantes.fittrackplus.domain.model.progression.ExerciseRole
import com.alvarocervantes.fittrackplus.domain.model.progression.ProgressionTuning
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Covers acceptance criterion 24 of the progression engine spec: a routine accepts at most
 * [ProgressionTuning.MAX_PRIMARY_EXERCISES] PRIMARY exercises, and the fourth is rejected with
 * an explicit signal rather than silently ignored.
 *
 * The limit is a product decision, not a physiological law: PRIMARY implies fatigue management,
 * and V1 has no cross-exercise fatigue, so the model would lie with more primaries.
 */
class RoutineProgressionRoleTest {

    @Test
    fun `marking up to the limit succeeds`() {
        var editor = editorWithFourExercises()

        repeat(ProgressionTuning.MAX_PRIMARY_EXERCISES) { index ->
            val result = editor.withExerciseProgressionRole(0, index, ExerciseRole.PRIMARY)
            assertFalse(result.primaryLimitReached)
            editor = result.editor
        }

        assertEquals(
            ProgressionTuning.MAX_PRIMARY_EXERCISES,
            editor.days[0].exercises.count { it.progressionRole == ExerciseRole.PRIMARY }
        )
    }

    @Test
    fun `the exercise beyond the limit is rejected and the editor is untouched`() {
        val editor = editorWithPrimariesAtLimit()

        val result = editor.withExerciseProgressionRole(
            dayIndex = 0,
            exerciseIndex = ProgressionTuning.MAX_PRIMARY_EXERCISES,
            role = ExerciseRole.PRIMARY
        )

        assertTrue(result.primaryLimitReached)
        assertEquals(editor, result.editor)
        assertEquals(
            ExerciseRole.ACCESSORY,
            result.editor.days[0].exercises[ProgressionTuning.MAX_PRIMARY_EXERCISES].progressionRole
        )
    }

    @Test
    fun `re-selecting PRIMARY on an exercise that already is PRIMARY does not trip the limit`() {
        val editor = editorWithPrimariesAtLimit()

        val result = editor.withExerciseProgressionRole(0, 0, ExerciseRole.PRIMARY)

        assertFalse(result.primaryLimitReached)
    }

    @Test
    fun `demoting a primary frees a slot`() {
        val atLimit = editorWithPrimariesAtLimit()

        val demoted = atLimit.withExerciseProgressionRole(0, 0, ExerciseRole.ACCESSORY)
        assertFalse(demoted.primaryLimitReached)

        val promoted = demoted.editor.withExerciseProgressionRole(
            dayIndex = 0,
            exerciseIndex = ProgressionTuning.MAX_PRIMARY_EXERCISES,
            role = ExerciseRole.PRIMARY
        )

        assertFalse(promoted.primaryLimitReached)
    }

    @Test
    fun `the limit counts primaries across every day of the routine`() {
        val editor = editorWithPrimariesSpreadAcrossDays()

        val result = editor.withExerciseProgressionRole(1, 1, ExerciseRole.PRIMARY)

        assertTrue(result.primaryLimitReached)
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
                        exercise(10L, "Press banca"),
                        exercise(11L, "Press militar"),
                        exercise(12L, "Fondos"),
                        exercise(13L, "Sentadilla")
                    )
                )
            )
        )
    }

    private fun editorWithPrimariesAtLimit(): RoutineEditorUiState {
        var editor = editorWithFourExercises()
        repeat(ProgressionTuning.MAX_PRIMARY_EXERCISES) { index ->
            editor = editor.withExerciseProgressionRole(0, index, ExerciseRole.PRIMARY).editor
        }
        return editor
    }

    private fun editorWithPrimariesSpreadAcrossDays(): RoutineEditorUiState {
        val base = RoutineEditorUiState(
            routineId = 1L,
            name = "Push/Pull",
            days = listOf(
                RoutineDayEditorUiState(
                    name = "Push",
                    exercises = listOf(exercise(10L, "Press banca"), exercise(11L, "Press militar"))
                ),
                RoutineDayEditorUiState(
                    name = "Pull",
                    exercises = listOf(exercise(20L, "Dominadas"), exercise(21L, "Remo"))
                )
            )
        )
        return base
            .withExerciseProgressionRole(0, 0, ExerciseRole.PRIMARY).editor
            .withExerciseProgressionRole(0, 1, ExerciseRole.PRIMARY).editor
            .withExerciseProgressionRole(1, 0, ExerciseRole.PRIMARY).editor
    }

    private fun exercise(id: Long, name: String): RoutineExerciseEditorUiState {
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
 * Regression tests for the bypass found on 2026-09-18 during the owner manual pass: duplicating an
 * exercise or a day cloned the PRIMARY role, so a routine could end up with 4+ primaries without
 * ever hitting the selector that enforces the limit.
 *
 * The selector was tested; the duplication paths were not. The limit lived in one place and the
 * copies went round it.
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
    fun `duplication cannot push the routine past the limit`() {
        var editor = editorWithFourExercisesForDuplication()
        repeat(ProgressionTuning.MAX_PRIMARY_EXERCISES) { index ->
            editor = editor.withExerciseProgressionRole(0, index, ExerciseRole.PRIMARY).editor
        }
        assertEquals(ProgressionTuning.MAX_PRIMARY_EXERCISES, editor.primaryExerciseCount())

        val afterExerciseCopy = editor.duplicateExercise(dayIndex = 0, exerciseIndex = 0)
        val afterDayCopy = afterExerciseCopy.duplicateDay(dayIndex = 0)

        assertEquals(ProgressionTuning.MAX_PRIMARY_EXERCISES, afterDayCopy.primaryExerciseCount())
        assertTrue(afterDayCopy.primaryExerciseCount() <= ProgressionTuning.MAX_PRIMARY_EXERCISES)
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
