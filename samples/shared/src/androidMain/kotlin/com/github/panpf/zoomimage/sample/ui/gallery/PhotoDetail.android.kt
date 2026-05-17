package com.github.panpf.zoomimage.sample.ui.gallery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.github.panpf.zoomimage.sample.AppSettings
import com.github.panpf.zoomimage.sample.image.PhotoPalette
import com.github.panpf.zoomimage.sample.ui.examples.BasicZoomImageSample
import com.github.panpf.zoomimage.sample.ui.examples.CoilZoomAsyncImageSample
import com.github.panpf.zoomimage.sample.ui.examples.GlideZoomAsyncImageSample
import com.github.panpf.zoomimage.sample.ui.examples.SketchZoomAsyncImageSample
import com.github.panpf.zoomimage.sample.ui.model.Photo
import org.koin.compose.koinInject

@Composable
actual fun PhotoDetail(
    photo: Photo,
    photoPaletteState: MutableState<PhotoPalette>,
    pageSelected: Boolean,
) {
    val appSettings: AppSettings = koinInject()
    val composeImageLoader by appSettings.composeImageLoader.collectAsState()
    when (composeImageLoader) {
        "Sketch" -> SketchZoomAsyncImageSample(photo, photoPaletteState, pageSelected)
        "Coil" -> CoilZoomAsyncImageSample(photo, photoPaletteState, pageSelected)
        "Glide" -> GlideZoomAsyncImageSample(photo, photoPaletteState, pageSelected)
        "Basic" -> BasicZoomImageSample(photo, photoPaletteState, pageSelected)
        else -> throw IllegalArgumentException("Unsupported composeImageLoader: $composeImageLoader")
    }
}