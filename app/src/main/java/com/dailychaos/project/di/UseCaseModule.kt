// File: app/src/main/java/com/dailychaos/project/di/UseCaseModule.kt
package com.dailychaos.project.di

import com.dailychaos.project.domain.repository.AuthRepository
import com.dailychaos.project.domain.repository.ChaosRepository
import com.dailychaos.project.domain.usecase.chaos.* // Import semua use case chaos
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * UseCase Dependency Injection Module - Chaos Use Cases Only
 *
 * "UseCase provides untuk chaos entries - Auth use cases ada di AuthUseCaseModule"
 */


@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideCreateChaosEntryUseCase(
        chaosRepository: ChaosRepository,
        authRepository: AuthRepository
    ): CreateChaosEntryUseCase {
        return CreateChaosEntryUseCase(chaosRepository, authRepository)
    }

    @Provides
    @Singleton
    fun provideGetChaosEntryUseCase(
        chaosRepository: ChaosRepository,
        authRepository: AuthRepository
    ): GetChaosEntryUseCase {
        return GetChaosEntryUseCase(chaosRepository, authRepository)
    }

    @Provides
    @Singleton
    fun provideGetChaosEntriesUseCase(
        chaosRepository: ChaosRepository,
        authRepository: AuthRepository
    ): GetChaosEntriesUseCase {
        return GetChaosEntriesUseCase(chaosRepository, authRepository)
    }

    @Provides
    @Singleton
    fun provideUpdateChaosEntryUseCase(
        chaosRepository: ChaosRepository,
        authRepository: AuthRepository
    ): UpdateChaosEntryUseCase {
        return UpdateChaosEntryUseCase(chaosRepository, authRepository)
    }

    @Provides
    @Singleton
    fun provideDeleteChaosEntryUseCase(
        chaosRepository: ChaosRepository,
        authRepository: AuthRepository
    ): DeleteChaosEntryUseCase {
        return DeleteChaosEntryUseCase(chaosRepository, authRepository)
    }
}