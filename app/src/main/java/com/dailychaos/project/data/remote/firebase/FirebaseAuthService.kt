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
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

/**
 * Firebase Authentication Service
 * "Bahkan petualang anonim butuh registrasi party yang benar!"
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
     * "Selamat datang di party! Pilih nama petualangmu dengan bijak!"
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
                return Result.failure(Exception("Username '$username' sudah dipakai petualang lain! Coba '${username}SangPahlawan'."))
            }

            // Sign in anonymously first
            val authResult = firebaseAuth.signInAnonymously().await()
            val user = authResult.user ?: throw Exception("Pendaftaran ke Guild gagal. Mungkin servernya lagi diserang pasukan Raja Iblis. Coba lagi!")

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
                Result.failure(profileResult.exceptionOrNull() ?: Exception("Gagal membuat profil adventurer-mu. Ini lebih merepotkan dari mengurus Aqua."))
            }
        } catch (e: FirebaseNetworkException) {
            Result.failure(Exception("Koneksi ke Guild (server) terputus! Mungkin ada serangan Destroyer di dekat sini. Cek koneksimu."))
        } catch (e: Exception) {
            Result.failure(Exception("Terjadi error tak terduga! Ini lebih kacau dari saat Darkness jadi tameng. Coba lagi sebentar."))
        }
    }

    /**
     * Register with email and password
     * "Bergabunglah dengan party menggunakan kredensial lengkap!"
     */
    suspend fun registerWithEmail(email: String, password: String, displayName: String): Result<FirebaseUser> {
        return try {
            // Create user with email/password
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user ?: throw Exception("Registrasi via email gagal. Mungkin ada sihir aneh yang menghalangi.")

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
                Result.failure(profileResult.exceptionOrNull() ?: Exception("Gagal membuat profil adventurer-mu. Ini lebih merepotkan dari mengurus Aqua."))
            }
        } catch (e: FirebaseAuthUserCollisionException) {
            Result.failure(Exception("Email ini sudah terdaftar di party lain. Kalau itu kamu, coba login saja!"))
        } catch (e: FirebaseAuthWeakPasswordException) {
            Result.failure(Exception("Password-mu terlalu lemah! Bahkan goblin bisa menebaknya. Coba yang lebih kuat."))
        } catch (e: FirebaseNetworkException) {
            Result.failure(Exception("Koneksi ke Guild (server) terputus! Mungkin ada serangan Destroyer di dekat sini. Cek koneksimu."))
        } catch (e: Exception) {
            Result.failure(Exception("Terjadi error tak terduga saat registrasi. Ini pasti ulah dewi tak berguna itu."))
        }
    }

    /**
     * Login with username (for username-based auth)
     * "Selamat datang kembali, petualang! Ayo kita cari profilmu."
     */
    suspend fun loginWithUsername(username: String): Result<Map<String, Any>> {
        return try {
            // Step 1: Cari username di collection 'usernames' untuk dapat UID_ASLI.
            val usernameDoc = firestore.collection("usernames")
                .document(username.lowercase())
                .get()
                .await()

            if (!usernameDoc.exists()) {
                return Result.failure(Exception("Adventurer dengan nama '$username' tidak ditemukan di Guild. Yakin tidak salah tulis?"))
            }

            val originalUserId = usernameDoc.getString("userId")
                ?: return Result.failure(Exception("Data UID untuk adventurer ini korup. Coba lapor ke Guild Master."))

            // Step 2: Gunakan UID_ASLI untuk mengambil data profil dari collection 'users'.
            val profileDoc = firestore.collection("users")
                .document(originalUserId)
                .get()
                .await()

            if (profileDoc.exists()) {
                // Tetap sign-in anonymously untuk mendapatkan sesi aktif.
                // Sesi ini hanya "tiket masuk", identitas aslinya adalah data profil yang kita fetch.
                firebaseAuth.signInAnonymously().await()

                val profileData = profileDoc.data ?: throw Exception("Data profil petualangmu hilang! Ini pasti ulah Vanir.")
                Result.success(profileData)
            } else {
                Result.failure(Exception("Profil untuk adventurer ini tidak ditemukan."))
            }
        } catch (e: FirebaseNetworkException) {
            Result.failure(Exception("Koneksi ke Guild (server) terputus! Mungkin ada serangan Destroyer di dekat sini. Cek koneksimu."))
        } catch (e: Exception) {
            Result.failure(Exception("Terjadi error tak terduga! Ini lebih kacau dari saat Darkness jadi tameng. Coba lagi sebentar."))
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
        } catch (e: FirebaseAuthInvalidUserException) {
            Result.failure(Exception("Adventurer dengan email ini tidak ditemukan. Mungkin mau coba Register?"))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(Exception("Password salah! Coba lagi, bahkan Darkness pun kadang lupa shield-nya."))
        } catch (e: FirebaseNetworkException) {
            Result.failure(Exception("Koneksi ke guild hall (server) gagal. Cek koneksi internetmu!"))
        } catch (e: Exception) {
            Result.failure(Exception("Terjadi error yang tidak diketahui. Bahkan Aqua pun bingung!"))
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

            firestore.collection("users")
                .document(userId)
                .set(userProfile)
                .await()

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
     */
    suspend fun getUserProfile(): Result<Map<String, Any>> {
        return try {
            val user = currentUser ?: throw Exception("Siapa kamu? Kamu harus login dulu untuk melihat profil.")

            val document = firestore.collection("users")
                .document(user.uid)
                .get()
                .await()

            if (document.exists()) {
                val data = document.data ?: throw Exception("Datanya ada, tapi isinya kosong. Ini pasti kerjaan sihir ilusi.")
                Result.success(data)
            } else {
                Result.failure(Exception("Profil petualang ini kosong, seperti dompet Kazuma di akhir bulan."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get user profile from Firestore by userId
     */
    suspend fun getUserProfile(userId: String): Result<Map<String, Any>> {
        return try {
            val document = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            if (document.exists()) {
                val data = document.data ?: throw Exception("Datanya ada, tapi isinya kosong. Ini pasti kerjaan sihir ilusi.")
                Result.success(data)
            } else {
                Result.failure(Exception("Profil petualang ini kosong, seperti dompet Kazuma di akhir bulan."))
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
            val user = currentUser ?: throw Exception("Siapa kamu? Kamu harus login dulu untuk mengubah profil.")

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
            val user = currentUser ?: throw Exception("Tidak bisa menghapus akun yang tidak login!")
            val userId = user.uid

            firestore.collection("users")
                .document(userId)
                .delete()
                .await()

            user.delete().await()

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
            username.length < 3 -> Result.failure(Exception("Username minimal 3 karakter, ya. Biar nggak kayak nama slime."))
            username.length > 20 -> Result.failure(Exception("Username maksimal 20 karakter. Ini nama petualang, bukan judul light novel."))
            !username.matches(Regex("^[a-zA-Z0-9_]+$")) -> Result.failure(Exception("Username hanya boleh huruf, angka, dan _. Jangan pakai sihir aneh-aneh."))
            username.startsWith("_") || username.endsWith("_") -> Result.failure(Exception("Underscore jangan di depan atau belakang, nanti tersandung pas berpetualang."))
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