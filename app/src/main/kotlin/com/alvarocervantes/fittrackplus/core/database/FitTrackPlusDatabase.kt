package com.alvarocervantes.fittrackplus.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.alvarocervantes.fittrackplus.data.local.dao.RoutineDao
import com.alvarocervantes.fittrackplus.data.local.dao.WorkoutDao
import com.alvarocervantes.fittrackplus.data.local.entity.RoutineDayEntity
import com.alvarocervantes.fittrackplus.data.local.entity.RoutineEntity
import com.alvarocervantes.fittrackplus.data.local.entity.RoutineExerciseAlternativeEntity
import com.alvarocervantes.fittrackplus.data.local.entity.RoutineExerciseEntity
import com.alvarocervantes.fittrackplus.data.local.entity.WorkoutExerciseEntity
import com.alvarocervantes.fittrackplus.data.local.entity.WorkoutSessionEntity
import com.alvarocervantes.fittrackplus.data.local.entity.WorkoutSetEntity

@Database(
    entities = [
        RoutineEntity::class,
        RoutineDayEntity::class,
        RoutineExerciseEntity::class,
        RoutineExerciseAlternativeEntity::class,
        WorkoutSessionEntity::class,
        WorkoutExerciseEntity::class,
        WorkoutSetEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class FitTrackPlusDatabase : RoomDatabase() {
    abstract fun routineDao(): RoutineDao
    abstract fun workoutDao(): WorkoutDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `routine_exercise_alternatives` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `routineExerciseId` INTEGER NOT NULL,
                `variantKey` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `targetSets` INTEGER NOT NULL,
                `targetRepsText` TEXT NOT NULL,
                `position` INTEGER NOT NULL,
                `notes` TEXT,
                FOREIGN KEY(`routineExerciseId`) REFERENCES `routine_exercises`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        database.execSQL(
            """
            CREATE INDEX IF NOT EXISTS `index_routine_exercise_alternatives_routineExerciseId`
            ON `routine_exercise_alternatives` (`routineExerciseId`)
            """.trimIndent()
        )
        database.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS `index_routine_exercise_alternatives_routineExerciseId_position`
            ON `routine_exercise_alternatives` (`routineExerciseId`, `position`)
            """.trimIndent()
        )
        database.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS `index_routine_exercise_alternatives_variantKey`
            ON `routine_exercise_alternatives` (`variantKey`)
            """.trimIndent()
        )

        database.execSQL(
            "ALTER TABLE `routine_exercises` ADD COLUMN `variantKey` TEXT NOT NULL DEFAULT ''"
        )
        database.execSQL(
            "ALTER TABLE `routine_exercises` ADD COLUMN `defaultVariantKey` TEXT NOT NULL DEFAULT ''"
        )
        database.execSQL(
            """
            UPDATE `routine_exercises`
            SET `variantKey` = 'exercise-' || `id`,
                `defaultVariantKey` = 'exercise-' || `id`
            """
        )

        database.execSQL(
            "ALTER TABLE `workout_exercises` ADD COLUMN `performedVariantKey` TEXT NOT NULL DEFAULT ''"
        )
        database.execSQL(
            """
            UPDATE `workout_exercises`
            SET `performedVariantKey` = CASE
                WHEN `exerciseTemplateId` IS NOT NULL THEN 'exercise-' || `exerciseTemplateId`
                ELSE 'legacy:' || LOWER(TRIM(`exerciseNameSnapshot`))
            END
            """
        )
    }
}
