package com.github.panpf.zoomimage.sample.ui.preview

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.github.panpf.zoomimage.compose.rememberZoomState
import com.github.panpf.zoomimage.compose.zoom.ScrollBarSpec
import com.github.panpf.zoomimage.images.ComposeResImageFiles
import com.github.panpf.zoomimage.sample.image.PhotoPalette
import com.github.panpf.zoomimage.sample.ui.components.rememberMyDialogState
import com.github.panpf.zoomimage.sample.ui.examples.ZoomImageTool
import com.github.panpf.zoomimage.sample.ui.model.Photo
import com.github.panpf.zoomimage.sample.ui.util.rememberCapturableState
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Preview
@Composable
fun ZoomImageToolPreview() {
    val zoomState = rememberZoomState()
    val colorScheme = MaterialTheme.colorScheme
    val photo = remember { Photo(ComposeResImageFiles.hugeCard.uri) }
    ZoomImageTool(
        photo = photo,
        zoomableState = zoomState.zoomable,
        subsamplingState = zoomState.subsampling,
        infoDialogState = rememberMyDialogState(),
        capturableState = rememberCapturableState(),
        photoPaletteState = remember { mutableStateOf(PhotoPalette(colorScheme)) },
        scrollBar = ScrollBarSpec.Medium.copy(windowInsets = WindowInsets.systemBars)
    )
}