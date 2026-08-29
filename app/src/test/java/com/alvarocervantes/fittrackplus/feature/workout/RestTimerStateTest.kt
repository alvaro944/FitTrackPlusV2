package com.alvarocervantes.fittrackplus.feature.workout

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RestTimerStateTest {

    @Test
    fun startRestTimerUsesRequestedDurationAndRuns() {
        val timer = RestTimerUiState().startRestTimer(seconds = 90, nowMillis = 1_000L)

        assertEquals(RestTimerStatus.Running, timer.status)
        assertEquals(90, timer.durationSeconds)
        assertEquals(90, timer.remainingSeconds)
        assertEquals(91_000L, timer.endsAtMillis)
    }

    @Test
    fun tickUsesWallClockTimeAndFinishesAfterTheEndTime() {
        val timer = RestTimerUiState().startRestTimer(seconds = 90, nowMillis = 1_000L)
            .tickRestTimer(nowMillis = 31_500L)

        assertEquals(60, timer.remainingSeconds)
        assertEquals(RestTimerStatus.Running, timer.status)

        val finished = timer.tickRestTimer(nowMillis = 91_000L)

        assertEquals(RestTimerStatus.Finished, finished.status)
        assertEquals(0, finished.remainingSeconds)
        assertEquals(null, finished.endsAtMillis)
    }

    @Test
    fun pausedTimerPreservesRemainingTimeAndResumeStartsANewWallClockDeadline() {
        val paused = RestTimerUiState().startRestTimer(seconds = 60, nowMillis = 1_000L)
            .pauseRestTimer(nowMillis = 21_000L)

        assertEquals(RestTimerStatus.Paused, paused.status)
        assertEquals(40, paused.remainingSeconds)
        assertEquals(null, paused.endsAtMillis)
        assertEquals(paused, paused.tickRestTimer(nowMillis = 80_000L))

        val resumed = paused.resumeRestTimer(nowMillis = 80_000L)

        assertEquals(RestTimerStatus.Running, resumed.status)
        assertEquals(40, resumed.remainingSeconds)
        assertEquals(120_000L, resumed.endsAtMillis)
    }

    @Test
    fun resetRestTimerReturnsToSelectedDuration() {
        val timer = RestTimerUiState().startRestTimer(seconds = 120, nowMillis = 1_000L)
            .tickRestTimer(nowMillis = 20_000L)
            .resetRestTimer()

        assertEquals(RestTimerStatus.Stopped, timer.status)
        assertEquals(120, timer.durationSeconds)
        assertEquals(120, timer.remainingSeconds)
        assertEquals(null, timer.endsAtMillis)
    }

    @Test
    fun cancelRestTimerClearsCountdownButKeepsAutoStartPreference() {
        val timer = RestTimerUiState(autoStartEnabled = true)
            .startRestTimer(seconds = 90)
            .cancelRestTimer()

        assertEquals(RestTimerStatus.Stopped, timer.status)
        assertEquals(0, timer.durationSeconds)
        assertEquals(0, timer.remainingSeconds)
        assertTrue(timer.autoStartEnabled)
    }

}
