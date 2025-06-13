package com.dailychaos.project.di

import android.content.Context
import androidx.room.Room
import com.dailychaos.project.data.local.database.ChaosDatabase
import com.dailychaos.project.data.local.dao.*
import com.dailychaos.project.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Database Dependency Injection Module
 *
 * "Setup local database - rumah aman untuk semua chaos entries kita"
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Provide Room Database
     */
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
            .fallbackToDestructiveMigration() // For development, in production use proper migrations
            .build()
    }

    /**
     * Provide Chaos Entry DAO
     */
    @Provides
    fun provideChaosEntryDao(database: ChaosDatabase): ChaosEntryDao {
        return database.chaosEntryDao()
    }

    /**
     * Provide Community Post DAO
     */
    @Provides
    fun provideCommunityPostDao(database: ChaosDatabase): CommunityPostDao {
        return database.communityPostDao()
    }

    /**
     * Provide Support Reaction DAO
     */
    @Provides
    fun provideSupportReactionDao(database: ChaosDatabase): SupportReactionDao {
        return database.supportReactionDao()
    }

    /**
     * Provide User DAO
     */
    @Provides
    fun provideUserDao(database: ChaosDatabase): UserDao {
        return database.userDao()
    }

    /**
     * Provide Cache Metadata DAO
     */
    @Provides
    fun provideCacheMetadataDao(database: ChaosDatabase): CacheMetadataDao {
        return database.cacheMetadataDao()
    }
}