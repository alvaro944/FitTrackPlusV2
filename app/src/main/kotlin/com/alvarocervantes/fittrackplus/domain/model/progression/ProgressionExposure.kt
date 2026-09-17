package com.alvarocervantes.fittrackplus.domain.model.progression

data class ProgressionExposure(
    val id: Long = 0L,
    val variantKey: String,
    val workoutExerciseId: Long?,
    val exposureIndex: Int,
    val performedAt: Long,
    val type: ExposureType,
    val prescribedLoadKg: Double,
    val prescribedRepMin: Int,
    val prescribedRepMax: Int,
    val prescribedTargetRir: Int,
    val prescribedSets: Int,
    val probeReps: Int?,
    val probeRir: Int?,
    val probeSurplus: Int?,
    val outcomeClass: OutcomeClass?,
    val setCompletionRatio: Double?,
    val exposureE1rm: Double?,
    val e1rmConfidence: E1rmConfidence?,
    val excludeFromTrend: Boolean,
    val userFlaggedBadDay: Boolean,
    val intraSessionAdjustmentSteps: Int,
    val decisionReason: String
)
