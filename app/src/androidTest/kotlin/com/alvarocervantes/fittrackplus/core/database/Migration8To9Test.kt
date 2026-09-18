package com.alvarocervantes.fittrackplus.core.database

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * v9 adds the load actually lifted on the probe set. Exposures written before it must survive the
 * migration untouched, with the new column NULL: the engine then falls back to the prescribed load,
 * which is the only information those rows ever had.
 */
@RunWith(AndroidJUnit4::class)
class Migration8To9Test {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        FitTrackPlusDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun existingExposuresSurviveWithNullLiftedLoad() {
        helper.createDatabase(TEST_DB_NAME, 8).apply {
            execSQL(
                """
                INSERT INTO progression_exposures (
                    variantKey, workoutExerciseId, exposureIndex, performedAt, type,
                    prescribedLoadKg, prescribedRepMin, prescribedRepMax, prescribedTargetRir,
                    prescribedSets, probeReps, probeRir, probeSurplus, outcomeClass,
                    setCompletionRatio, exposureE1rm, e1rmConfidence, excludeFromTrend,
                    userFlaggedBadDay, intraSessionAdjustmentSteps, decisionReason
                ) VALUES ('dominadas', NULL, 1, 1, 'STRENGTH', 10.0, 5, 5, 2, 3, 9, 3, 5,
                    'STRONG', 1.0, NULL, NULL, 0, 0, 0, 'CALIBRATION_BASELINE')
                """.trimIndent()
            )
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB_NAME, 9, true, MIGRATION_8_9).apply {
            query(
                "SELECT prescribedLoadKg, probeReps, probeLoadKg FROM progression_exposures " +
                    "WHERE variantKey = 'dominadas'"
            ).use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(10.0, cursor.getDouble(0), 0.0)
                assertEquals(9, cursor.getInt(1))
                assertTrue("probeLoadKg must be NULL on migrated rows", cursor.isNull(2))
            }
            close()
        }
    }

    @Test
    fun newExposuresCanStoreTheLiftedLoad() {
        helper.createDatabase(TEST_DB_NAME, 8).close()

        helper.runMigrationsAndValidate(TEST_DB_NAME, 9, true, MIGRATION_8_9).apply {
            execSQL(
                """
                INSERT INTO progression_exposures (
                    variantKey, workoutExerciseId, exposureIndex, performedAt, type,
                    prescribedLoadKg, prescribedRepMin, prescribedRepMax, prescribedTargetRir,
                    prescribedSets, probeLoadKg, probeReps, probeRir, probeSurplus, outcomeClass,
                    setCompletionRatio, exposureE1rm, e1rmConfidence, excludeFromTrend,
                    userFlaggedBadDay, intraSessionAdjustmentSteps, decisionReason
                ) VALUES ('dominadas', NULL, 1, 1, 'STRENGTH', 10.0, 5, 5, 2, 3, 15.0, 9, 3, 5,
                    'STRONG', 1.0, NULL, NULL, 0, 0, 0, 'CALIBRATION_BASELINE')
                """.trimIndent()
            )
            query("SELECT prescribedLoadKg, probeLoadKg FROM progression_exposures").use { cursor ->
                assertTrue(cursor.moveToFirst())
                // Prescribed and lifted live side by side: that pair is what the engine learns from.
                assertEquals(10.0, cursor.getDouble(0), 0.0)
                assertEquals(15.0, cursor.getDouble(1), 0.0)
            }
            close()
        }
    }

    private companion object {
        const val TEST_DB_NAME = "migration-8-9-test"
    }
}
