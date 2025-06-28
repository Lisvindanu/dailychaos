// File: app/src/main/java/com/dailychaos/project/data/mapper/SupportCommentMapper.kt
package com.dailychaos.project.data.mapper

import com.dailychaos.project.domain.model.SupportComment
import com.dailychaos.project.domain.model.SupportType
import com.google.firebase.Timestamp
import timber.log.Timber
import kotlinx.datetime.Instant
import kotlinx.datetime.Clock

/**
 * Mapper untuk SupportComment dari/ke Firestore
 */

/**
 * Convert Firestore document data to SupportComment model
 */
fun Map<String, Any>.toSupportComment(documentId: String, currentUserId: String? = null): SupportComment? {
    return try {
        SupportComment(
            id = documentId,
            postId = this["postId"] as? String ?: "",
            userId = this["userId"] as? String ?: "",
            username = this["username"] as? String ?: "",
            anonymousUsername = this["anonymousUsername"] as? String ?: "",
            content = this["content"] as? String ?: "",
            supportType = try {
                SupportType.valueOf(this["supportType"] as? String ?: "HEART")
            } catch (e: Exception) {
                SupportType.HEART
            },
            supportLevel = (this["supportLevel"] as? Long)?.toInt() ?: 1,
            isAnonymous = this["isAnonymous"] as? Boolean ?: true,
            createdAt = (this["createdAt"] as? Timestamp)?.toKotlinInstant() ?: Clock.System.now(),
            updatedAt = (this["updatedAt"] as? Timestamp)?.toKotlinInstant() ?: Clock.System.now(),
            likeCount = (this["likeCount"] as? Long)?.toInt() ?: 0,
            isLikedByCurrentUser = false, // Will be set separately
            isReported = this["isReported"] as? Boolean ?: false,
            isModerated = this["isModerated"] as? Boolean ?: false,
            parentCommentId = this["parentCommentId"] as? String,
            replyCount = (this["replyCount"] as? Long)?.toInt() ?: 0
        )
    } catch (e: Exception) {
        Timber.e(e, "❌ Error mapping SupportComment from Firestore data")
        null
    }
}

/**
 * Convert SupportComment to Firestore map
 */
fun SupportComment.toFirestoreMap(): Map<String, Any?> {
    return mapOf(
        "postId" to postId,
        "userId" to userId,
        "username" to username,
        "anonymousUsername" to anonymousUsername,
        "content" to content,
        "supportType" to supportType.name,
        "supportLevel" to supportLevel,
        "isAnonymous" to isAnonymous,
        "createdAt" to createdAt.toFirebaseTimestamp(),
        "updatedAt" to updatedAt.toFirebaseTimestamp(),
        "likeCount" to likeCount,
        "isReported" to isReported,
        "isModerated" to isModerated,
        "parentCommentId" to parentCommentId,
        "replyCount" to replyCount
    ).filterValues { it != null }
}

/**
 * Extension functions untuk conversion
 * ✅ FIXED: Proper parameter names for Kotlin DateTime API
 */
private fun Timestamp.toKotlinInstant(): Instant {
    return Instant.fromEpochSeconds(
        epochSeconds = seconds, // ✅ FIXED: Use correct parameter name
        nanosecondAdjustment = nanoseconds
    )
}

private fun Instant.toFirebaseTimestamp(): Timestamp {
    return Timestamp(epochSeconds, nanosecondsOfSecond)
}