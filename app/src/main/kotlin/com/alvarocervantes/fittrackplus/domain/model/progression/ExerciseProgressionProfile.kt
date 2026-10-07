package com.alvarocervantes.fittrackplus.domain.model.progression

data class ExerciseProgressionProfile(
    val variantKey: String,
    val goalWeightKg: Double? = null,
    val loadIncrementKg: Double = ProgressionTuning.DEFAULT_INCREMENT_UPPER_KG,
    val state: ProgressionState = ProgressionState.CALIBRATING,
    val loadVolumeKg: Double? = null,
    val loadStrengthKg: Double? = null,
    val pendingConfirmVolume: Boolean = false,
    val pendingConfirmStrength: Boolean = false,
    val nextExposureType: ExposureType = ExposureType.VOLUME,
    val hardExposureStreak: Int = 0,
    val exposuresSinceRecovery: Int = 0,
    val consecutiveFailVolume: Int = 0,
    val consecutiveFailStrength: Int = 0,
    val previousLoadVolumeKg: Double? = null,
    val previousLoadStrengthKg: Double? = null,
    val stallCount: Int = 0,
    val ewmaStrengthE1rm: Double? = null,
    val ewmaVolumeE1rm: Double? = null,
    val strengthPointCount: Int = 0,
    val volumePointCount: Int = 0,
    val recoveryExposuresRemaining: Int = 0,
    val evaluationFailStreak: Int = 0,
    val calibrationRetries: Int = 0
) {
    val isConsolidating: Boolean
        get() = pendingConfirmVolume || pendingConfirmStrength
}
