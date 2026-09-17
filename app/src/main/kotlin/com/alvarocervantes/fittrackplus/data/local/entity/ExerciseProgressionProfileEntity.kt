package com.alvarocervantes.fittrackplus.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercise_progression_profiles")
data class ExerciseProgressionProfileEntity(
    @PrimaryKey val variantKey: String,
    val goalWeightKg: Double? = null,
    @ColumnInfo(defaultValue = "2.5")
    val loadIncrementKg: Double = 2.5,
    @ColumnInfo(defaultValue = "'CALIBRATING'")
    val state: String = "CALIBRATING",
    val loadVolumeKg: Double? = null,
    val loadStrengthKg: Double? = null,
    @ColumnInfo(defaultValue = "0")
    val pendingConfirmVolume: Boolean = false,
    @ColumnInfo(defaultValue = "0")
    val pendingConfirmStrength: Boolean = false,
    @ColumnInfo(defaultValue = "'VOLUME'")
    val nextExposureType: String = "VOLUME",
    @ColumnInfo(defaultValue = "0")
    val hardExposureStreak: Int = 0,
    @ColumnInfo(defaultValue = "0")
    val exposuresSinceRecovery: Int = 0,
    @ColumnInfo(defaultValue = "0")
    val consecutiveFailVolume: Int = 0,
    @ColumnInfo(defaultValue = "0")
    val consecutiveFailStrength: Int = 0,
    val previousLoadVolumeKg: Double? = null,
    val previousLoadStrengthKg: Double? = null,
    @ColumnInfo(defaultValue = "0")
    val stallCount: Int = 0,
    val ewmaStrengthE1rm: Double? = null,
    val ewmaVolumeE1rm: Double? = null,
    @ColumnInfo(defaultValue = "0")
    val strengthPointCount: Int = 0,
    @ColumnInfo(defaultValue = "0")
    val volumePointCount: Int = 0,
    @ColumnInfo(defaultValue = "0")
    val recoveryExposuresRemaining: Int = 0,
    @ColumnInfo(defaultValue = "0")
    val evaluationFailStreak: Int = 0,
    @ColumnInfo(defaultValue = "0")
    val calibrationRetries: Int = 0
)
