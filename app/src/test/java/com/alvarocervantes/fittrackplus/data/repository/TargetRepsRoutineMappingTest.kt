package com.alvarocervantes.fittrackplus.data.repository

import com.alvarocervantes.fittrackplus.domain.model.RoutineExerciseDraft
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TargetRepsRoutineMappingTest {

    @Test
    fun parsesStructuredRangeWithoutNormalizingSourceText() {
        val entity = exerciseDraft(targetRepsText = " 8-12 ")
            .toRoutineExerciseEntity(
                routineDayId = 3,
                position = 1,
                variantKey = "bench"
            )

        assertEquals(" 8-12 ", entity.targetRepsText)
        assertEquals(8, entity.targetRepsMin)
        assertEquals(12, entity.targetRepsMax)
    }

    @Test
    fun preservesUnstructuredSourceTextWithNullRange() {
        val entity = exerciseDraft(targetRepsText = "AMRAP")
            .toRoutineExerciseEntity(
                routineDayId = 3,
                position = 1,
                variantKey = "bench"
            )

        assertEquals("AMRAP", entity.targetRepsText)
        assertNull(entity.targetRepsMin)
        assertNull(entity.targetRepsMax)
    }

    private fun exerciseDraft(targetRepsText: String): RoutineExerciseDraft {
        return RoutineExerciseDraft(
            name = "Bench press",
            targetSets = 3,
            targetRepsText = targetRepsText
        )
    }
}
