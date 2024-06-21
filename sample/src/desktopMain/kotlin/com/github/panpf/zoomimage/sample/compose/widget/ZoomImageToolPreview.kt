package com.github.panpf.zoomimage.sample.compose.widget

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.github.panpf.zoomimage.compose.rememberZoomState

@Preview
@Composable
fun ZoomImageToolPreview() {
    val zoomState = rememberZoomState()
    ZoomImageTool(
        zoomableState = zoomState.zoomable,
        subsamplingState = zoomState.subsampling,
        infoDialogState = rememberMyDialogState(),
        imageUri = "ic_rotate_right.xml"
    )
}