package com.alvarocervantes.fittrackplus.domain.usecase

import com.alvarocervantes.fittrackplus.data.local.entity.WorkoutSessionEntity
import com.alvarocervantes.fittrackplus.data.local.relation.WorkoutSessionWithExercises
import com.alvarocervantes.fittrackplus.data.repository.RoutineRepository
import com.alvarocervantes.fittrackplus.data.repository.WorkoutRepository
import com.alvarocervantes.fittrackplus.domain.model.RoutineDaySnapshot
import com.alvarocervantes.fittrackplus.domain.model.RoutineDraft
import com.alvarocervantes.fittrackplus.domain.model.RoutineSnapshot
import com.alvarocervantes.fittrackplus.domain.model.RoutineSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StartWorkoutSessionUseCaseTest {

    @Test
    fun startsNextDayUsingOnlyFinishedSessions() = runBlocking {
        val routineRepository = FakeRoutineRepository(
            routine = routine(
                day(1, "Push", 0),
                day(2, "Pull", 1),
                day(3, "Legs", 2)
            )
        )
        val workoutRepository = FakeWorkoutRepository(finishedSessionsForRoutine = 1)
        val useCase = StartWorkoutSessionUseCase(
            routineRepository = routineRepository,
            workoutRepository = workoutRepository,
            getNextRoutineDay = GetNextRoutineDayUseCase()
        )

        val started = useCase(10)

        assertEquals(101L, started?.sessionId)
        assertEquals("Pull", started?.dayNameSnapshot)
        assertEquals(1, started?.weekNumber)
        assertEquals("Pull", workoutRepository.createdDayNames.single())
    }

    @Test
    fun resumesActiveSessionWithoutCreatingDuplicate() = runBlocking {
        val activeSession = WorkoutSessionWithExercises(
            session = WorkoutSessionEntity(
                id = 55,
                routineId = 10,
                routineNameSnapshot = "PPL",
                routineDayId = 2,
                dayNameSnapshot = "Pull",
                startedAt = 1000,
                weekNumber = 1
            ),
            exercises = emptyList()
        )
        val workoutRepository = FakeWorkoutRepository(activeSession = activeSession)
        val useCase = StartWorkoutSessionUseCase(
            routineRepository = FakeRoutineRepository(routine(day(1, "Push", 0))),
            workoutRepository = workoutRepository,
            getNextRoutineDay = GetNextRoutineDayUseCase()
        )

        val started = useCase(10)

        assertEquals(55L, started?.sessionId)
        assertEquals("Pull", started?.dayNameSnapshot)
        assertTrue(workoutRepository.createdDayNames.isEmpty())
    }

    private fun routine(vararg days: RoutineDaySnapshot): RoutineSnapshot {
        return RoutineSnapshot(
            id = 10,
            name = "PPL",
            days = days.toList()
        )
    }

    private fun day(id: Long, name: String, position: Int): RoutineDaySnapshot {
        return RoutineDaySnapshot(
            id = id,
            name = name,
            position = position,
            exercises = emptyList()
        )
    }
}

private class FakeRoutineRepository(
    private val routine: RoutineSnapshot?
) : RoutineRepository {
    override fun observeRoutines(): Flow<List<RoutineSummary>> = flowOf(emptyList())
    override fun observeArchivedRoutines(): Flow<List<RoutineSummary>> = flowOf(emptyList())
    override suspend fun getRoutineSnapshot(routineId: Long): RoutineSnapshot? = routine
    override suspend fun createRoutine(draft: RoutineDraft): Long = error("Not used")
    override suspend fun replaceRoutine(routineId: Long, draft: RoutineDraft) = error("Not used")
    override suspend fun archiveRoutine(routineId: Long) = error("Not used")
    override suspend fun restoreRoutine(routineId: Long) = error("Not used")
}

private class FakeWorkoutRepository(
    private val activeSession: WorkoutSessionWithExercises? = null,
    private val finishedSessionsForRoutine: Int = 0
) : WorkoutRepository {
    val createdDayNames = mutableListOf<String>()

    override fun observeSessions(): Flow<List<WorkoutSessionEntity>> = flowOf(emptyList())

    override fun observeFinishedSessions(): Flow<List<WorkoutSessionEntity>> = flowOf(emptyList())

    override fun observeFinishedSessionsWithExercises(): Flow<List<WorkoutSessionWithExercises>> = flowOf(emptyList())

    override suspend fun getActiveSessionWithExercises(): WorkoutSessionWithExercises? = activeSession

    override suspend fun getSessionWithExercises(sessionId: Long): WorkoutSessionWithExercises? = activeSession

    override suspend fun getFinishedSessionWithExercises(sessionId: Long): WorkoutSessionWithExercises? = null

    override suspend fun countFinishedSessionsForRoutine(routineId: Long): Int = finishedSessionsForRoutine

    override suspend fun countSessions(): Int = 0

    override suspend fun createSessionFromRoutineDay(
        routine: RoutineSnapshot,
        day: RoutineDaySnapshot,
        weekNumber: Int
    ): Long {
        createdDayNames += day.name
        return 101
    }

    override suspend fun updateSet(setId: Long, weightKg: Double, reps: Int) = Unit

    override suspend fun finishSession(sessionId: Long, notes: String?) = Unit

    override suspend fun getLastWeightKgForExerciseSet(exerciseName: String, setNumber: Int): Double? = null
    override suspend fun getMaxWeightForExercise(exerciseName: String): Double? = null
    override suspend fun getMaxSetVolumeForExercise(exerciseName: String): Double? = null
}
