package com.github.panpf.zoomimage.sample

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import coil3.SingletonImageLoader
import com.github.panpf.sketch.SingletonSketch
import com.github.panpf.zoomimage.sample.ui.App
import kotlinx.coroutines.launch

fun main() {
    initials()

    application {
        val coroutineScope = rememberCoroutineScope()
        Window(
            title = "ZoomImage",
            onCloseRequest = ::exitApplication,
            state = rememberWindowState(size = DpSize(1000.dp, 800.dp)),
            onKeyEvent = {
                coroutineScope.launch {
                    EventBus.keyEvent.emit(it)
                }
                false
            }
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