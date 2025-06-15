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

    // File: app/src/main/java/com/dailychaos/project/data/remote/firebase/FirebaseAuthService.kt
// Additional methods untuk registrasi (tambahkan ke existing FirebaseAuthService)

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
                displayName = displayName,
                email = null,
                authType = "username"
            )

            // Save user info to preferences
            userPreferences.setUserId(user.uid)
            userPreferences.setAnonymousUsername(username)
            userPreferences.setDisplayName(displayName)

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
            if (!ValidationUtil.isValidEmail(email)) {
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
                .setDisplayName(displayName)
                .build()
            user.updateProfile(profileUpdates).await()

            // Generate username from email (fallback)
            val generatedUsername = generateUsernameFromEmail(email)

            // Create user profile in Firestore
            createUserProfile(
                userId = user.uid,
                username = generatedUsername,
                displayName = displayName,
                email = email,
                authType = "email"
            )

            // Save user info to preferences
            userPreferences.setUserId(user.uid)
            userPreferences.setUserEmail(email)
            userPreferences.setDisplayName(displayName)

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
            "lastActiveAt" to System.currentTimeMillis()
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

    companion object {
        private val FORBIDDEN_USERNAMES = setOf(
            "admin", "administrator", "root", "system", "chaos", "dailychaos",
            "moderator", "mod", "support", "help", "api", "null", "undefined",
            "test", "demo", "example", "user", "username", "password"
        )
    }
}