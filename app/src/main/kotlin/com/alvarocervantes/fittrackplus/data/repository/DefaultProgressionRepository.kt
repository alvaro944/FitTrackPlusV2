package com.alvarocervantes.fittrackplus.data.repository

import com.alvarocervantes.fittrackplus.data.local.dao.ProgressionDao
import com.alvarocervantes.fittrackplus.data.local.entity.ExerciseProgressionProfileEntity
import com.alvarocervantes.fittrackplus.data.local.entity.ProgressionExposureEntity
import com.alvarocervantes.fittrackplus.domain.model.progression.E1rmConfidence
import com.alvarocervantes.fittrackplus.domain.model.progression.ExerciseProgressionProfile
import com.alvarocervantes.fittrackplus.domain.model.progression.ExposureType
import com.alvarocervantes.fittrackplus.domain.model.progression.OutcomeClass
import com.alvarocervantes.fittrackplus.domain.model.progression.ProgressionExposure
import com.alvarocervantes.fittrackplus.domain.model.progression.ProgressionState
import javax.inject.Inject

class DefaultProgressionRepository @Inject constructor(
    private val progressionDao: ProgressionDao
) : ProgressionRepository {

    override suspend fun getProfile(variantKey: String): ExerciseProgressionProfile? {
        return progressionDao.getProfile(variantKey)?.toDomain()
    }

    override suspend fun upsertProfile(profile: ExerciseProgressionProfile) {
        progressionDao.upsertProfile(profile.toEntity())
    }

    override suspend fun getExposures(variantKey: String): List<ProgressionExposure> {
        return progressionDao.getExposures(variantKey).map { it.toDomain() }
    }

    override suspend fun insertExposure(exposure: ProgressionExposure): Long {
        return progressionDao.insertExposure(exposure.toEntity())
    }
}

private fun ExerciseProgressionProfileEntity.toDomain(): ExerciseProgressionProfile {
    return ExerciseProgressionProfile(
        variantKey = variantKey,
        goalWeightKg = goalWeightKg,
        loadIncrementKg = loadIncrementKg,
        state = ProgressionState.valueOf(state),
        loadVolumeKg = loadVolumeKg,
        loadStrengthKg = loadStrengthKg,
        pendingConfirmVolume = pendingConfirmVolume,
        pendingConfirmStrength = pendingConfirmStrength,
        nextExposureType = ExposureType.valueOf(nextExposureType),
        hardExposureStreak = hardExposureStreak,
        exposuresSinceRecovery = exposuresSinceRecovery,
        consecutiveFailVolume = consecutiveFailVolume,
        consecutiveFailStrength = consecutiveFailStrength,
        previousLoadVolumeKg = previousLoadVolumeKg,
        previousLoadStrengthKg = previousLoadStrengthKg,
        stallCount = stallCount,
        ewmaStrengthE1rm = ewmaStrengthE1rm,
        ewmaVolumeE1rm = ewmaVolumeE1rm,
        strengthPointCount = strengthPointCount,
        volumePointCount = volumePointCount,
        recoveryExposuresRemaining = recoveryExposuresRemaining,
        evaluationFailStreak = evaluationFailStreak,
        calibrationRetries = calibrationRetries
    )
}

private fun ExerciseProgressionProfile.toEntity(): ExerciseProgressionProfileEntity {
    return ExerciseProgressionProfileEntity(
        variantKey = variantKey,
        goalWeightKg = goalWeightKg,
        loadIncrementKg = loadIncrementKg,
        state = state.name,
        loadVolumeKg = loadVolumeKg,
        loadStrengthKg = loadStrengthKg,
        pendingConfirmVolume = pendingConfirmVolume,
        pendingConfirmStrength = pendingConfirmStrength,
        nextExposureType = nextExposureType.name,
        hardExposureStreak = hardExposureStreak,
        exposuresSinceRecovery = exposuresSinceRecovery,
        consecutiveFailVolume = consecutiveFailVolume,
        consecutiveFailStrength = consecutiveFailStrength,
        previousLoadVolumeKg = previousLoadVolumeKg,
        previousLoadStrengthKg = previousLoadStrengthKg,
        stallCount = stallCount,
        ewmaStrengthE1rm = ewmaStrengthE1rm,
        ewmaVolumeE1rm = ewmaVolumeE1rm,
        strengthPointCount = strengthPointCount,
        volumePointCount = volumePointCount,
        recoveryExposuresRemaining = recoveryExposuresRemaining,
        evaluationFailStreak = evaluationFailStreak,
        calibrationRetries = calibrationRetries
    )
}

private fun ProgressionExposureEntity.toDomain(): ProgressionExposure {
    return ProgressionExposure(
        id = id,
        variantKey = variantKey,
        workoutExerciseId = workoutExerciseId,
        exposureIndex = exposureIndex,
        performedAt = performedAt,
        type = ExposureType.valueOf(type),
        prescribedLoadKg = prescribedLoadKg,
        prescribedRepMin = prescribedRepMin,
        prescribedRepMax = prescribedRepMax,
        prescribedTargetRir = prescribedTargetRir,
        prescribedSets = prescribedSets,
        probeReps = probeReps,
        probeRir = probeRir,
        probeSurplus = probeSurplus,
        outcomeClass = outcomeClass?.let(OutcomeClass::valueOf),
        setCompletionRatio = setCompletionRatio,
        exposureE1rm = exposureE1rm,
        e1rmConfidence = e1rmConfidence?.let(E1rmConfidence::valueOf),
        excludeFromTrend = excludeFromTrend,
        userFlaggedBadDay = userFlaggedBadDay,
        intraSessionAdjustmentSteps = intraSessionAdjustmentSteps,
        decisionReason = decisionReason
    )
}

private fun ProgressionExposure.toEntity(): ProgressionExposureEntity {
    return ProgressionExposureEntity(
        id = id,
        variantKey = variantKey,
        workoutExerciseId = workoutExerciseId,
        exposureIndex = exposureIndex,
        performedAt = performedAt,
        type = type.name,
        prescribedLoadKg = prescribedLoadKg,
        prescribedRepMin = prescribedRepMin,
        prescribedRepMax = prescribedRepMax,
        prescribedTargetRir = prescribedTargetRir,
        prescribedSets = prescribedSets,
        probeReps = probeReps,
        probeRir = probeRir,
        probeSurplus = probeSurplus,
        outcomeClass = outcomeClass?.name,
        setCompletionRatio = setCompletionRatio,
        exposureE1rm = exposureE1rm,
        e1rmConfidence = e1rmConfidence?.name,
        excludeFromTrend = excludeFromTrend,
        userFlaggedBadDay = userFlaggedBadDay,
        intraSessionAdjustmentSteps = intraSessionAdjustmentSteps,
        decisionReason = decisionReason
    )
}
