// File: app/src/main/java/com/dailychaos/project/preferences/UserPreferences.kt
package com.dailychaos.project.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.dailychaos.project.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.catch
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * User Preferences Manager menggunakan DataStore
 *
 * "Seperti inventory Kazuma - tempat menyimpan semua setting penting"
 */
@Singleton
class UserPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    // Preference Keys
    companion object {
        private val FIRST_LAUNCH = booleanPreferencesKey("first_launch")
        private val THEME_MODE = stringPreferencesKey("theme_mode")
        private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val DAILY_REMINDER_TIME = stringPreferencesKey("daily_reminder_time")
        private val SHARE_BY_DEFAULT = booleanPreferencesKey("share_by_default")
        private val SHOW_CHAOS_LEVEL = booleanPreferencesKey("show_chaos_level")
        private val KONOSUBA_QUOTES_ENABLED = booleanPreferencesKey("konosuba_quotes_enabled")
        private val ANONYMOUS_MODE = booleanPreferencesKey("anonymous_mode")
        private val ANONYMOUS_USERNAME = stringPreferencesKey("anonymous_username")
        private val USER_ID = stringPreferencesKey("user_id")
        private val LAST_SYNC_TIME = longPreferencesKey("last_sync_time")
        private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")

        // Additional keys for registration
        private val USER_EMAIL = stringPreferencesKey("user_email")
        private val DISPLAY_NAME = stringPreferencesKey("display_name")
        private val AUTH_TYPE = stringPreferencesKey("auth_type")
        private val CHAOS_LEVEL = intPreferencesKey("chaos_level")
        private val PARTY_ROLE = stringPreferencesKey("party_role")
        private val USERNAME = stringPreferencesKey("username") // ADDED untuk username registration
    }

    // ================================
    // READ PREFERENCES
    // ================================

    /**
     * Check if this is first launch
     */
    val isFirstLaunch: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[FIRST_LAUNCH] ?: true
        }

    /**
     * Get theme mode
     */
    val themeMode: Flow<ThemeMode> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val themeName = preferences[THEME_MODE] ?: "SYSTEM"
            try {
                ThemeMode.valueOf(themeName)
            } catch (e: IllegalArgumentException) {
                ThemeMode.SYSTEM
            }
        }

    /**
     * Check if notifications are enabled
     */
    val notificationsEnabled: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[NOTIFICATIONS_ENABLED] ?: true
        }

    /**
     * Get daily reminder time
     */
    val dailyReminderTime: Flow<String?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[DAILY_REMINDER_TIME]
        }

    /**
     * Check if share by default is enabled
     */
    val shareByDefault: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[SHARE_BY_DEFAULT] ?: false
        }

    /**
     * Check if show chaos level is enabled
     */
    val showChaosLevel: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[SHOW_CHAOS_LEVEL] ?: true
        }

    /**
     * Check if KonoSuba quotes are enabled
     */
    val konosubaQuotesEnabled: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KONOSUBA_QUOTES_ENABLED] ?: true
        }

    /**
     * Check if anonymous mode is enabled
     */
    val anonymousMode: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[ANONYMOUS_MODE] ?: true
        }

    /**
     * Get anonymous username
     */
    val anonymousUsername: Flow<String?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[ANONYMOUS_USERNAME]
        }

    /**
     * Get user ID
     */
    val userId: Flow<String?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[USER_ID]
        }

    /**
     * Get user email
     */
    val userEmail: Flow<String?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[USER_EMAIL]
        }

    /**
     * Get display name
     */
    val displayName: Flow<String?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[DISPLAY_NAME]
        }

    /**
     * Get username (ADDED untuk username registration)
     */
    val username: Flow<String?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[USERNAME]
        }

    /**
     * Get authentication type
     */
    val authType: Flow<String?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[AUTH_TYPE]
        }

    /**
     * Get chaos level
     */
    val chaosLevel: Flow<Int> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[CHAOS_LEVEL] ?: 1
        }

    /**
     * Get party role
     */
    val partyRole: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PARTY_ROLE] ?: "Newbie Adventurer"
        }

    /**
     * Get last sync time
     */
    val lastSyncTime: Flow<Long> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[LAST_SYNC_TIME] ?: 0L
        }

    /**
     * Check if onboarding is completed
     */
    val onboardingCompleted: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[ONBOARDING_COMPLETED] ?: false
        }

    /**
     * Check if user profile is complete
     */
    val isProfileComplete: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val hasUserId = !preferences[USER_ID].isNullOrBlank()
            val hasDisplayName = !preferences[DISPLAY_NAME].isNullOrBlank()
            val hasAuthType = !preferences[AUTH_TYPE].isNullOrBlank()

            hasUserId && hasDisplayName && hasAuthType
        }

    // ================================
    // WRITE PREFERENCES
    // ================================

    /**
     * Set first launch completed
     */
    suspend fun setFirstLaunchCompleted() {
        dataStore.edit { preferences ->
            preferences[FIRST_LAUNCH] = false
        }
    }

    /**
     * Set theme mode
     */
    suspend fun setThemeMode(themeMode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE] = themeMode.name
        }
    }

    /**
     * Set notifications enabled
     */
    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED] = enabled
        }
    }

    /**
     * Set daily reminder time
     */
    suspend fun setDailyReminderTime(time: String?) {
        dataStore.edit { preferences ->
            if (time != null) {
                preferences[DAILY_REMINDER_TIME] = time
            } else {
                preferences.remove(DAILY_REMINDER_TIME)
            }
        }
    }

    /**
     * Set share by default
     */
    suspend fun setShareByDefault(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SHARE_BY_DEFAULT] = enabled
        }
    }

    /**
     * Set show chaos level
     */
    suspend fun setShowChaosLevel(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SHOW_CHAOS_LEVEL] = enabled
        }
    }

    /**
     * Set KonoSuba quotes enabled
     */
    suspend fun setKonosubaQuotesEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KONOSUBA_QUOTES_ENABLED] = enabled
        }
    }

    /**
     * Set anonymous mode
     */
    suspend fun setAnonymousMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[ANONYMOUS_MODE] = enabled
        }
    }

    /**
     * Set anonymous username
     */
    suspend fun setAnonymousUsername(username: String) {
        dataStore.edit { preferences ->
            preferences[ANONYMOUS_USERNAME] = username
        }
    }

    /**
     * Set user ID
     */
    suspend fun setUserId(userId: String?) {
        dataStore.edit { preferences ->
            if (userId != null) {
                preferences[USER_ID] = userId
            } else {
                preferences.remove(USER_ID)
            }
        }
    }

    /**
     * Set user email
     */
    suspend fun setUserEmail(email: String) {
        dataStore.edit { preferences ->
            preferences[USER_EMAIL] = email
        }
    }

    /**
     * Set display name
     */
    suspend fun setDisplayName(displayName: String) {
        dataStore.edit { preferences ->
            preferences[DISPLAY_NAME] = displayName
        }
    }

    /**
     * Set username (ADDED untuk username registration)
     */
    suspend fun setUsername(username: String?) {
        dataStore.edit { preferences ->
            if (username != null) {
                preferences[USERNAME] = username
            } else {
                preferences.remove(USERNAME)
            }
        }
    }

    /**
     * Set authentication type (username, email, anonymous)
     */
    suspend fun setAuthType(authType: String) {
        dataStore.edit { preferences ->
            preferences[AUTH_TYPE] = authType
        }
    }

    /**
     * Set chaos level
     */
    suspend fun setChaosLevel(level: Int) {
        dataStore.edit { preferences ->
            preferences[CHAOS_LEVEL] = level
        }
    }

    /**
     * Set party role
     */
    suspend fun setPartyRole(role: String) {
        dataStore.edit { preferences ->
            preferences[PARTY_ROLE] = role
        }
    }

    /**
     * Update last sync time
     */
    suspend fun updateLastSyncTime(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[LAST_SYNC_TIME] = timestamp
        }
    }

    /**
     * Set onboarding completed
     */
    suspend fun setOnboardingCompleted() {
        dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = true
        }
    }

    // ================================
    // AUTHENTICATION HELPER METHODS (ADDED)
    // ================================

    /**
     * Save user data after successful authentication (ADDED)
     */
    suspend fun saveUserData(
        userId: String,
        username: String?,
        displayName: String,
        email: String? = null
    ) {
        dataStore.edit { preferences ->
            preferences[USER_ID] = userId
            if (username != null) {
                preferences[USERNAME] = username
            }
            preferences[DISPLAY_NAME] = displayName
            if (email != null) {
                preferences[USER_EMAIL] = email
            }
        }
    }

    /**
     * Update user profile data (ADDED)
     */
    suspend fun updateUserProfile(
        username: String?,
        displayName: String,
        email: String? = null
    ) {
        dataStore.edit { preferences ->
            if (username != null) {
                preferences[USERNAME] = username
            }
            preferences[DISPLAY_NAME] = displayName
            if (email != null) {
                preferences[USER_EMAIL] = email
            }
        }
    }

    // ================================
    // CLEAR PREFERENCES
    // ================================

    /**
     * Clear all preferences (logout)
     */
    suspend fun clearAllPreferences() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    /**
     * Clear user-specific preferences (keep app settings)
     */
    suspend fun clearUserPreferences() {
        dataStore.edit { preferences ->
            preferences.remove(USER_ID)
            preferences.remove(USER_EMAIL)
            preferences.remove(USERNAME) // ADDED
            preferences.remove(ANONYMOUS_USERNAME)
            preferences.remove(DISPLAY_NAME)
            preferences.remove(AUTH_TYPE)
            preferences.remove(CHAOS_LEVEL)
            preferences.remove(PARTY_ROLE)
            preferences.remove(LAST_SYNC_TIME)
            // Keep theme, notifications, and other app settings
        }
    }

    /**
     * Clear all user data (for logout)
     */
    suspend fun clearUserData() {
        clearUserPreferences()
    }
}