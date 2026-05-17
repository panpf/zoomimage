package com.github.panpf.zoomimage.sample.ui.examples

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.github.panpf.zoomimage.sample.image.sketchUri2CoilModel

@Composable
fun CoilZoomImageMinimapContent(sketchImageUri: String) {
    val context = LocalPlatformContext.current
    val coilModel = remember(sketchImageUri) {
        sketchUri2CoilModel(context, sketchImageUri)
    }
    coil3.compose.AsyncImage(
        model = ImageRequest.Builder(context).apply {
            data(coilModel)
            crossfade(true)
        }.build(),
        contentDescription = "Minimap",
        modifier = Modifier.fillMaxSize()
    )
}