package com.dailychaos.project.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.dailychaos.project.data.remote.api.KonoSubaApiService
import com.dailychaos.project.presentation.theme.DailyChaosTheme
import com.dailychaos.project.presentation.ui.navigation.ChaosNavGraph
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * MainActivity - Entry point aplikasi Daily Chaos
 *
 * "Seperti guild hall dimana semua petualangan dimulai!"
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // ViewModel yang sudah ada dari proyek Anda
    private val mainViewModel: MainViewModel by viewModels()

    // Inject KonoSuba API Service
    @Inject
    lateinit var konoSubaApi: KonoSubaApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Install splash screen API
        installSplashScreen()

        setContent {
            DailyChaosTheme {
                // Box sebagai container untuk latar belakang gradasi
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            // Terapkan gradasi yang sama dari ProfileScreen
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFFFF3E0), // Warna atas (lebih terang)
                                    Color(0xFFFFE0B2)  // Warna bawah (sedikit lebih gelap)
                                )
                            )
                        )
                ) {
                    // Surface dibuat transparan agar gradasi dari Box terlihat
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.Transparent
                    ) {
                        val navController = rememberNavController()
                        ChaosNavGraph(
                            navController = navController,
                            mainViewModel = mainViewModel,
                            konoSubaApiService = konoSubaApi
                        )
                    }
                }
            }
        }
    }
}