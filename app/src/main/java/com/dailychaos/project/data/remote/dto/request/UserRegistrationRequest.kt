
// File: app/src/main/java/com/dailychaos/project/data/remote/dto/request/UserRegistrationRequest.kt
package com.dailychaos.project.data.remote.dto.request

import kotlinx.serialization.Serializable

/**
 * User Registration Request DTO
 * "Data untuk daftar party baru - welcome to the chaos!"
 */
@Serializable
data class UserRegistrationRequest(
    val username: String,
    val displayName: String,
    val email: String? = null,
    val authType: String, // "username" or "email"
    val deviceInfo: DeviceInfo? = null,
    val preferences: UserPreferencesRequest? = null
)

@Serializable
data class DeviceInfo(
    val deviceId: String,
    val platform: String, // "android", "ios"
    val appVersion: String,
    val osVersion: String
)

@Serializable
data class UserPreferencesRequest(
    val theme: String = "system",
    val notificationsEnabled: Boolean = true,
    val konoSubaQuotesEnabled: Boolean = true,
    val anonymousMode: Boolean = false,
    val shareByDefault: Boolean = false,
    val showChaosLevel: Boolean = true,
    val reminderTime: String = "20:00",
    val language: String = "id"
)