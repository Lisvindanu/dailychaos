// File: app/src/main/java/com/dailychaos/project/data/repository/AuthRepositoryImpl.kt
package com.dailychaos.project.data.repository

import com.dailychaos.project.data.remote.firebase.FirebaseAuthService
import com.dailychaos.project.domain.model.AuthState
import com.dailychaos.project.domain.model.User
import com.dailychaos.project.domain.model.UserSettings
import com.dailychaos.project.domain.model.UsernameValidation
import com.dailychaos.project.domain.repository.AuthRepository
import com.dailychaos.project.util.ValidationUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Authentication Repository Implementation
 * "Where the party registration magic happens!"
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuthService: FirebaseAuthService
) : AuthRepository {

    override fun getAuthState(): Flow<AuthState> = flow {
        emit(AuthState.Loading)

        try {
            if (firebaseAuthService.isAuthenticated()) {
                val user = getCurrentUser()
                if (user != null) {
                    emit(AuthState.Authenticated(user))
                } else {
                    emit(AuthState.Unauthenticated)
                }
            } else {
                emit(AuthState.Unauthenticated)
            }
        } catch (e: Exception) {
            emit(AuthState.Error(e.message ?: "Authentication error"))
        }
    }

    override suspend fun loginWithUsername(username: String): Result<User> {
        return try {
            val firebaseResult = firebaseAuthService.loginWithUsername(username)
            if (firebaseResult.isSuccess) {
                val firebaseUser = firebaseResult.getOrThrow()
                val profileResult = firebaseAuthService.getUserProfile(firebaseUser.uid)

                if (profileResult.isSuccess) {
                    val profile = profileResult.getOrThrow()
                    val user = mapFirebaseProfileToUser(profile)
                    Result.success(user)
                } else {
                    Result.failure(profileResult.exceptionOrNull()!!)
                }
            } else {
                Result.failure(firebaseResult.exceptionOrNull()!!)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return firebaseAuthService.logout()
    }

    override suspend fun getCurrentUser(): User? {
        return try {
            val firebaseUser = firebaseAuthService.currentUser
            if (firebaseUser != null) {
                val profileResult = firebaseAuthService.getUserProfile(firebaseUser.uid)
                if (profileResult.isSuccess) {
                    mapFirebaseProfileToUser(profileResult.getOrThrow())
                } else null
            } else null
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun updateUserProfile(updates: Map<String, Any>): Result<Unit> {
        return firebaseAuthService.updateUserProfile(updates)
    }

    override suspend fun deleteAccount(): Result<Unit> {
        return firebaseAuthService.deleteAccount()
    }

    override suspend fun validateUsername(username: String): UsernameValidation {
        return try {
            val errorMessage = ValidationUtil.getUsernameErrorMessage(username)
            if (errorMessage == null) {
                UsernameValidation(true, "Username valid!")
            } else {
                UsernameValidation(
                    false,
                    errorMessage,
                    if (!ValidationUtil.isValidUsernameFormat(username)) {
                        ValidationUtil.generateUsernameSuggestions(username)
                    } else emptyList()
                )
            }
        } catch (e: Exception) {
            UsernameValidation(false, "Error validating username: ${e.message}")
        }
    }

    override fun isAuthenticated(): Boolean {
        return firebaseAuthService.isAuthenticated()
    }

    /**
     * Map Firebase profile data to domain User model
     */
    private fun mapFirebaseProfileToUser(profile: Map<String, Any>): User {
        return User(
            id = profile["uid"] as? String ?: "",
            email = profile["email"] as? String,
            anonymousUsername = profile["username"] as? String ?: "",
            isAnonymous = profile["isAnonymous"] as? Boolean ?: true,
            chaosEntriesCount = (profile["chaosEntriesCount"] as? Long)?.toInt() ?: 0,
            supportGivenCount = (profile["totalSupportGiven"] as? Long)?.toInt() ?: 0,
            supportReceivedCount = (profile["totalSupportReceived"] as? Long)?.toInt() ?: 0,
            streakDays = (profile["streakDays"] as? Long)?.toInt() ?: 0,
            joinedAt = (profile["createdAt"] as? com.google.firebase.Timestamp)?.toDate()?.toInstant()?.toKotlinInstant()
                ?: Instant.DISTANT_PAST,
            lastActiveAt = (profile["lastActiveAt"] as? com.google.firebase.Timestamp)?.toDate()?.toInstant()?.toKotlinInstant()
                ?: Instant.DISTANT_PAST,
            settings = mapToUserSettings(profile["settings"] as? Map<String, Any> ?: emptyMap())
        )
    }

    /**
     * Map Firebase settings to UserSettings model
     */
    private fun mapToUserSettings(settingsMap: Map<String, Any>): UserSettings {
        return UserSettings(
            themeMode = com.dailychaos.project.domain.model.ThemeMode.valueOf(
                settingsMap["theme"] as? String ?: "SYSTEM"
            ),
            notificationsEnabled = settingsMap["notificationsEnabled"] as? Boolean ?: true,
            dailyReminderTime = settingsMap["reminderTime"] as? String ?: "20:00",
            shareByDefault = settingsMap["shareByDefault"] as? Boolean ?: false,
            showChaosLevel = settingsMap["showChaosLevel"] as? Boolean ?: true,
            konosubaQuotesEnabled = settingsMap["konoSubaQuotesEnabled"] as? Boolean ?: true,
            anonymousMode = settingsMap["anonymousMode"] as? Boolean ?: true
        )
    }
}
