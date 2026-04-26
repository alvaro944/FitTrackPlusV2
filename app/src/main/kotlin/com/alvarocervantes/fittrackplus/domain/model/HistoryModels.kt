package com.alvarocervantes.fittrackplus.domain.model

data class WorkoutHistorySummary(
    val sessionId: Long,
    val routineName: String,
    val dayName: String,
    val startedAt: Long,
    val finishedAt: Long,
    val weekNumber: Int,
    val totalVolumeKg: Double,
    val durationMillis: Long,
    val setCount: Int
)

data class WorkoutHistoryDetail(
    val sessionId: Long,
    val routineName: String,
    val dayName: String,
    val startedAt: Long,
    val finishedAt: Long,
    val weekNumber: Int,
    val notes: String?,
    val exercises: List<WorkoutHistoryExercise>
)

data class WorkoutHistoryExercise(
    val exerciseId: Long,
    val name: String,
    val targetRepsText: String,
    val sets: List<WorkoutHistorySet>
)

data class WorkoutHistorySet(
    val setId: Long,
    val setNumber: Int,
    val weightKg: Double,
    val reps: Int,
    val notes: String?
)
