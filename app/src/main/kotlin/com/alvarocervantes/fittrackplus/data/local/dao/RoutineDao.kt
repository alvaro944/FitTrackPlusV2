package com.alvarocervantes.fittrackplus.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.alvarocervantes.fittrackplus.data.local.entity.RoutineDayEntity
import com.alvarocervantes.fittrackplus.data.local.entity.RoutineEntity
import com.alvarocervantes.fittrackplus.data.local.entity.RoutineExerciseAlternativeEntity
import com.alvarocervantes.fittrackplus.data.local.entity.RoutineExerciseEntity
import com.alvarocervantes.fittrackplus.data.local.relation.RoutineWithDays
import kotlinx.coroutines.flow.Flow

@Suppress("TooManyFunctions")
@Dao
interface RoutineDao {
    @Query(
        """
        SELECT r.id, r.name, r.createdAt, r.updatedAt, r.isArchived, COUNT(d.id) AS dayCount
        FROM routines r
        LEFT JOIN routine_days d ON d.routineId = r.id
        WHERE r.isArchived = 0
        GROUP BY r.id
        ORDER BY r.updatedAt DESC
        """
    )
    fun observeRoutineSummaries(): Flow<List<RoutineSummaryRow>>

    @Transaction
    @Query("SELECT * FROM routines WHERE id = :routineId AND isArchived = 0")
    suspend fun getRoutineWithDays(routineId: Long): RoutineWithDays?

    @Transaction
    @Query(
        """
        SELECT r.* FROM routines r
        INNER JOIN routine_days d ON d.routineId = r.id
        INNER JOIN routine_exercises e ON e.routineDayId = d.id
        WHERE e.id = :routineExerciseId
        LIMIT 1
        """
    )
    suspend fun getRoutineWithDaysForExercise(routineExerciseId: Long): RoutineWithDays?

    @Query("SELECT COUNT(*) FROM routines")
    suspend fun countRoutines(): Int

    @Query("DELETE FROM routines")
    suspend fun deleteAllRoutines()

    @Insert
    suspend fun insertRoutine(routine: RoutineEntity): Long

    @Insert
    suspend fun insertDay(day: RoutineDayEntity): Long

    @Insert
    suspend fun insertExercise(exercise: RoutineExerciseEntity): Long

    @Insert
    suspend fun insertExerciseAlternative(alternative: RoutineExerciseAlternativeEntity): Long

    @Query("DELETE FROM routine_days WHERE routineId = :routineId")
    suspend fun deleteDaysForRoutine(routineId: Long)

    @Update
    suspend fun updateRoutine(routine: RoutineEntity)

    @Query(
        """
        UPDATE routine_exercises
        SET defaultVariantKey = :defaultVariantKey
        WHERE id = :routineExerciseId
        """
    )
    suspend fun updateExerciseDefaultVariant(
        routineExerciseId: Long,
        defaultVariantKey: String
    )

    @Query("UPDATE routines SET isArchived = 1, updatedAt = :updatedAt WHERE id = :routineId")
    suspend fun archiveRoutine(routineId: Long, updatedAt: Long)

    @Query(
        """
        SELECT r.id, r.name, r.createdAt, r.updatedAt, r.isArchived, COUNT(d.id) AS dayCount
        FROM routines r
        LEFT JOIN routine_days d ON d.routineId = r.id
        WHERE r.isArchived = 1
        GROUP BY r.id
        ORDER BY r.updatedAt DESC
        """
    )
    fun observeArchivedRoutineSummaries(): Flow<List<RoutineSummaryRow>>

    @Query("UPDATE routines SET isArchived = 0, updatedAt = :updatedAt WHERE id = :routineId")
    suspend fun restoreRoutine(routineId: Long, updatedAt: Long)
}

data class RoutineSummaryRow(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isArchived: Boolean,
    val dayCount: Int
)
