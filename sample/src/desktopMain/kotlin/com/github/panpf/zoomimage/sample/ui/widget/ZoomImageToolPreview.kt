package com.github.panpf.zoomimage.sample.ui.widget

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.github.panpf.zoomimage.compose.rememberZoomState
import com.github.panpf.zoomimage.sample.ui.widget.ZoomImageTool
import com.github.panpf.zoomimage.sample.ui.widget.rememberMyDialogState

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