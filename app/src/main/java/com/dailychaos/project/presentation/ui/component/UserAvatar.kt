package com.dailychaos.project.presentation.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import kotlin.math.abs // Import the 'abs' function

/**
 * User Avatar Component
 *
 * "Avatar untuk user anonim dengan warna yang unik berdasarkan username"
 */

@Composable
fun UserAvatar(
    username: String,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    val backgroundColor = remember(username) {
        val colors = listOf(
            Color(0xFFE57373), Color(0xFF81C784), Color(0xFF64B5F6),
            Color(0xFFFFB74D), Color(0xFFBA68C8), Color(0xFF4FC3F7)
        )
        // FIX: Use the absolute value of the hash code to prevent a negative index.
        colors[abs(username.hashCode()) % colors.size]
    }

    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        color = backgroundColor
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = username.firstOrNull()?.uppercase() ?: "?",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}