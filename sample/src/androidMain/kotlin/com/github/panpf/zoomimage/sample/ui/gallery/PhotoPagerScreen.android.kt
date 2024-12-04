package com.github.panpf.zoomimage.sample.ui.gallery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.github.panpf.sketch.LocalPlatformContext
import com.github.panpf.zoomimage.sample.appSettings
import com.github.panpf.zoomimage.sample.image.PhotoPalette
import com.github.panpf.zoomimage.sample.ui.examples.BasicPagerBackground
import com.github.panpf.zoomimage.sample.ui.examples.CoilPagerBackground
import com.github.panpf.zoomimage.sample.ui.examples.GlidePagerBackground
import com.github.panpf.zoomimage.sample.ui.examples.SketchPagerBackground

@Composable
actual fun PhotoPagerBackground(
    sketchImageUri: String,
    photoPaletteState: MutableState<PhotoPalette>
) {
    val appSettings = LocalPlatformContext.current.appSettings
    val composeImageLoader by appSettings.composeImageLoader.collectAsState()
    when (composeImageLoader) {
        "Sketch" -> SketchPagerBackground(sketchImageUri, photoPaletteState)
        "Coil" -> CoilPagerBackground(sketchImageUri, photoPaletteState)
        "Glide" -> GlidePagerBackground(sketchImageUri, photoPaletteState)
        "Basic" -> BasicPagerBackground(sketchImageUri, photoPaletteState)
        else -> throw IllegalArgumentException("Unsupported composeImageLoader: $composeImageLoader")
    }
}