package com.github.panpf.zoomimage.sample.ui.preview

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.github.panpf.zoomimage.compose.rememberZoomState
import com.github.panpf.zoomimage.sample.image.PhotoPalette
import com.github.panpf.zoomimage.sample.ui.gallery.ZoomImageTool
import com.github.panpf.zoomimage.sample.ui.widget.rememberMyDialogState

@Preview
@Composable
fun ZoomImageToolPreview() {
    val zoomState = rememberZoomState()
    val colorScheme = MaterialTheme.colorScheme
    ZoomImageTool(
        imageUri = "ic_rotate_right.xml",
        zoomableState = zoomState.zoomable,
        subsamplingState = zoomState.subsampling,
        infoDialogState = rememberMyDialogState(),
        photoPaletteState = remember { mutableStateOf(PhotoPalette(colorScheme)) }
    )
}