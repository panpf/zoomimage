package com.github.panpf.zoomimage.sample

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import coil3.SingletonImageLoader
import com.github.panpf.sketch.SingletonSketch
import com.github.panpf.zoomimage.sample.ui.App

fun main() {
    initials()
    application {
        Window(
            title = "ZoomImage",
            onCloseRequest = ::exitApplication,
            state = rememberWindowState(size = DpSize(1200.dp, 800.dp)),
        ) {
            App()
        }
    }
}

private fun initials() {
    SingletonSketch.setSafe { newSketch(it) }
    SingletonImageLoader.setSafe { newCoil(it) }
    cleanImageLoaderMemoryCache()
}