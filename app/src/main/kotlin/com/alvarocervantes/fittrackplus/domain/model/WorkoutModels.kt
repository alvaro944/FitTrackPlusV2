package com.alvarocervantes.fittrackplus.domain.model

data class StartedWorkoutSession(
    val sessionId: Long,
    val routineId: Long,
    val routineNameSnapshot: String,
    val dayNameSnapshot: String,
    val weekNumber: Int
)
