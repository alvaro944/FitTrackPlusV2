package com.alvarocervantes.fittrackplus.domain.usecase

import com.alvarocervantes.fittrackplus.data.local.entity.WorkoutSessionEntity
import com.alvarocervantes.fittrackplus.data.local.relation.WorkoutSessionWithExercises
import com.alvarocervantes.fittrackplus.data.repository.WorkoutRepository
import com.alvarocervantes.fittrackplus.domain.model.RoutineDaySnapshot
import com.alvarocervantes.fittrackplus.domain.model.RoutineSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReopenWorkoutSessionUseCaseTest {

    @Test
    fun reopensWhenNoSessionIsActive() = runBlocking {
        val repository = FakeReopenWorkoutRepository(activeSession = null)
        val useCase = ReopenWorkoutSessionUseCase(repository)

        val result = useCase(42)

        assertEquals(ReopenWorkoutSessionResult.Reopened, result)
        assertEquals(42L, repository.reopenedSessionId)
    }

    @Test
    fun blocksWhenAnotherSessionIsAlreadyActive() = runBlocking {
        val active = WorkoutSessionWithExercises(
            session = WorkoutSessionEntity(
                id = 7,
                routineId = 1,
                routineNameSnapshot = "PPL",
                routineDayId = 1,
                dayNameSnapshot = "Push",
                startedAt = 1000,
                weekNumber = 1
            ),
            exercises = emptyList()
        )
        val repository = FakeReopenWorkoutRepository(activeSession = active)
        val useCase = ReopenWorkoutSessionUseCase(repository)

        val result = useCase(42)

        assertEquals(ReopenWorkoutSessionResult.BlockedByActiveSession, result)
        assertNull(repository.reopenedSessionId)
    }
}

private class FakeReopenWorkoutRepository(
    private val activeSession: WorkoutSessionWithExercises?
) : WorkoutRepository {
    var reopenedSessionId: Long? = null

    override fun observeSessions(): Flow<List<WorkoutSessionEntity>> = flowOf(emptyList())
    override fun observeFinishedSessions(): Flow<List<WorkoutSessionEntity>> = flowOf(emptyList())
    override fun observeFinishedSessionsWithExercises(): Flow<List<WorkoutSessionWithExercises>> = flowOf(emptyList())
    override fun observeActiveSession(): Flow<WorkoutSessionWithExercises?> = flowOf(activeSession)

    override suspend fun getActiveSessionWithExercises(): WorkoutSessionWithExercises? = activeSession
    override suspend fun getSessionWithExercises(sessionId: Long): WorkoutSessionWithExercises? = null
    override suspend fun getFinishedSessionWithExercises(sessionId: Long): WorkoutSessionWithExercises? = null
    override suspend fun countFinishedSessionsForRoutine(routineId: Long): Int = 0
    override suspend fun countSessions(): Int = 0

    override suspend fun createSessionFromRoutineDay(
        routine: RoutineSnapshot,
        day: RoutineDaySnapshot,
        weekNumber: Int
    ): Long = error("Not used")

    override suspend fun updateSet(setId: Long, weightKg: Double, reps: Int) = Unit
    override suspend fun finishSession(sessionId: Long, notes: String?) = Unit

    override suspend fun reopenSession(sessionId: Long) {
        reopenedSessionId = sessionId
    }

    override suspend fun getLastWeightKgForExerciseSet(variantKey: String, setNumber: Int): Double? = null
    override suspend fun getLastRepsForExerciseSet(variantKey: String, setNumber: Int): Int? = null
    override suspend fun getMaxWeightForExercise(variantKey: String): Double? = null
    override suspend fun getMaxSetVolumeForExercise(variantKey: String): Double? = null
}
