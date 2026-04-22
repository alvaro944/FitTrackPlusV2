package com.alvarocervantes.fittrackplus.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.alvarocervantes.fittrackplus.data.local.entity.RoutineDayEntity
import com.alvarocervantes.fittrackplus.data.local.entity.RoutineEntity
import com.alvarocervantes.fittrackplus.data.local.entity.RoutineExerciseEntity
import com.alvarocervantes.fittrackplus.data.local.relation.RoutineWithDays
import kotlinx.coroutines.flow.Flow

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

    @Query("SELECT COUNT(*) FROM routines")
    suspend fun countRoutines(): Int

    @Insert
    suspend fun insertRoutine(routine: RoutineEntity): Long

    @Insert
    suspend fun insertDay(day: RoutineDayEntity): Long

    @Insert
    suspend fun insertExercise(exercise: RoutineExerciseEntity): Long

    @Query("DELETE FROM routine_days WHERE routineId = :routineId")
    suspend fun deleteDaysForRoutine(routineId: Long)

    @Update
    suspend fun updateRoutine(routine: RoutineEntity)

    @Query("UPDATE routines SET isArchived = 1, updatedAt = :updatedAt WHERE id = :routineId")
    suspend fun archiveRoutine(routineId: Long, updatedAt: Long)
}

data class RoutineSummaryRow(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isArchived: Boolean,
    val dayCount: Int
)
