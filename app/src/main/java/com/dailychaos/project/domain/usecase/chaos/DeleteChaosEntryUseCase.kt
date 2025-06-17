// File: app/src/main/java/com/dailychaos/project/domain/usecase/chaos/DeleteChaosEntryUseCase.kt
package com.dailychaos.project.domain.usecase.chaos

import com.dailychaos.project.domain.repository.AuthRepository
import com.dailychaos.project.domain.repository.ChaosRepository
import javax.inject.Inject
import timber.log.Timber

/**
 * Delete Chaos Entry Use Case
 * "Menghapus jejak petualangan chaos yang tak ingin diingat!"
 */
class DeleteChaosEntryUseCase @Inject constructor(
    private val chaosRepository: ChaosRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(entryId: String): Result<Unit> {
        return try {
            // FIX: Gunakan authRepository.getCurrentUser() yang merupakan suspend function
            val userId = authRepository.getCurrentUser()?.id
            if (userId.isNullOrBlank()) {
                Timber.e("DeleteChaosEntryUseCase: User not authenticated.")
                return Result.failure(IllegalStateException("User not authenticated."))
            }

            if (entryId.isBlank()) {
                return Result.failure(IllegalArgumentException("ID entry chaos tidak boleh kosong untuk penghapusan!"))
            }

            chaosRepository.deleteChaosEntry(userId, entryId)
        } catch (e: Exception) {
            Timber.e(e, "Error deleting chaos entry in use case")
            Result.failure(e)
        }
    }
}