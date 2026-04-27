package com.alvarocervantes.fittrackplus.domain.usecase

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import com.alvarocervantes.fittrackplus.data.repository.WorkoutRepository

class GetWorkoutStreakUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    suspend operator fun invoke(): Int {
        val sessions = workoutRepository.observeFinishedSessions().first()
        if (sessions.isEmpty()) return 0

        val today = LocalDate.now()
        val uniqueDays = sessions
            .mapNotNull { it.finishedAt }
            .map { millis ->
                Instant.ofEpochMilli(millis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }
            .toSortedSet(reverseOrder())

        if (uniqueDays.isEmpty()) return 0

        val mostRecent = uniqueDays.first()
        // Racha rota si la sesion mas reciente es anterior a ayer
        if (mostRecent.isBefore(today.minusDays(1))) return 0

        var streak = 0
        var expected = mostRecent

        for (day in uniqueDays) {
            when {
                day == expected -> {
                    streak++
                    expected = expected.minusDays(1)
                }
                day.isBefore(expected) -> break
                // day > expected: ignorar (no deberia ocurrir con set desc)
            }
        }
        return streak
    }
}
