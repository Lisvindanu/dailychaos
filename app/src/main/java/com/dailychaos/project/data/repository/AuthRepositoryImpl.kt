// File: app/src/main/java/com/dailychaos/project/data/repository/AuthRepositoryImpl.kt
package com.dailychaos.project.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.dailychaos.project.data.remote.firebase.FirebaseAuthService
import com.dailychaos.project.domain.model.AuthState
import com.dailychaos.project.domain.model.User
import com.dailychaos.project.domain.model.UsernameValidation
import com.dailychaos.project.domain.repository.AuthRepository
import com.dailychaos.project.preferences.UserPreferences
import com.dailychaos.project.util.ValidationUtil
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

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuthService: FirebaseAuthService,
    private val validationUtil: ValidationUtil,
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
    override suspend fun getCurrentUser(): User? {
        try {
            // Ambil tipe login dan ID yang tersimpan secara lokal
            val authType = userPreferences.authType.first()
            val storedUserId = userPreferences.userId.first()

            // --- KONDISI 1: Jika login via "username" ---
            // Kita percaya pada ID yang disimpan, bukan sesi sementara di Firebase Auth.
            if (authType == "username" && !storedUserId.isNullOrBlank()) {
                val profileResult = firebaseAuthService.getUserProfile(storedUserId)
                return if (profileResult.isSuccess) {
                    mapFirebaseProfileToUser(profileResult.getOrThrow())
                } else {
                    userPreferences.clearUserData() // Bersihkan jika ID sudah tidak valid
                    null
                }
            }

            // --- KONDISI 2: "Cara biasa" untuk login via Email atau Registrasi Anonim baru ---
            val firebaseUser = firebaseAuthService.currentUser
            if (firebaseUser != null) {
                val profileResult = firebaseAuthService.getUserProfile(firebaseUser.uid)
                return if (profileResult.isSuccess) {
                    mapFirebaseProfileToUser(profileResult.getOrThrow())
                } else {
                    null
                }
            }

            // Jika tidak ada kondisi yang terpenuhi, berarti tidak ada user yang login
            return null

        } catch (e: Exception) {
            // Jika terjadi error, kembalikan null
            return null
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun loginWithUsername(username: String): Result<User> {
        return try {
            val profileResult = firebaseAuthService.loginWithUsername(username)
            if (profileResult.isSuccess) {
                Result.success(mapFirebaseProfileToUser(profileResult.getOrThrow()))
            } else {
                Result.failure(profileResult.exceptionOrNull()!!)
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
                    Result.success(mapFirebaseProfileToUser(profileResult.getOrThrow()))
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
                    Result.success(mapFirebaseProfileToUser(profileResult.getOrThrow()))
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
                    Result.success(mapFirebaseProfileToUser(profileResult.getOrThrow()))
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

    override suspend fun updateUserProfile(updates: Map<String, Any>): Result<Unit> {
        return firebaseAuthService.updateUserProfile(updates)
    }

    override suspend fun deleteAccount(): Result<Unit> {
        return firebaseAuthService.deleteAccount()
    }

    override suspend fun validateUsername(username: String): UsernameValidation {
        return validationUtil.validateUsername(username)
    }

    override fun isAuthenticated(): Boolean {
        return firebaseAuthService.isAuthenticated()
    }
}