package com.alvarocervantes.fittrackplus.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.alvarocervantes.fittrackplus.data.local.entity.RoutineExerciseAlternativeEntity
import com.alvarocervantes.fittrackplus.data.local.entity.RoutineExerciseEntity

data class RoutineExerciseWithAlternatives(
    @Embedded val exercise: RoutineExerciseEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "routineExerciseId"
    )
    val alternatives: List<RoutineExerciseAlternativeEntity>
)
