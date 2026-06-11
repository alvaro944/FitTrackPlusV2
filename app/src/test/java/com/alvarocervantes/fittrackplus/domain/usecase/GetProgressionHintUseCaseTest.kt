package com.alvarocervantes.fittrackplus.domain.usecase

import com.alvarocervantes.fittrackplus.data.local.entity.WorkoutSessionEntity
import com.alvarocervantes.fittrackplus.data.local.relation.WorkoutSessionWithExercises
import com.alvarocervantes.fittrackplus.data.repository.WorkoutRepository
import com.alvarocervantes.fittrackplus.domain.model.ProgressionHint
import com.alvarocervantes.fittrackplus.domain.model.RoutineDaySnapshot
import com.alvarocervantes.fittrackplus.domain.model.RoutineSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetProgressionHintUseCaseTest {

    @Test
    fun returnsUpWhenTwoOfLastThreeSessionsBeatTopRange() = runTest {
        val repository = ProgressionHintWorkoutRepository(
            averagesByVariant = mapOf("bench" to listOf(13.0, 12.4, 10.0))
        )

        val result = GetProgressionHintUseCase(repository)(
            variantKey = "bench",
            targetRepsText = "8-12"
        )

        assertEquals(ProgressionHint.UP, result)
    }

    @Test
    fun returnsDownWhenTwoOfLastThreeSessionsMissBottomRange() = runTest {
        val repository = ProgressionHintWorkoutRepository(
            averagesByVariant = mapOf("squat" to listOf(5.0, 7.0, 8.0))
        )

        val result = GetProgressionHintUseCase(repository)(
            variantKey = "squat",
            targetRepsText = "8-12"
        )

        assertEquals(ProgressionHint.DOWN, result)
    }

    @Test
    fun returnsNoneWhenThereAreFewerThanTwoSessionsWithData() = runTest {
        val repository = ProgressionHintWorkoutRepository(
            averagesByVariant = mapOf("row" to listOf(11.0))
        )

        val result = GetProgressionHintUseCase(repository)(
            variantKey = "row",
            targetRepsText = "10"
        )

        assertEquals(ProgressionHint.NONE, result)
    }

    @Test
    fun returnsNoneWhenTargetRepsTextCannotBeParsed() = runTest {
        val repository = ProgressionHintWorkoutRepository(
            averagesByVariant = mapOf("pullup" to listOf(14.0, 15.0, 13.0))
        )

        val result = GetProgressionHintUseCase(repository)(
            variantKey = "pullup",
            targetRepsText = "Al fallo"
        )

        assertEquals(ProgressionHint.NONE, result)
    }
}

private class ProgressionHintWorkoutRepository(
    private val averagesByVariant: Map<String, List<Double>>
) : WorkoutRepository {
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
    override suspend fun updateSet(setId: Long, weightKg: Double, reps: Int) = error("Not used")
    override suspend fun finishSession(sessionId: Long, notes: String?) = error("Not used")
    override suspend fun getLastWeightKgForExerciseSet(variantKey: String, setNumber: Int): Double? = null
    override suspend fun getMaxWeightForExercise(variantKey: String): Double? = null
    override suspend fun getMaxSetVolumeForExercise(variantKey: String): Double? = null
    override suspend fun getRecentAverageRepsForExercise(variantKey: String, limit: Int): List<Double> {
        return averagesByVariant[variantKey].orEmpty().take(limit)
    }
}
