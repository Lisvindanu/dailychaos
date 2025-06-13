package com.dailychaos.project.presentation.ui.screen.chaos.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dailychaos.project.presentation.theme.DailyChaosTheme
import com.dailychaos.project.presentation.ui.component.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChaosHistoryScreen(
    viewModel: ChaosHistoryViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToEntry: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chaos History") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            // Search and Filter Bar
            ChaosSearchBar(
                query = uiState.searchQuery,
                onQueryChange = { viewModel.onEvent(ChaosHistoryEvent.SearchQueryChanged(it)) },
                onSearch = { /* Search is real-time */ },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            ChaosFilterChips(
                selectedFilters = uiState.activeFilters,
                onFilterToggle = { viewModel.onEvent(ChaosHistoryEvent.FilterToggled(it)) },
            )

            // Content
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading -> {
                        LoadingIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            message = "Loading your chaotic history..."
                        )
                    }
                    uiState.error != null -> {
                        ErrorMessage(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp),
                            message = uiState.error!!,
                            onRetryClick = { viewModel.onEvent(ChaosHistoryEvent.Retry) }
                        )
                    }
                    uiState.entriesByDate.isEmpty() -> {
                        EmptyState(
                            modifier = Modifier.align(Alignment.Center),
                            illustration = "🤷",
                            title = "No Chaos Found",
                            subtitle = "No entries match your search. Try adjusting the filters or search term."
                        )
                    }
                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            uiState.entriesByDate.forEach { (date, entries) ->
                                stickyHeader {
                                    Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.background) {
                                        Text(
                                            text = date,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                    }
                                }
                                items(entries) { entry ->
                                    ChaosEntryCard(
                                        chaosEntry = entry,
                                        onCardClick = { onNavigateToEntry(entry.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChaosHistoryScreenPreview() {
    DailyChaosTheme {
        ChaosHistoryScreen(onNavigateBack = {}, onNavigateToEntry = {})
    }
}