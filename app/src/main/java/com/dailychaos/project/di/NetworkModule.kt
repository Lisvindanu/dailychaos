package com.dailychaos.project.di

import com.dailychaos.project.data.remote.api.KonoSubaApiImpl
import com.dailychaos.project.data.remote.api.KonoSubaApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideKonoSubaApiService(): KonoSubaApiService {
        return KonoSubaApiImpl()
    }
}