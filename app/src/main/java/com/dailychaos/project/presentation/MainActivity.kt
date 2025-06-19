package com.dailychaos.project.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.dailychaos.project.data.remote.api.KonoSubaApiService
import com.dailychaos.project.presentation.theme.DailyChaosTheme
import com.dailychaos.project.presentation.ui.navigation.ChaosNavGraph
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    @Inject
    lateinit var konoSubaApi: KonoSubaApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()

        setContent {
            DailyChaosTheme {
                // Latar belakang sekarang menggunakan warna dari tema.
                // Tidak perlu lagi Box dengan gradien hardcoded.
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
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