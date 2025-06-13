package com.dailychaos.project.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * UseCase Dependency Injection Module
 *
 * "UseCase provides - temporarily empty untuk local development"
 */
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    // TEMPORARY: Empty module untuk local development
    // Nanti di-enable ketika implement use cases

    /*
    @Provides
    @Singleton
    fun provideCreateChaosEntryUseCase(
        chaosRepository: ChaosRepository
    ): CreateChaosEntryUseCase {
        return CreateChaosEntryUseCase(chaosRepository)
    }
    */
}