package com.alvarocervantes.fittrackplus.domain.usecase

import com.alvarocervantes.fittrackplus.data.repository.WorkoutRepository
import com.alvarocervantes.fittrackplus.domain.model.ProgressionHint
import com.alvarocervantes.fittrackplus.domain.model.TargetRepsRange
import javax.inject.Inject

class GetProgressionHintUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {

    suspend operator fun invoke(
        variantKey: String,
        targetRepsText: String
    ): ProgressionHint {
        val targetRange = TargetRepsRange.parse(targetRepsText) ?: return ProgressionHint.NONE
        val recentAverages = workoutRepository.getRecentAverageRepsForExercise(
            variantKey = variantKey,
            limit = RECENT_SESSION_LIMIT
        )
        if (recentAverages.size < REQUIRED_SESSION_COUNT) return ProgressionHint.NONE

        val aboveTopRangeCount = recentAverages.count { averageReps -> averageReps > targetRange.max }
        if (aboveTopRangeCount >= REQUIRED_SESSION_COUNT) {
            return ProgressionHint.UP
        }

        val belowBottomRangeCount = recentAverages.count { averageReps -> averageReps < targetRange.min }
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
    return TargetRepsRange.parse(targetRepsText)?.let { range -> range.min..range.max }
}
