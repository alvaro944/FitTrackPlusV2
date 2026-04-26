package com.alvarocervantes.fittrackplus.domain.model

data class WorkoutStats(
    val sessionVolumes: List<WorkoutSessionVolume>,
    val exerciseProgress: List<ExerciseProgress>,
    val exerciseRecords: List<ExerciseRecords>
)

enum class WorkoutStatsPeriod {
    All,
    LastFourWeeks,
    LastTwelveWeeks
}

data class WorkoutSessionVolume(
    val sessionId: Long,
    val routineName: String,
    val dayName: String,
    val startedAt: Long,
    val finishedAt: Long,
    val totalVolumeKg: Double
)

data class ExerciseProgress(
    val exerciseKey: String,
    val exerciseName: String,
    val entries: List<ExerciseProgressEntry>
)

data class ExerciseProgressEntry(
    val sessionId: Long,
    val finishedAt: Long,
    val volumeKg: Double,
    val maxWeightKg: Double,
    val totalReps: Int,
    val estimatedOneRepMaxKg: Double
)

data class ExerciseRecords(
    val exerciseKey: String,
    val exerciseName: String,
    val maxWeight: ExerciseSetRecord?,
    val maxReps: ExerciseSetRecord?,
    val bestSetVolume: ExerciseSetRecord?,
    val bestEstimatedOneRepMax: ExerciseSetRecord?
)

data class ExerciseSetRecord(
    val sessionId: Long,
    val finishedAt: Long,
    val weightKg: Double,
    val reps: Int,
    val setVolumeKg: Double,
    val estimatedOneRepMaxKg: Double
)
