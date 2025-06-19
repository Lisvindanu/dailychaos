// File: app/src/main/java/com/dailychaos/project/domain/usecase/chaos/GetChaosEntryUseCase.kt
package com.dailychaos.project.domain.usecase.chaos

import com.dailychaos.project.domain.model.ChaosEntry
import com.dailychaos.project.domain.repository.AuthRepository
import com.dailychaos.project.domain.repository.ChaosRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flow // Import flow builder
import javax.inject.Inject
import timber.log.Timber

/**
 * Get Chaos Entry Use Case
 * "Mengambil detail satu petualangan chaos berdasarkan ID!"
 */
class GetChaosEntryUseCase @Inject constructor(
    private val chaosRepository: ChaosRepository,
    private val authRepository: AuthRepository
) {
    // FIX: Menggunakan flow builder untuk menangani suspend function di dalam Flow
    operator fun invoke(entryId: String): Flow<ChaosEntry?> = flow {
        try {
            // FIX: Gunakan authRepository.getCurrentUser() yang merupakan suspend function
            val userId = authRepository.getCurrentUser()?.id
            if (userId.isNullOrBlank()) {
                Timber.e("GetChaosEntryUseCase: User not authenticated. Emitting null.")
                emit(null) // Mengirimkan null jika user tidak terautentikasi
                return@flow // Keluar dari flow builder
            }

            // Mengoleksi Flow dari repository dan memancarkan itemnya
            chaosRepository.getChaosEntry(userId, entryId).collect {
                emit(it)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting single chaos entry in use case")
            emit(null) // Mengirimkan null jika terjadi error
        }
    }
}