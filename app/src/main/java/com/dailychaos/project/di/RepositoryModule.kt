// File: app/src/main/java/com/dailychaos/project/di/RepositoryModule.kt
package com.dailychaos.project.di

import android.os.Build
import androidx.annotation.RequiresApi
import com.dailychaos.project.data.remote.firebase.FirebaseAuthService
import com.dailychaos.project.data.remote.firebase.FirebaseFirestoreService
import com.dailychaos.project.data.repository.AuthRepositoryImpl
import com.dailychaos.project.data.repository.ChaosRepositoryImpl
import com.dailychaos.project.data.repository.CommunityRepositoryImpl
import com.dailychaos.project.domain.repository.AuthRepository
import com.dailychaos.project.domain.repository.ChaosRepository
import com.dailychaos.project.domain.repository.CommunityRepository
import com.dailychaos.project.domain.repository.CommunityRepositoryExtended
import com.dailychaos.project.preferences.UserPreferences
import com.dailychaos.project.util.ValidationUtil
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Repository Dependency Injection Module
 * "Connecting the data layers to domain layer - Enhanced dengan support system"
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuthService: FirebaseAuthService,
        validationUtil: ValidationUtil,
        userPreferences: UserPreferences
    ): AuthRepository {
        return AuthRepositoryImpl(firebaseAuthService, validationUtil, userPreferences)
    }

    @Provides
    @Singleton
    fun provideChaosRepository(
        firestoreService: FirebaseFirestoreService,
        authService: FirebaseAuthService
    ): ChaosRepository {
        return ChaosRepositoryImpl(firestoreService, authService)
    }

    /**
     * ✅ FIXED: Provide CommunityRepositoryExtended with proper AuthRepository dependency
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @Provides
    @Singleton
    fun provideCommunityRepositoryExtended(
        firestore: FirebaseFirestore,
        authRepository: AuthRepository // ✅ FIXED: Add missing AuthRepository parameter
    ): CommunityRepositoryExtended {
        return CommunityRepositoryImpl(firestore, authRepository)
    }

    /**
     * ✅ FIXED: Provide base CommunityRepository that delegates to Extended implementation
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @Provides
    @Singleton
    fun provideCommunityRepository(
        communityRepositoryExtended: CommunityRepositoryExtended
    ): CommunityRepository {
        return communityRepositoryExtended
    }

}