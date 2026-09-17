package com.alvarocervantes.fittrackplus.domain.model.progression

data class ProgressionPrescription(
    val type: ExposureType,
    val prescribedLoadKg: Double,
    val prescribedRepMin: Int,
    val prescribedRepMax: Int,
    val prescribedTargetRir: Int,
    val prescribedSets: Int,
    val reason: ProgressionReason,
    val displayedE1rm: Double?,
    val nextProfile: ExerciseProgressionProfile
)
