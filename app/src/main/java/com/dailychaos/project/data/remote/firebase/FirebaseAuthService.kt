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
            // Validate email format
            if (!isValidEmail(email)) {
                return Result.failure(Exception("Format email tidak valid"))
            }

            // Validate password strength
            if (password.length < 6) {
                return Result.failure(Exception("Password minimal 6 karakter"))
            }

            // Create account with email and password
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user ?: throw Exception("Email registration gagal")

            // Update profile with display name
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName.ifBlank { generateUsernameFromEmail(email) })
                .build()
            user.updateProfile(profileUpdates).await()

            // Generate username from email (fallback)
            val generatedUsername = generateUsernameFromEmail(email)

            // Create user profile in Firestore
            val profileResult = createUserProfile(
                userId = user.uid,
                username = generatedUsername,
                displayName = displayName.ifBlank { generatedUsername },
                email = email,
                authType = "email"
            )

            if (profileResult.isSuccess) {
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
     * Login with username (for existing users)
     * "Welcome back to the party!"
     */
    suspend fun loginWithUsername(username: String): Result<FirebaseUser> {
        return try {
            // For username-based auth, we need to find the user first
            val userQuery = firestore.collection("users")
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .await()

            if (userQuery.isEmpty) {
                return Result.failure(Exception("Username '$username' tidak ditemukan! Silakan register terlebih dahulu."))
            }

            val userDoc = userQuery.documents.first()
            val userData = userDoc.data
            val authType = userData?.get("authType") as? String

            when (authType) {
                "username" -> {
                    // For username auth, we use anonymous auth and match the existing profile
                    val authResult = firebaseAuth.signInAnonymously().await()
                    val user = authResult.user ?: throw Exception("Login gagal")

                    // Update user preferences
                    userPreferences.setUserId(user.uid)
                    userPreferences.setAnonymousUsername(username)
                    userPreferences.setDisplayName(userData["displayName"] as? String ?: username)

                    Result.success(user)
                }
                "email" -> {
                    Result.failure(Exception("Username ini terdaftar dengan email. Silakan login menggunakan email."))
                }
                else -> {
                    Result.failure(Exception("Auth type tidak dikenal"))
                }
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
                "profileVersion" to 1
            )

            // Create user document
            firestore.collection("users")
                .document(userId)
                .set(userProfile)
                .await()

            // Create username lookup for quick availability checking
            firestore.collection("usernames")
                .document(username.lowercase())
                .set(mapOf(
                    "userId" to userId,
                    "username" to username,
                    "createdAt" to currentDate
                ))
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get user profile from Firestore
     */
    suspend fun getUserProfile(): Result<Map<String, Any>> {
        return try {
            val user = currentUser ?: throw Exception("User tidak login")

            val document = firestore.collection("users")
                .document(user.uid)
                .get()
                .await()

            if (document.exists()) {
                val data = document.data ?: emptyMap()

                // Update last login
                firestore.collection("users")
                    .document(user.uid)
                    .update("lastLogin", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
                    .await()

                Result.success(data)
            } else {
                Result.failure(Exception("Profil user tidak ditemukan"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Check if username is available
     */
    suspend fun checkUsernameAvailability(username: String): Boolean {
        return try {
            val document = firestore.collection("usernames")
                .document(username.lowercase())
                .get()
                .await()

            !document.exists()
        } catch (e: Exception) {
            false // Assume not available if error occurs
        }
    }

    /**
     * Validate username according to Daily Chaos rules
     */
    private fun validateUsername(username: String): Result<Unit> {
        return try {
            when {
                username.isBlank() -> {
                    Result.failure(Exception("Username tidak boleh kosong!"))
                }
                username.length < 3 -> {
                    Result.failure(Exception("Username minimal 3 karakter! Contoh: 'Megumin'"))
                }
                username.length > 20 -> {
                    Result.failure(Exception("Username maksimal 20 karakter! Singkat tapi berkesan!"))
                }
                !username.matches(Regex("^[a-zA-Z0-9_]+$")) -> {
                    Result.failure(Exception("Username hanya boleh huruf, angka, dan underscore!"))
                }
                FORBIDDEN_USERNAMES.contains(username.lowercase()) -> {
                    Result.failure(Exception("Username '$username' tidak diperbolehkan! Coba yang lebih kreatif!"))
                }
                else -> Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Validate email format
     */
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Generate username from email
     */
    private fun generateUsernameFromEmail(email: String): String {
        val localPart = email.substringBefore("@")
        val cleanUsername = localPart.replace(Regex("[^a-zA-Z0-9]"), "")
        return if (cleanUsername.length >= 3) cleanUsername else "user${System.currentTimeMillis()}"
    }

    /**
     * Get suggested usernames if current choice is taken
     * "Need backup names? We got you covered!"
     */
    fun getSuggestedUsernames(baseUsername: String): List<String> {
        val suggestions = mutableListOf<String>()

        // Generate variations
        val suffixes = listOf("Hero", "Master", "Pro", "Star", "Legend", "Epic")
        val numbers = listOf(123, 456, 789, 101, 2024)

        suffixes.forEach { suffix ->
            suggestions.add("${baseUsername}$suffix")
        }

        numbers.forEach { number ->
            suggestions.add("${baseUsername}$number")
        }

        // KonoSuba inspired additions
        val konoSubaSuffixes = listOf("Kazuma", "Aqua", "Megumin", "Darkness", "Adventurer", "Champion")
        konoSubaSuffixes.forEach { suffix ->
            suggestions.add("${baseUsername}$suffix")
        }

        return suggestions.take(6) // Return top 6 suggestions
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
     * Update user stats (chaos entries, streaks, etc.)
     */
    suspend fun updateUserStats(
        chaosEntries: Int? = null,
        dayStreak: Int? = null,
        supportGiven: Int? = null
    ): Result<Unit> {
        return try {
            val user = currentUser ?: throw Exception("User tidak login")

            val updates = mutableMapOf<String, Any>()
            chaosEntries?.let { updates["chaosEntries"] = it }
            dayStreak?.let {
                updates["dayStreak"] = it
                // Update longest streak if current is higher
                val currentProfile = getUserProfile().getOrNull()
                val currentLongest = (currentProfile?.get("longestStreak") as? Long)?.toInt() ?: 0
                if (it > currentLongest) {
                    updates["longestStreak"] = it
                }
            }
            supportGiven?.let { updates["supportGiven"] = it }

            if (updates.isNotEmpty()) {
                firestore.collection("users")
                    .document(user.uid)
                    .update(updates)
                    .await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        private val FORBIDDEN_USERNAMES = setOf(
            "admin", "administrator", "root", "system", "chaos", "dailychaos",
            "moderator", "mod", "support", "help", "api", "null", "undefined",
            "test", "demo", "example", "user", "username", "password",
            "guest", "anonymous", "bot", "service", "official"
        )
    }
}