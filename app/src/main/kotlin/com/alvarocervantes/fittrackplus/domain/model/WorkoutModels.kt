package com.alvarocervantes.fittrackplus.domain.model

enum class PrType { MaxWeight, MaxVolume }

data class StartedWorkoutSession(
    val sessionId: Long,
    val routineId: Long,
    val routineNameSnapshot: String,
    val dayNameSnapshot: String,
    val weekNumber: Int
)

data class WorkoutPreview(
    val routineId: Long,
    val routineName: String,
    val dayName: String,
    val weekNumber: Int,
    val exerciseCount: Int
)
