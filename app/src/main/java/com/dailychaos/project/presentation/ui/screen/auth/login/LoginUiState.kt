package com.dailychaos.project.presentation.ui.screen.auth.login

/**
 * Login Screen UI State
 *
 * "Manages state untuk login, apakah berhasil, gagal, atau loading."
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isPasswordVisible: Boolean = false
)

sealed class LoginEvent {
    data class EmailChanged(val email: String) : LoginEvent()
    data class PasswordChanged(val password: String) : LoginEvent()
    object TogglePasswordVisibility : LoginEvent()
    object LoginWithEmail : LoginEvent()
    object LoginAnonymously : LoginEvent()
    object ClearError : LoginEvent()
}