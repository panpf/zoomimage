package com.github.panpf.zoomimage.sample.ui.preview

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
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