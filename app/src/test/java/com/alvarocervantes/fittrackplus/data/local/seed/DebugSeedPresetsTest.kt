package com.alvarocervantes.fittrackplus.data.local.seed

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DebugSeedPresetsTest {

    @Test
    fun alvaroBaseRoutineContainsExpectedFourDaySplit() {
        val routine = buildAlvaroBaseRoutineSeed()

        assertEquals("Rutina Álvaro", routine.name)
        assertEquals(
            listOf(
                "Entreno 1 — Empujes",
                "Entreno 2 — Pierna Cuádriceps",
                "Entreno 3 — Espalda",
                "Entreno 4 — Pierna Femoral"
            ),
            routine.days.map { it.name }
        )
        assertEquals(listOf(7, 9, 8, 9), routine.days.map { it.exercises.size })
        assertTrue(
            routine.days.flatMap { it.exercises }.all { exercise ->
                exercise.targetReps == "8-12"
            }
        )
    }

    @Test
    fun alvaroBaseRoutineKeepsProvidedExerciseNamesAndSeries() {
        val routine = buildAlvaroBaseRoutineSeed()

        val pushDay = routine.days.first()
        assertEquals("Press plano", pushDay.exercises[0].name)
        assertEquals(2, pushDay.exercises[0].targetSets)
        assertEquals("Aperturas", pushDay.exercises.last().name)
        assertEquals(1, pushDay.exercises.last().targetSets)

        val femoralDay = routine.days.last()
        assertEquals("Peso muerto rumano", femoralDay.exercises.first().name)
        assertEquals(2, femoralDay.exercises.first().targetSets)
        assertEquals("Tríceps en polea", femoralDay.exercises.last().name)
        assertEquals(2, femoralDay.exercises.last().targetSets)
    }
}
