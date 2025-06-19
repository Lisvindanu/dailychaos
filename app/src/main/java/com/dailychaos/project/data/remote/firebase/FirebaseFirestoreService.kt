// File: app/src/main/java/com/dailychaos/project/data/remote/firebase/FirebaseFirestoreService.kt
package com.dailychaos.project.data.remote.firebase

import android.os.Build
import androidx.annotation.RequiresApi
import com.dailychaos.project.data.remote.dto.request.ChaosEntryRequest
import com.dailychaos.project.util.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import com.dailychaos.project.domain.model.CommunityPost
import kotlinx.datetime.toJavaInstant

/**
 * Firebase Firestore Service - Enhanced dengan Comprehensive Debug Logging
 * "Gudang data petualangan kita di Cloud dengan detailed tracking!"
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
     * Creates a new chaos entry in Firestore with comprehensive logging.
     * "Mencatat kejadian chaos baru di buku petualangan cloud dengan tracking detail!"
     */
    suspend fun createChaosEntry(userId: String, entry: ChaosEntryRequest): Result<String> {
        return try {
            Timber.d("🚀 ==================== FIRESTORE CREATE CHAOS ENTRY STARTED ====================")
            Timber.d("🚀 Input parameters:")
            Timber.d("  - User ID: '$userId'")
            Timber.d("  - Entry Title: '${entry.title}'")
            Timber.d("  - Entry Content length: ${entry.content.length}")
            Timber.d("  - Chaos Level: ${entry.chaosLevel}")
            Timber.d("  - Tags: ${entry.tags}")
            Timber.d("  - Share to Feed: ${entry.shareToFeed}")

            // 1. Validasi authentication terlebih dahulu
            Timber.d("🔐 ==================== AUTHENTICATION VALIDATION ====================")
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                Timber.e("❌ No authenticated user found")
                return Result.failure(Exception("User not authenticated"))
            }

            Timber.d("✅ Firebase Auth User found:")
            Timber.d("  - UID: ${currentUser.uid}")
            Timber.d("  - Email: ${currentUser.email ?: "NULL"}")
            Timber.d("  - Display Name: ${currentUser.displayName ?: "NULL"}")
            Timber.d("  - Is Anonymous: ${currentUser.isAnonymous}")
            Timber.d("  - Provider Data: ${currentUser.providerData.map { it.providerId }}")

            // FIXED: Remove strict user ID validation - trust the auth system
            // For username login, Firebase Auth UID might differ from Firestore document ID
            Timber.d("✅ Authentication validated, using provided User ID: $userId")
            Timber.d("   (Note: Firebase Auth UID ${currentUser.uid} may differ from Firestore document ID $userId for username auth)")

            // 2. Validate input data
            Timber.d("📋 ==================== INPUT VALIDATION ====================")
            val validationError = validateChaosEntryRequest(entry)
            if (validationError != null) {
                Timber.e("❌ Input validation failed: $validationError")
                return Result.failure(Exception("Invalid input: $validationError"))
            }
            Timber.d("✅ Input validation passed")

            // 3. Generate document reference and ID
            Timber.d("📍 ==================== DOCUMENT REFERENCE CREATION ====================")
            val entryRef = firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .collection(Constants.COLLECTION_CHAOS_ENTRIES)
                .document() // Auto-generate ID

            Timber.d("📍 Document reference created:")
            Timber.d("  - Document path: ${entryRef.path}")
            Timber.d("  - Entry ID: ${entryRef.id}")
            Timber.d("  - Collection: ${Constants.COLLECTION_USERS}/${userId}/${Constants.COLLECTION_CHAOS_ENTRIES}")

            // 4. Build data to be saved
            Timber.d("📦 ==================== DATA PREPARATION ====================")
            val timestamp = Timestamp.now()

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
                "createdAt" to timestamp,
                "updatedAt" to timestamp
            )

            Timber.d("📦 Data prepared for Firestore:")
            Timber.d("  - Document ID: ${data["id"]}")
            Timber.d("  - User ID: ${data["userId"]}")
            Timber.d("  - Title: ${data["title"]}")
            Timber.d("  - Content length: ${(data["content"] as String).length}")
            Timber.d("  - Chaos Level: ${data["chaosLevel"]}")
            Timber.d("  - Tags count: ${(data["tags"] as List<*>).size}")
            Timber.d("  - Mini Wins count: ${(data["miniWins"] as List<*>).size}")
            Timber.d("  - Share to Feed: ${data["shareToFeed"]}")
            Timber.d("  - Timestamp: $timestamp")

            // 5. Attempt to write to Firestore
            Timber.d("💾 ==================== FIRESTORE WRITE OPERATION ====================")
            Timber.d("💾 Starting Firestore write operation...")

            entryRef.set(data).await()

            Timber.d("✅ ==================== FIRESTORE WRITE SUCCESS ====================")
            Timber.d("✅ Chaos entry written to Firestore successfully!")
            Timber.d("✅ Entry ID: ${entryRef.id}")
            Timber.d("✅ Document path: ${entryRef.path}")

            // 6. Verify the write by reading back the document
            Timber.d("🔍 ==================== VERIFICATION READ ====================")
            try {
                val verificationSnapshot = entryRef.get().await()
                if (verificationSnapshot.exists()) {
                    Timber.d("✅ Verification successful - document exists in Firestore")
                    Timber.d("✅ Verified data title: ${verificationSnapshot.getString("title")}")
                    Timber.d("✅ Verified data user ID: ${verificationSnapshot.getString("userId")}")
                } else {
                    Timber.w("⚠️ Verification warning - document not found immediately after write")
                    // This might be normal due to eventual consistency, but still return success
                }
            } catch (verificationError: Exception) {
                Timber.w(verificationError, "⚠️ Verification read failed, but write was successful")
                // Don't fail the entire operation for verification failure
            }

            Timber.d("🎉 CREATE CHAOS ENTRY COMPLETED SUCCESSFULLY!")
            Result.success(entryRef.id)

        } catch (e: Exception) {
            Timber.e("❌ ==================== FIRESTORE ERROR ====================")
            Timber.e(e, "❌ Error creating chaos entry in Firestore")

            // Log detailed error information
            when (e) {
                is com.google.firebase.firestore.FirebaseFirestoreException -> {
                    Timber.e("🔥 Firestore specific error:")
                    Timber.e("  - Error code: ${e.code}")
                    Timber.e("  - Error message: ${e.message}")

                    // Handle specific Firestore errors
                    when (e.code) {
                        com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                            Timber.e("🚫 Permission denied - check Firestore security rules")
                            Timber.e("🚫 User might not have write permission to this collection")
                        }
                        com.google.firebase.firestore.FirebaseFirestoreException.Code.UNAUTHENTICATED -> {
                            Timber.e("🔐 User not authenticated properly with Firestore")
                        }
                        com.google.firebase.firestore.FirebaseFirestoreException.Code.UNAVAILABLE -> {
                            Timber.e("📡 Firestore service unavailable - network issue")
                        }
                        com.google.firebase.firestore.FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> {
                            Timber.e("⏰ Firestore operation timed out")
                        }
                        else -> {
                            Timber.e("🔥 Other Firestore error: ${e.code}")
                        }
                    }
                }
                is com.google.firebase.auth.FirebaseAuthException -> {
                    Timber.e("🔐 Firebase Auth error:")
                    Timber.e("  - Error code: ${e.errorCode}")
                    Timber.e("  - Error message: ${e.message}")
                }
                else -> {
                    Timber.e("💥 General error:")
                    Timber.e("  - Exception type: ${e::class.simpleName}")
                    Timber.e("  - Error message: ${e.message}")
                    Timber.e("  - Error cause: ${e.cause}")
                }
            }

            // Print stack trace for debugging
            e.printStackTrace()

            Result.failure(e)
        }
    }

    /**
     * Validates chaos entry request data
     */
    private fun validateChaosEntryRequest(entry: ChaosEntryRequest): String? {
        return when {
            entry.title.isBlank() -> "Title cannot be empty"
            entry.title.length < 3 -> "Title must be at least 3 characters"
            entry.title.length > 100 -> "Title must be less than 100 characters"
            entry.content.isBlank() -> "Content cannot be empty"
            entry.content.length < 10 -> "Content must be at least 10 characters"
            entry.content.length > 5000 -> "Content must be less than 5000 characters"
            entry.chaosLevel < 1 || entry.chaosLevel > 10 -> "Chaos level must be between 1 and 10"
            entry.tags.size > 10 -> "Maximum 10 tags allowed"
            entry.miniWins.size > 20 -> "Maximum 20 mini wins allowed"
            else -> null
        }
    }

    /**
     * Fetches a single chaos entry by its ID as a Flow for real-time updates.
     * "Mencari satu halaman dari buku petualangan chaos secara real-time!"
     */
    fun getChaosEntry(userId: String, entryId: String): Flow<Map<String, Any>?> {
        Timber.d("📖 Getting chaos entry: userId=$userId, entryId=$entryId")
        return firestore.collection(Constants.COLLECTION_USERS)
            .document(userId)
            .collection(Constants.COLLECTION_CHAOS_ENTRIES)
            .document(entryId)
            .snapshots()
            .map { snapshot ->
                val data = snapshot.data
                Timber.d("📖 Chaos entry data retrieved: ${data != null}")
                data
            }
    }

    /**
     * Fetches multiple chaos entries for a user, ordered by creation time.
     * "Membuka seluruh koleksi cerita chaos seorang petualang!"
     */
    fun getChaosEntries(userId: String, limit: Long? = null): Flow<List<Map<String, Any>>> {
        Timber.d("📚 Getting chaos entries for user: $userId, limit: $limit")
        return firestore.collection(Constants.COLLECTION_USERS)
            .document(userId)
            .collection(Constants.COLLECTION_CHAOS_ENTRIES)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .let { query ->
                if (limit != null) query.limit(limit) else query
            }
            .snapshots()
            .map { snapshot ->
                val entries = snapshot.documents.mapNotNull { document ->
                    document.data
                }
                Timber.d("📚 Retrieved ${entries.size} chaos entries")
                entries
            }
    }

    /**
     * Updates an existing chaos entry.
     * "Memperbarui detail petualangan chaos yang sudah tercatat!"
     */
    suspend fun updateChaosEntry(userId: String, entryId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            Timber.d("✏️ Updating chaos entry: userId=$userId, entryId=$entryId")
            firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .collection(Constants.COLLECTION_CHAOS_ENTRIES)
                .document(entryId)
                .update(updates + mapOf("updatedAt" to Timestamp.now()))
                .await()
            Timber.d("✅ Chaos entry $entryId updated successfully.")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "❌ Error updating chaos entry $entryId")
            Result.failure(e)
        }
    }

    /**
     * Deletes a chaos entry.
     * "Menghapus jejak chaos yang tak ingin diingat lagi."
     */
    suspend fun deleteChaosEntry(userId: String, entryId: String): Result<Unit> {
        return try {
            Timber.d("🗑️ Deleting chaos entry: userId=$userId, entryId=$entryId")
            firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .collection(Constants.COLLECTION_CHAOS_ENTRIES)
                .document(entryId)
                .delete()
                .await()
            Timber.d("✅ Chaos entry $entryId deleted successfully.")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "❌ Error deleting chaos entry $entryId")
            Result.failure(e)
        }
    }


    /**
     * Creates a new community post in the global community_posts collection.
     * This method is called when a user chooses to share their chaos entry to the community.
     * "Membagikan cerita chaos ke seluruh komunitas petualang!"
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun createCommunityPost(communityPost: CommunityPost): Result<String> {
        return try {
            Timber.d("🚀 ==================== FIRESTORE CREATE COMMUNITY POST STARTED ====================")
            Timber.d("🚀 Input CommunityPost:")
            Timber.d("  - Post ID: '${communityPost.id}'")
            Timber.d("  - User ID (Original): '${communityPost.userId}'") // Keep original for reference
            Timber.d("  - Username: '${communityPost.username}'")
            Timber.d("  - Title: '${communityPost.title}'")
            Timber.d("  - Content length: ${communityPost.description.length}")
            Timber.d("  - Chaos Level: ${communityPost.chaosLevel}")
            Timber.d("  - Is Anonymous: ${communityPost.isAnonymous}")

            // 1. Validasi authentication (ensure a user is still authenticated)
            Timber.d("🔐 Community Post Auth Validation:")
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                Timber.e("❌ No authenticated user found for community post creation")
                return Result.failure(Exception("User not authenticated for community post"))
            }

            // Generate document reference and ID if not provided (should be provided from mapper)
            val communityPostRef = firestore.collection(Constants.COLLECTION_COMMUNITY_POSTS).document(communityPost.id)

            Timber.d("📍 Community Post Document reference created:")
            Timber.d("  - Document path: ${communityPostRef.path}")
            Timber.d("  - Post ID: ${communityPostRef.id}")

            // Build data for the community post
            val data = hashMapOf(
                "id" to communityPost.id,
                "userId" to communityPost.userId, // Storing original userId for internal linking/moderation
                "username" to communityPost.username,
                "title" to communityPost.title,
                "content" to communityPost.description, // Use description as content
                "chaosLevel" to communityPost.chaosLevel,
                "miniWins" to communityPost.miniWins,
                "tags" to communityPost.tags,
                "isAnonymous" to communityPost.isAnonymous,
                "createdAt" to Timestamp(communityPost.createdAt.toJavaInstant()),
                "supportCount" to communityPost.supportCount // Initial support count
            )

            Timber.d("📦 Community Post Data prepared for Firestore:")
            Timber.d("  - Document ID: ${data["id"]}")
            Timber.d("  - Is Anonymous: ${data["isAnonymous"]}")
            Timber.d("  - Title: ${data["title"]}")


            // Set the document in the community_posts collection
            communityPostRef.set(data).await()

            Timber.d("✅ Community post written to Firestore successfully!")
            Timber.d("✅ Community Post ID: ${communityPostRef.id}")

            Result.success(communityPostRef.id)

        } catch (e: Exception) {
            Timber.e(e, "❌ Error creating community post in Firestore")
            Result.failure(e)
        }
    }

}