package com.dailychaos.project.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.dailychaos.project.presentation.theme.DailyChaosTheme
import com.dailychaos.project.presentation.ui.navigation.ChaosNavGraph
import dagger.hilt.android.AndroidEntryPoint

/**
 * MainActivity - Entry point aplikasi Daily Chaos
 *
 * "Seperti guild hall dimana semua petualangan dimulai!"
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DailyChaosTheme {
                DailyChaosApp(mainViewModel = mainViewModel)
            }
        }
    }
}

@Composable
fun DailyChaosApp(mainViewModel: MainViewModel? = null) {
    val navController = rememberNavController()
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        ChaosNavGraph(
            navController = navController,
            mainViewModel = mainViewModel
        )
    }
}