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
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale

class ObserveWorkoutStatsUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    operator fun invoke(): Flow<WorkoutStats> {
        return workoutRepository.observeFinishedSessionsWithExercises().map { sessions ->
            sessions.toWorkoutStats()
        }
    }
}

private fun List<WorkoutSessionWithExercises>.toWorkoutStats(): WorkoutStats {
    val finishedSessions = mapNotNull { session ->
        val finishedAt = session.session.finishedAt ?: return@mapNotNull null
        FinishedSession(session = session, finishedAt = finishedAt)
    }

    val recentSessions = finishedSessions.sortedWith(
        compareByDescending<FinishedSession> { it.finishedAt }
            .thenByDescending { it.session.session.startedAt }
    )
    val chronologicalSessions = finishedSessions.sortedWith(
        compareBy<FinishedSession> { it.finishedAt }
            .thenBy { it.session.session.startedAt }
    )

    val sessionVolumes = recentSessions.map { finishedSession ->
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

    val exerciseGroups = chronologicalSessions
        .flatMap { finishedSession ->
            finishedSession.session.exercises.mapNotNull { exercise ->
                val key = exercise.exercise.exerciseNameSnapshot.normalizedExerciseKey()
                if (key.isBlank()) {
                    null
                } else {
                    ExerciseSnapshot(
                        key = key,
                        name = exercise.exercise.exerciseNameSnapshot.trim(),
                        sessionId = finishedSession.session.session.id,
                        finishedAt = finishedSession.finishedAt,
                        exercise = exercise
                    )
                }
            }
        }
        .groupBy { snapshot -> snapshot.key }

    val exerciseProgress = exerciseGroups.map { (key, snapshots) ->
        ExerciseProgress(
            exerciseKey = key,
            exerciseName = snapshots.first().name,
            entries = snapshots.map { snapshot ->
                ExerciseProgressEntry(
                    sessionId = snapshot.sessionId,
                    finishedAt = snapshot.finishedAt,
                    volumeKg = snapshot.exercise.sets.sumOf { set -> set.volumeKg() },
                    maxWeightKg = snapshot.exercise.sets.maxOfOrNull { set -> set.weightKg } ?: 0.0,
                    totalReps = snapshot.exercise.sets.sumOf { set -> set.reps },
                    estimatedOneRepMaxKg = snapshot.exercise.sets.maxOfOrNull { set ->
                        set.estimatedOneRepMaxKg()
                    } ?: 0.0
                )
            }
        )
    }.sortedBy { progress -> progress.exerciseName.lowercase() }

    val exerciseRecords = exerciseGroups.map { (key, snapshots) ->
        val records = snapshots.flatMap { snapshot ->
            snapshot.exercise.sets.map { set ->
                set.toRecord(
                    sessionId = snapshot.sessionId,
                    finishedAt = snapshot.finishedAt
                )
            }
        }
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

    return WorkoutStats(
        sessionVolumes = sessionVolumes,
        exerciseProgress = exerciseProgress,
        exerciseRecords = exerciseRecords
    )
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
