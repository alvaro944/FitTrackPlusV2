package com.alvarocervantes.fittrackplus.domain.usecase

import com.alvarocervantes.fittrackplus.data.repository.RoutineRepository
import com.alvarocervantes.fittrackplus.data.repository.WorkoutRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

data class UserDataExport(
    val fileName: String,
    val content: String
)

class ExportUserDataUseCase @Inject constructor(
    private val routineRepository: RoutineRepository,
    private val workoutRepository: WorkoutRepository
) {
    @Suppress("CyclomaticComplexMethod")
    suspend operator fun invoke(nowMillis: Long = System.currentTimeMillis()): UserDataExport {
        val routineSummaries = (routineRepository.observeRoutines().first() +
            routineRepository.observeArchivedRoutines().first()).distinctBy { it.id }
        val routines = JSONArray().apply {
            routineSummaries.forEach { summary ->
                routineRepository.getRoutineSnapshot(summary.id)?.let { routine ->
                    put(JSONObject().apply {
                        put("id", routine.id)
                        put("name", routine.name)
                        put("days", JSONArray().apply {
                            routine.days.forEach { day -> put(JSONObject().apply {
                                put("id", day.id); put("name", day.name); put("position", day.position)
                                put("exercises", JSONArray().apply {
                                    day.exercises.forEach { exercise -> put(JSONObject().apply {
                                        put("id", exercise.id); put("name", exercise.name)
                                        put("variantKey", exercise.variantKey); put("defaultVariantKey", exercise.defaultVariantKey)
                                        put("targetSets", exercise.targetSets); put("targetReps", exercise.targetRepsText)
                                        put("targetRepsMin", exercise.targetRepsMin); put("targetRepsMax", exercise.targetRepsMax)
                                        put("notes", exercise.notes); put("position", exercise.position)
                                    }) }
                                })
                            }) }
                        })
                    })
                }
            }
        }
        val sessions = JSONArray().apply {
            workoutRepository.observeSessions().first().forEach { session ->
                workoutRepository.getSessionWithExercises(session.id)?.let { detail ->
                    put(JSONObject().apply {
                        put("id", session.id); put("routineName", session.routineNameSnapshot)
                        put("dayName", session.dayNameSnapshot); put("startedAt", session.startedAt)
                        put("finishedAt", session.finishedAt); put("weekNumber", session.weekNumber); put("notes", session.notes)
                        put("exercises", JSONArray().apply {
                            detail.exercises.forEach { exercise -> put(JSONObject().apply {
                                put("id", exercise.exercise.id); put("name", exercise.exercise.exerciseNameSnapshot)
                                put("variantKey", exercise.exercise.performedVariantKey); put("notes", exercise.exercise.notes)
                                put("sets", JSONArray().apply {
                                    exercise.sets.forEach { set -> put(JSONObject().apply {
                                        put("number", set.setNumber); put("weightKg", set.weightKg); put("reps", set.reps)
                                        put("completed", set.isCompleted); put("notes", set.notes)
                                    }) }
                                })
                            }) }
                        })
                    })
                }
            }
        }
        val payload = JSONObject().apply {
            put("format", "fittrackplus-export")
            put("version", 1)
            put("exportedAt", nowMillis)
            put("routines", routines)
            put("workoutSessions", sessions)
        }.toString(2)
        return UserDataExport("fittrackplus-backup-$nowMillis.json", payload)
    }
}
