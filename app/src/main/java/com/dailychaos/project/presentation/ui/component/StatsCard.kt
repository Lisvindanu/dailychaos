package com.dailychaos.project.presentation.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*

/**
 * Stats Card Component
 *
 * "Card untuk menampilkan statistik user dengan trend indicator"
 */

@Composable
fun StatsCard(
    title: String,
    value: String,
    icon: String,
    subtitle: String? = null,
    trend: StatsCardTrend? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = icon,
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                trend?.let {
                    TrendIndicator(trend = it)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            subtitle?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TrendIndicator(trend: StatsCardTrend) {
    val (icon, color) = when (trend) {
        StatsCardTrend.UP -> Icons.Default.TrendingUp to Color.Green
        StatsCardTrend.DOWN -> Icons.Default.TrendingDown to Color.Red
        StatsCardTrend.STABLE -> Icons.Default.TrendingFlat to Color.Gray
    }

    Icon(
        imageVector = icon,
        contentDescription = "Trend",
        tint = color,
        modifier = Modifier.size(16.dp)
    )
}

enum class StatsCardTrend {
    UP, DOWN, STABLE
}