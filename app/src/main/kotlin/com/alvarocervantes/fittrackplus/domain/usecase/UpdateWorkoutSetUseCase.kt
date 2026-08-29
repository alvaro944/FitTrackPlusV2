package com.alvarocervantes.fittrackplus.domain.usecase

import com.alvarocervantes.fittrackplus.data.repository.WorkoutRepository
import com.alvarocervantes.fittrackplus.domain.model.WeightUnit
import com.alvarocervantes.fittrackplus.domain.model.isWorkoutSetCompleted
import javax.inject.Inject

class UpdateWorkoutSetUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    /**
     * @param markCompletionFromData when true, the set's completion flag is derived from the data
     * (positive reps). Used from History, where filling in a set counts as completing it.
     * The live workout keeps completion manual (green button), so it leaves this false.
     */
    suspend operator fun invoke(
        setId: Long,
        weightText: String,
        repsText: String,
        weightUnit: WeightUnit = WeightUnit.Kilograms,
        markCompletionFromData: Boolean = false
    ) {
        val weightKg = parseWorkoutWeightText(weightText)
            ?.let(weightUnit::toKilograms)
            ?.coerceAtLeast(0.0)
            ?: 0.0
        val reps = repsText.toIntOrNull()?.coerceAtLeast(0) ?: 0
        workoutRepository.updateSet(setId = setId, weightKg = weightKg, reps = reps)
        if (markCompletionFromData) {
            workoutRepository.updateSetCompletion(setId = setId, isCompleted = isWorkoutSetCompleted(reps))
        }
    }
}

private fun parseWorkoutWeightText(weightText: String): Double? {
    return weightText.replace(',', '.').toDoubleOrNull()
}
