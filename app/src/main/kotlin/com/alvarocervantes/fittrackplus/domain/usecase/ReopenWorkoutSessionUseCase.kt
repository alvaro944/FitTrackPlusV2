package com.alvarocervantes.fittrackplus.domain.usecase

import com.alvarocervantes.fittrackplus.data.repository.WorkoutRepository
import javax.inject.Inject

/**
 * Reopens a finished-but-incomplete session so it can be continued. Because the app only allows a
 * single active (open) session at a time, reopening is blocked while another session is in progress.
 */
class ReopenWorkoutSessionUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    suspend operator fun invoke(sessionId: Long): ReopenWorkoutSessionResult {
        if (workoutRepository.getActiveSessionWithExercises() != null) {
            return ReopenWorkoutSessionResult.BlockedByActiveSession
        }
        workoutRepository.reopenSession(sessionId)
        return ReopenWorkoutSessionResult.Reopened
    }
}

enum class ReopenWorkoutSessionResult {
    Reopened,
    BlockedByActiveSession
}
