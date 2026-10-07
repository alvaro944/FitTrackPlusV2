package com.alvarocervantes.fittrackplus.data.repository

import com.alvarocervantes.fittrackplus.domain.model.progression.ExerciseProgressionProfile
import com.alvarocervantes.fittrackplus.domain.model.progression.ProgressionExposure

interface ProgressionRepository {
    suspend fun getProfile(variantKey: String): ExerciseProgressionProfile?
    suspend fun upsertProfile(profile: ExerciseProgressionProfile)
    suspend fun getExposures(variantKey: String): List<ProgressionExposure>
    suspend fun insertExposure(exposure: ProgressionExposure): Long
}
