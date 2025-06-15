// File: app/src/main/java/com/dailychaos/project/data/repository/AuthRepositoryImpl.kt
package com.dailychaos.project.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.dailychaos.project.data.remote.firebase.FirebaseAuthService
import com.dailychaos.project.domain.model.AuthState
import com.dailychaos.project.domain.model.User
import com.dailychaos.project.domain.model.UserSettings
import com.dailychaos.project.domain.model.ThemeMode
import com.dailychaos.project.domain.model.UsernameValidation
import com.dailychaos.project.domain.repository.AuthRepository
import com.dailychaos.project.util.ValidationUtil
import com.dailychaos.project.util.isValidUsernameFormat
import com.dailychaos.project.util.generateUsernameSuggestions
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
    private val firebaseAuthService: FirebaseAuthService,
    private val validationUtil: ValidationUtil
) : AuthRepository {

    @RequiresApi(Build.VERSION_CODES.O)
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

    @RequiresApi(Build.VERSION_CODES.O)
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

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun loginWithEmail(email: String, password: String): Result<User> {
        return try {
            val firebaseResult = firebaseAuthService.loginWithEmail(email, password)
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

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun registerWithUsername(username: String, displayName: String): Result<User> {
        return try {
            val firebaseResult = firebaseAuthService.registerWithUsername(username, displayName)
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

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun registerWithEmail(email: String, password: String, displayName: String): Result<User> {
        return try {
            val firebaseResult = firebaseAuthService.registerWithEmail(email, password, displayName)
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

    @RequiresApi(Build.VERSION_CODES.O)
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
            // Create a basic validation result first
            val basicValidation = when {
                username.isBlank() -> ValidationResult(false, "Username tidak boleh kosong!")
                username.length < 3 -> ValidationResult(false, "Username minimal 3 karakter!")
                username.length > 20 -> ValidationResult(false, "Username maksimal 20 karakter!")
                !username.matches(Regex("^[a-zA-Z0-9_]+$")) -> ValidationResult(false, "Username hanya boleh huruf, angka, dan underscore!")
                username.startsWith("_") || username.endsWith("_") -> ValidationResult(false, "Username tidak boleh dimulai atau diakhiri dengan underscore!")
                else -> ValidationResult(true, null)
            }

            if (basicValidation.isValid) {
                // Username format is valid, return success with proper domain model
                UsernameValidation(
                    isValid = true,
                    message = "Username valid!",
                    suggestions = emptyList()
                )
            } else {
                // Username has errors, get suggestions if format is invalid
                val suggestions = if (!username.isValidUsernameFormat()) {
                    username.generateUsernameSuggestions()
                } else {
                    emptyList()
                }

                // Return with proper domain model
                UsernameValidation(
                    isValid = false,
                    message = basicValidation.errorMessage ?: "Username tidak valid",
                    suggestions = suggestions
                )
            }
        } catch (e: Exception) {
            // Error case with proper domain model
            UsernameValidation(
                isValid = false,
                message = "Error validating username: ${e.message}",
                suggestions = emptyList()
            )
        }
    }

    override fun isAuthenticated(): Boolean {
        return firebaseAuthService.isAuthenticated()
    }

    /**
     * Map Firebase profile data to domain User model
     * FIX: Using correct property names that match User data class
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun mapFirebaseProfileToUser(profile: Map<String, Any>): User {
        return User(
            id = profile["userId"] as? String ?: "",
            email = profile["email"] as? String,
            anonymousUsername = profile["username"] as? String ?: "",
            isAnonymous = profile["authType"] as? String == "username",
            chaosEntriesCount = when (val count = profile["chaosEntries"]) {
                is Long -> count.toInt()
                is Int -> count
                is Double -> count.toInt()
                is String -> count.toIntOrNull() ?: 0
                else -> 0
            },
            supportGivenCount = when (val count = profile["supportGiven"]) {
                is Long -> count.toInt()
                is Int -> count
                is Double -> count.toInt()
                is String -> count.toIntOrNull() ?: 0
                else -> 0
            },
            supportReceivedCount = when (val count = profile["supportReceived"]) {
                is Long -> count.toInt()
                is Int -> count
                is Double -> count.toInt()
                is String -> count.toIntOrNull() ?: 0
                else -> 0
            },
            streakDays = when (val streak = profile["dayStreak"]) {
                is Long -> streak.toInt()
                is Int -> streak
                is Double -> streak.toInt()
                is String -> streak.toIntOrNull() ?: 0
                else -> 0
            },
            joinedAt = (profile["joinDate"] as? String)?.let { dateString ->
                try {
                    // Convert Firebase date string to Kotlin Instant
                    val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    java.time.LocalDateTime.parse(dateString, formatter)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toInstant()
                        .toKotlinInstant()
                } catch (e: Exception) {
                    kotlinx.datetime.Clock.System.now()
                }
            } ?: kotlinx.datetime.Clock.System.now(),
            lastActiveAt = (profile["lastLoginDate"] as? String)?.let { dateString ->
                try {
                    val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    java.time.LocalDateTime.parse(dateString, formatter)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toInstant()
                        .toKotlinInstant()
                } catch (e: Exception) {
                    kotlinx.datetime.Clock.System.now()
                }
            } ?: kotlinx.datetime.Clock.System.now(),
            settings = UserSettings(
                themeMode = ThemeMode.SYSTEM,
                notificationsEnabled = true,
                dailyReminderTime = null,
                shareByDefault = false,
                showChaosLevel = true,
                konosubaQuotesEnabled = true,
                anonymousMode = profile["authType"] as? String == "username"
            )
        )
    }

    /**
     * Simple validation result for internal use
     */
    private data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    )
}