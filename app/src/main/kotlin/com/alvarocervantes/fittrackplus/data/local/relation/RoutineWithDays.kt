package com.alvarocervantes.fittrackplus.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.alvarocervantes.fittrackplus.data.local.entity.RoutineDayEntity
import com.alvarocervantes.fittrackplus.data.local.entity.RoutineEntity

data class RoutineWithDays(
    @Embedded val routine: RoutineEntity,
    @Relation(
        entity = RoutineDayEntity::class,
        parentColumn = "id",
        entityColumn = "routineId"
    )
    val days: List<RoutineDayWithExercises>
)
