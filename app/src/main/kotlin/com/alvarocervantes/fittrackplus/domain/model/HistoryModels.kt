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
    val exercises: List<WorkoutHistoryExercise>,
    val comparison: WorkoutHistoryComparison? = null
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

data class WorkoutHistoryComparison(
    val previousSessionId: Long,
    val previousFinishedAt: Long,
    val totalVolumeDelta: WorkoutHistoryMetricDelta,
    val durationMillisDelta: WorkoutHistoryMetricDelta,
    val setCountDelta: WorkoutHistoryMetricDelta,
    val bestSet: WorkoutHistoryBestSetComparison
)

data class WorkoutHistoryMetricDelta(
    val currentValue: Double,
    val previousValue: Double,
    val deltaValue: Double,
    val direction: WorkoutHistoryDeltaDirection
)

enum class WorkoutHistoryDeltaDirection {
    Up,
    Down,
    Same,
    Unavailable
}

data class WorkoutHistoryBestSetComparison(
    val current: WorkoutHistoryBestSet?,
    val previous: WorkoutHistoryBestSet?,
    val delta: WorkoutHistoryMetricDelta
)

data class WorkoutHistoryBestSet(
    val exerciseName: String,
    val weightKg: Double,
    val reps: Int,
    val volumeKg: Double
)
