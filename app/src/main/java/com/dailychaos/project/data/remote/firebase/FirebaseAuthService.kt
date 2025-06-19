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
 * Firebase Authentication Service - SIMPLIFIED SINGLE COLLECTION
 * "Satu collection users aja, simple dan efektif!"
 */
@Singleton
class FirebaseAuthService @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userPreferences: UserPreferences,
    private val functions: FirebaseFunctions
) {

    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    fun isAuthenticated(): Boolean = currentUser != null

    /**
     * Register with username - Simple approach with single collection
     */
    suspend fun registerWithUsername(username: String, displayName: String): Result<FirebaseUser> {
        return try {
            val trimmedUsername = username.trim()
            val trimmedDisplayName = displayName.trim().ifBlank { trimmedUsername }

            Timber.d("🎭 REGISTER with username: '$trimmedUsername', displayName: '$trimmedDisplayName'")

            // Basic validation
            if (trimmedUsername.isBlank()) {
                return Result.failure(Exception("Username tidak boleh kosong!"))
            }
            if (trimmedUsername.length < 3) {
                return Result.failure(Exception("Username minimal 3 karakter."))
            }
            if (trimmedUsername.length > 30) {
                return Result.failure(Exception("Username maksimal 30 karakter."))
            }

            // Check if username already exists in users collection
            val existingUser = firestore.collection("users")
                .whereEqualTo("usernameLower", trimmedUsername.lowercase())
                .limit(1)
                .get()
                .await()

            if (!existingUser.isEmpty) {
                return Result.failure(Exception("Username '$trimmedUsername' sudah dipakai!"))
            }

            // Create Firebase Auth user
            val authResult = firebaseAuth.signInAnonymously().await()
            val user = authResult.user ?: throw Exception("Failed to create user")

            // Create user profile in Firestore
            val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

            val userProfile = mapOf(
                "uid" to user.uid,
                "userId" to user.uid,
                "username" to trimmedUsername,
                "usernameLower" to trimmedUsername.lowercase(), // For case-insensitive search
                "displayName" to trimmedDisplayName,
                "authType" to "username",
                "bio" to "",
                "email" to null,
                "favoriteKonoSubaCharacter" to "",
                "favoriteQuote" to "",
                "profilePicture" to null,
                "profileVersion" to 1,
                "partyRole" to "Newbie Adventurer",
                "chaosLevel" to 1,
                "chaosEntries" to 0,
                "chaosEntriesCount" to 0,
                "totalEntries" to 0,
                "dayStreak" to 0,
                "longestStreak" to 0,
                "streakDays" to 0,
                "achievements" to emptyList<String>(),
                "supportGiven" to 0,
                "supportReceived" to 0,
                "totalSupportGiven" to 0,
                "totalSupportReceived" to 0,
                "totalSupportsGiven" to 0,
                "totalSupportsReceived" to 0,
                "isActive" to true,
                "isAnonymous" to false,
                "settings" to mapOf(
                    "notificationsEnabled" to true,
                    "konoSubaQuotesEnabled" to true,
                    "anonymousMode" to false,
                    "reminderTime" to "20:00",
                    "shareByDefault" to false,
                    "theme" to "system",
                    "showChaosLevel" to true
                ),
                "createdAt" to currentDate,
                "joinDate" to currentDate,
                "lastActiveAt" to currentDate,
                "lastLogin" to currentDate,
                "lastLoginDate" to currentDate
            )

            firestore.collection("users")
                .document(user.uid)
                .set(userProfile)
                .await()

            updateUserPreferences(user.uid, trimmedUsername, trimmedDisplayName, "username")

            Timber.d("🎉 Registration successful! UID: ${user.uid}")
            Result.success(user)

        } catch (e: Exception) {
            Timber.e(e, "❌ Registration failed")
            val errorMessage = when {
                e.message?.contains("already exists") == true -> "Username sudah dipakai!"
                e.message?.contains("network") == true -> "Koneksi bermasalah. Cek internet!"
                else -> "Registrasi gagal: ${e.message}"
            }
            Result.failure(Exception(errorMessage))
        }
    }

    /**
     * Login with username - Simple lookup in users collection
     */
    suspend fun loginWithUsername(username: String): Result<FirebaseUser> {
        return try {
            val trimmedUsername = username.trim()

            Timber.d("🏠 LOGIN with username: '$trimmedUsername'")

            if (trimmedUsername.isBlank()) {
                return Result.failure(Exception("Username tidak boleh kosong!"))
            }

            // Find user by username in users collection
            val userQuery = firestore.collection("users")
                .whereEqualTo("usernameLower", trimmedUsername.lowercase())
                .limit(1)
                .get()
                .await()

            if (userQuery.isEmpty) {
                return Result.failure(Exception("Username '$trimmedUsername' tidak ditemukan!"))
            }

            val userDoc = userQuery.documents[0]
            val userData = userDoc.data ?: throw Exception("User data not found")
            val userId = userDoc.id

            // Get the custom token using Cloud Function (if you still want to use it)
            // OR create a custom token directly here
            val customToken = try {
                // Try using Cloud Function first
                val data: Map<String, Any> = mapOf(
                    "username" to trimmedUsername,
                    "isRegistration" to false
                )

                val result = functions
                    .getHttpsCallable("generateCustomToken")
                    .call(data)
                    .await()

                val customTokenData = result.data as? Map<*, *>
                    ?: throw Exception("Cloud Function response is null")

                customTokenData["customToken"] as? String
                    ?: throw Exception("Custom token missing from response")
            } catch (e: Exception) {
                Timber.w("Cloud Function failed, fallback to direct auth: ${e.message}")
                // Fallback: Sign in anonymously and update the auth user
                val authResult = firebaseAuth.signInAnonymously().await()
                val tempUser = authResult.user ?: throw Exception("Failed to create auth session")

                // Update the anonymous user with profile info
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(userData["displayName"] as? String ?: trimmedUsername)
                    .build()
                tempUser.updateProfile(profileUpdates).await()

                // Update preferences and return success
                updateUserPreferences(
                    userId = tempUser.uid,
                    username = trimmedUsername,
                    displayName = userData["displayName"] as? String ?: trimmedUsername,
                    authType = "username"
                )

                Timber.d("🎉 Login successful (fallback)! UID: ${tempUser.uid}")
                return Result.success(tempUser)
            }

            // Sign in with custom token
            val authResult = firebaseAuth.signInWithCustomToken(customToken).await()
            val user = authResult.user ?: throw Exception("Sign in failed after getting token")

            // Update last login
            firestore.collection("users")
                .document(userId)
                .update(mapOf(
                    "lastActiveAt" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
                    "lastLogin" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
                    "lastLoginDate" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                ))
                .await()

            updateUserPreferences(
                userId = userId,
                username = trimmedUsername,
                displayName = userData["displayName"] as? String ?: trimmedUsername,
                authType = "username"
            )

            Timber.d("🎉 Login successful! UID: ${user.uid}")
            Result.success(user)

        } catch (e: Exception) {
            Timber.e(e, "❌ Login failed")
            val errorMessage = when {
                e.message?.contains("not found") == true -> "Username tidak ditemukan!"
                e.message?.contains("network") == true -> "Koneksi bermasalah. Cek internet!"
                else -> "Login gagal: ${e.message}"
            }
            Result.failure(Exception(errorMessage))
        }
    }

    /**
     * Register with email - Create directly in users collection
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
     * Login with email
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
     * Register anonymous user
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

    private suspend fun createUserProfile(
        userId: String,
        username: String?,
        displayName: String,
        email: String?,
        authType: String
    ): Result<Unit> {
        return try {
            val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

            val userProfile = mapOf<String, Any?>(
                "uid" to userId,
                "userId" to userId,
                "username" to username,
                "usernameLower" to username?.lowercase(), // For case-insensitive search
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

            Timber.d("✅ User profile created for: $userId")
            Result.success(Unit)

        } catch (e: Exception) {
            Timber.e(e, "❌ Error creating user profile")
            Result.failure(e)
        }
    }

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

    suspend fun checkUsernameAvailability(username: String): Boolean {
        return try {
            val trimmedUsername = username.trim()
            if (trimmedUsername.isBlank()) return false

            val document = firestore.collection("users")
                .whereEqualTo("usernameLower", trimmedUsername.lowercase())
                .limit(1)
                .get()
                .await()

            document.isEmpty
        } catch (e: Exception) {
            Timber.e(e, "❌ Error checking username availability")
            false
        }
    }

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

    suspend fun deleteAccount(): Result<Unit> {
        return try {
            val user = currentUser ?: throw Exception("Tidak ada user yang login")
            val userId = user.uid

            // Delete from Firestore users collection only
            firestore.collection("users").document(userId).delete().await()

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

    fun generateRandomUsername(): String {
        val adjectives = listOf("Epic", "Chaos", "Brave", "Wild", "Cool", "Swift", "Bold", "Lucky")
        val nouns = listOf("Adventurer", "Hero", "Warrior", "Mage", "Explorer", "Knight", "Rogue", "Wizard")
        val number = (1000..9999).random()
        return "${adjectives.random()}${nouns.random()}$number"
    }
}