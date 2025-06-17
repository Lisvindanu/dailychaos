// File: app/src/main/java/com/dailychaos/project/data/repository/ChaosRepositoryImpl.kt
package com.dailychaos.project.data.repository

import com.dailychaos.project.data.remote.firebase.FirebaseAuthService
import com.dailychaos.project.data.remote.firebase.FirebaseFirestoreService
import com.dailychaos.project.data.mapper.toChaosEntry
import com.dailychaos.project.data.mapper.toChaosEntryRequest
import com.dailychaos.project.data.mapper.toFirestoreMap
import com.dailychaos.project.domain.model.ChaosEntry
import com.dailychaos.project.domain.repository.ChaosRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber // Import Timber for logging

/**
 * Chaos Repository Implementation - Enhanced dengan Debug Logging
 * "Mengelola data chaos, baik dari cloud maupun (nanti) lokal!"
 */
@Singleton
class ChaosRepositoryImpl @Inject constructor(
    private val firestoreService: FirebaseFirestoreService,
    private val authService: FirebaseAuthService // Perlu untuk mendapatkan userId
) : ChaosRepository {

    // Helper untuk mendapatkan userId saat ini
    private suspend fun getCurrentUserId(): String {
        return authService.currentUser?.uid ?: throw IllegalStateException("User not authenticated.")
    }

    override suspend fun createChaosEntry(userId: String, entry: ChaosEntry): Result<String> {
        return try {
            Timber.d("🚀 ==================== REPOSITORY CREATE CHAOS ENTRY STARTED ====================")
            Timber.d("🚀 Repository received parameters:")
            Timber.d("  - User ID: '$userId'")
            Timber.d("🚀 Repository received ChaosEntry:")
            Timber.d("  - ID: '${entry.id}'")
            Timber.d("  - UserID: '${entry.userId}'")
            Timber.d("  - Title: '${entry.title}'")
            Timber.d("  - Description length: ${entry.description.length}")
            Timber.d("  - Chaos Level: ${entry.chaosLevel}")
            Timber.d("  - Mini Wins: ${entry.miniWins}")
            Timber.d("  - Tags: ${entry.tags}")
            Timber.d("  - Share to Community: ${entry.isSharedToCommunity}")

            // Validate input
            if (userId.isBlank()) {
                val error = "User ID parameter cannot be empty"
                Timber.e("❌ Repository validation failed: $error")
                return Result.failure(Exception(error))
            }

            if (entry.title.isBlank()) {
                val error = "Title cannot be empty"
                Timber.e("❌ Repository validation failed: $error")
                return Result.failure(Exception(error))
            }

            if (entry.description.isBlank()) {
                val error = "Description cannot be empty"
                Timber.e("❌ Repository validation failed: $error")
                return Result.failure(Exception(error))
            }

            Timber.d("✅ Repository validation passed")

            // Convert to request DTO
            Timber.d("🔄 ==================== CONVERTING TO REQUEST DTO ====================")
            val request = entry.toChaosEntryRequest()

            Timber.d("🔄 Converted to ChaosEntryRequest:")
            Timber.d("  - Title: '${request.title}'")
            Timber.d("  - Content: '${request.content.take(50)}...'")
            Timber.d("  - Chaos Level: ${request.chaosLevel}")
            Timber.d("  - Share to Feed: ${request.shareToFeed}")
            Timber.d("  - Mini Wins: ${request.miniWins}")
            Timber.d("  - Tags: ${request.tags}")

            // Call Firebase service
            Timber.d("💾 ==================== CALLING FIREBASE SERVICE ====================")
            Timber.d("💾 Calling firestoreService.createChaosEntry(userId='$userId', request)")

            val result = firestoreService.createChaosEntry(userId, request)

            Timber.d("📤 Firebase service call completed, processing result...")

            result.fold(
                onSuccess = { entryId ->
                    Timber.d("✅ ==================== FIREBASE SERVICE SUCCESS ====================")
                    Timber.d("✅ Firebase service returned success with entry ID: $entryId")

                    if (entryId.isBlank()) {
                        val error = "Firebase service returned empty entry ID"
                        Timber.e("❌ $error")
                        return Result.failure(Exception(error))
                    }

                    Timber.d("🎉 REPOSITORY CREATE COMPLETED SUCCESSFULLY!")
                    Timber.d("🎉 Final entry ID: $entryId")
                    Result.success(entryId)
                },
                onFailure = { exception ->
                    Timber.e("❌ ==================== FIREBASE SERVICE FAILURE ====================")
                    Timber.e(exception, "❌ Firebase service failed to create chaos entry")
                    Timber.e("❌ Exception type: ${exception::class.simpleName}")
                    Timber.e("❌ Exception message: ${exception.message}")
                    Timber.e("❌ Exception cause: ${exception.cause}")

                    Result.failure(exception)
                }
            )

        } catch (e: Exception) {
            Timber.e("💥 ==================== REPOSITORY EXCEPTION ====================")
            Timber.e(e, "💥 Unexpected error in ChaosRepositoryImpl.createChaosEntry")
            Timber.e("💥 Exception type: ${e::class.simpleName}")
            Timber.e("💥 Exception message: ${e.message}")
            Timber.e("💥 Exception cause: ${e.cause}")
            e.printStackTrace()

            Result.failure(e)
        }
    }

    override fun getChaosEntry(userId: String, entryId: String): Flow<ChaosEntry?> {
        Timber.d("📖 Getting chaos entry: userId=$userId, entryId=$entryId")
        return firestoreService.getChaosEntry(userId, entryId).map { map ->
            val entry = map?.toChaosEntry()
            Timber.d("📖 Retrieved chaos entry: ${entry?.title ?: "NULL"}")
            entry
        }
    }

    override fun getAllChaosEntries(userId: String): Flow<List<ChaosEntry>> {
        Timber.d("📚 Getting all chaos entries for user: $userId")
        return firestoreService.getChaosEntries(userId).map { listMap ->
            val entries = listMap.map { it.toChaosEntry() }
            Timber.d("📚 Retrieved ${entries.size} chaos entries")
            entries
        }
    }

    override fun getRecentChaosEntries(userId: String, limit: Int): Flow<List<ChaosEntry>> {
        Timber.d("📚 Getting recent chaos entries for user: $userId, limit: $limit")
        return firestoreService.getChaosEntries(userId, limit.toLong()).map { listMap ->
            val entries = listMap.map { it.toChaosEntry() }
            Timber.d("📚 Retrieved ${entries.size} recent chaos entries")
            entries
        }
    }

    override suspend fun updateChaosEntry(userId: String, entry: ChaosEntry): Result<Unit> {
        return try {
            Timber.d("✏️ Updating chaos entry: userId=$userId, entryId=${entry.id}")
            val result = firestoreService.updateChaosEntry(userId, entry.id, entry.toFirestoreMap())

            result.fold(
                onSuccess = {
                    Timber.d("✅ Chaos entry ${entry.id} updated successfully")
                    Result.success(Unit)
                },
                onFailure = { exception ->
                    Timber.e(exception, "❌ Failed to update chaos entry ${entry.id}")
                    Result.failure(exception)
                }
            )
        } catch (e: Exception) {
            Timber.e(e, "❌ Error updating chaos entry: userId=$userId, entryId=${entry.id}")
            Result.failure(e)
        }
    }

    override suspend fun deleteChaosEntry(userId: String, entryId: String): Result<Unit> {
        return try {
            Timber.d("🗑️ Deleting chaos entry: userId=$userId, entryId=$entryId")
            val result = firestoreService.deleteChaosEntry(userId, entryId)

            result.fold(
                onSuccess = {
                    Timber.d("✅ Chaos entry $entryId deleted successfully")
                    Result.success(Unit)
                },
                onFailure = { exception ->
                    Timber.e(exception, "❌ Failed to delete chaos entry $entryId")
                    Result.failure(exception)
                }
            )
        } catch (e: Exception) {
            Timber.e(e, "❌ Error deleting chaos entry: userId=$userId, entryId=$entryId")
            Result.failure(e)
        }
    }
}