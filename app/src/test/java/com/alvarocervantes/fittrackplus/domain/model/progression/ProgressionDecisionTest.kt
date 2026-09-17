package com.alvarocervantes.fittrackplus.domain.model.progression

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressionDecisionTest {

    @Test
    fun `one failed exposure at a confirmed load keeps that lane load`() {
        val prescription = requirePrescription(
            progressingProfile(consecutiveFailStrength = 0),
            listOf(exposure(type = ExposureType.STRENGTH, outcome = OutcomeClass.FAILED))
        )

        assertEquals(100.0, requireNotNull(prescription.nextProfile.loadStrengthKg), 0.0)
        assertEquals(1, prescription.nextProfile.consecutiveFailStrength)
        assertEquals(80.0, requireNotNull(prescription.nextProfile.loadVolumeKg), 0.0)
        assertEquals(ProgressionReason.LOAD_HELD_ONE_FAILURE, prescription.reason)
    }

    @Test
    fun `two failed exposures in the same lane reduce one increment`() {
        val prescription = requirePrescription(
            progressingProfile(consecutiveFailStrength = 1),
            listOf(
                exposure(type = ExposureType.STRENGTH, outcome = OutcomeClass.FAILED, index = 1),
                exposure(type = ExposureType.STRENGTH, outcome = OutcomeClass.FAILED, index = 2)
            )
        )

        assertEquals(97.5, requireNotNull(prescription.nextProfile.loadStrengthKg), 0.0)
        assertEquals(0, prescription.nextProfile.consecutiveFailStrength)
        assertEquals(1, prescription.nextProfile.stallCount)
        assertEquals(ProgressionReason.LOAD_REDUCED_REPEATED, prescription.reason)
    }

    @Test
    fun `failed pending confirmation reverts to the previous load`() {
        val prescription = requirePrescription(
            progressingProfile(
                loadStrengthKg = 100.0,
                previousLoadStrengthKg = 95.0,
                pendingConfirmStrength = true
            ),
            listOf(exposure(type = ExposureType.STRENGTH, outcome = OutcomeClass.FAILED))
        )

        assertEquals(95.0, requireNotNull(prescription.nextProfile.loadStrengthKg), 0.0)
        assertFalse(prescription.nextProfile.pendingConfirmStrength)
        assertEquals(1, prescription.nextProfile.stallCount)
        assertEquals(ProgressionReason.LOAD_REVERTED, prescription.reason)
    }

    @Test
    fun `easy and strong outcomes raise only their own lane`() {
        val easyPrescription = requirePrescription(
            progressingProfile(loadStrengthKg = 100.0),
            listOf(exposure(type = ExposureType.STRENGTH, outcome = OutcomeClass.EASY))
        )
        val strongPrescription = requirePrescription(
            progressingProfile(loadStrengthKg = 100.0),
            listOf(exposure(type = ExposureType.STRENGTH, outcome = OutcomeClass.STRONG))
        )

        assertEquals(105.0, requireNotNull(easyPrescription.nextProfile.loadStrengthKg), 0.0)
        assertEquals(102.5, requireNotNull(strongPrescription.nextProfile.loadStrengthKg), 0.0)
        assertEquals(80.0, requireNotNull(easyPrescription.nextProfile.loadVolumeKg), 0.0)
        assertTrue(easyPrescription.nextProfile.pendingConfirmStrength)
    }

    @Test
    fun `probe adjustment is bounded and next decision ignores adjusted set load`() {
        assertEquals(
            ProgressionTuning.STEP_REDUCE,
            intraSessionAdjustmentSteps(-2, ProgressionState.PROGRESSING, ExposureType.STRENGTH)
        )

        val baseProfile = progressingProfile(consecutiveFailStrength = 1)
        val earlierFailedExposure = exposure(
            type = ExposureType.STRENGTH,
            outcome = OutcomeClass.FAILED,
            surplus = -2,
            index = 1
        )
        val loweredSets = exposure(
            type = ExposureType.STRENGTH,
            outcome = OutcomeClass.FAILED,
            surplus = -2,
            intraSessionAdjustmentSteps = ProgressionTuning.STEP_REDUCE,
            index = 2
        )
        val raisedSets = loweredSets.copy(intraSessionAdjustmentSteps = ProgressionTuning.STEP_STRONG)

        val afterLoweredSets = requirePrescription(baseProfile, listOf(earlierFailedExposure, loweredSets))
        val afterRaisedSets = requirePrescription(baseProfile, listOf(earlierFailedExposure, raisedSets))

        assertEquals(97.5, requireNotNull(afterLoweredSets.nextProfile.loadStrengthKg), 0.0)
        assertEquals(
            afterLoweredSets.nextProfile.loadStrengthKg,
            afterRaisedSets.nextProfile.loadStrengthKg
        )
    }

    @Test
    fun `recovery cooldown blocks every trigger before four exposures`() {
        val prescription = requirePrescription(
            progressingProfile(
                exposuresSinceRecovery = ProgressionTuning.RECOVERY_COOLDOWN_EXPOSURES - 2,
                hardExposureStreak = ProgressionTuning.R1_HARD_STREAK_CAP - 1
            ),
            listOf(exposure(type = ExposureType.STRENGTH, outcome = OutcomeClass.HARD))
        )

        assertEquals(ProgressionState.PROGRESSING, prescription.nextProfile.state)
    }

    @Test
    fun `unknown trend does not block hard streak trigger`() {
        val prescription = requirePrescription(
            progressingProfile(
                exposuresSinceRecovery = ProgressionTuning.RECOVERY_COOLDOWN_EXPOSURES - 1,
                hardExposureStreak = ProgressionTuning.R1_HARD_STREAK_CAP - 1
            ),
            listOf(exposure(type = ExposureType.STRENGTH, outcome = OutcomeClass.HARD))
        )

        assertEquals(ProgressionState.RECOVERING, prescription.nextProfile.state)
        assertEquals(ProgressionTuning.RECOVERY_HARD_TRIGGER_EXPOSURES, prescription.nextProfile.recoveryExposuresRemaining)
    }

    @Test
    fun `unknown trend does not block low surplus trigger`() {
        val lowSurplus = listOf(
            exposure(OutcomeClass.ON_TARGET, surplus = -1, index = 1),
            exposure(OutcomeClass.ON_TARGET, surplus = -1, index = 2),
            exposure(OutcomeClass.ON_TARGET, surplus = -1, index = 3)
        )

        val prescription = requirePrescription(
            progressingProfile(exposuresSinceRecovery = ProgressionTuning.RECOVERY_COOLDOWN_EXPOSURES - 1),
            lowSurplus
        )

        assertEquals(ProgressionState.RECOVERING, prescription.nextProfile.state)
        assertEquals(ProgressionTuning.STEP_STRONG, prescription.nextProfile.recoveryExposuresRemaining)
    }

    @Test
    fun `unknown trend does not block stall trigger`() {
        val prescription = requirePrescription(
            progressingProfile(
                exposuresSinceRecovery = ProgressionTuning.RECOVERY_COOLDOWN_EXPOSURES - 1,
                stallCount = ProgressionTuning.R4_STALL_COUNT
            ),
            listOf(exposure(type = ExposureType.STRENGTH, outcome = OutcomeClass.ON_TARGET))
        )

        assertEquals(ProgressionState.RECOVERING, prescription.nextProfile.state)
        assertEquals(ProgressionTuning.STEP_STRONG, prescription.nextProfile.recoveryExposuresRemaining)
    }

    @Test
    fun `unknown trend does not block repeated hard exposure trigger`() {
        val prescription = requirePrescription(
            progressingProfile(
                exposuresSinceRecovery = ProgressionTuning.RECOVERY_COOLDOWN_EXPOSURES - 1,
                hardExposureStreak = ProgressionTuning.R5_HARD_STREAK - 1
            ),
            listOf(exposure(type = ExposureType.STRENGTH, outcome = OutcomeClass.HARD))
        )

        assertEquals(ProgressionState.RECOVERING, prescription.nextProfile.state)
        assertEquals(ProgressionTuning.STEP_STRONG, prescription.nextProfile.recoveryExposuresRemaining)
    }

    @Test
    fun `bad day is excluded from trend recovery triggers and consecutive failures`() {
        val badDayExposures = listOf(
            exposure(OutcomeClass.FAILED, e1rm = 100.0, userFlaggedBadDay = true, index = 1),
            exposure(OutcomeClass.FAILED, e1rm = 90.0, userFlaggedBadDay = true, index = 2),
            exposure(OutcomeClass.FAILED, e1rm = 80.0, userFlaggedBadDay = true, index = 3)
        )
        val profile = progressingProfile(
            exposuresSinceRecovery = ProgressionTuning.RECOVERY_COOLDOWN_EXPOSURES - 1,
            consecutiveFailStrength = 1
        )

        val prescription = requirePrescription(profile, badDayExposures)

        assertEquals(StrengthTrend.UNKNOWN, calculateStrengthTrend(profile, badDayExposures))
        assertEquals(ProgressionState.PROGRESSING, prescription.nextProfile.state)
        assertEquals(1, prescription.nextProfile.consecutiveFailStrength)
        assertEquals(100.0, requireNotNull(prescription.nextProfile.loadStrengthKg), 0.0)
    }

    @Test
    fun `two failed evaluations return to calibrating`() {
        val prescription = requirePrescription(
            progressingProfile(
                state = ProgressionState.EVALUATING,
                evaluationFailStreak = 1,
                loadVolumeKg = 80.0,
                loadStrengthKg = 100.0
            ),
            listOf(exposure(type = ExposureType.EVALUATION, outcome = OutcomeClass.FAILED, surplus = -1))
        )

        assertEquals(ProgressionState.CALIBRATING, prescription.nextProfile.state)
        assertEquals(90.0, requireNotNull(prescription.nextProfile.loadVolumeKg), 0.0)
        assertEquals(90.0, requireNotNull(prescription.nextProfile.loadStrengthKg), 0.0)
    }

    @Test
    fun `calibration exits after valid samples from both lanes and an estimate`() {
        val calibrationHistory = listOf(
            exposure(OutcomeClass.ON_TARGET, type = ExposureType.VOLUME, e1rm = 90.0, index = 1),
            exposure(OutcomeClass.ON_TARGET, type = ExposureType.STRENGTH, e1rm = 100.0, index = 2),
            exposure(OutcomeClass.ON_TARGET, type = ExposureType.VOLUME, e1rm = 95.0, index = 3)
        )
        val profile = progressingProfile(
            state = ProgressionState.CALIBRATING,
            ewmaVolumeE1rm = 90.0,
            ewmaStrengthE1rm = 100.0,
            nextExposureType = ExposureType.VOLUME
        ).copy(volumePointCount = 1, strengthPointCount = 1)

        val prescription = requirePrescription(profile, calibrationHistory)

        assertEquals(ProgressionState.PROGRESSING, prescription.nextProfile.state)
        assertEquals(ExposureType.VOLUME, prescription.type)
    }

    @Test
    fun `last recovery exposure moves to evaluation`() {
        val prescription = requirePrescription(
            progressingProfile(
                state = ProgressionState.RECOVERING,
                recoveryExposuresRemaining = 1
            ),
            listOf(exposure(type = ExposureType.RECOVERY, outcome = OutcomeClass.ON_TARGET))
        )

        assertEquals(ProgressionState.EVALUATING, prescription.nextProfile.state)
        assertEquals(ExposureType.EVALUATION, prescription.type)
    }

    @Test
    fun `passed evaluation restores normal progression and resets recovery counters`() {
        val prescription = requirePrescription(
            progressingProfile(
                state = ProgressionState.EVALUATING,
                hardExposureStreak = 4,
                stallCount = 2,
                evaluationFailStreak = 1,
                exposuresSinceRecovery = 5
            ),
            listOf(exposure(type = ExposureType.EVALUATION, outcome = OutcomeClass.ON_TARGET, surplus = 0))
        )

        assertEquals(ProgressionState.PROGRESSING, prescription.nextProfile.state)
        assertEquals(0, prescription.nextProfile.hardExposureStreak)
        assertEquals(0, prescription.nextProfile.stallCount)
        assertEquals(0, prescription.nextProfile.evaluationFailStreak)
        assertEquals(0, prescription.nextProfile.exposuresSinceRecovery)
    }

    @Test
    fun `first failed evaluation demotes both lanes and keeps progressing`() {
        val prescription = requirePrescription(
            progressingProfile(state = ProgressionState.EVALUATING, loadVolumeKg = 80.0, loadStrengthKg = 100.0),
            listOf(exposure(type = ExposureType.EVALUATION, outcome = OutcomeClass.FAILED, surplus = -1))
        )

        assertEquals(ProgressionState.PROGRESSING, prescription.nextProfile.state)
        assertEquals(75.0, requireNotNull(prescription.nextProfile.loadVolumeKg), 0.0)
        assertEquals(95.0, requireNotNull(prescription.nextProfile.loadStrengthKg), 0.0)
        assertEquals(1, prescription.nextProfile.evaluationFailStreak)
    }

    @Test
    fun `calibration starts volume at the floor of its range`() {
        val prescription = requirePrescription(
            ExerciseProgressionProfile(
                variantKey = "squat",
                loadVolumeKg = 70.0,
                loadStrengthKg = 70.0
            ),
            emptyList()
        )

        assertEquals(ExposureType.VOLUME, prescription.type)
        assertEquals(ProgressionTuning.CALIBRATION_VOLUME_REPS, prescription.prescribedRepMin)
        assertEquals(ProgressionTuning.CALIBRATION_VOLUME_REPS, prescription.prescribedRepMax)
        assertEquals(ProgressionTuning.CALIBRATION_TARGET_RIR, prescription.prescribedTargetRir)
    }

    @Test
    fun `specificity changes both lane prescriptions as strength approaches the goal`() {
        val base = requirePrescription(
            progressingProfile(goalWeightKg = 100.0, ewmaStrengthE1rm = 84.0),
            emptyList()
        )
        val specific = requirePrescription(
            progressingProfile(goalWeightKg = 100.0, ewmaStrengthE1rm = 90.0),
            emptyList()
        )
        val peaking = requirePrescription(
            progressingProfile(
                goalWeightKg = 100.0,
                ewmaStrengthE1rm = 96.0,
                nextExposureType = ExposureType.STRENGTH
            ),
            emptyList()
        )

        assertEquals(7, base.prescribedRepMin)
        assertEquals(8, specific.prescribedRepMax)
        assertEquals(ProgressionTuning.SPECIFIC_VOLUME_TARGET_RIR, specific.prescribedTargetRir)
        assertEquals(2, peaking.prescribedRepMin)
        assertEquals(4, peaking.prescribedRepMax)
        assertEquals(ProgressionTuning.PEAKING_STRENGTH_TARGET_RIR, peaking.prescribedTargetRir)
    }

    @Test
    fun `displayed e1rm comes from the strength ewma`() {
        val prescription = requirePrescription(
            progressingProfile(ewmaStrengthE1rm = 130.0, ewmaVolumeE1rm = 150.0),
            emptyList()
        )

        assertEquals(130.0, requireNotNull(prescription.displayedE1rm), 0.0)
        assertNotEquals(prescription.nextProfile.ewmaVolumeE1rm, prescription.displayedE1rm)
        assertNotNull(prescription.reason)
    }

    /**
     * The engine returns null when calibration has no seed. Every test below exercises a seeded
     * profile, so they assert on a prescription that must exist.
     */
    private fun requirePrescription(
        profile: ExerciseProgressionProfile,
        recentExposures: List<ProgressionExposure>
    ): ProgressionPrescription = requireNotNull(calculateNextPrescription(profile, recentExposures))

    private fun progressingProfile(
        state: ProgressionState = ProgressionState.PROGRESSING,
        loadVolumeKg: Double = 80.0,
        loadStrengthKg: Double = 100.0,
        previousLoadStrengthKg: Double? = null,
        pendingConfirmStrength: Boolean = false,
        consecutiveFailStrength: Int = 0,
        hardExposureStreak: Int = 0,
        exposuresSinceRecovery: Int = 0,
        stallCount: Int = 0,
        recoveryExposuresRemaining: Int = 0,
        evaluationFailStreak: Int = 0,
        goalWeightKg: Double? = null,
        ewmaStrengthE1rm: Double? = null,
        ewmaVolumeE1rm: Double? = null,
        nextExposureType: ExposureType = ExposureType.VOLUME
    ): ExerciseProgressionProfile {
        return ExerciseProgressionProfile(
            variantKey = "squat",
            state = state,
            loadVolumeKg = loadVolumeKg,
            loadStrengthKg = loadStrengthKg,
            previousLoadStrengthKg = previousLoadStrengthKg,
            pendingConfirmStrength = pendingConfirmStrength,
            consecutiveFailStrength = consecutiveFailStrength,
            hardExposureStreak = hardExposureStreak,
            exposuresSinceRecovery = exposuresSinceRecovery,
            stallCount = stallCount,
            recoveryExposuresRemaining = recoveryExposuresRemaining,
            evaluationFailStreak = evaluationFailStreak,
            goalWeightKg = goalWeightKg,
            ewmaStrengthE1rm = ewmaStrengthE1rm,
            ewmaVolumeE1rm = ewmaVolumeE1rm,
            nextExposureType = nextExposureType
        )
    }

    private fun exposure(
        outcome: OutcomeClass,
        type: ExposureType = ExposureType.STRENGTH,
        surplus: Int = 0,
        e1rm: Double? = null,
        userFlaggedBadDay: Boolean = false,
        intraSessionAdjustmentSteps: Int = 0,
        index: Int = 1
    ): ProgressionExposure {
        return ProgressionExposure(
            variantKey = "squat",
            workoutExerciseId = null,
            exposureIndex = index,
            performedAt = index.toLong(),
            type = type,
            prescribedLoadKg = 100.0,
            prescribedRepMin = 4,
            prescribedRepMax = 6,
            prescribedTargetRir = 2,
            prescribedSets = 3,
            probeReps = 5,
            probeRir = 2,
            probeSurplus = surplus,
            outcomeClass = outcome,
            setCompletionRatio = 1.0,
            exposureE1rm = e1rm,
            e1rmConfidence = e1rm?.let { E1rmConfidence.HIGH },
            excludeFromTrend = false,
            userFlaggedBadDay = userFlaggedBadDay,
            intraSessionAdjustmentSteps = intraSessionAdjustmentSteps,
            decisionReason = "test"
        )
    }
}

/**
 * Regression test for the crash found on 2026-09-17 during the manual pass: marking an exercise as
 * PRIMARY created a profile in CALIBRATING with no starting load, and the engine threw
 * IllegalStateException the moment the workout screen asked for a prescription.
 *
 * An exercise with no history to calibrate from is an ordinary state, not a programming error.
 */
class ProgressionCalibrationSeedTest {

    @Test
    fun `an unseeded calibrating profile yields no prescription instead of throwing`() {
        val unseeded = ExerciseProgressionProfile(
            variantKey = "bench",
            state = ProgressionState.CALIBRATING,
            loadVolumeKg = null,
            loadStrengthKg = null
        )

        val prescription = calculateNextPrescription(unseeded, emptyList())

        assertNull(prescription)
    }

    @Test
    fun `a seeded calibrating profile does yield a prescription`() {
        val seeded = ExerciseProgressionProfile(
            variantKey = "bench",
            state = ProgressionState.CALIBRATING,
            loadVolumeKg = 60.0,
            loadStrengthKg = 60.0
        )

        val prescription = calculateNextPrescription(seeded, emptyList())

        assertNotNull(prescription)
        assertNotNull(requireNotNull(prescription).reason)
    }
}
