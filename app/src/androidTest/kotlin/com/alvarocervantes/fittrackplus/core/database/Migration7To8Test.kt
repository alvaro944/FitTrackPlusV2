package com.alvarocervantes.fittrackplus.core.database

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Migration7To8Test {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        FitTrackPlusDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrationPreservesExistingDataAndAppliesProgressionDefaults() {
        helper.createDatabase(TEST_DB_NAME, 7).apply {
            insertVersionSevenRoutineData()
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB_NAME, 8, true, MIGRATION_7_8).apply {
            assertRoutineExerciseWasPreservedWithAccessoryRole()
            assertProfileDefaults()
            assertDeletedWorkoutExerciseLeavesExposureOrphaned()
            close()
        }
    }

    private fun SupportSQLiteDatabase.insertVersionSevenRoutineData() {
        execSQL(
            "INSERT INTO routines (id, name, createdAt, updatedAt, isArchived) VALUES (1, 'Routine', 1, 1, 0)"
        )
        execSQL("INSERT INTO routine_days (id, routineId, name, position) VALUES (1, 1, 'Day', 0)")
        execSQL(
            """
            INSERT INTO routine_exercises (
                id, routineDayId, variantKey, defaultVariantKey, name, targetSets,
                targetRepsText, position, notes, targetRepsMin, targetRepsMax
            ) VALUES (1, 1, 'squat', 'squat', 'Squat', 3, '5-8', 0, NULL, 5, 8)
            """.trimIndent()
        )
    }

    private fun SupportSQLiteDatabase.assertRoutineExerciseWasPreservedWithAccessoryRole() {
        query("SELECT name, targetRepsMin, targetRepsMax, progressionRole FROM routine_exercises WHERE id = 1")
            .use { cursor ->
                check(cursor.moveToFirst())
                assertEquals("Squat", cursor.getString(0))
                assertEquals(5, cursor.getInt(1))
                assertEquals(8, cursor.getInt(2))
                assertEquals("ACCESSORY", cursor.getString(3))
            }
    }

    private fun SupportSQLiteDatabase.assertProfileDefaults() {
        execSQL("INSERT INTO exercise_progression_profiles (variantKey) VALUES ('squat')")
        query(
            """
            SELECT loadIncrementKg, state, pendingConfirmVolume, pendingConfirmStrength,
                nextExposureType, hardExposureStreak, calibrationRetries
            FROM exercise_progression_profiles
            WHERE variantKey = 'squat'
            """.trimIndent()
        ).use { cursor ->
            check(cursor.moveToFirst())
            assertEquals(2.5, cursor.getDouble(0), 0.0)
            assertEquals("CALIBRATING", cursor.getString(1))
            assertEquals(0, cursor.getInt(2))
            assertEquals(0, cursor.getInt(3))
            assertEquals("VOLUME", cursor.getString(4))
            assertEquals(0, cursor.getInt(5))
            assertEquals(0, cursor.getInt(6))
        }
    }

    private fun SupportSQLiteDatabase.assertDeletedWorkoutExerciseLeavesExposureOrphaned() {
        execSQL(
            """
            INSERT INTO workout_sessions (
                id, routineId, routineNameSnapshot, routineDayId, dayNameSnapshot, startedAt,
                finishedAt, weekNumber, notes, pausedMillis
            ) VALUES (1, NULL, 'Routine', NULL, 'Day', 1, 2, 1, NULL, 0)
            """.trimIndent()
        )
        execSQL(
            """
            INSERT INTO workout_exercises (
                id, sessionId, exerciseTemplateId, performedVariantKey, exerciseNameSnapshot,
                targetRepsSnapshot, notes, position, targetRepsMinSnapshot, targetRepsMaxSnapshot,
                firstSetRir
            ) VALUES (1, 1, NULL, 'squat', 'Squat', '5-8', NULL, 0, 5, 8, 2)
            """.trimIndent()
        )
        execSQL(
            """
            INSERT INTO progression_exposures (
                variantKey, workoutExerciseId, exposureIndex, performedAt, type, prescribedLoadKg,
                prescribedRepMin, prescribedRepMax, prescribedTargetRir, prescribedSets, probeReps,
                probeRir, probeSurplus, outcomeClass, setCompletionRatio, exposureE1rm,
                e1rmConfidence, excludeFromTrend, userFlaggedBadDay, intraSessionAdjustmentSteps,
                decisionReason
            ) VALUES ('squat', 1, 1, 2, 'VOLUME', 100.0, 5, 8, 2, 3, 5, 2, 0,
                'ON_TARGET', 1.0, 123.0, 'MEDIUM', 0, 0, 0, 'Initial exposure')
            """.trimIndent()
        )

        // MigrationTestHelper exposes a raw connection, so enable SQLite foreign keys explicitly.
        execSQL("PRAGMA foreign_keys = ON")
        execSQL("DELETE FROM workout_exercises WHERE id = 1")

        query("SELECT workoutExerciseId FROM progression_exposures WHERE variantKey = 'squat'")
            .use { cursor ->
                check(cursor.moveToFirst())
                assertNull(cursor.getString(0))
            }
        query("SELECT COUNT(*) FROM progression_exposures WHERE variantKey = 'squat'").use { cursor ->
            check(cursor.moveToFirst())
            assertEquals(1, cursor.getInt(0))
        }
    }

    private companion object {
        const val TEST_DB_NAME = "migration-7-8-test"
    }
}
