// app/src/main/java/com/dailychaos/project/ChaosApplication.kt
package com.dailychaos.project

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.dailychaos.project.util.Constants
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

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

        // Initialize Timber for logging (IMPORTANT: Do this first if you use Timber)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Set debug token BEFORE initializing Firebase App Check
        if (BuildConfig.DEBUG) {
            // Set debug token langsung di system property
//            System.setProperty("firebase.appcheck.debug_token", "0C43172D-6685-4894-8E6E-6966E1ED395B")
        }

        // Initialize Firebase App Check
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        if (BuildConfig.DEBUG) {
            // Use DebugAppCheckProviderFactory for debug builds
            firebaseAppCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
            Timber.d("Firebase App Check initialized with DebugAppCheckProviderFactory using token: 0C43172D-6685-4894-8E6E-6966E1ED395B")
        } else {
            // Use PlayIntegrityAppCheckProviderFactory for production builds
            firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance()
            )
            Timber.d("Firebase App Check initialized with PlayIntegrityAppCheckProviderFactory.")
        }

        // Initialize other app components
        initializeApp() // Your existing app initialization
        createNotificationChannels() // Your existing notification channel setup

        // Initialize crash reporting (your existing method)
        // initializeCrashReporting()
    }

    private fun initializeApp() {
        // Log aplikasi start (hanya di debug mode)
        if (BuildConfig.DEBUG) {
            Timber.d("🌪️ Daily Chaos Application Started - Ready to embrace the chaos!")
            Timber.d("📱 Version: ${BuildConfig.VERSION_NAME}")
            Timber.d("🔧 Debug Mode: ${BuildConfig.DEBUG}")
            Timber.d("🔥 Firebase Project: ${BuildConfig.FIREBASE_PROJECT_ID}")
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

            val generalChannel = NotificationChannel(Constants.NOTIFICATION_CHANNEL_GENERAL, "General Notifications", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "General app notifications"
                enableVibration(true)
                setShowBadge(true)
            }

            val supportChannel = NotificationChannel(Constants.NOTIFICATION_CHANNEL_SUPPORT, "Support Notifications", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Notifications when you receive support from the community"
                enableVibration(true)
                setShowBadge(true)
            }

            val syncChannel = NotificationChannel(Constants.NOTIFICATION_CHANNEL_SYNC, "Sync Status", NotificationManager.IMPORTANCE_LOW).apply {
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
        // TODO: Initialize Firebase Crashlytics when ready for production
        // FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)

        if (BuildConfig.DEBUG) {
            Timber.d("🔍 Crash reporting setup (disabled in debug)")
        }
    }

    companion object {
        lateinit var instance: ChaosApplication
            private set

        fun getAppContext(): Context = instance.applicationContext
    }

    init {
        instance = this
    }
}