// File: app/src/main/java/com/dailychaos/project/data/remote/firebase/FirebaseAuthService.kt
package com.dailychaos.project.data.remote.firebase

import com.dailychaos.project.preferences.UserPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions // <-- IMPORT INI
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
import timber.log.Timber // <-- IMPORT INI UNTUK LOGGING

/**
 * Firebase Authentication Service
 * "Bahkan petualang anonim butuh registrasi party yang benar!"
 */
@Singleton
class FirebaseAuthService @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userPreferences: UserPreferences,
    private val functions: FirebaseFunctions // <-- PASTIKAN INI DI-INJECT DI KONSTRUKTOR
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
            // Validasi username
            val validationResult = validateUsername(username)
            if (!validationResult.isSuccess) {
                Timber.e("FirebaseAuthService: Username validation failed: ${validationResult.exceptionOrNull()?.message}")
                return Result.failure(validationResult.exceptionOrNull()!!)
            }

            // Cek ketersediaan username di Firestore (melalui Cloud Function jika perlu,
            // tapi saat ini Anda punya fungsi `checkUsernameAvailability` yang langsung ke Firestore)
            val isAvailable = checkUsernameAvailability(username)
            if (!isAvailable) {
                Timber.e("FirebaseAuthService: Username '$username' is not available.")
                return Result.failure(Exception("Username '$username' sudah dipakai petualang lain! Coba '${username}SangPahlawan'."))
            }

            // Panggil Cloud Function untuk mendapatkan Custom Token untuk user baru ini
            // Fungsi ini akan membuat user di Firebase Auth (jika belum ada UID yang terkait dengan username ini)
            // Atau mengembalikan token untuk UID yang sudah ada jika username sudah punya UID di 'usernames'
            // Kita asumsikan registerWithUsername di AuthRepository akan memanggil ini dan sudah siap di backend.
            // Saat ini, `registerWithUsername` Anda di AuthRepository langsung ke Firebase Auth Service.
            // Jadi, kita akan panggil Cloud Function untuk membuat token.

            Timber.d("FirebaseAuthService: Calling Cloud Function 'generateCustomToken' for registration username: $username")
            val data = hashMapOf("username" to username)
            val result = functions
                .getHttpsCallable("generateCustomToken")
                .call(data)
                .await()

            val customTokenData = result.data as? Map<*, *>
            val tokenString = customTokenData?.get("customToken") as? String
                ?: throw Exception("Gagal mendapatkan token kustom untuk registrasi dari Guild.")

            // Gunakan Custom Token untuk login/sign up ke Firebase Auth di perangkat.
            val authResult = firebaseAuth.signInWithCustomToken(tokenString).await()
            val user = authResult.user ?: throw Exception("Pendaftaran ke Guild gagal setelah token. Coba lagi!")
            Timber.d("FirebaseAuthService: User signed in/registered with custom token. UID: ${user.uid}")

            // Buat profil pengguna di Firestore
            val profileResult = createUserProfile(
                userId = user.uid,
                username = username,
                displayName = displayName.ifBlank { username },
                email = null, // Untuk register username, email bisa null
                authType = "username"
            )

            if (profileResult.isSuccess) {
                // Update user preferences
                userPreferences.setUserId(user.uid)
                userPreferences.setAnonymousUsername(username) // Asumsi ini digunakan untuk username akun anonim
                userPreferences.setUsername(username) // Simpan juga username di pref
                userPreferences.setDisplayName(displayName.ifBlank { username })
                userPreferences.setAuthType("username") // Tandai sebagai login username

                Timber.d("FirebaseAuthService: User profile created and preferences updated for new username: ${user.uid}")
                Result.success(user)
            } else {
                Timber.e("FirebaseAuthService: Failed to create user profile for ${user.uid}. Deleting user.")
                user.delete().await()
                Result.failure(profileResult.exceptionOrNull() ?: Exception("Gagal membuat profil adventurer-mu. Ini lebih merepotkan dari mengurus Aqua."))
            }
        } catch (e: FirebaseNetworkException) {
            Timber.e(e, "FirebaseAuthService: Network error during registerWithUsername.")
            Result.failure(Exception("Koneksi ke Guild (server) terputus! Mungkin ada serangan Destroyer di dekat sini. Cek koneksimu."))
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
                userPreferences.setAuthType("email") // Tandai sebagai login email

                Timber.d("FirebaseAuthService: User profile created and preferences updated for email user: ${user.uid}")
                Result.success(user)
            } else {
                Timber.e("FirebaseAuthService: Failed to create user profile for ${user.uid}. Deleting user.")
                user.delete().await()
                Result.failure(profileResult.exceptionOrNull() ?: Exception("Gagal membuat profil adventurer-mu. Ini lebih merepotkan dari mengurus Aqua."))
            }
        } catch (e: FirebaseAuthUserCollisionException) {
            Timber.e(e, "FirebaseAuthService: Email collision during registerWithEmail.")
            Result.failure(Exception("Email ini sudah terdaftar di party lain. Kalau itu kamu, coba login saja!"))
        } catch (e: FirebaseAuthWeakPasswordException) {
            Timber.e(e, "FirebaseAuthService: Weak password during registerWithEmail.")
            Result.failure(Exception("Password-mu terlalu lemah! Bahkan goblin bisa menebaknya. Coba yang lebih kuat."))
        } catch (e: FirebaseNetworkException) {
            Timber.e(e, "FirebaseAuthService: Network error during registerWithEmail.")
            Result.failure(Exception("Koneksi ke Guild (server) terputus! Mungkin ada serangan Destroyer di dekat sini. Cek koneksimu."))
        } catch (e: Exception) {
            Timber.e(e, "FirebaseAuthService: Unexpected error during registerWithEmail.")
            Result.failure(Exception("Terjadi error tak terduga saat registrasi. Ini pasti ulah dewi tak berguna itu."))
        }
    }


    /**
     * Login with username (for username-based auth)
     * "Selamat datang kembali, petualang! Ayo kita cari profilmu."
     */
    // Ini adalah fungsi yang AKAN kita ubah untuk menggunakan Cloud Function
    // Ganti implementasi yang lama ini dengan yang memanggil Cloud Function.
    // Pastikan tipe kembaliannya juga FirebaseUser
    suspend fun loginWithUsername(username: String): Result<FirebaseUser> { // <-- Tipe kembalian ke FirebaseUser
        Timber.d("FirebaseAuthService: Attempting to login with username via Cloud Function: $username")
        return try {
            // Step 1: Panggil Cloud Function untuk mendapatkan Custom Token
            val data = hashMapOf("username" to username)
            Timber.d("FirebaseAuthService: Calling Cloud Function 'generateCustomToken' with username: $username")
            val result = functions
                .getHttpsCallable("generateCustomToken") // Nama fungsi Cloud Function Anda
                .call(data)
                .await()

            val customTokenData = result.data as? Map<*, *>
            val tokenString = customTokenData?.get("customToken") as? String
                ?: throw Exception("Gagal mendapatkan token kustom dari Guild. (Token is null or not String)")

            Timber.d("FirebaseAuthService: Received custom token. Signing in with custom token.")
            // Step 2: Gunakan Custom Token untuk login ke Firebase Auth di perangkat.
            val authResult = firebaseAuth.signInWithCustomToken(tokenString).await()
            val user = authResult.user ?: throw Exception("Gagal masuk dengan token petualang. (FirebaseUser is null)")

            Timber.d("FirebaseAuthService: Successfully signed in with custom token. User UID: ${user.uid}")

            // Sekarang Firebase Auth memiliki sesi aktif, Anda bisa memuat profil.
            // PENTING: Panggil getUserProfile(user.uid) untuk mendapatkan data Map<String, Any>
            val profileResult = getUserProfile(user.uid) // <-- Panggil getUserProfile dengan UID
            if (profileResult.isSuccess) {
                val profileData = profileResult.getOrThrow()
                Timber.d("FirebaseAuthService: User profile found for UID: ${user.uid}")
                userPreferences.setUserId(user.uid)
                userPreferences.setUsername(username) // Simpan username yang digunakan
                userPreferences.setAuthType("username") // Tandai sebagai login username
                userPreferences.setDisplayName(profileData["displayName"] as? String ?: username) // Update display name
                profileData["email"]?.let { userPreferences.setUserEmail(it as String) }

                Result.success(user) // Mengembalikan FirebaseUser yang terautentikasi
            } else {
                Timber.e("FirebaseAuthService: User profile not found for UID: ${user.uid} after custom token sign-in. Deleting user.")
                user.delete().await() // Hapus user dari Firebase Auth jika profil tidak lengkap
                Result.failure(Exception("Profil adventurer ini hilang setelah diundang ke party. Silakan coba lagi atau daftar."))
            }
        } catch (e: FirebaseNetworkException) {
            Timber.e(e, "FirebaseAuthService: Network error during username login.")
            Result.failure(Exception("Koneksi ke Guild (server) terputus. Cek koneksimu."))
        } catch (e: Exception) {
            Timber.e(e, "FirebaseAuthService: Unknown error during username login: ${e.message}")
            // Anda bisa menambahkan penanganan error spesifik dari Cloud Function di sini
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
            // Panggil getUserProfile(user.uid) karena kita punya UID sekarang
            val profileResult = getUserProfile(user.uid) // <-- Gunakan getUserProfile(userId: String)
            if (profileResult.isSuccess) {
                val profile = profileResult.getOrThrow()
                userPreferences.setUserId(user.uid)
                userPreferences.setUserEmail(email)
                userPreferences.setDisplayName(profile["displayName"] as? String ?: "User")
                userPreferences.setAuthType("email") // Tandai sebagai login email
                profile["username"]?.let { userPreferences.setUsername(it as String) } // Simpan username juga jika ada

                Timber.d("FirebaseAuthService: User profile loaded and preferences updated for email user: ${user.uid}")
            } else {
                Timber.w("FirebaseAuthService: User profile not found for email user ${user.uid}. Preferences might be incomplete.")
                // Jangan gagal login hanya karena profil tidak ditemukan, tapi log peringatan
            }

            Result.success(user)
        } catch (e: FirebaseAuthInvalidUserException) {
            Timber.e(e, "FirebaseAuthService: Invalid user during loginWithEmail.")
            Result.failure(Exception("Adventurer dengan email ini tidak ditemukan. Mungkin mau coba Register?"))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Timber.e(e, "FirebaseAuthService: Invalid credentials during loginWithEmail.")
            Result.failure(Exception("Password salah! Coba lagi, bahkan Darkness pun kadang lupa shield-nya."))
        } catch (e: FirebaseNetworkException) {
            Timber.e(e, "FirebaseAuthService: Network error during loginWithEmail.")
            Result.failure(Exception("Koneksi ke guild hall (server) gagal. Cek koneksi internetmu!"))
        } catch (e: Exception) {
            Timber.e(e, "FirebaseAuthService: Unexpected error during loginWithEmail.")
            Result.failure(Exception("Terjadi error yang tidak diketahui. Bahkan Aqua pun bingung!"))
        }
    }

    /**
     * Create user profile in Firestore
     */
    private suspend fun createUserProfile(
        userId: String,
        username: String?, // Bisa null untuk email auth
        displayName: String,
        email: String?,
        authType: String
    ): Result<Unit> {
        return try {
            Timber.d("FirebaseAuthService: createUserProfile started for userId: $userId, authType: $authType")
            val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

            val userProfile = hashMapOf<String, Any?>( // Gunakan Any? untuk mendukung null
                "userId" to userId,
                "username" to username, // Ini bisa null
                "displayName" to displayName,
                "email" to email, // Ini bisa null
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

            if (!username.isNullOrBlank()) { // Hanya simpan ke 'usernames' jika username tidak kosong
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
     * Ini akan menggunakan firebaseAuth.currentUser.uid
     */
    suspend fun getUserProfile(): Result<Map<String, Any>> {
        Timber.d("FirebaseAuthService: getUserProfile() called (no args).")
        val user = currentUser ?: run {
            Timber.e("FirebaseAuthService: No current user for getUserProfile() (no args).")
            return Result.failure(Exception("Siapa kamu? Kamu harus login dulu untuk melihat profil."))
        }
        return getUserProfile(user.uid) // Delegasikan ke fungsi yang ada
    }

    /**
     * Get user profile from Firestore by userId (takes userId arg)
     */
    suspend fun getUserProfile(userId: String): Result<Map<String, Any>> {
        Timber.d("FirebaseAuthService: getUserProfile(userId: $userId) called.")
        return try {
            val document = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            if (document.exists()) {
                val data = document.data ?: throw Exception("Datanya ada, tapi isinya kosong. Ini pasti kerjaan sihir ilusi.")
                Timber.d("FirebaseAuthService: Profile data found for userId: $userId")
                Result.success(data)
            } else {
                Timber.w("FirebaseAuthService: Profile not found for userId: $userId")
                Result.failure(Exception("Profil petualang ini kosong, seperti dompet Kazuma di akhir bulan."))
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

            // Optional: Hapus juga entri di koleksi 'usernames' jika ada
            val usernameDoc = firestore.collection("usernames").whereEqualTo("userId", userId).get().await().documents.firstOrNull()
            usernameDoc?.reference?.delete()?.await()
            Timber.d("FirebaseAuthService: Username entry deleted (if existed) for userId: $userId")

            // Hapus dokumen user dari Firestore
            firestore.collection("users")
                .document(userId)
                .delete()
                .await()
            Timber.d("FirebaseAuthService: User document deleted from Firestore for userId: $userId")

            // Hapus user dari Firebase Auth
            user.delete().await()
            Timber.d("FirebaseAuthService: User deleted from Firebase Auth: $userId")

            // Bersihkan preferences lokal
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
     * Validate username format and requirements (digunakan internal)
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
     * Check username availability (digunakan internal)
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
}