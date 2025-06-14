package com.dailychaos.project.data.remote.firebase

import com.dailychaos.project.preferences.UserPreferences
import com.dailychaos.project.util.ValidationUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase Authentication Service
 * "Even anonymous adventurers need proper party registration"
 */
@Singleton
class FirebaseAuthService @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userPreferences: UserPreferences
) {

    /**
     * Get current authenticated user
     */
    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    /**
     * Get authentication state as flow
     */
    fun getAuthStateFlow(): Flow<FirebaseUser?> = flow {
        firebaseAuth.addAuthStateListener { auth ->
            // Emit current user state
        }
        emit(firebaseAuth.currentUser)
    }

    /**
     * Login with username (anonymous auth + username validation)
     * "Join the party anonymously but with style!"
     */
    suspend fun loginWithUsername(username: String): Result<FirebaseUser> {
        return try {
            // Validate username first
            val validationResult = validateUsername(username)
            if (!validationResult.isSuccess) {
                return Result.failure(validationResult.exceptionOrNull()!!)
            }

            // Check username availability
            val isAvailable = checkUsernameAvailability(username)
            if (!isAvailable) {
                return Result.failure(Exception("Username sudah digunakan! Coba yang lain, seperti 'KazumaTheGreat2'"))
            }

            // Sign in anonymously first
            val authResult = firebaseAuth.signInAnonymously().await()
            val user = authResult.user ?: throw Exception("Anonymous sign in gagal")

            // Create user profile with username
            createUserProfile(user.uid, username)

            // Save user info to preferences
            userPreferences.setUserId(user.uid)
            userPreferences.setAnonymousUsername(username)

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Validate username according to Daily Chaos rules
     * "Even in chaos, we need some rules!"
     */
    private fun validateUsername(username: String): Result<Unit> {
        val errorMessage = ValidationUtil.getUsernameErrorMessage(username)
        return if (errorMessage == null) {
            Result.success(Unit)
        } else {
            Result.failure(Exception(errorMessage))
        }
    }

    /**
     * Check if username is available
     */
    private suspend fun checkUsernameAvailability(username: String): Boolean {
        return try {
            val query = firestore.collection("users")
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .await()

            query.isEmpty
        } catch (e: Exception) {
            // If error, assume not available to be safe
            false
        }
    }

    /**
     * Create user profile in Firestore
     */
    private suspend fun createUserProfile(uid: String, username: String) {
        try {
            val userProfile = hashMapOf(
                "uid" to uid,
                "username" to username,
                "displayName" to username,
                "email" to null,
                "isAnonymous" to true,
                "createdAt" to com.google.firebase.Timestamp.now(),
                "lastActiveAt" to com.google.firebase.Timestamp.now(),
                "chaosEntriesCount" to 0,
                "streakDays" to 0,
                "totalSupportGiven" to 0,
                "totalSupportReceived" to 0,
                "favoriteKonoSubaCharacter" to "undecided", // For later KonoSuba features
                "settings" to hashMapOf(
                    "theme" to "system",
                    "notificationsEnabled" to true,
                    "konoSubaQuotesEnabled" to true,
                    "anonymousMode" to true,
                    "shareByDefault" to false,
                    "showChaosLevel" to true,
                    "reminderTime" to "20:00"
                )
            )

            firestore.collection("users")
                .document(uid)
                .set(userProfile)
                .await()

            // Also create username lookup for quick checks
            firestore.collection("usernames")
                .document(username.lowercase())
                .set(hashMapOf(
                    "uid" to uid,
                    "username" to username,
                    "createdAt" to com.google.firebase.Timestamp.now()
                ))
                .await()

        } catch (e: Exception) {
            throw Exception("Gagal membuat profil user: ${e.message}")
        }
    }

    /**
     * Update user profile
     */
    suspend fun updateUserProfile(updates: Map<String, Any>): Result<Unit> {
        return try {
            val user = currentUser ?: throw Exception("User tidak login")

            firestore.collection("users")
                .document(user.uid)
                .update(updates)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get user profile from Firestore
     */
    suspend fun getUserProfile(uid: String? = null): Result<Map<String, Any>> {
        return try {
            val targetUid = uid ?: currentUser?.uid ?: throw Exception("User tidak login")

            val document = firestore.collection("users")
                .document(targetUid)
                .get()
                .await()

            if (document.exists()) {
                Result.success(document.data ?: emptyMap())
            } else {
                Result.failure(Exception("Profil user tidak ditemukan"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Logout user
     * "Time to leave the party... for now"
     */
    suspend fun logout(): Result<Unit> {
        return try {
            firebaseAuth.signOut()
            userPreferences.clearUserPreferences()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete user account and all data
     * "Full party disbandment - use with caution!"
     */
    suspend fun deleteAccount(): Result<Unit> {
        return try {
            val user = currentUser ?: throw Exception("User tidak login")
            val profile = getUserProfile().getOrThrow()
            val username = profile["username"] as? String

            // Delete user profile
            firestore.collection("users")
                .document(user.uid)
                .delete()
                .await()

            // Delete username lookup if exists
            if (username != null) {
                firestore.collection("usernames")
                    .document(username.lowercase())
                    .delete()
                    .await()
            }

            // Delete Firebase auth user
            user.delete().await()

            // Clear preferences
            userPreferences.clearUserPreferences()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Check if current user is authenticated
     */
    fun isAuthenticated(): Boolean = currentUser != null

    /**
     * Get suggested usernames if current choice is taken
     * "Need backup names? We got you covered!"
     */
    fun getSuggestedUsernames(baseUsername: String): List<String> {
        return ValidationUtil.generateUsernameSuggestions(baseUsername)
    }
}