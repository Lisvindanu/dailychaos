package com.dailychaos.project.di

import com.dailychaos.project.data.remote.firebase.FirebaseAuthService
import com.dailychaos.project.data.repository.AuthRepositoryImpl
import com.dailychaos.project.domain.repository.AuthRepository
import com.dailychaos.project.preferences.UserPreferences
import com.dailychaos.project.util.ValidationUtil
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Repository Dependency Injection Module
 * "Connecting the data layers to domain layer"
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
        return AuthRepositoryImpl(firebaseAuthService, validationUtil,userPreferences)
    }

    // TODO: Add other repositories as needed
    // @Provides
    // @Singleton
    // fun provideChaosRepository(...): ChaosRepository = ...

    // @Provides
    // @Singleton
    // fun provideCommunityRepository(...): CommunityRepository = ...
}