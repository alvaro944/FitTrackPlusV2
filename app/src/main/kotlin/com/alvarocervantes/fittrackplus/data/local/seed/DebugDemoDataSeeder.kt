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

        var activeRoutineId: Long? = null
        database.withTransaction {
            val routineId = routineDao.insertRoutine(
                RoutineEntity(
                    name = "PPL Demo",
                    createdAt = now - 12L * dayMillis,
                    updatedAt = now - 2L * dayMillis
                )
            )
            activeRoutineId = routineId

            val push = insertDay(
                routineId = routineId,
                name = "Push",
                position = 0,
                exercises = listOf(
                    DemoExercise("Press banca", 3, "8-10"),
                    DemoExercise("Press militar", 3, "8-10"),
                    DemoExercise("Fondos asistidos", 2, "10-12")
                )
            )
            val pull = insertDay(
                routineId = routineId,
                name = "Pull",
                position = 1,
                exercises = listOf(
                    DemoExercise("Dominadas asistidas", 3, "6-8"),
                    DemoExercise("Remo con barra", 3, "8-10"),
                    DemoExercise("Curl biceps", 2, "10-12")
                )
            )
            val legs = insertDay(
                routineId = routineId,
                name = "Legs",
                position = 2,
                exercises = listOf(
                    DemoExercise("Sentadilla", 3, "6-8"),
                    DemoExercise("Peso muerto rumano", 3, "8-10"),
                    DemoExercise("Prensa", 2, "10-12")
                )
            )

            insertFinishedSession(
                routineId = routineId,
                routineName = "PPL Demo",
                day = push,
                weekNumber = 1,
                startedAt = now - 9L * dayMillis,
                minutes = 58,
                setValues = listOf(
                    listOf(60.0 to 10, 62.5 to 9, 62.5 to 8),
                    listOf(35.0 to 10, 37.5 to 9, 37.5 to 8),
                    listOf(0.0 to 12, 0.0 to 11)
                )
            )
            insertFinishedSession(
                routineId = routineId,
                routineName = "PPL Demo",
                day = pull,
                weekNumber = 1,
                startedAt = now - 7L * dayMillis,
                minutes = 52,
                setValues = listOf(
                    listOf(0.0 to 8, 0.0 to 7, 0.0 to 7),
                    listOf(50.0 to 10, 52.5 to 9, 52.5 to 8),
                    listOf(12.5 to 12, 12.5 to 11)
                )
            )
            insertFinishedSession(
                routineId = routineId,
                routineName = "PPL Demo",
                day = legs,
                weekNumber = 1,
                startedAt = now - 5L * dayMillis,
                minutes = 64,
                setValues = listOf(
                    listOf(80.0 to 8, 82.5 to 7, 82.5 to 6),
                    listOf(65.0 to 10, 67.5 to 9, 67.5 to 8),
                    listOf(140.0 to 12, 145.0 to 10)
                )
            )
            insertFinishedSession(
                routineId = routineId,
                routineName = "PPL Demo",
                day = push.copy(name = "Push snapshot antiguo"),
                weekNumber = 2,
                startedAt = now - 2L * dayMillis,
                minutes = 56,
                setValues = listOf(
                    listOf(62.5 to 10, 65.0 to 9, 65.0 to 8),
                    listOf(37.5 to 10, 40.0 to 8, 40.0 to 8),
                    listOf(0.0 to 12, 0.0 to 12)
                )
            )
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
