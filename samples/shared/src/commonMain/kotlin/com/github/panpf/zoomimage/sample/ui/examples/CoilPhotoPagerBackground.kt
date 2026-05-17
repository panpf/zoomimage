package com.github.panpf.zoomimage.sample.ui.examples

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import com.github.panpf.zoomimage.sample.image.PhotoPalette

@Composable
fun CoilPagerBackground(
    sketchImageUri: String,
    photoPaletteState: MutableState<PhotoPalette>,
) {
    // Because it is not easy to implement blur and calculate Palette in Coil, I used Sketch instead, but cannot use memory cache.
    SketchPagerBackground(
        sketchImageUri = sketchImageUri,
        photoPaletteState = photoPaletteState,
        memoryCachePolicy = com.github.panpf.sketch.cache.CachePolicy.DISABLED
    )
}