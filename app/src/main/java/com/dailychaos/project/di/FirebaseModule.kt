package com.dailychaos.project.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Firebase Services Dependency Injection Module
 *
 * "Setup semua Firebase services - backend infrastructure untuk Daily Chaos"
 */
@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    /**
     * Provide Firebase Auth
     */
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    /**
     * Provide Firebase Firestore
     */
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance().apply {
            // Enable offline persistence
            firestoreSettings = firestoreSettings.toBuilder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestore.CACHE_SIZE_UNLIMITED)
                .build()
        }
    }

    /**
     * Provide Firebase Functions
     */
    @Provides
    @Singleton
    fun provideFirebaseFunctions(): FirebaseFunctions {
        return FirebaseFunctions.getInstance().apply {
            // Set emulator untuk development jika diperlukan
            // useEmulator("10.0.2.2", 5001) // Uncomment for emulator
        }
    }

    /**
     * Provide Firebase Storage
     */
    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }

    /**
     * Provide Firebase Messaging
     */
    @Provides
    @Singleton
    fun provideFirebaseMessaging(): FirebaseMessaging {
        return FirebaseMessaging.getInstance()
    }

    /**
     * Provide Firebase Analytics
     */
    @Provides
    @Singleton
    fun provideFirebaseAnalytics(
        @ApplicationContext context: Context
    ): FirebaseAnalytics {
        return FirebaseAnalytics.getInstance(context)
    }

    /**
     * Provide Firebase Crashlytics
     */
    @Provides
    @Singleton
    fun provideFirebaseCrashlytics(): FirebaseCrashlytics {
        return FirebaseCrashlytics.getInstance()
    }

    /**
     * Provide Firebase Performance
     */
    @Provides
    @Singleton
    fun provideFirebasePerformance(): FirebasePerformance {
        return FirebasePerformance.getInstance()
    }

    /**
     * Provide Firebase Remote Config
     */
    @Provides
    @Singleton
    fun provideFirebaseRemoteConfig(): FirebaseRemoteConfig {
        return FirebaseRemoteConfig.getInstance().apply {
            // Set default config values
            setDefaultsAsync(mapOf(
                "enable_konosuba_quotes" to true,
                "max_chaos_entry_length" to 1000,
                "support_cooldown_seconds" to 1,
                "enable_chaos_twins" to true,
                "maintenance_mode" to false,
                "min_app_version" to 1
            ))

            // Set fetch interval (1 hour for production, 0 for debug)
            val fetchInterval = if (com.dailychaos.project.BuildConfig.DEBUG) 0L else 3600L
            fetch(fetchInterval)
        }
    }
}