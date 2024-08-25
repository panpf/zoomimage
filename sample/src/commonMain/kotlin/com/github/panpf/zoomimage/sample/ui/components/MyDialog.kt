package com.github.panpf.zoomimage.sample.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.github.panpf.zoomimage.sample.ui.util.windowSize
import com.github.panpf.zoomimage.sample.util.RuntimePlatform
import com.github.panpf.zoomimage.sample.util.runtimePlatformInstance


@Composable
fun rememberMyDialogState(showing: Boolean = false): MyDialogState =
    remember { MyDialogState(showing) }

class MyDialogState(showing: Boolean = false) {
    var showing by mutableStateOf(showing)

    var contentReady = true

    fun show() {
        showing = true
    }
}

@Composable
fun MyDialog(
    state: MyDialogState,
    content: @Composable () -> Unit
) {
    if (state.showing) {
        if (state.contentReady) {
            Dialog(onDismissRequest = { state.showing = false }) {
                Surface(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(max = dialogMaxHeight())
                        .clip(RoundedCornerShape(20.dp))
                ) {
                    content()
                }
            }
        } else {
            state.showing = false
        }
    }
}


@Composable
fun dialogMaxHeight(): Dp {
    return if (runtimePlatformInstance == RuntimePlatform.Js) {
        600.dp
    } else {
        val density = LocalDensity.current
        val windowSize = windowSize()
        remember(windowSize, density) {
            with(density) {
                (windowSize.height * 0.8f).toInt().toDp()
            }
        }
    }
}