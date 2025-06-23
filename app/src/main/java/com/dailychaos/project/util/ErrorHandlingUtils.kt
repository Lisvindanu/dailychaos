// File: app/src/main/java/com/dailychaos/project/util/ErrorHandlingUtils.kt
package com.dailychaos.project.util

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.auth.FirebaseAuthException
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException

/**
 * Error Handling Utilities
 * "Utility functions untuk handle different types of errors dengan user-friendly messages"
 */
object ErrorHandlingUtils {

    /**
     * Get user-friendly error message from exception
     */
    fun getErrorMessage(context: Context, exception: Throwable): String {
        return when (exception) {
            is FirebaseFirestoreException -> getFirestoreErrorMessage(exception)
            is FirebaseAuthException -> getAuthErrorMessage(exception)
            is UnknownHostException -> "No internet connection. Please check your network and try again."
            is TimeoutException -> "Request timed out. Please try again."
            is IllegalArgumentException -> exception.message ?: "Invalid input provided."
            else -> exception.message ?: "An unexpected error occurred. Please try again."
        }
    }

    /**
     * Get specific error message for Firestore exceptions
     */
    private fun getFirestoreErrorMessage(exception: FirebaseFirestoreException): String {
        return when (exception.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                "Access denied. Please check your permissions or try logging in again."
            }
            FirebaseFirestoreException.Code.NOT_FOUND -> {
                "Content not found or has been removed."
            }
            FirebaseFirestoreException.Code.UNAVAILABLE -> {
                "Service temporarily unavailable. Please try again in a moment."
            }
            FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> {
                "Request timed out. Please check your connection and try again."
            }
            FirebaseFirestoreException.Code.UNAUTHENTICATED -> {
                "Please log in to continue."
            }
            FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED -> {
                "Too many requests. Please wait a moment and try again."
            }
            FirebaseFirestoreException.Code.FAILED_PRECONDITION -> {
                "Operation failed due to current system state. Please try again."
            }
            FirebaseFirestoreException.Code.ABORTED -> {
                "Operation was aborted. Please try again."
            }
            FirebaseFirestoreException.Code.OUT_OF_RANGE -> {
                "Invalid request parameters."
            }
            FirebaseFirestoreException.Code.UNIMPLEMENTED -> {
                "This feature is not available yet."
            }
            FirebaseFirestoreException.Code.INTERNAL -> {
                "Internal server error. Please try again later."
            }
            FirebaseFirestoreException.Code.DATA_LOSS -> {
                "Data corruption detected. Please contact support."
            }
            else -> {
                "Something went wrong: ${exception.message}"
            }
        }
    }

    /**
     * Get specific error message for Auth exceptions
     */
    private fun getAuthErrorMessage(exception: FirebaseAuthException): String {
        return when (exception.errorCode) {
            "ERROR_INVALID_EMAIL" -> "Please enter a valid email address."
            "ERROR_WRONG_PASSWORD" -> "Incorrect password. Please try again."
            "ERROR_USER_NOT_FOUND" -> "No account found with this email address."
            "ERROR_USER_DISABLED" -> "This account has been disabled."
            "ERROR_TOO_MANY_REQUESTS" -> "Too many attempts. Please try again later."
            "ERROR_OPERATION_NOT_ALLOWED" -> "This sign-in method is not enabled."
            "ERROR_EMAIL_ALREADY_IN_USE" -> "An account with this email already exists."
            "ERROR_WEAK_PASSWORD" -> "Password is too weak. Please choose a stronger password."
            "ERROR_NETWORK_REQUEST_FAILED" -> "Network error. Please check your connection."
            else -> exception.message ?: "Authentication failed. Please try again."
        }
    }

    /**
     * Check if error is related to permissions
     */
    fun isPermissionError(exception: Throwable): Boolean {
        return when (exception) {
            is FirebaseFirestoreException ->
                exception.code == FirebaseFirestoreException.Code.PERMISSION_DENIED
            else ->
                exception.message?.contains("permission", ignoreCase = true) == true ||
                        exception.message?.contains("access denied", ignoreCase = true) == true
        }
    }

    /**
     * Check if error is network related
     */
    fun isNetworkError(exception: Throwable): Boolean {
        return when (exception) {
            is UnknownHostException -> true
            is TimeoutException -> true
            is FirebaseFirestoreException ->
                exception.code == FirebaseFirestoreException.Code.UNAVAILABLE ||
                        exception.code == FirebaseFirestoreException.Code.DEADLINE_EXCEEDED
            else ->
                exception.message?.contains("network", ignoreCase = true) == true ||
                        exception.message?.contains("connection", ignoreCase = true) == true
        }
    }

    /**
     * Check if error requires user authentication
     */
    fun requiresAuthentication(exception: Throwable): Boolean {
        return when (exception) {
            is FirebaseFirestoreException ->
                exception.code == FirebaseFirestoreException.Code.UNAUTHENTICATED
            is FirebaseAuthException -> true
            else ->
                exception.message?.contains("authentication", ignoreCase = true) == true ||
                        exception.message?.contains("login", ignoreCase = true) == true
        }
    }

    /**
     * Get retry suggestion based on error type
     */
    fun getRetrySuggestion(exception: Throwable): String {
        return when {
            isNetworkError(exception) -> "Check your internet connection and try again."
            isPermissionError(exception) -> "Try logging out and logging back in."
            requiresAuthentication(exception) -> "Please log in to continue."
            else -> "Please try again in a moment."
        }
    }
}




