// File: app/src/main/java/com/dailychaos/project/presentation/ui/screen/community/feed/CommunityFeedScreen.kt
package com.dailychaos.project.presentation.ui.screen.community.feed

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dailychaos.project.presentation.ui.component.CommunityFeedFilterBar
import com.dailychaos.project.presentation.ui.component.CommunityPostCard
import com.dailychaos.project.presentation.ui.component.EmptyState
import com.dailychaos.project.presentation.ui.component.ErrorMessage
import com.dailychaos.project.presentation.ui.component.FilterSummaryChip
import com.dailychaos.project.presentation.ui.component.LoadingIndicator
import com.dailychaos.project.presentation.ui.component.LoadMoreIndicator

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityFeedScreen(
    viewModel: CommunityFeedViewModel = hiltViewModel(),
    onNavigateToPost: (String) -> Unit,
    onNavigateToTwins: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val paginationState by viewModel.paginationState.collectAsState()
    val metadataState by viewModel.metadataState.collectAsState()
    val isFilterVisible by viewModel.isFilterVisible.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    // Handle error messages
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.onEvent(CommunityFeedEvent.ClearError)
        }
    }

    // Detect scroll for pagination
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo }
            .collect { layoutInfo ->
                val totalItems = layoutInfo.totalItemsCount
                val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

                if (totalItems > 0 && lastVisibleIndex >= totalItems - 3 && viewModel.canLoadMore()) {
                    viewModel.onFilterEvent(FilterEvent.LoadMore)
                }
            }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Community Feed")
                        if (viewModel.getActiveFilterCount() > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Badge {
                                Text(viewModel.getActiveFilterCount().toString())
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onFilterEvent(FilterEvent.ToggleFilter) }) {
                        Icon(
                            if (isFilterVisible) Icons.Default.FilterListOff else Icons.Default.FilterList,
                            contentDescription = "Filter"
                        )
                    }
                    IconButton(onClick = onNavigateToTwins) {
                        Icon(Icons.Default.People, contentDescription = "Find Chaos Twins")
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filter Bar
            CommunityFeedFilterBar(
                isVisible = isFilterVisible,
                selectedTimeFilter = filterState.timeFilter,
                selectedChaosLevel = filterState.chaosLevelRange,
                selectedTags = filterState.selectedTags,
                availableTags = metadataState.popularTags,
                onTimeFilterChange = { viewModel.onFilterEvent(FilterEvent.UpdateTimeFilter(it)) },
                onChaosLevelChange = { viewModel.onFilterEvent(FilterEvent.UpdateChaosLevel(it)) },
                onTagToggle = { viewModel.onFilterEvent(FilterEvent.ToggleTag(it)) },
                onClearFilters = { viewModel.onFilterEvent(FilterEvent.ClearFilters) }
            )

            // Filter Summary
            if (viewModel.getActiveFilterCount() > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterSummaryChip(
                        filterCount = viewModel.getActiveFilterCount(),
                        onToggleFilter = { viewModel.onFilterEvent(FilterEvent.ToggleFilter) }
                    )

                    if (paginationState.totalCount > 0) {
                        Text(
                            text = "${uiState.posts.size} of ${paginationState.totalCount} posts",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Main Content
            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { viewModel.onEvent(CommunityFeedEvent.Refresh) },
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    uiState.isLoading -> {
                        LoadingIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            message = "Loading filtered posts..."
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
                            illustration = if (viewModel.getActiveFilterCount() > 0) "🔍" else "🍃",
                            title = if (viewModel.getActiveFilterCount() > 0) "No Results Found" else "The Guild is Quiet",
                            subtitle = if (viewModel.getActiveFilterCount() > 0)
                                "Try adjusting your filters"
                            else
                                "No one has shared their chaos yet. Be the first to start a new adventure!",
                            actionText = if (viewModel.getActiveFilterCount() > 0) "Clear Filters" else "Refresh",
                            onActionClick = {
                                if (viewModel.getActiveFilterCount() > 0) {
                                    viewModel.onFilterEvent(FilterEvent.ClearFilters)
                                } else {
                                    viewModel.onEvent(CommunityFeedEvent.Refresh)
                                }
                            },
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    else -> {
                        LazyColumn(
                            state = listState,
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

                            // Load More Indicator
                            item {
                                LoadMoreIndicator(isLoading = paginationState.isLoadingMore)
                            }

                            // End Spacing
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}