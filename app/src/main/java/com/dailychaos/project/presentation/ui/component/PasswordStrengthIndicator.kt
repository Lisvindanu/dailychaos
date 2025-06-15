// File: app/src/main/java/com/dailychaos/project/presentation/ui/component/PasswordStrengthIndicator.kt
package com.dailychaos.project.presentation.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dailychaos.project.util.PasswordStrength

@Composable
fun PasswordStrengthIndicator(
    password: String,
    strength: PasswordStrength,
    modifier: Modifier = Modifier
) {
    if (password.isBlank()) return

    val strengthColor by animateColorAsState(
        targetValue = Color(strength.color),
        animationSpec = tween(300),
        label = "PasswordStrengthColor"
    )

    val progress = when (strength) {
        PasswordStrength.WEAK -> 0.25f
        PasswordStrength.MEDIUM -> 0.5f
        PasswordStrength.STRONG -> 0.75f
        PasswordStrength.VERY_STRONG -> 1f
    }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Kekuatan Password:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = strength.displayName,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = strengthColor
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Progress bars
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(4) { index ->
                val barProgress = when {
                    index < (progress * 4).toInt() -> 1f
                    index == (progress * 4).toInt() -> (progress * 4) % 1
                    else -> 0f
                }

                PasswordStrengthBar(
                    progress = barProgress,
                    color = strengthColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Password tips
        PasswordTips(password = password, strength = strength)
    }
}

@Composable
private fun PasswordStrengthBar(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
    ) {
        val width = size.width
        val height = size.height

        // Background
        drawRoundRect(
            color = Color.Gray.copy(alpha = 0.3f),
            size = Size(width, height),
            cornerRadius = CornerRadius(height / 2, height / 2)
        )

        // Progress
        if (progress > 0) {
            drawRoundRect(
                color = color,
                size = Size(width * progress, height),
                cornerRadius = CornerRadius(height / 2, height / 2)
            )
        }
    }
}

@Composable
private fun PasswordTips(
    password: String,
    strength: PasswordStrength
) {
    val tips = mutableListOf<String>()

    if (password.length < 8) {
        tips.add("• Minimal 8 karakter")
    }

    if (!password.any { it.isLowerCase() }) {
        tips.add("• Tambahkan huruf kecil")
    }

    if (!password.any { it.isUpperCase() }) {
        tips.add("• Tambahkan huruf besar")
    }

    if (!password.any { it.isDigit() }) {
        tips.add("• Tambahkan angka")
    }

    if (!password.any { "!@#$%^&*()_+-=[]{}|;:,.<>?".contains(it) }) {
        tips.add("• Tambahkan simbol")
    }

    if (tips.isNotEmpty() && strength != PasswordStrength.VERY_STRONG) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = "Tips untuk password yang lebih kuat:",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                tips.take(3).forEach { tip ->
                    Text(
                        text = tip,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}