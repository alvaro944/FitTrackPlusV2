package com.alvarocervantes.fittrackplus.domain.usecase

import com.alvarocervantes.fittrackplus.data.local.entity.WorkoutSessionEntity
import com.alvarocervantes.fittrackplus.data.local.relation.WorkoutSessionWithExercises
import com.alvarocervantes.fittrackplus.data.repository.WorkoutRepository
import com.alvarocervantes.fittrackplus.domain.model.PrType
import com.alvarocervantes.fittrackplus.domain.model.RoutineDaySnapshot
import com.alvarocervantes.fittrackplus.domain.model.RoutineSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DetectPersonalRecordUseCaseTest {

    @Test
    fun noPreviousHistory_firstSetIsMaxWeightPR() = runTest {
        val repo = PrWorkoutRepository(maxWeight = null, maxVolume = null)
        val useCase = DetectPersonalRecordUseCase(repo)
        assertEquals(PrType.MaxWeight, useCase("Bench Press", weightKg = 80.0, reps = 5))
    }

    @Test
    fun weightHigherThanPrevMax_isMaxWeightPR() = runTest {
        val repo = PrWorkoutRepository(maxWeight = 100.0, maxVolume = 500.0)
        val useCase = DetectPersonalRecordUseCase(repo)
        assertEquals(PrType.MaxWeight, useCase("Bench Press", weightKg = 105.0, reps = 1))
    }

    @Test
    fun weightEqualToPrevMax_isNotPR() = runTest {
        val repo = PrWorkoutRepository(maxWeight = 100.0, maxVolume = 500.0)
        val useCase = DetectPersonalRecordUseCase(repo)
        assertNull(useCase("Bench Press", weightKg = 100.0, reps = 5))
    }

    @Test
    fun weightLowerButVolumeHigher_isMaxVolumePR() = runTest {
        // prevMaxWeight = 100, prevMaxVolume = 500 (100 * 5)
        val repo = PrWorkoutRepository(maxWeight = 100.0, maxVolume = 500.0)
        val useCase = DetectPersonalRecordUseCase(repo)
        // 90 kg × 6 reps = 540 kg > 500 kg: PR de volumen
        assertEquals(PrType.MaxVolume, useCase("Bench Press", weightKg = 90.0, reps = 6))
    }

    @Test
    fun weightLowerAndVolumeEqualOrLower_isNotPR() = runTest {
        val repo = PrWorkoutRepository(maxWeight = 100.0, maxVolume = 500.0)
        val useCase = DetectPersonalRecordUseCase(repo)
        // 80 kg × 6 reps = 480 kg < 500 kg: no es PR
        assertNull(useCase("Bench Press", weightKg = 80.0, reps = 6))
    }

    @Test
    fun zeroReps_isNotPR() = runTest {
        val repo = PrWorkoutRepository(maxWeight = null, maxVolume = null)
        val useCase = DetectPersonalRecordUseCase(repo)
        assertNull(useCase("Bench Press", weightKg = 100.0, reps = 0))
    }

    @Test
    fun zeroWeight_isNotPR() = runTest {
        val repo = PrWorkoutRepository(maxWeight = null, maxVolume = null)
        val useCase = DetectPersonalRecordUseCase(repo)
        assertNull(useCase("Bench Press", weightKg = 0.0, reps = 10))
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private class PrWorkoutRepository(
        private val maxWeight: Double?,
        private val maxVolume: Double?
    ) : WorkoutRepository {
        override fun observeSessions(): Flow<List<WorkoutSessionEntity>> = flowOf(emptyList())
        override fun observeFinishedSessions(): Flow<List<WorkoutSessionEntity>> = flowOf(emptyList())
        override fun observeFinishedSessionsWithExercises(): Flow<List<WorkoutSessionWithExercises>> = flowOf(emptyList())
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
        override suspend fun getLastWeightKgForExerciseSet(exerciseName: String, setNumber: Int): Double? = null
        override suspend fun getMaxWeightForExercise(exerciseName: String): Double? = maxWeight
        override suspend fun getMaxSetVolumeForExercise(exerciseName: String): Double? = maxVolume
    }
}
