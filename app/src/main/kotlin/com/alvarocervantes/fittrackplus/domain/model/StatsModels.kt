package com.alvarocervantes.fittrackplus.domain.model

data class WorkoutStats(
    val sessionVolumes: List<WorkoutSessionVolume>,
    val exerciseProgress: List<ExerciseProgress>,
    val exerciseRecords: List<ExerciseRecords>
)

enum class WorkoutStatsPeriod(val label: String) {
    LastFourWeeks("4 semanas"),
    LastTwelveWeeks("12 semanas"),
    All("Todo")
}

data class WorkoutSessionVolume(
    val sessionId: Long,
    val routineId: Long?,
    val routineName: String,
    val dayId: Long?,
    val dayName: String,
    val startedAt: Long,
    val finishedAt: Long,
    val totalVolumeKg: Double
)

data class ExerciseProgress(
    val exerciseKey: String,
    val exerciseName: String,
    val scopeKey: String = exerciseKey,
    val routineName: String = "",
    val dayName: String = "",
    val exercisePosition: Int = Int.MAX_VALUE,
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
    val scopeKey: String = exerciseKey,
    val routineName: String = "",
    val dayName: String = "",
    val exercisePosition: Int = Int.MAX_VALUE,
    val maxWeight: ExerciseSetRecord?,
    val maxReps: ExerciseSetRecord?,
    val bestSetVolume: ExerciseSetRecord?,
    val bestEstimatedOneRepMax: ExerciseSetRecord?
)

data class HeatmapDay(
    val epochDay: Long,
    val totalVolumeKg: Double,
    val intensityLevel: Int,  // 0 = sin actividad, 1-4 = cuartiles del rango con actividad
    /**
     * Sesiones terminadas ese dia. Es lo que decide si el dia cuenta como entrenado: un
     * entrenamiento de peso corporal tiene volumen 0 y aun asi es un dia entrenado.
     */
    val sessionCount: Int = 0
)

data class ExerciseSetRecord(
    val sessionId: Long,
    val finishedAt: Long,
    val weightKg: Double,
    val reps: Int,
    val setVolumeKg: Double,
    val estimatedOneRepMaxKg: Double
)
