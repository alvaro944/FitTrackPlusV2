package com.alvarocervantes.fittrackplus.domain.usecase

import com.alvarocervantes.fittrackplus.data.local.entity.WorkoutSetEntity
import com.alvarocervantes.fittrackplus.data.local.relation.WorkoutExerciseWithSets
import com.alvarocervantes.fittrackplus.data.local.relation.WorkoutSessionWithExercises
import com.alvarocervantes.fittrackplus.data.repository.WorkoutRepository
import com.alvarocervantes.fittrackplus.domain.model.ExerciseProgress
import com.alvarocervantes.fittrackplus.domain.model.ExerciseProgressEntry
import com.alvarocervantes.fittrackplus.domain.model.ExerciseRecords
import com.alvarocervantes.fittrackplus.domain.model.ExerciseSetRecord
import com.alvarocervantes.fittrackplus.domain.model.WorkoutSessionVolume
import com.alvarocervantes.fittrackplus.domain.model.WorkoutStats
import com.alvarocervantes.fittrackplus.domain.model.WorkoutStatsPeriod
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DAY_MILLIS: Long = 86_400_000

class ObserveWorkoutStatsUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    operator fun invoke(
        period: WorkoutStatsPeriod = WorkoutStatsPeriod.All,
        nowMillis: Long = System.currentTimeMillis()
    ): Flow<WorkoutStats> {
        return workoutRepository.observeFinishedSessionsWithExercises().map { sessions ->
            sessions.toWorkoutStats(period = period, nowMillis = nowMillis)
        }
    }
}

private fun List<WorkoutSessionWithExercises>.toWorkoutStats(
    period: WorkoutStatsPeriod,
    nowMillis: Long
): WorkoutStats {
    val finishedSessions = toFinishedSessions().filterByPeriod(
        period = period,
        nowMillis = nowMillis
    )
    val exerciseGroups = finishedSessions.chronological().toExerciseGroups()

    return WorkoutStats(
        sessionVolumes = finishedSessions.recent().toSessionVolumes(),
        exerciseProgress = exerciseGroups.toExerciseProgress(),
        exerciseRecords = exerciseGroups.toExerciseRecords()
    )
}

private fun List<WorkoutSessionWithExercises>.toFinishedSessions(): List<FinishedSession> {
    return mapNotNull { session ->
        val finishedAt = session.session.finishedAt ?: return@mapNotNull null
        FinishedSession(session = session, finishedAt = finishedAt)
    }
}

private fun List<FinishedSession>.filterByPeriod(
    period: WorkoutStatsPeriod,
    nowMillis: Long
): List<FinishedSession> {
    val cutoff = when (period) {
        WorkoutStatsPeriod.All -> return this
        WorkoutStatsPeriod.LastFourWeeks -> nowMillis - 4 * 7 * DAY_MILLIS
        WorkoutStatsPeriod.LastTwelveWeeks -> nowMillis - 12 * 7 * DAY_MILLIS
    }
    return filter { session -> session.finishedAt >= cutoff }
}

private fun List<FinishedSession>.recent(): List<FinishedSession> {
    return sortedWith(
        compareByDescending<FinishedSession> { it.finishedAt }
            .thenByDescending { it.session.session.startedAt }
    )
}

private fun List<FinishedSession>.chronological(): List<FinishedSession> {
    return sortedWith(
        compareBy<FinishedSession> { it.finishedAt }
            .thenBy { it.session.session.startedAt }
    )
}

private fun List<FinishedSession>.toSessionVolumes(): List<WorkoutSessionVolume> {
    return map { finishedSession ->
        val session = finishedSession.session.session
        WorkoutSessionVolume(
            sessionId = session.id,
            routineName = session.routineNameSnapshot,
            dayName = session.dayNameSnapshot,
            startedAt = session.startedAt,
            finishedAt = finishedSession.finishedAt,
            totalVolumeKg = finishedSession.session.exercises.sumOf { exercise ->
                exercise.sets.sumOf { set -> set.volumeKg() }
            }
        )
    }
}

private fun List<FinishedSession>.toExerciseGroups(): Map<String, List<ExerciseSnapshot>> {
    return flatMap { finishedSession ->
        finishedSession.session.exercises.mapNotNull { exercise ->
            finishedSession.toExerciseSnapshot(exercise)
        }
    }.groupBy { snapshot -> snapshot.key }
}

private fun FinishedSession.toExerciseSnapshot(
    exercise: WorkoutExerciseWithSets
): ExerciseSnapshot? {
    val key = exercise.exercise.exerciseNameSnapshot.normalizedExerciseKey()
    return if (key.isBlank()) {
        null
    } else {
        ExerciseSnapshot(
            key = key,
            name = exercise.exercise.exerciseNameSnapshot.trim(),
            sessionId = session.session.id,
            finishedAt = finishedAt,
            exercise = exercise
        )
    }
}

private fun Map<String, List<ExerciseSnapshot>>.toExerciseProgress(): List<ExerciseProgress> {
    return map { (key, snapshots) ->
        ExerciseProgress(
            exerciseKey = key,
            exerciseName = snapshots.first().name,
            entries = snapshots.map { snapshot ->
                snapshot.toProgressEntry()
            }
        )
    }.sortedBy { progress -> progress.exerciseName.lowercase() }
}

private fun ExerciseSnapshot.toProgressEntry(): ExerciseProgressEntry {
    return ExerciseProgressEntry(
        sessionId = sessionId,
        finishedAt = finishedAt,
        volumeKg = exercise.sets.sumOf { set -> set.volumeKg() },
        maxWeightKg = exercise.sets.maxOfOrNull { set -> set.weightKg } ?: 0.0,
        totalReps = exercise.sets.sumOf { set -> set.reps },
        estimatedOneRepMaxKg = exercise.sets.maxOfOrNull { set ->
            set.estimatedOneRepMaxKg()
        } ?: 0.0
    )
}

private fun Map<String, List<ExerciseSnapshot>>.toExerciseRecords(): List<ExerciseRecords> {
    return map { (key, snapshots) ->
        val records = snapshots.toSetRecords()
        ExerciseRecords(
            exerciseKey = key,
            exerciseName = snapshots.first().name,
            maxWeight = records
                .filter { record -> record.weightKg > 0.0 && record.reps > 0 }
                .maxByOrNull { record -> record.weightKg },
            maxReps = records
                .filter { record -> record.reps > 0 }
                .maxByOrNull { record -> record.reps },
            bestSetVolume = records
                .filter { record -> record.weightKg > 0.0 && record.reps > 0 }
                .maxByOrNull { record -> record.setVolumeKg },
            bestEstimatedOneRepMax = records
                .filter { record -> record.weightKg > 0.0 && record.reps > 0 }
                .maxByOrNull { record -> record.estimatedOneRepMaxKg }
        )
    }.sortedBy { records -> records.exerciseName.lowercase() }
}

private fun List<ExerciseSnapshot>.toSetRecords(): List<ExerciseSetRecord> {
    return flatMap { snapshot ->
        snapshot.exercise.sets.map { set ->
            set.toRecord(
                sessionId = snapshot.sessionId,
                finishedAt = snapshot.finishedAt
            )
        }
    }
}

private data class FinishedSession(
    val session: WorkoutSessionWithExercises,
    val finishedAt: Long
)

private data class ExerciseSnapshot(
    val key: String,
    val name: String,
    val sessionId: Long,
    val finishedAt: Long,
    val exercise: WorkoutExerciseWithSets
)

private fun String.normalizedExerciseKey(): String {
    return trim().lowercase(Locale.ROOT)
}

private fun WorkoutSetEntity.volumeKg(): Double {
    return weightKg * reps
}

private fun WorkoutSetEntity.estimatedOneRepMaxKg(): Double {
    return if (weightKg > 0.0 && reps > 0) {
        weightKg * (1.0 + reps / 30.0)
    } else {
        0.0
    }
}

private fun WorkoutSetEntity.toRecord(
    sessionId: Long,
    finishedAt: Long
): ExerciseSetRecord {
    return ExerciseSetRecord(
        sessionId = sessionId,
        finishedAt = finishedAt,
        weightKg = weightKg,
        reps = reps,
        setVolumeKg = volumeKg(),
        estimatedOneRepMaxKg = estimatedOneRepMaxKg()
    )
}
