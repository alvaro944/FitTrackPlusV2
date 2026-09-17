package com.alvarocervantes.fittrackplus.domain.model.progression

/**
 * Load adjustment applied to the sets that follow the probe, expressed in whole load increments.
 *
 * The probe set always runs at the prescribed load: it is the only measurement taken without
 * accumulated fatigue, and it is the one the next exposure's decision reads.
 *
 * ANTI-DOUBLE-COUNTING, and this is the one rule that can break the whole engine: the next
 * exposure's decision uses `probeSurplus` and nothing else. It must never read the adjusted sets.
 * Counting both makes the engine amplify its own correction and oscillate.
 */
fun intraSessionAdjustmentSteps(
    probeSurplus: Int?,
    state: ProgressionState,
    exposureType: ExposureType
): Int {
    if (probeSurplus == null) return 0
    if (!allowsIntraSessionAdjustment(state, exposureType)) return 0

    val steps = when {
        probeSurplus >= ProgressionTuning.SURPLUS_EASY_MIN -> ProgressionTuning.INTRA_MAX_STEPS
        probeSurplus == ProgressionTuning.SURPLUS_STRONG -> 1
        probeSurplus <= ProgressionTuning.INTRA_REDUCE_SURPLUS_MAX -> -1
        else -> 0
    }
    return steps.coerceIn(-ProgressionTuning.INTRA_MAX_STEPS, ProgressionTuning.INTRA_MAX_STEPS)
}

/**
 * Calibration needs an untouched reference, and recovery and evaluation exist precisely to hold a
 * fixed load, so none of them may be adjusted mid-session.
 */
fun allowsIntraSessionAdjustment(state: ProgressionState, exposureType: ExposureType): Boolean {
    if (state != ProgressionState.PROGRESSING) return false
    return exposureType == ExposureType.VOLUME || exposureType == ExposureType.STRENGTH
}

/** Load proposed for the sets after the probe. The user may ignore it: what they log is what is saved. */
fun intraSessionSuggestedLoadKg(
    prescribedLoadKg: Double,
    loadIncrementKg: Double,
    steps: Int
): Double {
    if (steps == 0) return prescribedLoadKg
    val suggested = prescribedLoadKg + steps * loadIncrementKg
    return if (suggested > 0.0) suggested else prescribedLoadKg
}
