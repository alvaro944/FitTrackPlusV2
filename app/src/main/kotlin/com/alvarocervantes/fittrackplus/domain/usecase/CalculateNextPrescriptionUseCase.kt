package com.alvarocervantes.fittrackplus.domain.usecase

import com.alvarocervantes.fittrackplus.data.repository.ProgressionRepository
import com.alvarocervantes.fittrackplus.data.repository.WorkoutRepository
import com.alvarocervantes.fittrackplus.domain.model.progression.ExerciseProgressionProfile
import com.alvarocervantes.fittrackplus.domain.model.progression.ProgressionPrescription
import com.alvarocervantes.fittrackplus.domain.model.progression.ProgressionState
import com.alvarocervantes.fittrackplus.domain.model.progression.calculateNextPrescription
import javax.inject.Inject

class CalculateNextPrescriptionUseCase @Inject constructor(
    private val progressionRepository: ProgressionRepository,
    private val workoutRepository: WorkoutRepository
) {

    suspend operator fun invoke(variantKey: String): ProgressionPrescription? {
        val profile = progressionRepository.getProfile(variantKey) ?: return null
        val seeded = seedCalibrationFromHistory(profile, variantKey) ?: return null
        return calculateNextPrescription(seeded, progressionRepository.getExposures(variantKey))
    }

    /**
     * Gives a freshly promoted PRIMARY exercise a starting load taken from what the user has already
     * lifted, so calibration does not need a form before it can begin.
     *
     * Returns null when there is no history to seed from: the exercise simply shows no prescription
     * until the owner sets one. That is a normal state, not a failure.
     */
    private suspend fun seedCalibrationFromHistory(
        profile: ExerciseProgressionProfile,
        variantKey: String
    ): ExerciseProgressionProfile? {
        val needsSeed = profile.state == ProgressionState.CALIBRATING &&
            profile.loadVolumeKg == null &&
            profile.loadStrengthKg == null
        if (!needsSeed) return profile

        val heaviestLogged = workoutRepository.getMaxWeightForExercise(variantKey)
            ?.takeIf { it > 0.0 }
            ?: return null

        val seeded = profile.copy(loadVolumeKg = heaviestLogged, loadStrengthKg = heaviestLogged)
        progressionRepository.upsertProfile(seeded)
        return seeded
    }
}
