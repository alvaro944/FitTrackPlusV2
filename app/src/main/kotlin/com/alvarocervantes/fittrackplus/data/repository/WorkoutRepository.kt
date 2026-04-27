package com.alvarocervantes.fittrackplus.data.repository

import com.alvarocervantes.fittrackplus.data.local.entity.WorkoutSessionEntity
import com.alvarocervantes.fittrackplus.data.local.relation.WorkoutSessionWithExercises
import com.alvarocervantes.fittrackplus.domain.model.RoutineDaySnapshot
import com.alvarocervantes.fittrackplus.domain.model.RoutineSnapshot
import kotlinx.coroutines.flow.Flow

@Suppress("TooManyFunctions")
interface WorkoutRepository {
    fun observeSessions(): Flow<List<WorkoutSessionEntity>>
    fun observeFinishedSessions(): Flow<List<WorkoutSessionEntity>>
    fun observeFinishedSessionsWithExercises(): Flow<List<WorkoutSessionWithExercises>>
    fun observeActiveSession(): Flow<WorkoutSessionWithExercises?>
    suspend fun getActiveSessionWithExercises(): WorkoutSessionWithExercises?
    suspend fun getSessionWithExercises(sessionId: Long): WorkoutSessionWithExercises?
    suspend fun getFinishedSessionWithExercises(sessionId: Long): WorkoutSessionWithExercises?
    suspend fun countFinishedSessionsForRoutine(routineId: Long): Int
    suspend fun countSessions(): Int
    suspend fun createSessionFromRoutineDay(
        routine: RoutineSnapshot,
        day: RoutineDaySnapshot,
        weekNumber: Int
    ): Long
    suspend fun updateSet(setId: Long, weightKg: Double, reps: Int)
    suspend fun finishSession(sessionId: Long, notes: String? = null)
    suspend fun getLastWeightKgForExerciseSet(exerciseName: String, setNumber: Int): Double?
    suspend fun getMaxWeightForExercise(exerciseName: String): Double?
    suspend fun getMaxSetVolumeForExercise(exerciseName: String): Double?
}
