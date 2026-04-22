package com.alvarocervantes.fittrackplus.domain.usecase

import com.alvarocervantes.fittrackplus.domain.model.RoutineDaySnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GetNextRoutineDayUseCaseTest {
    private val useCase = GetNextRoutineDayUseCase()

    @Test
    fun cyclesDaysAndIncrementsWeek() {
        val days = listOf(
            day(id = 1, name = "Push", position = 0),
            day(id = 2, name = "Pull", position = 1),
            day(id = 3, name = "Legs", position = 2)
        )

        assertEquals("Push", useCase(days, completedSessionsForRoutine = 0)?.day?.name)
        assertEquals(1, useCase(days, completedSessionsForRoutine = 0)?.weekNumber)
        assertEquals("Pull", useCase(days, completedSessionsForRoutine = 1)?.day?.name)
        assertEquals("Legs", useCase(days, completedSessionsForRoutine = 2)?.day?.name)
        assertEquals("Push", useCase(days, completedSessionsForRoutine = 3)?.day?.name)
        assertEquals(2, useCase(days, completedSessionsForRoutine = 3)?.weekNumber)
    }

    @Test
    fun returnsNullWhenRoutineHasNoDays() {
        assertNull(useCase(days = emptyList(), completedSessionsForRoutine = 4))
    }

    private fun day(id: Long, name: String, position: Int): RoutineDaySnapshot {
        return RoutineDaySnapshot(
            id = id,
            name = name,
            position = position,
            exercises = emptyList()
        )
    }
}
