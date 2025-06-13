package com.dailychaos.project.presentation.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailychaos.project.BuildConfig
import com.dailychaos.project.domain.model.ThemeMode
import com.dailychaos.project.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            userPreferences.themeMode.collectLatest { theme ->
                _uiState.update { it.copy(themeMode = theme, isLoading = false, version = BuildConfig.VERSION_NAME) }
            }
            // Add collectors for other preferences too
        }
    }

    fun onEvent(event: SettingsEvent) {
        viewModelScope.launch {
            when (event) {
                is SettingsEvent.OnThemeChange -> userPreferences.setThemeMode(event.themeMode)
                is SettingsEvent.OnNotificationsToggle -> userPreferences.setNotificationsEnabled(event.enabled)
                is SettingsEvent.OnKonoSubaQuotesToggle -> userPreferences.setKonosubaQuotesEnabled(event.enabled)
                else -> { /* Navigation handled in UI */ }
            }
        }
    }
}