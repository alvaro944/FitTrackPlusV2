package com.alvarocervantes.fittrackplus.feature.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.alvarocervantes.fittrackplus.data.repository.WorkoutRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Singleton
class WidgetUpdateObserver @Inject constructor(
    @ApplicationContext private val context: Context,
    private val workoutRepository: WorkoutRepository
) {
    fun start(scope: CoroutineScope) {
        workoutRepository.observeFinishedSessions()
            .onEach { FitTrackPlusWidget().updateAll(context) }
            .catch { }
            .launchIn(scope)
    }
}
