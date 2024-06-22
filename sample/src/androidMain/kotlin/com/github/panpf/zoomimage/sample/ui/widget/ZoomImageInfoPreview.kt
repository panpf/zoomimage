package com.github.panpf.zoomimage.sample.ui.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.github.panpf.zoomimage.compose.rememberZoomState
import com.github.panpf.zoomimage.sample.ui.widget.ZoomImageInfo

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