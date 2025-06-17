// File: app/src/main/java/com/dailychaos/project/domain/usecase/chaos/GetChaosEntriesUseCase.kt
package com.dailychaos.project.domain.usecase.chaos

import com.dailychaos.project.domain.model.ChaosEntry
import com.dailychaos.project.domain.repository.AuthRepository
import com.dailychaos.project.domain.repository.ChaosRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flow // Import flow builder
import javax.inject.Inject
import timber.log.Timber

/**
 * Get Chaos Entries Use Case
 * "Mengambil daftar petualangan chaos seorang Adventurer!"
 */
class GetChaosEntriesUseCase @Inject constructor(
    private val chaosRepository: ChaosRepository,
    private val authRepository: AuthRepository
) {
    // FIX: Menggunakan flow builder untuk menangani suspend function di dalam Flow
    operator fun invoke(userId: String? = null, limit: Int? = null): Flow<List<ChaosEntry>> = flow {
        try {
            // FIX: Gunakan authRepository.getCurrentUser() yang merupakan suspend function
            val currentUserId = userId ?: authRepository.getCurrentUser()?.id
            if (currentUserId.isNullOrBlank()) {
                Timber.e("GetChaosEntriesUseCase: User ID is null or blank. Emitting empty list.")
                emit(emptyList()) // Mengirimkan list kosong jika user tidak terautentikasi
                return@flow // Keluar dari flow builder
            }

            val entriesFlow = if (limit != null) {
                chaosRepository.getRecentChaosEntries(currentUserId, limit)
            } else {
                chaosRepository.getAllChaosEntries(currentUserId)
            }

            // Mengoleksi Flow dari repository dan memancarkan itemnya
            entriesFlow.collect {
                emit(it)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting chaos entries in use case")
            emit(emptyList()) // Mengirimkan list kosong jika terjadi error
        }
    }
}