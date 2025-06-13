package com.dailychaos.project.presentation.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*

/**
 * Animated Counter Component
 *
 * "Counter dengan animasi smooth untuk menampilkan statistik"
 */

@Composable
fun AnimatedCounter(
    count: Int,
    label: String,
    modifier: Modifier = Modifier
) {
    val animatedCount by animateIntAsState(
        targetValue = count,
        animationSpec = tween(durationMillis = 1000),
        label = "counter_animation"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = animatedCount.toString(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}