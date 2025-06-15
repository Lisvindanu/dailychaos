// File: app/src/main/java/com/dailychaos/project/data/remote/firebase/FirebaseAuthService.kt
package com.dailychaos.project.data.remote.firebase

import com.dailychaos.project.preferences.UserPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
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
     * Check if user is authenticated
     */
    fun isAuthenticated(): Boolean = currentUser != null

    /**
     * Register with username (anonymous auth + username validation)
     * "Welcome new party member! Choose your adventure name wisely!"
     */
    suspend fun registerWithUsername(username: String, displayName: String): Result<FirebaseUser> {
        return try {
            // Validate username first
            val validationResult = validateUsername(username)
            if (!validationResult.isSuccess) {
                return Result.failure(validationResult.exceptionOrNull()!!)
            }

            // Check username availability
            val isAvailable = checkUsernameAvailability(username)
            if (!isAvailable) {
                return Result.failure(Exception("Username '$username' sudah digunakan! Coba yang lain seperti '${username}Hero'"))
            }

            // Sign in anonymously first
            val authResult = firebaseAuth.signInAnonymously().await()
            val user = authResult.user ?: throw Exception("Anonymous registration gagal")

            // Create user profile with username and display name
            val profileResult = createUserProfile(
                userId = user.uid,
                username = username,
                displayName = displayName.ifBlank { username },
                email = null,
                authType = "username"
            )

            if (profileResult.isSuccess) {
                // Update user preferences
                userPreferences.setUserId(user.uid)
                userPreferences.setAnonymousUsername(username)
                userPreferences.setDisplayName(displayName.ifBlank { username })

                Result.success(user)
            } else {
                // Rollback: delete user if profile creation fails
                user.delete().await()
                Result.failure(profileResult.exceptionOrNull() ?: Exception("Failed to create user profile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Register with email and password
     * "Join the party with full credentials!"
     */
    suspend fun registerWithEmail(email: String, password: String, displayName: String): Result<FirebaseUser> {
        return try {
            // Create user with email/password
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user ?: throw Exception("Email registration gagal")

            // Update Firebase Auth profile
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            user.updateProfile(profileUpdates).await()

            // Create Firestore profile
            val profileResult = createUserProfile(
                userId = user.uid,
                username = "", // No username for email auth
                displayName = displayName,
                email = email,
                authType = "email"
            )

            if (profileResult.isSuccess) {
                // Update user preferences
                userPreferences.setUserId(user.uid)
                userPreferences.setUserEmail(email)
                userPreferences.setDisplayName(displayName)

                Result.success(user)
            } else {
                // Rollback: delete user if profile creation fails
                user.delete().await()
                Result.failure(profileResult.exceptionOrNull() ?: Exception("Failed to create user profile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Login with username (for username-based auth)
     * "Welcome back, adventurer! Let's find your profile."
     *
     * FIX: This function should not perform a new anonymous sign-in.
     * It should find the user ID associated with the username and return the profile.
     */
    suspend fun loginWithUsername(username: String): Result<Map<String, Any>> {
        return try {
            // Step 1: Cari username di collection 'usernames' untuk dapat UID_ASLI.
            val usernameDoc = firestore.collection("usernames")
                .document(username.lowercase())
                .get()
                .await()

            if (!usernameDoc.exists()) {
                return Result.failure(Exception("Username '$username' tidak ditemukan."))
            }

            val originalUserId = usernameDoc.getString("userId")
                ?: return Result.failure(Exception("Data UID untuk username ini korup."))

            // Step 2: Gunakan UID_ASLI untuk mengambil data profil dari collection 'users'.
            val profileDoc = firestore.collection("users")
                .document(originalUserId)
                .get()
                .await()

            if (profileDoc.exists()) {
                // Tetap sign-in anonymously untuk mendapatkan sesi aktif.
                // Sesi ini hanya "tiket masuk", identitas aslinya adalah data profil yang kita fetch.
                firebaseAuth.signInAnonymously().await()

                val profileData = profileDoc.data ?: throw Exception("Data profil tidak ditemukan")
                Result.success(profileData)
            } else {
                Result.failure(Exception("Profile tidak ditemukan."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Login with email and password
     */
    suspend fun loginWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = authResult.user ?: throw Exception("Login gagal")

            // Load user profile and update preferences
            val profileResult = getUserProfile()
            if (profileResult.isSuccess) {
                val profile = profileResult.getOrNull()
                userPreferences.setUserId(user.uid)
                userPreferences.setUserEmail(email)
                userPreferences.setDisplayName(profile?.get("displayName") as? String ?: "User")
            }

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Create user profile in Firestore
     */
    private suspend fun createUserProfile(
        userId: String,
        username: String,
        displayName: String,
        email: String?,
        authType: String
    ): Result<Unit> {
        return try {
            val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

            val userProfile = hashMapOf(
                "userId" to userId,
                "username" to username,
                "displayName" to displayName,
                "email" to email,
                "authType" to authType,
                "joinDate" to currentDate,
                "lastLogin" to currentDate,
                "chaosEntries" to 0,
                "dayStreak" to 0,
                "longestStreak" to 0,
                "supportGiven" to 0,
                "supportReceived" to 0,
                "isActive" to true,
                "profileVersion" to 1,
                "bio" to "",
                "chaosLevel" to 1,
                "partyRole" to "Newbie Adventurer",
                "profilePicture" to null,
                "lastLoginDate" to currentDate,
                "achievements" to emptyList<String>()
            )

            // Create user document
            firestore.collection("users")
                .document(userId)
                .set(userProfile)
                .await()

            // Create username lookup for quick availability checking (only for username auth)
            if (username.isNotBlank()) {
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

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get user profile from Firestore (current user)
     * FIX: This method should exist for ProfileViewModel
     */
    suspend fun getUserProfile(): Result<Map<String, Any>> {
        return try {
            val user = currentUser ?: throw Exception("User tidak login")

            val document = firestore.collection("users")
                .document(user.uid)
                .get()
                .await()

            if (document.exists()) {
                val data = document.data ?: throw Exception("Profile data tidak ditemukan")
                Result.success(data)
            } else {
                Result.failure(Exception("Profile tidak ditemukan"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get user profile from Firestore by userId
     * FIX: This method should exist for AuthRepositoryImpl
     */
    suspend fun getUserProfile(userId: String): Result<Map<String, Any>> {
        return try {
            val document = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            if (document.exists()) {
                val data = document.data ?: throw Exception("Profile data tidak ditemukan")
                Result.success(data)
            } else {
                Result.failure(Exception("Profile tidak ditemukan"))
            }
        } catch (e: Exception) {
            Result.failure(e)
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
     * Delete user account
     */
    suspend fun deleteAccount(): Result<Unit> {
        return try {
            val user = currentUser ?: throw Exception("User tidak login")
            val userId = user.uid

            // Delete from Firestore first
            firestore.collection("users")
                .document(userId)
                .delete()
                .await()

            // Delete from Firebase Auth
            user.delete().await()

            // Clear preferences
            userPreferences.clearUserData()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Logout user
     */
    suspend fun logout(): Result<Unit> {
        return try {
            firebaseAuth.signOut()
            userPreferences.clearUserData()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Validate username format and requirements
     */
    private fun validateUsername(username: String): Result<Unit> {
        return when {
            username.length < 3 -> Result.failure(Exception("Username harus minimal 3 karakter"))
            username.length > 20 -> Result.failure(Exception("Username maksimal 20 karakter"))
            !username.matches(Regex("^[a-zA-Z0-9_]+$")) -> Result.failure(Exception("Username hanya boleh menggunakan huruf, angka, dan underscore"))
            username.startsWith("_") || username.endsWith("_") -> Result.failure(Exception("Username tidak boleh diawali atau diakhiri dengan underscore"))
            else -> Result.success(Unit)
        }
    }

    /**
     * Check username availability
     */
    suspend fun checkUsernameAvailability(username: String): Boolean {
        return try {
            val document = firestore.collection("usernames")
                .document(username.lowercase())
                .get()
                .await()

            !document.exists()
        } catch (e: Exception) {
            false
        }
    }
}