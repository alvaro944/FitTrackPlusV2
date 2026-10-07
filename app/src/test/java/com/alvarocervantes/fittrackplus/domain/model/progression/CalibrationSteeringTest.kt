package com.alvarocervantes.fittrackplus.domain.model.progression

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Regression tests for the calibration bug found by the owner on 2026-09-18, reproduced with the
 * exact data pulled from their emulator.
 *
 * Every probe reported surplus (the exercise felt easy) and calibration still lowered the load
 * 12.5 -> 10 -> 7.5 -> 5. Two coupled causes:
 *  1. With no e1RM, a WORKING load was fed to estimateLoad, which expects an e1RM and divides it.
 *  2. A light load with reserve left gives reps + rir above the confidence cutoff, so no e1RM was
 *     ever produced and calibration was stuck on that broken fallback forever.
 */
class CalibrationSteeringTest {

    private val increment = 2.5

    @Test
    fun `an easy calibration raises the load instead of lowering it`() {
        // Exposure 1 from the owner's data: 12.5 kg x 10 @RIR1, surplus +2, no e1RM.
        val prescription = requireNotNull(
            calculateNextPrescription(
                seededProfile(),
                listOf(calibrationExposure(1, ExposureType.VOLUME, 12.5, OutcomeClass.STRONG))
            )
        )

        assertTrue(
            "An easy probe must never lower the load; got ${prescription.prescribedLoadKg}",
            prescription.prescribedLoadKg >= 12.5
        )
        assertEquals(15.0, prescription.prescribedLoadKg, 0.0)
    }

    @Test
    fun `the owner's exact sequence climbs instead of decaying`() {
        // The real sequence, all easy, none estimable.
        val exposures = listOf(
            calibrationExposure(1, ExposureType.VOLUME, 12.5, OutcomeClass.ON_TARGET),
            calibrationExposure(2, ExposureType.STRENGTH, 10.0, OutcomeClass.STRONG),
            calibrationExposure(3, ExposureType.VOLUME, 7.5, OutcomeClass.ON_TARGET)
        )

        val prescription = requireNotNull(calculateNextPrescription(seededProfile(), exposures))

        // Before the fix this decayed to 5.0. The last probe was ON_TARGET, so it holds.
        assertTrue(prescription.prescribedLoadKg >= 7.5)
    }

    @Test
    fun `a failed calibration probe does lower the load`() {
        val prescription = requireNotNull(
            calculateNextPrescription(
                seededProfile(),
                listOf(calibrationExposure(1, ExposureType.VOLUME, 12.5, OutcomeClass.FAILED))
            )
        )

        assertTrue(prescription.prescribedLoadKg < 12.5)
    }

    @Test
    fun `with an e1RM available calibration derives the load from it`() {
        val prescription = requireNotNull(
            calculateNextPrescription(
                seededProfile().copy(ewmaVolumeE1rm = 60.0, volumePointCount = 1),
                listOf(calibrationExposure(1, ExposureType.VOLUME, 12.5, OutcomeClass.ON_TARGET, e1rm = 60.0))
            )
        )

        // 60 / (1 + (5 + 2) / 30) = 48.6 -> floored to the increment.
        assertEquals(47.5, prescription.prescribedLoadKg, 0.0)
    }

    private fun seededProfile() = ExerciseProgressionProfile(
        variantKey = "dominadas",
        state = ProgressionState.CALIBRATING,
        loadVolumeKg = 12.5,
        loadStrengthKg = 12.5,
        loadIncrementKg = increment
    )

    @Test
    fun `calibration steers from what was lifted, not from what was prescribed`() {
        // Session 11 of the owner's real data: the engine asked for 10 kg, they lifted 15 kg x 9
        // and still had 3 in reserve. Steering from the prescribed 10 would keep underestimating
        // them; steering from the lifted 15 is the only honest starting point.
        val prescription = requireNotNull(
            calculateNextPrescription(
                seededProfile(),
                listOf(
                    calibrationExposure(
                        index = 1,
                        type = ExposureType.STRENGTH,
                        load = 10.0,
                        outcome = OutcomeClass.STRONG,
                        lifted = 15.0
                    )
                )
            )
        )

        // 15 lifted + one step for a STRONG probe, never anything anchored to the prescribed 10.
        assertEquals(17.5, prescription.prescribedLoadKg, 0.0)
    }

    private fun calibrationExposure(
        index: Int,
        type: ExposureType,
        load: Double,
        outcome: OutcomeClass,
        e1rm: Double? = null,
        lifted: Double? = null
    ) = ProgressionExposure(
        variantKey = "dominadas",
        workoutExerciseId = null,
        exposureIndex = index,
        performedAt = index.toLong(),
        type = type,
        prescribedLoadKg = load,
        prescribedRepMin = 7,
        prescribedRepMax = 7,
        prescribedTargetRir = 2,
        prescribedSets = 3,
        probeLoadKg = lifted,
        probeReps = 10,
        probeRir = 4,
        probeSurplus = 3,
        outcomeClass = outcome,
        setCompletionRatio = 1.0,
        exposureE1rm = e1rm,
        e1rmConfidence = e1rm?.let { E1rmConfidence.MEDIUM },
        excludeFromTrend = false,
        userFlaggedBadDay = false,
        intraSessionAdjustmentSteps = 0,
        decisionReason = ProgressionReason.CALIBRATION_BASELINE.name
    )
}
