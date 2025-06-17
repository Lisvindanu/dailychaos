// File: app/src/main/java/com/dailychaos/project/di/FirebaseModule.kt
package com.dailychaos.project.di

import android.content.Context
import com.dailychaos.project.data.remote.firebase.FirebaseAuthService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions // <-- Pastikan ini diimport
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Firebase Services Dependency Injection Module
 *
 * "Simplified Firebase setup untuk local development"
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
            // Enable offline persistence - simplified untuk local dev
            try {
                val settings = com.google.firebase.firestore.FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .build()
                firestoreSettings = settings
            } catch (e: Exception) {
                // Ignore settings error untuk local development
                android.util.Log.w("FirebaseModule", "Could not set Firestore settings: ${e.message}")
            }
        }
    }

    // <-- Pastikan bagian ini TIDAK dikomentari dan ditambahkan
    @Provides
    @Singleton
    fun provideFirebaseFunctions(): FirebaseFunctions {
        // PENTING: Gunakan lokasi di mana Anda me-deploy Cloud Function.
        // Berdasarkan output deploy Anda, lokasinya adalah "us-central1".
        return FirebaseFunctions.getInstance("us-central1")
    }

    /**
     * Provide FirebaseAuthService
     */
    @Provides
    @Singleton
    fun provideFirebaseAuthService(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore,
        userPreferences: com.dailychaos.project.preferences.UserPreferences,
        functions: FirebaseFunctions // <-- Tambahkan parameter ini
    ): FirebaseAuthService {
        return FirebaseAuthService(firebaseAuth, firestore, userPreferences, functions) // <-- Lewatkan 'functions' di sini
    }

    // Hapus bagian TEMPORARY yang dikomentari di bawah ini jika sudah tidak diperlukan
    /*
    @Provides
    @Singleton
    fun provideFirebaseFunctions(): FirebaseFunctions {
        return FirebaseFunctions.getInstance()
    }
    // ... dan sisanya
    */
}