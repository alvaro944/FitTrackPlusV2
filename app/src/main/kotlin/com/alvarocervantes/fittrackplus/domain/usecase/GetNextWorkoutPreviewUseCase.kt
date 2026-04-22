package com.alvarocervantes.fittrackplus.domain.usecase

import com.alvarocervantes.fittrackplus.data.repository.RoutineRepository
import com.alvarocervantes.fittrackplus.data.repository.WorkoutRepository
import com.alvarocervantes.fittrackplus.domain.model.WorkoutPreview
import javax.inject.Inject

class GetNextWorkoutPreviewUseCase @Inject constructor(
    private val routineRepository: RoutineRepository,
    private val workoutRepository: WorkoutRepository,
    private val getNextRoutineDay: GetNextRoutineDayUseCase
) {
    suspend operator fun invoke(routineId: Long): WorkoutPreview? {
        val routine = routineRepository.getRoutineSnapshot(routineId) ?: return null
        val completedSessions = workoutRepository.countFinishedSessionsForRoutine(routineId)
        val nextDay = getNextRoutineDay(
            days = routine.days,
            completedSessionsForRoutine = completedSessions
        ) ?: return null

        return WorkoutPreview(
            routineId = routine.id,
            routineName = routine.name,
            dayName = nextDay.day.name,
            weekNumber = nextDay.weekNumber,
            exerciseCount = nextDay.day.exercises.size
        )
    }
}
