package com.alvarocervantes.fittrackplus.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alvarocervantes.fittrackplus.data.local.entity.ExerciseProgressionProfileEntity
import com.alvarocervantes.fittrackplus.data.local.entity.ProgressionExposureEntity

@Dao
interface ProgressionDao {
    @Query("SELECT * FROM exercise_progression_profiles WHERE variantKey = :variantKey")
    suspend fun getProfile(variantKey: String): ExerciseProgressionProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: ExerciseProgressionProfileEntity)

    @Query(
        """
        SELECT * FROM progression_exposures
        WHERE variantKey = :variantKey
        ORDER BY exposureIndex ASC
        """
    )
    suspend fun getExposures(variantKey: String): List<ProgressionExposureEntity>

    // Exposures are immutable historical snapshots: they are inserted once and never updated.
    @Insert
    suspend fun insertExposure(exposure: ProgressionExposureEntity): Long
}
