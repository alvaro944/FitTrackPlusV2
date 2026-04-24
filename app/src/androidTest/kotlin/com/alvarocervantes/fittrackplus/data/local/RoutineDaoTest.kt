package com.alvarocervantes.fittrackplus.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alvarocervantes.fittrackplus.core.database.FitTrackPlusDatabase
import com.alvarocervantes.fittrackplus.data.local.entity.RoutineEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoutineDaoTest {

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

    // ── Insert & observe activas ──────────────────────────────────────────────

    @Test
    fun insertRoutine_appearsInActiveSummaries() = runTest {
        db.routineDao().insertRoutine(
            RoutineEntity(name = "PPL", createdAt = 100L, updatedAt = 100L)
        )

        val summaries = db.routineDao().observeRoutineSummaries().first()
        assertEquals(1, summaries.size)
        assertEquals("PPL", summaries[0].name)
        assertEquals(false, summaries[0].isArchived)
    }

    @Test
    fun noRoutines_activeSummariesIsEmpty() = runTest {
        val summaries = db.routineDao().observeRoutineSummaries().first()
        assertEquals(0, summaries.size)
    }

    @Test
    fun multipleRoutines_orderedByUpdatedAtDesc() = runTest {
        val dao = db.routineDao()
        dao.insertRoutine(RoutineEntity(name = "Older", createdAt = 100L, updatedAt = 100L))
        dao.insertRoutine(RoutineEntity(name = "Newer", createdAt = 200L, updatedAt = 200L))

        val summaries = dao.observeRoutineSummaries().first()
        assertEquals(2, summaries.size)
        assertEquals("Newer", summaries[0].name)
        assertEquals("Older", summaries[1].name)
    }

    // ── Archive ───────────────────────────────────────────────────────────────

    @Test
    fun archivedRoutine_disappearsFromActive() = runTest {
        val dao = db.routineDao()
        val id = dao.insertRoutine(RoutineEntity(name = "Push Pull Legs", createdAt = 100L, updatedAt = 100L))

        dao.archiveRoutine(id, updatedAt = 200L)

        val active = dao.observeRoutineSummaries().first()
        assertEquals(0, active.size)
    }

    @Test
    fun archivedRoutine_appearsInArchivedSummaries() = runTest {
        val dao = db.routineDao()
        val id = dao.insertRoutine(RoutineEntity(name = "Bro Split", createdAt = 100L, updatedAt = 100L))

        dao.archiveRoutine(id, updatedAt = 200L)

        val archived = dao.observeArchivedRoutineSummaries().first()
        assertEquals(1, archived.size)
        assertEquals("Bro Split", archived[0].name)
        assertEquals(true, archived[0].isArchived)
    }

    // ── Restore ───────────────────────────────────────────────────────────────

    @Test
    fun restoreRoutine_movesBackToActive() = runTest {
        val dao = db.routineDao()
        val id = dao.insertRoutine(RoutineEntity(name = "Upper Lower", createdAt = 100L, updatedAt = 100L))

        dao.archiveRoutine(id, updatedAt = 200L)
        dao.restoreRoutine(id, updatedAt = 300L)

        val active = dao.observeRoutineSummaries().first()
        assertEquals(1, active.size)
        assertEquals("Upper Lower", active[0].name)

        val archived = dao.observeArchivedRoutineSummaries().first()
        assertEquals(0, archived.size)
    }

    // ── countRoutines ─────────────────────────────────────────────────────────

    @Test
    fun countRoutines_reflectsInsertedRows() = runTest {
        val dao = db.routineDao()
        assertEquals(0, dao.countRoutines())

        dao.insertRoutine(RoutineEntity(name = "A", createdAt = 1L, updatedAt = 1L))
        assertEquals(1, dao.countRoutines())

        dao.insertRoutine(RoutineEntity(name = "B", createdAt = 2L, updatedAt = 2L))
        assertEquals(2, dao.countRoutines())
    }
}
