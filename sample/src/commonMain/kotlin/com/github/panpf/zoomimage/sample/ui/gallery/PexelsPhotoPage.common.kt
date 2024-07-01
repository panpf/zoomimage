package com.github.panpf.zoomimage.sample.ui.gallery

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen

@Composable
fun Screen.PexelsPhotoPage() {
    Box(Modifier.fillMaxSize()) {
        Text("Pexels Photos", Modifier.align(Alignment.Center))
        // TODO Add Pexels photos
    }
}