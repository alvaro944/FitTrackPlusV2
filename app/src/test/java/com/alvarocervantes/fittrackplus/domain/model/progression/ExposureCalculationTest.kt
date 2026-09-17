package com.alvarocervantes.fittrackplus.domain.model.progression

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExposureCalculationTest {

    @Test
    fun `strength baseline classifications follow criterion 13`() {
        val strong = classifyExposure(
            probeReps = 5,
            probeRir = 4,
            prescribedRepMin = 4,
            prescribedRepMax = 6,
            prescribedTargetRir = 2,
            completedSetCount = 3,
            prescribedSets = 3
        )
        val onTarget = classifyExposure(
            probeReps = 5,
            probeRir = 2,
            prescribedRepMin = 4,
            prescribedRepMax = 6,
            prescribedTargetRir = 2,
            completedSetCount = 3,
            prescribedSets = 3
        )
        val failedAtFive = classifyExposure(
            probeReps = 5,
            probeRir = 0,
            prescribedRepMin = 4,
            prescribedRepMax = 6,
            prescribedTargetRir = 2,
            completedSetCount = 3,
            prescribedSets = 3
        )
        val failedBelowMinimum = classifyExposure(
            probeReps = 4,
            probeRir = 0,
            prescribedRepMin = 4,
            prescribedRepMax = 6,
            prescribedTargetRir = 2,
            completedSetCount = 3,
            prescribedSets = 3
        )

        assertEquals(OutcomeClass.STRONG, strong.outcomeClass)
        assertEquals(OutcomeClass.ON_TARGET, onTarget.outcomeClass)
        assertEquals(OutcomeClass.FAILED, failedAtFive.outcomeClass)
        assertEquals(OutcomeClass.FAILED, failedBelowMinimum.outcomeClass)
        assertEquals(2, strong.surplus)
        assertEquals(0, onTarget.surplus)
        assertEquals(-2, failedAtFive.surplus)
        assertEquals(-3, failedBelowMinimum.surplus)
    }

    @Test
    fun `first guard caps an otherwise easy probe at hard`() {
        val result = classifyExposure(
            probeReps = 4,
            probeRir = 6,
            prescribedRepMin = 5,
            prescribedRepMax = 7,
            prescribedTargetRir = 2,
            completedSetCount = 3,
            prescribedSets = 3
        )

        assertEquals(OutcomeClass.HARD, result.outcomeClass)
    }

    @Test
    fun `second guard forces failed when probe is more than one rep below minimum`() {
        val result = classifyExposure(
            probeReps = 3,
            probeRir = 4,
            prescribedRepMin = 5,
            prescribedRepMax = 7,
            prescribedTargetRir = 2,
            completedSetCount = 3,
            prescribedSets = 3
        )

        assertEquals(OutcomeClass.FAILED, result.outcomeClass)
    }

    @Test
    fun `third guard downgrades outcome when set completion is below threshold`() {
        val result = classifyExposure(
            probeReps = 6,
            probeRir = 5,
            prescribedRepMin = 5,
            prescribedRepMax = 7,
            prescribedTargetRir = 2,
            completedSetCount = 1,
            prescribedSets = 3
        )

        assertEquals(OutcomeClass.STRONG, result.outcomeClass)
    }

    @Test
    fun `fourth guard downgrades missing rir and excludes it from trend`() {
        val result = classifyExposure(
            probeReps = 6,
            probeRir = null,
            prescribedRepMin = 5,
            prescribedRepMax = 7,
            prescribedTargetRir = 2,
            completedSetCount = 3,
            prescribedSets = 3
        )

        assertNull(result.surplus)
        assertEquals(OutcomeClass.HARD, result.outcomeClass)
        assertTrue(result.excludeFromTrend)
    }

    @Test
    fun `guards apply in order when multiple conditions are true`() {
        val result = classifyExposure(
            probeReps = 3,
            probeRir = null,
            prescribedRepMin = 5,
            prescribedRepMax = 7,
            prescribedTargetRir = 2,
            completedSetCount = 1,
            prescribedSets = 3
        )

        assertEquals(OutcomeClass.FAILED, result.outcomeClass)
        assertTrue(result.excludeFromTrend)
    }

    @Test
    fun `volume and strength ewmas never mix`() {
        val profile = ExerciseProgressionProfile(
            variantKey = "squat",
            ewmaStrengthE1rm = 100.0,
            ewmaVolumeE1rm = 120.0,
            strengthPointCount = 3,
            volumePointCount = 3
        )

        val afterVolume = updateE1rmEwmas(
            profile,
            exposure(type = ExposureType.VOLUME, e1rm = 130.0, confidence = E1rmConfidence.HIGH)
        )
        val afterStrength = updateE1rmEwmas(
            afterVolume,
            exposure(type = ExposureType.STRENGTH, e1rm = 110.0, confidence = E1rmConfidence.MEDIUM)
        )

        assertEquals(100.0, requireNotNull(afterVolume.ewmaStrengthE1rm), 0.0)
        assertEquals(123.0, requireNotNull(afterVolume.ewmaVolumeE1rm), 0.0)
        assertEquals(101.8, requireNotNull(afterStrength.ewmaStrengthE1rm), 0.0)
        assertEquals(123.0, requireNotNull(afterStrength.ewmaVolumeE1rm), 0.0)
    }

    @Test
    fun `strength trend wins over volume trend when both have enough points`() {
        val exposures = listOf(
                exposure(type = ExposureType.STRENGTH, e1rm = 100.0),
                exposure(type = ExposureType.STRENGTH, e1rm = 102.0),
                exposure(type = ExposureType.STRENGTH, e1rm = 104.0),
                exposure(type = ExposureType.STRENGTH, e1rm = 106.0),
                exposure(type = ExposureType.VOLUME, e1rm = 100.0),
                exposure(type = ExposureType.VOLUME, e1rm = 90.0),
                exposure(type = ExposureType.VOLUME, e1rm = 80.0),
                exposure(type = ExposureType.VOLUME, e1rm = 70.0)
            )
        val trend = calculateStrengthTrend(profileFor(exposures), exposures)

        assertEquals(StrengthTrend.RISING, trend)
    }

    @Test
    fun `volume trend is fallback and insufficient data is unknown`() {
        val exposures = listOf(
                exposure(type = ExposureType.STRENGTH, e1rm = 100.0),
                exposure(type = ExposureType.STRENGTH, e1rm = 102.0),
                exposure(type = ExposureType.STRENGTH, e1rm = 104.0),
                exposure(type = ExposureType.VOLUME, e1rm = 100.0),
                exposure(type = ExposureType.VOLUME, e1rm = 102.0),
                exposure(type = ExposureType.VOLUME, e1rm = 104.0),
                exposure(type = ExposureType.VOLUME, e1rm = 106.0)
            )
        val volumeTrend = calculateStrengthTrend(profileFor(exposures), exposures)

        assertEquals(StrengthTrend.RISING, volumeTrend)
        val insufficientExposures = listOf(exposure(type = ExposureType.STRENGTH, e1rm = 100.0))
        assertEquals(
            StrengthTrend.UNKNOWN,
            calculateStrengthTrend(profileFor(insufficientExposures), insufficientExposures)
        )
    }

    @Test
    fun `asymmetric trend bands keep a small decline flat`() {
        val exposures = listOf(
                exposure(type = ExposureType.STRENGTH, e1rm = 100.0),
                exposure(type = ExposureType.STRENGTH, e1rm = 99.0),
                exposure(type = ExposureType.STRENGTH, e1rm = 98.0),
                exposure(type = ExposureType.STRENGTH, e1rm = 97.0)
            )
        val trend = calculateStrengthTrend(profileFor(exposures), exposures)

        assertEquals(StrengthTrend.FLAT, trend)
    }

    @Test
    fun `asymmetric trend bands classify a large decline as falling`() {
        val exposures = listOf(
                exposure(type = ExposureType.STRENGTH, e1rm = 100.0),
                exposure(type = ExposureType.STRENGTH, e1rm = 90.0),
                exposure(type = ExposureType.STRENGTH, e1rm = 80.0),
                exposure(type = ExposureType.STRENGTH, e1rm = 70.0)
            )
        val trend = calculateStrengthTrend(profileFor(exposures), exposures)

        assertEquals(StrengthTrend.FALLING, trend)
    }

    @Test
    fun `truncated trend history is rejected instead of silently becoming unknown`() {
        val fullHistory = listOf(
            exposure(type = ExposureType.STRENGTH, e1rm = 100.0),
            exposure(type = ExposureType.STRENGTH, e1rm = 102.0),
            exposure(type = ExposureType.STRENGTH, e1rm = 104.0),
            exposure(type = ExposureType.STRENGTH, e1rm = 106.0)
        )

        assertThrows(IllegalArgumentException::class.java) {
            calculateStrengthTrend(profileFor(fullHistory), fullHistory.takeLast(1))
        }
    }

    private fun exposure(
        type: ExposureType,
        e1rm: Double,
        confidence: E1rmConfidence = E1rmConfidence.HIGH
    ): ProgressionExposure {
        return ProgressionExposure(
            variantKey = "squat",
            workoutExerciseId = null,
            exposureIndex = nextExposureIndex++,
            performedAt = nextExposureIndex.toLong(),
            type = type,
            prescribedLoadKg = 100.0,
            prescribedRepMin = 4,
            prescribedRepMax = 6,
            prescribedTargetRir = 2,
            prescribedSets = 3,
            probeReps = 5,
            probeRir = 2,
            probeSurplus = 0,
            outcomeClass = OutcomeClass.ON_TARGET,
            setCompletionRatio = 1.0,
            exposureE1rm = e1rm,
            e1rmConfidence = confidence,
            excludeFromTrend = false,
            userFlaggedBadDay = false,
            intraSessionAdjustmentSteps = 0,
            decisionReason = "test"
        )
    }

    private fun profileFor(exposures: List<ProgressionExposure>): ExerciseProgressionProfile {
        return ExerciseProgressionProfile(
            variantKey = "squat",
            strengthPointCount = exposures.count { it.type == ExposureType.STRENGTH },
            volumePointCount = exposures.count { it.type == ExposureType.VOLUME }
        )
    }

    private companion object {
        var nextExposureIndex = 0
    }
}
