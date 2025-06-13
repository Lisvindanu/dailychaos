package com.dailychaos.project.presentation.ui.component

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dailychaos.project.domain.model.SupportType
import com.dailychaos.project.presentation.theme.DailyChaosTheme
import kotlinx.coroutines.delay

/**
 * Support Button Components
 *
 * "Seperti party support di KonoSuba - saling backup satu sama lain!"
 */

@Composable
fun SupportButton(
    supportType: SupportType,
    count: Int = 0,
    isSupported: Boolean = false,
    onSupportClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    showCount: Boolean = true,
    enabled: Boolean = true
) {
    var isPressed by remember { mutableStateOf(false) }

    // Animation untuk button press
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "button_scale"
    )

    // Animation untuk support state
    val supportScale by animateFloatAsState(
        targetValue = if (isSupported) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "support_scale"
    )

    // Heart animation untuk HEART type
    var showHeartAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(isSupported) {
        if (isSupported && supportType == SupportType.HEART) {
            showHeartAnimation = true
            delay(1000)
            showHeartAnimation = false
        }
    }

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(
                color = if (isSupported) {
                    getSupportColor(supportType).copy(alpha = 0.2f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
            .clickable(enabled = enabled) {
                isPressed = true
                onSupportClick()
            }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Emoji with animation
            Box(
                modifier = Modifier.scale(supportScale),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = supportType.emoji,
                    fontSize = 18.sp
                )

                // Floating hearts animation
                if (showHeartAnimation && supportType == SupportType.HEART) {
                    FloatingHearts()
                }
            }

            // Count (if enabled and > 0)
            if (showCount && count > 0) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (isSupported) {
                        getSupportColor(supportType)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@Composable
fun SupportButtonGroup(
    supportCounts: Map<SupportType, Int>,
    userSupports: Set<SupportType> = emptySet(),
    onSupportClick: (SupportType) -> Unit = {},
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(SupportType.values()) { supportType ->
            SupportButton(
                supportType = supportType,
                count = supportCounts[supportType] ?: 0,
                isSupported = supportType in userSupports,
                onSupportClick = { onSupportClick(supportType) },
                enabled = enabled
            )
        }
    }
}

@Composable
fun CompactSupportButton(
    totalSupports: Int,
    topSupportType: SupportType? = null,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (topSupportType != null) {
                Text(
                    text = topSupportType.emoji,
                    fontSize = 14.sp
                )
            }

            Icon(
                Icons.Default.Favorite,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )

            Text(
                text = if (totalSupports > 0) totalSupports.toString() else "Support",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun SupportDialog(
    onDismiss: () -> Unit,
    onSupportClick: (SupportType) -> Unit,
    currentSupports: Set<SupportType> = emptySet(),
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Send Support 💙",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column {
                Text(
                    text = "Show some love to your chaos twin!",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Support options
                SupportType.values().forEach { supportType ->
                    SupportOptionRow(
                        supportType = supportType,
                        isSelected = supportType in currentSupports,
                        onClick = { onSupportClick(supportType) }
                    )

                    if (supportType != SupportType.values().last()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        },
        modifier = modifier
    )
}

@Composable
private fun SupportOptionRow(
    supportType: SupportType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            getSupportColor(supportType).copy(alpha = 0.1f)
        } else {
            Color.Transparent
        },
        label = "background_color"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = supportType.emoji,
            fontSize = 24.sp,
            modifier = Modifier.padding(end = 12.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = supportType.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = supportType.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (isSelected) {
            Icon(
                Icons.Default.Favorite,
                contentDescription = "Selected",
                tint = getSupportColor(supportType),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun FloatingHearts() {
    var showHearts by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(1000)
        showHearts = false
    }

    AnimatedVisibility(
        visible = showHearts,
        enter = scaleIn() + fadeIn(),
        exit = scaleOut() + fadeOut()
    ) {
        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center
        ) {
            repeat(3) { index ->
                val offset by animateFloatAsState(
                    targetValue = if (showHearts) -20f else 0f,
                    animationSpec = tween(
                        durationMillis = 800,
                        delayMillis = index * 100
                    ),
                    label = "heart_offset_$index"
                )

                Text(
                    text = "💙",
                    fontSize = 12.sp,
                    modifier = Modifier.offset(y = offset.dp)
                )
            }
        }
    }
}

@Composable
private fun getSupportColor(supportType: SupportType): Color {
    return when (supportType) {
        SupportType.HEART -> Color(0xFF2196F3) // Blue
        SupportType.HUG -> Color(0xFFFF9800) // Orange
        SupportType.SOLIDARITY -> Color(0xFF4CAF50) // Green
        SupportType.STRENGTH -> Color(0xFF9C27B0) // Purple
        SupportType.HOPE -> Color(0xFFFFEB3B) // Yellow
    }
}

@Preview(showBackground = true)
@Composable
fun SupportButtonPreview() {
    DailyChaosTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Individual support buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SupportButton(
                    supportType = SupportType.HEART,
                    count = 5,
                    isSupported = true
                )

                SupportButton(
                    supportType = SupportType.HUG,
                    count = 3,
                    isSupported = false
                )

                SupportButton(
                    supportType = SupportType.SOLIDARITY,
                    count = 8,
                    isSupported = true
                )
            }

            // Support button group
            SupportButtonGroup(
                supportCounts = mapOf(
                    SupportType.HEART to 12,
                    SupportType.HUG to 8,
                    SupportType.SOLIDARITY to 5,
                    SupportType.STRENGTH to 3,
                    SupportType.HOPE to 7
                ),
                userSupports = setOf(SupportType.HEART, SupportType.HOPE)
            )

            // Compact support button
            CompactSupportButton(
                totalSupports = 25,
                topSupportType = SupportType.HEART
            )
        }
    }
}