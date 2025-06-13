package com.dailychaos.project.presentation.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*

/**
 * Chaos Filter Chips Component
 *
 * "Horizontal scrollable chips untuk filtering chaos entries"
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChaosFilterChips(
    selectedFilters: Set<ChaosFilter>,
    onFilterToggle: (ChaosFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(ChaosFilter.values()) { filter ->
            FilterChip(
                selected = filter in selectedFilters,
                onClick = { onFilterToggle(filter) },
                label = { Text(filter.displayName) },
                leadingIcon = {
                    Text(
                        text = filter.emoji,
                        fontSize = 16.sp
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

enum class ChaosFilter(val displayName: String, val emoji: String) {
    TODAY("Today", "📅"),
    THIS_WEEK("This Week", "📊"),
    HIGH_CHAOS("High Chaos", "🔥"),
    LOW_CHAOS("Peaceful", "😌"),
    WITH_WINS("With Wins", "🏆"),
    SHARED("Shared", "🤝")
}