package com.alvarocervantes.fittrackplus.data.local.seed

import androidx.room.withTransaction
import com.alvarocervantes.fittrackplus.core.database.FitTrackPlusDatabase
import com.alvarocervantes.fittrackplus.data.local.dao.RoutineDao
import com.alvarocervantes.fittrackplus.data.local.dao.WorkoutDao
import com.alvarocervantes.fittrackplus.data.local.entity.RoutineDayEntity
import com.alvarocervantes.fittrackplus.data.local.entity.RoutineEntity
import com.alvarocervantes.fittrackplus.data.local.entity.RoutineExerciseEntity
import com.alvarocervantes.fittrackplus.data.local.entity.WorkoutExerciseEntity
import com.alvarocervantes.fittrackplus.data.local.entity.WorkoutSessionEntity
import com.alvarocervantes.fittrackplus.data.local.entity.WorkoutSetEntity
import com.alvarocervantes.fittrackplus.data.preferences.UserPreferencesRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Singleton
class DebugDemoDataSeeder @Inject constructor(
    private val database: FitTrackPlusDatabase,
    private val routineDao: RoutineDao,
    private val workoutDao: WorkoutDao,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun seedIfEmpty() {
        scope.launch {
            runCatching {
                val hasData = routineDao.countRoutines() > 0 || workoutDao.countSessions() > 0
                if (!hasData) {
                    seedDemoData()
                }
            }
        }
    }

    suspend fun reseed() {
        database.withTransaction {
            workoutDao.deleteAllSessions()
            routineDao.deleteAllRoutines()
        }
        userPreferencesRepository.setActiveRoutineId(null)
        seedDemoData()
    }

    private suspend fun seedDemoData() {
        val now = System.currentTimeMillis()
        val dayMillis = 24L * 60L * 60L * 1000L
        val catalog = buildDebugSeedCatalog()

        var activeRoutineId: Long? = null
        database.withTransaction {
            catalog.routines.forEach { routine ->
                val routineId = routineDao.insertRoutine(
                    RoutineEntity(
                        name = routine.name,
                        createdAt = now - (routine.createdDaysAgo * dayMillis),
                        updatedAt = now - (routine.updatedDaysAgo * dayMillis)
                    )
                )
                if (routine.name == catalog.activeRoutineName) {
                    activeRoutineId = routineId
                }

                val seededDays = routine.days.mapIndexed { dayIndex, day ->
                    insertDay(
                        routineId = routineId,
                        name = day.name,
                        position = dayIndex,
                        exercises = day.exercises.map { exercise ->
                            DemoExercise(
                                name = exercise.name,
                                targetSets = exercise.targetSets,
                                targetReps = exercise.targetReps
                            )
                        }
                    )
                }

                routine.finishedSessions.forEach { session ->
                    val baseDay = seededDays[session.dayIndex]
                    insertFinishedSession(
                        routineId = routineId,
                        routineName = routine.name,
                        day = session.snapshotDayName?.let { snapshotName ->
                            baseDay.copy(name = snapshotName)
                        } ?: baseDay,
                        weekNumber = session.weekNumber,
                        startedAt = now - (session.startedDaysAgo * dayMillis),
                        minutes = session.minutes,
                        setValues = session.setValues
                    )
                }
            }
        }

        activeRoutineId?.let { userPreferencesRepository.setActiveRoutineId(it) }
    }

    private suspend fun insertDay(
        routineId: Long,
        name: String,
        position: Int,
        exercises: List<DemoExercise>
    ): DemoDay {
        val dayId = routineDao.insertDay(
            RoutineDayEntity(
                routineId = routineId,
                name = name,
                position = position
            )
        )
        val demoExercises = exercises.mapIndexed { index, exercise ->
            val exerciseId = routineDao.insertExercise(
                RoutineExerciseEntity(
                    routineDayId = dayId,
                    variantKey = "exercise-$dayId-$index",
                    defaultVariantKey = "exercise-$dayId-$index",
                    name = exercise.name,
                    targetSets = exercise.targetSets,
                    targetRepsText = exercise.targetReps,
                    position = index
                )
            )
            DemoWorkoutExercise(
                id = exerciseId,
                name = exercise.name,
                targetSets = exercise.targetSets,
                targetReps = exercise.targetReps,
                position = index
            )
        }
        return DemoDay(
            id = dayId,
            name = name,
            position = position,
            exercises = demoExercises
        )
    }

    private suspend fun insertFinishedSession(
        routineId: Long,
        routineName: String,
        day: DemoDay,
        weekNumber: Int,
        startedAt: Long,
        minutes: Int,
        setValues: List<List<Pair<Double, Int>>>
    ) {
        val sessionId = workoutDao.insertSession(
            WorkoutSessionEntity(
                routineId = routineId,
                routineNameSnapshot = routineName,
                routineDayId = day.id,
                dayNameSnapshot = day.name,
                startedAt = startedAt,
                finishedAt = startedAt + minutes * 60L * 1000L,
                weekNumber = weekNumber
            )
        )

        day.exercises.forEachIndexed { exerciseIndex, exercise ->
            val workoutExerciseId = workoutDao.insertExercise(
                WorkoutExerciseEntity(
                    sessionId = sessionId,
                    exerciseTemplateId = exercise.id,
                    performedVariantKey = "exercise-${exercise.id}",
                    exerciseNameSnapshot = exercise.name,
                    targetRepsSnapshot = exercise.targetReps,
                    position = exercise.position
                )
            )
            val values = setValues.getOrNull(exerciseIndex).orEmpty()
            repeat(exercise.targetSets) { setIndex ->
                val value = values.getOrNull(setIndex) ?: (0.0 to 0)
                workoutDao.insertSet(
                    WorkoutSetEntity(
                        workoutExerciseId = workoutExerciseId,
                        setNumber = setIndex + 1,
                        weightKg = value.first,
                        reps = value.second
                    )
                )
            }
        }
    }
}

private data class DemoExercise(
    val name: String,
    val targetSets: Int,
    val targetReps: String
)

private data class DemoDay(
    val id: Long,
    val name: String,
    val position: Int,
    val exercises: List<DemoWorkoutExercise>
)

private data class DemoWorkoutExercise(
    val id: Long,
    val name: String,
    val targetSets: Int,
    val targetReps: String,
    val position: Int
)
