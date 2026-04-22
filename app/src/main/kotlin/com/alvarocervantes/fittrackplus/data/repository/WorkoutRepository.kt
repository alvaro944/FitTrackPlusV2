package com.alvarocervantes.fittrackplus.data.repository

import com.alvarocervantes.fittrackplus.data.local.entity.WorkoutSessionEntity
import com.alvarocervantes.fittrackplus.data.local.relation.WorkoutSessionWithExercises
import com.alvarocervantes.fittrackplus.domain.model.RoutineDaySnapshot
import com.alvarocervantes.fittrackplus.domain.model.RoutineSnapshot
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    fun observeSessions(): Flow<List<WorkoutSessionEntity>>
    fun observeFinishedSessions(): Flow<List<WorkoutSessionEntity>>
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
}
