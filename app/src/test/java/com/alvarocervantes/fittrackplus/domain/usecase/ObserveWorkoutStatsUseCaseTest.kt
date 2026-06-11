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
import com.alvarocervantes.fittrackplus.domain.model.WorkoutStatsPeriod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ObserveWorkoutStatsUseCaseTest {

    @Test
    fun calculatesVolumesProgressAndRecordsFromFinishedSnapshotSessions() = runTest {
        val repository = StatsWorkoutRepository(
            sessions = listOf(
                sessionWithExercises(
                    sessionId = 1,
                    routineName = "PPL Snapshot",
                    dayName = "Push",
                    startedAt = 90,
                    finishedAt = 100,
                    exercises = listOf(
                        exercise(
                            id = 11,
                            sessionId = 1,
                            name = "Bench Press",
                            position = 0,
                            sets = listOf(
                                set(id = 101, exerciseId = 11, setNumber = 1, weightKg = 100.0, reps = 5),
                                set(id = 102, exerciseId = 11, setNumber = 2, weightKg = 90.0, reps = 8)
                            )
                        ),
                        exercise(
                            id = 12,
                            sessionId = 1,
                            name = "Curl",
                            position = 1,
                            sets = listOf(
                                set(id = 103, exerciseId = 12, setNumber = 1, weightKg = 10.0, reps = 12)
                            )
                        )
                    )
                ),
                sessionWithExercises(
                    sessionId = 2,
                    routineName = "PPL Snapshot",
                    dayName = "Push",
                    startedAt = 190,
                    finishedAt = 200,
                    exercises = listOf(
                        exercise(
                            id = 21,
                            sessionId = 2,
                            name = " bench press ",
                            position = 0,
                            sets = listOf(
                                set(id = 201, exerciseId = 21, setNumber = 1, weightKg = 102.5, reps = 4),
                                set(id = 202, exerciseId = 21, setNumber = 2, weightKg = 80.0, reps = 10)
                            )
                        ),
                        exercise(
                            id = 22,
                            sessionId = 2,
                            name = "Pull Ups",
                            position = 1,
                            sets = listOf(
                                set(id = 203, exerciseId = 22, setNumber = 1, weightKg = 0.0, reps = 12),
                                set(id = 204, exerciseId = 22, setNumber = 2, weightKg = 0.0, reps = 10)
                            )
                        )
                    )
                ),
                sessionWithExercises(
                    sessionId = 3,
                    routineName = "Open",
                    dayName = "Open",
                    startedAt = 300,
                    finishedAt = null,
                    exercises = listOf(
                        exercise(
                            id = 31,
                            sessionId = 3,
                            name = "Bench Press",
                            position = 0,
                            sets = listOf(
                                set(id = 301, exerciseId = 31, setNumber = 1, weightKg = 200.0, reps = 20)
                            )
                        )
                    )
                )
            )
        )
        val useCase = ObserveWorkoutStatsUseCase(repository)

        useCase().test {
            val stats = awaitItem()

            // Session volumes — ordered newest first
            assertEquals(2, stats.sessionVolumes.size)
            assertEquals(2L, stats.sessionVolumes[0].sessionId)
            assertEquals(1210.0, stats.sessionVolumes[0].totalVolumeKg, 0.0)
            assertEquals(1L, stats.sessionVolumes[1].sessionId)
            assertEquals(1340.0, stats.sessionVolumes[1].totalVolumeKg, 0.0)

            // Bench Press progress — normalised key groups " bench press " with "Bench Press"
            val benchProgress = stats.exerciseProgress.single { it.exerciseKey == "bench press" }
            assertEquals("Bench Press", benchProgress.exerciseName)
            assertEquals(2, benchProgress.entries.size)
            assertEquals(1L, benchProgress.entries[0].sessionId)
            assertEquals(1220.0, benchProgress.entries[0].volumeKg, 0.0)
            assertEquals(13, benchProgress.entries[0].totalReps)
            assertEquals(116.666, benchProgress.entries[0].estimatedOneRepMaxKg, 0.01)
            assertEquals(2L, benchProgress.entries[1].sessionId)
            assertEquals(1210.0, benchProgress.entries[1].volumeKg, 0.0)
            assertEquals(14, benchProgress.entries[1].totalReps)

            // Bench Press records
            val benchRecords = stats.exerciseRecords.single { it.exerciseKey == "bench press" }
            assertEquals(102.5, benchRecords.maxWeight?.weightKg ?: -1.0, 0.0)
            assertEquals(10, benchRecords.maxReps?.reps)
            assertEquals(800.0, benchRecords.bestSetVolume?.setVolumeKg ?: -1.0, 0.0)
            assertEquals(116.666, benchRecords.bestEstimatedOneRepMax?.estimatedOneRepMaxKg ?: -1.0, 0.01)

            // Open session must NOT appear in any list
            assertEquals(0, stats.sessionVolumes.count { it.sessionId == 3L })
            assertNull(stats.exerciseProgress.firstOrNull { it.exerciseName == "Open" })

            awaitComplete()
        }
    }

    @Test
    fun handlesZeroWeightExerciseRecordsWithoutWeightedMarks() = runTest {
        val repository = StatsWorkoutRepository(
            sessions = listOf(
                sessionWithExercises(
                    sessionId = 4,
                    routineName = "Bodyweight",
                    dayName = "Pull",
                    startedAt = 400,
                    finishedAt = 450,
                    exercises = listOf(
                        exercise(
                            id = 41,
                            sessionId = 4,
                            name = "Pull Ups",
                            position = 0,
                            sets = listOf(
                                set(id = 401, exerciseId = 41, setNumber = 1, weightKg = 0.0, reps = 12),
                                set(id = 402, exerciseId = 41, setNumber = 2, weightKg = 0.0, reps = 0)
                            )
                        )
                    )
                )
            )
        )
        val useCase = ObserveWorkoutStatsUseCase(repository)

        useCase().test {
            val stats = awaitItem()

            assertEquals(0.0, stats.sessionVolumes.single().totalVolumeKg, 0.0)
            val records = stats.exerciseRecords.single()
            assertEquals(12, records.maxReps?.reps)
            assertNull(records.maxWeight)
            assertNull(records.bestSetVolume)
            assertNull(records.bestEstimatedOneRepMax)
            assertNotNull(stats.exerciseProgress.single().entries.single())

            awaitComplete()
        }
    }

    @Test
    fun emptySessionList_producesEmptyStats() = runTest {
        val repository = StatsWorkoutRepository(sessions = emptyList())
        val useCase = ObserveWorkoutStatsUseCase(repository)

        useCase().test {
            val stats = awaitItem()
            assertEquals(0, stats.sessionVolumes.size)
            assertEquals(0, stats.exerciseProgress.size)
            assertEquals(0, stats.exerciseRecords.size)
            awaitComplete()
        }
    }

    @Test
    fun allPeriod_includesAllFinishedSessionsAndPreservesSnapshots() = runTest {
        val repository = StatsWorkoutRepository(
            sessions = statsPeriodSessions(nowMillis = NOW_MILLIS)
        )
        val useCase = ObserveWorkoutStatsUseCase(repository)

        useCase(
            period = WorkoutStatsPeriod.All,
            nowMillis = NOW_MILLIS
        ).test {
            val stats = awaitItem()

            assertEquals(listOf(3L, 2L, 1L), stats.sessionVolumes.map { it.sessionId })
            assertEquals("Legacy Snapshot", stats.sessionVolumes.last().routineName)
            assertEquals("Old Push", stats.sessionVolumes.last().dayName)
            assertNull(stats.sessionVolumes.firstOrNull { it.sessionId == 4L })

            awaitComplete()
        }
    }

    @Test
    fun lastFourWeeks_excludesOlderSessionsAndRecalculatesRecordsWithinPeriod() = runTest {
        val repository = StatsWorkoutRepository(
            sessions = statsPeriodSessions(nowMillis = NOW_MILLIS)
        )
        val useCase = ObserveWorkoutStatsUseCase(repository)

        useCase(
            period = WorkoutStatsPeriod.LastFourWeeks,
            nowMillis = NOW_MILLIS
        ).test {
            val stats = awaitItem()

            assertEquals(listOf(3L, 2L), stats.sessionVolumes.map { it.sessionId })
            val benchRecords = stats.exerciseRecords.single { it.exerciseKey == "bench press" }
            assertEquals(90.0, benchRecords.maxWeight?.weightKg ?: -1.0, 0.0)
            assertEquals(12, benchRecords.maxReps?.reps)
            assertEquals(960.0, benchRecords.bestSetVolume?.setVolumeKg ?: -1.0, 0.0)

            awaitComplete()
        }
    }

    @Test
    fun lastTwelveWeeks_includesSessionsInsideTwelveWeekCutoff() = runTest {
        val repository = StatsWorkoutRepository(
            sessions = statsPeriodSessions(nowMillis = NOW_MILLIS)
        )
        val useCase = ObserveWorkoutStatsUseCase(repository)

        useCase(
            period = WorkoutStatsPeriod.LastTwelveWeeks,
            nowMillis = NOW_MILLIS
        ).test {
            val stats = awaitItem()

            assertEquals(listOf(3L, 2L, 1L), stats.sessionVolumes.map { it.sessionId })
            val benchRecords = stats.exerciseRecords.single { it.exerciseKey == "bench press" }
            assertEquals(140.0, benchRecords.maxWeight?.weightKg ?: -1.0, 0.0)

            awaitComplete()
        }
    }

    @Test
    fun finishedSessionsWithoutCompletedSets_areExcludedFromAllStats() = runTest {
        val repository = StatsWorkoutRepository(
            sessions = listOf(
                sessionWithExercises(
                    sessionId = 10,
                    routineName = "Push",
                    dayName = "Dia 1",
                    startedAt = 1_000,
                    finishedAt = 2_000,
                    exercises = listOf(
                        exercise(
                            id = 101,
                            sessionId = 10,
                            name = "Bench Press",
                            position = 0,
                            sets = listOf(
                                set(id = 1001, exerciseId = 101, setNumber = 1, weightKg = 0.0, reps = 0),
                                set(id = 1002, exerciseId = 101, setNumber = 2, weightKg = 20.0, reps = 0)
                            )
                        )
                    )
                ),
                sessionWithExercises(
                    sessionId = 11,
                    routineName = "Push",
                    dayName = "Dia 4",
                    startedAt = 4_000,
                    finishedAt = 5_000,
                    exercises = listOf(
                        exercise(
                            id = 111,
                            sessionId = 11,
                            name = "Bench Press",
                            position = 0,
                            sets = listOf(
                                set(id = 1101, exerciseId = 111, setNumber = 1, weightKg = 60.0, reps = 8)
                            )
                        )
                    )
                ),
                sessionWithExercises(
                    sessionId = 12,
                    routineName = "Pull",
                    dayName = "Dia 7",
                    startedAt = 7_000,
                    finishedAt = 8_000,
                    exercises = listOf(
                        exercise(
                            id = 121,
                            sessionId = 12,
                            name = "Pull Ups",
                            position = 0,
                            sets = listOf(
                                set(id = 1201, exerciseId = 121, setNumber = 1, weightKg = 0.0, reps = 10)
                            )
                        )
                    )
                )
            )
        )
        val useCase = ObserveWorkoutStatsUseCase(repository)

        useCase().test {
            val stats = awaitItem()

            assertEquals(listOf(12L, 11L), stats.sessionVolumes.map { it.sessionId })
            assertEquals(2, stats.exerciseProgress.flatMap { it.entries }.size)
            assertEquals(0, stats.exerciseProgress.flatMap { it.entries }.count { it.sessionId == 10L })
            assertEquals(0, stats.exerciseRecords.count { it.exerciseName == "Bench Press" && it.maxReps?.sessionId == 10L })

            awaitComplete()
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun sessionWithExercises(
        sessionId: Long,
        routineName: String,
        dayName: String,
        startedAt: Long,
        finishedAt: Long?,
        exercises: List<WorkoutExerciseWithSets>
    ): WorkoutSessionWithExercises = WorkoutSessionWithExercises(
        session = WorkoutSessionEntity(
            id = sessionId,
            routineId = 1,
            routineNameSnapshot = routineName,
            routineDayId = 1,
            dayNameSnapshot = dayName,
            startedAt = startedAt,
            finishedAt = finishedAt,
            weekNumber = 1
        ),
        exercises = exercises
    )

    private fun exercise(
        id: Long,
        sessionId: Long,
        name: String,
        position: Int,
        sets: List<WorkoutSetEntity>
    ): WorkoutExerciseWithSets = WorkoutExerciseWithSets(
        exercise = WorkoutExerciseEntity(
            id = id,
            sessionId = sessionId,
            exerciseTemplateId = id + 100,
            exerciseNameSnapshot = name,
            targetRepsSnapshot = "8-10",
            position = position
        ),
        sets = sets
    )

    private fun set(
        id: Long,
        exerciseId: Long,
        setNumber: Int,
        weightKg: Double,
        reps: Int
    ): WorkoutSetEntity = WorkoutSetEntity(
        id = id,
        workoutExerciseId = exerciseId,
        setNumber = setNumber,
        weightKg = weightKg,
        reps = reps
    )

    private fun statsPeriodSessions(nowMillis: Long): List<WorkoutSessionWithExercises> {
        val dayMillis = 86_400_000L
        return listOf(
            sessionWithExercises(
                sessionId = 1,
                routineName = "Legacy Snapshot",
                dayName = "Old Push",
                startedAt = nowMillis - dayMillis * 60 - 1_000,
                finishedAt = nowMillis - dayMillis * 60,
                exercises = listOf(
                    exercise(
                        id = 101,
                        sessionId = 1,
                        name = "Bench Press",
                        position = 0,
                        sets = listOf(
                            set(id = 1001, exerciseId = 101, setNumber = 1, weightKg = 140.0, reps = 3)
                        )
                    )
                )
            ),
            sessionWithExercises(
                sessionId = 2,
                routineName = "Current Snapshot",
                dayName = "Push",
                startedAt = nowMillis - dayMillis * 14 - 1_000,
                finishedAt = nowMillis - dayMillis * 14,
                exercises = listOf(
                    exercise(
                        id = 201,
                        sessionId = 2,
                        name = "Bench Press",
                        position = 0,
                        sets = listOf(
                            set(id = 2001, exerciseId = 201, setNumber = 1, weightKg = 90.0, reps = 10)
                        )
                    )
                )
            ),
            sessionWithExercises(
                sessionId = 3,
                routineName = "Current Snapshot",
                dayName = "Push",
                startedAt = nowMillis - dayMillis * 2 - 1_000,
                finishedAt = nowMillis - dayMillis * 2,
                exercises = listOf(
                    exercise(
                        id = 301,
                        sessionId = 3,
                        name = "Bench Press",
                        position = 0,
                        sets = listOf(
                            set(id = 3001, exerciseId = 301, setNumber = 1, weightKg = 80.0, reps = 12)
                        )
                    )
                )
            ),
            sessionWithExercises(
                sessionId = 4,
                routineName = "Open",
                dayName = "Open",
                startedAt = nowMillis - dayMillis,
                finishedAt = null,
                exercises = listOf(
                    exercise(
                        id = 401,
                        sessionId = 4,
                        name = "Bench Press",
                        position = 0,
                        sets = listOf(
                            set(id = 4001, exerciseId = 401, setNumber = 1, weightKg = 200.0, reps = 10)
                        )
                    )
                )
            )
        )
    }

    private companion object {
        const val NOW_MILLIS = 1_700_000_000_000L
    }
}

private class StatsWorkoutRepository(
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
    override suspend fun discardSession(sessionId: Long) = error("Not used")
    override suspend fun getLastWeightKgForExerciseSet(exerciseName: String, setNumber: Int): Double? = null
    override suspend fun getMaxWeightForExercise(exerciseName: String): Double? = null
    override suspend fun getMaxSetVolumeForExercise(exerciseName: String): Double? = null
}
