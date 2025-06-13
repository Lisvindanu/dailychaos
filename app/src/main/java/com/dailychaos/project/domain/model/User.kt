package com.dailychaos.project.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * User - Data pengguna
 */
@Serializable
data class User(
    val id: String = "",
    val email: String? = null,
    val anonymousUsername: String = "",
    val isAnonymous: Boolean = true,
    val chaosEntriesCount: Int = 0,
    val supportGivenCount: Int = 0,
    val supportReceivedCount: Int = 0,
    val streakDays: Int = 0,
    val joinedAt: Instant = Instant.DISTANT_PAST,
    val lastActiveAt: Instant = Instant.DISTANT_PAST,
    val preferences: UserPreferences = UserPreferences()
)

/**
 * User Preferences - Preferensi pengguna
 */
@Serializable
data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val notificationsEnabled: Boolean = true,
    val dailyReminderTime: String? = null, // Format: "HH:mm"
    val shareByDefault: Boolean = false,
    val showChaosLevel: Boolean = true,
    val konosubaQuotesEnabled: Boolean = true,
    val anonymousMode: Boolean = true
)

/**
 * Theme Mode
 */
enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}