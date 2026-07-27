package com.alvarocervantes.fittrackplus.domain.model

data class TargetRepsRange(
    val min: Int,
    val max: Int
) {
    companion object {
        private val rangePattern = Regex("""^(\d{1,2})\s*-\s*(\d{1,2})$""")

        fun parse(text: String?): TargetRepsRange? {
            val normalized = text?.trim() ?: return null
            val exactReps = normalized.toIntOrNull()
                ?.takeIf { reps -> reps in VALID_REPS_RANGE }
            if (exactReps != null) {
                return TargetRepsRange(min = exactReps, max = exactReps)
            }

            val rangeMatch = if (normalized.isEmpty()) {
                null
            } else {
                rangePattern.matchEntire(normalized)
            }
            return rangeMatch
                ?.let { match ->
                    val minReps = match.groupValues[1].toInt()
                    val maxReps = match.groupValues[2].toInt()
                    if (
                        minReps in VALID_REPS_RANGE &&
                        maxReps in VALID_REPS_RANGE &&
                        minReps <= maxReps
                    ) {
                        TargetRepsRange(min = minReps, max = maxReps)
                    } else {
                        null
                    }
                }
        }

        private val VALID_REPS_RANGE = 1..99
    }
}
