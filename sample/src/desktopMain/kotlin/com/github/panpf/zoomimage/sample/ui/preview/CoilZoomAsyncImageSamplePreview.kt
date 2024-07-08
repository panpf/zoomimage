package com.github.panpf.zoomimage.sample.ui.preview

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.github.panpf.sketch.fetch.newComposeResourceUri
import com.github.panpf.zoomimage.sample.image.PhotoPalette
import com.github.panpf.zoomimage.sample.ui.examples.CoilZoomAsyncImageSample


@Preview
@Composable
private fun CoilZoomAsyncImageSamplePreview() {
    val colorScheme = MaterialTheme.colorScheme
    CoilZoomAsyncImageSample(
        sketchImageUri = newComposeResourceUri(resourcePath = "files/huge_china.jpg"),
        photoPaletteState = remember { mutableStateOf(PhotoPalette(colorScheme)) }
    )
}