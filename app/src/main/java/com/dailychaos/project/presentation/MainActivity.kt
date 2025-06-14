package com.dailychaos.project.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.dailychaos.project.R
import com.dailychaos.project.presentation.theme.DailyChaosTheme
import com.dailychaos.project.presentation.ui.navigation.ChaosNavGraph
import dagger.hilt.android.AndroidEntryPoint

/**
 * MainActivity - Entry point aplikasi Daily Chaos
 *
 * "Seperti guild hall dimana semua petualangan dimulai!"
 */
//@AndroidEntryPoint
//class MainActivity : ComponentActivity() {
//    private val mainViewModel: MainViewModel by viewModels()
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//
//        setContent {
//            DailyChaosTheme {
//                DailyChaosApp(mainViewModel = mainViewModel)
//            }
//        }
//    }
//}
//
//@Composable
//fun DailyChaosApp(mainViewModel: MainViewModel? = null) {
//    val navController = rememberNavController()
//    Surface(
//        modifier = Modifier.fillMaxSize(),
//        color = MaterialTheme.colorScheme.background
//    ) {
//        ChaosNavGraph(
//            navController = navController,
//            mainViewModel = mainViewModel
//        )
//    }
//}


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // ViewModel yang sudah ada dari proyek Anda
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // FIX: Menggunakan Splash Screen API. Karena ViewModel tidak memiliki state loading,
        // splash screen akan hilang setelah waktu default. Ini akan mengatasi error kompilasi.
        installSplashScreen()

        setContent {
            DailyChaosTheme {
                // Gunakan Box sebagai root untuk menampung latar belakang dan konten
                Box(modifier = Modifier.fillMaxSize()) {
                    // 1. Gambar sebagai Latar Belakang
                    Image(
                        painter = painterResource(id = R.drawable.old_paper_bg_01),
                        contentDescription = "Parchment Background",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // 2. Konten Aplikasi di atas Latar Belakang
                    // Struktur asli proyek memberikan seluruh ViewModel, mari kita ikuti itu.
                    val navController = rememberNavController()
                    ChaosNavGraph(
                        navController = navController,
                        mainViewModel = mainViewModel
                    )
                }
            }
        }
    }
}
