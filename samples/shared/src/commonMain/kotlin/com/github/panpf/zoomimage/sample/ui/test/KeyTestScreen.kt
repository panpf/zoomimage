package com.github.panpf.zoomimage.sample.ui.test

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import com.github.panpf.zoomimage.SketchZoomAsyncImage
import com.github.panpf.zoomimage.compose.zoom.ScrollBarSpec
import com.github.panpf.zoomimage.compose.zoom.keyZoom
import com.github.panpf.zoomimage.images.ComposeResImageFiles
import com.github.panpf.zoomimage.rememberSketchZoomState
import com.github.panpf.zoomimage.sample.ui.base.BaseScreen
import com.github.panpf.zoomimage.sample.ui.base.ToolbarScaffold
import com.github.panpf.zoomimage.util.Logger

@Composable
fun KeyTestScreen() {
    BaseScreen {
        ToolbarScaffold("KeyZoom") {
            val focusRequester = remember { FocusRequester() }
            val zoomState = rememberSketchZoomState()
            zoomState.logger.level = Logger.Level.Debug
            SketchZoomAsyncImage(
                uri = ComposeResImageFiles.hugeChina.uri,
                contentDescription = "",
                zoomState = zoomState,
                scrollBar = ScrollBarSpec.Medium.copy(windowInsets = WindowInsets.navigationBars),
                modifier = Modifier.fillMaxSize()
                    .focusRequester(focusRequester)
                    .focusable()
                    .keyZoom(zoomState.zoomable),
            )

            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }
//            val size = with(LocalDensity.current) { 200.toDp() }
//            SketchZoomAsyncImage(
//                uri = ComposeResImageFiles.anim.uri,
//                contentDescription = "Anim",
//                modifier = Modifier.size(size),
//                zoomState = rememberSketchZoomState(logLevel = Logger.Level.Debug)
//            )
        }
    }
}