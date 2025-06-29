package com.dailychaos.project.presentation.ui.screen.auth.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dailychaos.project.presentation.theme.DailyChaosTheme
import com.dailychaos.project.presentation.ui.component.OnboardingPage
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
    onOnboardingComplete: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { viewModel.pages.size })
    val scope = rememberCoroutineScope()

    // Track current page untuk fix button visibility
    val currentPage by remember { derivedStateOf { pagerState.currentPage } }
    val isLastPage = currentPage == viewModel.pages.size - 1

    // Update ViewModel state when page changes
    LaunchedEffect(currentPage) {
        viewModel.onPageChanged(currentPage)
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { pageIndex ->
                val pageData = viewModel.pages[pageIndex]
                OnboardingPage(
                    title = pageData.title,
                    subtitle = pageData.subtitle,
                    illustration = pageData.illustration
                )
            }

            // Controls Section - ALWAYS VISIBLE
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    // Page Indicators
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(viewModel.pages.size) { iteration ->
                            val color = if (currentPage == iteration) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outline
                            }

                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .clip(CircleShape)
                                    .size(if (currentPage == iteration) 12.dp else 8.dp)
//                                    .background(color)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Skip Button (only show if not last page)
                        if (!isLastPage) {
                            TextButton(
                                onClick = {
                                    viewModel.completeOnboarding()
                                    onOnboardingComplete()
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Skip")
                            }
                        } else {
                            Spacer(modifier = Modifier.width(72.dp)) // Placeholder untuk balance
                        }

                        // Next/Get Started Button - ALWAYS VISIBLE
                        if (isLastPage) {
                            Button(
                                onClick = {
                                    viewModel.completeOnboarding()
                                    onOnboardingComplete()
                                },
                                modifier = Modifier.fillMaxWidth(0.6f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Get Started!")
                            }
                        } else {
                            Button(
                                onClick = {
                                    scope.launch {
                                        pagerState.animateScrollToPage(currentPage + 1)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(0.6f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Next")
                            }
                        }
                    }
                }
            }
        }
    }
}