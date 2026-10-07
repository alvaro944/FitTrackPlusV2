package com.alvarocervantes.fittrackplus.domain.model.progression

fun calculateSurplus(
    probeReps: Int,
    probeRir: Int?,
    prescribedRepMin: Int,
    prescribedRepMax: Int,
    prescribedTargetRir: Int
): Int? {
    return probeRir?.let { rir ->
        val repMidpoint = (prescribedRepMin + prescribedRepMax) / 2
        (probeReps + rir) - (repMidpoint + prescribedTargetRir)
    }
}

fun classifyExposure(
    probeReps: Int,
    probeRir: Int?,
    prescribedRepMin: Int,
    prescribedRepMax: Int,
    prescribedTargetRir: Int,
    completedSetCount: Int,
    prescribedSets: Int
): ExposureClassification {
    require(prescribedSets > 0) { "Prescribed sets must be positive" }

    val surplus = calculateSurplus(
        probeReps = probeReps,
        probeRir = probeRir,
        prescribedRepMin = prescribedRepMin,
        prescribedRepMax = prescribedRepMax,
        prescribedTargetRir = prescribedTargetRir
    )
    val setCompletionRatio = completedSetCount.toDouble() / prescribedSets
    var outcomeClass = surplus?.toOutcomeClass() ?: OutcomeClass.ON_TARGET

    if (probeReps < prescribedRepMin) {
        outcomeClass = outcomeClass.noBetterThanHard()
    }
    if (probeReps < prescribedRepMin - 1) {
        outcomeClass = OutcomeClass.FAILED
    }
    if (setCompletionRatio < ProgressionTuning.SET_COMPLETION_RATIO_MIN) {
        outcomeClass = outcomeClass.downgrade()
    }

    val excludeFromTrend = probeRir == null
    if (excludeFromTrend) {
        outcomeClass = outcomeClass.downgrade()
    }

    return ExposureClassification(
        surplus = surplus,
        outcomeClass = outcomeClass,
        setCompletionRatio = setCompletionRatio,
        excludeFromTrend = excludeFromTrend
    )
}

fun updateE1rmEwmas(
    profile: ExerciseProgressionProfile,
    exposure: ProgressionExposure
): ExerciseProgressionProfile {
    val exposureE1rm = exposure.exposureE1rm ?: return profile
    val confidence = exposure.e1rmConfidence ?: return profile
    if (exposure.excludeFromTrend || exposure.userFlaggedBadDay) {
        return profile
    }

    // Epley reads VOLUME systematically higher than STRENGTH; mixing them creates a sawtooth
    // that triggers phantom recoveries, so each exposure lane maintains its own EWMA.
    return when (exposure.type) {
        ExposureType.STRENGTH -> profile.copy(
            ewmaStrengthE1rm = profile.ewmaStrengthE1rm.nextEwma(exposureE1rm, confidence),
            strengthPointCount = profile.strengthPointCount + 1
        )
        ExposureType.VOLUME -> profile.copy(
            ewmaVolumeE1rm = profile.ewmaVolumeE1rm.nextEwma(exposureE1rm, confidence),
            volumePointCount = profile.volumePointCount + 1
        )
        ExposureType.RECOVERY,
        ExposureType.EVALUATION -> profile
    }
}

/**
 * Calculates the trend from the complete, unwindowed history that produced [profile]'s EWMA
 * point counts. A truncated history is rejected rather than silently returning [StrengthTrend.UNKNOWN].
 */
fun calculateStrengthTrend(
    profile: ExerciseProgressionProfile,
    recentExposures: List<ProgressionExposure>
): StrengthTrend {
    requireLaneHistoryIsComplete(profile, recentExposures, ExposureType.STRENGTH)
    requireLaneHistoryIsComplete(profile, recentExposures, ExposureType.VOLUME)

    return calculateLaneTrend(recentExposures, ExposureType.STRENGTH)
        ?: calculateLaneTrend(recentExposures, ExposureType.VOLUME)
        ?: StrengthTrend.UNKNOWN
}

private fun requireLaneHistoryIsComplete(
    profile: ExerciseProgressionProfile,
    recentExposures: List<ProgressionExposure>,
    type: ExposureType
) {
    val actualPointCount = recentExposures.count {
        it.type == type &&
            !it.excludeFromTrend &&
            !it.userFlaggedBadDay &&
            it.exposureE1rm != null &&
            it.e1rmConfidence != null
    }
    val expectedPointCount = when (type) {
        ExposureType.STRENGTH -> profile.strengthPointCount
        ExposureType.VOLUME -> profile.volumePointCount
        ExposureType.RECOVERY,
        ExposureType.EVALUATION -> return
    }

    require(actualPointCount == expectedPointCount) {
        "Strength trend requires complete $type exposure history"
    }
}

private fun Int.toOutcomeClass(): OutcomeClass {
    return when {
        this >= ProgressionTuning.SURPLUS_EASY_MIN -> OutcomeClass.EASY
        this == ProgressionTuning.SURPLUS_STRONG -> OutcomeClass.STRONG
        this >= ProgressionTuning.SURPLUS_ON_TARGET_MIN -> OutcomeClass.ON_TARGET
        this == ProgressionTuning.SURPLUS_HARD -> OutcomeClass.HARD
        else -> OutcomeClass.FAILED
    }
}

private fun OutcomeClass.noBetterThanHard(): OutcomeClass {
    return when (this) {
        OutcomeClass.EASY,
        OutcomeClass.STRONG,
        OutcomeClass.ON_TARGET -> OutcomeClass.HARD
        OutcomeClass.HARD,
        OutcomeClass.FAILED -> this
    }
}

private fun OutcomeClass.downgrade(): OutcomeClass {
    return when (this) {
        OutcomeClass.EASY -> OutcomeClass.STRONG
        OutcomeClass.STRONG -> OutcomeClass.ON_TARGET
        OutcomeClass.ON_TARGET -> OutcomeClass.HARD
        OutcomeClass.HARD,
        OutcomeClass.FAILED -> OutcomeClass.FAILED
    }
}

private fun Double?.nextEwma(
    exposureE1rm: Double,
    confidence: E1rmConfidence
): Double {
    val previousEwma = this ?: return exposureE1rm
    return previousEwma + ProgressionTuning.EWMA_ALPHA * confidence.weight() *
        (exposureE1rm - previousEwma)
}

private fun E1rmConfidence.weight(): Double {
    return when (this) {
        E1rmConfidence.HIGH -> ProgressionTuning.WEIGHT_HIGH
        E1rmConfidence.MEDIUM -> ProgressionTuning.WEIGHT_MEDIUM
    }
}

private fun calculateLaneTrend(
    recentExposures: List<ProgressionExposure>,
    type: ExposureType
): StrengthTrend? {
    val ewmAs = recentExposures
        .asSequence()
        .filter { it.type == type }
        .filter { !it.excludeFromTrend && !it.userFlaggedBadDay }
        .filter { it.exposureE1rm != null && it.e1rmConfidence != null }
        .sortedBy { it.exposureIndex }
        .fold(mutableListOf<Double>()) { ewmAs, exposure ->
            val nextEwma = ewmAs.lastOrNull().nextEwma(
                exposureE1rm = requireNotNull(exposure.exposureE1rm),
                confidence = requireNotNull(exposure.e1rmConfidence)
            )
            ewmAs += nextEwma
            ewmAs
        }

    if (ewmAs.size < ProgressionTuning.TREND_MIN_POINTS) {
        return null
    }

    val currentEwma = ewmAs.last()
    val comparisonEwma = ewmAs[ewmAs.lastIndex - ProgressionTuning.TREND_LOOKBACK]
    val deltaPercent = (currentEwma - comparisonEwma) / comparisonEwma * 100

    return when {
        deltaPercent >= ProgressionTuning.TREND_RISING_PCT -> StrengthTrend.RISING
        deltaPercent <= ProgressionTuning.TREND_FALLING_PCT -> StrengthTrend.FALLING
        else -> StrengthTrend.FLAT
    }
}
