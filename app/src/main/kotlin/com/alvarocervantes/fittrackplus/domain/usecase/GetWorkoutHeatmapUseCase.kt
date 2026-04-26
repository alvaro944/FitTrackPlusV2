package com.alvarocervantes.fittrackplus.domain.usecase

import com.alvarocervantes.fittrackplus.data.local.entity.WorkoutSetEntity
import com.alvarocervantes.fittrackplus.data.repository.WorkoutRepository
import com.alvarocervantes.fittrackplus.domain.model.HeatmapDay
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DAY_MS: Long = 86_400_000L
private const val YEAR_MS: Long = 365 * DAY_MS

class GetWorkoutHeatmapUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    operator fun invoke(
        nowMillis: Long = System.currentTimeMillis()
    ): Flow<List<HeatmapDay>> {
        val sinceMs = nowMillis - YEAR_MS
        val todayEpochDay = nowMillis / DAY_MS

        return workoutRepository.observeFinishedSessionsWithExercises().map { sessions ->
            // Sumar volumen por dia a partir de sesiones finalizadas en el ultimo año
            val volumeByDay = mutableMapOf<Long, Double>()
            sessions.forEach { session ->
                val finishedAt = session.session.finishedAt ?: return@forEach
                if (finishedAt < sinceMs) return@forEach
                val epochDay = finishedAt / DAY_MS
                val sessionVolume = session.exercises.sumOf { exercise ->
                    exercise.sets.sumOf { set -> set.volumeKg() }
                }
                volumeByDay[epochDay] = (volumeByDay[epochDay] ?: 0.0) + sessionVolume
            }

            // Calcular niveles de intensidad por percentil
            val activeVolumes = volumeByDay.values.filter { it > 0.0 }.sorted()
            val thresholds = computeQuartileThresholds(activeVolumes)

            // Generar lista para los 365 dias (del mas antiguo al mas reciente)
            val startDay = todayEpochDay - 364
            (0..364).map { offset ->
                val day = startDay + offset
                val volume = volumeByDay[day] ?: 0.0
                HeatmapDay(
                    epochDay = day,
                    totalVolumeKg = volume,
                    intensityLevel = when {
                        volume <= 0.0 -> 0
                        thresholds.isEmpty() -> 1
                        volume <= thresholds[0] -> 1
                        thresholds.size < 2 || volume <= thresholds[1] -> 2
                        thresholds.size < 3 || volume <= thresholds[2] -> 3
                        else -> 4
                    }
                )
            }
        }
    }

    /** Devuelve hasta 3 umbrales (P25, P50, P75) de la lista de volumenes activos. */
    private fun computeQuartileThresholds(sorted: List<Double>): List<Double> {
        if (sorted.isEmpty()) return emptyList()
        return listOf(25, 50, 75).mapNotNull { pct ->
            val idx = (sorted.size * pct / 100.0).toInt().coerceIn(0, sorted.size - 1)
            sorted[idx]
        }.distinct()
    }
}

private fun WorkoutSetEntity.volumeKg(): Double = weightKg * reps
