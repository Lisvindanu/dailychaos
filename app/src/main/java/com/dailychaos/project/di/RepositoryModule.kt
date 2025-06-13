package com.dailychaos.project.di

import com.dailychaos.project.data.repository.*
import com.dailychaos.project.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Repository Dependency Injection Module
 *
 * "Binding abstract repositories ke implementation konkret -
 * seperti binding skill Kazuma ke implementation yang actual"
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Bind Auth Repository
     */
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    /**
     * Bind Chaos Repository
     */
    @Binds
    @Singleton
    abstract fun bindChaosRepository(
        chaosRepositoryImpl: ChaosRepositoryImpl
    ): ChaosRepository

    /**
     * Bind Community Repository
     */
    @Binds
    @Singleton
    abstract fun bindCommunityRepository(
        communityRepositoryImpl: CommunityRepositoryImpl
    ): CommunityRepository

    /**
     * Bind User Repository
     */
    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository
}