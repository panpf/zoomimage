package com.github.panpf.zoomimage.sample

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.github.panpf.sketch.PlatformContext
import com.github.panpf.zoomimage.sample.ui.App

fun main() {
    initialApp(PlatformContext.INSTANCE)
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