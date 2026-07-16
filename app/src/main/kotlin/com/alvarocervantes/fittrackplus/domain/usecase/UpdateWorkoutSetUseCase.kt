package com.alvarocervantes.fittrackplus.domain.usecase

import com.alvarocervantes.fittrackplus.data.repository.WorkoutRepository
import javax.inject.Inject

class UpdateWorkoutSetUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    /**
     * @param markCompletionFromData when true, the set's completion flag is derived from the data
     * (weight > 0 and reps > 0). Used from History, where filling in a set counts as completing it.
     * The live workout keeps completion manual (green button), so it leaves this false.
     */
    suspend operator fun invoke(
        setId: Long,
        weightText: String,
        repsText: String,
        markCompletionFromData: Boolean = false
    ) {
        val weightKg = parseWorkoutWeightText(weightText)?.coerceAtLeast(0.0) ?: 0.0
        val reps = repsText.toIntOrNull()?.coerceAtLeast(0) ?: 0
        workoutRepository.updateSet(setId = setId, weightKg = weightKg, reps = reps)
        if (markCompletionFromData) {
            workoutRepository.updateSetCompletion(setId = setId, isCompleted = weightKg > 0.0 && reps > 0)
        }
    }
}

private fun parseWorkoutWeightText(weightText: String): Double? {
    return weightText.replace(',', '.').toDoubleOrNull()
}
