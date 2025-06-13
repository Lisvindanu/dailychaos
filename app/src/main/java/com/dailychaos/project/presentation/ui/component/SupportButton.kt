package com.dailychaos.project.presentation.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.*
import com.dailychaos.project.domain.model.SupportType

/**
 * Support Button Component
 *
 * "Button untuk memberikan dukungan emosional dengan animasi yang lembut"
 */

@Composable
fun SupportButton(
    supportType: SupportType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "support_button_scale"
    )

    Button(
        onClick = {
            onClick()
            // Add haptic feedback here if needed
        },
        modifier = modifier.scale(animatedScale),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = supportType.emoji,
            fontSize = 16.sp
        )
    }
}