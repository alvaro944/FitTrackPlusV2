package com.alvarocervantes.fittrackplus.data.repository

import androidx.room.withTransaction
import com.alvarocervantes.fittrackplus.core.database.FitTrackPlusDatabase
import com.alvarocervantes.fittrackplus.data.local.dao.WorkoutDao
import com.alvarocervantes.fittrackplus.data.local.entity.WorkoutExerciseEntity
import com.alvarocervantes.fittrackplus.data.local.entity.WorkoutSessionEntity
import com.alvarocervantes.fittrackplus.data.local.entity.WorkoutSetEntity
import com.alvarocervantes.fittrackplus.data.local.relation.WorkoutSessionWithExercises
import com.alvarocervantes.fittrackplus.domain.model.RoutineDaySnapshot
import com.alvarocervantes.fittrackplus.domain.model.RoutineExerciseAlternativeSnapshot
import com.alvarocervantes.fittrackplus.domain.model.RoutineExerciseSnapshot
import com.alvarocervantes.fittrackplus.domain.model.RoutineSnapshot
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DefaultWorkoutRepository @Inject constructor(
    private val database: FitTrackPlusDatabase,
    private val workoutDao: WorkoutDao
) : WorkoutRepository {

    override fun observeSessions(): Flow<List<WorkoutSessionEntity>> {
        return workoutDao.observeSessions()
    }

    override fun observeFinishedSessions(): Flow<List<WorkoutSessionEntity>> {
        return workoutDao.observeFinishedSessions()
    }

    override fun observeFinishedSessionsWithExercises(): Flow<List<WorkoutSessionWithExercises>> {
        return workoutDao.observeFinishedSessionsWithExercises()
    }

    override fun observeActiveSession(): Flow<WorkoutSessionWithExercises?> {
        return workoutDao.observeActiveSession().map { it.firstOrNull() }
    }

    override suspend fun getActiveSessionWithExercises(): WorkoutSessionWithExercises? {
        return workoutDao.getActiveSessionWithExercises()
    }

    override suspend fun getSessionWithExercises(sessionId: Long): WorkoutSessionWithExercises? {
        return workoutDao.getSessionWithExercises(sessionId)
    }

    override suspend fun getFinishedSessionWithExercises(sessionId: Long): WorkoutSessionWithExercises? {
        return workoutDao.getFinishedSessionWithExercises(sessionId)
    }

    override suspend fun countFinishedSessionsForRoutine(routineId: Long): Int {
        return workoutDao.countFinishedSessionsForRoutine(routineId)
    }

    override suspend fun countSessions(): Int {
        return workoutDao.countSessions()
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
                val activeVariant = exercise.activeVariant()
                val workoutExerciseId = workoutDao.insertExercise(
                    WorkoutExerciseEntity(
                        sessionId = sessionId,
                        exerciseTemplateId = exercise.id,
                        performedVariantKey = activeVariant.variantKey,
                        exerciseNameSnapshot = activeVariant.name,
                        targetRepsSnapshot = activeVariant.targetRepsText,
                        position = exercise.position
                    )
                )

                repeat(activeVariant.targetSets) { setIndex ->
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

    override suspend fun replaceWorkoutExerciseVariant(
        workoutExerciseId: Long,
        variantKey: String,
        exerciseName: String,
        targetRepsText: String,
        targetSets: Int
    ): Boolean {
        return database.withTransaction {
            val workoutExercise = workoutDao.getExercise(workoutExerciseId) ?: return@withTransaction false
            val currentSets = workoutDao.getSetsForExercise(workoutExerciseId)
            val hasRecordedData = currentSets.any { it.weightKg > 0.0 || it.reps > 0 }
            if (hasRecordedData) {
                return@withTransaction false
            }

            workoutDao.updateExercise(
                workoutExercise.copy(
                    performedVariantKey = variantKey,
                    exerciseNameSnapshot = exerciseName,
                    targetRepsSnapshot = targetRepsText
                )
            )
            workoutDao.deleteSetsForExercise(workoutExerciseId)
            repeat(targetSets) { setIndex ->
                workoutDao.insertSet(
                    WorkoutSetEntity(
                        workoutExerciseId = workoutExerciseId,
                        setNumber = setIndex + 1,
                        weightKg = 0.0,
                        reps = 0
                    )
                )
            }
            true
        }
    }

    override suspend fun updateSet(setId: Long, weightKg: Double, reps: Int) {
        val set = workoutDao.getSet(setId) ?: return
        workoutDao.updateSet(
            set.copy(
                weightKg = weightKg.coerceAtLeast(0.0),
                reps = reps.coerceAtLeast(0)
            )
        )
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

    override suspend fun discardSession(sessionId: Long) {
        workoutDao.deleteSession(sessionId)
    }

    override suspend fun getLastWeightKgForExerciseSet(variantKey: String, setNumber: Int): Double? {
        return workoutDao.getLastWeightKgForExerciseSet(variantKey, setNumber)
    }

    override suspend fun getMaxWeightForExercise(variantKey: String): Double? {
        return workoutDao.getMaxWeightForExercise(variantKey)
    }

    override suspend fun getMaxSetVolumeForExercise(variantKey: String): Double? {
        return workoutDao.getMaxSetVolumeForExercise(variantKey)
    }

    override suspend fun getRecentAverageRepsForExercise(variantKey: String, limit: Int): List<Double> {
        return workoutDao.getRecentAverageRepsForExercise(variantKey, limit)
    }
}

private data class ActiveRoutineVariant(
    val variantKey: String,
    val name: String,
    val targetSets: Int,
    val targetRepsText: String
)

private fun RoutineExerciseSnapshot.activeVariant(): ActiveRoutineVariant {
    val selectedAlternative = alternatives.firstOrNull { it.variantKey == defaultVariantKey }
    return if (selectedAlternative != null) {
        selectedAlternative.toActiveVariant()
    } else {
        ActiveRoutineVariant(
            variantKey = variantKey,
            name = name,
            targetSets = targetSets,
            targetRepsText = targetRepsText
        )
    }
}

private fun RoutineExerciseAlternativeSnapshot.toActiveVariant(): ActiveRoutineVariant {
    return ActiveRoutineVariant(
        variantKey = variantKey,
        name = name,
        targetSets = targetSets,
        targetRepsText = targetRepsText
    )
}
