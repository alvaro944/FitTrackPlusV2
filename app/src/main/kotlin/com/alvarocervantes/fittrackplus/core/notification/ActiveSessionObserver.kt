package com.alvarocervantes.fittrackplus.core.notification

import com.alvarocervantes.fittrackplus.data.repository.WorkoutRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Singleton
class ActiveSessionObserver @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val notificationManager: ActiveSessionNotificationManager
) {
    fun start(scope: CoroutineScope) {
        scope.launch {
            workoutRepository.observeActiveSession().collect { session ->
                if (session != null) {
                    notificationManager.show(
                        routineName = session.session.routineNameSnapshot,
                        dayName = session.session.dayNameSnapshot
                    )
                } else {
                    notificationManager.cancel()
                }
            }
        }
    }
}
