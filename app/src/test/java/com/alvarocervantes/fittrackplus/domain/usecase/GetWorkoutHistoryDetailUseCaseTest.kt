package com.alvarocervantes.fittrackplus.domain.usecase

import com.alvarocervantes.fittrackplus.data.local.entity.WorkoutExerciseEntity
import com.alvarocervantes.fittrackplus.data.local.entity.WorkoutSessionEntity
import com.alvarocervantes.fittrackplus.data.local.entity.WorkoutSetEntity
import com.alvarocervantes.fittrackplus.data.local.relation.WorkoutExerciseWithSets
import com.alvarocervantes.fittrackplus.data.local.relation.WorkoutSessionWithExercises
import com.alvarocervantes.fittrackplus.data.repository.WorkoutRepository
import com.alvarocervantes.fittrackplus.domain.model.RoutineDaySnapshot
import com.alvarocervantes.fittrackplus.domain.model.RoutineSnapshot
import com.alvarocervantes.fittrackplus.domain.model.WorkoutHistoryDeltaDirection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GetWorkoutHistoryDetailUseCaseTest {

    @Test
    fun loadsFinishedSessionDetailUsingSnapshotFieldsAndOrdering() = runBlocking {
        val detail = WorkoutSessionWithExercises(
            session = WorkoutSessionEntity(
                id = 42,
                routineId = 7,
                routineNameSnapshot = "Snapshot Routine",
                routineDayId = 8,
                dayNameSnapshot = "Snapshot Day",
                startedAt = 1000,
                finishedAt = 2000,
                weekNumber = 3
            ),
            exercises = listOf(
                exerciseWithSets(
                    id = 2,
                    position = 1,
                    name = "Second Snapshot Exercise",
                    targetReps = "10-12",
                    sets = listOf(
                        set(id = 22, setNumber = 2, weightKg = 30.0, reps = 10),
                        set(id = 21, setNumber = 1, weightKg = 27.5, reps = 12)
                    )
                ),
                exerciseWithSets(
                    id = 1,
                    position = 0,
                    name = "First Snapshot Exercise",
                    targetReps = "6-8",
                    sets = listOf(
                        set(id = 12, setNumber = 2, weightKg = 82.5, reps = 7),
                        set(id = 11, setNumber = 1, weightKg = 80.0, reps = 8)
                    )
                )
            )
        )
        val useCase = GetWorkoutHistoryDetailUseCase(
            HistoryDetailWorkoutRepository(detail = detail)
        )

        val result = useCase(42)

        assertEquals("Snapshot Routine", result?.routineName)
        assertEquals("Snapshot Day", result?.dayName)
        assertEquals("First Snapshot Exercise", result?.exercises?.first()?.name)
        assertEquals("6-8", result?.exercises?.first()?.targetRepsText)
        assertEquals(1, result?.exercises?.first()?.sets?.first()?.setNumber)
        assertEquals(80.0, result?.exercises?.first()?.sets?.first()?.weightKg ?: -1.0, 0.0)
        assertEquals(8, result?.exercises?.first()?.sets?.first()?.reps)
    }

    @Test
    fun returnsNullForOpenSessionDetail() = runBlocking {
        val detail = WorkoutSessionWithExercises(
            session = WorkoutSessionEntity(
                id = 42,
                routineId = 7,
                routineNameSnapshot = "Open Routine",
                routineDayId = 8,
                dayNameSnapshot = "Open Day",
                startedAt = 1000,
                finishedAt = null,
                weekNumber = 1
            ),
            exercises = emptyList()
        )
        val useCase = GetWorkoutHistoryDetailUseCase(
            HistoryDetailWorkoutRepository(detail = detail)
        )

        assertNull(useCase(42))
    }

    @Test
    fun comparesAgainstMostRecentPreviousFinishedSessionWithSameRoutineAndDaySnapshots() = runBlocking {
        val current = sessionWithSingleSet(
            sessionId = 5,
            routineName = "Push Pull Legs",
            dayName = "Push",
            startedAt = 10_000,
            finishedAt = 20_000,
            weightKg = 100.0,
            reps = 8
        )
        val previousComparable = sessionWithSingleSet(
            sessionId = 4,
            routineName = "Push Pull Legs",
            dayName = "Push",
            startedAt = 2_000,
            finishedAt = 8_000,
            weightKg = 90.0,
            reps = 8
        )
        val olderComparable = sessionWithSingleSet(
            sessionId = 3,
            routineName = "Push Pull Legs",
            dayName = "Push",
            startedAt = 500,
            finishedAt = 1_500,
            weightKg = 200.0,
            reps = 1
        )
        val openSameDay = sessionWithSingleSet(
            sessionId = 6,
            routineName = "Push Pull Legs",
            dayName = "Push",
            startedAt = 21_000,
            finishedAt = null,
            weightKg = 200.0,
            reps = 10
        )
        val otherDay = sessionWithSingleSet(
            sessionId = 2,
            routineName = "Push Pull Legs",
            dayName = "Pull",
            startedAt = 3_000,
            finishedAt = 9_000,
            weightKg = 10.0,
            reps = 1
        )
        val otherRoutine = sessionWithSingleSet(
            sessionId = 1,
            routineName = "Upper Lower",
            dayName = "Push",
            startedAt = 3_000,
            finishedAt = 9_000,
            weightKg = 10.0,
            reps = 1
        )
        val useCase = GetWorkoutHistoryDetailUseCase(
            HistoryDetailWorkoutRepository(
                detail = current,
                finishedSessions = listOf(
                    current,
                    olderComparable,
                    previousComparable,
                    openSameDay,
                    otherDay,
                    otherRoutine
                )
            )
        )

        val comparison = useCase(5)?.comparison

        assertEquals(4L, comparison?.previousSessionId)
        assertEquals(800.0, comparison?.totalVolumeDelta?.currentValue ?: -1.0, 0.0)
        assertEquals(720.0, comparison?.totalVolumeDelta?.previousValue ?: -1.0, 0.0)
        assertEquals(80.0, comparison?.totalVolumeDelta?.deltaValue ?: -1.0, 0.0)
        assertEquals(WorkoutHistoryDeltaDirection.Up, comparison?.totalVolumeDelta?.direction)
        assertEquals(10_000.0, comparison?.durationMillisDelta?.currentValue ?: -1.0, 0.0)
        assertEquals(6_000.0, comparison?.durationMillisDelta?.previousValue ?: -1.0, 0.0)
        assertEquals(WorkoutHistoryDeltaDirection.Up, comparison?.durationMillisDelta?.direction)
        assertEquals(1.0, comparison?.setCountDelta?.currentValue ?: -1.0, 0.0)
        assertEquals(WorkoutHistoryDeltaDirection.Same, comparison?.setCountDelta?.direction)
        assertEquals("Bench Press", comparison?.bestSet?.current?.exerciseName)
        assertEquals(800.0, comparison?.bestSet?.delta?.currentValue ?: -1.0, 0.0)
        assertEquals(720.0, comparison?.bestSet?.delta?.previousValue ?: -1.0, 0.0)
    }

    @Test
    fun exposesNoComparisonWhenThereIsNoPreviousComparableSession() = runBlocking {
        val current = sessionWithSingleSet(
            sessionId = 5,
            routineName = "Push Pull Legs",
            dayName = "Push",
            startedAt = 10_000,
            finishedAt = 20_000,
            weightKg = 100.0,
            reps = 8
        )
        val useCase = GetWorkoutHistoryDetailUseCase(
            HistoryDetailWorkoutRepository(
                detail = current,
                finishedSessions = listOf(
                    current,
                    sessionWithSingleSet(
                        sessionId = 4,
                        routineName = "Push Pull Legs",
                        dayName = "Pull",
                        startedAt = 2_000,
                        finishedAt = 8_000,
                        weightKg = 100.0,
                        reps = 8
                    )
                )
            )
        )

        assertNull(useCase(5)?.comparison)
    }

    private fun exerciseWithSets(
        id: Long,
        position: Int,
        name: String,
        targetReps: String,
        sets: List<WorkoutSetEntity>
    ): WorkoutExerciseWithSets {
        return WorkoutExerciseWithSets(
            exercise = WorkoutExerciseEntity(
                id = id,
                sessionId = 42,
                exerciseTemplateId = 100 + id,
                exerciseNameSnapshot = name,
                targetRepsSnapshot = targetReps,
                position = position
            ),
            sets = sets
        )
    }

    private fun set(
        id: Long,
        setNumber: Int,
        weightKg: Double,
        reps: Int
    ): WorkoutSetEntity {
        return WorkoutSetEntity(
            id = id,
            workoutExerciseId = 1,
            setNumber = setNumber,
            weightKg = weightKg,
            reps = reps
        )
    }
}

private fun sessionWithSingleSet(
    sessionId: Long,
    routineName: String,
    dayName: String,
    startedAt: Long,
    finishedAt: Long?,
    weightKg: Double,
    reps: Int
): WorkoutSessionWithExercises {
    return WorkoutSessionWithExercises(
        session = WorkoutSessionEntity(
            id = sessionId,
            routineId = 7,
            routineNameSnapshot = routineName,
            routineDayId = 8,
            dayNameSnapshot = dayName,
            startedAt = startedAt,
            finishedAt = finishedAt,
            weekNumber = 1
        ),
        exercises = listOf(
            WorkoutExerciseWithSets(
                exercise = WorkoutExerciseEntity(
                    id = sessionId * 10,
                    sessionId = sessionId,
                    exerciseTemplateId = 100,
                    exerciseNameSnapshot = "Bench Press",
                    targetRepsSnapshot = "8",
                    position = 0
                ),
                sets = listOf(
                    WorkoutSetEntity(
                        id = sessionId * 100,
                        workoutExerciseId = sessionId * 10,
                        setNumber = 1,
                        weightKg = weightKg,
                        reps = reps
                    )
                )
            )
        )
    )
}

private class HistoryDetailWorkoutRepository(
    private val detail: WorkoutSessionWithExercises?,
    private val finishedSessions: List<WorkoutSessionWithExercises> = emptyList()
) : WorkoutRepository {
    override fun observeSessions(): Flow<List<WorkoutSessionEntity>> = flowOf(emptyList())
    override fun observeFinishedSessions(): Flow<List<WorkoutSessionEntity>> = flowOf(emptyList())
    override fun observeFinishedSessionsWithExercises(): Flow<List<WorkoutSessionWithExercises>> = flowOf(finishedSessions)
    override fun observeActiveSession(): Flow<WorkoutSessionWithExercises?> = flowOf(null)
    override suspend fun getActiveSessionWithExercises(): WorkoutSessionWithExercises? = null
    override suspend fun getSessionWithExercises(sessionId: Long): WorkoutSessionWithExercises? = detail
    override suspend fun getFinishedSessionWithExercises(sessionId: Long): WorkoutSessionWithExercises? = detail
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
    override suspend fun getLastRepsForExerciseSet(exerciseName: String, setNumber: Int): Int? = null
    override suspend fun getMaxWeightForExercise(exerciseName: String): Double? = null
    override suspend fun getMaxSetVolumeForExercise(exerciseName: String): Double? = null
}
