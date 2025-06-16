package com.dailychaos.project.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.Clock // Pastikan import Clock ada
import kotlinx.serialization.Serializable

/**
 * User - Data pengguna
 */
@Serializable
data class User(
    val id: String = "",
    val email: String? = null,
    val displayName: String = "", // <-- TAMBAHKAN BIDANG INI
    val anonymousUsername: String = "",
    val isAnonymous: Boolean = true,
    val chaosEntriesCount: Int = 0,
    val supportGivenCount: Int = 0,
    val supportReceivedCount: Int = 0,
    val streakDays: Int = 0,
    val joinedAt: Instant = Clock.System.now(), // Gunakan Clock.System.now() sebagai default
    val lastActiveAt: Instant = Clock.System.now(),
    val settings: UserSettings = UserSettings()
)

/**
 * User Settings - Settings pengguna
 */
@Serializable
data class UserSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val notificationsEnabled: Boolean = true,
    val dailyReminderTime: String? = null,
    val shareByDefault: Boolean = false,
    val showChaosLevel: Boolean = true,
    val konosubaQuotesEnabled: Boolean = true,
    val anonymousMode: Boolean = true
)

/**
 * Login request data
 */
data class LoginRequest(
    val username: String
)