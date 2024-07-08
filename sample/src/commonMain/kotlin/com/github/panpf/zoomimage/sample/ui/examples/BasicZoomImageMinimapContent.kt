package com.github.panpf.zoomimage.sample.ui.examples

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.github.panpf.sketch.rememberAsyncImagePainter
import com.github.panpf.sketch.request.ComposableImageRequest

@Composable
fun BasicZoomImageMinimapContent(sketchImageUri: String) {
    Image(
        painter = rememberAsyncImagePainter(ComposableImageRequest(sketchImageUri) {
            crossfade()
        }),
        contentDescription = "Minimap",
        modifier = Modifier.fillMaxSize()
    )
}