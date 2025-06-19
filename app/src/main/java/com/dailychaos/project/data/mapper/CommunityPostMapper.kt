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

/**
 * Mapper untuk CommunityPost
 * "Mengubah CommunityPost dari format Firestore ke domain dan sebaliknya!"
 */

@RequiresApi(Build.VERSION_CODES.O)
fun Map<String, Any>.toCommunityPost(): CommunityPost {
    return CommunityPost(
        id = this["id"] as? String ?: "",
        chaosEntryId = this["chaosEntryId"] as? String ?: "",
        anonymousUsername = this["anonymousUsername"] as? String ?: "",
        title = this["title"] as? String ?: "",
        description = this["description"] as? String ?: this["content"] as? String ?: "",
        chaosLevel = (this["chaosLevel"] as? Long)?.toInt() ?: 5,
        tags = this["tags"] as? List<String> ?: emptyList(),
        supportCount = (this["supportCount"] as? Long)?.toInt() ?: 0,
        twinCount = (this["twinCount"] as? Long)?.toInt() ?: 0,
        createdAt = parseFirestoreDateTime(this["createdAt"]),
        isReported = this["isReported"] as? Boolean ?: false,
        isModerated = this["isModerated"] as? Boolean ?: false
    )
}

@RequiresApi(Build.VERSION_CODES.O)
fun CommunityPost.toFirestoreMap(): MutableMap<String, Any> {
    return mutableMapOf(
        "id" to this.id,
        "chaosEntryId" to this.chaosEntryId,
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
        "isModerated" to this.isModerated
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