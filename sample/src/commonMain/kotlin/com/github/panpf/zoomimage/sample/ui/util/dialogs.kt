package com.github.panpf.zoomimage.sample.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.panpf.zoomimage.sample.util.RuntimePlatform
import com.github.panpf.zoomimage.sample.util.runtimePlatformInstance


@Composable
fun getSettingsDialogHeight(): Dp {
    return if (runtimePlatformInstance == RuntimePlatform.Js) {
        600.dp
    } else {
        val density = LocalDensity.current
        val windowSize = windowSize()
        remember {
            with(density) {
                (windowSize.height * 0.8f).toInt().toDp()
            }
        }
    }
}