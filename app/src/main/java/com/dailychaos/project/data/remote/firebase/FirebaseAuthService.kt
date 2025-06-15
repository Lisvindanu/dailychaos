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
            createUserProfile(
                userId = user.uid,
                username = username,
                displayName = displayName.ifBlank { username },
                email = null,
                authType = "username"
            )

            // Save user info to preferences
            userPreferences.setUserId(user.uid)
            userPreferences.setAnonymousUsername(username)
            userPreferences.setDisplayName(displayName.ifBlank { username })

            Result.success(user)
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
            createUserProfile(
                userId = user.uid,
                username = generatedUsername,
                displayName = displayName.ifBlank { generatedUsername },
                email = email,
                authType = "email"
            )

            // Save user info to preferences
            userPreferences.setUserId(user.uid)
            userPreferences.setUserEmail(email)
            userPreferences.setDisplayName(displayName.ifBlank { generatedUsername })

            Result.success(user)
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
            // This is a simplified login - in real app you'd need proper session management
            // For now, we'll treat it as re-registration since we use anonymous auth
            registerWithUsername(username, username)
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
                .document(username)
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
                    Result.failure(Exception("Username hanya boleh huruf, angka, dan underscore! Contoh: 'kazuma_hero'"))
                }
                username.startsWith("_") || username.endsWith("_") -> {
                    Result.failure(Exception("Username tidak boleh dimulai atau diakhiri dengan underscore!"))
                }
                username.contains("__") -> {
                    Result.failure(Exception("Username tidak boleh ada double underscore!"))
                }
                username.lowercase() in FORBIDDEN_USERNAMES -> {
                    Result.failure(Exception("Username '$username' tidak boleh digunakan! Coba yang lain."))
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
        val baseUsername = email.substringBefore("@")
            .replace(Regex("[^a-zA-Z0-9]"), "")
            .lowercase()
            .take(15)

        val randomSuffix = (100..999).random()
        return "${baseUsername}$randomSuffix"
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
    ) {
        val userProfile = hashMapOf(
            "userId" to userId,
            "username" to username,
            "displayName" to displayName,
            "email" to email,
            "authType" to authType,
            "createdAt" to System.currentTimeMillis(),
            "isActive" to true,
            "chaosLevel" to 1,
            "totalEntries" to 0,
            "totalSupportsGiven" to 0,
            "totalSupportsReceived" to 0,
            "partyRole" to "Newbie Adventurer",
            "bio" to "Baru join party chaos! 🌪️",
            "favoriteQuote" to "Every chaos is a new adventure!",
            "achievements" to emptyList<String>(),
            "lastActiveAt" to System.currentTimeMillis(),
            "settings" to hashMapOf(
                "theme" to "system",
                "notificationsEnabled" to true,
                "konoSubaQuotesEnabled" to true,
                "anonymousMode" to (authType == "username"),
                "shareByDefault" to false,
                "showChaosLevel" to true,
                "reminderTime" to "20:00"
            )
        )

        // Save to users collection
        firestore.collection("users")
            .document(userId)
            .set(userProfile)
            .await()

        // Also save username to separate collection for uniqueness checking
        firestore.collection("usernames")
            .document(username)
            .set(mapOf(
                "userId" to userId,
                "createdAt" to System.currentTimeMillis()
            ))
            .await()
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

    companion object {
        private val FORBIDDEN_USERNAMES = setOf(
            "admin", "administrator", "root", "system", "chaos", "dailychaos",
            "moderator", "mod", "support", "help", "api", "null", "undefined",
            "test", "demo", "example", "user", "username", "password",
            "guest", "anonymous", "bot", "service", "official"
        )
    }
}