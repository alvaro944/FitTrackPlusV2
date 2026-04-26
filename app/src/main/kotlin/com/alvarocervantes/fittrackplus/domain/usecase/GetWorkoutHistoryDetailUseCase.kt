package com.alvarocervantes.fittrackplus.domain.usecase

import com.alvarocervantes.fittrackplus.data.local.relation.WorkoutSessionWithExercises
import com.alvarocervantes.fittrackplus.data.repository.WorkoutRepository
import com.alvarocervantes.fittrackplus.domain.model.WorkoutHistoryBestSet
import com.alvarocervantes.fittrackplus.domain.model.WorkoutHistoryBestSetComparison
import com.alvarocervantes.fittrackplus.domain.model.WorkoutHistoryComparison
import com.alvarocervantes.fittrackplus.domain.model.WorkoutHistoryDetail
import com.alvarocervantes.fittrackplus.domain.model.WorkoutHistoryDeltaDirection
import com.alvarocervantes.fittrackplus.domain.model.WorkoutHistoryExercise
import com.alvarocervantes.fittrackplus.domain.model.WorkoutHistoryMetricDelta
import com.alvarocervantes.fittrackplus.domain.model.WorkoutHistorySet
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class GetWorkoutHistoryDetailUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    suspend operator fun invoke(sessionId: Long): WorkoutHistoryDetail? {
        val currentSession = workoutRepository.getFinishedSessionWithExercises(sessionId) ?: return null
        val currentDetail = currentSession.toHistoryDetail() ?: return null
        val previousSession = workoutRepository.observeFinishedSessionsWithExercises()
            .first()
            .findPreviousComparableSession(currentSession)
        return currentDetail.copy(
            comparison = previousSession?.let { currentSession.toComparison(it) }
        )
    }
}

private fun WorkoutSessionWithExercises.toHistoryDetail(): WorkoutHistoryDetail? {
    val finishedAt = session.finishedAt ?: return null
    return WorkoutHistoryDetail(
        sessionId = session.id,
        routineName = session.routineNameSnapshot,
        dayName = session.dayNameSnapshot,
        startedAt = session.startedAt,
        finishedAt = finishedAt,
        weekNumber = session.weekNumber,
        notes = session.notes,
        exercises = exercises
            .sortedBy { it.exercise.position }
            .map { exerciseWithSets ->
                WorkoutHistoryExercise(
                    exerciseId = exerciseWithSets.exercise.id,
                    name = exerciseWithSets.exercise.exerciseNameSnapshot,
                    targetRepsText = exerciseWithSets.exercise.targetRepsSnapshot,
                    sets = exerciseWithSets.sets
                        .sortedBy { it.setNumber }
                        .map { set ->
                            WorkoutHistorySet(
                                setId = set.id,
                                setNumber = set.setNumber,
                                weightKg = set.weightKg,
                                reps = set.reps,
                                notes = set.notes
                            )
                        }
                )
            }
    )
}

private fun List<WorkoutSessionWithExercises>.findPreviousComparableSession(
    current: WorkoutSessionWithExercises
): WorkoutSessionWithExercises? {
    val currentFinishedAt = current.session.finishedAt ?: return null
    return filter { candidate ->
        val candidateFinishedAt = candidate.session.finishedAt
        candidate.session.id != current.session.id &&
            candidateFinishedAt != null &&
            candidateFinishedAt < currentFinishedAt &&
            candidate.session.routineNameSnapshot == current.session.routineNameSnapshot &&
            candidate.session.dayNameSnapshot == current.session.dayNameSnapshot
    }.maxWithOrNull(
        compareBy<WorkoutSessionWithExercises> { it.session.finishedAt ?: Long.MIN_VALUE }
            .thenBy { it.session.startedAt }
    )
}

private fun WorkoutSessionWithExercises.toComparison(
    previous: WorkoutSessionWithExercises
): WorkoutHistoryComparison {
    return WorkoutHistoryComparison(
        previousSessionId = previous.session.id,
        previousFinishedAt = previous.session.finishedAt ?: previous.session.startedAt,
        totalVolumeDelta = metricDelta(totalVolumeKg(), previous.totalVolumeKg()),
        durationMillisDelta = metricDelta(durationMillis().toDouble(), previous.durationMillis().toDouble()),
        setCountDelta = metricDelta(setCount().toDouble(), previous.setCount().toDouble()),
        bestSet = bestSetComparison(previous)
    )
}

private fun WorkoutSessionWithExercises.bestSetComparison(
    previous: WorkoutSessionWithExercises
): WorkoutHistoryBestSetComparison {
    val currentBestSet = bestSet()
    val previousBestSet = previous.bestSet()
    return WorkoutHistoryBestSetComparison(
        current = currentBestSet,
        previous = previousBestSet,
        delta = nullableMetricDelta(
            current = currentBestSet?.volumeKg,
            previous = previousBestSet?.volumeKg
        )
    )
}

private fun WorkoutSessionWithExercises.totalVolumeKg(): Double {
    return exercises.sumOf { exerciseWithSets ->
        exerciseWithSets.sets.sumOf { set -> set.weightKg * set.reps }
    }
}

private fun WorkoutSessionWithExercises.durationMillis(): Long {
    val finishedAt = session.finishedAt ?: return 0
    return (finishedAt - session.startedAt).coerceAtLeast(0)
}

private fun WorkoutSessionWithExercises.setCount(): Int {
    return exercises.sumOf { exerciseWithSets -> exerciseWithSets.sets.size }
}

private fun WorkoutSessionWithExercises.bestSet(): WorkoutHistoryBestSet? {
    return exercises
        .flatMap { exerciseWithSets ->
            exerciseWithSets.sets.map { set ->
                WorkoutHistoryBestSet(
                    exerciseName = exerciseWithSets.exercise.exerciseNameSnapshot,
                    weightKg = set.weightKg,
                    reps = set.reps,
                    volumeKg = set.weightKg * set.reps
                )
            }
        }
        .filter { set -> set.volumeKg > 0.0 }
        .maxByOrNull { set -> set.volumeKg }
}

private fun metricDelta(current: Double, previous: Double): WorkoutHistoryMetricDelta {
    val delta = current - previous
    return WorkoutHistoryMetricDelta(
        currentValue = current,
        previousValue = previous,
        deltaValue = delta,
        direction = when {
            delta > 0.0 -> WorkoutHistoryDeltaDirection.Up
            delta < 0.0 -> WorkoutHistoryDeltaDirection.Down
            else -> WorkoutHistoryDeltaDirection.Same
        }
    )
}

private fun nullableMetricDelta(current: Double?, previous: Double?): WorkoutHistoryMetricDelta {
    if (current == null || previous == null) {
        return WorkoutHistoryMetricDelta(
            currentValue = current ?: 0.0,
            previousValue = previous ?: 0.0,
            deltaValue = 0.0,
            direction = WorkoutHistoryDeltaDirection.Unavailable
        )
    }
    return metricDelta(current, previous)
}
