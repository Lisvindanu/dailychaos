package com.dailychaos.project.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Repository Dependency Injection Module
 *
 * "Binding repositories - temporarily disabled untuk local development"
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    // TEMPORARY: Comment out semua @Binds untuk local development
    // Nanti di-enable ketika implement repositories

    /*
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindChaosRepository(
        chaosRepositoryImpl: ChaosRepositoryImpl
    ): ChaosRepository

    @Binds
    @Singleton
    abstract fun bindCommunityRepository(
        communityRepositoryImpl: CommunityRepositoryImpl
    ): CommunityRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository
    */
}