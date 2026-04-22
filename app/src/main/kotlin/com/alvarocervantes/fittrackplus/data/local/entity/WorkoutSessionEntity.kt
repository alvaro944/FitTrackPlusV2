package com.alvarocervantes.fittrackplus.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_sessions",
    indices = [
        Index("routineId"),
        Index("routineDayId"),
        Index("startedAt")
    ]
)
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val routineId: Long?,
    val routineNameSnapshot: String,
    val routineDayId: Long?,
    val dayNameSnapshot: String,
    val startedAt: Long,
    val finishedAt: Long? = null,
    val weekNumber: Int,
    val notes: String? = null
)
