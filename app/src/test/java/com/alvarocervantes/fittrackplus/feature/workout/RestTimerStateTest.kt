package com.alvarocervantes.fittrackplus.feature.workout

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RestTimerStateTest {

    @Test
    fun startRestTimerUsesRequestedDurationAndRuns() {
        val timer = RestTimerUiState().startRestTimer(seconds = 90)

        assertEquals(RestTimerStatus.Running, timer.status)
        assertEquals(90, timer.durationSeconds)
        assertEquals(90, timer.remainingSeconds)
    }

    @Test
    fun tickCountsDownUntilFinished() {
        val timer = RestTimerUiState().startRestTimer(seconds = 2)
            .tickRestTimer()
            .tickRestTimer()

        assertEquals(RestTimerStatus.Finished, timer.status)
        assertEquals(0, timer.remainingSeconds)
    }

    @Test
    fun pausedTimerDoesNotTickAndCanResume() {
        val paused = RestTimerUiState().startRestTimer(seconds = 60)
            .tickRestTimer()
            .pauseRestTimer()

        assertEquals(paused, paused.tickRestTimer())

        val resumed = paused.resumeRestTimer()

        assertEquals(RestTimerStatus.Running, resumed.status)
        assertEquals(59, resumed.remainingSeconds)
    }

    @Test
    fun resetRestTimerReturnsToSelectedDuration() {
        val timer = RestTimerUiState().startRestTimer(seconds = 120)
            .tickRestTimer()
            .resetRestTimer()

        assertEquals(RestTimerStatus.Stopped, timer.status)
        assertEquals(120, timer.durationSeconds)
        assertEquals(120, timer.remainingSeconds)
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

    @Test
    fun autoStartOnlyWhenRepsBecomePositiveAndTimerIsIdle() {
        assertTrue(
            shouldAutoStartRestTimer(
                previousRepsText = "0",
                nextRepsText = "8",
                timer = RestTimerUiState(autoStartEnabled = true)
            )
        )
        assertFalse(
            shouldAutoStartRestTimer(
                previousRepsText = "8",
                nextRepsText = "10",
                timer = RestTimerUiState(autoStartEnabled = true)
            )
        )
        assertFalse(
            shouldAutoStartRestTimer(
                previousRepsText = "",
                nextRepsText = "8",
                timer = RestTimerUiState(autoStartEnabled = false)
            )
        )
        assertFalse(
            shouldAutoStartRestTimer(
                previousRepsText = "",
                nextRepsText = "8",
                timer = RestTimerUiState(autoStartEnabled = true).startRestTimer(seconds = 60)
            )
        )
    }
}
