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


// Light Theme Color Scheme
//private val LightColorScheme = lightColorScheme(
//    primary = ChaosBlue,
//    onPrimary = ChaosWhite,
//    primaryContainer = ChaosBlueLight,
//    onPrimaryContainer = ChaosBlueDark,
//
//    secondary = KazumaGreen,
//    onSecondary = ChaosWhite,
//    secondaryContainer = KazumaGreenLight,
//    onSecondaryContainer = KazumaGreenDark,
//
//    tertiary = DarknessGold,
//    onTertiary = ChaosWhite,
//    tertiaryContainer = DarknessGoldLight,
//    onTertiaryContainer = DarknessGoldDark,
//
//    error = ErrorLight,
//    onError = OnErrorLight,
//    errorContainer = Color(0xFFFFDAD6),
//    onErrorContainer = Color(0xFF410002),
//
//    background = ChaosWhite,
//    onBackground = ChaosBlack,
//    surface = SurfaceLight,
//    onSurface = ChaosBlack,
//    surfaceVariant = SurfaceVariantLight,
//    onSurfaceVariant = ChaosGray,
//
//    outline = ChaosGrayLight,
//    outlineVariant = Color(0xFFCAC4D0),
//    scrim = ChaosBlack,
//    inverseSurface = Color(0xFF313033),
//    inverseOnSurface = Color(0xFFF4EFF4),
//    inversePrimary = Color(0xFFB6C2FF)
//)
//
//// Dark Theme Color Scheme
//private val DarkColorScheme = darkColorScheme(
//    primary = ChaosBlueLight,
//    onPrimary = ChaosBlueDark,
//    primaryContainer = ChaosBlueDark,
//    onPrimaryContainer = ChaosBlueLight,
//
//    secondary = KazumaGreenLight,
//    onSecondary = KazumaGreenDark,
//    secondaryContainer = KazumaGreenDark,
//    onSecondaryContainer = KazumaGreenLight,
//
//    tertiary = DarknessGoldLight,
//    onTertiary = DarknessGoldDark,
//    tertiaryContainer = DarknessGoldDark,
//    onTertiaryContainer = DarknessGoldLight,
//
//    error = ErrorDark,
//    onError = OnErrorDark,
//    errorContainer = Color(0xFF93000A),
//    onErrorContainer = Color(0xFFFFDAD6),
//
//    background = Color(0xFF1C1B1F),
//    onBackground = Color(0xFFE6E1E5),
//    surface = SurfaceDark,
//    onSurface = Color(0xFFE6E1E5),
//    surfaceVariant = SurfaceVariantDark,
//    onSurfaceVariant = Color(0xFFCAC4D0),
//
//    outline = Color(0xFF938F99),
//    outlineVariant = Color(0xFF49454F),
//    scrim = ChaosBlack,
//    inverseSurface = Color(0xFFE6E1E5),
//    inverseOnSurface = Color(0xFF313033),
//    inversePrimary = Color(0xFF5A5FFF)
//)
//
//@Composable
//fun DailyChaosTheme(
//    darkTheme: Boolean = isSystemInDarkTheme(),
//    // Dynamic color is available on Android 12+
//    dynamicColor: Boolean = true,
//    content: @Composable () -> Unit
//) {
//    val colorScheme = when {
//        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
//            val context = LocalContext.current
//            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
//        }
//        darkTheme -> DarkColorScheme
//        else -> LightColorScheme
//    }
//
//    val view = LocalView.current
//    if (!view.isInEditMode) {
//        SideEffect {
//            val window = (view.context as Activity).window
//            window.statusBarColor = colorScheme.primary.toArgb()
//            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
//        }
//    }
//
//    MaterialTheme(
//        colorScheme = colorScheme,
//        typography = ChaosTypography,
//        shapes = ChaosShapes,
//        content = content
//    )
//}
//
//// Backward compatibility
//@Composable
//fun ProjectTheme(
//    darkTheme: Boolean = isSystemInDarkTheme(),
//    dynamicColor: Boolean = true,
//    content: @Composable () -> Unit
//) {
//    DailyChaosTheme(
//        darkTheme = darkTheme,
//        dynamicColor = dynamicColor,
//        content = content
//    )
//}


// Membuat skema warna untuk tema terang (Light Theme) menggunakan warna Parchment.
private val ParchmentLightColorScheme = lightColorScheme(
    primary = BurntBrown,
    onPrimary = Parchment,
    secondary = Sepia,
    onSecondary = Parchment,
    background = Parchment, // Dibuat transparan agar gambar latar terlihat
    surface = Parchment,    // Dibuat transparan agar gambar latar terlihat
    onBackground = InkBlack,
    onSurface = InkBlack,
    error = Color(0xFFB00020),
    onError = Color.White,
    surfaceVariant = OldPaperHighlight, // Warna untuk komponen seperti Card
    onSurfaceVariant = InkBlack,
    outline = FadedBrown
)


@Composable
fun DailyChaosTheme(
    darkTheme: Boolean = false, // Kita paksa light theme agar efek kertasnya maksimal
    content: @Composable () -> Unit
) {
    val colorScheme = ParchmentLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Gunakan warna gelap untuk status bar agar kontras dengan ikon yang terang
            window.statusBarColor = BurntBrown.toArgb()
            // Karena status bar gelap, ikon di atasnya harus terang
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        // FIX: Menggunakan nama Typography dan Shapes yang ada di proyek Anda
        typography = ChaosTypography,
        shapes = ChaosShapes,
        content = content
    )
}