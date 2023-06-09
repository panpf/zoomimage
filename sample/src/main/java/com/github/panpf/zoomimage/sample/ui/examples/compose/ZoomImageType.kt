package com.github.panpf.zoomimage.sample.ui.examples.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.github.panpf.zoomimage.sample.ui.photoalbum.compose.CoilListImage
import com.github.panpf.zoomimage.sample.ui.photoalbum.compose.SketchListImage

enum class ZoomImageType(
    val title: String,
    val drawListContent: @Composable (sketchImageUri: String, modifier: Modifier) -> Unit,
    val drawContent: @Composable (sketchImageUri: String) -> Unit,
) {
    MyZoomImage(
        title = "ZoomImage",
        drawListContent = { sketchImageUri, modifier ->
            SketchListImage(sketchImageUri, modifier)
        },
        drawContent = { sketchImageUri ->
            ZoomImageSample(sketchImageUri)
        },
    ),

    SketchZoomAsyncImage(
        title = "SketchZoomAsyncImage",
        drawListContent = { sketchImageUri, modifier ->
            SketchListImage(sketchImageUri, modifier)
        },
        drawContent = { sketchImageUri ->
            SketchZoomAsyncImageSample(sketchImageUri)
        },
    ),

    TelephotoZoomableAsyncImage(
        title = "ZoomableAsyncImage",
        drawListContent = { sketchImageUri, modifier ->
            CoilListImage(sketchImageUri, modifier)
        },
        drawContent = { sketchImageUri ->
            TelephotoZoomableAsyncImageSample(sketchImageUri)
        },
    ),
}