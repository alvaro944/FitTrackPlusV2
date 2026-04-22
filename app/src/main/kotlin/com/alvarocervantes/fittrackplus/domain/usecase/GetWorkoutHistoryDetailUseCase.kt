package com.alvarocervantes.fittrackplus.domain.usecase

import com.alvarocervantes.fittrackplus.data.local.relation.WorkoutSessionWithExercises
import com.alvarocervantes.fittrackplus.data.repository.WorkoutRepository
import com.alvarocervantes.fittrackplus.domain.model.WorkoutHistoryDetail
import com.alvarocervantes.fittrackplus.domain.model.WorkoutHistoryExercise
import com.alvarocervantes.fittrackplus.domain.model.WorkoutHistorySet
import javax.inject.Inject

class GetWorkoutHistoryDetailUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    suspend operator fun invoke(sessionId: Long): WorkoutHistoryDetail? {
        return workoutRepository.getFinishedSessionWithExercises(sessionId)?.toHistoryDetail()
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
