package com.alvarocervantes.fittrackplus.domain.usecase

import com.alvarocervantes.fittrackplus.data.repository.WorkoutRepository
import com.alvarocervantes.fittrackplus.domain.model.PrType
import javax.inject.Inject

/**
 * Detecta si (exerciseName, weightKg, reps) supera estrictamente el mejor
 * registro histórico del ejercicio en sesiones **finalizadas**.
 * Devuelve el tipo de PR que se bate, o null si no hay nuevo record.
 */
class DetectPersonalRecordUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    suspend operator fun invoke(
        exerciseName: String,
        weightKg: Double,
        reps: Int
    ): PrType? {
        if (weightKg <= 0.0 || reps <= 0) return null

        val setVolume = weightKg * reps

        val prevMaxWeight = workoutRepository.getMaxWeightForExercise(exerciseName) ?: 0.0
        val prevMaxVolume = workoutRepository.getMaxSetVolumeForExercise(exerciseName) ?: 0.0

        return when {
            weightKg > prevMaxWeight -> PrType.MaxWeight
            setVolume > prevMaxVolume -> PrType.MaxVolume
            else -> null
        }
    }
}
