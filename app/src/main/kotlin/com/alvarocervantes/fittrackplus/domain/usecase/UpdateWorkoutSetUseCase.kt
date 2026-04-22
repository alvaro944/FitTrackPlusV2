package com.alvarocervantes.fittrackplus.domain.usecase

import com.alvarocervantes.fittrackplus.data.repository.WorkoutRepository
import javax.inject.Inject

class UpdateWorkoutSetUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    suspend operator fun invoke(setId: Long, weightText: String, repsText: String) {
        workoutRepository.updateSet(
            setId = setId,
            weightKg = weightText.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0,
            reps = repsText.toIntOrNull()?.coerceAtLeast(0) ?: 0
        )
    }
}
