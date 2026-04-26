package com.alvarocervantes.fittrackplus.feature.workout

const val DEFAULT_REST_TIMER_SECONDS: Int = 90

enum class RestTimerStatus {
    Stopped,
    Running,
    Paused,
    Finished
}

data class RestTimerUiState(
    val durationSeconds: Int = 0,
    val remainingSeconds: Int = 0,
    val status: RestTimerStatus = RestTimerStatus.Stopped,
    val autoStartEnabled: Boolean = false
) {
    val isActive: Boolean
        get() = status != RestTimerStatus.Stopped
    val progress: Float
        get() = if (durationSeconds <= 0) {
            0f
        } else {
            remainingSeconds.toFloat() / durationSeconds.toFloat()
        }
}

fun RestTimerUiState.startRestTimer(seconds: Int): RestTimerUiState {
    val normalizedSeconds = seconds.coerceAtLeast(1)
    return copy(
        durationSeconds = normalizedSeconds,
        remainingSeconds = normalizedSeconds,
        status = RestTimerStatus.Running
    )
}

fun RestTimerUiState.tickRestTimer(): RestTimerUiState {
    if (status != RestTimerStatus.Running) return this
    val nextRemaining = (remainingSeconds - 1).coerceAtLeast(0)
    return copy(
        remainingSeconds = nextRemaining,
        status = if (nextRemaining == 0) RestTimerStatus.Finished else RestTimerStatus.Running
    )
}

fun RestTimerUiState.pauseRestTimer(): RestTimerUiState {
    return if (status == RestTimerStatus.Running) copy(status = RestTimerStatus.Paused) else this
}

fun RestTimerUiState.resumeRestTimer(): RestTimerUiState {
    return if (status == RestTimerStatus.Paused && remainingSeconds > 0) {
        copy(status = RestTimerStatus.Running)
    } else {
        this
    }
}

fun RestTimerUiState.resetRestTimer(): RestTimerUiState {
    return if (durationSeconds > 0) {
        copy(remainingSeconds = durationSeconds, status = RestTimerStatus.Stopped)
    } else {
        this
    }
}

fun RestTimerUiState.cancelRestTimer(): RestTimerUiState {
    return copy(durationSeconds = 0, remainingSeconds = 0, status = RestTimerStatus.Stopped)
}

fun RestTimerUiState.withAutoStart(enabled: Boolean): RestTimerUiState {
    return copy(autoStartEnabled = enabled)
}

fun shouldAutoStartRestTimer(
    previousRepsText: String,
    nextRepsText: String,
    timer: RestTimerUiState
): Boolean {
    if (!timer.autoStartEnabled || timer.status == RestTimerStatus.Running || timer.status == RestTimerStatus.Paused) {
        return false
    }
    return !previousRepsText.isPositiveRepsText() && nextRepsText.isPositiveRepsText()
}

private fun String.isPositiveRepsText(): Boolean {
    return toIntOrNull()?.let { reps -> reps > 0 } == true
}
