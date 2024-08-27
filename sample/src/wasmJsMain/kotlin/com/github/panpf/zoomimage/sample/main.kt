package com.github.panpf.zoomimage.sample

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import coil3.SingletonImageLoader
import com.github.panpf.sketch.SingletonSketch
import com.github.panpf.zoomimage.sample.ui.App

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initials()
    CanvasBasedWindow("ZoomImageSample-WASM") {
        App()
    }
}

private fun initials() {
    SingletonSketch.setSafe { newSketch(it) }
    SingletonImageLoader.setSafe { newCoil(it) }
    cleanImageLoaderMemoryCache()
}