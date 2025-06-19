// Complete Fixed AuthRepositoryImpl.kt
package com.dailychaos.project.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.dailychaos.project.data.remote.firebase.FirebaseAuthService
import com.dailychaos.project.domain.model.AuthState
import com.dailychaos.project.domain.model.User
import com.dailychaos.project.domain.model.UserProfile
import com.dailychaos.project.domain.model.UserProfilePreferences
import com.dailychaos.project.domain.model.UsernameValidation
import com.dailychaos.project.domain.repository.AuthRepository
import com.dailychaos.project.preferences.UserPreferences
import com.dailychaos.project.util.ValidationUtil
import com.dailychaos.project.util.generateUsernameSuggestions
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuthService: FirebaseAuthService,
    private val validationUtil: ValidationUtil,
    private val userPreferences: UserPreferences
) : AuthRepository {

    @RequiresApi(Build.VERSION_CODES.O)
    private fun mapFirebaseProfileToUser(profile: Map<String, Any>): User {
        // Helper function for flexible date parsing
        fun parseDate(value: Any?): Instant {
            return when (value) {
                is Timestamp -> value.toDate().toInstant().toKotlinInstant()
                is String -> {
                    try {
                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(value)?.toInstant()?.toKotlinInstant() ?: Clock.System.now()
                    } catch (e: Exception) {
                        Clock.System.now()
                    }
                }
                else -> Clock.System.now()
            }
        }

        // Helper function for safe Long to Int conversion
        fun parseLongToInt(value: Any?): Int {
            return (value as? Long ?: 0L).toInt()
        }

        return User(
            id = profile["userId"] as? String ?: profile["uid"] as? String ?: "",
            email = profile["email"] as? String,
            displayName = profile["displayName"] as? String ?: "",
            anonymousUsername = profile["username"] as? String ?: "",
            isAnonymous = (profile["authType"] as? String) in listOf("anonymous", "username"),
            chaosEntriesCount = parseLongToInt(profile["chaosEntries"] ?: profile["chaosEntriesCount"]),
            supportGivenCount = parseLongToInt(profile["supportGiven"] ?: profile["totalSupportsGiven"]),
            supportReceivedCount = parseLongToInt(profile["supportReceived"] ?: profile["totalSupportsReceived"]),
            streakDays = parseLongToInt(profile["dayStreak"]),
            joinedAt = parseDate(profile["joinDate"] ?: profile["createdAt"]),
            lastActiveAt = parseDate(profile["lastLoginDate"] ?: profile["lastActiveAt"] ?: profile["lastLogin"])
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getAuthState(): Flow<AuthState> = flow {
        emit(AuthState.Loading)
        try {
            val firebaseUser = firebaseAuthService.currentUser
            if (firebaseUser != null) {
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
            Timber.e(e, "AuthRepositoryImpl: Error in getAuthState.")
            emit(AuthState.Error(e.message ?: "Authentication error"))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getCurrentUser(): User? {
        Timber.d("AuthRepositoryImpl: getCurrentUser called.")
        return try {
            val firebaseUser = firebaseAuthService.currentUser
            if (firebaseUser != null) {
                Timber.d("AuthRepositoryImpl: FirebaseUser found: ${firebaseUser.uid}")
                val profileResult = firebaseAuthService.getUserProfile(firebaseUser.uid)
                return if (profileResult.isSuccess) {
                    val user = mapFirebaseProfileToUser(profileResult.getOrThrow())
                    Timber.d("AuthRepositoryImpl: Mapped Firebase profile to User: ${user.id}")
                    user
                } else {
                    Timber.w("AuthRepositoryImpl: Failed to get user profile from Firestore for UID: ${firebaseUser.uid}. Clearing local data.")
                    userPreferences.clearUserData()
                    null
                }
            } else {
                Timber.d("AuthRepositoryImpl: No FirebaseUser found. Checking local preferences.")
                val storedAuthType = userPreferences.authType.first()
                val storedUserId = userPreferences.userId.first()

                if (storedAuthType in listOf("username", "anonymous") && !storedUserId.isNullOrBlank()) {
                    val profileResult = firebaseAuthService.getUserProfile(storedUserId)
                    return if (profileResult.isSuccess) {
                        val user = mapFirebaseProfileToUser(profileResult.getOrThrow())
                        Timber.d("AuthRepositoryImpl: Found user in preferences for $storedAuthType auth: ${user.id}")
                        user
                    } else {
                        Timber.w("AuthRepositoryImpl: Failed to get user profile from Firestore using stored ID: $storedUserId. Clearing local data.")
                        userPreferences.clearUserData()
                        null
                    }
                }
                Timber.d("AuthRepositoryImpl: No active user and no valid local preferences found.")
                return null
            }
        } catch (e: Exception) {
            Timber.e(e, "AuthRepositoryImpl: Error getting current user.")
            userPreferences.clearUserData()
            return null
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun loginWithUsername(username: String): Result<User> {
        Timber.d("AuthRepositoryImpl: Attempting to login with username: $username")
        return try {
            // FIXED: Minimal validation for login - let server handle the rest
            when {
                username.isBlank() -> {
                    Timber.e("AuthRepositoryImpl: Username is blank")
                    return Result.failure(Exception("Username tidak boleh kosong!"))
                }
                username.length < 3 -> {
                    Timber.e("AuthRepositoryImpl: Username too short: ${username.length}")
                    return Result.failure(Exception("Username minimal 3 karakter."))
                }
                username.length > 30 -> {
                    Timber.e("AuthRepositoryImpl: Username too long: ${username.length}")
                    return Result.failure(Exception("Username terlalu panjang."))
                }
            }

            Timber.d("AuthRepositoryImpl: Basic validation passed, proceeding with Firebase login")

            val firebaseUserResult = firebaseAuthService.loginWithUsername(username)

            if (firebaseUserResult.isSuccess) {
                val firebaseUser = firebaseUserResult.getOrThrow()
                Timber.d("AuthRepositoryImpl: Firebase user authenticated with UID: ${firebaseUser.uid}")

                val userProfileResult = firebaseAuthService.getUserProfile(firebaseUser.uid)

                if (userProfileResult.isSuccess) {
                    val userProfileData = userProfileResult.getOrThrow()
                    val user = mapFirebaseProfileToUser(userProfileData)
                    Timber.d("AuthRepositoryImpl: User profile mapped for user: ${user.displayName}")
                    Result.success(user)
                } else {
                    Timber.e("AuthRepositoryImpl: Failed to fetch user profile after username login: ${userProfileResult.exceptionOrNull()?.message}")
                    Result.failure(userProfileResult.exceptionOrNull() ?: Exception("Gagal memuat profil pengguna setelah login username."))
                }
            } else {
                Timber.e("AuthRepositoryImpl: Username login failed: ${firebaseUserResult.exceptionOrNull()?.message}")
                Result.failure(firebaseUserResult.exceptionOrNull()!!)
            }
        } catch (e: Exception) {
            Timber.e(e, "AuthRepositoryImpl: Unexpected error during username login.")
            Result.failure(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun loginWithEmail(email: String, password: String): Result<User> {
        Timber.d("AuthRepositoryImpl: Attempting to login with email: $email")
        return try {
            val emailValidation = validationUtil.isValidEmail(email)
            if (!emailValidation) {
                return Result.failure(IllegalArgumentException("Format email tidak valid!"))
            }
            val passwordValidation = validationUtil.validatePassword(password)
            if (!passwordValidation.isValid) {
                return Result.failure(IllegalArgumentException(passwordValidation.errorMessage ?: "Password tidak valid!"))
            }

            val firebaseResult = firebaseAuthService.loginWithEmail(email, password)
            if (firebaseResult.isSuccess) {
                val firebaseUser = firebaseResult.getOrThrow()
                Timber.d("AuthRepositoryImpl: Firebase user authenticated with UID: ${firebaseUser.uid} via email.")
                val profileResult = firebaseAuthService.getUserProfile(firebaseUser.uid)
                if (profileResult.isSuccess) {
                    val user = mapFirebaseProfileToUser(profileResult.getOrThrow())
                    Timber.d("AuthRepositoryImpl: User profile mapped for email user: ${user.displayName}")
                    Result.success(user)
                } else {
                    Timber.e("AuthRepositoryImpl: Failed to fetch user profile after email login: ${profileResult.exceptionOrNull()?.message}")
                    Result.failure(profileResult.exceptionOrNull() ?: Exception("Gagal memuat profil pengguna setelah login email."))
                }
            } else {
                Timber.e("AuthRepositoryImpl: Email login failed in firebaseAuthService: ${firebaseResult.exceptionOrNull()?.message}")
                Result.failure(firebaseResult.exceptionOrNull()!!)
            }
        } catch (e: Exception) {
            Timber.e(e, "AuthRepositoryImpl: Unexpected error during email login.")
            Result.failure(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun registerWithUsername(username: String, displayName: String): Result<User> {
        Timber.d("AuthRepositoryImpl: Attempting to register with username: $username, displayName: $displayName")
        return try {
            // FIXED: Minimal validation for registration - let FirebaseAuthService handle complex rules
            when {
                username.isBlank() -> {
                    return Result.failure(IllegalArgumentException("Username tidak boleh kosong!"))
                }
                username.length < 3 -> {
                    return Result.failure(IllegalArgumentException("Username minimal 3 karakter."))
                }
                displayName.isBlank() -> {
                    return Result.failure(IllegalArgumentException("Display name tidak boleh kosong."))
                }
            }

            val firebaseResult = firebaseAuthService.registerWithUsername(username, displayName)
            if (firebaseResult.isSuccess) {
                val firebaseUser = firebaseResult.getOrThrow()
                Timber.d("AuthRepositoryImpl: Firebase user registered with UID: ${firebaseUser.uid} via username.")
                val profileResult = firebaseAuthService.getUserProfile(firebaseUser.uid)
                if (profileResult.isSuccess) {
                    val user = mapFirebaseProfileToUser(profileResult.getOrThrow())
                    Timber.d("AuthRepositoryImpl: User profile mapped for new username user: ${user.displayName}")
                    Result.success(user)
                } else {
                    Timber.e("AuthRepositoryImpl: Failed to fetch user profile after username registration: ${profileResult.exceptionOrNull()?.message}")
                    Result.failure(profileResult.exceptionOrNull() ?: Exception("Gagal memuat profil pengguna setelah registrasi username."))
                }
            } else {
                Timber.e("AuthRepositoryImpl: Username registration failed in firebaseAuthService: ${firebaseResult.exceptionOrNull()?.message}")
                Result.failure(firebaseResult.exceptionOrNull()!!)
            }
        } catch (e: Exception) {
            Timber.e(e, "AuthRepositoryImpl: Unexpected error during username registration.")
            Result.failure(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun registerWithEmail(email: String, password: String, displayName: String): Result<User> {
        Timber.d("AuthRepositoryImpl: Attempting to register with email: $email, displayName: $displayName")
        return try {
            val emailValidation = validationUtil.isValidEmail(email)
            if (!emailValidation) {
                return Result.failure(IllegalArgumentException("Format email tidak valid!"))
            }
            val passwordValidation = validationUtil.validatePassword(password)
            if (!passwordValidation.isValid) {
                return Result.failure(IllegalArgumentException(passwordValidation.errorMessage ?: "Password tidak valid!"))
            }
            val displayNameValidation = validationUtil.validateDisplayName(displayName)
            if (!displayNameValidation.isValid) {
                return Result.failure(IllegalArgumentException(displayNameValidation.errorMessage ?: "Display name tidak valid!"))
            }

            val firebaseResult = firebaseAuthService.registerWithEmail(email, password, displayName)
            if (firebaseResult.isSuccess) {
                val firebaseUser = firebaseResult.getOrThrow()
                Timber.d("AuthRepositoryImpl: Firebase user registered with UID: ${firebaseUser.uid} via email.")
                val profileResult = firebaseAuthService.getUserProfile(firebaseUser.uid)
                if (profileResult.isSuccess) {
                    val user = mapFirebaseProfileToUser(profileResult.getOrThrow())
                    Timber.d("AuthRepositoryImpl: User profile mapped for new email user: ${user.displayName}")
                    Result.success(user)
                } else {
                    Timber.e("AuthRepositoryImpl: Failed to fetch user profile after email registration: ${profileResult.exceptionOrNull()?.message}")
                    Result.failure(profileResult.exceptionOrNull() ?: Exception("Gagal memuat profil pengguna setelah registrasi email."))
                }
            } else {
                Timber.e("AuthRepositoryImpl: Email registration failed in firebaseAuthService: ${firebaseResult.exceptionOrNull()?.message}")
                Result.failure(firebaseResult.exceptionOrNull()!!)
            }
        } catch (e: Exception) {
            Timber.e(e, "AuthRepositoryImpl: Unexpected error during email registration.")
            Result.failure(e)
        }
    }

    override suspend fun getUserProfile(userId: String?): Result<UserProfile> {
        Timber.d("AuthRepositoryImpl: getUserProfile called for userId: $userId")
        return try {
            val targetUserId = userId ?: firebaseAuthService.currentUser?.uid
            if (targetUserId == null) {
                return Result.failure(Exception("Tidak ada user yang login"))
            }

            val profileResult = firebaseAuthService.getUserProfile(targetUserId)
            if (profileResult.isSuccess) {
                val profileData = profileResult.getOrThrow()
                val userProfile = mapFirebaseProfileToUserProfile(profileData)
                Result.success(userProfile)
            } else {
                Result.failure(profileResult.exceptionOrNull() ?: Exception("Gagal mengambil profil pengguna"))
            }
        } catch (e: Exception) {
            Timber.e(e, "AuthRepositoryImpl: Error getting user profile")
            Result.failure(e)
        }
    }

    override suspend fun checkUsernameAvailability(username: String): Boolean {
        Timber.d("AuthRepositoryImpl: checkUsernameAvailability called for: $username")
        return try {
            firebaseAuthService.checkUsernameAvailability(username)
        } catch (e: Exception) {
            Timber.e(e, "AuthRepositoryImpl: Error checking username availability")
            false
        }
    }

    override fun generateRandomUsername(): String {
        Timber.d("AuthRepositoryImpl: generateRandomUsername called")
        return firebaseAuthService.generateRandomUsername()
    }


    // Helper function to map Firebase profile to UserProfile domain model
    private fun mapFirebaseProfileToUserProfile(profile: Map<String, Any>): UserProfile {
        fun parseLongToInt(value: Any?): Int {
            return (value as? Long ?: 0L).toInt()
        }

        val settings = profile["settings"] as? Map<String, Any> ?: emptyMap()

        return UserProfile(
            userId = profile["userId"] as? String ?: profile["uid"] as? String ?: "",
            username = profile["username"] as? String,
            displayName = profile["displayName"] as? String ?: "",
            email = profile["email"] as? String,
            chaosEntries = parseLongToInt(profile["chaosEntries"] ?: profile["chaosEntriesCount"]),
            dayStreak = parseLongToInt(profile["dayStreak"]),
            supportGiven = parseLongToInt(profile["supportGiven"] ?: profile["totalSupportsGiven"]),
            joinDate = profile["joinDate"] as? String ?: profile["createdAt"] as? String ?: "",
            authType = profile["authType"] as? String ?: "username",
            profilePicture = profile["profilePicture"] as? String,
            bio = profile["bio"] as? String ?: "",
            chaosLevel = parseLongToInt(profile["chaosLevel"]),
            partyRole = profile["partyRole"] as? String ?: "Newbie Adventurer",
            isActive = profile["isActive"] as? Boolean ?: true,
            lastLoginDate = profile["lastLoginDate"] as? String ?: profile["lastActiveAt"] as? String,
            achievements = (profile["achievements"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
            preferences = UserProfilePreferences(
                shareByDefault = settings["shareByDefault"] as? Boolean ?: false,
                showChaosLevel = settings["showChaosLevel"] as? Boolean ?: true,
                enableNotifications = settings["notificationsEnabled"] as? Boolean ?: true,
                publicProfile = true, // Default value since not in Firebase
                showEmail = false // Default value since not in Firebase
            )
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun registerAnonymous(username: String, displayName: String): Result<User> {
        Timber.d("AuthRepositoryImpl: Attempting to register anonymous user: $username, displayName: $displayName")
        return try {
            // Basic validation for anonymous registration
            when {
                username.isBlank() -> {
                    return Result.failure(IllegalArgumentException("Username tidak boleh kosong!"))
                }
                username.length < 3 -> {
                    return Result.failure(IllegalArgumentException("Username minimal 3 karakter."))
                }
                displayName.isBlank() -> {
                    return Result.failure(IllegalArgumentException("Display name tidak boleh kosong."))
                }
            }

            val firebaseResult = firebaseAuthService.registerAnonymous(username, displayName)
            if (firebaseResult.isSuccess) {
                val firebaseUser = firebaseResult.getOrThrow()
                Timber.d("AuthRepositoryImpl: Anonymous user registered with UID: ${firebaseUser.uid}")
                val profileResult = firebaseAuthService.getUserProfile(firebaseUser.uid)
                if (profileResult.isSuccess) {
                    val user = mapFirebaseProfileToUser(profileResult.getOrThrow())
                    Timber.d("AuthRepositoryImpl: User profile mapped for new anonymous user: ${user.displayName}")
                    Result.success(user)
                } else {
                    Timber.e("AuthRepositoryImpl: Failed to fetch user profile after anonymous registration: ${profileResult.exceptionOrNull()?.message}")
                    Result.failure(profileResult.exceptionOrNull() ?: Exception("Gagal memuat profil pengguna setelah registrasi anonymous."))
                }
            } else {
                Timber.e("AuthRepositoryImpl: Anonymous registration failed in firebaseAuthService: ${firebaseResult.exceptionOrNull()?.message}")
                Result.failure(firebaseResult.exceptionOrNull()!!)
            }
        } catch (e: Exception) {
            Timber.e(e, "AuthRepositoryImpl: Unexpected error during anonymous registration.")
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        Timber.d("AuthRepositoryImpl: Logout called.")
        return firebaseAuthService.logout()
    }

    override suspend fun updateUserProfile(updates: Map<String, Any>): Result<Unit> {
        Timber.d("AuthRepositoryImpl: updateUserProfile called.")
        return firebaseAuthService.updateUserProfile(updates)
    }

    override suspend fun deleteAccount(): Result<Unit> {
        Timber.d("AuthRepositoryImpl: deleteAccount called.")
        return firebaseAuthService.deleteAccount()
    }

    override suspend fun validateUsername(username: String): UsernameValidation {
        Timber.d("AuthRepositoryImpl: validateUsername called for: $username")

        return try {
            // FIXED: Very permissive validation for login/registration
            when {
                username.isBlank() -> UsernameValidation(
                    isValid = false,
                    message = "Username tidak boleh kosong!",
                    suggestions = emptyList()
                )
                username.length < 3 -> UsernameValidation(
                    isValid = false,
                    message = "Username minimal 3 karakter.",
                    suggestions = username.generateUsernameSuggestions()
                )
                username.length > 30 -> UsernameValidation(
                    isValid = false,
                    message = "Username terlalu panjang.",
                    suggestions = username.generateUsernameSuggestions()
                )
                // FIXED: Very permissive regex - allow most characters for existing usernames
                !username.matches(Regex("^[a-zA-Z0-9_.-]+$")) -> UsernameValidation(
                    isValid = false,
                    message = "Username mengandung karakter yang tidak valid.",
                    suggestions = username.generateUsernameSuggestions()
                )
                else -> UsernameValidation(
                    isValid = true,
                    message = "Username format valid",
                    suggestions = emptyList()
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "AuthRepositoryImpl: Error during username validation")
            // FIXED: Return permissive result on error
            UsernameValidation(
                isValid = true,
                message = "Validation skipped due to error",
                suggestions = emptyList()
            )
        }
    }

    override fun isAuthenticated(): Boolean {
        Timber.d("AuthRepositoryImpl: isAuthenticated called. Result: ${firebaseAuthService.isAuthenticated()}")
        return firebaseAuthService.isAuthenticated()
    }
}