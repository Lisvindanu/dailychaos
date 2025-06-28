// File: app/src/main/java/com/dailychaos/project/presentation/ui/component/CommunityFeedFilterComponents.kt
package com.dailychaos.project.presentation.ui.component

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dailychaos.project.domain.repository.TimeFilter
import com.dailychaos.project.domain.repository.displayName

/**
 * Komponen Filter untuk CommunityFeedScreen
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityFeedFilterBar(
    isVisible: Boolean,
    selectedTimeFilter: TimeFilter,
    selectedChaosLevel: IntRange?,
    selectedTags: Set<String>,
    availableTags: List<String>,
    onTimeFilterChange: (TimeFilter) -> Unit,
    onChaosLevelChange: (IntRange?) -> Unit,
    onTagToggle: (String) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filter",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    val hasActiveFilters = selectedTimeFilter != TimeFilter.ALL ||
                            selectedChaosLevel != null ||
                            selectedTags.isNotEmpty()

                    if (hasActiveFilters) {
                        TextButton(onClick = onClearFilters) {
                            Text("Clear All")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Time Filter
                Text(
                    text = "Time Range",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(TimeFilter.values()) { timeFilter ->
                        FilterChip(
                            onClick = { onTimeFilterChange(timeFilter) },
                            label = { Text(timeFilter.displayName) },
                            selected = selectedTimeFilter == timeFilter
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Chaos Level Filter
                Text(
                    text = "Chaos Level",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        onClick = { onChaosLevelChange(null) },
                        label = { Text("Any Level") },
                        selected = selectedChaosLevel == null,
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        onClick = { onChaosLevelChange(1..3) },
                        label = { Text("Low (1-3)") },
                        selected = selectedChaosLevel == (1..3),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        onClick = { onChaosLevelChange(4..7) },
                        label = { Text("Med (4-7)") },
                        selected = selectedChaosLevel == (4..7),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        onClick = { onChaosLevelChange(8..10) },
                        label = { Text("High (8-10)") },
                        selected = selectedChaosLevel == (8..10),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Tags Filter
                if (availableTags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Tags",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(availableTags) { tag ->
                            FilterChip(
                                onClick = { onTagToggle(tag) },
                                label = { Text(tag) },
                                selected = selectedTags.contains(tag),
                                leadingIcon = if (selectedTags.contains(tag)) {
                                    {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else null
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterSummaryChip(
    filterCount: Int,
    onToggleFilter: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (filterCount > 0) {
        AssistChip(
            onClick = onToggleFilter,
            label = { Text("$filterCount filter${if (filterCount > 1) "s" else ""}") },
            leadingIcon = {
                Icon(
                    Icons.Default.FilterList,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            },
            modifier = modifier
        )
    }
}

@Composable
fun LoadMoreIndicator(
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    if (isLoading) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Loading more...",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

// Extension property untuk TimeFilter display names
val TimeFilter.displayName: String
    get() = when (this) {
        TimeFilter.ALL -> "All Time"
        TimeFilter.TODAY -> "Today"
        TimeFilter.WEEK -> "This Week"
        TimeFilter.MONTH -> "This Month"
    }