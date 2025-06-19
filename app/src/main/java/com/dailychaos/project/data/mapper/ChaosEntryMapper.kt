// File: app/src/main/java/com/dailychaos/project/data/mapper/ChaosEntryMapper.kt
package com.dailychaos.project.data.mapper

import android.os.Build
import androidx.annotation.RequiresApi
import com.dailychaos.project.data.remote.dto.request.ChaosEntryRequest
import com.dailychaos.project.domain.model.ChaosEntry
import com.dailychaos.project.domain.model.SyncStatus
import com.google.firebase.Timestamp
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant

/**
 * Mapper untuk ChaosEntry
 * "Mengubah ChaosEntry dari format domain ke Firebase dan sebaliknya!"
 */
@RequiresApi(Build.VERSION_CODES.O)
fun Map<String, Any>.toChaosEntry(): ChaosEntry {
    return ChaosEntry(
        id = this["id"] as? String ?: "",
        userId = this["userId"] as? String ?: "",
        title = this["title"] as? String ?: "",
        description = this["content"] as? String ?: "", // Match "content" from Firestore
        chaosLevel = (this["chaosLevel"] as? Long)?.toInt() ?: 5, // Firestore reads Int as Long
        miniWins = this["miniWins"] as? List<String> ?: emptyList(),
        tags = this["tags"] as? List<String> ?: emptyList(),
        createdAt = (this["createdAt"] as? Timestamp)?.toDate()?.toInstant()?.toKotlinInstant() ?: Instant.DISTANT_PAST,
        updatedAt = (this["updatedAt"] as? Timestamp)?.toDate()?.toInstant()?.toKotlinInstant() ?: Instant.DISTANT_PAST,
        isSharedToCommunity = this["shareToFeed"] as? Boolean ?: false, // Match "shareToFeed" from Firestore
        syncStatus = SyncStatus.SYNCED, // As it's from Firebase, assume synced
        localId = 0L // Not applicable for remote mapping
    )
}

@RequiresApi(Build.VERSION_CODES.O)
fun ChaosEntry.toFirestoreMap(): Map<String, Any> {
    return hashMapOf(
        "id" to this.id,
        "userId" to this.userId,
        "title" to this.title,
        "content" to this.description, // Match "content" for Firestore
        "chaosLevel" to this.chaosLevel,
        "miniWins" to this.miniWins,
        "tags" to this.tags,
        "createdAt" to Timestamp(this.createdAt.toJavaInstant()),
        "updatedAt" to Timestamp(this.updatedAt.toJavaInstant()),
        "isSharedToCommunity" to this.isSharedToCommunity,
        "shareToFeed" to this.isSharedToCommunity // Match "shareToFeed" for Firestore
    )
}

// Opsional: Mapper dari domain ke request DTO jika diperlukan oleh FirestoreService
fun ChaosEntry.toChaosEntryRequest(): ChaosEntryRequest {
    return ChaosEntryRequest(
        title = this.title,
        content = this.description,
        chaosLevel = this.chaosLevel,
        mood = "unknown", // Default atau dapat disesuaikan
        tags = this.tags,
        isAnonymous = false, // Default atau dapat disesuaikan
        shareToFeed = this.isSharedToCommunity,
        miniWins = this.miniWins
    )
}