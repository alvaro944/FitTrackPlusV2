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

    @Test
    fun getLastWeightKgForExerciseSet_returnsLatestFinishedWeight() = runTest {
        val dao = db.workoutDao()

        // Older finished session — 80 kg
        val s1Id = dao.insertSession(
            WorkoutSessionEntity(
                routineId = 1L, routineNameSnapshot = "PPL",
                routineDayId = 1L, dayNameSnapshot = "Push",
                startedAt = 100L, finishedAt = 200L, weekNumber = 1
            )
        )
        val ex1Id = dao.insertExercise(
            WorkoutExerciseEntity(
                sessionId = s1Id, exerciseTemplateId = 10L,
                exerciseNameSnapshot = "Bench Press",
                targetRepsSnapshot = "8", position = 0
            )
        )
        dao.insertSet(WorkoutSetEntity(workoutExerciseId = ex1Id, setNumber = 1, weightKg = 80.0, reps = 8))

        // More recent finished session — 85 kg (should win)
        val s2Id = dao.insertSession(
            WorkoutSessionEntity(
                routineId = 1L, routineNameSnapshot = "PPL",
                routineDayId = 1L, dayNameSnapshot = "Push",
                startedAt = 300L, finishedAt = 400L, weekNumber = 2
            )
        )
        val ex2Id = dao.insertExercise(
            WorkoutExerciseEntity(
                sessionId = s2Id, exerciseTemplateId = 10L,
                exerciseNameSnapshot = "Bench Press",
                targetRepsSnapshot = "8", position = 0
            )
        )
        dao.insertSet(WorkoutSetEntity(workoutExerciseId = ex2Id, setNumber = 1, weightKg = 85.0, reps = 6))

        // Open session with 200 kg — must NOT be returned
        val s3Id = dao.insertSession(
            WorkoutSessionEntity(
                routineId = 1L, routineNameSnapshot = "PPL",
                routineDayId = 1L, dayNameSnapshot = "Push",
                startedAt = 500L, finishedAt = null, weekNumber = 3
            )
        )
        val ex3Id = dao.insertExercise(
            WorkoutExerciseEntity(
                sessionId = s3Id, exerciseTemplateId = 10L,
                exerciseNameSnapshot = "Bench Press",
                targetRepsSnapshot = "8", position = 0
            )
        )
        dao.insertSet(WorkoutSetEntity(workoutExerciseId = ex3Id, setNumber = 1, weightKg = 200.0, reps = 1))

        val result = dao.getLastWeightKgForExerciseSet("Bench Press", 1)
        assertEquals(85.0, result ?: -1.0, 0.0)
    }

    @Test
    fun getLastWeightKgForExerciseSet_returnsNullWhenNoData() = runTest {
        val result = db.workoutDao().getLastWeightKgForExerciseSet("Squat", 1)
        assertNull(result)
    }

    @Test
    fun getLastWeightKgForExerciseSet_respectsSetNumber() = runTest {
        val dao = db.workoutDao()
        val sId = dao.insertSession(
            WorkoutSessionEntity(
                routineId = 1L, routineNameSnapshot = "PPL",
                routineDayId = 1L, dayNameSnapshot = "Push",
                startedAt = 100L, finishedAt = 200L, weekNumber = 1
            )
        )
        val exId = dao.insertExercise(
            WorkoutExerciseEntity(
                sessionId = sId, exerciseTemplateId = 10L,
                exerciseNameSnapshot = "OHP",
                targetRepsSnapshot = "5", position = 0
            )
        )
        dao.insertSet(WorkoutSetEntity(workoutExerciseId = exId, setNumber = 1, weightKg = 60.0, reps = 5))
        dao.insertSet(WorkoutSetEntity(workoutExerciseId = exId, setNumber = 2, weightKg = 55.0, reps = 5))

        assertEquals(60.0, dao.getLastWeightKgForExerciseSet("OHP", 1) ?: -1.0, 0.0)
        assertEquals(55.0, dao.getLastWeightKgForExerciseSet("OHP", 2) ?: -1.0, 0.0)
        assertNull(dao.getLastWeightKgForExerciseSet("OHP", 3))
    }
}
