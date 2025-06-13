package com.dailychaos.project.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Database Dependency Injection Module
 *
 * "Database setup - temporarily disabled untuk local development"
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // TEMPORARY: Comment out database provides untuk local development
    // Nanti di-enable ketika implement Room database

    /*
    @Provides
    @Singleton
    fun provideChaosDatabase(
        @ApplicationContext context: Context
    ): ChaosDatabase {
        return Room.databaseBuilder(
            context,
            ChaosDatabase::class.java,
            Constants.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideChaosEntryDao(database: ChaosDatabase): ChaosEntryDao {
        return database.chaosEntryDao()
    }
    */
}