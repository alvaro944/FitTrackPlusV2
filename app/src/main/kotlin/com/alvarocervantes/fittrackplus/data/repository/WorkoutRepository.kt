package com.alvarocervantes.fittrackplus.data.repository

import com.alvarocervantes.fittrackplus.data.local.entity.WorkoutSessionEntity
import com.alvarocervantes.fittrackplus.data.local.relation.WorkoutSessionWithExercises
import com.alvarocervantes.fittrackplus.domain.model.RoutineDaySnapshot
import com.alvarocervantes.fittrackplus.domain.model.RoutineSnapshot
import kotlinx.coroutines.flow.Flow

@Suppress("TooManyFunctions")
interface WorkoutRepository {
    fun observeSessions(): Flow<List<WorkoutSessionEntity>>
    fun observeFinishedSessions(): Flow<List<WorkoutSessionEntity>>
    fun observeFinishedSessionsWithExercises(): Flow<List<WorkoutSessionWithExercises>>
    fun observeActiveSession(): Flow<WorkoutSessionWithExercises?>
    suspend fun getActiveSessionWithExercises(): WorkoutSessionWithExercises?
    suspend fun getSessionWithExercises(sessionId: Long): WorkoutSessionWithExercises?
    suspend fun getFinishedSessionWithExercises(sessionId: Long): WorkoutSessionWithExercises?
    suspend fun countFinishedSessionsForRoutine(routineId: Long): Int
    suspend fun countSessions(): Int
    suspend fun createSessionFromRoutineDay(
        routine: RoutineSnapshot,
        day: RoutineDaySnapshot,
        weekNumber: Int
    ): Long
    /**
     * Whether this exercise already holds work: any set with weight, reps or a completion mark.
     * Callers use it to decide whether swapping a variant should rebuild the sets or keep them.
     */
    suspend fun workoutExerciseHasLoggedSets(workoutExerciseId: Long): Boolean =
        error("Not implemented")

    /**
     * Points this session's exercise at another variant.
     *
     * With [keepLoggedSets] false the sets are rebuilt from the new variant's prescription, which
     * is what you want before training it. With it true only the labels and targets change and the
     * logged sets stay: the reps really happened, the variant they were filed under was just wrong.
     */
    suspend fun replaceWorkoutExerciseVariant(
        workoutExerciseId: Long,
        variantKey: String,
        exerciseName: String,
        targetRepsText: String,
        targetSets: Int,
        notes: String? = null,
        keepLoggedSets: Boolean = false
    ): Boolean = error("Not implemented")
    suspend fun updateSet(setId: Long, weightKg: Double, reps: Int)
    suspend fun updateSetCompletion(setId: Long, isCompleted: Boolean) = Unit
    suspend fun updateSetNotes(setId: Long, notes: String?) = Unit
    suspend fun finishSession(sessionId: Long, notes: String? = null)
    suspend fun discardSession(sessionId: Long): Unit = error("Not implemented")
    suspend fun reopenSession(sessionId: Long): Unit = error("Not implemented")
    suspend fun getLastWeightKgForExerciseSet(variantKey: String, setNumber: Int): Double?
    suspend fun getLastRepsForExerciseSet(variantKey: String, setNumber: Int): Int?
    suspend fun getMaxWeightForExercise(variantKey: String): Double?
    suspend fun getMaxSetVolumeForExercise(variantKey: String): Double?
    suspend fun getRecentAverageRepsForExercise(variantKey: String, limit: Int): List<Double> = emptyList()
}
