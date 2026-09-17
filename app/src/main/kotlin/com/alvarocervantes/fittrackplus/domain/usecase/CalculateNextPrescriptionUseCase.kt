package com.alvarocervantes.fittrackplus.domain.usecase

import com.alvarocervantes.fittrackplus.data.repository.ProgressionRepository
import com.alvarocervantes.fittrackplus.domain.model.progression.ProgressionPrescription
import com.alvarocervantes.fittrackplus.domain.model.progression.calculateNextPrescription
import javax.inject.Inject

class CalculateNextPrescriptionUseCase @Inject constructor(
    private val progressionRepository: ProgressionRepository
) {
    suspend operator fun invoke(variantKey: String): ProgressionPrescription? {
        val profile = progressionRepository.getProfile(variantKey) ?: return null
        return calculateNextPrescription(profile, progressionRepository.getExposures(variantKey))
    }
}
