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

        // Initialize any global configurations here
        // Firebase akan auto-initialize dengan google-services.json

        // Log untuk debugging (hanya di debug mode)
        if (BuildConfig.DEBUG) {
            android.util.Log.d("DailyChaos", "🌪️ Daily Chaos Application Started - Ready to embrace the chaos!")
        }
    }
}