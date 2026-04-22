package com.alvarocervantes.fittrackplus.data.repository

import com.alvarocervantes.fittrackplus.domain.model.RoutineDraft
import com.alvarocervantes.fittrackplus.domain.model.RoutineSnapshot
import com.alvarocervantes.fittrackplus.domain.model.RoutineSummary
import kotlinx.coroutines.flow.Flow

interface RoutineRepository {
    fun observeRoutines(): Flow<List<RoutineSummary>>
    suspend fun getRoutineSnapshot(routineId: Long): RoutineSnapshot?
    suspend fun createRoutine(draft: RoutineDraft): Long
    suspend fun replaceRoutine(routineId: Long, draft: RoutineDraft)
    suspend fun archiveRoutine(routineId: Long)
}
