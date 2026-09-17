package com.alvarocervantes.fittrackplus.domain.model.progression

data class StrengthEstimate(
    val e1rmKg: Double,
    val confidence: E1rmConfidence
)

enum class E1rmConfidence {
    HIGH,
    MEDIUM
}

fun calculateStrengthEstimate(
    loadKg: Double,
    reps: Int,
    rir: Int?
): StrengthEstimate? {
    if (loadKg <= 0.0 || reps <= 0) {
        return null
    }
    if (rir == null || rir < 0) {
        return null
    }

    val effectiveReps = reps + rir
    val confidence = when {
        effectiveReps <= ProgressionTuning.CONF_HIGH_MAX_EFFECTIVE_REPS -> E1rmConfidence.HIGH
        effectiveReps <= ProgressionTuning.CONF_MEDIUM_MAX_EFFECTIVE_REPS -> E1rmConfidence.MEDIUM
        else -> return null
    }

    // Keep Epley so existing historical curves are not retroactively rewritten.
    val e1rmKg = loadKg * (1.0 + effectiveReps / ProgressionTuning.EPLEY_REP_DIVISOR)
    return StrengthEstimate(e1rmKg = e1rmKg, confidence = confidence)
}
