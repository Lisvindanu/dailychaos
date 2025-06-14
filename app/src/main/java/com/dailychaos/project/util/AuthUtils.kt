// File: app/src/main/java/com/dailychaos/project/util/AuthUtils.kt
package com.dailychaos.project.util

import com.dailychaos.project.preferences.UserPreferences
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Authentication utilities untuk Daily Chaos
 * "Checking party membership status!"
 */
@Singleton
class AuthUtils @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val userPreferences: UserPreferences
) {

    /**
     * Check if user is already authenticated
     * Returns Flow<Boolean> untuk reactive UI
     */
    fun isUserAuthenticated(): Flow<Boolean> = flow {
        // Check Firebase auth state
        val isFirebaseAuth = firebaseAuth.currentUser != null

        if (isFirebaseAuth) {
            // Also check if we have user ID in preferences
            userPreferences.userId.collect { userId ->
                emit(!userId.isNullOrBlank())
            }
        } else {
            emit(false)
        }
    }

    /**
     * Get user authentication status with user ID
     */
    fun getAuthState(): Flow<Pair<Boolean, String?>> {
        return combine(
            flow { emit(firebaseAuth.currentUser != null) },
            userPreferences.userId
        ) { isFirebaseAuth, userId ->
            Pair(isFirebaseAuth && !userId.isNullOrBlank(), userId)
        }
    }

    /**
     * Check if this is first launch
     */
    fun isFirstLaunch(): Flow<Boolean> = userPreferences.isFirstLaunch

    /**
     * Check if onboarding is completed
     */
    fun isOnboardingCompleted(): Flow<Boolean> = userPreferences.onboardingCompleted
}