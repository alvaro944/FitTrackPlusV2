package com.alvarocervantes.fittrackplus.domain.usecase

import com.alvarocervantes.fittrackplus.data.repository.ProgressionRepository
import com.alvarocervantes.fittrackplus.domain.repository.ExerciseLoadHistory
import com.alvarocervantes.fittrackplus.domain.model.progression.ExerciseProgressionProfile
import com.alvarocervantes.fittrackplus.domain.model.progression.ProgressionExposure
import com.alvarocervantes.fittrackplus.domain.model.progression.ProgressionState
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Two machines for the same movement are the same pattern with different resistance. Each keeps its
 * own profile so the plates do not contaminate one another's trend, but a variant with nothing of
 * its own must not start from zero just because the usual machine was busy.
 */
class CalculateNextPrescriptionSeedTest {

    private val main = "press-banca-maquina-vieja"
    private val alternative = "press-banca-maquina-nueva"

    @Test
    fun `a variant with its own history is seeded from that history, not from a sibling`() = runTest {
        val progression = FakeProgressionRepository()
        progression.profiles[main] = profileWithLoad(main, 90.0)
        val workout = fakeHistory(mapOf(alternative to 70.0))

        val prescription = useCase(progression, workout)(
            variantKey = alternative,
            siblingVariantKeys = listOf(main, alternative)
        )

        assertNotNull(prescription)
        assertEquals(70.0, requireNotNull(progression.profiles[alternative]).loadVolumeKg)
    }

    @Test
    fun `a brand new variant is seeded from a sibling profile`() = runTest {
        val progression = FakeProgressionRepository()
        progression.profiles[main] = profileWithLoad(main, 90.0)
        val workout = fakeHistory(emptyMap())

        val prescription = useCase(progression, workout)(
            variantKey = alternative,
            siblingVariantKeys = listOf(main, alternative)
        )

        assertNotNull(prescription)
        assertEquals(90.0, requireNotNull(progression.profiles[alternative]).loadVolumeKg)
    }

    @Test
    fun `a brand new variant falls back to a sibling's logged history`() = runTest {
        val progression = FakeProgressionRepository()
        val workout = fakeHistory(mapOf(main to 85.0))

        val prescription = useCase(progression, workout)(
            variantKey = alternative,
            siblingVariantKeys = listOf(main, alternative)
        )

        assertNotNull(prescription)
        assertEquals(85.0, requireNotNull(progression.profiles[alternative]).loadVolumeKg)
    }

    @Test
    fun `with nothing known anywhere there is no prescription and no profile invented`() = runTest {
        val progression = FakeProgressionRepository()
        val workout = fakeHistory(emptyMap())

        val prescription = useCase(progression, workout)(
            variantKey = alternative,
            siblingVariantKeys = listOf(main, alternative)
        )

        assertNull(prescription)
        assertNull(progression.profiles[alternative])
    }

    @Test
    fun `the sibling profile is never merged into the new one`() = runTest {
        val progression = FakeProgressionRepository()
        progression.profiles[main] = profileWithLoad(main, 90.0).copy(
            state = ProgressionState.PROGRESSING,
            ewmaStrengthE1rm = 120.0,
            strengthPointCount = 8
        )
        val workout = fakeHistory(emptyMap())

        useCase(progression, workout)(
            variantKey = alternative,
            siblingVariantKeys = listOf(main, alternative)
        )

        // The seed is a starting load, not a transplant: the new machine calibrates on its own.
        val seeded = requireNotNull(progression.profiles[alternative])
        assertEquals(ProgressionState.CALIBRATING, seeded.state)
        assertNull(seeded.ewmaStrengthE1rm)
        assertEquals(0, seeded.strengthPointCount)
    }

    private fun useCase(
        progression: ProgressionRepository,
        workout: ExerciseLoadHistory
    ) = CalculateNextPrescriptionUseCase(progression, workout)

    private fun fakeHistory(heaviest: Map<String, Double>) = ExerciseLoadHistory { heaviest[it] }

    private fun profileWithLoad(variantKey: String, load: Double) = ExerciseProgressionProfile(
        variantKey = variantKey,
        state = ProgressionState.CALIBRATING,
        loadVolumeKg = load,
        loadStrengthKg = load
    )

    private class FakeProgressionRepository : ProgressionRepository {
        val profiles = mutableMapOf<String, ExerciseProgressionProfile>()

        override suspend fun getProfile(variantKey: String) = profiles[variantKey]
        override suspend fun upsertProfile(profile: ExerciseProgressionProfile) {
            profiles[profile.variantKey] = profile
        }
        override suspend fun getExposures(variantKey: String): List<ProgressionExposure> = emptyList()
        override suspend fun insertExposure(exposure: ProgressionExposure): Long = 1L
    }
}
