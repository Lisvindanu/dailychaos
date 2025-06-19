// File: app/src/main/java/com/dailychaos/project/data/mapper/CommunityPostMapper.kt
package com.dailychaos.project.data.mapper

import android.os.Build
import androidx.annotation.RequiresApi
import com.dailychaos.project.domain.model.CommunityPost
import com.google.firebase.Timestamp
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale
import com.dailychaos.project.domain.model.ChaosEntry // Import ChaosEntry
import com.dailychaos.project.util.Constants // Import Constants for anonymous prefixes
import kotlin.random.Random // For generating random index for anonymous username

/**
 * Mapper untuk CommunityPost
 * "Mengubah CommunityPost dari format Firestore ke domain dan sebaliknya!"
 */

@RequiresApi(Build.VERSION_CODES.O)
fun Map<String, Any>.toCommunityPost(): CommunityPost {
    return CommunityPost(
        id = this["id"] as? String ?: "",
        // Assuming chaosEntryId is the ID of the original entry, but userId and username are for the community post
        chaosEntryId = this["chaosEntryId"] as? String ?: "",
        userId = this["userId"] as? String ?: "", // Add userId for reference/moderation
        username = this["username"] as? String ?: "", // Add username for display
        anonymousUsername = this["anonymousUsername"] as? String ?: "",
        title = this["title"] as? String ?: "",
        description = this["description"] as? String ?: this["content"] as? String ?: "",
        chaosLevel = (this["chaosLevel"] as? Long)?.toInt() ?: 5,
        tags = this["tags"] as? List<String> ?: emptyList(),
        supportCount = (this["supportCount"] as? Long)?.toInt() ?: 0,
        twinCount = (this["twinCount"] as? Long)?.toInt() ?: 0,
        createdAt = parseFirestoreDateTime(this["createdAt"]),
        isReported = this["isReported"] as? Boolean ?: false,
        isModerated = this["isModerated"] as? Boolean ?: false,
        isAnonymous = this["isAnonymous"] as? Boolean ?: false // Ensure isAnonymous is mapped
    )
}

@RequiresApi(Build.VERSION_CODES.O)
fun CommunityPost.toFirestoreMap(): MutableMap<String, Any> {
    return mutableMapOf(
        "id" to this.id,
        "chaosEntryId" to this.chaosEntryId,
        "userId" to this.userId, // Store original userId for moderation/linking
        "username" to this.username, // Store username
        "anonymousUsername" to this.anonymousUsername,
        "title" to this.title,
        "description" to this.description,
        "content" to this.description, // Also store as content for compatibility
        "chaosLevel" to this.chaosLevel,
        "tags" to this.tags,
        "supportCount" to this.supportCount,
        "twinCount" to this.twinCount,
        "createdAt" to Timestamp(this.createdAt.toJavaInstant()),
        "isReported" to this.isReported,
        "isModerated" to this.isModerated,
        "isAnonymous" to this.isAnonymous // Store isAnonymous flag
    )
}

/**
 * Converts a ChaosEntry domain model to a CommunityPost domain model.
 * This is used when a user decides to share their personal chaos entry to the community.
 *
 * @param originalUsername The username of the original poster.
 */
@RequiresApi(Build.VERSION_CODES.O)
fun ChaosEntry.toCommunityPost(originalUsername: String): CommunityPost {
    val anonymousName = if (this.isSharedToCommunity) {
        val randomIndex = Random.nextInt(Constants.ANONYMOUS_PREFIXES.size)
        Constants.ANONYMOUS_PREFIXES[randomIndex] + "_" + Random.nextInt(1000, 9999)
    } else {
        originalUsername // If not shared anonymously, use their actual username
    }

    return CommunityPost(
        id = this.id, // Reusing ID from ChaosEntry, or could generate new one if needed
        chaosEntryId = this.id, // Reference to the original chaos entry
        userId = this.userId, // Keep original user ID for moderation purposes, but not publicly displayed if anonymous
        username = originalUsername, // Keep original username for internal use
        anonymousUsername = if (this.isSharedToCommunity) anonymousName else originalUsername, // Publicly displayed username
        title = this.title,
        description = this.description,
        chaosLevel = this.chaosLevel,
        tags = this.tags,
        supportCount = 0, // Fresh count for community post
        twinCount = 0, // Fresh count for community post
        createdAt = this.createdAt,
        isReported = false,
        isModerated = false,
        isAnonymous = this.isSharedToCommunity // Crucially set based on shareToCommunity
    )
}


/**
 * Parse Firestore datetime yang bisa berupa Timestamp atau String
 */
@RequiresApi(Build.VERSION_CODES.O)
private fun parseFirestoreDateTime(value: Any?): Instant {
    return when (value) {
        is Timestamp -> {
            // Jika Firestore mengembalikan Timestamp object
            value.toDate().toInstant().toKotlinInstant()
        }
        is String -> {
            // Jika Firestore mengembalikan String (seperti "2025-06-19 18:50:16")
            try {
                // Parse string datetime format: "yyyy-MM-dd HH:mm:ss"
                val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                formatter.parse(value)?.toInstant()?.toKotlinInstant() ?: Instant.DISTANT_PAST
            } catch (e: Exception) {
                Timber.w("Failed to parse datetime string: $value, using DISTANT_PAST")
                Instant.DISTANT_PAST
            }
        }
        is Long -> {
            // Jika Firestore mengembalikan timestamp sebagai Long (epoch seconds)
            try {
                Instant.fromEpochSeconds(value)
            } catch (e: Exception) {
                Timber.w("Failed to parse timestamp long: $value, using DISTANT_PAST")
                Instant.DISTANT_PAST
            }
        }
        else -> {
            Timber.w("Unknown datetime format: $value (${value?.javaClass?.simpleName}), using DISTANT_PAST")
            Instant.DISTANT_PAST
        }
    }
}