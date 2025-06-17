// File: app/src/main/java/com/dailychaos/project/data/remote/firebase/FirebaseFirestoreService.kt
package com.dailychaos.project.data.remote.firebase

import com.dailychaos.project.data.remote.dto.request.ChaosEntryRequest
import com.dailychaos.project.util.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import timber.log.Timber // Import Timber for logging
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.datetime.Instant
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.snapshots // Tambahkan import ini

/**
 * Firebase Firestore Service
 * "Gudang data petualangan kita di Cloud!"
 */
@Singleton
class FirebaseFirestoreService @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    init {
        if (System.getProperty("timber.tree.planted") == null) {
            System.setProperty("timber.tree.planted", "true")
        }
    }

    /**
     * Creates a new chaos entry in Firestore.
     * "Mencatat kejadian chaos baru di buku petualangan cloud!"
     */
    suspend fun createChaosEntry(userId: String, entry: ChaosEntryRequest): Result<String> {
        return try {
            val entryRef = firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .collection(Constants.COLLECTION_CHAOS_ENTRIES)
                .document() // Auto-generate ID

            val data = hashMapOf(
                "id" to entryRef.id,
                "userId" to userId,
                "title" to entry.title,
                "content" to entry.content,
                "chaosLevel" to entry.chaosLevel,
                "mood" to entry.mood,
                "tags" to entry.tags,
                "isAnonymous" to entry.isAnonymous,
                "shareToFeed" to entry.shareToFeed,
                "location" to entry.location?.let {
                    mapOf(
                        "city" to it.city,
                        "country" to it.country,
                        "timezone" to it.timezone,
                        "coordinates" to it.coordinates?.let { coords ->
                            mapOf("latitude" to coords.latitude, "longitude" to coords.longitude)
                        }
                    )
                },
                "attachments" to entry.attachments.map {
                    mapOf(
                        "type" to it.type,
                        "url" to it.url,
                        "fileName" to it.fileName,
                        "fileSize" to it.fileSize,
                        "mimeType" to it.mimeType
                    )
                },
                "weatherData" to entry.weatherData?.let {
                    mapOf(
                        "condition" to it.condition,
                        "temperature" to it.temperature,
                        "humidity" to it.humidity,
                        "description" to it.description
                    )
                },
                "miniWins" to entry.miniWins,
                "gratitudeNotes" to entry.gratitudeNotes,
                "tomorrowGoals" to entry.tomorrowGoals,
                "createdAt" to Timestamp.now(),
                "updatedAt" to Timestamp.now()
            )

            entryRef.set(data).await()
            Timber.d("Chaos entry created with ID: ${entryRef.id}")
            Result.success(entryRef.id)
        } catch (e: Exception) {
            Timber.e(e, "Error creating chaos entry")
            Result.failure(e)
        }
    }

    /**
     * Fetches a single chaos entry by its ID as a Flow for real-time updates.
     * "Mencari satu halaman dari buku petualangan chaos secara real-time!"
     */
    fun getChaosEntry(userId: String, entryId: String): Flow<Map<String, Any>?> {
        return firestore.collection(Constants.COLLECTION_USERS)
            .document(userId)
            .collection(Constants.COLLECTION_CHAOS_ENTRIES)
            .document(entryId)
            .snapshots() // Menggunakan snapshots() untuk mendapatkan Flow
            .map { snapshot ->
                snapshot.data
            }
    }

    /**
     * Fetches multiple chaos entries for a user, ordered by creation time.
     * "Membuka seluruh koleksi cerita chaos seorang petualang!"
     */
    fun getChaosEntries(userId: String, limit: Long? = null): Flow<List<Map<String, Any>>> {
        return firestore.collection(Constants.COLLECTION_USERS)
            .document(userId)
            .collection(Constants.COLLECTION_CHAOS_ENTRIES)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .let { query ->
                if (limit != null) query.limit(limit) else query
            }
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { document ->
                    document.data
                }
            }
    }

    /**
     * Updates an existing chaos entry.
     * "Memperbarui detail petualangan chaos yang sudah tercatat!"
     */
    suspend fun updateChaosEntry(userId: String, entryId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .collection(Constants.COLLECTION_CHAOS_ENTRIES)
                .document(entryId)
                .update(updates + mapOf("updatedAt" to Timestamp.now()))
                .await()
            Timber.d("Chaos entry $entryId updated successfully.")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error updating chaos entry $entryId")
            Result.failure(e)
        }
    }

    /**
     * Deletes a chaos entry.
     * "Menghapus jejak chaos yang tak ingin diingat lagi."
     */
    suspend fun deleteChaosEntry(userId: String, entryId: String): Result<Unit> {
        return try {
            firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .collection(Constants.COLLECTION_CHAOS_ENTRIES)
                .document(entryId)
                .delete()
                .await()
            Timber.d("Chaos entry $entryId deleted successfully.")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error deleting chaos entry $entryId")
            Result.failure(e)
        }
    }
}