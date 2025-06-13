package com.dailychaos.project

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.dailychaos.project.util.Constants
import dagger.hilt.android.HiltAndroidApp

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

        // Initialize app
        initializeApp()

        // Setup notification channels
        createNotificationChannels()

        // Initialize crash reporting (commented out untuk development)
        // initializeCrashReporting()
    }

    private fun initializeApp() {
        // Log aplikasi start (hanya di debug mode)
        if (BuildConfig.DEBUG) {
            android.util.Log.d("DailyChaos", "🌪️ Daily Chaos Application Started - Ready to embrace the chaos!")
            android.util.Log.d("DailyChaos", "📱 Version: ${BuildConfig.VERSION_NAME}")
            android.util.Log.d("DailyChaos", "🔧 Debug Mode: ${BuildConfig.DEBUG}")
            android.util.Log.d("DailyChaos", "🔥 Firebase Project: ${BuildConfig.FIREBASE_PROJECT_ID}")
        }

        // Setup global exception handler untuk debugging
        if (BuildConfig.DEBUG) {
            Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
                android.util.Log.e("DailyChaos", "Uncaught exception in thread ${thread.name}", exception)
            }
        }

        println("✅ Daily Chaos initialized successfully!")
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // General notifications channel
            val generalChannel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_GENERAL,
                "General Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General app notifications"
                enableVibration(true)
                setShowBadge(true)
            }

            // Support notifications channel
            val supportChannel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_SUPPORT,
                "Support Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications when you receive support from the community"
                enableVibration(true)
                setShowBadge(true)
            }

            // Sync notifications channel
            val syncChannel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_SYNC,
                "Sync Status",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Data synchronization status"
                enableVibration(false)
                setShowBadge(false)
            }

            // Create all channels
            notificationManager.createNotificationChannels(
                listOf(generalChannel, supportChannel, syncChannel)
            )

            if (BuildConfig.DEBUG) {
                android.util.Log.d("DailyChaos", "📱 Notification channels created successfully")
            }
        }
    }

    private fun initializeCrashReporting() {
        // TODO: Initialize Firebase Crashlytics when ready for production
        // FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)

        if (BuildConfig.DEBUG) {
            android.util.Log.d("DailyChaos", "🔍 Crash reporting setup (disabled in debug)")
        }
    }

    companion object {
        /**
         * Get application context safely
         */
        lateinit var instance: ChaosApplication
            private set

        fun getAppContext(): Context = instance.applicationContext
    }

    init {
        instance = this
    }
}