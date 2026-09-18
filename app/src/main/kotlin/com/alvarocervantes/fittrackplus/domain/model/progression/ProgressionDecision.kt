@file:Suppress("TooManyFunctions")

package com.alvarocervantes.fittrackplus.domain.model.progression

import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.round

private data class ProfileResolution(
    val profile: ExerciseProgressionProfile,
    val reason: ProgressionReason
)

private data class ExposureParameters(
    val repMin: Int,
    val repMax: Int,
    val targetRir: Int
)

private data class ProgressingOutcome(
    val profile: ExerciseProgressionProfile,
    val loadDecision: LoadDecision
)

private enum class LoadDecision {
    INCREASED,
    HELD,
    REDUCED,
    REVERTED,
    UNCHANGED
}

/**
 * Returns null when calibration has no starting load yet.
 *
 * A freshly marked PRIMARY exercise with no history has nothing to calibrate from, and that is an
 * ordinary state, not a programming error. Throwing here crashed the workout screen the first time
 * an exercise was promoted, so the absence is now part of the contract: the caller seeds the profile
 * or shows nothing.
 */
fun calculateNextPrescription(
    profile: ExerciseProgressionProfile,
    recentExposures: List<ProgressionExposure>
): ProgressionPrescription? {
    if (profile.state == ProgressionState.CALIBRATING &&
        profile.loadVolumeKg == null &&
        profile.loadStrengthKg == null
    ) {
        return null
    }
    val exposures = recentExposures.sortedBy(ProgressionExposure::exposureIndex)
    val lastExposure = exposures.lastOrNull()
    val measuredProfile = lastExposure?.let { updateE1rmEwmas(profile, it) } ?: profile
    val resolution = when (measuredProfile.state) {
        ProgressionState.CALIBRATING -> resolveCalibration(measuredProfile, exposures, lastExposure)
        ProgressionState.PROGRESSING -> resolveProgressing(measuredProfile, exposures, lastExposure)
        ProgressionState.RECOVERING -> resolveRecovering(measuredProfile, lastExposure)
        ProgressionState.EVALUATING -> resolveEvaluating(measuredProfile, lastExposure)
    }

    return prescriptionFor(resolution, exposures)
}

// The intra-session rule lives in IntraSessionAdjustment.kt. One rule, one place: two copies of it
// would drift, and this one decides loads.

@Suppress("CyclomaticComplexMethod")
private fun resolveCalibration(
    profile: ExerciseProgressionProfile,
    exposures: List<ProgressionExposure>,
    lastExposure: ProgressionExposure?
): ProfileResolution {
    val calibrationExposures = exposures.filter { it.type.isCalibrationType() }
    val validExposures = calibrationExposures.filter { it.outcomeClass != OutcomeClass.FAILED }
    val hasBothTypes = validExposures.any { it.type == ExposureType.VOLUME } &&
        validExposures.any { it.type == ExposureType.STRENGTH }
    val hasEstimate = validExposures.any { it.exposureE1rm != null }

    if (validExposures.size >= ProgressionTuning.CALIBRATION_EXPOSURES && hasBothTypes && hasEstimate) {
        val nextProfile = profile.copy(
            state = ProgressionState.PROGRESSING,
            loadVolumeKg = profile.ewmaVolumeE1rm ?: profile.loadVolumeKg ?: calibrationSeed(profile),
            loadStrengthKg = profile.ewmaStrengthE1rm ?: profile.loadStrengthKg ?: calibrationSeed(profile),
            nextExposureType = ExposureType.VOLUME,
            calibrationRetries = 0
        )
        return ProfileResolution(nextProfile, ProgressionReason.CALIBRATION_COMPLETE)
    }

    if (lastExposure?.outcomeClass == OutcomeClass.FAILED &&
        lastExposure.type.isCalibrationType() &&
        profile.calibrationRetries < ProgressionTuning.CALIBRATION_MAX_RETRIES
    ) {
        val retryLoad = lastExposure.prescribedLoadKg * ProgressionTuning.CALIBRATION_FAIL_FACTOR
        val nextProfile = profile.withLaneLoad(lastExposure.type, retryLoad).copy(
            nextExposureType = lastExposure.type,
            calibrationRetries = profile.calibrationRetries + 1
        )
        return ProfileResolution(nextProfile, ProgressionReason.CALIBRATION_RETRY)
    }

    val nextType = when (validExposures.size) {
        0 -> ExposureType.VOLUME
        1 -> ExposureType.STRENGTH
        else -> ExposureType.VOLUME
    }
    return ProfileResolution(
        profile.copy(nextExposureType = nextType),
        ProgressionReason.CALIBRATION_BASELINE
    )
}

private fun resolveProgressing(
    profile: ExerciseProgressionProfile,
    exposures: List<ProgressionExposure>,
    lastExposure: ProgressionExposure?
): ProfileResolution {
    if (lastExposure == null || !lastExposure.type.isProgressionType()) {
        return ProfileResolution(profile, ProgressionReason.PROGRESSION_ALTERNATING)
    }

    val progressingOutcome = if (lastExposure.userFlaggedBadDay) {
        ProgressingOutcome(
            profile.copy(
                exposuresSinceRecovery = profile.exposuresSinceRecovery + 1,
                nextExposureType = lastExposure.type.oppositeLane()
            ),
            LoadDecision.UNCHANGED
        )
    } else {
        applyProgressingOutcome(profile, exposures, lastExposure)
    }
    val afterOutcome = progressingOutcome.profile
    val profileWithExposure = afterOutcome.copy(
        exposuresSinceRecovery = if (lastExposure.userFlaggedBadDay) {
            afterOutcome.exposuresSinceRecovery
        } else {
            afterOutcome.exposuresSinceRecovery + 1
        },
        hardExposureStreak = if (lastExposure.userFlaggedBadDay) {
            afterOutcome.hardExposureStreak
        } else {
            afterOutcome.hardExposureStreakFor(lastExposure.outcomeClass)
        }
    )
    val trigger = recoveryTrigger(profileWithExposure, exposures)
    if (trigger != null) {
        return ProfileResolution(
            profileWithExposure.copy(
                state = ProgressionState.RECOVERING,
                recoveryExposuresRemaining = trigger.exposures
            ),
            trigger.reason
        )
    }

    val reason = if (lastExposure.userFlaggedBadDay) {
        ProgressionReason.BAD_DAY
    } else {
        outcomeReason(
            lastExposure.outcomeClass,
            lastExposure.type,
            progressingOutcome.loadDecision,
            profileWithExposure
        )
    }
    return ProfileResolution(profileWithExposure, reason)
}

@Suppress("LongMethod", "CyclomaticComplexMethod")
private fun applyProgressingOutcome(
    profile: ExerciseProgressionProfile,
    exposures: List<ProgressionExposure>,
    lastExposure: ProgressionExposure
): ProgressingOutcome {
    val type = lastExposure.type
    val currentLoad = profile.laneLoad(type) ?: lastExposure.prescribedLoadKg
    val pendingConfirm = profile.lanePendingConfirm(type)
    val consecutiveFail = profile.laneConsecutiveFail(type)
    val previousLoad = profile.lanePreviousLoad(type)
    val nextType = type.oppositeLane()
    val previousSameLaneOutcome = exposures.dropLast(1)
        .lastOrNull { it.type == type && !it.userFlaggedBadDay }
        ?.outcomeClass
    val immediatelyPriorFailedCount = if (previousSameLaneOutcome == OutcomeClass.FAILED) {
        ProgressionTuning.FAILS_TO_REVERT
    } else {
        0
    }

    return when (lastExposure.outcomeClass) {
        OutcomeClass.EASY -> {
            val previousWasEasy = exposures.dropLast(1)
                .lastOrNull { it.type == type && !it.userFlaggedBadDay }
                ?.outcomeClass == OutcomeClass.EASY
            val steps = if (previousWasEasy) {
                ProgressionTuning.STEP_STRONG
            } else {
                ProgressionTuning.STEP_EASY
            }
            val increasedLoad = if (previousWasEasy) {
                currentLoad + steps * profile.loadIncrementKg
            } else {
                minOf(
                    currentLoad + steps * profile.loadIncrementKg,
                    currentLoad * (1 + ProgressionTuning.MAX_SINGLE_JUMP_PCT / ProgressionTuning.PERCENT_BASE)
                )
            }
            ProgressingOutcome(
                profile.withLaneDecision(
                    type = type,
                    load = increasedLoad,
                    previousLoad = currentLoad,
                    pendingConfirm = true,
                    consecutiveFail = 0,
                    nextType = nextType
                ),
                LoadDecision.INCREASED
            )
        }
        OutcomeClass.STRONG -> ProgressingOutcome(
            profile.withLaneDecision(
                type = type,
                load = currentLoad + ProgressionTuning.STEP_STRONG * profile.loadIncrementKg,
                previousLoad = currentLoad,
                pendingConfirm = true,
                consecutiveFail = 0,
                nextType = nextType
            ),
            LoadDecision.INCREASED
        )
        OutcomeClass.ON_TARGET -> ProgressingOutcome(
            profile.withLaneDecision(
                type = type,
                load = currentLoad,
                previousLoad = previousLoad,
                pendingConfirm = false,
                consecutiveFail = 0,
                nextType = nextType
            ),
            LoadDecision.HELD
        )
        OutcomeClass.HARD -> ProgressingOutcome(
            profile.withLaneDecision(
                type = type,
                load = currentLoad,
                previousLoad = previousLoad,
                pendingConfirm = pendingConfirm,
                consecutiveFail = if (pendingConfirm) consecutiveFail else consecutiveFail + 1,
                nextType = nextType
            ),
            LoadDecision.HELD
        )
        OutcomeClass.FAILED -> when {
            pendingConfirm -> ProgressingOutcome(
                profile.withLaneDecision(
                    type = type,
                    load = previousLoad ?: currentLoad,
                    previousLoad = previousLoad,
                    pendingConfirm = false,
                    consecutiveFail = 0,
                    nextType = nextType
                ).copy(stallCount = profile.stallCount + 1),
                LoadDecision.REVERTED
            )
            immediatelyPriorFailedCount + 1 >= ProgressionTuning.FAILS_TO_REDUCE -> ProgressingOutcome(
                profile.withLaneDecision(
                    type = type,
                    load = currentLoad + ProgressionTuning.STEP_REDUCE * profile.loadIncrementKg,
                    previousLoad = previousLoad,
                    pendingConfirm = false,
                    consecutiveFail = 0,
                    nextType = nextType
                ).copy(stallCount = profile.stallCount + 1),
                LoadDecision.REDUCED
            )
            else -> ProgressingOutcome(
                profile.withLaneDecision(
                    type = type,
                    load = currentLoad,
                    previousLoad = previousLoad,
                    pendingConfirm = false,
                    consecutiveFail = consecutiveFail + 1,
                    nextType = nextType
                ),
                LoadDecision.HELD
            )
        }
        null -> ProgressingOutcome(profile.copy(nextExposureType = nextType), LoadDecision.UNCHANGED)
    }
}

private fun resolveRecovering(
    profile: ExerciseProgressionProfile,
    lastExposure: ProgressionExposure?
): ProfileResolution {
    if (lastExposure?.type != ExposureType.RECOVERY) {
        return ProfileResolution(profile, ProgressionReason.RECOVERY_EXPOSURE)
    }

    val remaining = (profile.recoveryExposuresRemaining - 1).coerceAtLeast(0)
    if (remaining == 0) {
        return ProfileResolution(
            profile.copy(
                state = ProgressionState.EVALUATING,
                recoveryExposuresRemaining = 0
            ),
            ProgressionReason.RECOVERY_COMPLETE
        )
    }
    return ProfileResolution(
        profile.copy(
            recoveryExposuresRemaining = remaining,
            nextExposureType = profile.nextExposureType.oppositeLane()
        ),
        ProgressionReason.RECOVERY_CONTINUE
    )
}

private fun resolveEvaluating(
    profile: ExerciseProgressionProfile,
    lastExposure: ProgressionExposure?
): ProfileResolution {
    if (lastExposure?.type != ExposureType.EVALUATION) {
        return ProfileResolution(profile, ProgressionReason.EVALUATION_EXPOSURE)
    }

    val passed = lastExposure.probeSurplus?.let {
        it >= ProgressionTuning.EVALUATION_PASS_SURPLUS
    } ?: false
    if (passed) {
        return ProfileResolution(
            profile.copy(
                state = ProgressionState.PROGRESSING,
                hardExposureStreak = 0,
                stallCount = 0,
                evaluationFailStreak = 0,
                exposuresSinceRecovery = 0
            ),
            ProgressionReason.EVALUATION_PASSED
        )
    }
    if (profile.evaluationFailStreak == 0) {
        return ProfileResolution(
            profile.copy(
                state = ProgressionState.PROGRESSING,
                loadVolumeKg = profile.loadVolumeKg?.demote(profile.loadIncrementKg),
                loadStrengthKg = profile.loadStrengthKg?.demote(profile.loadIncrementKg),
                hardExposureStreak = 0,
                stallCount = 0,
                evaluationFailStreak = 1,
                exposuresSinceRecovery = 0
            ),
            ProgressionReason.EVALUATION_HARD
        )
    }

    val recalibrationLoad = requireNotNull(profile.loadStrengthKg) {
        "Evaluating requires a strength lane load"
    } * ProgressionTuning.EVALUATION_RECALIBRATE_FACTOR
    return ProfileResolution(
        profile.copy(
            state = ProgressionState.CALIBRATING,
            loadVolumeKg = recalibrationLoad,
            loadStrengthKg = recalibrationLoad,
            hardExposureStreak = 0,
            stallCount = 0,
            evaluationFailStreak = 0,
            exposuresSinceRecovery = 0,
            calibrationRetries = 0,
            nextExposureType = ExposureType.VOLUME
        ),
        ProgressionReason.EVALUATION_FAILED_TWICE
    )
}

private fun prescriptionFor(
    resolution: ProfileResolution,
    exposures: List<ProgressionExposure>
): ProgressionPrescription {
    val profile = resolution.profile
    val parts = when (profile.state) {
        ProgressionState.CALIBRATING -> calibrationPrescription(profile, exposures)
        ProgressionState.PROGRESSING -> progressingPrescription(profile)
        ProgressionState.RECOVERING -> recoveryPrescription(profile, exposures)
        ProgressionState.EVALUATING -> evaluationPrescription(profile)
    }
    return ProgressionPrescription(
        type = parts.type,
        prescribedLoadKg = parts.load,
        prescribedRepMin = parts.parameters.repMin,
        prescribedRepMax = parts.parameters.repMax,
        prescribedTargetRir = parts.parameters.targetRir,
        prescribedSets = parts.sets,
        reason = resolution.reason,
        displayedE1rm = profile.ewmaStrengthE1rm,
        nextProfile = profile
    )
}

private fun calibrationPrescription(
    profile: ExerciseProgressionProfile,
    exposures: List<ProgressionExposure>
): PrescriptionParts {
    val type = profile.nextExposureType
    val parameters = when (type) {
        ExposureType.VOLUME -> ExposureParameters(
            repMin = ProgressionTuning.CALIBRATION_VOLUME_REPS,
            repMax = ProgressionTuning.CALIBRATION_VOLUME_REPS,
            targetRir = ProgressionTuning.CALIBRATION_TARGET_RIR
        )
        ExposureType.STRENGTH -> ExposureParameters(
            repMin = ProgressionTuning.CALIBRATION_STRENGTH_REPS,
            repMax = ProgressionTuning.CALIBRATION_STRENGTH_REPS,
            targetRir = ProgressionTuning.CALIBRATION_TARGET_RIR
        )
        ExposureType.RECOVERY,
        ExposureType.EVALUATION -> error("Calibration must prescribe a progression lane")
    }
    val lastCalibration = exposures.lastOrNull { it.type.isCalibrationType() }
    val calibrationE1rm = profile.ewmaVolumeE1rm ?: profile.ewmaStrengthE1rm
    val load = when {
        lastCalibration == null -> calibrationSeed(profile)
        // With an e1RM, derive the lane load from it. estimateLoad expects an e1RM and divides it.
        calibrationE1rm != null -> estimateLoad(
            calibrationE1rm,
            parameters.repMin,
            parameters.targetRir,
            profile.loadIncrementKg
        )
        // Without one, steer by what the probe said. Feeding a working load to estimateLoad here
        // divided a load the owner had already lifted, so an easy session lowered the weight:
        // 12.5 -> 10 -> 7.5 -> 5 while every probe reported surplus. Found in the 2026-09-18 pass.
        else -> steerCalibrationLoad(lastCalibration, profile.loadIncrementKg)
    }
    return PrescriptionParts(type, parameters, load, ProgressionTuning.CALIBRATION_EXPOSURES)
}

private fun progressingPrescription(profile: ExerciseProgressionProfile): PrescriptionParts {
    val type = profile.nextExposureType
    val parameters = specificityParameters(profile.specificityTier(), type)
    val load = requireNotNull(profile.laneLoad(type)) { "Progressing requires a $type lane load" }
    return PrescriptionParts(type, parameters, load, ProgressionTuning.CALIBRATION_EXPOSURES)
}

private fun recoveryPrescription(
    profile: ExerciseProgressionProfile,
    exposures: List<ProgressionExposure>
): PrescriptionParts {
    val lane = profile.nextExposureType
    val parameters = specificityParameters(profile.specificityTier(), lane)
    val load = requireNotNull(profile.laneLoad(lane)) { "Recovering requires a $lane lane load" }
    val normalSets = exposures.lastOrNull { it.type.isProgressionType() }?.prescribedSets
        ?: ProgressionTuning.CALIBRATION_EXPOSURES
    val recoverySets = maxOf(
        ProgressionTuning.RECOVERY_MIN_SETS,
        ceil(normalSets * ProgressionTuning.RECOVERY_SETS_FACTOR).toInt()
    )
    return PrescriptionParts(
        ExposureType.RECOVERY,
        parameters.copy(repMax = parameters.repMin, targetRir = ProgressionTuning.RECOVERY_TARGET_RIR),
        roundToIncrement(load * ProgressionTuning.RECOVERY_LOAD_FACTOR, profile.loadIncrementKg),
        recoverySets
    )
}

private fun evaluationPrescription(profile: ExerciseProgressionProfile): PrescriptionParts {
    val parameters = specificityParameters(profile.specificityTier(), ExposureType.STRENGTH)
    val load = requireNotNull(profile.loadStrengthKg) { "Evaluating requires a strength lane load" }
    return PrescriptionParts(
        ExposureType.EVALUATION,
        parameters.copy(targetRir = ProgressionTuning.EVALUATION_TARGET_RIR),
        load,
        ProgressionTuning.EVALUATION_SETS
    )
}

private data class PrescriptionParts(
    val type: ExposureType,
    val parameters: ExposureParameters,
    val load: Double,
    val sets: Int
)

private data class RecoveryTrigger(val exposures: Int, val reason: ProgressionReason)

@Suppress("ReturnCount")
private fun recoveryTrigger(
    profile: ExerciseProgressionProfile,
    exposures: List<ProgressionExposure>
): RecoveryTrigger? {
    if (profile.exposuresSinceRecovery < ProgressionTuning.RECOVERY_COOLDOWN_EXPOSURES) return null

    if (profile.hardExposureStreak >= ProgressionTuning.R1_HARD_STREAK_CAP) {
        return RecoveryTrigger(
            ProgressionTuning.RECOVERY_HARD_TRIGGER_EXPOSURES,
            ProgressionReason.RECOVERY_HARD_STREAK
        )
    }
    val trend = calculateStrengthTrend(profile, exposures)
    val recentJudged = exposures.filterNot(ProgressionExposure::userFlaggedBadDay)
    val badCount = recentJudged.takeLast(ProgressionTuning.R2_BAD_IN_LAST_N)
        .count { it.outcomeClass == OutcomeClass.HARD || it.outcomeClass == OutcomeClass.FAILED }
    if (trend == StrengthTrend.FALLING && badCount >= ProgressionTuning.R2_BAD_REQUIRED) {
        return RecoveryTrigger(
            ProgressionTuning.RECOVERY_HARD_TRIGGER_EXPOSURES,
            ProgressionReason.RECOVERY_FALLING_TREND
        )
    }
    val recentSurpluses = recentJudged.mapNotNull(ProgressionExposure::probeSurplus)
        .takeLast(ProgressionTuning.R2_BAD_IN_LAST_N)
    if (recentSurpluses.size == ProgressionTuning.R2_BAD_IN_LAST_N &&
        recentSurpluses.average() <= ProgressionTuning.R3_MEAN_SURPLUS_MAX &&
        trend != StrengthTrend.RISING
    ) {
        return RecoveryTrigger(ProgressionTuning.STEP_STRONG, ProgressionReason.RECOVERY_LOW_SURPLUS)
    }
    if (profile.stallCount >= ProgressionTuning.R4_STALL_COUNT) {
        return RecoveryTrigger(ProgressionTuning.STEP_STRONG, ProgressionReason.RECOVERY_REPEATED_STALLS)
    }
    if (profile.hardExposureStreak >= ProgressionTuning.R5_HARD_STREAK) {
        return RecoveryTrigger(ProgressionTuning.STEP_STRONG, ProgressionReason.RECOVERY_REPEATED_HARD)
    }
    return null
}

private fun ExerciseProgressionProfile.specificityTier(): SpecificityTier {
    val goal = goalWeightKg ?: return SpecificityTier.BASE
    val strengthEstimate = ewmaStrengthE1rm ?: return SpecificityTier.BASE
    val ratio = strengthEstimate / goal
    return when {
        ratio >= ProgressionTuning.PEAKING_TIER_RATIO -> SpecificityTier.PEAKING
        ratio >= ProgressionTuning.SPECIFIC_TIER_RATIO -> SpecificityTier.SPECIFIC
        else -> SpecificityTier.BASE
    }
}

private fun specificityParameters(tier: SpecificityTier, type: ExposureType): ExposureParameters {
    return when (tier to type) {
        SpecificityTier.BASE to ExposureType.VOLUME -> ExposureParameters(
            ProgressionTuning.BASE_VOLUME_REP_MIN,
            ProgressionTuning.BASE_VOLUME_REP_MAX,
            ProgressionTuning.BASE_TARGET_RIR
        )
        SpecificityTier.BASE to ExposureType.STRENGTH -> ExposureParameters(
            ProgressionTuning.BASE_STRENGTH_REP_MIN,
            ProgressionTuning.BASE_STRENGTH_REP_MAX,
            ProgressionTuning.BASE_TARGET_RIR
        )
        SpecificityTier.SPECIFIC to ExposureType.VOLUME -> ExposureParameters(
            ProgressionTuning.SPECIFIC_VOLUME_REP_MIN,
            ProgressionTuning.SPECIFIC_VOLUME_REP_MAX,
            ProgressionTuning.SPECIFIC_VOLUME_TARGET_RIR
        )
        SpecificityTier.SPECIFIC to ExposureType.STRENGTH -> ExposureParameters(
            ProgressionTuning.SPECIFIC_STRENGTH_REP_MIN,
            ProgressionTuning.SPECIFIC_STRENGTH_REP_MAX,
            ProgressionTuning.SPECIFIC_STRENGTH_TARGET_RIR
        )
        SpecificityTier.PEAKING to ExposureType.VOLUME -> ExposureParameters(
            ProgressionTuning.PEAKING_VOLUME_REP_MIN,
            ProgressionTuning.PEAKING_VOLUME_REP_MAX,
            ProgressionTuning.PEAKING_VOLUME_TARGET_RIR
        )
        SpecificityTier.PEAKING to ExposureType.STRENGTH -> ExposureParameters(
            ProgressionTuning.PEAKING_STRENGTH_REP_MIN,
            ProgressionTuning.PEAKING_STRENGTH_REP_MAX,
            ProgressionTuning.PEAKING_STRENGTH_TARGET_RIR
        )
        else -> error("Specificity only applies to progression lanes")
    }
}

private fun ExerciseProgressionProfile.hardExposureStreakFor(outcome: OutcomeClass?): Int {
    return when (outcome) {
        OutcomeClass.HARD,
        OutcomeClass.FAILED -> hardExposureStreak + 1
        else -> 0
    }
}

private fun outcomeReason(
    outcome: OutcomeClass?,
    type: ExposureType,
    decision: LoadDecision,
    profile: ExerciseProgressionProfile
): ProgressionReason {
    // profile is the post-decision state; use decision for distinctions erased by the transition.
    return when (outcome) {
        OutcomeClass.EASY -> ProgressionReason.LOAD_INCREASED_EASY
        OutcomeClass.STRONG -> ProgressionReason.LOAD_INCREASED_STRONG
        OutcomeClass.ON_TARGET -> ProgressionReason.LOAD_CONFIRMED
        OutcomeClass.HARD -> ProgressionReason.LOAD_HELD_HARD
        OutcomeClass.FAILED -> when (decision) {
            LoadDecision.REVERTED -> ProgressionReason.LOAD_REVERTED
            LoadDecision.HELD -> if (profile.laneConsecutiveFail(type) > 0) {
                ProgressionReason.LOAD_HELD_ONE_FAILURE
            } else {
                ProgressionReason.LOAD_HELD_FAILURE
            }
            LoadDecision.REDUCED -> ProgressionReason.LOAD_REDUCED_REPEATED
            else -> ProgressionReason.LOAD_UNCHANGED_FAILURE
        }
        null -> ProgressionReason.LOAD_UNCHANGED_NO_OUTCOME
    }
}

private fun ExerciseProgressionProfile.withLaneDecision(
    type: ExposureType,
    load: Double,
    previousLoad: Double?,
    pendingConfirm: Boolean,
    consecutiveFail: Int,
    nextType: ExposureType
): ExerciseProgressionProfile {
    return when (type) {
        ExposureType.VOLUME -> copy(
            loadVolumeKg = load,
            previousLoadVolumeKg = previousLoad,
            pendingConfirmVolume = pendingConfirm,
            consecutiveFailVolume = consecutiveFail,
            nextExposureType = nextType
        )
        ExposureType.STRENGTH -> copy(
            loadStrengthKg = load,
            previousLoadStrengthKg = previousLoad,
            pendingConfirmStrength = pendingConfirm,
            consecutiveFailStrength = consecutiveFail,
            nextExposureType = nextType
        )
        ExposureType.RECOVERY,
        ExposureType.EVALUATION -> error("Only progression lanes have load decisions")
    }
}

private fun ExerciseProgressionProfile.withLaneLoad(type: ExposureType, load: Double): ExerciseProgressionProfile {
    return when (type) {
        ExposureType.VOLUME -> copy(loadVolumeKg = load)
        ExposureType.STRENGTH -> copy(loadStrengthKg = load)
        ExposureType.RECOVERY,
        ExposureType.EVALUATION -> error("Only progression lanes have loads")
    }
}

private fun ExerciseProgressionProfile.laneLoad(type: ExposureType): Double? {
    return when (type) {
        ExposureType.VOLUME -> loadVolumeKg
        ExposureType.STRENGTH -> loadStrengthKg
        ExposureType.RECOVERY,
        ExposureType.EVALUATION -> null
    }
}

private fun ExerciseProgressionProfile.lanePendingConfirm(type: ExposureType): Boolean {
    return when (type) {
        ExposureType.VOLUME -> pendingConfirmVolume
        ExposureType.STRENGTH -> pendingConfirmStrength
        ExposureType.RECOVERY,
        ExposureType.EVALUATION -> false
    }
}

private fun ExerciseProgressionProfile.laneConsecutiveFail(type: ExposureType): Int {
    return when (type) {
        ExposureType.VOLUME -> consecutiveFailVolume
        ExposureType.STRENGTH -> consecutiveFailStrength
        ExposureType.RECOVERY,
        ExposureType.EVALUATION -> 0
    }
}

private fun ExerciseProgressionProfile.lanePreviousLoad(type: ExposureType): Double? {
    return when (type) {
        ExposureType.VOLUME -> previousLoadVolumeKg
        ExposureType.STRENGTH -> previousLoadStrengthKg
        ExposureType.RECOVERY,
        ExposureType.EVALUATION -> null
    }
}

private fun ExposureType.isProgressionType(): Boolean = this == ExposureType.VOLUME || this == ExposureType.STRENGTH

private fun ExposureType.isCalibrationType(): Boolean = isProgressionType()

private fun ExposureType.oppositeLane(): ExposureType {
    return when (this) {
        ExposureType.VOLUME -> ExposureType.STRENGTH
        ExposureType.STRENGTH -> ExposureType.VOLUME
        ExposureType.RECOVERY,
        ExposureType.EVALUATION -> error("Recovery and evaluation do not have an opposite lane")
    }
}

private fun calibrationSeed(profile: ExerciseProgressionProfile): Double {
    return profile.loadVolumeKg ?: profile.loadStrengthKg
    ?: error("Calibration requires a user-provided starting load")
}

/**
 * Next calibration load when no e1RM is available yet, read straight from the last probe.
 *
 * Calibration cannot rely on an e1RM that may never appear: a light load with reserve left gives
 * `reps + rir` above the confidence cutoff, so nothing is estimable. Steering by the surplus fixes
 * that on its own — raising the load lowers the reserve, the effective reps drop under the cutoff,
 * an e1RM becomes estimable and calibration can finish.
 */
private fun steerCalibrationLoad(last: ProgressionExposure, increment: Double): Double {
    val steps = when (last.outcomeClass) {
        OutcomeClass.EASY -> ProgressionTuning.STEP_EASY
        OutcomeClass.STRONG -> ProgressionTuning.STEP_STRONG
        OutcomeClass.FAILED -> ProgressionTuning.STEP_REDUCE
        OutcomeClass.ON_TARGET,
        OutcomeClass.HARD,
        null -> 0
    }
    val steered = last.prescribedLoadKg + steps * increment
    return if (steered > 0.0) steered else last.prescribedLoadKg
}

private fun estimateLoad(e1rm: Double, reps: Int, rir: Int, increment: Double): Double {
    return floorToIncrement(e1rm / (1 + (reps + rir) / ProgressionTuning.EPLEY_REP_DIVISOR), increment)
}

private fun floorToIncrement(load: Double, increment: Double): Double = floor(load / increment) * increment

private fun roundToIncrement(load: Double, increment: Double): Double = round(load / increment) * increment

private fun Double.demote(increment: Double): Double {
    return roundToIncrement(
        this * (1 + ProgressionTuning.EVALUATION_DEMOTE_PCT / ProgressionTuning.PERCENT_BASE),
        increment
    )
}
