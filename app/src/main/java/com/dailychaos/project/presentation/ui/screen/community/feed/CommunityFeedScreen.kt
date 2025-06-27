package com.dailychaos.project.presentation.ui.screen.community.feed

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityFeedScreen(
    viewModel: CommunityFeedViewModel = hiltViewModel(),
    onNavigateToPost: (String) -> Unit,
    onNavigateToTwins: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle error messages
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.onEvent(CommunityFeedEvent.ClearError)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.onEvent(CommunityFeedEvent.Refresh) },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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

                        item {
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun CommunityFeedScreenPreview() {
    DailyChaosTheme {
        CommunityFeedScreen(onNavigateToPost = {}, onNavigateToTwins = {})
    }
}