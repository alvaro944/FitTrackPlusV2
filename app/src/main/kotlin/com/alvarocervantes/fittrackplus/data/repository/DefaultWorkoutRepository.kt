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
import com.alvarocervantes.fittrackplus.domain.model.TargetRepsRange
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
                    exercise.toWorkoutExerciseEntity(
                        sessionId = sessionId,
                        activeVariant = activeVariant
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

    override suspend fun workoutExerciseHasLoggedSets(workoutExerciseId: Long): Boolean {
        workoutDao.getExercise(workoutExerciseId) ?: return false
        return hasRecordedData(workoutExerciseId)
    }

    private suspend fun hasRecordedData(workoutExerciseId: Long): Boolean {
        return workoutDao.getSetsForExercise(workoutExerciseId)
            .any { it.weightKg > 0.0 || it.reps > 0 || it.isCompleted }
    }

    override suspend fun replaceWorkoutExerciseVariant(
        workoutExerciseId: Long,
        variantKey: String,
        exerciseName: String,
        targetRepsText: String,
        targetSets: Int,
        notes: String?,
        keepLoggedSets: Boolean
    ): Boolean {
        return database.withTransaction {
            val workoutExercise = workoutDao.getExercise(workoutExerciseId) ?: return@withTransaction false

            val targetRange = TargetRepsRange.parse(targetRepsText)
            workoutDao.updateExercise(
                workoutExercise.copy(
                    performedVariantKey = variantKey,
                    exerciseNameSnapshot = exerciseName,
                    targetRepsSnapshot = targetRepsText,
                    notes = notes?.trim()?.ifBlank { null },
                    targetRepsMinSnapshot = targetRange?.min,
                    targetRepsMaxSnapshot = targetRange?.max
                )
            )

            // Relabelling an exercise you already trained must not throw the work away: the reps
            // happened, only the variant they were filed under was wrong. Rebuilding the sets from
            // the new prescription is only right when nothing has been logged yet.
            if (!keepLoggedSets) {
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
            }
            true
        }
    }

    override suspend fun updateSet(setId: Long, weightKg: Double, reps: Int) {
        val set = workoutDao.getSet(setId) ?: return
        workoutDao.updateSet(
            set.copy(
                weightKg = weightKg.coerceAtLeast(0.0),
                reps = reps.coerceAtLeast(0),
                isCompleted = false
            )
        )
    }

    override suspend fun updateSetCompletion(setId: Long, isCompleted: Boolean) {
        val set = workoutDao.getSet(setId) ?: return
        workoutDao.updateSet(set.copy(isCompleted = isCompleted))
    }

    override suspend fun updateSetNotes(setId: Long, notes: String?) {
        val set = workoutDao.getSet(setId) ?: return
        workoutDao.updateSet(set.copy(notes = notes?.trim()?.ifBlank { null }))
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

    override suspend fun reopenSession(sessionId: Long) {
        val session = workoutDao.getSession(sessionId) ?: return
        val finishedAt = session.finishedAt ?: return
        val pausedGap = (System.currentTimeMillis() - finishedAt).coerceAtLeast(0)
        workoutDao.updateSession(
            session.copy(
                finishedAt = null,
                pausedMillis = session.pausedMillis + pausedGap
            )
        )
    }

    override suspend fun getLastWeightKgForExerciseSet(variantKey: String, setNumber: Int): Double? {
        return workoutDao.getLastWeightKgForExerciseSet(variantKey, setNumber)
    }

    override suspend fun getLastRepsForExerciseSet(variantKey: String, setNumber: Int): Int? {
        return workoutDao.getLastRepsForExerciseSet(variantKey, setNumber)
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
    val targetRepsText: String,
    val notes: String?,
    val targetRepsMin: Int?,
    val targetRepsMax: Int?
)

internal fun RoutineExerciseSnapshot.toWorkoutExerciseEntity(sessionId: Long): WorkoutExerciseEntity {
    return toWorkoutExerciseEntity(
        sessionId = sessionId,
        activeVariant = activeVariant()
    )
}

private fun RoutineExerciseSnapshot.toWorkoutExerciseEntity(
    sessionId: Long,
    activeVariant: ActiveRoutineVariant
): WorkoutExerciseEntity {
    return WorkoutExerciseEntity(
        sessionId = sessionId,
        exerciseTemplateId = id,
        performedVariantKey = activeVariant.variantKey,
        exerciseNameSnapshot = activeVariant.name,
        targetRepsSnapshot = activeVariant.targetRepsText,
        notes = activeVariant.notes,
        position = position,
        targetRepsMinSnapshot = activeVariant.targetRepsMin,
        targetRepsMaxSnapshot = activeVariant.targetRepsMax
    )
}

private fun RoutineExerciseSnapshot.activeVariant(): ActiveRoutineVariant {
    val selectedAlternative = alternatives.firstOrNull { it.variantKey == defaultVariantKey }
    return if (selectedAlternative != null) {
        selectedAlternative.toActiveVariant()
    } else {
        ActiveRoutineVariant(
            variantKey = variantKey,
            name = name,
            targetSets = targetSets,
            targetRepsText = targetRepsText,
            notes = notes,
            targetRepsMin = targetRepsMin,
            targetRepsMax = targetRepsMax
        )
    }
}

private fun RoutineExerciseAlternativeSnapshot.toActiveVariant(): ActiveRoutineVariant {
    return ActiveRoutineVariant(
        variantKey = variantKey,
        name = name,
        targetSets = targetSets,
        targetRepsText = targetRepsText,
        notes = notes,
        targetRepsMin = targetRepsMin,
        targetRepsMax = targetRepsMax
    )
}
