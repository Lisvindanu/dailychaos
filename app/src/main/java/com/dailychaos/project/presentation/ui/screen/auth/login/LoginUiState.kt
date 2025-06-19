// Fixed LoginUiState.kt
package com.dailychaos.project.presentation.ui.screen.auth.login

/**
 * Login Screen UI State
 * "Manages state untuk login, apakah berhasil, gagal, atau loading."
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val username: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isPasswordVisible: Boolean = false,
    val loginMode: LoginMode = LoginMode.USERNAME,
    val isUsernameValid: Boolean = true, // FIXED: Changed from false to true
    val usernameError: String? = null,
    val suggestions: List<String> = emptyList()
)

enum class LoginMode {
    USERNAME,
    EMAIL,
    ANONYMOUS
}

sealed class LoginEvent {
    data class EmailChanged(val email: String) : LoginEvent()
    data class PasswordChanged(val password: String) : LoginEvent()
    data class UsernameChanged(val username: String) : LoginEvent()
    object TogglePasswordVisibility : LoginEvent()
    object LoginWithEmail : LoginEvent()
    object LoginWithUsername : LoginEvent()
    object LoginAnonymously : LoginEvent()
    data class SwitchLoginMode(val mode: LoginMode) : LoginEvent()
    data class SuggestionClicked(val suggestion: String) : LoginEvent()
    object ClearError : LoginEvent()
}