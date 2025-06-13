package com.dailychaos.project.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val ChaosShapes = Shapes(
    // Small components - buttons, chips
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),

    // Medium components - cards, dialogs
    medium = RoundedCornerShape(12.dp),

    // Large components - sheets, large surfaces
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)
