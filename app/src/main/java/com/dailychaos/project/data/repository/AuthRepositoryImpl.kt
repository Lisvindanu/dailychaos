package com.dailychaos.project.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.dailychaos.project.data.remote.firebase.FirebaseAuthService
import com.dailychaos.project.domain.model.AuthState
import com.dailychaos.project.domain.model.User
import com.dailychaos.project.domain.model.UsernameValidation
import com.dailychaos.project.domain.repository.AuthRepository
import com.dailychaos.project.preferences.UserPreferences
import com.dailychaos.project.util.ValidationUtil // Pastikan ini diimport
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
import timber.log.Timber // Pastikan ini diimport

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuthService: FirebaseAuthService,
    private val validationUtil: ValidationUtil, // Sudah benar di-inject
    private val userPreferences: UserPreferences
) : AuthRepository {

    @RequiresApi(Build.VERSION_CODES.O)
    private fun mapFirebaseProfileToUser(profile: Map<String, Any>): User {
        // Fungsi helper untuk parsing tanggal yang fleksibel (bisa handle String atau Timestamp)
        fun parseDate(value: Any?): Instant {
            return when (value) {
                is Timestamp -> value.toDate().toInstant().toKotlinInstant()
                is String -> {
                    try {
                        // Coba format umum yang terlihat di database Anda
                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(value)?.toInstant()?.toKotlinInstant() ?: Clock.System.now()
                    } catch (e: Exception) {
                        Clock.System.now() // Fallback jika parsing gagal
                    }
                }
                else -> Clock.System.now() // Fallback untuk tipe data lain atau null
            }
        }

        // Fungsi helper untuk parsing angka dari Long ke Int dengan aman
        fun parseLongToInt(value: Any?): Int {
            return (value as? Long ?: 0L).toInt()
        }

        return User(
            id = profile["userId"] as? String ?: profile["uid"] as? String ?: "",
            email = profile["email"] as? String,
            displayName = profile["displayName"] as? String ?: "",
            anonymousUsername = profile["username"] as? String ?: "",
            isAnonymous = profile["authType"] as? String != "email",
            chaosEntriesCount = parseLongToInt(profile["chaosEntries"] ?: profile["chaosEntriesCount"]),
            supportGivenCount = parseLongToInt(profile["supportGiven"] ?: profile["totalSupportsGiven"]),
            supportReceivedCount = parseLongToInt(profile["supportReceived"] ?: profile["totalSupportsReceived"]),
            streakDays = parseLongToInt(profile["dayStreak"]),
            joinedAt = parseDate(profile["joinDate"] ?: profile["createdAt"]),
            lastActiveAt = parseDate(profile["lastLoginDate"] ?: profile["lastActiveAt"] ?: profile["lastLogin"])
            // settings akan menggunakan default dari model User
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

                if (storedAuthType == "username" && !storedUserId.isNullOrBlank()) {
                    val profileResult = firebaseAuthService.getUserProfile(storedUserId)
                    return if (profileResult.isSuccess) {
                        val user = mapFirebaseProfileToUser(profileResult.getOrThrow())
                        Timber.d("AuthRepositoryImpl: Found user in preferences for username auth: ${user.id}")
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
            val validationResult = validationUtil.validateUsername(username)
            // === PERBAIKAN: Gunakan `isValid` dan `message` dari UsernameValidation ===
            if (!validationResult.isValid) {
                Timber.e("AuthRepositoryImpl: Username validation failed: ${validationResult.message}")
                return Result.failure(Exception(validationResult.message ?: "Validasi username gagal."))
            }

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
            if (!emailValidation) { // Menggunakan !emailValidation karena isValidEmail mengembalikan Boolean
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
            val usernameValidationResult = validationUtil.validateUsername(username)
            // === PERBAIKAN: Gunakan `isValid` dan `message` dari UsernameValidation ===
            if (!usernameValidationResult.isValid) {
                Timber.e("AuthRepositoryImpl: Username validation failed during registerWithUsername: ${usernameValidationResult.message}")
                return Result.failure(IllegalArgumentException(usernameValidationResult.message ?: "Validasi username gagal."))
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
        return validationUtil.validateUsername(username)
    }

    override fun isAuthenticated(): Boolean {
        Timber.d("AuthRepositoryImpl: isAuthenticated called. Result: ${firebaseAuthService.isAuthenticated()}")
        return firebaseAuthService.isAuthenticated()
    }
}