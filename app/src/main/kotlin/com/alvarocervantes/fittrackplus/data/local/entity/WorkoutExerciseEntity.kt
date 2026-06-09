package com.alvarocervantes.fittrackplus.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_exercises",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("sessionId"),
        Index("exerciseTemplateId"),
        Index(value = ["sessionId", "position"], unique = true)
    ]
)
data class WorkoutExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val exerciseTemplateId: Long?,
    val performedVariantKey: String = "",
    val exerciseNameSnapshot: String,
    val targetRepsSnapshot: String,
    val position: Int
)
