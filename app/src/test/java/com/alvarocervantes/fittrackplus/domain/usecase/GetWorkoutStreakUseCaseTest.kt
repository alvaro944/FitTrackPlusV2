package com.alvarocervantes.fittrackplus.domain.usecase

import com.alvarocervantes.fittrackplus.data.local.entity.WorkoutSessionEntity
import com.alvarocervantes.fittrackplus.data.local.relation.WorkoutSessionWithExercises
import com.alvarocervantes.fittrackplus.data.repository.WorkoutRepository
import com.alvarocervantes.fittrackplus.domain.model.RoutineDaySnapshot
import com.alvarocervantes.fittrackplus.domain.model.RoutineSnapshot
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetWorkoutStreakUseCaseTest {

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun LocalDate.toMillis(): Long =
        atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun sessionOn(date: LocalDate, id: Long = date.toEpochDay()): WorkoutSessionEntity =
        WorkoutSessionEntity(
            id = id,
            routineId = 1L,
            routineNameSnapshot = "Test",
            routineDayId = 1L,
            dayNameSnapshot = "Day A",
            startedAt = date.toMillis(),
            finishedAt = date.toMillis() + 3_600_000L,
            weekNumber = 1
        )

    private fun repoWith(vararg sessions: WorkoutSessionEntity): WorkoutRepository =
        StreakWorkoutRepository(sessions.toList())

    // ── Tests ─────────────────────────────────────────────────────────────────

    @Test
    fun noSessions_streakIsZero() = runTest {
        val useCase = GetWorkoutStreakUseCase(repoWith())
        assertEquals(0, useCase())
    }

    @Test
    fun oneSessionToday_streakIsOne() = runTest {
        val today = LocalDate.now()
        val useCase = GetWorkoutStreakUseCase(repoWith(sessionOn(today)))
        assertEquals(1, useCase())
    }

    @Test
    fun oneSessionYesterday_streakIsOne() = runTest {
        val yesterday = LocalDate.now().minusDays(1)
        val useCase = GetWorkoutStreakUseCase(repoWith(sessionOn(yesterday)))
        assertEquals(1, useCase())
    }

    @Test
    fun oneSessionTwoDaysAgo_streakIsZero() = runTest {
        val twoDaysAgo = LocalDate.now().minusDays(2)
        val useCase = GetWorkoutStreakUseCase(repoWith(sessionOn(twoDaysAgo)))
        assertEquals(0, useCase())
    }

    @Test
    fun sevenConsecutiveDaysEndingToday_streakIsSeven() = runTest {
        val today = LocalDate.now()
        val sessions = (0L..6L).map { daysAgo -> sessionOn(today.minusDays(daysAgo), id = daysAgo) }
        val useCase = GetWorkoutStreakUseCase(repoWith(*sessions.toTypedArray()))
        assertEquals(7, useCase())
    }

    @Test
    fun gapBreaksStreak_onlyRecentDaysCounted() = runTest {
        val today = LocalDate.now()
        val sessions = listOf(
            sessionOn(today, id = 1),
            sessionOn(today.minusDays(1), id = 2),
            // gap: minusDays(2) missing
            sessionOn(today.minusDays(3), id = 3)
        )
        val useCase = GetWorkoutStreakUseCase(repoWith(*sessions.toTypedArray()))
        assertEquals(2, useCase())
    }

    @Test
    fun multipleSessionsSameDay_countedOnce() = runTest {
        val today = LocalDate.now()
        val sessions = listOf(
            sessionOn(today, id = 1),
            sessionOn(today, id = 2),
            sessionOn(today.minusDays(1), id = 3)
        )
        val useCase = GetWorkoutStreakUseCase(repoWith(*sessions.toTypedArray()))
        assertEquals(2, useCase())
    }

    // ── Fake repository ───────────────────────────────────────────────────────

    private class StreakWorkoutRepository(
        private val sessions: List<WorkoutSessionEntity>
    ) : WorkoutRepository {
        override fun observeSessions(): Flow<List<WorkoutSessionEntity>> = flowOf(sessions)
        override fun observeFinishedSessions(): Flow<List<WorkoutSessionEntity>> = flowOf(sessions)
        override fun observeFinishedSessionsWithExercises(): Flow<List<WorkoutSessionWithExercises>> =
            flowOf(emptyList())
        override fun observeActiveSession(): Flow<WorkoutSessionWithExercises?> = flowOf(null)
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
        override suspend fun getLastWeightKgForExerciseSet(exerciseName: String, setNumber: Int): Double? = null
        override suspend fun getMaxWeightForExercise(exerciseName: String): Double? = null
        override suspend fun getMaxSetVolumeForExercise(exerciseName: String): Double? = null
    }
}
