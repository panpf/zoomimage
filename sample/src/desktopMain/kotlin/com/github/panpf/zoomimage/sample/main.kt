package com.github.panpf.zoomimage.sample

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.github.panpf.sketch.SingletonSketch
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.decode.supportSkiaGif
import com.github.panpf.sketch.fetch.supportComposeResources
import com.github.panpf.zoomimage.sample.ui.Page
import com.github.panpf.zoomimage.sample.ui.navigation.NavigationContainer
import com.github.panpf.zoomimage.sample.ui.theme.AppTheme
import com.github.panpf.zoomimage.sample.ui.util.EventBus
import kotlinx.coroutines.launch

fun main() {
    initialSketch()
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
            AppTheme {
                NavigationContainer(Page.Main)
            }
        }
    }
}

fun initialSketch() {
    SingletonSketch.setSafe {
        Sketch.Builder(it).apply {
            components {
                supportComposeResources()
                supportSkiaGif()
            }
        }.build()
    }
}