package com.alvarocervantes.fittrackplus.feature.history

import org.junit.Assert.assertEquals
import org.junit.Test

class HistoryFiltersTest {

    @Test
    fun allPeriodDoesNotFilterSessions() {
        val sessions = sampleSessions()

        val result = sessions.applyHistoryFilters(
            period = HistoryPeriodFilter.All,
            sort = HistorySortOrder.Recent,
            nowMillis = NOW
        )

        assertEquals(listOf(1L, 2L, 3L), result.map { it.sessionId })
    }

    @Test
    fun fourWeeksPeriodExcludesOlderSessions() {
        val sessions = sampleSessions()

        val result = sessions.applyHistoryFilters(
            period = HistoryPeriodFilter.LastFourWeeks,
            sort = HistorySortOrder.Recent,
            nowMillis = NOW
        )

        assertEquals(listOf(1L, 2L), result.map { it.sessionId })
    }

    @Test
    fun twelveWeeksPeriodIncludesSessionsInsideCutoff() {
        val sessions = sampleSessions()

        val result = sessions.applyHistoryFilters(
            period = HistoryPeriodFilter.LastTwelveWeeks,
            sort = HistorySortOrder.Recent,
            nowMillis = NOW
        )

        assertEquals(listOf(1L, 2L, 3L), result.map { it.sessionId })
    }

    @Test
    fun sortOrdersUseExpectedFields() {
        val sessions = sampleSessions()

        assertEquals(
            listOf(1L, 2L, 3L),
            sessions.applyHistoryFilters(HistoryPeriodFilter.All, HistorySortOrder.Recent, NOW)
                .map { it.sessionId }
        )
        assertEquals(
            listOf(3L, 2L, 1L),
            sessions.applyHistoryFilters(HistoryPeriodFilter.All, HistorySortOrder.Oldest, NOW)
                .map { it.sessionId }
        )
        assertEquals(
            listOf(2L, 1L, 3L),
            sessions.applyHistoryFilters(HistoryPeriodFilter.All, HistorySortOrder.HighestVolume, NOW)
                .map { it.sessionId }
        )
    }

    private fun sampleSessions(): List<HistorySessionUiState> {
        return listOf(
            session(id = 1, finishedAt = NOW, volume = 1_000.0),
            session(id = 2, finishedAt = NOW - TWO_WEEKS, volume = 1_500.0),
            session(id = 3, finishedAt = NOW - EIGHT_WEEKS, volume = 500.0)
        )
    }

    private fun session(id: Long, finishedAt: Long, volume: Double): HistorySessionUiState {
        return HistorySessionUiState(
            sessionId = id,
            routineName = "Routine $id",
            dayName = "Day $id",
            startedAt = finishedAt - 60_000,
            finishedAt = finishedAt,
            weekNumber = id.toInt(),
            totalVolumeKg = volume,
            durationMillis = 60_000,
            setCount = id.toInt()
        )
    }

    private companion object {
        const val DAY: Long = 86_400_000
        const val TWO_WEEKS: Long = 14 * DAY
        const val EIGHT_WEEKS: Long = 56 * DAY
        const val NOW: Long = 1_700_000_000_000
    }
}
