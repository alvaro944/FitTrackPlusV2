package com.alvarocervantes.fittrackplus.domain.usecase

import com.alvarocervantes.fittrackplus.data.repository.RoutineRepository
import com.alvarocervantes.fittrackplus.data.repository.WorkoutRepository
import com.alvarocervantes.fittrackplus.domain.model.StartedWorkoutSession
import javax.inject.Inject

class StartWorkoutSessionUseCase @Inject constructor(
    private val routineRepository: RoutineRepository,
    private val workoutRepository: WorkoutRepository,
    private val getNextRoutineDay: GetNextRoutineDayUseCase
) {
    suspend operator fun invoke(routineId: Long): StartedWorkoutSession? {
        workoutRepository.getActiveSessionWithExercises()?.let { activeSession ->
            return StartedWorkoutSession(
                sessionId = activeSession.session.id,
                routineId = activeSession.session.routineId ?: routineId,
                routineNameSnapshot = activeSession.session.routineNameSnapshot,
                dayNameSnapshot = activeSession.session.dayNameSnapshot,
                weekNumber = activeSession.session.weekNumber
            )
        }

        val routine = routineRepository.getRoutineSnapshot(routineId) ?: return null
        val completedSessions = workoutRepository.countFinishedSessionsForRoutine(routineId)
        val nextDay = getNextRoutineDay(
            days = routine.days,
            completedSessionsForRoutine = completedSessions
        ) ?: return null

        val sessionId = workoutRepository.createSessionFromRoutineDay(
            routine = routine,
            day = nextDay.day,
            weekNumber = nextDay.weekNumber
        )

        return StartedWorkoutSession(
            sessionId = sessionId,
            routineId = routine.id,
            routineNameSnapshot = routine.name,
            dayNameSnapshot = nextDay.day.name,
            weekNumber = nextDay.weekNumber
        )
    }
}
