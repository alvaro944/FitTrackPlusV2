package com.alvarocervantes.fittrackplus.domain.usecase

import com.alvarocervantes.fittrackplus.data.health.HealthConnectRepository
import com.alvarocervantes.fittrackplus.domain.model.StepsData
import java.time.LocalDate
import javax.inject.Inject

class ReadDailyStepsUseCase @Inject constructor(
    private val healthConnectRepository: HealthConnectRepository
) {
    suspend operator fun invoke(): StepsData? {
        if (!healthConnectRepository.hasPermission()) return null
        val todaySteps = healthConnectRepository.readTodaySteps() ?: return null
        val weekSteps = healthConnectRepository.readWeekSteps() ?: emptyMap()
        return StepsData(todaySteps = todaySteps, weekDaySteps = weekSteps)
    }

    suspend fun readForWeekStart(weekStart: LocalDate): Map<Int, Long>? =
        healthConnectRepository.readWeekStepsForWeekStart(weekStart)
}
