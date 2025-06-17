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
 * Firebase Authentication Service
 * "Bahkan petualang anonim butuh registrasi party yang benar!"
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
     * Register with username (anonymous auth + username validation)
     * "Selamat datang di party! Pilih nama petualangmu dengan bijak!"
     */
    suspend fun registerWithUsername(username: String, displayName: String): Result<FirebaseUser> {
        return try {
            Timber.d("FirebaseAuthService: registerWithUsername started. Username: $username, DisplayName: $displayName")

            // FIXED: Minimal validation only - let Cloud Function handle complex rules
            if (username.isBlank()) {
                return Result.failure(Exception("Username tidak boleh kosong!"))
            }

            if (username.length < 3) {
                return Result.failure(Exception("Username minimal 3 karakter."))
            }

            if (username.length > 30) {
                return Result.failure(Exception("Username terlalu panjang."))
            }

            // Check availability locally first
            val isAvailable = checkUsernameAvailability(username)
            if (!isAvailable) {
                Timber.e("FirebaseAuthService: Username '$username' is not available.")
                return Result.failure(Exception("Username '$username' sudah dipakai petualang lain! Coba username lain."))
            }

            // Call Cloud Function for REGISTRATION
            Timber.d("FirebaseAuthService: Calling Cloud Function for REGISTRATION with username: $username")
            val data = hashMapOf(
                "username" to username,
                "isRegistration" to true  // IMPORTANT: Flag untuk registrasi
            )

            val result = functions
                .getHttpsCallable("generateCustomToken")
                .call(data)
                .await()

            val customTokenData = result.data as? Map<*, *>
            val tokenString = customTokenData?.get("customToken") as? String
                ?: throw Exception("Gagal mendapatkan token kustom untuk registrasi dari Guild.")

            val newUserId = customTokenData["userId"] as? String
                ?: throw Exception("Gagal mendapatkan userId dari Cloud Function.")

            // Sign in with custom token
            val authResult = firebaseAuth.signInWithCustomToken(tokenString).await()
            val user = authResult.user ?: throw Exception("Pendaftaran ke Guild gagal setelah token. Coba lagi!")
            Timber.d("FirebaseAuthService: User signed in/registered with custom token. UID: ${user.uid}")

            // Create user profile in Firestore (Cloud Function doesn't do this)
            val profileResult = createUserProfile(
                userId = newUserId,  // Use the userId from Cloud Function
                username = username,
                displayName = displayName.ifBlank { username },
                email = null,
                authType = "username"
            )

            if (profileResult.isSuccess) {
                // Update user preferences
                userPreferences.setUserId(newUserId)
                userPreferences.setAnonymousUsername(username)
                userPreferences.setUsername(username)
                userPreferences.setDisplayName(displayName.ifBlank { username })
                userPreferences.setAuthType("username")

                Timber.d("FirebaseAuthService: User profile created and preferences updated for new username: ${newUserId}")
                Result.success(user)
            } else {
                Timber.e("FirebaseAuthService: Failed to create user profile for ${newUserId}. Deleting user.")
                user.delete().await()
                Result.failure(profileResult.exceptionOrNull() ?: Exception("Gagal membuat profil adventurer-mu."))
            }
        } catch (e: FirebaseNetworkException) {
            Timber.e(e, "FirebaseAuthService: Network error during registerWithUsername.")
            Result.failure(Exception("Koneksi ke Guild (server) terputus! Cek koneksi internetmu."))
        } catch (e: Exception) {
            Timber.e(e, "FirebaseAuthService: Unexpected error during registerWithUsername.")
            Result.failure(e)
        }
    }

    /**
     * Register with email and password
     * "Bergabunglah dengan party menggunakan kredensial lengkap!"
     */
    suspend fun registerWithEmail(email: String, password: String, displayName: String): Result<FirebaseUser> {
        return try {
            Timber.d("FirebaseAuthService: registerWithEmail started. Email: $email, DisplayName: $displayName")
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user ?: throw Exception("Registrasi via email gagal. Mungkin ada sihir aneh yang menghalangi.")
            Timber.d("FirebaseAuthService: Firebase Auth user created. UID: ${user.uid}, Email: ${user.email}")

            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            user.updateProfile(profileUpdates).await()
            Timber.d("FirebaseAuthService: Firebase Auth profile updated for ${user.uid}.")

            val profileResult = createUserProfile(
                userId = user.uid,
                username = "",
                displayName = displayName,
                email = email,
                authType = "email"
            )

            if (profileResult.isSuccess) {
                userPreferences.setUserId(user.uid)
                userPreferences.setUserEmail(email)
                userPreferences.setDisplayName(displayName)
                userPreferences.setAuthType("email")

                Timber.d("FirebaseAuthService: User profile created and preferences updated for email user: ${user.uid}")
                Result.success(user)
            } else {
                Timber.e("FirebaseAuthService: Failed to create user profile for ${user.uid}. Deleting user.")
                user.delete().await()
                Result.failure(profileResult.exceptionOrNull() ?: Exception("Gagal membuat profil adventurer-mu."))
            }
        } catch (e: FirebaseAuthUserCollisionException) {
            Timber.e(e, "FirebaseAuthService: Email collision during registerWithEmail.")
            Result.failure(Exception("Email ini sudah terdaftar di party lain. Kalau itu kamu, coba login saja!"))
        } catch (e: FirebaseAuthWeakPasswordException) {
            Timber.e(e, "FirebaseAuthService: Weak password during registerWithEmail.")
            Result.failure(Exception("Password-mu terlalu lemah! Bahkan goblin bisa menebaknya. Coba yang lebih kuat."))
        } catch (e: FirebaseNetworkException) {
            Timber.e(e, "FirebaseAuthService: Network error during registerWithEmail.")
            Result.failure(Exception("Koneksi ke Guild (server) terputus! Cek koneksi internetmu."))
        } catch (e: Exception) {
            Timber.e(e, "FirebaseAuthService: Unexpected error during registerWithEmail.")
            Result.failure(Exception("Terjadi error tak terduga saat registrasi."))
        }
    }

    /**
     * Register anonymous user
     * "Untuk petualang yang ingin langsung action tanpa ribet!"
     */
    suspend fun registerAnonymous(username: String, displayName: String): Result<FirebaseUser> {
        return try {
            Timber.d("FirebaseAuthService: registerAnonymous started. Username: $username, DisplayName: $displayName")

            // Create anonymous Firebase auth
            val authResult = firebaseAuth.signInAnonymously().await()
            val user = authResult.user ?: throw Exception("Gagal membuat sesi anonymous")
            Timber.d("FirebaseAuthService: Anonymous auth created. UID: ${user.uid}")

            // Create user profile for anonymous user
            val profileResult = createUserProfile(
                userId = user.uid,
                username = username,
                displayName = displayName,
                email = null,
                authType = "anonymous"
            )

            if (profileResult.isSuccess) {
                // Update user preferences
                userPreferences.setUserId(user.uid)
                userPreferences.setAnonymousUsername(username)
                userPreferences.setUsername(username)
                userPreferences.setDisplayName(displayName)
                userPreferences.setAuthType("anonymous")

                Timber.d("FirebaseAuthService: Anonymous user profile created: ${user.uid}")
                Result.success(user)
            } else {
                Timber.e("FirebaseAuthService: Failed to create anonymous profile for ${user.uid}. Deleting user.")
                user.delete().await()
                Result.failure(profileResult.exceptionOrNull() ?: Exception("Gagal membuat profil anonymous."))
            }
        } catch (e: FirebaseNetworkException) {
            Timber.e(e, "FirebaseAuthService: Network error during registerAnonymous.")
            Result.failure(Exception("Koneksi ke server terputus. Cek koneksi internetmu."))
        } catch (e: Exception) {
            Timber.e(e, "FirebaseAuthService: Unexpected error during registerAnonymous.")
            Result.failure(e)
        }
    }

    /**
     * Login with username (for username-based auth)
     * "Selamat datang kembali, petualang! Ayo kita cari profilmu."
     */
    suspend fun loginWithUsername(username: String): Result<FirebaseUser> {
        Timber.d("FirebaseAuthService: Attempting to login with username: $username")
        return try {
            // STEP 1: Get userId from usernames collection
            Timber.d("FirebaseAuthService: Looking up username in usernames collection: $username")
            val usernameDoc = firestore.collection("usernames")
                .document(username.lowercase())
                .get()
                .await()

            if (!usernameDoc.exists()) {
                Timber.e("FirebaseAuthService: Username not found: $username")
                return Result.failure(Exception("Username '$username' tidak ditemukan. Pastikan username benar atau coba register."))
            }

            val userId = usernameDoc.data?.get("userId") as? String
            if (userId.isNullOrBlank()) {
                Timber.e("FirebaseAuthService: UserId is null for username: $username")
                return Result.failure(Exception("Data username tidak valid. Silakan hubungi support."))
            }

            Timber.d("FirebaseAuthService: Found userId: $userId for username: $username")

            // STEP 2: Verify user profile exists
            val profileResult = getUserProfile(userId)
            if (!profileResult.isSuccess) {
                Timber.e("FirebaseAuthService: User profile not found for userId: $userId")
                return Result.failure(Exception("Profil pengguna tidak ditemukan. Data mungkin corrupt."))
            }

            val profileData = profileResult.getOrThrow()
            Timber.d("FirebaseAuthService: User profile verified for userId: $userId")

            // STEP 3: Try Cloud Function approach first
            try {
                Timber.d("FirebaseAuthService: Calling Cloud Function for LOGIN with existing userId: $userId")
                val data = hashMapOf(
                    "username" to username,
                    "userId" to userId,
                    "isRegistration" to false  // IMPORTANT: Flag untuk login
                )

                val result = functions
                    .getHttpsCallable("generateCustomToken")
                    .call(data)
                    .await()

                val customTokenData = result.data as? Map<*, *>
                val tokenString = customTokenData?.get("customToken") as? String
                    ?: throw Exception("Cloud Function failed to return custom token")

                Timber.d("FirebaseAuthService: Received custom token from Cloud Function")

                // Sign in with custom token
                val authResult = firebaseAuth.signInWithCustomToken(tokenString).await()
                val user = authResult.user ?: throw Exception("Failed to sign in with custom token")

                Timber.d("FirebaseAuthService: Successfully signed in. User UID: ${user.uid}")

                // Update preferences with correct userId
                userPreferences.setUserId(userId)
                userPreferences.setUsername(username)
                userPreferences.setAuthType("username")
                userPreferences.setDisplayName(profileData["displayName"] as? String ?: username)
                profileData["email"]?.let { userPreferences.setUserEmail(it as String) }

                Timber.d("FirebaseAuthService: Login successful for username: $username, userId: $userId")
                Result.success(user)

            } catch (cloudFunctionError: Exception) {
                // FALLBACK: If Cloud Function fails, use anonymous auth as workaround
                Timber.w(cloudFunctionError, "FirebaseAuthService: Cloud Function failed, trying anonymous fallback")

                val authResult = firebaseAuth.signInAnonymously().await()
                val user = authResult.user ?: throw Exception("Anonymous fallback also failed")

                // Update preferences with the real userId from database
                userPreferences.setUserId(userId)
                userPreferences.setUsername(username)
                userPreferences.setAuthType("username")
                userPreferences.setDisplayName(profileData["displayName"] as? String ?: username)
                profileData["email"]?.let { userPreferences.setUserEmail(it as String) }

                Timber.d("FirebaseAuthService: Login successful via anonymous fallback")
                Result.success(user)
            }

        } catch (e: FirebaseNetworkException) {
            Timber.e(e, "FirebaseAuthService: Network error during username login.")
            Result.failure(Exception("Koneksi ke server terputus. Cek koneksi internet Anda."))
        } catch (e: Exception) {
            Timber.e(e, "FirebaseAuthService: Error during username login: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Login with email and password
     */
    suspend fun loginWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            Timber.d("FirebaseAuthService: loginWithEmail started. Email: $email")
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = authResult.user ?: throw Exception("Login gagal")
            Timber.d("FirebaseAuthService: Firebase Auth user logged in. UID: ${user.uid}, Email: ${user.email}")

            // Load user profile and update preferences
            val profileResult = getUserProfile(user.uid)
            if (profileResult.isSuccess) {
                val profile = profileResult.getOrThrow()
                userPreferences.setUserId(user.uid)
                userPreferences.setUserEmail(email)
                userPreferences.setDisplayName(profile["displayName"] as? String ?: "User")
                userPreferences.setAuthType("email")
                profile["username"]?.let { userPreferences.setUsername(it as String) }

                Timber.d("FirebaseAuthService: User profile loaded and preferences updated for email user: ${user.uid}")
            } else {
                Timber.w("FirebaseAuthService: User profile not found for email user ${user.uid}. Preferences might be incomplete.")
            }

            Result.success(user)
        } catch (e: FirebaseAuthInvalidUserException) {
            Timber.e(e, "FirebaseAuthService: Invalid user during loginWithEmail.")
            Result.failure(Exception("Adventurer dengan email ini tidak ditemukan. Mungkin mau coba Register?"))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Timber.e(e, "FirebaseAuthService: Invalid credentials during loginWithEmail.")
            Result.failure(Exception("Password salah! Coba lagi."))
        } catch (e: FirebaseNetworkException) {
            Timber.e(e, "FirebaseAuthService: Network error during loginWithEmail.")
            Result.failure(Exception("Koneksi ke server gagal. Cek koneksi internetmu!"))
        } catch (e: Exception) {
            Timber.e(e, "FirebaseAuthService: Unexpected error during loginWithEmail.")
            Result.failure(Exception("Terjadi error yang tidak diketahui."))
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
            Timber.d("FirebaseAuthService: createUserProfile started for userId: $userId, authType: $authType")
            val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

            val userProfile = hashMapOf<String, Any?>(
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

            firestore.collection("users")
                .document(userId)
                .set(userProfile)
                .await()
            Timber.d("FirebaseAuthService: User profile document created in Firestore for userId: $userId")

            // Create username mapping for username/anonymous auth (not for email-only)
            if (!username.isNullOrBlank() && authType != "email") {
                firestore.collection("usernames")
                    .document(username.lowercase())
                    .set(mapOf(
                        "userId" to userId,
                        "username" to username,
                        "createdAt" to currentDate,
                        "authType" to authType
                    ))
                    .await()
                Timber.d("FirebaseAuthService: Username document created in Firestore for username: $username")
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "FirebaseAuthService: Error creating user profile in Firestore for userId: $userId")
            Result.failure(e)
        }
    }

    /**
     * Get user profile from Firestore (current user, takes no arg)
     */
    suspend fun getUserProfile(): Result<Map<String, Any>> {
        Timber.d("FirebaseAuthService: getUserProfile() called (no args).")
        val user = currentUser ?: run {
            Timber.e("FirebaseAuthService: No current user for getUserProfile() (no args).")
            return Result.failure(Exception("Siapa kamu? Kamu harus login dulu untuk melihat profil."))
        }
        return getUserProfile(user.uid)
    }

    /**
     * Get user profile from Firestore by userId
     */
    suspend fun getUserProfile(userId: String): Result<Map<String, Any>> {
        Timber.d("FirebaseAuthService: getUserProfile(userId: $userId) called.")
        return try {
            val document = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            if (document.exists()) {
                val data = document.data ?: throw Exception("Datanya ada, tapi isinya kosong.")
                Timber.d("FirebaseAuthService: Profile data found for userId: $userId")
                Result.success(data)
            } else {
                Timber.w("FirebaseAuthService: Profile not found for userId: $userId")
                Result.failure(Exception("Profil petualang ini tidak ditemukan."))
            }
        } catch (e: Exception) {
            Timber.e(e, "FirebaseAuthService: Error fetching user profile for userId: $userId")
            Result.failure(e)
        }
    }

    /**
     * Update user profile
     */
    suspend fun updateUserProfile(updates: Map<String, Any>): Result<Unit> {
        return try {
            Timber.d("FirebaseAuthService: updateUserProfile called.")
            val user = currentUser ?: throw Exception("Siapa kamu? Kamu harus login dulu untuk mengubah profil.")

            firestore.collection("users")
                .document(user.uid)
                .update(updates)
                .await()
            Timber.d("FirebaseAuthService: User profile updated for userId: ${user.uid}")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "FirebaseAuthService: Error updating user profile for userId: ${currentUser?.uid}")
            Result.failure(e)
        }
    }

    /**
     * Delete user account
     */
    suspend fun deleteAccount(): Result<Unit> {
        return try {
            Timber.d("FirebaseAuthService: deleteAccount called.")
            val user = currentUser ?: throw Exception("Tidak bisa menghapus akun yang tidak login!")
            val userId = user.uid

            // Delete username entry if exists
            val usernameDoc = firestore.collection("usernames").whereEqualTo("userId", userId).get().await().documents.firstOrNull()
            usernameDoc?.reference?.delete()?.await()
            Timber.d("FirebaseAuthService: Username entry deleted (if existed) for userId: $userId")

            // Delete user document from Firestore
            firestore.collection("users")
                .document(userId)
                .delete()
                .await()
            Timber.d("FirebaseAuthService: User document deleted from Firestore for userId: $userId")

            // Delete user from Firebase Auth
            user.delete().await()
            Timber.d("FirebaseAuthService: User deleted from Firebase Auth: $userId")

            // Clear local preferences
            userPreferences.clearUserData()
            Timber.d("FirebaseAuthService: Local user data cleared.")

            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "FirebaseAuthService: Error deleting account for userId: ${currentUser?.uid}")
            Result.failure(e)
        }
    }

    /**
     * Logout user
     */
    suspend fun logout(): Result<Unit> {
        return try {
            Timber.d("FirebaseAuthService: Logout called.")
            firebaseAuth.signOut()
            userPreferences.clearUserData()
            Timber.d("FirebaseAuthService: User signed out and local data cleared.")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "FirebaseAuthService: Error during logout.")
            Result.failure(e)
        }
    }

    /**
     * Check username availability
     */
    suspend fun checkUsernameAvailability(username: String): Boolean {
        return try {
            Timber.d("FirebaseAuthService: checkUsernameAvailability for username: $username")
            val document = firestore.collection("usernames")
                .document(username.lowercase())
                .get()
                .await()

            val isAvailable = !document.exists()
            Timber.d("FirebaseAuthService: Username '$username' available: $isAvailable")
            isAvailable
        } catch (e: Exception) {
            Timber.e(e, "FirebaseAuthService: Error checking username availability.")
            false
        }
    }

    /**
     * Generate random username for anonymous users
     */
    fun generateRandomUsername(): String {
        val adjectives = listOf("Epic", "Chaos", "Brave", "Wild", "Cool", "Swift", "Bold", "Lucky")
        val nouns = listOf("Adventurer", "Hero", "Warrior", "Mage", "Explorer", "Knight", "Rogue", "Wizard")
        val number = (100..999).random()

        val adjective = adjectives.random()
        val noun = nouns.random()

        return "${adjective}${noun}${number}"
    }
}