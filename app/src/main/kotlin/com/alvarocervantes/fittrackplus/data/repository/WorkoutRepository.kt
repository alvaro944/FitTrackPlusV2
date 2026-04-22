package com.alvarocervantes.fittrackplus.data.repository

import com.alvarocervantes.fittrackplus.data.local.entity.WorkoutSessionEntity
import com.alvarocervantes.fittrackplus.domain.model.RoutineDaySnapshot
import com.alvarocervantes.fittrackplus.domain.model.RoutineSnapshot
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    fun observeSessions(): Flow<List<WorkoutSessionEntity>>
    suspend fun countFinishedSessionsForRoutine(routineId: Long): Int
    suspend fun createSessionFromRoutineDay(
        routine: RoutineSnapshot,
        day: RoutineDaySnapshot,
        weekNumber: Int
    ): Long
    suspend fun finishSession(sessionId: Long, notes: String? = null)
}
