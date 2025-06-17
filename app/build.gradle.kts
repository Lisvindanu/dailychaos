// File: app/build.gradle.kts
import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.services) // Firebase
    alias(libs.plugins.firebase.crashlytics) // Crashlytics
    alias(libs.plugins.firebase.perf) // Performance monitoring
}

android {
    namespace = "com.dailychaos.project"
    compileSdk = 35

    buildFeatures {
        compose = true
        buildConfig = true
    }

    // SIGNING CONFIG untuk Release
    signingConfigs {
        create("release") {
            storeFile = file("../daily-chaos-release-key.keystore")
            storePassword = "DailyChaosKey2025"
            keyAlias = "daily-chaos-key"
            keyPassword = "DailyChaosKey2025"
        }
    }

    defaultConfig {
        applicationId = "com.dailychaos.project"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // App configuration
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(FileInputStream(localPropertiesFile))
        }

        val appName = localProperties.getProperty("APP_NAME") ?:
        System.getenv("APP_NAME") ?:
        "Daily Chaos"

        val firebaseProjectId = localProperties.getProperty("FIREBASE_PROJECT_ID") ?:
        System.getenv("FIREBASE_PROJECT_ID") ?:
        "daily-chaos-dev"

        buildConfigField("String", "APP_NAME", "\"$appName\"")
        buildConfigField("String", "FIREBASE_PROJECT_ID", "\"$firebaseProjectId\"")
    }

    buildTypes {
        debug {
            isDebuggable = true
            // applicationIdSuffix = ".debug"  // COMMENT OUT INI
            versionNameSuffix = "-debug"

            val localProperties = Properties()
            val localPropertiesFile = rootProject.file("local.properties")
            if (localPropertiesFile.exists()) {
                localProperties.load(FileInputStream(localPropertiesFile))
            }

            val appName = localProperties.getProperty("APP_NAME") ?:
            System.getenv("APP_NAME") ?:
            "Daily Chaos Debug"

            val firebaseProjectId = localProperties.getProperty("FIREBASE_PROJECT_ID") ?:
            System.getenv("FIREBASE_PROJECT_ID") ?:
            "daily-chaos-dev"

            buildConfigField("String", "APP_NAME", "\"$appName\"")
            buildConfigField("String", "FIREBASE_PROJECT_ID", "\"$firebaseProjectId\"")
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // GUNAKAN SIGNING CONFIG
            signingConfig = signingConfigs.getByName("release")

            // Production configuration
            val firebaseProjectId = System.getenv("FIREBASE_PROJECT_ID_PROD") ?:
            System.getenv("FIREBASE_PROJECT_ID") ?:
            "daily-chaos-prod"

            buildConfigField("String", "FIREBASE_PROJECT_ID", "\"$firebaseProjectId\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/LICENSE.md"
            excludes += "/META-INF/LICENSE.txt"
            excludes += "/META-INF/NOTICE.md"
            excludes += "/META-INF/NOTICE.txt"
            excludes += "/META-INF/ASL2.0"
            excludes += "/META-INF/LGPL2.1"
            excludes += "**/META-INF/LICENSE.md"
            excludes += "**/META-INF/LICENSE"
            excludes += "**/META-INF/NOTICE"
            excludes += "**/META-INF/DEPENDENCIES"

            // JUnit conflict fixes
            excludes += "**/META-INF/LICENSE-notice.md"
            excludes += "/META-INF/LICENSE-notice.md"
            excludes += "**/META-INF/LICENSE-notice.txt"
            excludes += "/META-INF/LICENSE-notice.txt"
            excludes += "**/META-INF/junit-platform.properties"
            excludes += "**/META-INF/io.netty.versions.properties"
            excludes += "**/META-INF/gradle-plugins/**"
            excludes += "**/META-INF/native-image/**"
            excludes += "**/META-INF/*.kotlin_module"
            excludes += "**/META-INF/versions/**"
            excludes += "**/META-INF/maven/**"
            excludes += "**/META-INF/services/**"
            excludes += "**/META-INF/extensions.idx"
            excludes += "**/META-INF/INDEX.LIST"
            excludes += "**/OSGI-INF/**"
        }
    }
    lint {
        disable.add("NullSafeMutableLiveData")
    }
}

dependencies {
    // Core & Lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    // UI & Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.compose.navigation)

    // Pull to refresh & Material
    implementation("androidx.compose.material:material")

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx) // Dipastikan ada untuk Kotlin extensions
    implementation(libs.firebase.functions.ktx)
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.firebase.analytics.ktx)
    implementation(libs.firebase.crashlytics.ktx)
    implementation(libs.firebase.perf.ktx)
    implementation(libs.firebase.config.ktx)

    // Logging - Timber
    implementation("com.jakewharton.timber:timber:5.0.1")

    // Hilt - Dependency Injection
    implementation(libs.hilt.android)
    implementation(libs.androidx.core.splashscreen)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Room - Local Database
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Kotlinx Serialization & DateTime
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)

    // DataStore - Preferences
    implementation(libs.datastore.preferences)

    // Image Loading - Coil
    implementation(libs.coil.compose)

    // Networking - Retrofit & OkHttp (untuk non-Firebase APIs jika diperlukan)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    // Image picker
    implementation(libs.androidx.activity.compose.v182)

    // Networking & API
    implementation(libs.retrofit.v290)
    implementation(libs.converter.gson.v290)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    // Image Loading (Coil for Compose)
    implementation(libs.coil.compose.v250)
    implementation("io.coil-kt:coil-compose:2.5.0")

    // JSON Parsing
    implementation("com.google.code.gson:gson:2.11.0")

    // Unit Tests
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.truth)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.arch.core.testing)
    testImplementation(libs.mockk)

    // Android Instrumented Tests
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.androidx.ui.test.junit4.android)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.core.ktx)
    androidTestImplementation(libs.hilt.android.testing)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.mockk.agent)
    kspAndroidTest(libs.hilt.compiler)

    // Debugging
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    debugImplementation(libs.chucker)
    implementation(kotlin("test"))

    implementation("androidx.compose.animation:animation:1.8.2")
    implementation("androidx.compose.material:material:1.8.2") // Untuk PullToRefresh
}