// File: app/src/main/java/com/dailychaos/project/data/remote/firebase/FirebaseAuthService.kt
package com.dailychaos.project.data.remote.firebase

import com.dailychaos.project.preferences.UserPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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
     * Register with username (email auth + username/displayName)
     * FIX: User won't be anonymous if they register with username + display name
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

            // Create email from username for Firebase Auth (tidak anonymous)
            val generatedEmail = "${username}@dailychaos.local"
            val temporaryPassword = generateSecurePassword()

            // Create user with email/password (bukan anonymous)
            val authResult = firebaseAuth.createUserWithEmailAndPassword(generatedEmail, temporaryPassword).await()
            val user = authResult.user ?: throw Exception("User creation failed")

            // Update profile with display name untuk avoid anonymous
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName.ifBlank { username })
                .build()
            user.updateProfile(profileUpdates).await()

            // Create user profile with username and display name
            val profileResult = createUserProfile(
                userId = user.uid,
                username = username,
                displayName = displayName.ifBlank { username },
                email = generatedEmail,
                authType = "username"
            )

            if (profileResult.isSuccess) {
                // Save to preferences
                userPreferences.saveUserData(user.uid, username, displayName.ifBlank { username })
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
    suspend fun registerWithEmail(
        email: String,
        password: String,
        displayName: String
    ): Result<FirebaseUser> {
        return try {
            // Validate email format
            if (!isValidEmail(email)) {
                return Result.failure(Exception("Format email tidak valid!"))
            }

            // Check if email is already in use
            val isEmailAvailable = checkEmailAvailability(email)
            if (!isEmailAvailable) {
                return Result.failure(Exception("Email sudah terdaftar! Gunakan email lain atau coba login."))
            }

            // Create user with email and password
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user ?: throw Exception("Registration failed")

            // Update profile dengan display name
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName.ifBlank { "Chaos Member" })
                .build()
            user.updateProfile(profileUpdates).await()

            // Create user profile in Firestore
            val profileResult = createUserProfile(
                userId = user.uid,
                username = null, // No username for email registration
                displayName = displayName.ifBlank { "Chaos Member" },
                email = email,
                authType = "email"
            )

            if (profileResult.isSuccess) {
                // Save to preferences
                userPreferences.saveUserData(user.uid, null, displayName.ifBlank { "Chaos Member" })
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
     * Login with username
     * FIX: Login using stored credentials for username-based accounts
     */
    suspend fun loginWithUsername(username: String): Result<FirebaseUser> {
        return try {
            // Find user by username in Firestore
            val userQuery = firestore.collection("users")
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .await()

            if (userQuery.documents.isEmpty()) {
                return Result.failure(Exception("Username '$username' tidak ditemukan! Pastikan username benar atau daftar dulu."))
            }

            val userDoc = userQuery.documents.first()
            val userData = userDoc.data ?: return Result.failure(Exception("Data user tidak valid"))

            // Get email from stored user data
            val storedEmail = userData["email"] as? String
                ?: return Result.failure(Exception("Email user tidak ditemukan"))

            // Get stored password hash (in real app, use proper auth flow)
            val storedPasswordHash = userData["passwordHash"] as? String
                ?: return Result.failure(Exception("Password tidak ditemukan"))

            // Sign in with email/password
            val authResult = firebaseAuth.signInWithEmailAndPassword(storedEmail, storedPasswordHash).await()
            val user = authResult.user ?: throw Exception("Login failed")

            // Update preferences
            val displayName = userData["displayName"] as? String ?: username
            userPreferences.saveUserData(user.uid, username, displayName)

            Result.success(user)
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
            val user = authResult.user ?: throw Exception("Login failed")

            // Update preferences from Firestore profile
            val userProfile = getUserProfileData(user.uid)
            if (userProfile.isSuccess) {
                val profileData = userProfile.getOrNull()
                val username = profileData?.get("username") as? String
                val displayName = profileData?.get("displayName") as? String ?: "Chaos Member"
                userPreferences.saveUserData(user.uid, username, displayName)
            }

            Result.success(user)
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
     * Create user profile in Firestore
     */
    private suspend fun createUserProfile(
        userId: String,
        username: String?,
        displayName: String,
        email: String?,
        authType: String
    ): Result<Unit> {
        return try {
            val userData = hashMapOf(
                "userId" to userId,
                "username" to username,
                "displayName" to displayName,
                "email" to email,
                "authType" to authType,
                "createdAt" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
                "isActive" to true,
                "profilePicture" to null,
                "bio" to "",
                "chaosCount" to 0
            )

            // Save password hash for username-based accounts
            if (authType == "username" && email != null) {
                userData["passwordHash"] = generateSecurePassword()
            }

            firestore.collection("users")
                .document(userId)
                .set(userData)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get user profile data from Firestore
     */
    suspend fun getUserProfileData(userId: String): Result<Map<String, Any>?> {
        return try {
            val document = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            if (document.exists()) {
                Result.success(document.data)
            } else {
                Result.failure(Exception("User profile not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Validate username format
     */
    private fun validateUsername(username: String): Result<Unit> {
        return when {
            username.isBlank() -> Result.failure(Exception("Username tidak boleh kosong!"))
            username.length < 3 -> Result.failure(Exception("Username minimal 3 karakter!"))
            username.length > 20 -> Result.failure(Exception("Username maksimal 20 karakter!"))
            !username.matches(Regex("^[a-zA-Z0-9_]+$")) ->
                Result.failure(Exception("Username hanya boleh huruf, angka, dan underscore!"))
            else -> Result.success(Unit)
        }
    }

    /**
     * Check username availability
     */
    private suspend fun checkUsernameAvailability(username: String): Boolean {
        return try {
            val query = firestore.collection("users")
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .await()

            query.documents.isEmpty()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Check email availability
     */
    private suspend fun checkEmailAvailability(email: String): Boolean {
        return try {
            val query = firestore.collection("users")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .await()

            query.documents.isEmpty()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Validate email format
     */
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Generate secure password for username-based accounts
     */
    private fun generateSecurePassword(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*"
        return (1..16)
            .map { chars.random() }
            .joinToString("")
    }
}