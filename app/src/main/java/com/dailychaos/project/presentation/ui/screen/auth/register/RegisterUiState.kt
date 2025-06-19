// Fixed RegisterUiState.kt
package com.dailychaos.project.presentation.ui.screen.auth.register

/**
 * Register Screen UI State
 *
 * "Manages state untuk register, dari username sampai email registration."
 */
data class RegisterUiState(
    // Common fields
    val displayName: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val registerMode: RegisterMode = RegisterMode.USERNAME,

    // Username registration fields
    val username: String = "",
    val isUsernameValid: Boolean = true, // FIXED: Changed from false to true
    val usernameError: String? = null,
    val suggestions: List<String> = emptyList(),

    // Email registration fields
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null
)


enum class RegisterMode {
    USERNAME,
    EMAIL
}

sealed class RegisterEvent {
    // Common events
    data class DisplayNameChanged(val displayName: String) : RegisterEvent()
    data class SwitchRegisterMode(val mode: RegisterMode) : RegisterEvent()
    object ClearError : RegisterEvent()

    // Username registration events
    data class UsernameChanged(val username: String) : RegisterEvent()
    data class SuggestionClicked(val suggestion: String) : RegisterEvent()
    object RegisterWithUsername : RegisterEvent()

    // Email registration events
    data class EmailChanged(val email: String) : RegisterEvent()
    data class PasswordChanged(val password: String) : RegisterEvent()
    data class ConfirmPasswordChanged(val confirmPassword: String) : RegisterEvent()
    object TogglePasswordVisibility : RegisterEvent()
    object ToggleConfirmPasswordVisibility : RegisterEvent()
    object RegisterWithEmail : RegisterEvent()
}