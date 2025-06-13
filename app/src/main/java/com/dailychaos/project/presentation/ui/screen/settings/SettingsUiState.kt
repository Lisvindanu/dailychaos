package com.dailychaos.project.presentation.ui.screen.settings

import com.dailychaos.project.domain.model.ThemeMode

data class SettingsUiState(
    val isLoading: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val notificationsEnabled: Boolean = true,
    val konosubaQuotesEnabled: Boolean = true,
    val version: String = "1.0.0"
)

sealed class SettingsEvent {
    data class OnThemeChange(val themeMode: ThemeMode) : SettingsEvent()
    data class OnNotificationsToggle(val enabled: Boolean) : SettingsEvent()
    data class OnKonoSubaQuotesToggle(val enabled: Boolean) : SettingsEvent()
    object OnPrivacyPolicyClick : SettingsEvent()
    object OnAboutClick : SettingsEvent()
}