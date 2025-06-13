package com.dailychaos.project.presentation.ui.screen.auth.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailychaos.project.preferences.UserPreferences
import com.dailychaos.project.util.KonoSubaQuotes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState = _uiState.asStateFlow()

    val pages = listOf(
        OnboardingPageData(
            illustration = "🌪️",
            title = "Welcome to the Party!",
            subtitle = "This is a safe space to turn your daily struggles into shared adventures.",
            quote = KonoSubaQuotes.getQuoteByCharacter(KonoSubaQuotes.Character.PARTY)
        ),
        OnboardingPageData(
            illustration = "📝",
            title = "Log Your Daily Chaos",
            subtitle = "Write down your personal struggles in a private journal. No judgment here.",
            quote = KonoSubaQuotes.getQuoteByCharacter(KonoSubaQuotes.Character.KAZUMA)
        ),
        OnboardingPageData(
            illustration = "🤝",
            title = "Find Your Chaos Twins",
            subtitle = "Share your entries anonymously and discover others who truly get it.",
            quote = KonoSubaQuotes.getQuoteByCharacter(KonoSubaQuotes.Character.DARKNESS)
        ),
        OnboardingPageData(
            illustration = "💙",
            title = "Give & Receive Support",
            subtitle = "Just like Kazuma's party, we back each other up, no matter how dysfunctional.",
            quote = KonoSubaQuotes.getQuoteByCharacter(KonoSubaQuotes.Character.AQUA)
        )
    )

    fun onPageChanged(page: Int) {
        _uiState.update { it.copy(currentPage = page) }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            userPreferences.setOnboardingCompleted()
        }
    }
}