package com.alvarocervantes.fittrackplus.domain.usecase

import com.alvarocervantes.fittrackplus.data.local.entity.WorkoutSessionEntity
import com.alvarocervantes.fittrackplus.data.repository.WorkoutRepository
import com.alvarocervantes.fittrackplus.domain.model.WorkoutHistorySummary
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveWorkoutHistoryUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    operator fun invoke(): Flow<List<WorkoutHistorySummary>> {
        return workoutRepository.observeFinishedSessions().map { sessions ->
            sessions.mapNotNull { session -> session.toHistorySummary() }
        }
    }
}

private fun WorkoutSessionEntity.toHistorySummary(): WorkoutHistorySummary? {
    val finishedAt = finishedAt ?: return null
    return WorkoutHistorySummary(
        sessionId = id,
        routineName = routineNameSnapshot,
        dayName = dayNameSnapshot,
        startedAt = startedAt,
        finishedAt = finishedAt,
        weekNumber = weekNumber
    )
}
