package com.dailychaos.project.presentation.ui.screen.auth.onboarding

import com.dailychaos.project.util.KonoSubaQuotes

data class OnboardingUiState(
    val currentPage: Int = 0,
    val totalPages: Int = 4
)

data class OnboardingPageData(
    val illustration: String,
    val title: String,
    val subtitle: String,
    val quote: KonoSubaQuotes.Quote
)