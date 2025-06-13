package com.dailychaos.project.presentation.ui.component

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.dailychaos.project.domain.model.ChaosLevel
import com.dailychaos.project.presentation.theme.*

/**
 * Chaos Level Picker Component
 *
 * "Slider interaktif untuk memilih tingkat chaos dengan animasi explosion!"
 */

@Composable
fun ChaosLevelPicker(
    chaosLevel: Int,
    onChaosLevelChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedScale by animateFloatAsState(
        targetValue = if (chaosLevel >= 8) 1.2f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "chaos_scale"
    )

    val explosionVisible by remember(chaosLevel) {
        derivedStateOf { chaosLevel >= 9 }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Chaos Level Display
        Card(
            modifier = Modifier
                .scale(animatedScale)
                .size(120.dp),
            shape = CircleShape,
            colors = CardDefaults.cardColors(
                containerColor = when {
                    chaosLevel <= 3 -> KazumaGreen // Green - peaceful
                    chaosLevel <= 6 -> DarknessGold // Orange - moderate
                    chaosLevel <= 8 -> ExplosionRed // Red - high chaos
                    else -> MaterialTheme.colorScheme.secondary // Purple for EXPLOSION
                }
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = ChaosLevel.fromValue(chaosLevel).emoji,
                        style = MaterialTheme.typography.headlineLarge,
                        fontSize = 32.sp
                    )
                    Text(
                        text = chaosLevel.toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                // Explosion Animation - menggunakan Box sebagai parent
                if (explosionVisible) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        ExplosionEffect()
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chaos Level Description
        Text(
            text = ChaosLevel.fromValue(chaosLevel).description,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Slider
        Slider(
            value = chaosLevel.toFloat(),
            onValueChange = { onChaosLevelChange(it.toInt()) },
            valueRange = 1f..10f,
            steps = 8,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )

        // Level indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("😴", fontSize = 16.sp)
            Text("💥", fontSize = 16.sp)
        }
    }
}

@Composable
private fun ExplosionEffect() {
    val infiniteTransition = rememberInfiniteTransition(label = "explosion")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "explosion_scale"
    )

    Text(
        text = "💥",
        fontSize = (24 * scale).sp,
        modifier = Modifier.scale(scale)
    )
}