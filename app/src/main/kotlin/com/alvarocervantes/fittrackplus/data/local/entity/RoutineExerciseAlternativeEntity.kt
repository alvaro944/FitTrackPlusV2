package com.alvarocervantes.fittrackplus.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "routine_exercise_alternatives",
    foreignKeys = [
        ForeignKey(
            entity = RoutineExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["routineExerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("routineExerciseId"),
        Index(value = ["routineExerciseId", "position"], unique = true),
        Index(value = ["variantKey"], unique = true)
    ]
)
data class RoutineExerciseAlternativeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val routineExerciseId: Long,
    val variantKey: String,
    val name: String,
    val targetSets: Int,
    val targetRepsText: String,
    val position: Int,
    val notes: String? = null
)
