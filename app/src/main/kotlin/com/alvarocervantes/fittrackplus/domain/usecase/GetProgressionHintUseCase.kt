package com.alvarocervantes.fittrackplus.domain.usecase

import com.alvarocervantes.fittrackplus.data.repository.WorkoutRepository
import com.alvarocervantes.fittrackplus.domain.model.ProgressionHint
import javax.inject.Inject

class GetProgressionHintUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {

    suspend operator fun invoke(
        variantKey: String,
        targetRepsText: String
    ): ProgressionHint {
        val targetRange = parseProgressionTargetRange(targetRepsText) ?: return ProgressionHint.NONE
        val recentAverages = workoutRepository.getRecentAverageRepsForExercise(
            variantKey = variantKey,
            limit = RECENT_SESSION_LIMIT
        )
        if (recentAverages.size < REQUIRED_SESSION_COUNT) return ProgressionHint.NONE

        val aboveTopRangeCount = recentAverages.count { averageReps -> averageReps > targetRange.last }
        if (aboveTopRangeCount >= REQUIRED_SESSION_COUNT) {
            return ProgressionHint.UP
        }

        val belowBottomRangeCount = recentAverages.count { averageReps -> averageReps < targetRange.first }
        if (belowBottomRangeCount >= REQUIRED_SESSION_COUNT) {
            return ProgressionHint.DOWN
        }

        return ProgressionHint.NONE
    }

    private companion object {
        const val RECENT_SESSION_LIMIT = 3
        const val REQUIRED_SESSION_COUNT = 2
    }
}

internal fun parseProgressionTargetRange(targetRepsText: String): IntRange? {
    val normalized = targetRepsText.trim()
    val exactRange = normalized.toIntOrNull()
        ?.takeIf { reps -> reps in 1..99 }
        ?.let { reps -> reps..reps }
    if (exactRange != null) {
        return exactRange
    }

    val rangeMatch = if (normalized.isEmpty()) {
        null
    } else {
        Regex("""^(\d{1,2})\s*-\s*(\d{1,2})$""").matchEntire(normalized)
    }
    return rangeMatch
        ?.let { match ->
            val minReps = match.groupValues[1].toInt()
            val maxReps = match.groupValues[2].toInt()
            if (minReps in 1..99 && maxReps in 1..99 && minReps <= maxReps) {
                minReps..maxReps
            } else {
                null
            }
        }
}
