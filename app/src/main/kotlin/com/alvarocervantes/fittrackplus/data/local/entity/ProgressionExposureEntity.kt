package com.alvarocervantes.fittrackplus.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "progression_exposures",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutExerciseId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("variantKey"),
        Index("workoutExerciseId")
    ]
)
data class ProgressionExposureEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val variantKey: String,
    val workoutExerciseId: Long?,
    val exposureIndex: Int,
    val performedAt: Long,
    val type: String,
    val prescribedLoadKg: Double,
    val prescribedRepMin: Int,
    val prescribedRepMax: Int,
    val prescribedTargetRir: Int,
    val prescribedSets: Int,
    val probeReps: Int?,
    val probeRir: Int?,
    val probeSurplus: Int?,
    val outcomeClass: String?,
    val setCompletionRatio: Double?,
    val exposureE1rm: Double?,
    val e1rmConfidence: String?,
    val excludeFromTrend: Boolean,
    val userFlaggedBadDay: Boolean,
    val intraSessionAdjustmentSteps: Int,
    val decisionReason: String
)
