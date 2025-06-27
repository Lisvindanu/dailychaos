// app/src/main/java/com/dailychaos/project/ChaosApplication.kt
package com.dailychaos.project

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.dailychaos.project.util.Constants
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * Daily Chaos Application Class
 *
 * "Seperti party Kazuma yang chaos tapi tetap punya tujuan,
 * aplikasi ini juga butuh koordinasi yang baik!"
 */
@HiltAndroidApp
class ChaosApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Inisialisasi Timber untuk logging (PENTING: Lakukan ini pertama jika menggunakan Timber)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Inisialisasi Firebase App Check
        initializeFirebaseAppCheck()

        // Inisialisasi komponen aplikasi lainnya
        initializeApp()
        createNotificationChannels()
        initializeCrashReporting()
    }

    private fun initializeFirebaseAppCheck() {
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        if (BuildConfig.DEBUG) {
            // Setel token debug dari BuildConfig jika tersedia
            if (BuildConfig.FIREBASE_APP_CHECK_DEBUG_TOKEN.isNotBlank()) {
                System.setProperty(
                    "firebase.appcheck.debug_token",
                    BuildConfig.FIREBASE_APP_CHECK_DEBUG_TOKEN
                )
                Timber.d("Firebase App Check debug token set from BuildConfig: ${BuildConfig.FIREBASE_APP_CHECK_DEBUG_TOKEN}")
            } else {
                Timber.w("Firebase App Check debug token is empty. Please set FIREBASE_APP_CHECK_DEBUG_TOKEN in local.properties")
            }

            firebaseAppCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
            Timber.d("Firebase App Check initialized with DebugAppCheckProviderFactory.")
        } else {
            firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance()
            )
            Timber.d("Firebase App Check initialized with PlayIntegrityAppCheckProviderFactory.")
        }
    }

    private fun initializeApp() {
        // Log aplikasi start (hanya di debug mode)
        if (BuildConfig.DEBUG) {
            Timber.d("🌪️ Daily Chaos Application Started - Ready to embrace the chaos!")
            Timber.d("📱 Version: ${BuildConfig.VERSION_NAME}")
            Timber.d("🔧 Debug Mode: ${BuildConfig.DEBUG}")
            Timber.d("🔥 Firebase Project: ${BuildConfig.FIREBASE_PROJECT_ID}")
            if (BuildConfig.FIREBASE_APP_CHECK_DEBUG_TOKEN.isNotBlank()) {
                Timber.d("🔐 Firebase App Check Debug Token: ${BuildConfig.FIREBASE_APP_CHECK_DEBUG_TOKEN}")
            }
        }

        // Setup global exception handler untuk debugging
        if (BuildConfig.DEBUG) {
            Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
                Timber.e(exception, "Uncaught exception in thread ${thread.name}")
            }
        }

        Timber.d("✅ Daily Chaos initialized successfully!")
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val generalChannel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_GENERAL,
                "General Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General app notifications"
                enableVibration(true)
                setShowBadge(true)
            }

            val supportChannel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_SUPPORT,
                "Support Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications when you receive support from the community"
                enableVibration(true)
                setShowBadge(true)
            }

            val syncChannel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_SYNC,
                "Sync Status",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Data synchronization status"
                enableVibration(false)
                setShowBadge(false)
            }

            notificationManager.createNotificationChannels(listOf(generalChannel, supportChannel, syncChannel))

            if (BuildConfig.DEBUG) {
                Timber.d("📱 Notification channels created successfully")
            }
        }
    }

    private fun initializeCrashReporting() {
        // Aktifkan koleksi data Crashlytics hanya untuk build non-debug
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)

        if (BuildConfig.DEBUG) {
            Timber.d("🔍 Crash reporting setup (collection disabled in debug)")
        } else {
            Timber.d("🚀 Crash reporting enabled for release build.")
        }
    }
}