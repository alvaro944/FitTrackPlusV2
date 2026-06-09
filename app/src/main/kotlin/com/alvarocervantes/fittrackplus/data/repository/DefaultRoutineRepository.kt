package com.alvarocervantes.fittrackplus.data.repository

import androidx.room.withTransaction
import com.alvarocervantes.fittrackplus.core.database.FitTrackPlusDatabase
import com.alvarocervantes.fittrackplus.data.local.dao.RoutineDao
import com.alvarocervantes.fittrackplus.data.local.entity.RoutineDayEntity
import com.alvarocervantes.fittrackplus.data.local.entity.RoutineEntity
import com.alvarocervantes.fittrackplus.data.local.entity.RoutineExerciseAlternativeEntity
import com.alvarocervantes.fittrackplus.data.local.entity.RoutineExerciseEntity
import com.alvarocervantes.fittrackplus.data.local.relation.RoutineWithDays
import com.alvarocervantes.fittrackplus.domain.model.RoutineDayDraft
import com.alvarocervantes.fittrackplus.domain.model.RoutineDaySnapshot
import com.alvarocervantes.fittrackplus.domain.model.RoutineDraft
import com.alvarocervantes.fittrackplus.domain.model.RoutineExerciseAlternativeDraft
import com.alvarocervantes.fittrackplus.domain.model.RoutineExerciseAlternativeSnapshot
import com.alvarocervantes.fittrackplus.domain.model.RoutineExerciseSnapshot
import com.alvarocervantes.fittrackplus.domain.model.RoutineSnapshot
import com.alvarocervantes.fittrackplus.domain.model.RoutineSummary
import java.util.UUID
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

    override fun observeArchivedRoutines(): Flow<List<RoutineSummary>> {
        return routineDao.observeArchivedRoutineSummaries().map { rows ->
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

    override suspend fun createExerciseAlternative(
        routineExerciseId: Long,
        draft: RoutineExerciseAlternativeDraft
    ): RoutineExerciseAlternativeSnapshot {
        return database.withTransaction {
            val routine = routineDao.getRoutineWithDaysForExercise(routineExerciseId)
                ?: error("No se encontro el ejercicio base para crear una alternativa.")
            val exerciseWithAlternatives = routine.days
                .flatMap { it.exercises }
                .firstOrNull { it.exercise.id == routineExerciseId }
                ?: error("No se encontro el ejercicio base para crear una alternativa.")

            val alternative = RoutineExerciseAlternativeEntity(
                routineExerciseId = routineExerciseId,
                variantKey = draft.variantKey ?: newVariantKey(),
                name = draft.name.trim(),
                targetSets = draft.targetSets,
                targetRepsText = draft.targetRepsText.trim(),
                position = exerciseWithAlternatives.alternatives.size,
                notes = draft.notes?.trim()?.ifBlank { null }
            )
            val alternativeId = routineDao.insertExerciseAlternative(alternative)
            alternative.toSnapshot(id = alternativeId)
        }
    }

    override suspend fun setExerciseDefaultVariant(routineExerciseId: Long, variantKey: String) {
        database.withTransaction {
            routineDao.updateExerciseDefaultVariant(
                routineExerciseId = routineExerciseId,
                defaultVariantKey = variantKey
            )
        }
    }

    override suspend fun archiveRoutine(routineId: Long) {
        routineDao.archiveRoutine(
            routineId = routineId,
            updatedAt = System.currentTimeMillis()
        )
    }

    override suspend fun restoreRoutine(routineId: Long) {
        routineDao.restoreRoutine(
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
                val variantKey = exerciseDraft.variantKey ?: newVariantKey()
                val exerciseId = routineDao.insertExercise(
                    RoutineExerciseEntity(
                        routineDayId = dayId,
                        variantKey = variantKey,
                        defaultVariantKey = exerciseDraft.defaultVariantKey ?: variantKey,
                        name = exerciseDraft.name.trim(),
                        targetSets = exerciseDraft.targetSets,
                        targetRepsText = exerciseDraft.targetRepsText.trim(),
                        position = exerciseIndex,
                        notes = exerciseDraft.notes?.trim()?.ifBlank { null }
                    )
                )
                exerciseDraft.alternatives.forEachIndexed { alternativeIndex, alternativeDraft ->
                    routineDao.insertExerciseAlternative(
                        RoutineExerciseAlternativeEntity(
                            routineExerciseId = exerciseId,
                            variantKey = alternativeDraft.variantKey ?: newVariantKey(),
                            name = alternativeDraft.name.trim(),
                            targetSets = alternativeDraft.targetSets,
                            targetRepsText = alternativeDraft.targetRepsText.trim(),
                            position = alternativeIndex,
                            notes = alternativeDraft.notes?.trim()?.ifBlank { null }
                        )
                    )
                }
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
                        .sortedBy { it.exercise.position }
                        .map { exerciseWithAlternatives ->
                            val exercise = exerciseWithAlternatives.exercise
                            RoutineExerciseSnapshot(
                                id = exercise.id,
                                variantKey = exercise.variantKey,
                                defaultVariantKey = exercise.defaultVariantKey,
                                name = exercise.name,
                                targetSets = exercise.targetSets,
                                targetRepsText = exercise.targetRepsText,
                                position = exercise.position,
                                notes = exercise.notes,
                                alternatives = exerciseWithAlternatives.alternatives
                                    .sortedBy { it.position }
                                    .map { alternative ->
                                        alternative.toSnapshot()
                                    }
                            )
                        }
                )
            }
    )
}

private fun RoutineExerciseAlternativeEntity.toSnapshot(id: Long = this.id): RoutineExerciseAlternativeSnapshot {
    return RoutineExerciseAlternativeSnapshot(
        id = id,
        variantKey = variantKey,
        name = name,
        targetSets = targetSets,
        targetRepsText = targetRepsText,
        position = position,
        notes = notes
    )
}

private fun newVariantKey(): String = UUID.randomUUID().toString()
