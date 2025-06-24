// File: app/src/main/java/com/dailychaos/project/data/repository/CommunityRepositoryImpl.kt
package com.dailychaos.project.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.dailychaos.project.data.mapper.toCommunityPost
import com.dailychaos.project.data.mapper.toFirestoreMap
import com.dailychaos.project.domain.model.CommunityPost
import com.dailychaos.project.domain.model.SupportType
import com.dailychaos.project.domain.repository.CommunityRepository
import com.dailychaos.project.domain.repository.CommunityRepositoryExtended
import com.dailychaos.project.util.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Community Repository Implementation - Enhanced Support System
 * "Support yang bisa ganti tipe atau batalin dengan konfirmasi Megumin"
 */
@RequiresApi(Build.VERSION_CODES.O)
@Singleton
class CommunityRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : CommunityRepositoryExtended {

    companion object {
        private const val COLLECTION_SUPPORT_REACTIONS = "support_reactions"
        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_REPORTS = "reports"
    }

    // ============================================================================
    // ENHANCED SUPPORT OPERATIONS - NEW VERSION WITH TYPE CHANGE SUPPORT
    // ============================================================================

    /**
     * Enhanced give support - bisa ganti tipe support kalau udah kasih support sebelumnya
     */
    override suspend fun giveSupport(postId: String, userId: String, supportType: SupportType): Result<Unit> {
        return try {
            Timber.d("💙 ==================== GIVING SUPPORT ENHANCED ====================")
            Timber.d("💙 Giving support to post: $postId (type: $supportType)")
            Timber.d("💙 User ID: $userId")

            // Validasi input terlebih dahulu
            if (postId.isBlank()) {
                Timber.e("❌ Invalid postId: cannot be blank")
                return Result.failure(IllegalArgumentException("Post ID cannot be blank"))
            }

            if (userId.isBlank()) {
                Timber.e("❌ Invalid userId: cannot be blank")
                return Result.failure(IllegalArgumentException("User ID cannot be blank"))
            }

            // Cek apakah post masih ada
            val postSnapshot = firestore.collection(Constants.COLLECTION_COMMUNITY_POSTS)
                .document(postId)
                .get()
                .await()

            if (!postSnapshot.exists()) {
                Timber.e("❌ Post not found: $postId")
                return Result.failure(Exception("Post not found"))
            }

            // Cek apakah user sudah kasih support sebelumnya
            val existingSupportQuery = firestore.collection(COLLECTION_SUPPORT_REACTIONS)
                .whereEqualTo("postId", postId)
                .whereEqualTo("userId", userId)
                .limit(1)
                .get()
                .await()

            if (!existingSupportQuery.isEmpty) {
                val existingSupportDoc = existingSupportQuery.documents[0]
                val existingSupportType = existingSupportDoc.getString("supportType")

                Timber.d("🔄 User already gave support. Existing type: $existingSupportType, New type: ${supportType.name}")

                if (existingSupportType == supportType.name) {
                    // ✅ CRITICAL FIX: Same support type = TOGGLE SUPPORT (remove it)
                    // Let ViewModel handle the confirmation dialog, but Repository removes the support
                    Timber.d("🔄 Same support type detected - removing existing support (toggle behavior)")
                    return removeSupport(postId, userId)
                } else {
                    // Different support type - update the existing support
                    Timber.d("🔄 Changing support type from $existingSupportType to ${supportType.name}")
                    return changeSupportType(postId, userId, supportType, existingSupportDoc.id)
                }
            }

            // User belum kasih support - create new support
            Timber.d("💙 Creating new support reaction")

            // Gunakan transaction untuk atomicity yang lebih baik
            firestore.runTransaction { transaction ->
                try {
                    // 1. Add support reaction
                    val supportReaction = mapOf(
                        "postId" to postId,
                        "userId" to userId,
                        "supportType" to supportType.name,
                        "createdAt" to com.google.firebase.Timestamp.now()
                    )
                    val supportRef = firestore.collection(COLLECTION_SUPPORT_REACTIONS).document()
                    transaction.set(supportRef, supportReaction)

                    // 2. Increment support count on post
                    val postRef = firestore.collection(Constants.COLLECTION_COMMUNITY_POSTS).document(postId)
                    transaction.update(postRef, "supportCount", com.google.firebase.firestore.FieldValue.increment(1))

                    // 3. Update user profile dengan error handling
                    try {
                        val userRef = firestore.collection(COLLECTION_USERS).document(userId)
                        val userUpdates = mapOf(
                            "supportGiven" to com.google.firebase.firestore.FieldValue.increment(1),
                            "totalSupportsGiven" to com.google.firebase.firestore.FieldValue.increment(1),
                            "totalSupportGiven" to com.google.firebase.firestore.FieldValue.increment(1),
                            "lastActiveAt" to com.google.firebase.Timestamp.now()
                        )
                        transaction.update(userRef, userUpdates)
                        Timber.d("✅ User profile updated in transaction")
                    } catch (userError: Exception) {
                        Timber.w(userError, "⚠️ Failed to update user profile, but continuing with support operation")
                        // Don't fail the whole transaction for user profile update
                    }

                    Timber.d("✅ Support transaction completed successfully")
                    null // Return null for successful transaction
                } catch (e: Exception) {
                    Timber.e(e, "❌ Transaction failed")
                    throw e
                }
            }.await()

            Timber.d("✅ Support given successfully")
            Result.success(Unit)

        } catch (e: FirebaseFirestoreException) {
            Timber.e(e, "❌ Firestore error giving support to post: $postId")
            when (e.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                    Timber.e("🔒 Permission denied - check Firestore security rules")
                    Result.failure(Exception("Permission denied. Please check your access rights.", e))
                }
                FirebaseFirestoreException.Code.NOT_FOUND -> {
                    Result.failure(Exception("Post not found", e))
                }
                FirebaseFirestoreException.Code.UNAVAILABLE -> {
                    Result.failure(Exception("Service temporarily unavailable. Please try again.", e))
                }
                else -> {
                    Result.failure(Exception("Failed to give support: ${e.message}", e))
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Unexpected error giving support to post: $postId")
            Result.failure(Exception("Unexpected error occurred. Please try again.", e))
        }
    }

// ============================================================================
// 📝 ADDITIONAL HELPER METHOD untuk ViewModel
// ============================================================================

    /**
     * 🚨 NEW METHOD: Check if giving same support type (for ViewModel confirmation logic)
     * Returns true if user is trying to give the same support type they already gave
     */
    override suspend fun isSameSupportType(postId: String, userId: String, supportType: SupportType): Boolean {
        return try {
            val existingSupportQuery = firestore.collection(COLLECTION_SUPPORT_REACTIONS)
                .whereEqualTo("postId", postId)
                .whereEqualTo("userId", userId)
                .limit(1)
                .get()
                .await()

            if (!existingSupportQuery.isEmpty) {
                val existingSupportType = existingSupportQuery.documents[0].getString("supportType")
                return existingSupportType == supportType.name
            }

            false
        } catch (e: Exception) {
            Timber.e(e, "❌ Error checking same support type")
            false
        }
    }

    /**
     * Change support type - internal function untuk ganti tipe support yang udah ada
     */
    private suspend fun changeSupportType(
        postId: String,
        userId: String,
        newSupportType: SupportType,
        existingSupportDocId: String
    ): Result<Unit> {
        return try {
            Timber.d("🔄 ==================== CHANGING SUPPORT TYPE ====================")
            Timber.d("🔄 Changing support type for post: $postId to: ${newSupportType.name}")

            // Update existing support reaction with new type
            firestore.collection(COLLECTION_SUPPORT_REACTIONS)
                .document(existingSupportDocId)
                .update(mapOf(
                    "supportType" to newSupportType.name,
                    "updatedAt" to com.google.firebase.Timestamp.now()
                ))
                .await()

            // Update user's last active time
            try {
                firestore.collection(COLLECTION_USERS)
                    .document(userId)
                    .update("lastActiveAt", com.google.firebase.Timestamp.now())
                    .await()
            } catch (userError: Exception) {
                Timber.w(userError, "⚠️ Failed to update user last active time")
            }

            Timber.d("✅ Support type changed successfully")
            Result.success(Unit)

        } catch (e: Exception) {
            Timber.e(e, "❌ Error changing support type")
            Result.failure(Exception("Failed to change support type: ${e.message}", e))
        }
    }

    /**
     * Check if user has given support to a post
     * Returns the support type if found, null otherwise
     */
    override suspend fun getUserSupportType(postId: String, userId: String): SupportType? {
        return try {
            Timber.d("🔍 Checking user support for post: $postId, user: $userId")

            val supportQuery = firestore.collection(COLLECTION_SUPPORT_REACTIONS)
                .whereEqualTo("postId", postId)
                .whereEqualTo("userId", userId)
                .limit(1)
                .get()
                .await()

            if (supportQuery.documents.isNotEmpty()) {
                val supportDoc = supportQuery.documents[0]
                val supportTypeString = supportDoc.getString("supportType")

                return supportTypeString?.let {
                    try {
                        SupportType.valueOf(it)
                    } catch (e: IllegalArgumentException) {
                        Timber.w("Unknown support type: $it")
                        null
                    }
                }
            }

            null
        } catch (e: Exception) {
            Timber.e(e, "❌ Error checking user support type")
            null
        }
    }

    override suspend fun removeSupport(postId: String, userId: String): Result<Unit> {
        return try {
            Timber.d("💔 ==================== REMOVING SUPPORT ====================")
            Timber.d("💔 Removing support from post: $postId for user: $userId")

            // Validasi input
            if (postId.isBlank() || userId.isBlank()) {
                return Result.failure(IllegalArgumentException("Post ID and User ID cannot be blank"))
            }

            // Find existing support reaction
            val supportQuery = firestore.collection(COLLECTION_SUPPORT_REACTIONS)
                .whereEqualTo("postId", postId)
                .whereEqualTo("userId", userId)
                .limit(1)
                .get()
                .await()

            if (supportQuery.documents.isEmpty()) {
                Timber.w("⚠️ No support reaction found to remove for post: $postId, user: $userId")
                return Result.failure(Exception("No support found to remove"))
            }

            // Use transaction for atomicity
            firestore.runTransaction { transaction ->
                try {
                    // Delete support reaction
                    val supportDoc = supportQuery.documents[0]
                    transaction.delete(supportDoc.reference)

                    // Decrement support count on post
                    val postRef = firestore.collection(Constants.COLLECTION_COMMUNITY_POSTS).document(postId)
                    transaction.update(postRef, "supportCount", com.google.firebase.firestore.FieldValue.increment(-1))

                    // Update user profile
                    try {
                        val userRef = firestore.collection(COLLECTION_USERS).document(userId)
                        val userUpdates = mapOf(
                            "supportGiven" to com.google.firebase.firestore.FieldValue.increment(-1),
                            "totalSupportsGiven" to com.google.firebase.firestore.FieldValue.increment(-1),
                            "totalSupportGiven" to com.google.firebase.firestore.FieldValue.increment(-1),
                            "lastActiveAt" to com.google.firebase.Timestamp.now()
                        )
                        transaction.update(userRef, userUpdates)
                    } catch (userError: Exception) {
                        Timber.w(userError, "⚠️ Failed to update user profile during support removal")
                    }

                    null
                } catch (e: Exception) {
                    Timber.e(e, "❌ Transaction failed during support removal")
                    throw e
                }
            }.await()

            Timber.d("✅ Support removed successfully")
            Result.success(Unit)

        } catch (e: FirebaseFirestoreException) {
            Timber.e(e, "❌ Firestore error removing support from post: $postId")
            when (e.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                    Result.failure(Exception("Permission denied. Please check your access rights.", e))
                }
                else -> {
                    Result.failure(Exception("Failed to remove support: ${e.message}", e))
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Unexpected error removing support from post: $postId")
            Result.failure(Exception("Unexpected error occurred. Please try again.", e))
        }
    }

    // ============================================================================
    // COMMUNITY FEED OPERATIONS - Keep existing implementation
    // ============================================================================

    override fun getAllCommunityPosts(): Flow<List<CommunityPost>> {
        Timber.d("🌍 Getting all community posts from collection: ${Constants.COLLECTION_COMMUNITY_POSTS}")
        return firestore.collection(Constants.COLLECTION_COMMUNITY_POSTS)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                Timber.d("📄 Retrieved ${snapshot.documents.size} documents from Firestore")
                snapshot.documents.mapNotNull { doc ->
                    try {
                        Timber.d("🔄 Processing document: ${doc.id}")
                        doc.data?.toCommunityPost()
                    } catch (e: Exception) {
                        Timber.e(e, "❌ Error converting document ${doc.id} to CommunityPost")
                        null
                    }
                }.also { posts ->
                    Timber.d("✅ Successfully converted ${posts.size} community posts")
                }
            }
            .catch { exception ->
                Timber.e(exception, "❌ Error getting community posts from ${Constants.COLLECTION_COMMUNITY_POSTS}")
                emit(emptyList())
            }
    }

    override fun getRecentCommunityPosts(limit: Int): Flow<List<CommunityPost>> {
        Timber.d("🌍 Getting recent $limit community posts from collection: ${Constants.COLLECTION_COMMUNITY_POSTS}")
        return firestore.collection(Constants.COLLECTION_COMMUNITY_POSTS)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .snapshots()
            .map { snapshot ->
                Timber.d("📄 Retrieved ${snapshot.documents.size} recent documents from Firestore")
                snapshot.documents.mapNotNull { doc ->
                    try {
                        Timber.d("🔄 Processing recent document: ${doc.id}")
                        Timber.d("📋 Document data keys: ${doc.data?.keys}")
                        doc.data?.toCommunityPost()
                    } catch (e: Exception) {
                        Timber.e(e, "❌ Error converting recent document ${doc.id} to CommunityPost")
                        null
                    }
                }.also { posts ->
                    Timber.d("✅ Successfully converted ${posts.size} recent community posts")
                }
            }
            .catch { exception ->
                Timber.e(exception, "❌ Error getting recent community posts from ${Constants.COLLECTION_COMMUNITY_POSTS}")
                emit(emptyList())
            }
    }

    override fun getCommunityPostsByTags(tags: List<String>): Flow<List<CommunityPost>> {
        Timber.d("🌍 Getting community posts by tags: $tags from collection: ${Constants.COLLECTION_COMMUNITY_POSTS}")
        return firestore.collection(Constants.COLLECTION_COMMUNITY_POSTS)
            .whereArrayContainsAny("tags", tags)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                Timber.d("📄 Retrieved ${snapshot.documents.size} documents with tags: $tags")
                snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.data?.toCommunityPost()
                    } catch (e: Exception) {
                        Timber.e(e, "❌ Error converting document ${doc.id} to CommunityPost")
                        null
                    }
                }.also { posts ->
                    Timber.d("✅ Successfully converted ${posts.size} community posts with tags")
                }
            }
            .catch { exception ->
                Timber.e(exception, "❌ Error getting community posts by tags from ${Constants.COLLECTION_COMMUNITY_POSTS}")
                emit(emptyList())
            }
    }

    // ============================================================================
    // INDIVIDUAL POST OPERATIONS - Keep existing implementation
    // ============================================================================

    override fun getCommunityPost(postId: String): Flow<CommunityPost?> {
        Timber.d("🌍 Getting community post: $postId from collection: ${Constants.COLLECTION_COMMUNITY_POSTS}")
        return firestore.collection(Constants.COLLECTION_COMMUNITY_POSTS)
            .document(postId)
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    try {
                        snapshot.data?.toCommunityPost()
                    } catch (e: Exception) {
                        Timber.e(e, "Error converting document to CommunityPost: $postId")
                        null
                    }
                } else {
                    Timber.w("Community post not found: $postId")
                    null
                }
            }
            .catch { exception ->
                Timber.e(exception, "❌ Error getting community post: $postId from ${Constants.COLLECTION_COMMUNITY_POSTS}")
                emit(null)
            }
    }

    override suspend fun createCommunityPost(post: CommunityPost): Result<String> {
        return try {
            Timber.d("🌍 ==================== CREATING COMMUNITY POST ====================")
            Timber.d("🌍 Creating community post in collection: ${Constants.COLLECTION_COMMUNITY_POSTS}")

            val postMap = post.toFirestoreMap().toMutableMap()
            val documentRef = firestore.collection(Constants.COLLECTION_COMMUNITY_POSTS).document()
            postMap["id"] = documentRef.id

            documentRef.set(postMap).await()

            Timber.d("✅ Community post created successfully with ID: ${documentRef.id}")
            Result.success(documentRef.id)

        } catch (e: FirebaseFirestoreException) {
            Timber.e(e, "❌ Firestore error creating community post")
            when (e.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                    Result.failure(Exception("Permission denied. Cannot create post.", e))
                }
                else -> {
                    Result.failure(Exception("Failed to create post: ${e.message}", e))
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Unexpected error creating community post")
            Result.failure(e)
        }
    }

    override suspend fun updateCommunityPost(postId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            Timber.d("🌍 Updating community post: $postId")

            firestore.collection(Constants.COLLECTION_COMMUNITY_POSTS)
                .document(postId)
                .update(updates)
                .await()

            Timber.d("✅ Community post updated successfully: $postId")
            Result.success(Unit)

        } catch (e: FirebaseFirestoreException) {
            Timber.e(e, "❌ Firestore error updating community post: $postId")
            when (e.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                    Result.failure(Exception("Permission denied. Cannot update post.", e))
                }
                FirebaseFirestoreException.Code.NOT_FOUND -> {
                    Result.failure(Exception("Post not found.", e))
                }
                else -> {
                    Result.failure(Exception("Failed to update post: ${e.message}", e))
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Unexpected error updating community post: $postId")
            Result.failure(e)
        }
    }

    override suspend fun deleteCommunityPost(postId: String): Result<Unit> {
        return try {
            Timber.d("🌍 Deleting community post: $postId")

            firestore.collection(Constants.COLLECTION_COMMUNITY_POSTS)
                .document(postId)
                .delete()
                .await()

            Timber.d("✅ Community post deleted successfully: $postId")
            Result.success(Unit)

        } catch (e: FirebaseFirestoreException) {
            Timber.e(e, "❌ Firestore error deleting community post: $postId")
            when (e.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                    Result.failure(Exception("Permission denied. Cannot delete post.", e))
                }
                else -> {
                    Result.failure(Exception("Failed to delete post: ${e.message}", e))
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Unexpected error deleting community post: $postId")
            Result.failure(e)
        }
    }

    // ============================================================================
    // REPORT OPERATIONS
    // ============================================================================

    override suspend fun reportPost(postId: String, userId: String, reason: String): Result<Unit> {
        return try {
            Timber.d("🚨 Reporting post: $postId (reason: $reason)")

            val reportData = mapOf(
                "postId" to postId,
                "reportedBy" to userId,
                "reason" to reason,
                "createdAt" to com.google.firebase.Timestamp.now(),
                "status" to "pending"
            )

            firestore.collection(COLLECTION_REPORTS)
                .add(reportData)
                .await()

            Timber.d("✅ Post reported successfully")
            Result.success(Unit)

        } catch (e: FirebaseFirestoreException) {
            Timber.e(e, "❌ Firestore error reporting post: $postId")
            when (e.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                    Result.failure(Exception("Permission denied. Cannot report post.", e))
                }
                else -> {
                    Result.failure(Exception("Failed to report post: ${e.message}", e))
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Unexpected error reporting post: $postId")
            Result.failure(e)
        }
    }

    // ============================================================================
    // CHAOS TWINS OPERATIONS
    // ============================================================================

    override fun findChaosTwins(userId: String, tags: List<String>, chaosLevel: Int): Flow<List<CommunityPost>> {
        Timber.d("🔍 Finding chaos twins for user: $userId, tags: $tags, chaosLevel: $chaosLevel")
        return firestore.collection(Constants.COLLECTION_COMMUNITY_POSTS)
            .whereArrayContainsAny("tags", tags)
            .whereEqualTo("chaosLevel", chaosLevel)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(10)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try {
                        val post = doc.data?.toCommunityPost()
                        // Filter out user's own posts
                        if (post?.userId != userId) post else null
                    } catch (e: Exception) {
                        Timber.e(e, "❌ Error converting chaos twin document ${doc.id}")
                        null
                    }
                }
            }
            .catch { exception ->
                Timber.e(exception, "❌ Error finding chaos twins")
                emit(emptyList())
            }
    }

    // ============================================================================
    // STATISTICS
    // ============================================================================

    override suspend fun getCommunityStats(): Result<Map<String, Int>> {
        return try {
            Timber.d("📊 Getting community statistics")

            val postsSnapshot = firestore.collection(Constants.COLLECTION_COMMUNITY_POSTS)
                .get()
                .await()

            val supportSnapshot = firestore.collection(COLLECTION_SUPPORT_REACTIONS)
                .get()
                .await()

            val stats = mapOf(
                "totalPosts" to postsSnapshot.size(),
                "totalSupports" to supportSnapshot.size()
            )

            Timber.d("✅ Community stats retrieved: $stats")
            Result.success(stats)

        } catch (e: Exception) {
            Timber.e(e, "❌ Error getting community statistics")
            Result.failure(e)
        }
    }
}