// File: app/src/main/java/com/dailychaos/project/data/remote/dto/response/FirebaseResponse.kt
package com.dailychaos.project.data.remote.dto.response

import kotlinx.serialization.Serializable

/**
 * Generic Firebase Response DTO
 * "Standard response format untuk semua Firebase operations"
 */
@Serializable
data class FirebaseResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val errorCode: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val requestId: String? = null
)

/**
 * User Profile Response
 */
@Serializable
data class UserProfileResponse(
    val userId: String,
    val username: String,
    val displayName: String,
    val email: String? = null,
    val authType: String,
    val profileImageUrl: String? = null,
    val bio: String? = null,
    val partyRole: String,
    val chaosLevel: Int,
    val totalEntries: Int,
    val totalSupportsGiven: Int,
    val totalSupportsReceived: Int,
    val achievements: List<AchievementResponse>,
    val streakDays: Int,
    val joinedAt: Long,
    val lastActiveAt: Long,
    val isActive: Boolean,
    val settings: UserSettingsResponse,
    val stats: UserStatsResponse
)

@Serializable
data class UserSettingsResponse(
    val theme: String,
    val notificationsEnabled: Boolean,
    val konoSubaQuotesEnabled: Boolean,
    val anonymousMode: Boolean,
    val shareByDefault: Boolean,
    val showChaosLevel: Boolean,
    val reminderTime: String,
    val language: String,
    val privacyLevel: String
)

@Serializable
data class UserStatsResponse(
    val totalChaosEntries: Int,
    val averageChaosLevel: Double,
    val longestStreak: Int,
    val currentStreak: Int,
    val totalCommentsReceived: Int,
    val totalReactionsReceived: Int,
    val favoriteEmoji: String? = null,
    val mostActiveDay: String? = null,
    val chaosLevelDistribution: Map<String, Int> = emptyMap()
)

@Serializable
data class AchievementResponse(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val category: String,
    val unlockedAt: Long? = null,
    val progress: Int = 0,
    val maxProgress: Int = 100,
    val isUnlocked: Boolean = false,
    val rarity: String // "common", "rare", "epic", "legendary"
)