package com.alvarocervantes.fittrackplus.domain.usecase

import com.alvarocervantes.fittrackplus.data.repository.ProgressionRepository
import com.alvarocervantes.fittrackplus.domain.repository.ExerciseLoadHistory
import com.alvarocervantes.fittrackplus.domain.model.progression.ExerciseProgressionProfile
import com.alvarocervantes.fittrackplus.domain.model.progression.ProgressionPrescription
import com.alvarocervantes.fittrackplus.domain.model.progression.ProgressionState
import com.alvarocervantes.fittrackplus.domain.model.progression.ProgressionTuning
import com.alvarocervantes.fittrackplus.domain.model.progression.calculateNextPrescription
import javax.inject.Inject

/**
 * Resolves what the engine prescribes for the variant actually being performed.
 *
 * Each variant keeps its own profile on purpose. Two machines for the same movement are the same
 * pattern with different resistance: the plates differ, so the loads and the e1RM differ, and
 * merging them into one series would add a sawtooth that the trend detector would read as real
 * change. Keeping them apart also lets the owner compare how they are doing on each machine.
 *
 * What a separate profile must NOT do is start from zero every time the busy machine forces a swap,
 * so a variant with no profile of its own is seeded from what is already known: its own history
 * first, then a sibling variant of the same routine exercise.
 */
class CalculateNextPrescriptionUseCase @Inject constructor(
    private val progressionRepository: ProgressionRepository,
    private val loadHistory: ExerciseLoadHistory
) {

    suspend operator fun invoke(
        variantKey: String,
        siblingVariantKeys: List<String> = emptyList(),
        loadIncrementKg: Double = ProgressionTuning.DEFAULT_INCREMENT_UPPER_KG,
        goalWeightKg: Double? = null
    ): ProgressionPrescription? {
        val profile = progressionRepository.getProfile(variantKey)
            ?: newProfileFor(variantKey, siblingVariantKeys, loadIncrementKg, goalWeightKg)
            ?: return null
        val seeded = seedIfNeeded(profile, siblingVariantKeys) ?: return null
        return calculateNextPrescription(seeded, progressionRepository.getExposures(variantKey))
    }

    /**
     * Creates the profile the first time a PRIMARY exercise is performed with this variant, which is
     * what happens the moment the owner swaps to the machine next door.
     */
    private suspend fun newProfileFor(
        variantKey: String,
        siblingVariantKeys: List<String>,
        loadIncrementKg: Double,
        goalWeightKg: Double?
    ): ExerciseProgressionProfile? {
        val seed = resolveSeedKg(variantKey, siblingVariantKeys) ?: return null
        val created = ExerciseProgressionProfile(
            variantKey = variantKey,
            goalWeightKg = goalWeightKg,
            loadIncrementKg = loadIncrementKg,
            state = ProgressionState.CALIBRATING,
            loadVolumeKg = seed,
            loadStrengthKg = seed
        )
        progressionRepository.upsertProfile(created)
        return created
    }

    private suspend fun seedIfNeeded(
        profile: ExerciseProgressionProfile,
        siblingVariantKeys: List<String>
    ): ExerciseProgressionProfile? {
        val needsSeed = profile.state == ProgressionState.CALIBRATING &&
            profile.loadVolumeKg == null &&
            profile.loadStrengthKg == null
        if (!needsSeed) return profile

        val seed = resolveSeedKg(profile.variantKey, siblingVariantKeys) ?: return null
        val seeded = profile.copy(loadVolumeKg = seed, loadStrengthKg = seed)
        progressionRepository.upsertProfile(seeded)
        return seeded
    }

    /**
     * Seed order, most specific first:
     *
     * 1. What the owner has actually lifted on THIS variant. Nothing beats real data on the machine.
     * 2. A sibling variant's current working load. Same movement, so it is the right neighbourhood
     *    even if the plates do not match exactly.
     * 3. A sibling's logged history.
     *
     * Returns null when nothing is known, and the exercise simply shows no prescription yet. That is
     * a normal state, not a failure.
     */
    private suspend fun resolveSeedKg(
        variantKey: String,
        siblingVariantKeys: List<String>
    ): Double? {
        loadHistory.heaviestLoggedKg(variantKey)?.takeIf { it > 0.0 }?.let { return it }

        val siblings = siblingVariantKeys.filter { it != variantKey }
        siblings.firstNotNullOfOrNull { sibling ->
            progressionRepository.getProfile(sibling)
                ?.let { it.loadStrengthKg ?: it.loadVolumeKg }
                ?.takeIf { it > 0.0 }
        }?.let { return it }

        return siblings.firstNotNullOfOrNull { sibling ->
            loadHistory.heaviestLoggedKg(sibling)?.takeIf { it > 0.0 }
        }
    }
}
