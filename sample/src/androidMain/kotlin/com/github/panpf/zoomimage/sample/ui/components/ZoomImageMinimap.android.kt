package com.github.panpf.zoomimage.sample.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.github.panpf.zoomimage.sample.AppSettings
import com.github.panpf.zoomimage.sample.ui.examples.BasicZoomImageMinimapContent
import com.github.panpf.zoomimage.sample.ui.examples.CoilZoomImageMinimapContent
import com.github.panpf.zoomimage.sample.ui.examples.GlideZoomImageMinimapContent
import com.github.panpf.zoomimage.sample.ui.examples.SketchZoomImageMinimapContent
import org.koin.compose.koinInject

@Composable
actual fun ZoomImageMinimapContent(sketchImageUri: String) {
    val appSettings: AppSettings = koinInject()
    val composeImageLoader by appSettings.composeImageLoader.collectAsState()
    when (composeImageLoader) {
        "Sketch" -> SketchZoomImageMinimapContent(sketchImageUri)
        "Coil" -> CoilZoomImageMinimapContent(sketchImageUri)
        "Glide" -> GlideZoomImageMinimapContent(sketchImageUri)
        "Basic" -> BasicZoomImageMinimapContent(sketchImageUri)
        else -> throw IllegalArgumentException("Unsupported composeImageLoader: $composeImageLoader")
    }
}