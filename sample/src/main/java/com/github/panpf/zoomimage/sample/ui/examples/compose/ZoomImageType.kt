package com.github.panpf.zoomimage.sample.ui.examples.compose

import androidx.compose.runtime.Composable

enum class ZoomImageType(
    val title: String,
    val drawContent: @Composable (sketchImageUri: String) -> Unit
) {
    MyZoomImage(
        title = "ZoomImage",
        drawContent = { sketchImageUri ->
            ZoomImageSample(sketchImageUri)
        }
    ),

    SketchZoomAsyncImage(
        title = "SketchZoomAsyncImage",
        drawContent = { sketchImageUri ->
            SketchZoomAsyncImageSample(sketchImageUri)
        }
    ),

    TelephotoZoomableAsyncImage(
        title = "ZoomableAsyncImage",
        drawContent = { sketchImageUri ->
            TelephotoZoomableAsyncImageSample(sketchImageUri)
        }
    ),
}