package com.alvarocervantes.fittrackplus.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.alvarocervantes.fittrackplus.data.local.entity.WorkoutExerciseEntity
import com.alvarocervantes.fittrackplus.data.local.entity.WorkoutSessionEntity
import com.alvarocervantes.fittrackplus.data.local.entity.WorkoutSetEntity
import com.alvarocervantes.fittrackplus.data.local.relation.WorkoutSessionWithExercises
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workout_sessions ORDER BY startedAt DESC")
    fun observeSessions(): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_sessions WHERE finishedAt IS NOT NULL ORDER BY finishedAt DESC, startedAt DESC")
    fun observeFinishedSessions(): Flow<List<WorkoutSessionEntity>>

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE finishedAt IS NOT NULL ORDER BY finishedAt DESC, startedAt DESC")
    fun observeFinishedSessionsWithExercises(): Flow<List<WorkoutSessionWithExercises>>

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId")
    suspend fun getSessionWithExercises(sessionId: Long): WorkoutSessionWithExercises?

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId AND finishedAt IS NOT NULL")
    suspend fun getFinishedSessionWithExercises(sessionId: Long): WorkoutSessionWithExercises?

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE finishedAt IS NULL ORDER BY startedAt DESC LIMIT 1")
    suspend fun getActiveSessionWithExercises(): WorkoutSessionWithExercises?

    @Query("SELECT COUNT(*) FROM workout_sessions WHERE routineId = :routineId AND finishedAt IS NOT NULL")
    suspend fun countFinishedSessionsForRoutine(routineId: Long): Int

    @Query("SELECT COUNT(*) FROM workout_sessions")
    suspend fun countSessions(): Int

    @Query("DELETE FROM workout_sessions")
    suspend fun deleteAllSessions()

    @Insert
    suspend fun insertSession(session: WorkoutSessionEntity): Long

    @Insert
    suspend fun insertExercise(exercise: WorkoutExerciseEntity): Long

    @Insert
    suspend fun insertSet(set: WorkoutSetEntity): Long

    @Update
    suspend fun updateSet(set: WorkoutSetEntity)

    @Update
    suspend fun updateSession(session: WorkoutSessionEntity)

    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId")
    suspend fun getSession(sessionId: Long): WorkoutSessionEntity?

    @Query("SELECT * FROM workout_sets WHERE id = :setId")
    suspend fun getSet(setId: Long): WorkoutSetEntity?

    @Query("""
        SELECT ws.weightKg FROM workout_sets ws
        INNER JOIN workout_exercises we ON ws.workoutExerciseId = we.id
        INNER JOIN workout_sessions sess ON we.sessionId = sess.id
        WHERE we.exerciseNameSnapshot = :exerciseName
        AND sess.finishedAt IS NOT NULL
        AND ws.setNumber = :setNumber
        ORDER BY sess.startedAt DESC
        LIMIT 1
    """)
    suspend fun getLastWeightKgForExerciseSet(exerciseName: String, setNumber: Int): Double?

    @Query("""
        SELECT MAX(ws.weightKg) FROM workout_sets ws
        INNER JOIN workout_exercises we ON ws.workoutExerciseId = we.id
        INNER JOIN workout_sessions sess ON we.sessionId = sess.id
        WHERE LOWER(TRIM(we.exerciseNameSnapshot)) = LOWER(TRIM(:exerciseName))
        AND sess.finishedAt IS NOT NULL
        AND ws.reps > 0
    """)
    suspend fun getMaxWeightForExercise(exerciseName: String): Double?

    @Query("""
        SELECT MAX(ws.weightKg * ws.reps) FROM workout_sets ws
        INNER JOIN workout_exercises we ON ws.workoutExerciseId = we.id
        INNER JOIN workout_sessions sess ON we.sessionId = sess.id
        WHERE LOWER(TRIM(we.exerciseNameSnapshot)) = LOWER(TRIM(:exerciseName))
        AND sess.finishedAt IS NOT NULL
        AND ws.reps > 0
        AND ws.weightKg > 0
    """)
    suspend fun getMaxSetVolumeForExercise(exerciseName: String): Double?
}
