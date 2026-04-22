package com.alvarocervantes.fittrackplus.data.repository

import androidx.room.withTransaction
import com.alvarocervantes.fittrackplus.core.database.FitTrackPlusDatabase
import com.alvarocervantes.fittrackplus.data.local.dao.WorkoutDao
import com.alvarocervantes.fittrackplus.data.local.entity.WorkoutExerciseEntity
import com.alvarocervantes.fittrackplus.data.local.entity.WorkoutSessionEntity
import com.alvarocervantes.fittrackplus.data.local.entity.WorkoutSetEntity
import com.alvarocervantes.fittrackplus.domain.model.RoutineDaySnapshot
import com.alvarocervantes.fittrackplus.domain.model.RoutineSnapshot
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class DefaultWorkoutRepository @Inject constructor(
    private val database: FitTrackPlusDatabase,
    private val workoutDao: WorkoutDao
) : WorkoutRepository {

    override fun observeSessions(): Flow<List<WorkoutSessionEntity>> {
        return workoutDao.observeSessions()
    }

    override suspend fun countFinishedSessionsForRoutine(routineId: Long): Int {
        return workoutDao.countFinishedSessionsForRoutine(routineId)
    }

    override suspend fun createSessionFromRoutineDay(
        routine: RoutineSnapshot,
        day: RoutineDaySnapshot,
        weekNumber: Int
    ): Long {
        val startedAt = System.currentTimeMillis()
        return database.withTransaction {
            val sessionId = workoutDao.insertSession(
                WorkoutSessionEntity(
                    routineId = routine.id,
                    routineNameSnapshot = routine.name,
                    routineDayId = day.id,
                    dayNameSnapshot = day.name,
                    startedAt = startedAt,
                    weekNumber = weekNumber
                )
            )

            day.exercises.forEach { exercise ->
                val workoutExerciseId = workoutDao.insertExercise(
                    WorkoutExerciseEntity(
                        sessionId = sessionId,
                        exerciseTemplateId = exercise.id,
                        exerciseNameSnapshot = exercise.name,
                        targetRepsSnapshot = exercise.targetRepsText,
                        position = exercise.position
                    )
                )

                repeat(exercise.targetSets) { setIndex ->
                    workoutDao.insertSet(
                        WorkoutSetEntity(
                            workoutExerciseId = workoutExerciseId,
                            setNumber = setIndex + 1,
                            weightKg = 0.0,
                            reps = 0
                        )
                    )
                }
            }

            sessionId
        }
    }

    override suspend fun finishSession(sessionId: Long, notes: String?) {
        val session = workoutDao.getSession(sessionId) ?: return
        workoutDao.updateSession(
            session.copy(
                finishedAt = System.currentTimeMillis(),
                notes = notes?.trim()?.ifBlank { null }
            )
        )
    }
}
