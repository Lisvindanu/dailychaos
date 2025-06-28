// File: app/src/main/java/com/dailychaos/project/di/CommunityPaginationModule.kt
package com.dailychaos.project.di

import com.dailychaos.project.data.repository.CommunityRepositoryPaginationImpl
import com.dailychaos.project.domain.repository.CommunityRepositoryExtended
import com.dailychaos.project.domain.repository.CommunityRepositoryPagination
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dependency Injection Module untuk Pagination Extension
 * Tidak mengubah existing DI modules
 */
@Module
@InstallIn(SingletonComponent::class)
object CommunityPaginationModule {

    @Provides
    @Singleton
    fun provideCommunityRepositoryPagination(
        firestore: FirebaseFirestore,
        baseRepository: CommunityRepositoryExtended
    ): CommunityRepositoryPagination {
        return CommunityRepositoryPaginationImpl(firestore, baseRepository)
    }
}

// Optional: Factory untuk ViewModel extension jika diperlukan
// Bisa digunakan jika ingin inject extension secara terpisah