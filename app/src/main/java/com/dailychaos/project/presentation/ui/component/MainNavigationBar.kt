package com.dailychaos.project.presentation.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * MainNavigationBar - Bottom navigation bar kustom dengan tema Parchment.
 *
 * Membungkus NavigationBar standar untuk memberikan style kustom (latar belakang kertas tua dan border atas).
 *
 * @param modifier Modifier untuk kustomisasi dari luar.
 * @param content Berisi semua `NavigationBarItem` yang akan ditampilkan.
 */
@Composable
fun MainNavigationBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    // Surface sebagai container utama.
    Surface(
        modifier = modifier.fillMaxWidth(),
        // Menggunakan surfaceVariant sebagai latar belakang OldPaperHighlight
        color = MaterialTheme.colorScheme.surfaceVariant,
        // Memberi bayangan agar bar terangkat dari konten di belakangnya
        shadowElevation = 8.dp
    ) {
        Column {
            // Divider sebagai border cokelat di bagian atas
            Divider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)

            NavigationBar(
                modifier = Modifier,
                // Warna container NavigationBar dibuat transparan karena sudah di-handle oleh Surface
                containerColor = Color.Transparent,
                // Matikan shadow/elevation bawaan karena sudah di-handle oleh Surface
                tonalElevation = 0.dp,
                // Konten (semua NavigationBarItem) dimasukkan dari luar
                content = content
            )
        }
    }
}