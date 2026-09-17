package com.alvarocervantes.fittrackplus.domain.usecase

import com.alvarocervantes.fittrackplus.data.repository.ProgressionRepository
import com.alvarocervantes.fittrackplus.domain.model.progression.ProgressionExposure
import com.alvarocervantes.fittrackplus.domain.model.progression.ProgressionPrescription
import com.alvarocervantes.fittrackplus.domain.model.progression.calculateStrengthEstimate
import com.alvarocervantes.fittrackplus.domain.model.progression.classifyExposure
import com.alvarocervantes.fittrackplus.domain.model.progression.intraSessionAdjustmentSteps
import com.alvarocervantes.fittrackplus.domain.model.progression.updateE1rmEwmas
import javax.inject.Inject

/**
 * Writes the exposure a finished session produced, and advances the profile from it.
 *
 * Snapshot invariant (R22): an exposure is written once and is never recalculated. Open sessions
 * produce nothing — only a finished one does. An exposure whose workout exercise is later deleted
 * keeps counting for the trend: it happened.
 */
class RecordProgressionExposureUseCase @Inject constructor(
    private val progressionRepository: ProgressionRepository
) {

    suspend operator fun invoke(input: Input) {
        val profile = progressionRepository.getProfile(input.variantKey) ?: return
        val prescription = input.prescription

        val classification = classifyExposure(
            probeReps = input.probeReps,
            probeRir = input.probeRir,
            prescribedRepMin = prescription.prescribedRepMin,
            prescribedRepMax = prescription.prescribedRepMax,
            prescribedTargetRir = prescription.prescribedTargetRir,
            completedSetCount = input.completedSetCount,
            prescribedSets = prescription.prescribedSets
        )
        val estimate = calculateStrengthEstimate(
            loadKg = prescription.prescribedLoadKg,
            reps = input.probeReps,
            rir = input.probeRir
        )
        val exposure = ProgressionExposure(
            variantKey = input.variantKey,
            workoutExerciseId = input.workoutExerciseId,
            exposureIndex = progressionRepository.getExposures(input.variantKey).size + 1,
            performedAt = input.performedAt,
            type = prescription.type,
            prescribedLoadKg = prescription.prescribedLoadKg,
            prescribedRepMin = prescription.prescribedRepMin,
            prescribedRepMax = prescription.prescribedRepMax,
            prescribedTargetRir = prescription.prescribedTargetRir,
            prescribedSets = prescription.prescribedSets,
            probeReps = input.probeReps,
            probeRir = input.probeRir,
            probeSurplus = classification.surplus,
            outcomeClass = classification.outcomeClass,
            setCompletionRatio = classification.setCompletionRatio,
            exposureE1rm = estimate?.e1rmKg,
            e1rmConfidence = estimate?.confidence,
            excludeFromTrend = classification.excludeFromTrend || input.userFlaggedBadDay,
            userFlaggedBadDay = input.userFlaggedBadDay,
            // Recorded for the history, never read back by the next decision: that would double count.
            intraSessionAdjustmentSteps = intraSessionAdjustmentSteps(
                probeSurplus = classification.surplus,
                state = profile.state,
                exposureType = prescription.type
            ),
            decisionReason = prescription.decisionReason
        )

        progressionRepository.insertExposure(exposure)
        progressionRepository.upsertProfile(
            updateE1rmEwmas(prescription.nextProfile, exposure)
        )
    }

    data class Input(
        val variantKey: String,
        val workoutExerciseId: Long?,
        val performedAt: Long,
        val prescription: ProgressionPrescription,
        val probeReps: Int,
        val probeRir: Int?,
        val completedSetCount: Int,
        val userFlaggedBadDay: Boolean
    )
}
