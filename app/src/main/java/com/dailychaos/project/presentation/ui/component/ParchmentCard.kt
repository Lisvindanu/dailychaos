package com.dailychaos.project.presentation.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * ParchmentCard - Sebuah Card kustom dengan style kertas tua.
 *
 * Card ini secara otomatis akan memiliki warna latar belakang `OldPaperHighlight`
 * dan border berwarna `FadedBrown` sesuai dengan tema Parchment.
 *
 * @param modifier Modifier untuk kustomisasi dari luar.
 * @param content Konten yang akan ditampilkan di dalam card.
 */
@Composable
fun ParchmentCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        // Mengatur warna dan elevasi kartu
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant // Ini adalah OldPaperHighlight
        ),
        // Menambahkan border
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline), // Ini adalah FadedBrown
        // Menggunakan bentuk yang sudah didefinisikan di tema
        shape = MaterialTheme.shapes.medium,
        content = content
    )
}