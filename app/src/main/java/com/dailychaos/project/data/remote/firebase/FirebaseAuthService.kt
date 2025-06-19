// File: app/src/main/java/com/dailychaos/project/data/remote/firebase/FirebaseAuthService.kt
package com.dailychaos.project.data.remote.firebase

import com.dailychaos.project.preferences.UserPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import timber.log.Timber

/**
 * Firebase Authentication Service - CLOUD FUNCTION FIRST APPROACH
 * "Biarkan Cloud Function yang handle semua logic kompleks!"
 */
@Singleton
class FirebaseAuthService @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userPreferences: UserPreferences,
    private val functions: FirebaseFunctions
) {

    /**
     * Get current authenticated user
     */
    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    /**
     * Check if user is authenticated
     */
    fun isAuthenticated(): Boolean = currentUser != null

    /**
     * Register with username - PURE Cloud Function approach
     * "Cloud Function handle everything!"
     */
    suspend fun registerWithUsername(username: String, displayName: String): Result<FirebaseUser> {
        return try {
            Timber.d("🎭 REGISTER with username: '$username', displayName: '$displayName'")

            // Basic client-side validation only
            if (username.isBlank()) {
                return Result.failure(Exception("Username tidak boleh kosong!"))
            }
            if (username.length < 3) {
                return Result.failure(Exception("Username minimal 3 karakter."))
            }

            // Call Cloud Function for REGISTRATION
            val data = hashMapOf(
                "username" to username,
                "displayName" to displayName.ifBlank { username },
                "isRegistration" to true
            )

            Timber.d("📤 Sending to Cloud Function: $data")

            val result = functions
                .getHttpsCallable("generateCustomToken")
                .call(data)
                .await()

            val customTokenData = result.data as? Map<*, *>
                ?: throw Exception("Cloud Function response is null")

            val tokenString = customTokenData["customToken"] as? String
                ?: throw Exception("Custom token missing from response")

            val userId = customTokenData["userId"] as? String
                ?: throw Exception("UserId missing from response")

            Timber.d("✅ Got custom token for userId: $userId")

            // Sign in with custom token
            val authResult = firebaseAuth.signInWithCustomToken(tokenString).await()
            val user = authResult.user ?: throw Exception("Sign in failed after getting token")

            Timber.d("🎉 Registration successful! UID: ${user.uid}")

            // Update preferences - Cloud Function already created all data
            updateUserPreferences(userId, username, displayName.ifBlank { username }, "username")

            Result.success(user)

        } catch (e: Exception) {
            Timber.e(e, "❌ Registration failed")

            val errorMessage = when {
                e.message?.contains("already-exists") == true -> "Username '$username' sudah dipakai!"
                e.message?.contains("invalid-argument") == true -> "Username tidak valid."
                e.message?.contains("network") == true -> "Koneksi bermasalah. Cek internet!"
                else -> "Registrasi gagal: ${e.message}"
            }

            Result.failure(Exception(errorMessage))
        }
    }

    /**
     * Login with username - PURE Cloud Function approach
     * "Cloud Function verify everything!"
     */
    suspend fun loginWithUsername(username: String): Result<FirebaseUser> {
        return try {
            Timber.d("🏠 LOGIN with username: '$username'")

            if (username.isBlank()) {
                return Result.failure(Exception("Username tidak boleh kosong!"))
            }

            // Call Cloud Function for LOGIN
            val data = hashMapOf(
                "username" to username,
                "isRegistration" to false
            )

            Timber.d("📤 Sending to Cloud Function: $data")

            val result = functions
                .getHttpsCallable("generateCustomToken")
                .call(data)
                .await()

            val customTokenData = result.data as? Map<*, *>
                ?: throw Exception("Cloud Function response is null")

            val tokenString = customTokenData["customToken"] as? String
                ?: throw Exception("Custom token missing from response")

            val userId = customTokenData["userId"] as? String
                ?: throw Exception("UserId missing from response")

            Timber.d("✅ Got custom token for userId: $userId")

            // Sign in with custom token
            val authResult = firebaseAuth.signInWithCustomToken(tokenString).await()
            val user = authResult.user ?: throw Exception("Sign in failed after getting token")

            Timber.d("🎉 Login successful! UID: ${user.uid}")

            // Get user profile to update preferences
            val profileData = getUserProfileData(userId)
            if (profileData != null) {
                updateUserPreferences(
                    userId = userId,
                    username = username,
                    displayName = profileData["displayName"] as? String ?: username,
                    authType = "username"
                )
            }

            Result.success(user)

        } catch (e: Exception) {
            Timber.e(e, "❌ Login failed")

            val errorMessage = when {
                e.message?.contains("not-found") == true -> "Username '$username' tidak ditemukan."
                e.message?.contains("invalid-argument") == true -> "Username tidak valid."
                e.message?.contains("network") == true -> "Koneksi bermasalah. Cek internet!"
                else -> "Login gagal: ${e.message}"
            }

            Result.failure(Exception(errorMessage))
        }
    }

    /**
     * Register with email - LOCAL approach (no Cloud Function)
     */
    suspend fun registerWithEmail(email: String, password: String, displayName: String): Result<FirebaseUser> {
        return try {
            Timber.d("📧 REGISTER with email: $email")

            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user ?: throw Exception("Email registration failed")

            // Update Firebase Auth profile
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            user.updateProfile(profileUpdates).await()

            // Create user profile in Firestore
            val profileResult = createUserProfile(
                userId = user.uid,
                username = "",
                displayName = displayName,
                email = email,
                authType = "email"
            )

            if (profileResult.isSuccess) {
                updateUserPreferences(user.uid, "", displayName, "email", email)
                Timber.d("🎉 Email registration successful!")
                Result.success(user)
            } else {
                user.delete().await()
                Result.failure(Exception("Gagal membuat profil pengguna"))
            }

        } catch (e: FirebaseAuthUserCollisionException) {
            Result.failure(Exception("Email sudah terdaftar!"))
        } catch (e: FirebaseAuthWeakPasswordException) {
            Result.failure(Exception("Password terlalu lemah!"))
        } catch (e: Exception) {
            Timber.e(e, "❌ Email registration failed")
            Result.failure(Exception("Registrasi email gagal: ${e.message}"))
        }
    }

    /**
     * Login with email - DIRECT Firebase Auth
     */
    suspend fun loginWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            Timber.d("📧 LOGIN with email: $email")

            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = authResult.user ?: throw Exception("Email login failed")

            // Load user profile
            val profileData = getUserProfileData(user.uid)
            if (profileData != null) {
                updateUserPreferences(
                    userId = user.uid,
                    username = profileData["username"] as? String ?: "",
                    displayName = profileData["displayName"] as? String ?: "User",
                    authType = "email",
                    email = email
                )
            }

            Timber.d("🎉 Email login successful!")
            Result.success(user)

        } catch (e: FirebaseAuthInvalidUserException) {
            Result.failure(Exception("Email tidak ditemukan!"))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(Exception("Password salah!"))
        } catch (e: Exception) {
            Timber.e(e, "❌ Email login failed")
            Result.failure(Exception("Login email gagal: ${e.message}"))
        }
    }

    /**
     * Register anonymous user - LOCAL approach
     */
    suspend fun registerAnonymous(username: String, displayName: String): Result<FirebaseUser> {
        return try {
            Timber.d("👤 REGISTER anonymous: $username")

            val authResult = firebaseAuth.signInAnonymously().await()
            val user = authResult.user ?: throw Exception("Anonymous registration failed")

            val profileResult = createUserProfile(
                userId = user.uid,
                username = username,
                displayName = displayName,
                email = null,
                authType = "anonymous"
            )

            if (profileResult.isSuccess) {
                updateUserPreferences(user.uid, username, displayName, "anonymous")
                Timber.d("🎉 Anonymous registration successful!")
                Result.success(user)
            } else {
                user.delete().await()
                Result.failure(Exception("Gagal membuat profil anonymous"))
            }

        } catch (e: Exception) {
            Timber.e(e, "❌ Anonymous registration failed")
            Result.failure(Exception("Registrasi anonymous gagal: ${e.message}"))
        }
    }

    /**
     * Helper: Update user preferences
     */
    private suspend fun updateUserPreferences(
        userId: String,
        username: String,
        displayName: String,
        authType: String,
        email: String? = null
    ) {
        userPreferences.setUserId(userId)
        userPreferences.setUsername(username)
        userPreferences.setDisplayName(displayName)
        userPreferences.setAuthType(authType)

        if (authType == "username" || authType == "anonymous") {
            userPreferences.setAnonymousUsername(username)
        }

        email?.let { userPreferences.setUserEmail(it) }

        Timber.d("✅ User preferences updated for: $displayName ($authType)")
    }

    /**
     * Helper: Get user profile data from Firestore
     */
    private suspend fun getUserProfileData(userId: String): Map<String, Any>? {
        return try {
            val document = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            if (document.exists()) {
                document.data
            } else {
                Timber.w("⚠️ User profile not found for: $userId")
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Error getting user profile for: $userId")
            null
        }
    }

    /**
     * Create user profile in Firestore (for email/anonymous only)
     */
    private suspend fun createUserProfile(
        userId: String,
        username: String?,
        displayName: String,
        email: String?,
        authType: String
    ): Result<Unit> {
        return try {
            val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

            val userProfile = hashMapOf<String, Any?>(
                "uid" to userId,
                "userId" to userId,
                "username" to username,
                "displayName" to displayName,
                "email" to email,
                "authType" to authType,
                "bio" to "",
                "profilePicture" to null,
                "chaosLevel" to 1,
                "partyRole" to "Newbie Adventurer",
                "dayStreak" to 0,
                "longestStreak" to 0,
                "totalEntries" to 0,
                "chaosEntries" to 0,
                "chaosEntriesCount" to 0,
                "streakDays" to 0,
                "supportGiven" to 0,
                "supportReceived" to 0,
                "totalSupportGiven" to 0,
                "totalSupportReceived" to 0,
                "totalSupportsGiven" to 0,
                "totalSupportsReceived" to 0,
                "achievements" to emptyList<String>(),
                "favoriteQuote" to "",
                "favoriteKonoSubaCharacter" to "",
                "isActive" to true,
                "isAnonymous" to (authType == "anonymous"),
                "profileVersion" to 1,
                "settings" to mapOf(
                    "theme" to "system",
                    "notificationsEnabled" to true,
                    "reminderTime" to "20:00",
                    "anonymousMode" to false,
                    "shareByDefault" to false,
                    "konoSubaQuotesEnabled" to true,
                    "showChaosLevel" to true
                ),
                "createdAt" to currentDate,
                "joinDate" to currentDate,
                "lastActiveAt" to currentDate,
                "lastLogin" to currentDate,
                "lastLoginDate" to currentDate
            )

            firestore.collection("users")
                .document(userId)
                .set(userProfile)
                .await()

            // Create username mapping for anonymous (not email)
            if (!username.isNullOrBlank() && authType == "anonymous") {
                firestore.collection("usernames")
                    .document(username.lowercase())
                    .set(mapOf(
                        "userId" to userId,
                        "username" to username,
                        "createdAt" to currentDate,
                        "authType" to authType
                    ))
                    .await()
            }

            Timber.d("✅ User profile created for: $userId")
            Result.success(Unit)

        } catch (e: Exception) {
            Timber.e(e, "❌ Error creating user profile")
            Result.failure(e)
        }
    }

    /**
     * Get user profile (public method)
     */
    suspend fun getUserProfile(): Result<Map<String, Any>> {
        val user = currentUser ?: return Result.failure(Exception("Tidak ada user yang login"))
        return getUserProfile(user.uid)
    }

    suspend fun getUserProfile(userId: String): Result<Map<String, Any>> {
        val data = getUserProfileData(userId)
        return if (data != null) {
            Result.success(data)
        } else {
            Result.failure(Exception("Profil tidak ditemukan"))
        }
    }

    /**
     * Update user profile
     */
    suspend fun updateUserProfile(updates: Map<String, Any>): Result<Unit> {
        return try {
            val user = currentUser ?: throw Exception("Tidak ada user yang login")

            firestore.collection("users")
                .document(user.uid)
                .update(updates)
                .await()

            Timber.d("✅ User profile updated")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "❌ Error updating user profile")
            Result.failure(e)
        }
    }

    /**
     * Check username availability (optional - Cloud Function also checks)
     */
    suspend fun checkUsernameAvailability(username: String): Boolean {
        return try {
            val document = firestore.collection("usernames")
                .document(username.lowercase())
                .get()
                .await()
            !document.exists()
        } catch (e: Exception) {
            Timber.e(e, "❌ Error checking username availability")
            false
        }
    }

    /**
     * Logout
     */
    suspend fun logout(): Result<Unit> {
        return try {
            firebaseAuth.signOut()
            userPreferences.clearUserData()
            Timber.d("👋 User logged out")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "❌ Error during logout")
            Result.failure(e)
        }
    }

    /**
     * Delete account
     */
    suspend fun deleteAccount(): Result<Unit> {
        return try {
            val user = currentUser ?: throw Exception("Tidak ada user yang login")
            val userId = user.uid

            // Delete from Firestore
            firestore.collection("users").document(userId).delete().await()

            // Delete username mapping if exists
            val usernameQuery = firestore.collection("usernames")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            usernameQuery.documents.forEach { it.reference.delete().await() }

            // Delete from Firebase Auth
            user.delete().await()

            // Clear preferences
            userPreferences.clearUserData()

            Timber.d("🗑️ Account deleted")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "❌ Error deleting account")
            Result.failure(e)
        }
    }

    /**
     * Generate random username
     */
    fun generateRandomUsername(): String {
        val adjectives = listOf("Epic", "Chaos", "Brave", "Wild", "Cool", "Swift", "Bold", "Lucky")
        val nouns = listOf("Adventurer", "Hero", "Warrior", "Mage", "Explorer", "Knight", "Rogue", "Wizard")
        val number = (1000..9999).random()
        return "${adjectives.random()}${nouns.random()}$number"
    }
}