package com.github.panpf.zoomimage.sample.ui.examples.compose

import androidx.compose.runtime.Composable

enum class ZoomImageType(
    val title: String,
    val drawContent: @Composable (sketchImageUri: String) -> Unit
) {
    MyZoomImage(title = "MyZoomImage", drawContent = { sketchImageUri ->
        MyZoomImageSample(sketchImageUri)
    }),

    TelephotoZoomableImage(title = "TelephotoZoomableImage", drawContent = { sketchImageUri ->
        TelephotoZoomableAsyncImageSample(sketchImageUri)
    }),
}