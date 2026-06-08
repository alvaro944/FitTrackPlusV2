package com.alvarocervantes.fittrackplus.domain.usecase

import com.alvarocervantes.fittrackplus.data.local.relation.WorkoutSessionWithExercises
import com.alvarocervantes.fittrackplus.data.repository.WorkoutRepository
import com.alvarocervantes.fittrackplus.domain.model.WorkoutHistorySummary
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveWorkoutHistoryUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    operator fun invoke(): Flow<List<WorkoutHistorySummary>> {
        return workoutRepository.observeFinishedSessionsWithExercises().map { sessions ->
            sessions.mapNotNull { session -> session.toHistorySummary() }
        }
    }
}

private fun WorkoutSessionWithExercises.toHistorySummary(): WorkoutHistorySummary? {
    val finishedAt = session.finishedAt ?: return null
    val sets = exercises.flatMap { exercise -> exercise.sets }
    return WorkoutHistorySummary(
        sessionId = session.id,
        routineName = session.routineNameSnapshot,
        dayName = session.dayNameSnapshot,
        startedAt = session.startedAt,
        finishedAt = finishedAt,
        weekNumber = session.weekNumber,
        totalVolumeKg = sets.sumOf { set -> set.weightKg * set.reps },
        durationMillis = (finishedAt - session.startedAt).coerceAtLeast(0),
        setCount = sets.size
    )
}
