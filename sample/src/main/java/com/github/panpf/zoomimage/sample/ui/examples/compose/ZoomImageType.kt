package com.github.panpf.zoomimage.sample.ui.examples.compose

import androidx.compose.runtime.Composable

enum class ZoomImageType(
    val title: String,
    val drawContent: @Composable (sketchImageUri: String) -> Unit
) {
    MyZoomImage(
        title = "ZoomImage (My)",
        drawContent = { sketchImageUri ->
            ZoomImageSample(sketchImageUri)
        }
    ),

    SketchZoomAsyncImage(
        title = "SketchZoomAsyncImageSample (My)",
        drawContent = { sketchImageUri ->
            SketchZoomAsyncImageSample(sketchImageUri)
        }
    ),

    TelephotoZoomableAsyncImage(
        title = "ZoomableAsyncImage (Telephoto)",
        drawContent = { sketchImageUri ->
            TelephotoZoomableAsyncImageSample(sketchImageUri)
        }
    ),
}