// File: app/src/main/java/com/dailychaos/project/presentation/ui/component/TestLauncher.kt
package com.dailychaos.project.presentation.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dailychaos.project.BuildConfig

/**
 * Floating Action Button untuk testing features
 * Hanya muncul di DEBUG build
 */
@Composable
fun TestLauncher(
    onNavigateToTest: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Hanya tampil di DEBUG build
    if (BuildConfig.DEBUG) {
        var isExpanded by remember { mutableStateOf(false) }

        Box(
            modifier = modifier.padding(16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Test options (hanya muncul saat expanded)
                if (isExpanded) {
                    // Community Test Button
                    ExtendedFloatingActionButton(
                        onClick = {
                            onNavigateToTest()
                            isExpanded = false
                        },
                        icon = { Icon(Icons.Default.FilterList, contentDescription = null) },
                        text = { Text("Test Pagination") },
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                }

                // Main FAB
                FloatingActionButton(
                    onClick = { isExpanded = !isExpanded },
                    containerColor = if (isExpanded)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.BugReport,
                        contentDescription = if (isExpanded) "Close" else "Test Features"
                    )
                }
            }
        }
    }
}

/**
 * Cara menggunakan di screen:
 *
 * Box(modifier = Modifier.fillMaxSize()) {
 *     // Your screen content
 *
 *     TestLauncher(
 *         onNavigateToTest = {
 *             navController.navigate(ChaosDestinations.COMMUNITY_TEST_ROUTE)
 *         },
 *         modifier = Modifier.align(Alignment.BottomEnd)
 *     )
 * }
 */