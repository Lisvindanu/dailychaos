// File: app/src/main/java/com/dailychaos/project/data/mapper/CommunityPostMapper.kt
package com.dailychaos.project.data.mapper

import com.dailychaos.project.domain.model.CommunityPost
import com.google.firebase.Timestamp
import timber.log.Timber
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.datetime.toKotlinInstant

/**
 * Enhanced mapper untuk convert antara Firestore data dan CommunityPost domain model
 * Handles data dari existing chaos_entries yang di-share ke community_feed
 */

fun Map<String, Any>.toCommunityPost(): CommunityPost? {
    return try {
        Timber.d("🔄 Converting Firestore data to CommunityPost")
        Timber.d("📄 Raw data keys: ${this.keys}")
        Timber.d("📄 Raw data sample: ${this.entries.take(5).associate { it.key to it.value }}")

        // Extract required fields dengan multiple fallbacks
        val id = (this["id"] as? String)
            ?: (this["entryId"] as? String)
            ?: run {
                Timber.e("❌ Missing required field: id/entryId")
                return null
            }

        val userId = (this["userId"] as? String) ?: run {
            Timber.e("❌ Missing required field: userId")
            return null
        }

        val title = (this["title"] as? String) ?: run {
            Timber.e("❌ Missing required field: title")
            return null
        }

        // Handle different content field names yang bisa ada dari chaos_entries
        val description = (this["content"] as? String)
            ?: (this["description"] as? String)
            ?: run {
                Timber.e("❌ Missing required field: content/description")
                return null
            }

        val chaosLevel = (this["chaosLevel"] as? Long)?.toInt()
            ?: (this["chaosLevel"] as? Int)
            ?: run {
                Timber.e("❌ Missing or invalid chaosLevel")
                return null
            }

        // Handle timestamp conversion (from Firestore Timestamp to kotlinx Instant)
        val createdAt = when (val timestamp = this["createdAt"]) {
            is Timestamp -> {
                Instant.ofEpochSecond(timestamp.seconds, timestamp.nanoseconds.toLong()).toKotlinInstant()
            }
            is Long -> {
                Instant.ofEpochMilli(timestamp).toKotlinInstant()
            }
            else -> {
                Timber.w("⚠️ Invalid or missing createdAt timestamp, using current time")
                kotlinx.datetime.Clock.System.now()
            }
        }

        // Extract optional fields dengan smart defaults
        val username = (this["username"] as? String) ?: "Anonymous"
        val anonymousUsername = (this["anonymousUsername"] as? String) ?: generateAnonymousUsername()
        val isAnonymous = (this["isAnonymous"] as? Boolean) ?: true
        val supportCount = (this["supportCount"] as? Long)?.toInt() ?: 0
        val viewCount = (this["viewCount"] as? Long)?.toInt() ?: 0

        // Handle arrays - bisa dari chaos_entries original
        val tags = (this["tags"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
        val miniWins = (this["miniWins"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()

        val mood = (this["mood"] as? String) ?: "unknown"

        // Handle shareToFeed flag dari chaos_entries
        val shareToFeed = (this["shareToFeed"] as? Boolean) ?: true

        val communityPost = CommunityPost(
            id = id,
            chaosEntryId = id, // Use same ID for linking back to original entry
            userId = userId,
            username = username,
            anonymousUsername = anonymousUsername,
            title = title,
            description = description,
            chaosLevel = chaosLevel,
            miniWins = miniWins,
            tags = tags,
            supportCount = supportCount,
            twinCount = 0, // Calculate later if needed
            createdAt = createdAt,
            isReported = false,
            isModerated = false,
            isAnonymous = isAnonymous
        )

        Timber.d("✅ Successfully converted to CommunityPost: ${communityPost.id}")
        Timber.d("   - Title: ${communityPost.title}")
        Timber.d("   - User: ${communityPost.username} (anonymous: ${communityPost.isAnonymous})")
        Timber.d("   - Chaos Level: ${communityPost.chaosLevel}")
        Timber.d("   - Support Count: ${communityPost.supportCount}")
        Timber.d("   - Tags: ${communityPost.tags}")
        Timber.d("   - Mini Wins: ${communityPost.miniWins}")

        communityPost

    } catch (e: Exception) {
        Timber.e(e, "❌ Error converting Firestore data to CommunityPost")
        Timber.e("   Raw data: $this")
        null
    }
}

fun CommunityPost.toFirestoreMap(): Map<String, Any> {
    Timber.d("🔄 Converting CommunityPost to Firestore map: $id")

    val map = mutableMapOf<String, Any>(
        "id" to id,
        "chaosEntryId" to chaosEntryId,
        "userId" to userId,
        "username" to username,
        "anonymousUsername" to anonymousUsername,
        "title" to title,
        "content" to description, // Map description to content for Firestore
        "description" to description, // Keep both for compatibility
        "chaosLevel" to chaosLevel,
        "tags" to tags,
        "miniWins" to miniWins,
        "isAnonymous" to isAnonymous,
        "createdAt" to Timestamp.now(), // Always use server timestamp when writing
        "supportCount" to supportCount,
        "twinCount" to twinCount,
        "isReported" to isReported,
        "isModerated" to isModerated
    )

    Timber.d("✅ Firestore map created for ${id}")
    Timber.d("   - Map keys: ${map.keys}")

    return map
}

private fun generateAnonymousUsername(): String {
    val adjectives = listOf(
        "Brave", "Curious", "Adventurous", "Resilient", "Hopeful",
        "Strong", "Creative", "Wise", "Kind", "Bold", "Peaceful",
        "Determined", "Gentle", "Fierce", "Magical", "Mystical"
    )
    val nouns = listOf(
        "Explorer", "Warrior", "Dreamer", "Champion", "Wanderer",
        "Guardian", "Seeker", "Fighter", "Builder", "Healer",
        "Adventurer", "Mage", "Knight", "Scholar", "Artist"
    )

    return "${adjectives.random()} ${nouns.random()}"
}