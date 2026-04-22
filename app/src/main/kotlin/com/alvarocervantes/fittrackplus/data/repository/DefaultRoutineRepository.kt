package com.alvarocervantes.fittrackplus.data.repository

import androidx.room.withTransaction
import com.alvarocervantes.fittrackplus.core.database.FitTrackPlusDatabase
import com.alvarocervantes.fittrackplus.data.local.dao.RoutineDao
import com.alvarocervantes.fittrackplus.data.local.entity.RoutineDayEntity
import com.alvarocervantes.fittrackplus.data.local.entity.RoutineEntity
import com.alvarocervantes.fittrackplus.data.local.entity.RoutineExerciseEntity
import com.alvarocervantes.fittrackplus.data.local.relation.RoutineWithDays
import com.alvarocervantes.fittrackplus.domain.model.RoutineDayDraft
import com.alvarocervantes.fittrackplus.domain.model.RoutineDaySnapshot
import com.alvarocervantes.fittrackplus.domain.model.RoutineDraft
import com.alvarocervantes.fittrackplus.domain.model.RoutineExerciseSnapshot
import com.alvarocervantes.fittrackplus.domain.model.RoutineSnapshot
import com.alvarocervantes.fittrackplus.domain.model.RoutineSummary
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DefaultRoutineRepository @Inject constructor(
    private val database: FitTrackPlusDatabase,
    private val routineDao: RoutineDao
) : RoutineRepository {

    override fun observeRoutines(): Flow<List<RoutineSummary>> {
        return routineDao.observeRoutineSummaries().map { rows ->
            rows.map { row ->
                RoutineSummary(
                    id = row.id,
                    name = row.name,
                    dayCount = row.dayCount,
                    updatedAt = row.updatedAt
                )
            }
        }
    }

    override suspend fun getRoutineSnapshot(routineId: Long): RoutineSnapshot? {
        return routineDao.getRoutineWithDays(routineId)?.toSnapshot()
    }

    override suspend fun createRoutine(draft: RoutineDraft): Long {
        val now = System.currentTimeMillis()
        return database.withTransaction {
            val routineId = routineDao.insertRoutine(
                RoutineEntity(
                    name = draft.name.trim(),
                    createdAt = now,
                    updatedAt = now
                )
            )
            insertDays(routineId = routineId, days = draft.days)
            routineId
        }
    }

    override suspend fun replaceRoutine(routineId: Long, draft: RoutineDraft) {
        val existing = routineDao.getRoutineWithDays(routineId)?.routine ?: return
        val now = System.currentTimeMillis()

        database.withTransaction {
            routineDao.updateRoutine(
                existing.copy(
                    name = draft.name.trim(),
                    updatedAt = now
                )
            )
            routineDao.deleteDaysForRoutine(routineId)
            insertDays(routineId = routineId, days = draft.days)
        }
    }

    override suspend fun archiveRoutine(routineId: Long) {
        routineDao.archiveRoutine(
            routineId = routineId,
            updatedAt = System.currentTimeMillis()
        )
    }

    private suspend fun insertDays(routineId: Long, days: List<RoutineDayDraft>) {
        days.forEachIndexed { dayIndex, dayDraft ->
            val dayId = routineDao.insertDay(
                RoutineDayEntity(
                    routineId = routineId,
                    name = dayDraft.name.trim().ifBlank { "Sesion ${dayIndex + 1}" },
                    position = dayIndex
                )
            )

            dayDraft.exercises.forEachIndexed { exerciseIndex, exerciseDraft ->
                routineDao.insertExercise(
                    RoutineExerciseEntity(
                        routineDayId = dayId,
                        name = exerciseDraft.name.trim(),
                        targetSets = exerciseDraft.targetSets,
                        targetRepsText = exerciseDraft.targetRepsText.trim(),
                        position = exerciseIndex,
                        notes = exerciseDraft.notes?.trim()?.ifBlank { null }
                    )
                )
            }
        }
    }
}

private fun RoutineWithDays.toSnapshot(): RoutineSnapshot {
    return RoutineSnapshot(
        id = routine.id,
        name = routine.name,
        days = days
            .sortedBy { it.day.position }
            .map { dayWithExercises ->
                RoutineDaySnapshot(
                    id = dayWithExercises.day.id,
                    name = dayWithExercises.day.name,
                    position = dayWithExercises.day.position,
                    exercises = dayWithExercises.exercises
                        .sortedBy { it.position }
                        .map { exercise ->
                            RoutineExerciseSnapshot(
                                id = exercise.id,
                                name = exercise.name,
                                targetSets = exercise.targetSets,
                                targetRepsText = exercise.targetRepsText,
                                position = exercise.position,
                                notes = exercise.notes
                            )
                        }
                )
            }
    )
}
