package com.dailychaos.project.presentation.ui.component

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.*

/**
 * Floating Action Menu Component
 *
 * "FAB menu yang expandable untuk multiple actions"
 */

@Composable
fun FloatingActionMenu(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onNewChaosEntry: () -> Unit,
    onShareToCommunity: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End
    ) {
        // Menu items
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Share to community
                FloatingActionMenuItem(
                    icon = Icons.Default.Share,
                    label = "Share to Community",
                    onClick = onShareToCommunity
                )

                // New chaos entry
                FloatingActionMenuItem(
                    icon = Icons.Default.Add,
                    label = "New Chaos Entry",
                    onClick = onNewChaosEntry
                )
            }
        }

        // Main FAB
        FloatingActionButton(
            onClick = onToggle,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.Add,
                contentDescription = if (isExpanded) "Close menu" else "Open menu",
                modifier = Modifier.rotate(if (isExpanded) 45f else 0f)
            )
        }
    }
}

@Composable
private fun FloatingActionMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        // Label
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = MaterialTheme.colorScheme.inverseSurface,
            modifier = Modifier.padding(end = 16.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.inverseOnSurface,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        // Mini FAB
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}