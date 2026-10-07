package com.alvarocervantes.fittrackplus.di

import com.alvarocervantes.fittrackplus.data.repository.WorkoutRepository
import com.alvarocervantes.fittrackplus.domain.repository.ExerciseLoadHistory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProgressionModule {

    /** Narrow view over the workout history, so the engine does not depend on the whole repository. */
    @Provides
    @Singleton
    fun provideExerciseLoadHistory(
        workoutRepository: WorkoutRepository
    ): ExerciseLoadHistory = ExerciseLoadHistory { variantKey ->
        workoutRepository.getMaxWeightForExercise(variantKey)
    }
}
