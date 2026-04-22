package com.alvarocervantes.fittrackplus.di

import com.alvarocervantes.fittrackplus.data.repository.DefaultRoutineRepository
import com.alvarocervantes.fittrackplus.data.repository.DefaultWorkoutRepository
import com.alvarocervantes.fittrackplus.data.repository.RoutineRepository
import com.alvarocervantes.fittrackplus.data.repository.WorkoutRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindRoutineRepository(
        repository: DefaultRoutineRepository
    ): RoutineRepository

    @Binds
    @Singleton
    abstract fun bindWorkoutRepository(
        repository: DefaultWorkoutRepository
    ): WorkoutRepository
}
