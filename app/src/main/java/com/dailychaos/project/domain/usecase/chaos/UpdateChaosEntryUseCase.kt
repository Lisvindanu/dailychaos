// File: app/src/main/java/com/dailychaos/project/domain/usecase/chaos/UpdateChaosEntryUseCase.kt
package com.dailychaos.project.domain.usecase.chaos

import com.dailychaos.project.domain.model.ChaosEntry
import com.dailychaos.project.domain.repository.AuthRepository
import com.dailychaos.project.domain.repository.ChaosRepository
import javax.inject.Inject
import timber.log.Timber

/**
 * Update Chaos Entry Use Case
 * "Memperbarui catatan petualangan chaos yang sudah ada!"
 */
class UpdateChaosEntryUseCase @Inject constructor(
    private val chaosRepository: ChaosRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(entry: ChaosEntry): Result<Unit> {
        return try {
            // FIX: Gunakan authRepository.getCurrentUser() yang merupakan suspend function
            val userId = authRepository.getCurrentUser()?.id
            if (userId.isNullOrBlank()) {
                Timber.e("UpdateChaosEntryUseCase: User not authenticated.")
                return Result.failure(IllegalStateException("User not authenticated."))
            }

            // Pastikan ID entry tidak kosong
            if (entry.id.isBlank()) {
                return Result.failure(IllegalArgumentException("ID entry chaos tidak boleh kosong untuk pembaruan!"))
            }
            if (entry.title.isBlank()) {
                return Result.failure(IllegalArgumentException("Title chaos tidak boleh kosong!"))
            }
            if (entry.description.isBlank() || entry.description.length < 10) {
                return Result.failure(IllegalArgumentException("Deskripsi chaos minimal 10 karakter!"))
            }

            // Update timestamp
            val entryToUpdate = entry.copy(updatedAt = kotlinx.datetime.Clock.System.now())

            chaosRepository.updateChaosEntry(userId, entryToUpdate)
        } catch (e: Exception) {
            Timber.e(e, "Error updating chaos entry in use case")
            Result.failure(e)
        }
    }
}