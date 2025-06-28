// File: app/src/main/java/com/dailychaos/project/data/repository/CommunityRepositoryImpl.kt
package com.dailychaos.project.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.dailychaos.project.data.mapper.toCommunityPost
import com.dailychaos.project.data.mapper.toFirestoreMap
import com.dailychaos.project.data.mapper.toSupportComment // ✅ FIXED: Add import
import com.dailychaos.project.domain.model.CommunityPost
import com.dailychaos.project.domain.model.SupportComment
import com.dailychaos.project.domain.model.SupportCommentRequest
import com.dailychaos.project.domain.model.SupportType
import com.dailychaos.project.domain.repository.AuthRepository
import com.dailychaos.project.domain.repository.CommunityRepositoryExtended
import com.dailychaos.project.util.Constants
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.Clock
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
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) : CommunityRepositoryExtended {

    companion object {
        private const val COLLECTION_SUPPORT_REACTIONS = "support_reactions"
        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_REPORTS = "reports"
        private const val COLLECTION_COMMENTS = "support_comments"
        private const val COLLECTION_COMMENT_LIKES = "comment_likes"
    }

    // ============================================================================
    // ENHANCED SUPPORT OPERATIONS
    // ============================================================================

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
            val postSnapshot = firestore.collection(Constants.COLLECTION_COMMUNITY_FEED)
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
                    // Same support type = TOGGLE SUPPORT (remove it)
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
                        "createdAt" to Timestamp.now()
                    )
                    val supportRef = firestore.collection(COLLECTION_SUPPORT_REACTIONS).document()
                    transaction.set(supportRef, supportReaction)

                    // 2. Increment support count on post
                    val postRef = firestore.collection(Constants.COLLECTION_COMMUNITY_FEED).document(postId)
                    transaction.update(postRef, "supportCount", com.google.firebase.firestore.FieldValue.increment(1))

                    // 3. Update user profile dengan error handling
                    try {
                        val userRef = firestore.collection(COLLECTION_USERS).document(userId)
                        val userUpdates = mapOf(
                            "supportGiven" to com.google.firebase.firestore.FieldValue.increment(1),
                            "totalSupportsGiven" to com.google.firebase.firestore.FieldValue.increment(1),
                            "totalSupportGiven" to com.google.firebase.firestore.FieldValue.increment(1),
                            "lastActiveAt" to Timestamp.now()
                        )
                        transaction.update(userRef, userUpdates)
                        Timber.d("✅ User profile updated in transaction")
                    } catch (userError: Exception) {
                        Timber.w(userError, "⚠️ Failed to update user profile, but continuing with support operation")
                    }

                    Timber.d("✅ Support transaction completed successfully")
                    null
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
                    "updatedAt" to Timestamp.now()
                ))
                .await()

            // Update user's last active time
            try {
                firestore.collection(COLLECTION_USERS)
                    .document(userId)
                    .update("lastActiveAt", Timestamp.now())
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

    override suspend fun getUserLikedComments(commentIds: List<String>, userId: String): List<String> {
        return try {
            Timber.d("🔍 Getting liked comments for user: $userId, comments: $commentIds")

            if (commentIds.isEmpty()) {
                return emptyList()
            }

            // Query collection comment_likes untuk mendapatkan like status
            val likedComments = firestore.collection("comment_likes")
                .whereEqualTo("userId", userId)
                .whereIn("commentId", commentIds)
                .get()
                .await()

            val likedCommentIds = likedComments.documents.mapNotNull { doc ->
                doc.getString("commentId")
            }

            Timber.d("✅ Found ${likedCommentIds.size} liked comments: $likedCommentIds")
            likedCommentIds

        } catch (e: Exception) {
            Timber.e(e, "❌ Error getting user liked comments")
            emptyList()
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
                    val postRef = firestore.collection(Constants.COLLECTION_COMMUNITY_FEED).document(postId)
                    transaction.update(postRef, "supportCount", com.google.firebase.firestore.FieldValue.increment(-1))

                    // Update user profile
                    try {
                        val userRef = firestore.collection(COLLECTION_USERS).document(userId)
                        val userUpdates = mapOf(
                            "supportGiven" to com.google.firebase.firestore.FieldValue.increment(-1),
                            "totalSupportsGiven" to com.google.firebase.firestore.FieldValue.increment(-1),
                            "totalSupportGiven" to com.google.firebase.firestore.FieldValue.increment(-1),
                            "lastActiveAt" to Timestamp.now()
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

        } catch (e: Exception) {
            Timber.e(e, "❌ Unexpected error removing support from post: $postId")
            Result.failure(Exception("Unexpected error occurred. Please try again.", e))
        }
    }

    // ============================================================================
    // COMMUNITY FEED OPERATIONS
    // ============================================================================

    override fun getAllCommunityPosts(): Flow<List<CommunityPost>> {
        Timber.d("🌍 Getting all community posts from collection: ${Constants.COLLECTION_COMMUNITY_FEED}")
        return firestore.collection(Constants.COLLECTION_COMMUNITY_FEED)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                Timber.d("📄 Retrieved ${snapshot.documents.size} documents from Firestore")
                snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.data?.toCommunityPost()
                    } catch (e: Exception) {
                        Timber.e(e, "❌ Error converting document ${doc.id} to CommunityPost")
                        null
                    }
                }
            }
            .catch { exception ->
                Timber.e(exception, "❌ Error getting community posts")
                emit(emptyList())
            }
    }

    override fun getRecentCommunityPosts(limit: Int): Flow<List<CommunityPost>> {
        return firestore.collection(Constants.COLLECTION_COMMUNITY_FEED)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.data?.toCommunityPost()
                    } catch (e: Exception) {
                        Timber.e(e, "❌ Error converting document ${doc.id}")
                        null
                    }
                }
            }
            .catch { exception ->
                Timber.e(exception, "❌ Error getting recent community posts")
                emit(emptyList())
            }
    }

    override fun getCommunityPostsByTags(tags: List<String>): Flow<List<CommunityPost>> {
        return firestore.collection(Constants.COLLECTION_COMMUNITY_FEED)
            .whereArrayContainsAny("tags", tags)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.data?.toCommunityPost()
                    } catch (e: Exception) {
                        Timber.e(e, "❌ Error converting document ${doc.id}")
                        null
                    }
                }
            }
            .catch { exception ->
                Timber.e(exception, "❌ Error getting community posts by tags")
                emit(emptyList())
            }
    }

    override fun getCommunityPost(postId: String): Flow<CommunityPost?> {
        return firestore.collection(Constants.COLLECTION_COMMUNITY_FEED)
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
                    null
                }
            }
            .catch { exception ->
                Timber.e(exception, "❌ Error getting community post: $postId")
                emit(null)
            }
    }

    override suspend fun createCommunityPost(post: CommunityPost): Result<String> {
        return try {
            val postMap = post.toFirestoreMap().toMutableMap()
            val documentRef = firestore.collection(Constants.COLLECTION_COMMUNITY_FEED).document()
            postMap["id"] = documentRef.id

            documentRef.set(postMap).await()
            Result.success(documentRef.id)
        } catch (e: Exception) {
            Timber.e(e, "❌ Error creating community post")
            Result.failure(e)
        }
    }

    override suspend fun updateCommunityPost(postId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            firestore.collection(Constants.COLLECTION_COMMUNITY_FEED)
                .document(postId)
                .update(updates)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "❌ Error updating community post: $postId")
            Result.failure(e)
        }
    }

    override suspend fun deleteCommunityPost(postId: String): Result<Unit> {
        return try {
            firestore.collection(Constants.COLLECTION_COMMUNITY_FEED)
                .document(postId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "❌ Error deleting community post: $postId")
            Result.failure(e)
        }
    }

    override suspend fun reportPost(postId: String, userId: String, reason: String): Result<Unit> {
        return try {
            val reportData = mapOf(
                "postId" to postId,
                "reportedBy" to userId,
                "reason" to reason,
                "createdAt" to Timestamp.now(),
                "status" to "pending"
            )

            firestore.collection(COLLECTION_REPORTS)
                .add(reportData)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "❌ Error reporting post: $postId")
            Result.failure(e)
        }
    }

    override fun findChaosTwins(userId: String, tags: List<String>, chaosLevel: Int): Flow<List<CommunityPost>> {
        return firestore.collection(Constants.COLLECTION_COMMUNITY_FEED)
            .whereArrayContainsAny("tags", tags)
            .whereEqualTo("chaosLevel", chaosLevel)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(10)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try {
                        val post = doc.data?.toCommunityPost()
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

    override suspend fun getCommunityStats(): Result<Map<String, Int>> {
        return try {
            val postsSnapshot = firestore.collection(Constants.COLLECTION_COMMUNITY_FEED)
                .get()
                .await()

            val supportSnapshot = firestore.collection(COLLECTION_SUPPORT_REACTIONS)
                .get()
                .await()

            val stats = mapOf(
                "totalPosts" to postsSnapshot.size(),
                "totalSupports" to supportSnapshot.size()
            )

            Result.success(stats)
        } catch (e: Exception) {
            Timber.e(e, "❌ Error getting community statistics")
            Result.failure(e)
        }
    }

    // ============================================================================
    // COMMENT OPERATIONS
    // ============================================================================

    override fun getPostComments(postId: String): Flow<List<SupportComment>> {
        return try {
            Timber.d("💬 Loading comments for post: $postId")

            firestore.collection(COLLECTION_COMMENTS)
                .whereEqualTo("postId", postId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull { document ->
                        try {
                            val data = document.data ?: return@mapNotNull null
                            data.toSupportComment(document.id) // ✅ FIXED: Use mapper extension
                        } catch (e: Exception) {
                            Timber.e(e, "❌ Error parsing comment document: ${document.id}")
                            null
                        }
                    }
                }
                .catch { exception ->
                    Timber.e(exception, "❌ Error loading comments for post: $postId")
                    emit(emptyList())
                }
        } catch (e: Exception) {
            Timber.e(e, "💥 Unexpected error in getPostComments")
            flowOf(emptyList())
        }
    }

    override suspend fun postComment(commentRequest: SupportCommentRequest): Result<String> {
        return try {
            Timber.d("💬 ==================== POSTING COMMENT ====================")
            Timber.d("💬 Comment request: $commentRequest")

            // Validation
            if (commentRequest.postId.isBlank()) {
                return Result.failure(IllegalArgumentException("Post ID cannot be blank"))
            }
            if (commentRequest.content.isBlank()) {
                return Result.failure(IllegalArgumentException("Comment content cannot be blank"))
            }

            val currentUserId = getCurrentUserId()
                ?: return Result.failure(IllegalStateException("User not authenticated"))

            // Get user info untuk anonymous username
            val userDoc = firestore.collection(COLLECTION_USERS)
                .document(currentUserId)
                .get()
                .await()

            val username = userDoc.getString("username") ?: "Anonymous"
            val anonymousUsername = if (commentRequest.isAnonymous) {
                generateAnonymousUsername(username)
            } else {
                username
            }

            // Create comment document
            val commentData = mapOf(
                "postId" to commentRequest.postId,
                "userId" to currentUserId,
                "username" to username,
                "anonymousUsername" to anonymousUsername,
                "content" to commentRequest.content.trim(),
                "supportType" to commentRequest.supportType.name,
                "supportLevel" to commentRequest.supportLevel,
                "isAnonymous" to commentRequest.isAnonymous,
                "createdAt" to Timestamp.now(),
                "updatedAt" to Timestamp.now(),
                "likeCount" to 0,
                "isReported" to false,
                "isModerated" to false,
                "parentCommentId" to commentRequest.parentCommentId,
                "replyCount" to 0
            )

            // Add comment to firestore
            val commentRef = firestore.collection(COLLECTION_COMMENTS)
                .add(commentData)
                .await()

            Timber.d("✅ Comment posted successfully: ${commentRef.id}")

            // Update post's comment count (optional)
            updatePostCommentCount(commentRequest.postId, 1)

            Result.success(commentRef.id)

        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to post comment")
            Result.failure(e)
        }
    }

    override suspend fun likeComment(commentId: String, userId: String): Result<Unit> {
        return try {
            Timber.d("👍 Toggling like for comment: $commentId")

            val likeDocId = "${commentId}_${userId}"
            val likeRef = firestore.collection(COLLECTION_COMMENT_LIKES).document(likeDocId)
            val commentRef = firestore.collection(COLLECTION_COMMENTS).document(commentId)

            firestore.runTransaction { transaction ->
                val likeDoc = transaction.get(likeRef)
                val commentDoc = transaction.get(commentRef)

                val currentLikeCount = commentDoc.getLong("likeCount")?.toInt() ?: 0

                if (likeDoc.exists()) {
                    // Unlike - remove like
                    transaction.delete(likeRef)
                    transaction.update(commentRef, "likeCount", maxOf(0, currentLikeCount - 1))
                    Timber.d("👎 Comment unliked")
                } else {
                    // Like - add like
                    val likeData = mapOf(
                        "commentId" to commentId,
                        "userId" to userId,
                        "createdAt" to Timestamp.now()
                    )
                    transaction.set(likeRef, likeData)
                    transaction.update(commentRef, "likeCount", currentLikeCount + 1)
                    Timber.d("👍 Comment liked")
                }
            }.await()

            Result.success(Unit)

        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to toggle comment like")
            Result.failure(e)
        }
    }

    override suspend fun reportComment(commentId: String, userId: String, reason: String): Result<Unit> {
        return try {
            Timber.d("🚨 Reporting comment: $commentId, reason: $reason")

            val reportData = mapOf(
                "commentId" to commentId,
                "reportedBy" to userId,
                "reason" to reason,
                "createdAt" to Timestamp.now(),
                "status" to "pending"
            )

            firestore.collection("comment_reports")
                .add(reportData)
                .await()

            Timber.d("✅ Comment reported successfully")
            Result.success(Unit)

        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to report comment")
            Result.failure(e)
        }
    }

    override suspend fun deleteComment(commentId: String, userId: String): Result<Unit> {
        return try {
            Timber.d("🗑️ Deleting comment: $commentId")

            // Check if user owns the comment
            val commentDoc = firestore.collection(COLLECTION_COMMENTS)
                .document(commentId)
                .get()
                .await()

            val commentUserId = commentDoc.getString("userId")
            if (commentUserId != userId) {
                return Result.failure(SecurityException("You can only delete your own comments"))
            }

            // Get postId before deletion for updating count
            val postId = commentDoc.getString("postId") ?: ""

            // Delete comment
            firestore.collection(COLLECTION_COMMENTS)
                .document(commentId)
                .delete()
                .await()

            // Update post comment count
            if (postId.isNotBlank()) {
                updatePostCommentCount(postId, -1)
            }

            Timber.d("✅ Comment deleted successfully")
            Result.success(Unit)

        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to delete comment")
            Result.failure(e)
        }
    }

    override suspend fun getCommentStats(postId: String): Result<Map<SupportType, Int>> {
        return try {
            Timber.d("📊 Getting comment stats for post: $postId")

            val comments = firestore.collection(COLLECTION_COMMENTS)
                .whereEqualTo("postId", postId)
                .get()
                .await()

            val stats = comments.documents.groupBy { doc ->
                val supportTypeStr = doc.getString("supportType") ?: "HEART"
                try {
                    SupportType.valueOf(supportTypeStr)
                } catch (e: Exception) {
                    SupportType.HEART
                }
            }.mapValues { it.value.size }

            Timber.d("✅ Comment stats retrieved: $stats")
            Result.success(stats)

        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to get comment stats")
            Result.failure(e)
        }
    }

    // ============================================================================
    // HELPER METHODS
    // ============================================================================

    private suspend fun updatePostCommentCount(postId: String, delta: Int) {
        try {
            val postRef = firestore.collection(Constants.COLLECTION_COMMUNITY_FEED)
                .document(postId)

            firestore.runTransaction { transaction ->
                val postDoc = transaction.get(postRef)
                val currentCount = postDoc.getLong("commentCount")?.toInt() ?: 0
                val newCount = maxOf(0, currentCount + delta)
                transaction.update(postRef, "commentCount", newCount)
            }.await()

        } catch (e: Exception) {
            Timber.w(e, "⚠️ Failed to update post comment count")
        }
    }

    private fun generateAnonymousUsername(originalUsername: String): String {
        val random = (1000..9999).random()
        return "${originalUsername}_$random"
    }

    private suspend fun getCurrentUserId(): String? {
        return try {
            authRepository.getCurrentUser()?.id
        } catch (e: Exception) {
            Timber.e(e, "❌ Error getting current user")
            null
        }
    }
}