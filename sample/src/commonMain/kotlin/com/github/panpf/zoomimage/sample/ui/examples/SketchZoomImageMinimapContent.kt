package com.github.panpf.zoomimage.sample.ui.examples

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.github.panpf.sketch.request.ComposableImageRequest

@Composable
fun SketchZoomImageMinimapContent(sketchImageUri: String) {
    com.github.panpf.sketch.AsyncImage(
        request = ComposableImageRequest(sketchImageUri) {
            crossfade()
        },
        contentDescription = "Minimap",
        modifier = Modifier.fillMaxSize()
    )
}