package com.dailychaos.project.domain.usecase.chaos

import com.dailychaos.project.domain.model.ChaosEntry
import com.dailychaos.project.domain.repository.AuthRepository
import com.dailychaos.project.domain.repository.ChaosRepository
import timber.log.Timber
import javax.inject.Inject

/**
 * CreateChaosEntryUseCase - Fixed untuk match interface ChaosRepository
 *
 * "Use case untuk membuat chaos entry baru dengan interface yang benar!"
 */
class CreateChaosEntryUseCase @Inject constructor(
    private val chaosRepository: ChaosRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(chaosEntry: ChaosEntry): Result<String> {
        return try {
            Timber.d("🚀 ==================== CREATE CHAOS ENTRY USE CASE STARTED ====================")
            Timber.d("🚀 Input ChaosEntry:")
            Timber.d("  - ID: '${chaosEntry.id}'")
            Timber.d("  - UserID: '${chaosEntry.userId}'")
            Timber.d("  - Title: '${chaosEntry.title}'")
            Timber.d("  - Description length: ${chaosEntry.description.length}")
            Timber.d("  - Chaos Level: ${chaosEntry.chaosLevel}")
            Timber.d("  - Mini Wins: ${chaosEntry.miniWins}")
            Timber.d("  - Tags: ${chaosEntry.tags}")
            Timber.d("  - Share to Community: ${chaosEntry.isSharedToCommunity}")

            // 1. Get current user from AuthRepository
            Timber.d("🔐 ==================== GETTING CURRENT USER ====================")
            val currentUser = authRepository.getCurrentUser()

            if (currentUser == null) {
                val error = "User not authenticated - cannot create chaos entry"
                Timber.e("❌ $error")
                return Result.failure(Exception(error))
            }

            Timber.d("✅ Current user found:")
            Timber.d("  - User ID: ${currentUser.id}")
            Timber.d("  - Display Name: ${currentUser.displayName}")
            Timber.d("  - Email: ${currentUser.email}")
            Timber.d("  - Is Anonymous: ${currentUser.isAnonymous}")

            // 2. Create chaos entry with proper user ID
            val chaosEntryWithUserId = chaosEntry.copy(userId = currentUser.id)

            Timber.d("📝 ==================== CHAOS ENTRY WITH USER ID ====================")
            Timber.d("📝 Updated ChaosEntry:")
            Timber.d("  - ID: '${chaosEntryWithUserId.id}'")
            Timber.d("  - UserID: '${chaosEntryWithUserId.userId}'")
            Timber.d("  - Title: '${chaosEntryWithUserId.title}'")
            Timber.d("  - Description: '${chaosEntryWithUserId.description.take(50)}...'")

            // 3. Validate chaos entry
            Timber.d("✅ ==================== VALIDATING CHAOS ENTRY ====================")
            val validationError = validateChaosEntry(chaosEntryWithUserId)
            if (validationError != null) {
                Timber.e("❌ Validation failed: $validationError")
                return Result.failure(Exception("Validation failed: $validationError"))
            }
            Timber.d("✅ Chaos entry validation passed")

            // 4. Call repository to create chaos entry
            // FIX: Match the interface signature - createChaosEntry(userId: String, entry: ChaosEntry)
            Timber.d("💾 ==================== CALLING REPOSITORY ====================")
            Timber.d("💾 Calling chaosRepository.createChaosEntry(userId='${currentUser.id}', entry=chaosEntry)")

            val result = chaosRepository.createChaosEntry(currentUser.id, chaosEntryWithUserId)

            Timber.d("📤 Repository call completed, processing result...")

            result.fold(
                onSuccess = { entryId ->
                    Timber.d("✅ ==================== REPOSITORY SUCCESS ====================")
                    Timber.d("✅ Repository returned success with entry ID: $entryId")

                    if (entryId.isBlank()) {
                        val error = "Repository returned empty entry ID"
                        Timber.e("❌ $error")
                        return Result.failure(Exception(error))
                    }

                    Timber.d("🎉 USE CASE COMPLETED SUCCESSFULLY!")
                    Timber.d("🎉 Final entry ID: $entryId")
                    return Result.success(entryId)
                },
                onFailure = { exception ->
                    Timber.e("❌ ==================== REPOSITORY FAILURE ====================")
                    Timber.e(exception, "❌ Repository failed to create chaos entry")
                    Timber.e("❌ Exception type: ${exception::class.simpleName}")
                    Timber.e("❌ Exception message: ${exception.message}")
                    Timber.e("❌ Exception cause: ${exception.cause}")

                    return Result.failure(exception)
                }
            )

        } catch (e: Exception) {
            Timber.e("💥 ==================== USE CASE EXCEPTION ====================")
            Timber.e(e, "💥 Unexpected error in CreateChaosEntryUseCase")
            Timber.e("💥 Exception type: ${e::class.simpleName}")
            Timber.e("💥 Exception message: ${e.message}")
            Timber.e("💥 Exception cause: ${e.cause}")
            e.printStackTrace()

            Result.failure(e)
        }
    }

    private fun validateChaosEntry(chaosEntry: ChaosEntry): String? {
        Timber.d("🔍 Validating chaos entry...")

        return when {
            chaosEntry.userId.isBlank() -> {
                Timber.w("❌ Validation: User ID is blank")
                "User ID cannot be empty"
            }
            chaosEntry.title.isBlank() -> {
                Timber.w("❌ Validation: Title is blank")
                "Title cannot be empty"
            }
            chaosEntry.title.length < 3 -> {
                Timber.w("❌ Validation: Title too short (${chaosEntry.title.length})")
                "Title must be at least 3 characters"
            }
            chaosEntry.title.length > 100 -> {
                Timber.w("❌ Validation: Title too long (${chaosEntry.title.length})")
                "Title must be less than 100 characters"
            }
            chaosEntry.description.isBlank() -> {
                Timber.w("❌ Validation: Description is blank")
                "Description cannot be empty"
            }
            chaosEntry.description.length < 10 -> {
                Timber.w("❌ Validation: Description too short (${chaosEntry.description.length})")
                "Description must be at least 10 characters"
            }
            chaosEntry.description.length > 1000 -> {
                Timber.w("❌ Validation: Description too long (${chaosEntry.description.length})")
                "Description must be less than 1000 characters"
            }
            chaosEntry.chaosLevel < 1 || chaosEntry.chaosLevel > 10 -> {
                Timber.w("❌ Validation: Invalid chaos level (${chaosEntry.chaosLevel})")
                "Chaos level must be between 1 and 10"
            }
            else -> {
                Timber.d("✅ Validation: All checks passed")
                null
            }
        }
    }
}