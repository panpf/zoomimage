package com.github.panpf.zoomimage.sample.compose.widget

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.github.panpf.zoomimage.compose.rememberZoomState

@Preview
@Composable
fun ZoomImageInfoPreview() {
    val zoomState = rememberZoomState()
    ZoomImageInfo(
        imageUri = "https://www.sample.com/sample.jpg",
        zoomable = zoomState.zoomable,
        subsampling = zoomState.subsampling
    )
}