package com.dailychaos.project.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val ChaosShapes = Shapes(
    // Small components - buttons, chips
    extraSmall = RoundedCornerShape(2.dp),
    small = RoundedCornerShape(4.dp),

    // Medium components - cards, dialogs
    medium = RoundedCornerShape(6.dp),

    // Large components - sheets, large surfaces
    large = RoundedCornerShape(8.dp),
    extraLarge = RoundedCornerShape(12.dp)
)
