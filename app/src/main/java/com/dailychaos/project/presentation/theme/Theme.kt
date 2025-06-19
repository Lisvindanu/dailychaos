package com.dailychaos.project.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Mendefinisikan skema warna untuk Light Mode
private val LightColorScheme = lightColorScheme(
    primary = WoodBrown,
    onPrimary = Parchment,
    primaryContainer = BorderBrown,
    onPrimaryContainer = InkColor,

    secondary = GoldAccent,
    onSecondary = InkColor,
    secondaryContainer = GoldAccent.copy(alpha = 0.2f),
    onSecondaryContainer = WoodBrown,

    tertiary = BlueAccent,
    onTertiary = InkColor,
    tertiaryContainer = BlueAccent.copy(alpha = 0.2f),
    onTertiaryContainer = WoodBrown,

    error = ErrorRed,
    onError = Parchment,
    errorContainer = ErrorRed.copy(alpha = 0.2f),
    onErrorContainer = ErrorRed,

    background = Parchment,
    onBackground = InkColor,
    surface = HighlightPaper,
    onSurface = InkColor,

    surfaceVariant = HighlightPaper,
    onSurfaceVariant = InkColor.copy(alpha = 0.7f),
    outline = BorderBrown
)

// Mendefinisikan skema warna untuk Dark Mode
private val DarkColorScheme = darkColorScheme(
    primary = LightWoodBrown,
    onPrimary = DarkSurface,
    primaryContainer = WoodBrown,
    onPrimaryContainer = DarkInkColor,

    secondary = LightGoldAccent,
    onSecondary = DarkSurface,
    secondaryContainer = GoldAccent.copy(alpha = 0.3f),
    onSecondaryContainer = LightGoldAccent,

    tertiary = LightBlueAccent,
    onTertiary = DarkSurface,
    tertiaryContainer = BlueAccent.copy(alpha = 0.3f),
    onTertiaryContainer = LightBlueAccent,

    error = ErrorRed,
    onError = DarkSurface,
    errorContainer = ErrorRed.copy(alpha = 0.3f),
    onErrorContainer = ErrorRed,

    background = DarkSurface,
    onBackground = DarkInkColor,
    surface = DarkHighlight,
    onSurface = DarkInkColor,

    surfaceVariant = DarkHighlight,
    onSurfaceVariant = DarkInkColor.copy(alpha = 0.7f),
    outline = WoodBrown
)

@Composable
fun DailyChaosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color di-nonaktifkan agar tema kita yang berlaku
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Memilih color scheme berdasarkan tema sistem
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Status bar mengikuti warna latar belakang utama
            window.statusBarColor = colorScheme.background.toArgb()
            // Ikon status bar akan terang di tema gelap, dan gelap di tema terang
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ChaosTypography,
        shapes = ChaosShapes,
        content = content
    )
}