package com.dailychaos.project.presentation.ui.screen.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailychaos.project.domain.usecase.auth.AuthUseCases
import com.dailychaos.project.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.util.Log // Impor untuk logging standar Android
import com.google.firebase.functions.ktx.functions // Impor untuk Firebase Functions
import com.google.firebase.ktx.Firebase // Impor untuk Firebase
import kotlinx.coroutines.tasks.await

import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val authUseCases: AuthUseCases
) : ViewModel() {

    // Tag untuk logging
    private val TAG = "SplashViewModel"

    private val _navigationEvent = MutableSharedFlow<SplashDestination>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            runMigrationOnce() // Jalankan pengecekan migrasi terlebih dahulu
            decideNextScreen()
        }
    }

    private suspend fun runMigrationOnce() {
        val migrationCompleted = userPreferences.isMigrationCompleted.first()
        Log.d(TAG, "SplashViewModel: isMigrationCompleted preference value: $migrationCompleted") // Log untuk memeriksa nilai preferensi

        if (!migrationCompleted) {
            Log.d(TAG, "🚀 Memulai panggilan fungsi migrasi: runUsernameLowercaseMigration (dari startup)")
            try {
                val functions = Firebase.functions("us-central1") // Ganti dengan region Cloud Function Anda

                val result = functions
                    .getHttpsCallable("runUsernameLowercaseMigration") // Nama fungsi callable Anda
                    .call() // Panggil tanpa payload data
                    .await() // Menggunakan await() untuk gaya coroutine

                val resultData = result?.data
                Log.d(TAG, "✅ Panggilan fungsi migrasi berhasil (dari startup). Status: $resultData")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Panggilan fungsi migrasi gagal (dari startup): ${e.message}", e) // Log error dengan exception
            } finally {
                userPreferences.setMigrationCompleted(true)
                Log.d(TAG, "Flag penyelesaian migrasi diatur ke true (dari startup).")
            }
        } else {
            Log.d(TAG, "Migrasi sudah selesai, dilewati (dari startup).")
        }
    }

    // Fungsi baru untuk dipanggil dari tombol di UI (mengabaikan flag completion untuk testing)
    fun forceRunMigration() {
        viewModelScope.launch {
            Log.d(TAG, "🚀 Memulai PANGGILAN MANUAL fungsi migrasi: runUsernameLowercaseMigration")
            try {
                val functions = Firebase.functions("us-central1") // Ganti dengan region Cloud Function Anda

                val result = functions
                    .getHttpsCallable("runUsernameLowercaseMigration") // Nama fungsi callable Anda
                    .call() // Panggil tanpa payload data
                    .await() // Menggunakan await() untuk gaya coroutine

                val resultData = result?.data
                Log.d(TAG, "✅ PANGGILAN MANUAL fungsi migrasi berhasil. Status: $resultData")
            } catch (e: Exception) {
                Log.e(TAG, "❌ PANGGILAN MANUAL fungsi migrasi gagal: ${e.message}", e) // Log error dengan exception
            } finally {
                // Anda bisa memilih untuk TIDAK mengatur `setMigrationCompleted(true)` di sini
                // jika Anda ingin tombol ini selalu dapat dipicu untuk pengujian berulang
                // tanpa harus menghapus data aplikasi. Namun, perlu diingat ini hanya untuk debug.
                Log.d(TAG, "Panggilan migrasi manual selesai.")
            }
        }
    }

    private fun decideNextScreen() {
        viewModelScope.launch {
            Log.d(TAG, "SplashViewModel: Delaying for 2500ms before deciding next screen.")
            delay(2500)

            val isLoggedIn = authUseCases.isAuthenticated()
            val onboardingCompleted = userPreferences.onboardingCompleted.first()

            val destination = when {
                !onboardingCompleted -> SplashDestination.Onboarding
                !isLoggedIn -> SplashDestination.Auth
                else -> SplashDestination.Home
            }
            Log.d(TAG, "SplashViewModel: Decided next screen: $destination")
            _navigationEvent.emit(destination)
        }
    }
}