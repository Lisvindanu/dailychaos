// File: app/src/main/java/com/dailychaos/project/presentation/ui/theme/ChaosColors.kt
package com.dailychaos.project.presentation.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * ChaosColors Object - Color constants for Daily Chaos theme
 *
 * "Warna-warna yang konsisten untuk UI yang chaos tapi tetap rapi"
 */
object ChaosColors {
    // Primary colors - using your defined parchment theme
    val primary = Color(0xFF6B4F3A)        // BurntBrown
    val primaryLight = Color(0xFFA1887F)   // FadedBrown
    val primaryDark = Color(0xFF704214)    // Sepia

    // Secondary colors - KonoSuba inspired
    val secondary = Color(0xFF4CAF50)      // KazumaGreen
    val secondaryLight = Color(0xFF81C784) // KazumaGreenLight
    val secondaryDark = Color(0xFF388E3C)  // KazumaGreenDark

    // Surface colors - Parchment theme
    val background = Color(0xFFF5EEDC)     // Parchment
    val surface = Color(0xFFFFF8E1)        // OldPaperHighlight
    val surfaceVariant = Color(0xFFF5F5DC)

    // Text colors
    val onBackground = Color(0xFF2C2B2A)   // InkBlack
    val onSurface = Color(0xFF2C2B2A)      // InkBlack
    val onPrimary = Color(0xFFF5EEDC)      // Parchment
    val onSecondary = Color(0xFFFFFBFE)    // ChaosWhite

    // Status colors
    val success = Color(0xFF4CAF50)        // KazumaGreen
    val warning = Color(0xFFFFB300)        // DarknessGold
    val error = Color(0xFFFF5722)          // ExplosionRed
    val info = Color(0xFF2196F3)           // ChaosBlue

    // Neutral colors
    val white = Color(0xFFFFFBFE)          // ChaosWhite
    val black = Color(0xFF1C1B1F)          // ChaosBlack
    val gray = Color(0xFF49454F)           // ChaosGray
    val grayLight = Color(0xFF79747E)      // ChaosGrayLight
    val grayDark = Color(0xFF1C1B1F)       // ChaosGrayDark
}