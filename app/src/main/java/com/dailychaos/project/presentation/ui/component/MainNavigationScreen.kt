package com.dailychaos.project.presentation.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * DEPRECATED: MainNavigationScreen has been moved to ChaosNavGraph
 *
 * This file is kept for backwards compatibility but should not be used.
 * Use ChaosNavGraph instead for main navigation.
 *
 * "Navigation lama sudah diganti dengan yang lebih clean di ChaosNavGraph!"
 */

@Deprecated(
    message = "Use ChaosNavGraph instead for better navigation structure",
    replaceWith = ReplaceWith(
        "ChaosNavGraph(navController, mainViewModel)",
        "com.dailychaos.project.presentation.ui.navigation.ChaosNavGraph"
    ),
    level = DeprecationLevel.WARNING
)
@Composable
fun MainNavigationScreen(
    onNavigateToAuth: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // This component is deprecated and should not be used
    // Navigation is now handled by ChaosNavGraph for better organization
    //
    // Migration guide:
    // 1. Replace MainNavigationScreen usage with ChaosNavGraph
    // 2. Remove this file after confirming no usages remain
    // 3. Update any remaining references to use the new navigation structure
}