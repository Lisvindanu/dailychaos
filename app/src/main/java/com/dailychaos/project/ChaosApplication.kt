package com.dailychaos.project

import android.app.Application
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

        // Initialize app untuk local development
        initializeApp()
    }

    private fun initializeApp() {
        // Log aplikasi start (hanya di debug mode)
        if (BuildConfig.DEBUG) {
            android.util.Log.d("DailyChaos", "🌪️ Daily Chaos Application Started - Ready to embrace the chaos!")
            android.util.Log.d("DailyChaos", "📱 Version: ${BuildConfig.VERSION_NAME}")
            android.util.Log.d("DailyChaos", "🔧 Debug Mode: ${BuildConfig.DEBUG}")
        }

        // TODO: Initialize Firebase ketika siap production
        // TODO: Initialize database migrations
        // TODO: Setup notification channels
        // TODO: Initialize crash reporting

        println("✅ Daily Chaos initialized successfully!")
    }
}