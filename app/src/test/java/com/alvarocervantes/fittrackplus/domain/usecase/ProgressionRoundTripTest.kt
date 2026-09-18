package com.alvarocervantes.fittrackplus.domain.usecase

import com.alvarocervantes.fittrackplus.data.repository.ProgressionRepository
import com.alvarocervantes.fittrackplus.domain.model.progression.ExerciseProgressionProfile
import com.alvarocervantes.fittrackplus.domain.model.progression.ProgressionExposure
import com.alvarocervantes.fittrackplus.domain.model.progression.ProgressionState
import com.alvarocervantes.fittrackplus.domain.repository.ExerciseLoadHistory
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Drives the engine the way the app does, session after session: prescribe, train, record,
 * prescribe again. Every other engine test builds a profile and a history by hand and calls the
 * decision once, so none of them could see what happens when the recorder and the calculator both
 * touch the same profile.
 */
class ProgressionRoundTripTest {

    @Test
    fun `many estimable sessions in a row never break the engine`() = runTest {
        val progression = InMemoryProgression()
        val history = ExerciseLoadHistory { 70.0 }
        val calculate = CalculateNextPrescriptionUseCase(progression, history)
        val record = RecordProgressionExposureUseCase(progression)

        repeat(SESSIONS) { session ->
            val prescription = requireNotNull(calculate(VARIANT)) {
                "Session ${session + 1} produced no prescription"
            }

            // Estimable every time: reps + rir stays under the confidence cutoff.
            record(
                RecordProgressionExposureUseCase.Input(
                    variantKey = VARIANT,
                    workoutExerciseId = null,
                    performedAt = session.toLong(),
                    prescription = prescription,
                    probeLoadKg = prescription.prescribedLoadKg,
                    probeReps = prescription.prescribedRepMin,
                    probeRir = 2,
                    completedSetCount = prescription.prescribedSets,
                    userFlaggedBadDay = false
                )
            )
        }

        assertEquals(SESSIONS, progression.exposures.size)
    }

    @Test
    fun `stored point counts always match the estimable history`() = runTest {
        val progression = InMemoryProgression()
        val calculate = CalculateNextPrescriptionUseCase(progression, ExerciseLoadHistory { 70.0 })
        val record = RecordProgressionExposureUseCase(progression)

        repeat(SESSIONS) { session ->
            val prescription = requireNotNull(calculate(VARIANT))
            record(
                RecordProgressionExposureUseCase.Input(
                    variantKey = VARIANT,
                    workoutExerciseId = null,
                    performedAt = session.toLong(),
                    prescription = prescription,
                    probeLoadKg = prescription.prescribedLoadKg,
                    probeReps = prescription.prescribedRepMin,
                    probeRir = 2,
                    completedSetCount = prescription.prescribedSets,
                    userFlaggedBadDay = false
                )
            )

            val profile = requireNotNull(progression.profiles[VARIANT])
            val estimable = progression.exposures.filter { it.exposureE1rm != null && !it.excludeFromTrend }
            // The profile stored after a session must already account for exactly what happened.
            // The last exposure is folded in once, at read time; if the recorder folds it in too,
            // these drift apart and the trend check throws.
            assertEquals(
                "strength points after session ${session + 1}",
                estimable.count { it.type.name == "STRENGTH" },
                profile.strengthPointCount + pendingStrength(progression)
            )
        }
    }

    /** Points of the last exposure, which the stored profile deliberately does not include yet. */
    private fun pendingStrength(progression: InMemoryProgression): Int {
        val last = progression.exposures.lastOrNull() ?: return 0
        return if (last.type.name == "STRENGTH" && last.exposureE1rm != null && !last.excludeFromTrend) 1 else 0
    }

    private class InMemoryProgression : ProgressionRepository {
        val profiles = mutableMapOf<String, ExerciseProgressionProfile>(
            VARIANT to ExerciseProgressionProfile(
                variantKey = VARIANT,
                state = ProgressionState.CALIBRATING,
                loadVolumeKg = 60.0,
                loadStrengthKg = 60.0
            )
        )
        val exposures = mutableListOf<ProgressionExposure>()

        override suspend fun getProfile(variantKey: String) = profiles[variantKey]
        override suspend fun upsertProfile(profile: ExerciseProgressionProfile) {
            profiles[profile.variantKey] = profile
        }
        override suspend fun getExposures(variantKey: String) = exposures.filter { it.variantKey == variantKey }
        override suspend fun insertExposure(exposure: ProgressionExposure): Long {
            exposures += exposure
            return exposures.size.toLong()
        }
    }

    private companion object {
        const val VARIANT = "press-banca"
        const val SESSIONS = 14
    }
}
