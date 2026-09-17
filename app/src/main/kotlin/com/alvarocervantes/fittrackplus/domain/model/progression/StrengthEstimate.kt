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
        effectiveReps <= HIGH_CONFIDENCE_MAX_EFFECTIVE_REPS -> E1rmConfidence.HIGH
        effectiveReps <= MEDIUM_CONFIDENCE_MAX_EFFECTIVE_REPS -> E1rmConfidence.MEDIUM
        else -> return null
    }

    // Keep Epley so existing historical curves are not retroactively rewritten.
    val e1rmKg = loadKg * (1.0 + effectiveReps / EPLEY_REP_DIVISOR)
    return StrengthEstimate(e1rmKg = e1rmKg, confidence = confidence)
}

private const val HIGH_CONFIDENCE_MAX_EFFECTIVE_REPS = 6
private const val MEDIUM_CONFIDENCE_MAX_EFFECTIVE_REPS = 10
private const val EPLEY_REP_DIVISOR = 30.0
