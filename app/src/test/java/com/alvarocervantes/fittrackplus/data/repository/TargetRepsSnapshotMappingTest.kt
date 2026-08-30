package com.alvarocervantes.fittrackplus.data.repository

import com.alvarocervantes.fittrackplus.domain.model.RoutineExerciseAlternativeSnapshot
import com.alvarocervantes.fittrackplus.domain.model.RoutineExerciseSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TargetRepsSnapshotMappingTest {

    @Test
    fun createsStructuredRangeSnapshotFromRoutineExercise() {
        val entity = routineExercise(
            targetRepsText = "8-12",
            targetRepsMin = 8,
            targetRepsMax = 12
        )
            .toWorkoutExerciseEntity(sessionId = 42)

        assertEquals("8-12", entity.targetRepsSnapshot)
        assertEquals(8, entity.targetRepsMinSnapshot)
        assertEquals(12, entity.targetRepsMaxSnapshot)
    }

    @Test
    fun snapshotsBaseExerciseNotes() {
        val entity = routineExercise(
            targetRepsText = "8-12",
            targetRepsMin = 8,
            targetRepsMax = 12
        ).copy(notes = "Pause at the bottom")
            .toWorkoutExerciseEntity(sessionId = 42)

        assertEquals("Pause at the bottom", entity.notes)
    }

    @Test
    fun preservesUnstructuredSnapshotText() {
        val entity = routineExercise(
            targetRepsText = "AMRAP",
            targetRepsMin = null,
            targetRepsMax = null
        )
            .toWorkoutExerciseEntity(sessionId = 42)

        assertEquals("AMRAP", entity.targetRepsSnapshot)
        assertNull(entity.targetRepsMinSnapshot)
        assertNull(entity.targetRepsMaxSnapshot)
    }

    @Test
    fun snapshotsStructuredRangeFromSelectedAlternative() {
        val exercise = routineExercise(
            targetRepsText = "8-12",
            targetRepsMin = 8,
            targetRepsMax = 12
        ).copy(
            defaultVariantKey = "incline-bench",
            alternatives = listOf(
                RoutineExerciseAlternativeSnapshot(
                    id = 9,
                    variantKey = "incline-bench",
                    name = "Incline bench press",
                    targetSets = 4,
                    targetRepsText = "6-8",
                    position = 0,
                    notes = "Use a shallow incline",
                    targetRepsMin = 6,
                    targetRepsMax = 8
                )
            )
        )

        val entity = exercise.toWorkoutExerciseEntity(sessionId = 42)

        assertEquals("incline-bench", entity.performedVariantKey)
        assertEquals("6-8", entity.targetRepsSnapshot)
        assertEquals(6, entity.targetRepsMinSnapshot)
        assertEquals(8, entity.targetRepsMaxSnapshot)
        assertEquals("Use a shallow incline", entity.notes)
    }

    private fun routineExercise(
        targetRepsText: String,
        targetRepsMin: Int?,
        targetRepsMax: Int?
    ): RoutineExerciseSnapshot {
        return RoutineExerciseSnapshot(
            id = 7,
            variantKey = "bench",
            defaultVariantKey = "bench",
            name = "Bench press",
            targetSets = 3,
            targetRepsText = targetRepsText,
            position = 2,
            notes = null,
            alternatives = emptyList(),
            targetRepsMin = targetRepsMin,
            targetRepsMax = targetRepsMax
        )
    }
}
