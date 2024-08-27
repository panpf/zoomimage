package com.github.panpf.zoomimage.sample

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import coil3.SingletonImageLoader
import com.github.panpf.sketch.SingletonSketch
import com.github.panpf.zoomimage.sample.ui.App
import org.jetbrains.skiko.wasm.onWasmReady

@OptIn(ExperimentalComposeUiApi::class)
fun main() = onWasmReady {
    initials()
    CanvasBasedWindow("ZoomImageSample") {
        App()
    }
}

private fun initials() {
    SingletonSketch.setSafe { newSketch(it) }
    SingletonImageLoader.setSafe { newCoil(it) }
    cleanImageLoaderMemoryCache()
}