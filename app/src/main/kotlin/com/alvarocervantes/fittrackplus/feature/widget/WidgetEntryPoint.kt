package com.alvarocervantes.fittrackplus.feature.widget

import com.alvarocervantes.fittrackplus.data.repository.WorkoutRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun workoutRepository(): WorkoutRepository
}
