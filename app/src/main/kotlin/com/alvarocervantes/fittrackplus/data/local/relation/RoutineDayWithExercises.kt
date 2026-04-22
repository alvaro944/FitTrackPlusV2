package com.alvarocervantes.fittrackplus.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.alvarocervantes.fittrackplus.data.local.entity.RoutineDayEntity
import com.alvarocervantes.fittrackplus.data.local.entity.RoutineExerciseEntity

data class RoutineDayWithExercises(
    @Embedded val day: RoutineDayEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "routineDayId"
    )
    val exercises: List<RoutineExerciseEntity>
)
