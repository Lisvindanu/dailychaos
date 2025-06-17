// File: app/src/main/java/com/dailychaos/project/domain/usecase/chaos/CreateChaosEntryUseCase.kt
package com.dailychaos.project.domain.usecase.chaos

import com.dailychaos.project.domain.model.ChaosEntry
import com.dailychaos.project.domain.repository.AuthRepository
import com.dailychaos.project.domain.repository.ChaosRepository
import javax.inject.Inject
import timber.log.Timber // Untuk logging

/**
 * Create Chaos Entry Use Case
 * "Mencatat petualangan chaos baru seorang Adventurer!"
 */
class CreateChaosEntryUseCase @Inject constructor(
    private val chaosRepository: ChaosRepository,
    private val authRepository: AuthRepository // Diperlukan untuk mendapatkan userId
) {
    suspend operator fun invoke(entry: ChaosEntry): Result<String> {
        return try {
            // FIX: Gunakan authRepository.getCurrentUser() yang merupakan suspend function
            val userId = authRepository.getCurrentUser()?.id
            if (userId.isNullOrBlank()) {
                Timber.e("CreateChaosEntryUseCase: User not authenticated.")
                return Result.failure(IllegalStateException("User not authenticated."))
            }

            // Validasi dasar di Use Case (opsional, bisa juga di ViewModel atau model itu sendiri)
            if (entry.title.isBlank()) {
                return Result.failure(IllegalArgumentException("Title chaos tidak boleh kosong!"))
            }
            if (entry.description.isBlank() || entry.description.length < 10) {
                return Result.failure(IllegalArgumentException("Deskripsi chaos minimal 10 karakter!"))
            }

            // Buat ChaosEntry dengan userId yang benar dan timestamp saat ini
            val entryToCreate = entry.copy(
                userId = userId,
                // ID akan di-generate oleh repository atau Firestore
                createdAt = kotlinx.datetime.Clock.System.now(),
                updatedAt = kotlinx.datetime.Clock.System.now()
            )

            chaosRepository.createChaosEntry(userId, entryToCreate)
        } catch (e: Exception) {
            Timber.e(e, "Error creating chaos entry in use case")
            Result.failure(e)
        }
    }
}