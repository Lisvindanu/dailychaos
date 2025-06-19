// File: app/src/main/java/com/dailychaos/project/di/CommunityUseCaseModule.kt
package com.dailychaos.project.di

import com.dailychaos.project.domain.repository.AuthRepository
import com.dailychaos.project.domain.repository.ChaosRepository
import com.dailychaos.project.domain.repository.CommunityRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Community Use Case Dependency Injection Module
 * "Setting up community operations use cases"
 *
 * NOTE: Since we're using repository directly in ViewModels for now,
 * these use cases are optional and can be implemented later if needed.
 */
@Module
@InstallIn(SingletonComponent::class)
object CommunityUseCaseModule {

    // Currently using repository directly in ViewModels
    // Use cases can be implemented later for more complex business logic

    /*
    @Provides
    @Singleton
    fun provideGetCommunityPostsUseCase(
        communityRepository: CommunityRepository
    ): GetCommunityPostsUseCase {
        return GetCommunityPostsUseCase(communityRepository)
    }

    @Provides
    @Singleton
    fun provideShareChaosEntryUseCase(
        chaosRepository: ChaosRepository,
        communityRepository: CommunityRepository,
        authRepository: AuthRepository
    ): ShareChaosEntryUseCase {
        return ShareChaosEntryUseCase(chaosRepository, communityRepository, authRepository)
    }

    @Provides
    @Singleton
    fun provideGiveSupportUseCase(
        communityRepository: CommunityRepository,
        authRepository: AuthRepository
    ): GiveSupportUseCase {
        return GiveSupportUseCase(communityRepository, authRepository)
    }

    @Provides
    @Singleton
    fun provideFindChaosTwinsUseCase(
        communityRepository: CommunityRepository,
        authRepository: AuthRepository
    ): FindChaosTwinsUseCase {
        return FindChaosTwinsUseCase(communityRepository, authRepository)
    }
    */
}