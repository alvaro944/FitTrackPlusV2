package com.alvarocervantes.fittrackplus.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alvarocervantes.fittrackplus.core.database.FitTrackPlusDatabase
import com.alvarocervantes.fittrackplus.data.local.entity.WorkoutExerciseEntity
import com.alvarocervantes.fittrackplus.data.local.entity.WorkoutSessionEntity
import com.alvarocervantes.fittrackplus.data.local.entity.WorkoutSetEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkoutDaoTest {

    private lateinit var db: FitTrackPlusDatabase

    @Before
    fun createDb() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            FitTrackPlusDatabase::class.java
        ).allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    // ── Active session ────────────────────────────────────────────────────────

    @Test
    fun insertSession_returnsActiveSession() = runTest {
        db.workoutDao().insertSession(
            WorkoutSessionEntity(
                routineId = 1L,
                routineNameSnapshot = "PPL",
                routineDayId = 1L,
                dayNameSnapshot = "Push",
                startedAt = 1000L,
                weekNumber = 1
            )
        )

        val active = db.workoutDao().getActiveSessionWithExercises()
        assertNotNull(active)
        assertEquals("Push", active!!.session.dayNameSnapshot)
        assertNull(active.session.finishedAt)
    }

    @Test
    fun activeSession_isNullWhenNoneOpen() = runTest {
        val active = db.workoutDao().getActiveSessionWithExercises()
        assertNull(active)
    }

    @Test
    fun activeSession_isNullAfterFinishing() = runTest {
        val dao = db.workoutDao()
        val session = WorkoutSessionEntity(
            routineId = 1L,
            routineNameSnapshot = "PPL",
            routineDayId = 1L,
            dayNameSnapshot = "Pull",
            startedAt = 1000L,
            weekNumber = 1
        )
        val id = dao.insertSession(session)
        dao.updateSession(session.copy(id = id, finishedAt = 2000L))

        assertNull(dao.getActiveSessionWithExercises())
    }

    // ── Finished sessions ─────────────────────────────────────────────────────

    @Test
    fun finishSession_appearsInFinishedSessions() = runTest {
        val dao = db.workoutDao()
        val session = WorkoutSessionEntity(
            routineId = 1L,
            routineNameSnapshot = "PPL",
            routineDayId = 1L,
            dayNameSnapshot = "Push",
            startedAt = 1000L,
            weekNumber = 1
        )
        val id = dao.insertSession(session)

        // Before finishing — not in finished list
        assertEquals(0, dao.observeFinishedSessions().first().size)

        // Finish
        dao.updateSession(session.copy(id = id, finishedAt = 2000L))

        val finished = dao.observeFinishedSessions().first()
        assertEquals(1, finished.size)
        assertEquals(id, finished[0].id)
        assertEquals(2000L, finished[0].finishedAt)
    }

    @Test
    fun openSession_doesNotAppearInFinishedSessions() = runTest {
        db.workoutDao().insertSession(
            WorkoutSessionEntity(
                routineId = 1L,
                routineNameSnapshot = "PPL",
                routineDayId = 1L,
                dayNameSnapshot = "Legs",
                startedAt = 1000L,
                weekNumber = 1
            )
        )
        val finished = db.workoutDao().observeFinishedSessions().first()
        assertEquals(0, finished.size)
    }

    // ── Last weight per exercise/set ──────────────────────────────────────────
    //
    // The query keys on `performedVariantKey` (not on the exercise name) and only
    // considers completed sets of finished sessions, so fixtures must set both
    // `performedVariantKey` and `isCompleted` explicitly — the entity defaults
    // ("" and false) match nothing.

    @Test
    fun getLastWeightKgForExerciseSet_returnsLatestFinishedWeight() = runTest {
        val dao = db.workoutDao()

        // Older finished session — 80 kg
        val s1Id = dao.insertSession(finishedSession(startedAt = 100L, finishedAt = 200L, weekNumber = 1))
        val ex1Id = dao.insertExercise(
            exercise(sessionId = s1Id, variantKey = BENCH_PRESS_KEY, name = "Bench Press", targetReps = "8")
        )
        dao.insertSet(completedSet(workoutExerciseId = ex1Id, setNumber = 1, weightKg = 80.0, reps = 8))

        // More recent finished session — 85 kg (should win)
        val s2Id = dao.insertSession(finishedSession(startedAt = 300L, finishedAt = 400L, weekNumber = 2))
        val ex2Id = dao.insertExercise(
            exercise(sessionId = s2Id, variantKey = BENCH_PRESS_KEY, name = "Bench Press", targetReps = "8")
        )
        dao.insertSet(completedSet(workoutExerciseId = ex2Id, setNumber = 1, weightKg = 85.0, reps = 6))

        // Open session with 200 kg — must NOT be returned
        val s3Id = dao.insertSession(finishedSession(startedAt = 500L, finishedAt = null, weekNumber = 3))
        val ex3Id = dao.insertExercise(
            exercise(sessionId = s3Id, variantKey = BENCH_PRESS_KEY, name = "Bench Press", targetReps = "8")
        )
        dao.insertSet(completedSet(workoutExerciseId = ex3Id, setNumber = 1, weightKg = 200.0, reps = 1))

        val result = dao.getLastWeightKgForExerciseSet(BENCH_PRESS_KEY, 1)
        assertEquals(85.0, result ?: -1.0, 0.0)
    }

    @Test
    fun getLastWeightKgForExerciseSet_returnsNullWhenNoData() = runTest {
        val result = db.workoutDao().getLastWeightKgForExerciseSet("squat", 1)
        assertNull(result)
    }

    @Test
    fun getLastWeightKgForExerciseSet_ignoresIncompleteSets() = runTest {
        val dao = db.workoutDao()
        val sessionId = dao.insertSession(finishedSession(startedAt = 100L, finishedAt = 200L, weekNumber = 1))
        val exerciseId = dao.insertExercise(
            exercise(sessionId = sessionId, variantKey = OHP_KEY, name = "OHP", targetReps = "5")
        )
        dao.insertSet(
            completedSet(workoutExerciseId = exerciseId, setNumber = 1, weightKg = 60.0, reps = 5)
                .copy(isCompleted = false)
        )

        assertNull(dao.getLastWeightKgForExerciseSet(OHP_KEY, 1))
    }

    @Test
    fun getLastWeightKgForExerciseSet_respectsSetNumber() = runTest {
        val dao = db.workoutDao()
        val sessionId = dao.insertSession(finishedSession(startedAt = 100L, finishedAt = 200L, weekNumber = 1))
        val exerciseId = dao.insertExercise(
            exercise(sessionId = sessionId, variantKey = OHP_KEY, name = "OHP", targetReps = "5")
        )
        dao.insertSet(completedSet(workoutExerciseId = exerciseId, setNumber = 1, weightKg = 60.0, reps = 5))
        dao.insertSet(completedSet(workoutExerciseId = exerciseId, setNumber = 2, weightKg = 55.0, reps = 5))

        assertEquals(60.0, dao.getLastWeightKgForExerciseSet(OHP_KEY, 1) ?: -1.0, 0.0)
        assertEquals(55.0, dao.getLastWeightKgForExerciseSet(OHP_KEY, 2) ?: -1.0, 0.0)
        assertNull(dao.getLastWeightKgForExerciseSet(OHP_KEY, 3))
    }

    // ── Fixtures ──────────────────────────────────────────────────────────────

    private fun finishedSession(
        startedAt: Long,
        finishedAt: Long?,
        weekNumber: Int
    ) = WorkoutSessionEntity(
        routineId = 1L,
        routineNameSnapshot = "PPL",
        routineDayId = 1L,
        dayNameSnapshot = "Push",
        startedAt = startedAt,
        finishedAt = finishedAt,
        weekNumber = weekNumber
    )

    private fun exercise(
        sessionId: Long,
        variantKey: String,
        name: String,
        targetReps: String
    ) = WorkoutExerciseEntity(
        sessionId = sessionId,
        exerciseTemplateId = 10L,
        performedVariantKey = variantKey,
        exerciseNameSnapshot = name,
        targetRepsSnapshot = targetReps,
        position = 0
    )

    private fun completedSet(
        workoutExerciseId: Long,
        setNumber: Int,
        weightKg: Double,
        reps: Int
    ) = WorkoutSetEntity(
        workoutExerciseId = workoutExerciseId,
        setNumber = setNumber,
        weightKg = weightKg,
        reps = reps,
        isCompleted = true
    )

    private companion object {
        const val BENCH_PRESS_KEY = "bench-press"
        const val OHP_KEY = "ohp"
    }
}
