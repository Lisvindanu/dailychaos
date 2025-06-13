package com.dailychaos.project.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Preferences DataStore Dependency Injection Module
 *
 * "Setup preferences storage - tempat menyimpan settings user"
 */
@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule {

    // DataStore extension
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = "daily_chaos_preferences"
    )

    /**
     * Provide DataStore Preferences
     */
    @Provides
    @Singleton
    fun provideDataStorePreferences(
        @ApplicationContext context: Context
    ): DataStore<Preferences> {
        return context.dataStore
    }
}