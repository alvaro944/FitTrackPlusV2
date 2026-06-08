package com.alvarocervantes.fittrackplus.domain.usecase

import app.cash.turbine.test
import com.alvarocervantes.fittrackplus.data.local.entity.WorkoutExerciseEntity
import com.alvarocervantes.fittrackplus.data.local.entity.WorkoutSessionEntity
import com.alvarocervantes.fittrackplus.data.local.entity.WorkoutSetEntity
import com.alvarocervantes.fittrackplus.data.local.relation.WorkoutExerciseWithSets
import com.alvarocervantes.fittrackplus.data.local.relation.WorkoutSessionWithExercises
import com.alvarocervantes.fittrackplus.data.repository.WorkoutRepository
import com.alvarocervantes.fittrackplus.domain.model.RoutineDaySnapshot
import com.alvarocervantes.fittrackplus.domain.model.RoutineSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private const val DAY_MS = 86_400_000L
private const val NOW_MS = 1_700_000_000_000L

class GetWorkoutHeatmapUseCaseTest {

    @Test
    fun emptyDatabase_returns365DaysAllLevelZero() = runTest {
        val repo = HeatmapWorkoutRepository(emptyList())
        val useCase = GetWorkoutHeatmapUseCase(repo)

        useCase(nowMillis = NOW_MS).test {
            val days = awaitItem()
            assertEquals(365, days.size)
            assertTrue(days.all { it.intensityLevel == 0 })
            assertTrue(days.all { it.totalVolumeKg == 0.0 })
            awaitComplete()
        }
    }

    @Test
    fun singleSessionToday_producesOneDayWithLevelOneAndCorrectVolume() = runTest {
        // 80kg * 5 reps = 400kg volumen
        val repo = HeatmapWorkoutRepository(
            listOf(
                session(
                    sessionId = 1,
                    finishedAt = NOW_MS - DAY_MS,  // ayer, dentro del año
                    sets = listOf(set(weightKg = 80.0, reps = 5))
                )
            )
        )
        val useCase = GetWorkoutHeatmapUseCase(repo)

        useCase(nowMillis = NOW_MS).test {
            val days = awaitItem()
            val activeDays = days.filter { it.intensityLevel > 0 }
            assertEquals(1, activeDays.size)
            assertEquals(400.0, activeDays.first().totalVolumeKg, 0.001)
            assertEquals(1, activeDays.first().intensityLevel)
            awaitComplete()
        }
    }

    @Test
    fun twoSessionsSameDay_volumeIsAggregated() = runTest {
        val dayMs = NOW_MS - DAY_MS * 5
        val repo = HeatmapWorkoutRepository(
            listOf(
                session(sessionId = 1, finishedAt = dayMs,             sets = listOf(set(weightKg = 100.0, reps = 3))),
                session(sessionId = 2, finishedAt = dayMs + 3_600_000, sets = listOf(set(weightKg = 50.0,  reps = 10)))
            )
        )
        val useCase = GetWorkoutHeatmapUseCase(repo)

        useCase(nowMillis = NOW_MS).test {
            val days = awaitItem()
            val active = days.filter { it.intensityLevel > 0 }
            // Ambas sesiones caen en el mismo epoch day
            assertEquals(1, active.size)
            assertEquals(800.0, active.first().totalVolumeKg, 0.001)  // 300 + 500
            awaitComplete()
        }
    }

    @Test
    fun sessionOlderThan365Days_isExcluded() = runTest {
        val repo = HeatmapWorkoutRepository(
            listOf(
                session(
                    sessionId = 1,
                    finishedAt = NOW_MS - 365 * DAY_MS - 1,  // un ms fuera del rango
                    sets = listOf(set(weightKg = 100.0, reps = 10))
                )
            )
        )
        val useCase = GetWorkoutHeatmapUseCase(repo)

        useCase(nowMillis = NOW_MS).test {
            val days = awaitItem()
            assertTrue(days.all { it.intensityLevel == 0 })
            awaitComplete()
        }
    }

    @Test
    fun openSession_isIgnored() = runTest {
        val repo = HeatmapWorkoutRepository(
            listOf(
                session(sessionId = 1, finishedAt = null, sets = listOf(set(weightKg = 200.0, reps = 10)))
            )
        )
        val useCase = GetWorkoutHeatmapUseCase(repo)

        useCase(nowMillis = NOW_MS).test {
            val days = awaitItem()
            assertTrue(days.all { it.intensityLevel == 0 })
            awaitComplete()
        }
    }

    @Test
    fun fourSessionsDifferentDays_quartilesAssignLevels1To4() = runTest {
        // Cuatro sesiones en días distintos con volúmenes muy distintos → 4 niveles distintos
        val repo = HeatmapWorkoutRepository(
            listOf(
                session(sessionId = 1, finishedAt = NOW_MS - DAY_MS * 4, sets = listOf(set(weightKg = 10.0,  reps = 1))),   // 10 kg  → nivel 1
                session(sessionId = 2, finishedAt = NOW_MS - DAY_MS * 3, sets = listOf(set(weightKg = 50.0,  reps = 1))),   // 50 kg  → nivel 2
                session(sessionId = 3, finishedAt = NOW_MS - DAY_MS * 2, sets = listOf(set(weightKg = 100.0, reps = 1))),   // 100 kg → nivel 3
                session(sessionId = 4, finishedAt = NOW_MS - DAY_MS * 1, sets = listOf(set(weightKg = 500.0, reps = 1)))    // 500 kg → nivel 4
            )
        )
        val useCase = GetWorkoutHeatmapUseCase(repo)

        useCase(nowMillis = NOW_MS).test {
            val days = awaitItem()
            val active = days.filter { it.intensityLevel > 0 }.sortedBy { it.totalVolumeKg }
            assertEquals(4, active.size)
            // El orden de niveles debe ser creciente con el volumen
            assertTrue(active.zipWithNext().all { (a, b) -> a.intensityLevel <= b.intensityLevel })
            awaitComplete()
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun session(
        sessionId: Long,
        finishedAt: Long?,
        sets: List<WorkoutSetEntity>
    ) = WorkoutSessionWithExercises(
        session = WorkoutSessionEntity(
            id = sessionId,
            routineId = 1,
            routineNameSnapshot = "Test",
            routineDayId = 1,
            dayNameSnapshot = "Day",
            startedAt = (finishedAt ?: 0L) - 3_600_000L,
            finishedAt = finishedAt,
            weekNumber = 1
        ),
        exercises = listOf(
            WorkoutExerciseWithSets(
                exercise = WorkoutExerciseEntity(
                    id = sessionId * 100,
                    sessionId = sessionId,
                    exerciseTemplateId = 1,
                    exerciseNameSnapshot = "Exercise",
                    targetRepsSnapshot = "8-12",
                    position = 0
                ),
                sets = sets
            )
        )
    )

    private fun set(weightKg: Double, reps: Int) = WorkoutSetEntity(
        id = 0,
        workoutExerciseId = 0,
        setNumber = 1,
        weightKg = weightKg,
        reps = reps
    )
}

private class HeatmapWorkoutRepository(
    private val sessions: List<WorkoutSessionWithExercises>
) : WorkoutRepository {
    override fun observeSessions(): Flow<List<WorkoutSessionEntity>> = flowOf(emptyList())
    override fun observeFinishedSessions(): Flow<List<WorkoutSessionEntity>> = flowOf(emptyList())
    override fun observeFinishedSessionsWithExercises(): Flow<List<WorkoutSessionWithExercises>> = flowOf(sessions)
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
