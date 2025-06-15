// File: app/src/main/java/com/dailychaos/project/domain/model/UserProfile.kt
package com.dailychaos.project.domain.model

/**
 * User Profile Data Model
 *
 * "Model untuk profile adventurer - lengkap dengan stats dan achievement!"
 */
data class UserProfile(
    val userId: String = "",
    val username: String? = null,
    val displayName: String = "",
    val email: String? = null,
    val chaosEntries: Int = 0,
    val dayStreak: Int = 0,
    val supportGiven: Int = 0,
    val joinDate: String = "",
    val authType: String = "username", // "username", "email", "anonymous"
    val profilePicture: String? = null,
    val bio: String = "",
    val chaosLevel: Int = 1,
    val partyRole: String = "Newbie Adventurer",
    val isActive: Boolean = true,
    val lastLoginDate: String? = null,
    val achievements: List<String> = emptyList(),
    val preferences: UserProfilePreferences = UserProfilePreferences()
)

/**
 * User Profile Preferences
 */
data class UserProfilePreferences(
    val shareByDefault: Boolean = false,
    val showChaosLevel: Boolean = true,
    val enableNotifications: Boolean = true,
    val publicProfile: Boolean = true,
    val showEmail: Boolean = false
)