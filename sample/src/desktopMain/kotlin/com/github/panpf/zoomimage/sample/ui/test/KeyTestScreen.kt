package com.github.panpf.zoomimage.sample.ui.test

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.zoomimage.SketchZoomAsyncImage
import com.github.panpf.zoomimage.compose.rememberZoomImageLogger
import com.github.panpf.zoomimage.compose.zoom.keyboardZoom
import com.github.panpf.zoomimage.rememberSketchZoomState
import com.github.panpf.zoomimage.sample.ui.base.BaseScreen
import com.github.panpf.zoomimage.sample.ui.base.ToolbarScaffold
import com.github.panpf.zoomimage.util.Logger

class KeyTestScreen : BaseScreen() {

    @Composable
    override fun DrawContent() {
        ToolbarScaffold("Key", ignoreNavigationBarInsets = true) {
            val focusRequester = remember { FocusRequester() }
            val zoomState = rememberSketchZoomState(rememberZoomImageLogger(tag = "SketchZoomAsyncImage", level = Logger.Level.Debug))
            SketchZoomAsyncImage(
                uri = ResourceImages.hugeChina.uri,
                contentDescription = "",
                zoomState = zoomState,
                modifier = Modifier.fillMaxSize()
                    .focusRequester(focusRequester)
                    .focusable()
                    .keyboardZoom(zoomState.zoomable),
            )

            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }
        }
    }
}