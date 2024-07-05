package com.github.panpf.zoomimage.sample.ui.preview

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import com.github.panpf.sketch.fetch.newComposeResourceUri
import com.github.panpf.zoomimage.sample.image.PhotoPalette
import com.github.panpf.zoomimage.sample.ui.examples.SketchZoomAsyncImageSample


@Preview
@Composable
private fun SketchZoomAsyncImageSamplePreview() {
    val colorScheme = MaterialTheme.colorScheme
    SketchZoomAsyncImageSample(
        sketchImageUri = newComposeResourceUri(resourcePath = "files/huge_china.jpg"),
        photoPaletteState = mutableStateOf(PhotoPalette(colorScheme))
    )
}