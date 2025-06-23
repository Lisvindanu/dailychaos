package com.dailychaos.project.presentation.theme

import androidx.compose.ui.graphics.Color

// ============================================
// IMPROVED COLOR PALETTE FOR ANDROID APP
// Tema: Warm Paper & Ink - Optimized for accessibility and visual harmony
// ============================================

//KonoSuba Inspired Colors
val ChaosBlue = Color(0xFF2196F3)        // Aqua's signature blue
val ChaosBlueLight = Color(0xFF64B5F6)
val ChaosBlueDark = Color(0xFF1976D2)

val ExplosionRed = Color(0xFFFF5722)     // Megumin's explosion
val ExplosionRedLight = Color(0xFFFF8A65)
val ExplosionRedDark = Color(0xFFD84315)

val DarknessGold = Color(0xFFFFB300)     // Darkness's armor
val DarknessGoldLight = Color(0xFFFFCC02)
val DarknessGoldDark = Color(0xFFFF8F00)

val KazumaGreen = Color(0xFF4CAF50)      // Kazuma's practical green
val KazumaGreenLight = Color(0xFF81C784)
val KazumaGreenDark = Color(0xFF388E3C)

// --- LIGHT MODE COLORS ---
object LightColors {
 // Primary Colors
 val Primary = Color(0xFF8D6E63)         // Warm brown - lebih saturated dan balanced
 val OnPrimary = Color(0xFFFFFFFF)       // Pure white untuk kontras optimal
 val PrimaryContainer = Color(0xFFD7CCC8) // Light brown container
 val OnPrimaryContainer = Color(0xFF3E2723) // Dark brown text

 // Secondary Colors
 val Secondary = Color(0xFFFF8F00)       // Warm amber - lebih vibrant dari gold
 val OnSecondary = Color(0xFF000000)     // Black untuk kontras
 val SecondaryContainer = Color(0xFFFFE0B2) // Light amber container
 val OnSecondaryContainer = Color(0xFF4E2C00) // Dark amber text

 // Tertiary Colors
 val Tertiary = Color(0xFF0277BD)        // Strong blue - lebih saturated
 val OnTertiary = Color(0xFFFFFFFF)      // White untuk kontras
 val TertiaryContainer = Color(0xFFB3E5FC) // Light blue container
 val OnTertiaryContainer = Color(0xFF01579B) // Dark blue text

 // Background & Surface
 val Background = Color(0xFFFFFBFE)      // Slight warm white
 val OnBackground = Color(0xFF1C1B1F)    // Near black dengan hint warm
 val Surface = Color(0xFFFEF7FF)         // Very light warm surface
 val OnSurface = Color(0xFF1C1B1F)       // Consistent dengan OnBackground
 val SurfaceVariant = Color(0xFFF4EFF4)   // Subtle warm variant
 val OnSurfaceVariant = Color(0xFF49454F) // Medium gray dengan warm undertone

 // Error Colors
 val Error = Color(0xFFBA1A1A)           // Standard Material error
 val OnError = Color(0xFFFFFFFF)         // White
 val ErrorContainer = Color(0xFFFFDAD6)  // Light red container
 val OnErrorContainer = Color(0xFF410002) // Dark red text

 // Outline & Dividers
 val Outline = Color(0xFF79747E)         // Medium gray outline
 val OutlineVariant = Color(0xFFCAC4D0)  // Light gray outline
 val Scrim = Color(0xFF000000)           // Standard scrim


}

// --- DARK MODE COLORS ---
object DarkColors {
 // Primary Colors
 val Primary = Color(0xFFD7CCC8)         // Light brown - inverse dari light mode
 val OnPrimary = Color(0xFF3E2723)       // Dark brown
 val PrimaryContainer = Color(0xFF5D4037) // Medium brown container
 val OnPrimaryContainer = Color(0xFFEFEBE9) // Very light brown text

 // Secondary Colors
 val Secondary = Color(0xFFFFCC02)       // Bright amber untuk dark mode
 val OnSecondary = Color(0xFF4E2C00)     // Dark amber
 val SecondaryContainer = Color(0xFFFF8F00) // Medium amber container
 val OnSecondaryContainer = Color(0xFFFFE0B2) // Light amber text

 // Tertiary Colors
 val Tertiary = Color(0xFF40C4FF)        // Light blue untuk dark mode
 val OnTertiary = Color(0xFF01579B)      // Dark blue
 val TertiaryContainer = Color(0xFF0288D1) // Medium blue container
 val OnTertiaryContainer = Color(0xFFB3E5FC) // Light blue text

 // Background & Surface
 val Background = Color(0xFF1C1B1F)      // Dark neutral background
 val OnBackground = Color(0xFFE6E1E5)    // Light text
 val Surface = Color(0xFF1C1B1F)         // Same as background
 val OnSurface = Color(0xFFE6E1E5)       // Light text
 val SurfaceVariant = Color(0xFF49454F)   // Medium dark surface
 val OnSurfaceVariant = Color(0xFFCAC4D0) // Light gray text

 // Error Colors
 val Error = Color(0xFFFFB4AB)           // Light red untuk dark mode
 val OnError = Color(0xFF690005)         // Dark red
 val ErrorContainer = Color(0xFF93000A)  // Medium red container
 val OnErrorContainer = Color(0xFFFFDAD6) // Light red text

 // Outline & Dividers
 val Outline = Color(0xFF938F99)         // Medium gray outline
 val OutlineVariant = Color(0xFF49454F)  // Dark gray outline
 val Scrim = Color(0xFF000000)           // Standard scrim
}

// --- SEMANTIC COLORS (untuk kedua mode) ---
object SemanticColors {
 // Success Colors
 val SuccessLight = Color(0xFF4CAF50)
 val OnSuccessLight = Color(0xFFFFFFFF)
 val SuccessDark = Color(0xFF81C784)
 val OnSuccessDark = Color(0xFF1B5E20)

 // Warning Colors
 val WarningLight = Color(0xFFF57C00)
 val OnWarningLight = Color(0xFFFFFFFF)
 val WarningDark = Color(0xFFFFB74D)
 val OnWarningDark = Color(0xFFE65100)

 // Info Colors
 val InfoLight = Color(0xFF2196F3)
 val OnInfoLight = Color(0xFFFFFFFF)
 val InfoDark = Color(0xFF64B5F6)
 val OnInfoDark = Color(0xFF0D47A1)
}

// --- GRADIENTS (opsional untuk efek visual) ---
object Gradients {
 val WarmPaper = listOf(
  Color(0xFFFFFBFE),
  Color(0xFFFEF7FF)
 )
 val WoodGrain = listOf(
  Color(0xFF8D6E63),
  Color(0xFF5D4037)
 )
 val GoldenHour = listOf(
  Color(0xFFFFCC02),
  Color(0xFFFF8F00)
 )
}
