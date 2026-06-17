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
import org.junit.Test

class UpdateWorkoutSetUseCaseTest {

    @Test
    fun persistsParsedWeightAndReps() = runBlocking {
        val repository = SetUpdateWorkoutRepository()
        val useCase = UpdateWorkoutSetUseCase(repository)

        useCase(setId = 7, weightText = "82.5", repsText = "8")

        assertEquals(7L, repository.lastSetId)
        assertEquals(82.5, repository.lastWeightKg ?: -1.0, 0.0)
        assertEquals(8, repository.lastReps)
    }

    @Test
    fun normalizesBlankInvalidAndNegativeValuesToZero() = runBlocking {
        val repository = SetUpdateWorkoutRepository()
        val useCase = UpdateWorkoutSetUseCase(repository)

        useCase(setId = 8, weightText = "-12", repsText = "oops")

        assertEquals(8L, repository.lastSetId)
        assertEquals(0.0, repository.lastWeightKg ?: -1.0, 0.0)
        assertEquals(0, repository.lastReps)
    }

    @Test
    fun normalizesDecimalAndNegativeRepsToZero() = runBlocking {
        val repository = SetUpdateWorkoutRepository()
        val useCase = UpdateWorkoutSetUseCase(repository)

        useCase(setId = 9, weightText = "70", repsText = "8.5")

        assertEquals(9L, repository.lastSetId)
        assertEquals(70.0, repository.lastWeightKg ?: -1.0, 0.0)
        assertEquals(0, repository.lastReps)

        useCase(setId = 10, weightText = "72.5", repsText = "-4")

        assertEquals(10L, repository.lastSetId)
        assertEquals(72.5, repository.lastWeightKg ?: -1.0, 0.0)
        assertEquals(0, repository.lastReps)
    }

    @Test
    fun acceptsCommaDecimalSeparatorForWeight() = runBlocking {
        val repository = SetUpdateWorkoutRepository()
        val useCase = UpdateWorkoutSetUseCase(repository)

        useCase(setId = 11, weightText = "82,5", repsText = "6")

        assertEquals(11L, repository.lastSetId)
        assertEquals(82.5, repository.lastWeightKg ?: -1.0, 0.0)
        assertEquals(6, repository.lastReps)
    }
}

private class SetUpdateWorkoutRepository : WorkoutRepository {
    var lastSetId: Long? = null
    var lastWeightKg: Double? = null
    var lastReps: Int? = null

    override fun observeSessions(): Flow<List<WorkoutSessionEntity>> = flowOf(emptyList())
    override fun observeFinishedSessions(): Flow<List<WorkoutSessionEntity>> = flowOf(emptyList())
    override fun observeFinishedSessionsWithExercises(): Flow<List<WorkoutSessionWithExercises>> = flowOf(emptyList())
    override fun observeActiveSession(): Flow<WorkoutSessionWithExercises?> = flowOf(null)
    override suspend fun getActiveSessionWithExercises(): WorkoutSessionWithExercises? = null
    override suspend fun getSessionWithExercises(sessionId: Long): WorkoutSessionWithExercises? = null
    override suspend fun getFinishedSessionWithExercises(sessionId: Long): WorkoutSessionWithExercises? = null
    override suspend fun countFinishedSessionsForRoutine(routineId: Long): Int = 0
    override suspend fun countSessions(): Int = 0
    override suspend fun createSessionFromRoutineDay(
        routine: RoutineSnapshot,
        day: RoutineDaySnapshot,
        weekNumber: Int
    ): Long = error("Not used")

    override suspend fun updateSet(setId: Long, weightKg: Double, reps: Int) {
        lastSetId = setId
        lastWeightKg = weightKg
        lastReps = reps
    }

    override suspend fun finishSession(sessionId: Long, notes: String?) = Unit
    override suspend fun getLastWeightKgForExerciseSet(exerciseName: String, setNumber: Int): Double? = null
    override suspend fun getLastRepsForExerciseSet(exerciseName: String, setNumber: Int): Int? = null
    override suspend fun getMaxWeightForExercise(exerciseName: String): Double? = null
    override suspend fun getMaxSetVolumeForExercise(exerciseName: String): Double? = null
}
