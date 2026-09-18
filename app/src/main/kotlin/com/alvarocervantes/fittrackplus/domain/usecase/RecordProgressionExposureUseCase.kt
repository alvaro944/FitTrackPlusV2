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

        // Swapping to an alternative mid-session changes the performed variant, and a machine with
        // different plates is a different resistance. Writing this prescription into that variant's
        // history would attribute a load the user never actually trained on it.
        if (prescription.nextProfile.variantKey != input.variantKey) return

        val classification = classifyExposure(
            probeReps = input.probeReps,
            probeRir = input.probeRir,
            prescribedRepMin = prescription.prescribedRepMin,
            prescribedRepMax = prescription.prescribedRepMax,
            prescribedTargetRir = prescription.prescribedTargetRir,
            completedSetCount = input.completedSetCount,
            prescribedSets = prescription.prescribedSets
        )
        // The e1RM measures the owner, so it comes from what they actually lifted. Estimating from
        // the prescribed load made the engine measure itself: 15 kg lifted read as 10 kg.
        val liftedLoadKg = input.probeLoadKg ?: prescription.prescribedLoadKg
        val estimate = calculateStrengthEstimate(
            loadKg = liftedLoadKg,
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
            probeLoadKg = liftedLoadKg,
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
            decisionReason = prescription.reason.name
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
        /** What was actually on the bar for the probe set. Null only if the set had no weight logged. */
        val probeLoadKg: Double?,
        val probeReps: Int,
        val probeRir: Int?,
        val completedSetCount: Int,
        val userFlaggedBadDay: Boolean
    )
}
