package com.alvarocervantes.fittrackplus.domain.usecase

import com.alvarocervantes.fittrackplus.data.repository.WorkoutRepository
import javax.inject.Inject

class FinishWorkoutSessionUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    suspend operator fun invoke(sessionId: Long, notes: String? = null) {
        workoutRepository.finishSession(sessionId = sessionId, notes = notes)
    }
}
