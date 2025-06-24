package com.dailychaos.project.presentation.ui.screen.community.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dailychaos.project.presentation.theme.DailyChaosTheme
import com.dailychaos.project.presentation.ui.component.CommunityPostCard
import com.dailychaos.project.presentation.ui.component.EmptyState
import com.dailychaos.project.presentation.ui.component.ErrorMessage
import com.dailychaos.project.presentation.ui.component.LoadingIndicator

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CommunityFeedScreen(
    viewModel: CommunityFeedViewModel = hiltViewModel(),
    onNavigateToPost: (String) -> Unit,
    onNavigateToTwins: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // ✅ Handle error messages
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            // Auto-clear error after showing
            viewModel.onEvent(CommunityFeedEvent.ClearError)
        }
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isRefreshing,
        onRefresh = { viewModel.onEvent(CommunityFeedEvent.Refresh) }
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }, // ✅ ENHANCED: Add snackbar support
        topBar = {
            TopAppBar(
                title = { Text("Community Feed") },
                actions = {
                    IconButton(onClick = onNavigateToTwins) {
                        Icon(Icons.Default.People, contentDescription = "Find Chaos Twins")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pullRefresh(pullRefreshState)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        message = "Gathering stories from fellow adventurers..."
                    )
                }
                uiState.error != null && uiState.posts.isEmpty() -> {
                    ErrorMessage(
                        message = uiState.error!!,
                        onRetryClick = { viewModel.onEvent(CommunityFeedEvent.Retry) },
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                uiState.posts.isEmpty() -> {
                    EmptyState(
                        illustration = "🍃",
                        title = "The Guild is Quiet",
                        subtitle = "No one has shared their chaos yet. Be the first to start a new adventure!",
                        actionText = "Share Your Chaos",
                        onActionClick = { /* TODO: Navigate to create/share */ },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.posts) { post ->
                            CommunityPostCard(
                                communityPost = post,
                                onCardClick = { onNavigateToPost(post.id) },
                                onSupportClick = { type ->
                                    viewModel.onEvent(CommunityFeedEvent.GiveSupport(post.id, type))
                                },
                                onReportClick = {
                                    viewModel.onEvent(CommunityFeedEvent.ReportPost(post.id))
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // ✅ ENHANCED: Add some bottom padding for last item
                        item {
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(8.dp))
                        }
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = uiState.isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CommunityFeedScreenPreview() {
    DailyChaosTheme {
        // Fake implementation to satisfy navigation
        CommunityFeedScreen(onNavigateToPost = {}, onNavigateToTwins = {})
    }
}