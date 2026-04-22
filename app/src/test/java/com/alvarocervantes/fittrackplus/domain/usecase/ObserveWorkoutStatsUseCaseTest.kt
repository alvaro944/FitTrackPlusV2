package com.alvarocervantes.fittrackplus.domain.usecase

import com.alvarocervantes.fittrackplus.data.local.entity.WorkoutExerciseEntity
import com.alvarocervantes.fittrackplus.data.local.entity.WorkoutSessionEntity
import com.alvarocervantes.fittrackplus.data.local.entity.WorkoutSetEntity
import com.alvarocervantes.fittrackplus.data.local.relation.WorkoutExerciseWithSets
import com.alvarocervantes.fittrackplus.data.local.relation.WorkoutSessionWithExercises
import com.alvarocervantes.fittrackplus.data.repository.WorkoutRepository
import com.alvarocervantes.fittrackplus.domain.model.RoutineDaySnapshot
import com.alvarocervantes.fittrackplus.domain.model.RoutineSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ObserveWorkoutStatsUseCaseTest {

    @Test
    fun calculatesVolumesProgressAndRecordsFromFinishedSnapshotSessions() = runBlocking {
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

        val stats = useCase().first()

        assertEquals(2, stats.sessionVolumes.size)
        assertEquals(2L, stats.sessionVolumes[0].sessionId)
        assertEquals(1210.0, stats.sessionVolumes[0].totalVolumeKg, 0.0)
        assertEquals(1L, stats.sessionVolumes[1].sessionId)
        assertEquals(1340.0, stats.sessionVolumes[1].totalVolumeKg, 0.0)

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

        val benchRecords = stats.exerciseRecords.single { it.exerciseKey == "bench press" }
        assertEquals(102.5, benchRecords.maxWeight?.weightKg ?: -1.0, 0.0)
        assertEquals(10, benchRecords.maxReps?.reps)
        assertEquals(800.0, benchRecords.bestSetVolume?.setVolumeKg ?: -1.0, 0.0)
        assertEquals(116.666, benchRecords.bestEstimatedOneRepMax?.estimatedOneRepMaxKg ?: -1.0, 0.01)
    }

    @Test
    fun handlesZeroWeightExerciseRecordsWithoutWeightedMarks() = runBlocking {
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

        val stats = useCase().first()

        assertEquals(0.0, stats.sessionVolumes.single().totalVolumeKg, 0.0)
        val records = stats.exerciseRecords.single()
        assertEquals(12, records.maxReps?.reps)
        assertNull(records.maxWeight)
        assertNull(records.bestSetVolume)
        assertNull(records.bestEstimatedOneRepMax)
        assertNotNull(stats.exerciseProgress.single().entries.single())
    }

    private fun sessionWithExercises(
        sessionId: Long,
        routineName: String,
        dayName: String,
        startedAt: Long,
        finishedAt: Long?,
        exercises: List<WorkoutExerciseWithSets>
    ): WorkoutSessionWithExercises {
        return WorkoutSessionWithExercises(
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
    }

    private fun exercise(
        id: Long,
        sessionId: Long,
        name: String,
        position: Int,
        sets: List<WorkoutSetEntity>
    ): WorkoutExerciseWithSets {
        return WorkoutExerciseWithSets(
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
    }

    private fun set(
        id: Long,
        exerciseId: Long,
        setNumber: Int,
        weightKg: Double,
        reps: Int
    ): WorkoutSetEntity {
        return WorkoutSetEntity(
            id = id,
            workoutExerciseId = exerciseId,
            setNumber = setNumber,
            weightKg = weightKg,
            reps = reps
        )
    }
}

private class StatsWorkoutRepository(
    private val sessions: List<WorkoutSessionWithExercises>
) : WorkoutRepository {
    override fun observeSessions(): Flow<List<WorkoutSessionEntity>> = flowOf(emptyList())
    override fun observeFinishedSessions(): Flow<List<WorkoutSessionEntity>> = flowOf(emptyList())
    override fun observeFinishedSessionsWithExercises(): Flow<List<WorkoutSessionWithExercises>> = flowOf(sessions)
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
