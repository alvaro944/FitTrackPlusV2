package com.alvarocervantes.fittrackplus.di

import android.content.Context
import androidx.room.Room
import com.alvarocervantes.fittrackplus.core.database.FitTrackPlusDatabase
import com.alvarocervantes.fittrackplus.core.database.MIGRATION_1_2
import com.alvarocervantes.fittrackplus.data.local.dao.RoutineDao
import com.alvarocervantes.fittrackplus.data.local.dao.WorkoutDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): FitTrackPlusDatabase {
        return Room.databaseBuilder(
            context,
            FitTrackPlusDatabase::class.java,
            "fittrackplus_v2.db"
        ).addMigrations(MIGRATION_1_2).build()
    }

    @Provides
    fun provideRoutineDao(database: FitTrackPlusDatabase): RoutineDao {
        return database.routineDao()
    }

    @Provides
    fun provideWorkoutDao(database: FitTrackPlusDatabase): WorkoutDao {
        return database.workoutDao()
    }
}
