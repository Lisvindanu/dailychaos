// Top-level build file where you can add configuration options common to all sub-projects/modules.
// Applying plugins here as 'false' means they are available for modules to apply.
plugins {
    // Apply the Android Application plugin at the project level (false means it's not applied to the root project itself)
    alias(libs.plugins.android.application) apply false
    // Apply the Kotlin Android plugin
    alias(libs.plugins.kotlin.android) apply false
    // Apply the Kotlin Compose plugin
    alias(libs.plugins.kotlin.compose) apply false
    // Apply the Hilt Android plugin
    alias(libs.plugins.hilt.android) apply false
    // Apply the KSP (Kotlin Symbol Processing) plugin
    alias(libs.plugins.ksp) apply false
    // Apply the Kotlin Serialization plugin
    alias(libs.plugins.kotlin.serialization) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false

}