package com.dailychaos.project.presentation.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dailychaos.project.domain.model.ChaosLevel
import com.dailychaos.project.presentation.theme.DailyChaosTheme
import kotlin.math.cos
import kotlin.math.sin

/**
 * Chaos Level Picker Component
 *
 * "Seperti skill tree Megumin - dari peaceful sampai EXPLOSION!"
 */
@Composable
fun ChaosLevelPicker(
    selectedLevel: Int = 5,
    onLevelChanged: (Int) -> Unit = {},
    modifier: Modifier = Modifier,
    showAnimation: Boolean = true
) {
    val chaosLevel = ChaosLevel.fromValue(selectedLevel)

    // Animation untuk explosion effect
    val infiniteTransition = rememberInfiniteTransition(label = "chaos_animation")
    val animatedScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (showAnimation && selectedLevel >= 8) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale_animation"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "Chaos Level",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Current level display with animation
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = getChaosLevelColor(selectedLevel),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = selectedLevel.toString(),
                    fontSize = (24 * animatedScale).sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Chaos level description
            Text(
                text = "${chaosLevel.emoji} ${chaosLevel.description}",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Custom slider with chaos levels
            ChaosSlider(
                value = selectedLevel,
                onValueChange = onLevelChanged,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Quick level buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickLevelButton(
                    level = 1,
                    emoji = "😴",
                    isSelected = selectedLevel == 1,
                    onClick = { onLevelChanged(1) }
                )
                QuickLevelButton(
                    level = 5,
                    emoji = "🌪️",
                    isSelected = selectedLevel == 5,
                    onClick = { onLevelChanged(5) }
                )
                QuickLevelButton(
                    level = 10,
                    emoji = "💥",
                    isSelected = selectedLevel == 10,
                    onClick = { onLevelChanged(10) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // KonoSuba quote for current level
            Text(
                text = ChaosLevel.getRandomQuote(chaosLevel),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }
}

@Composable
private fun ChaosSlider(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Level indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            repeat(10) { index ->
                val level = index + 1
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = if (level <= value) getChaosLevelColor(level) else Color.Gray.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Actual slider
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 1f..10f,
            steps = 8, // 10 levels - 2 endpoints = 8 steps
            colors = SliderDefaults.colors(
                thumbColor = getChaosLevelColor(value),
                activeTrackColor = getChaosLevelColor(value).copy(alpha = 0.7f)
            )
        )
    }
}

@Composable
private fun QuickLevelButton(
    level: Int,
    emoji: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(
                if (isSelected) getChaosLevelColor(level) else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = emoji,
                fontSize = 16.sp
            )
            Text(
                text = level.toString(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun getChaosLevelColor(level: Int): Color {
    return when (level) {
        in 1..2 -> MaterialTheme.colorScheme.secondary // Peaceful
        in 3..4 -> MaterialTheme.colorScheme.tertiary // Minor chaos
        in 5..6 -> MaterialTheme.colorScheme.primary // Moderate chaos
        in 7..8 -> MaterialTheme.colorScheme.error // Major chaos
        else -> Color(0xFFFF5722) // EXPLOSION!
    }
}

@Preview(showBackground = true)
@Composable
fun ChaosLevelPickerPreview() {
    DailyChaosTheme {
        var selectedLevel by remember { mutableIntStateOf(7) }

        ChaosLevelPicker(
            selectedLevel = selectedLevel,
            onLevelChanged = { selectedLevel = it },
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ChaosLevelPickerExplosionPreview() {
    DailyChaosTheme {
        ChaosLevelPicker(
            selectedLevel = 10,
            onLevelChanged = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}