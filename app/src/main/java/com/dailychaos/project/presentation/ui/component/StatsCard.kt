package com.dailychaos.project.presentation.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Stats Card Component
 *
 * "Card untuk menampilkan statistik user dengan trend indicator"
 */

enum class StatsCardTrend {
    UP, DOWN, STABLE
}

@Composable
fun StatsCard(
    title: String,
    value: String,
    // Parameter 'icon' tidak digunakan di layout ini, bisa dihapus jika mau
    // icon: String,
    subtitle: String? = null,
    trend: StatsCardTrend? = null,
    modifier: Modifier = Modifier
) {
    // Menggunakan ParchmentCard yang sudah kita buat
    ParchmentCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary
            )
            if (subtitle != null || trend != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (trend != null) {
                        TrendIndicator(trend = trend)
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrendIndicator(trend: StatsCardTrend) {
    val (icon, color) = when (trend) {
        StatsCardTrend.UP -> Icons.Default.TrendingUp to MaterialTheme.colorScheme.secondary
        StatsCardTrend.DOWN -> Icons.Default.TrendingDown to MaterialTheme.colorScheme.error
        StatsCardTrend.STABLE -> Icons.Default.TrendingFlat to MaterialTheme.colorScheme.outline
    }

    Icon(
        imageVector = icon,
        contentDescription = "Trend",
        tint = color,
        modifier = Modifier.size(16.dp)
    )
}