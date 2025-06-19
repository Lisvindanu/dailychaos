// File: app/src/main/java/com/dailychaos/project/data/repository/ChaosRepositoryImpl.kt
package com.dailychaos.project.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.dailychaos.project.data.remote.firebase.FirebaseAuthService
import com.dailychaos.project.data.remote.firebase.FirebaseFirestoreService
import com.dailychaos.project.data.mapper.toChaosEntry
import com.dailychaos.project.data.mapper.toChaosEntryRequest
import com.dailychaos.project.data.mapper.toFirestoreMap
import com.dailychaos.project.domain.model.ChaosEntry
import com.dailychaos.project.domain.repository.ChaosRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
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

    @RequiresApi(Build.VERSION_CODES.O)
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
            Timber.d("  - Created At: ${entry.createdAt}")
            Timber.d("  - Updated At: ${entry.updatedAt}")

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

            // Ensure entry has proper timestamps
            val now = Clock.System.now()
            val entryWithTimestamps = if (entry.createdAt == Instant.DISTANT_PAST) {
                entry.copy(
                    createdAt = now,
                    updatedAt = now
                )
            } else {
                entry.copy(updatedAt = now)
            }

            Timber.d("🕐 Timestamps set - CreatedAt: ${entryWithTimestamps.createdAt}, UpdatedAt: ${entryWithTimestamps.updatedAt}")

            // Convert to request DTO
            Timber.d("🔄 ==================== CONVERTING TO REQUEST DTO ====================")
            val request = entryWithTimestamps.toChaosEntryRequest()

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

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getChaosEntry(userId: String, entryId: String): Flow<ChaosEntry?> {
        return try {
            Timber.d("🔍 Getting single chaos entry: userId=$userId, entryId=$entryId")

            firestoreService.getChaosEntry(userId, entryId)
                .map { firestoreData ->
                    firestoreData?.let { data ->
                        try {
                            Timber.d("🔄 Converting firestore data to ChaosEntry")
                            Timber.d("  - Raw data keys: ${data.keys}")
                            Timber.d("  - CreatedAt raw: ${data["createdAt"]} (${data["createdAt"]?.javaClass?.simpleName})")
                            Timber.d("  - UpdatedAt raw: ${data["updatedAt"]} (${data["updatedAt"]?.javaClass?.simpleName})")

                            val chaosEntry = data.toChaosEntry()
                            Timber.d("✅ Successfully converted to ChaosEntry: ${chaosEntry.id}")
                            chaosEntry
                        } catch (e: Exception) {
                            Timber.e(e, "❌ Error converting firestore data to ChaosEntry")
                            Timber.e("  - Raw data: $data")
                            null
                        }
                    }
                }
                .catch { e ->
                    Timber.e(e, "❌ Error getting chaos entry")
                    emit(null)
                }
        } catch (e: Exception) {
            Timber.e(e, "❌ Error in getChaosEntry")
            flowOf(null)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getAllChaosEntries(userId: String): Flow<List<ChaosEntry>> {
        return try {
            Timber.d("🔍 Getting all chaos entries for user: $userId")

            firestoreService.getChaosEntries(userId, null)
                .map { firestoreDataList ->
                    val chaosEntries = firestoreDataList.mapNotNull { firestoreData ->
                        try {
                            Timber.d("🔄 Converting entry: ${firestoreData["id"]}")
                            firestoreData.toChaosEntry()
                        } catch (e: Exception) {
                            Timber.e(e, "❌ Error converting firestore data to ChaosEntry for entry: ${firestoreData["id"]}")
                            null
                        }
                    }
                    Timber.d("✅ Successfully converted ${chaosEntries.size} chaos entries")
                    chaosEntries
                }
                .catch { e ->
                    Timber.e(e, "❌ Error getting all chaos entries")
                    emit(emptyList())
                }
        } catch (e: Exception) {
            Timber.e(e, "❌ Error in getAllChaosEntries")
            flowOf(emptyList())
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getChaosEntriesByDateRange(
        userId: String,
        startDate: String,
        endDate: String
    ): Flow<List<ChaosEntry>> {
        return try {
            Timber.d("🔍 Getting chaos entries by date range: userId=$userId, start=$startDate, end=$endDate")

            // For now, get all entries and filter by date
            // TODO: Implement date range query in FirestoreService
            getAllChaosEntries(userId)
                .map { entries ->
                    entries.filter { entry ->
                        val entryDateString = entry.createdAt.toString()
                        // Simple date comparison - you might want to use proper date parsing
                        entryDateString >= startDate && entryDateString <= endDate
                    }
                }
                .catch { e ->
                    Timber.e(e, "❌ Error getting chaos entries by date range")
                    emit(emptyList())
                }
        } catch (e: Exception) {
            Timber.e(e, "❌ Error in getChaosEntriesByDateRange")
            flowOf(emptyList())
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getRecentChaosEntries(userId: String, limit: Int): Flow<List<ChaosEntry>> {
        return try {
            Timber.d("🔍 Getting recent chaos entries for user: $userId, limit: $limit")

            firestoreService.getChaosEntries(userId, limit.toLong())
                .map { firestoreDataList ->
                    val chaosEntries = firestoreDataList.mapNotNull { firestoreData ->
                        try {
                            firestoreData.toChaosEntry()
                        } catch (e: Exception) {
                            Timber.e(e, "❌ Error converting firestore data to ChaosEntry")
                            null
                        }
                    }
                    Timber.d("✅ Successfully converted ${chaosEntries.size} chaos entries")
                    chaosEntries
                }
                .catch { e ->
                    Timber.e(e, "❌ Error getting recent chaos entries")
                    emit(emptyList())
                }
        } catch (e: Exception) {
            Timber.e(e, "❌ Error in getRecentChaosEntries")
            flowOf(emptyList())
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun updateChaosEntry(userId: String, entry: ChaosEntry): Result<Unit> {
        return try {
            Timber.d("✏️ Updating chaos entry: userId=$userId, entryId=${entry.id}")

            // Update timestamp
            val entryWithUpdatedTimestamp = entry.copy(updatedAt = Clock.System.now())

            val result = firestoreService.updateChaosEntry(userId, entry.id, entryWithUpdatedTimestamp.toFirestoreMap())

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