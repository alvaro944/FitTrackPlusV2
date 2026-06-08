package com.alvarocervantes.fittrackplus.feature.routines

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutineEditorOperationsTest {

    @Test
    fun duplicateDayCopiesDayAndExercisesAndMarksDirty() {
        val editor = sampleEditor()

        val result = editor.duplicateDay(dayIndex = 0)

        assertTrue(result.isDirty)
        assertEquals(listOf("Push", "Push copia", "Pull"), result.days.map { it.name })
        assertEquals(editor.days.first().exercises, result.days[1].exercises)
    }

    @Test
    fun duplicateExerciseCopiesExerciseAndMarksDirty() {
        val editor = sampleEditor()

        val result = editor.duplicateExercise(dayIndex = 0, exerciseIndex = 0)

        assertTrue(result.isDirty)
        assertEquals(listOf("Bench Press", "Bench Press copia", "Overhead Press"), result.days[0].exercises.map { it.name })
        assertEquals("3", result.days[0].exercises[1].targetSets)
        assertEquals("8-12", result.days[0].exercises[1].targetRepsText)
        assertEquals("Use spotter", result.days[0].exercises[1].notes)
    }

    @Test
    fun moveDayKeepsBoundsAndMovesIntermediateItems() {
        val editor = sampleEditor()

        assertEquals(editor, editor.moveDay(dayIndex = 0, direction = MoveDirection.Up))
        assertEquals(editor, editor.moveDay(dayIndex = 1, direction = MoveDirection.Down))

        val result = editor.moveDay(dayIndex = 1, direction = MoveDirection.Up)

        assertTrue(result.isDirty)
        assertEquals(listOf("Pull", "Push"), result.days.map { it.name })
    }

    @Test
    fun moveExerciseKeepsBoundsAndMovesIntermediateItems() {
        val editor = sampleEditor()

        assertEquals(editor, editor.moveExercise(dayIndex = 0, exerciseIndex = 0, direction = MoveDirection.Up))
        assertEquals(editor, editor.moveExercise(dayIndex = 0, exerciseIndex = 1, direction = MoveDirection.Down))

        val result = editor.moveExercise(dayIndex = 0, exerciseIndex = 1, direction = MoveDirection.Up)

        assertTrue(result.isDirty)
        assertEquals(listOf("Overhead Press", "Bench Press"), result.days[0].exercises.map { it.name })
    }

    @Test
    fun invalidIndexesReturnSameEditor() {
        val editor = sampleEditor()

        assertFalse(editor.isDirty)
        assertEquals(editor, editor.duplicateDay(dayIndex = 99))
        assertEquals(editor, editor.duplicateExercise(dayIndex = 99, exerciseIndex = 0))
        assertEquals(editor, editor.moveExercise(dayIndex = 0, exerciseIndex = 99, direction = MoveDirection.Up))
    }

    private fun sampleEditor(): RoutineEditorUiState {
        return RoutineEditorUiState(
            name = "Sample",
            days = listOf(
                RoutineDayEditorUiState(
                    name = "Push",
                    exercises = listOf(
                        RoutineExerciseEditorUiState(
                            name = "Bench Press",
                            targetSets = "3",
                            targetRepsText = "8-12",
                            notes = "Use spotter"
                        ),
                        RoutineExerciseEditorUiState(
                            name = "Overhead Press",
                            targetSets = "3",
                            targetRepsText = "6-8"
                        )
                    )
                ),
                RoutineDayEditorUiState(
                    name = "Pull",
                    exercises = listOf(
                        RoutineExerciseEditorUiState(
                            name = "Pull Up",
                            targetSets = "3",
                            targetRepsText = "AMRAP"
                        )
                    )
                )
            )
        )
    }
}
