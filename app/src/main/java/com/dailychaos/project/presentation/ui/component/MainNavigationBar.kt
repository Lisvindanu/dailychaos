package com.dailychaos.project.presentation.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.*

/**
 * Main Navigation Bar Component
 *
 * "Bottom navigation bar dengan animasi emoji untuk selected state"
 */

@Composable
fun MainNavigationBar(
    selectedRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            NavigationItem("home", "Home", "🏠", Icons.Default.Home),
            NavigationItem("journal", "Journal", "📝", Icons.Default.Book),
            NavigationItem("community", "Community", "🤝", Icons.Default.People),
            NavigationItem("profile", "Profile", "👤", Icons.Default.Person)
        )

        items.forEach { item ->
            NavigationBarItem(
                selected = selectedRoute == item.route,
                onClick = { onNavigate(item.route) },
                icon = {
                    if (selectedRoute == item.route) {
                        Text(
                            text = item.emoji,
                            fontSize = 24.sp
                        )
                    } else {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label
                        )
                    }
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                alwaysShowLabel = true
            )
        }
    }
}

private data class NavigationItem(
    val route: String,
    val label: String,
    val emoji: String,
    val icon: ImageVector
)