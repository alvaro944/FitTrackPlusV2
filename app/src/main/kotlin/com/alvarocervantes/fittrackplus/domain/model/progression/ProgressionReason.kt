package com.alvarocervantes.fittrackplus.domain.model.progression

/**
 * Why the engine decided what it decided.
 *
 * The domain says WHAT it decided; the UI decides HOW to say it. Building the sentence here would
 * put user-facing copy in the decision layer, which made the engine speak English inside a Spanish
 * app and left the explanation untranslatable. Same reasoning as [LoadDecision].
 *
 * Persisted by name in `progression_exposures.decisionReason`, so an exposure keeps its reason as a
 * stable code rather than as prose that a later wording change would silently rewrite.
 */
enum class ProgressionReason {
    CALIBRATION_COMPLETE,
    CALIBRATION_RETRY,
    CALIBRATION_BASELINE,
    PROGRESSION_ALTERNATING,
    BAD_DAY,
    RECOVERY_EXPOSURE,
    RECOVERY_COMPLETE,
    RECOVERY_CONTINUE,
    EVALUATION_EXPOSURE,
    EVALUATION_PASSED,
    EVALUATION_HARD,
    EVALUATION_FAILED_TWICE,
    RECOVERY_HARD_STREAK,
    RECOVERY_FALLING_TREND,
    RECOVERY_LOW_SURPLUS,
    RECOVERY_REPEATED_STALLS,
    RECOVERY_REPEATED_HARD,
    LOAD_INCREASED_EASY,
    LOAD_INCREASED_STRONG,
    LOAD_CONFIRMED,
    LOAD_HELD_HARD,
    LOAD_REVERTED,
    LOAD_HELD_ONE_FAILURE,
    LOAD_HELD_FAILURE,
    LOAD_REDUCED_REPEATED,
    LOAD_UNCHANGED_FAILURE,
    LOAD_UNCHANGED_NO_OUTCOME;

    companion object {
        /**
         * Unknown or legacy values fall back to [LOAD_UNCHANGED_NO_OUTCOME]. Exposures written
         * before this enum existed hold English prose, and an unreadable reason must not stop the
         * history from loading.
         */
        fun fromStoredName(stored: String?): ProgressionReason {
            return entries.firstOrNull { it.name == stored } ?: LOAD_UNCHANGED_NO_OUTCOME
        }
    }
}
