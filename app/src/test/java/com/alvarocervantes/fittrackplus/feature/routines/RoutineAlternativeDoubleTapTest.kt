package com.alvarocervantes.fittrackplus.feature.routines

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Regression tests for the "double tap on Crear alternativa creates two duplicate variants"
 * bug reported on 2026-07-06 dogfooding.
 *
 * The bug: in [RoutinesViewModel.addExerciseAlternative] every tap appends a new
 * alternative seeded with the parent exercise content. Two rapid taps before the
 * screen recomposes (the second tap hits the same button row state the first tap saw)
 * append TWO alternatives instead of one. The user edits only the visible one (the
 * first index captured by `editingAlternativeIndex = exercise.alternatives.size`
 * BEFORE the dispatch), names it, and the second untouched seed is silently saved
 * when the routine persists.
 *
 * These tests cover the pure editor-state function used by [RoutinesViewModel.addExerciseAlternative].
 */
class RoutineAlternativeDoubleTapTest {

    @Test
    fun addExerciseAlternative_calledOnce_addsOneSeedAlternative() {
        val editor = sampleEditorWithNoAlternatives()

        val result = editor.addSeedAlternative(dayIndex = 0, exerciseIndex = 0)

        assertEquals(1, result.days[0].exercises[0].alternatives.size)
        assertEquals("Bench Press", result.days[0].exercises[0].alternatives[0].name)
    }

    @Test
    fun addExerciseAlternative_tappedTwiceInQuickSuccession_mustNotAppendSecondDuplicate() {
        val editor = sampleEditorWithNoAlternatives()

        val afterFirst = editor.addSeedAlternative(dayIndex = 0, exerciseIndex = 0)
        val afterSecond = afterFirst.addSeedAlternative(dayIndex = 0, exerciseIndex = 0)

        // Desired behavior: only ONE seed alternative should exist after a double tap.
        assertEquals(
            "Double tap on Crear alternativa must append only one seed, not two duplicates.",
            1,
            afterSecond.days[0].exercises[0].alternatives.size
        )
    }

    @Test
    fun addExerciseAlternative_tappedTwice_keepsSingleSeedAndMarksDirty() {
        val editor = sampleEditorWithNoAlternatives()

        val afterFirst = editor.addSeedAlternative(dayIndex = 0, exerciseIndex = 0)
        val afterSecond = afterFirst.addSeedAlternative(dayIndex = 0, exerciseIndex = 0)

        // Behavior the fix must preserve: dirty flag stays set and the seed is a clone
        // of the parent exercise (so the editor view shows the variant form pre-filled).
        assertEquals(true, afterSecond.isDirty)
        // When double-tap is idempotent, the surviving alternative must still be the seed.
        assertEquals(1, afterSecond.days[0].exercises[0].alternatives.size)
        assertEquals("Bench Press", afterSecond.days[0].exercises[0].alternatives[0].name)
        assertEquals("3", afterSecond.days[0].exercises[0].alternatives[0].targetSets)
        assertEquals("8-12", afterSecond.days[0].exercises[0].alternatives[0].targetRepsText)
    }

    /**
     * Mirrors the editor update around [RoutinesViewModel.addExerciseAlternative].
     */
    private fun RoutineEditorUiState.addSeedAlternative(
        dayIndex: Int,
        exerciseIndex: Int
    ): RoutineEditorUiState {
        return updateExerciseInline(dayIndex, exerciseIndex) { exercise -> exercise.withSeedAlternative() }
            .copy(isDirty = true)
    }

    private fun RoutineEditorUiState.updateExerciseInline(
        dayIndex: Int,
        exerciseIndex: Int,
        transform: (RoutineExerciseEditorUiState) -> RoutineExerciseEditorUiState
    ): RoutineEditorUiState {
        return copy(
            days = days.replaceAtInline(dayIndex) { day ->
                day.copy(exercises = day.exercises.replaceAtInline(exerciseIndex, transform))
            }
        )
    }

    private fun <T> List<T>.replaceAtInline(index: Int, transform: (T) -> T): List<T> {
        if (index !in indices) return this
        return toMutableList().also { it[index] = transform(it[index]) }
    }

    private fun sampleEditorWithNoAlternatives(): RoutineEditorUiState {
        return RoutineEditorUiState(
            routineId = 1L,
            name = "Push/Pull/Legs",
            days = listOf(
                RoutineDayEditorUiState(
                    name = "Push",
                    exercises = listOf(
                        RoutineExerciseEditorUiState(
                            routineExerciseId = 10L,
                            variantKey = "exercise-1",
                            name = "Bench Press",
                            targetSets = "3",
                            targetRepsText = "8-12",
                            notes = "Use spotter"
                        )
                    )
                )
            )
        )
    }
}
