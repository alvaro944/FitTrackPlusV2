package com.alvarocervantes.fittrackplus.domain.usecase

import com.alvarocervantes.fittrackplus.data.local.entity.WorkoutSessionEntity
import com.alvarocervantes.fittrackplus.data.local.relation.WorkoutSessionWithExercises
import com.alvarocervantes.fittrackplus.data.repository.WorkoutRepository
import com.alvarocervantes.fittrackplus.domain.model.RoutineDaySnapshot
import com.alvarocervantes.fittrackplus.domain.model.RoutineSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveWorkoutHistoryUseCaseTest {

    @Test
    fun mapsFinishedSessionsFromSnapshots() = runBlocking {
        val repository = HistoryListWorkoutRepository(
            sessions = listOf(
                session(
                    id = 2,
                    routineName = "PPL Snapshot",
                    dayName = "Pull Snapshot",
                    startedAt = 200,
                    finishedAt = 260,
                    weekNumber = 2
                ),
                session(
                    id = 1,
                    routineName = "Old Routine Name",
                    dayName = "Push Snapshot",
                    startedAt = 100,
                    finishedAt = 150,
                    weekNumber = 1
                ),
                session(
                    id = 3,
                    routineName = "Open",
                    dayName = "Open Day",
                    startedAt = 300,
                    finishedAt = null,
                    weekNumber = 3
                )
            )
        )
        val useCase = ObserveWorkoutHistoryUseCase(repository)

        val summaries = useCase().first()

        assertEquals(2, summaries.size)
        assertEquals(2L, summaries[0].sessionId)
        assertEquals("PPL Snapshot", summaries[0].routineName)
        assertEquals("Pull Snapshot", summaries[0].dayName)
        assertEquals(260L, summaries[0].finishedAt)
        assertEquals("Old Routine Name", summaries[1].routineName)
    }

    private fun session(
        id: Long,
        routineName: String,
        dayName: String,
        startedAt: Long,
        finishedAt: Long?,
        weekNumber: Int
    ): WorkoutSessionEntity {
        return WorkoutSessionEntity(
            id = id,
            routineId = 99,
            routineNameSnapshot = routineName,
            routineDayId = 11,
            dayNameSnapshot = dayName,
            startedAt = startedAt,
            finishedAt = finishedAt,
            weekNumber = weekNumber
        )
    }
}

private class HistoryListWorkoutRepository(
    private val sessions: List<WorkoutSessionEntity>
) : WorkoutRepository {
    override fun observeSessions(): Flow<List<WorkoutSessionEntity>> = flowOf(sessions)
    override fun observeFinishedSessions(): Flow<List<WorkoutSessionEntity>> = flowOf(sessions)
    override fun observeFinishedSessionsWithExercises(): Flow<List<WorkoutSessionWithExercises>> = flowOf(emptyList())
    override suspend fun getActiveSessionWithExercises(): WorkoutSessionWithExercises? = null
    override suspend fun getSessionWithExercises(sessionId: Long): WorkoutSessionWithExercises? = null
    override suspend fun getFinishedSessionWithExercises(sessionId: Long): WorkoutSessionWithExercises? = null
    override suspend fun countFinishedSessionsForRoutine(routineId: Long): Int = 0
    override suspend fun countSessions(): Int = sessions.size
    override suspend fun createSessionFromRoutineDay(
        routine: RoutineSnapshot,
        day: RoutineDaySnapshot,
        weekNumber: Int
    ): Long = error("Not used")

    override suspend fun updateSet(setId: Long, weightKg: Double, reps: Int) = error("Not used")
    override suspend fun finishSession(sessionId: Long, notes: String?) = error("Not used")
}
