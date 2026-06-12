package com.alvarocervantes.fittrackplus.feature.home

import com.alvarocervantes.fittrackplus.data.local.entity.WorkoutSessionEntity
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeWeekActivityTest {

    @Test
    fun trainedDaysThisWeek_mapsWednesdayToIndexTwo() {
        val now = utcMillis(year = 2026, month = Calendar.JUNE, day = 11)
        val sessions = listOf(
            session(startedAt = utcMillis(year = 2026, month = Calendar.JUNE, day = 10))
        )

        val result = trainedDaysThisWeek(
            sessions = sessions,
            nowMillis = now,
            calendarFactory = ::utcCalendar
        )

        assertEquals(setOf(2), result)
    }

    @Test
    fun trainedDaysThisWeek_ignoresSessionsOutsideCurrentWeekAndDeduplicatesDays() {
        val now = utcMillis(year = 2026, month = Calendar.JUNE, day = 11)
        val sessions = listOf(
            session(startedAt = utcMillis(year = 2026, month = Calendar.JUNE, day = 9)),
            session(startedAt = utcMillis(year = 2026, month = Calendar.JUNE, day = 9, hour = 18)),
            session(startedAt = utcMillis(year = 2026, month = Calendar.JUNE, day = 8)),
            session(startedAt = utcMillis(year = 2026, month = Calendar.JUNE, day = 7))
        )

        val result = trainedDaysThisWeek(
            sessions = sessions,
            nowMillis = now,
            calendarFactory = ::utcCalendar
        )

        assertEquals(setOf(0, 1), result)
    }

    private fun session(startedAt: Long) = WorkoutSessionEntity(
        id = startedAt,
        routineId = 1,
        routineNameSnapshot = "Push Pull Legs",
        routineDayId = 10,
        dayNameSnapshot = "Push",
        startedAt = startedAt,
        finishedAt = startedAt + 3_600_000,
        weekNumber = 24
    )

    private fun utcMillis(
        year: Int,
        month: Int,
        day: Int,
        hour: Int = 12
    ): Long {
        return utcCalendar().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun utcCalendar(): Calendar {
        return Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US)
    }
}
