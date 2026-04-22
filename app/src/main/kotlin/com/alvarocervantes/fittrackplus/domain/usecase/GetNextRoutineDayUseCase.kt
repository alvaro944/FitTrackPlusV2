package com.alvarocervantes.fittrackplus.domain.usecase

import com.alvarocervantes.fittrackplus.domain.model.RoutineDaySnapshot
import javax.inject.Inject

class GetNextRoutineDayUseCase @Inject constructor() {
    operator fun invoke(
        days: List<RoutineDaySnapshot>,
        completedSessionsForRoutine: Int
    ): NextRoutineDay? {
        if (days.isEmpty()) return null

        val orderedDays = days.sortedBy { it.position }
        val safeCompletedSessions = completedSessionsForRoutine.coerceAtLeast(0)
        val nextDayIndex = safeCompletedSessions % orderedDays.size
        val weekNumber = safeCompletedSessions / orderedDays.size + 1

        return NextRoutineDay(
            day = orderedDays[nextDayIndex],
            weekNumber = weekNumber
        )
    }
}

data class NextRoutineDay(
    val day: RoutineDaySnapshot,
    val weekNumber: Int
)
