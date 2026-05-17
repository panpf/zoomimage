package com.github.panpf.zoomimage.sample

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.github.panpf.sketch.PlatformContext
import com.github.panpf.zoomimage.sample.ui.App

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initialApp(PlatformContext.INSTANCE)
    ComposeViewport {
        App()
    }
}