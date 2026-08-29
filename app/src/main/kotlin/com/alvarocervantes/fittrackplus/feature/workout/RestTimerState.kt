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
    val endsAtMillis: Long? = null,
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

fun RestTimerUiState.startRestTimer(
    seconds: Int,
    nowMillis: Long = System.currentTimeMillis()
): RestTimerUiState {
    val normalizedSeconds = seconds.coerceAtLeast(1)
    return copy(
        durationSeconds = normalizedSeconds,
        remainingSeconds = normalizedSeconds,
        status = RestTimerStatus.Running,
        endsAtMillis = nowMillis + normalizedSeconds * 1_000L
    )
}

fun RestTimerUiState.tickRestTimer(nowMillis: Long = System.currentTimeMillis()): RestTimerUiState {
    if (status != RestTimerStatus.Running) return this
    val endTime = endsAtMillis ?: return copy(status = RestTimerStatus.Finished, remainingSeconds = 0)
    val remainingMillis = endTime - nowMillis
    val nextRemaining = ((remainingMillis.coerceAtLeast(0) + 999L) / 1_000L).toInt()
    return copy(
        remainingSeconds = nextRemaining,
        status = if (nextRemaining == 0) RestTimerStatus.Finished else RestTimerStatus.Running,
        endsAtMillis = if (nextRemaining == 0) null else endTime
    )
}

fun RestTimerUiState.pauseRestTimer(nowMillis: Long = System.currentTimeMillis()): RestTimerUiState {
    val refreshed = tickRestTimer(nowMillis)
    return if (refreshed.status == RestTimerStatus.Running) {
        refreshed.copy(status = RestTimerStatus.Paused, endsAtMillis = null)
    } else {
        refreshed
    }
}

fun RestTimerUiState.resumeRestTimer(nowMillis: Long = System.currentTimeMillis()): RestTimerUiState {
    return if (status == RestTimerStatus.Paused && remainingSeconds > 0) {
        copy(status = RestTimerStatus.Running, endsAtMillis = nowMillis + remainingSeconds * 1_000L)
    } else {
        this
    }
}

fun RestTimerUiState.resetRestTimer(): RestTimerUiState {
    return if (durationSeconds > 0) {
        copy(remainingSeconds = durationSeconds, status = RestTimerStatus.Stopped, endsAtMillis = null)
    } else {
        this
    }
}

fun RestTimerUiState.cancelRestTimer(): RestTimerUiState {
    return copy(durationSeconds = 0, remainingSeconds = 0, status = RestTimerStatus.Stopped, endsAtMillis = null)
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
