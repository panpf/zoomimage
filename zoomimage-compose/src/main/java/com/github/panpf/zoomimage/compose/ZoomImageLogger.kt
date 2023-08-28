package com.github.panpf.zoomimage.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.github.panpf.zoomimage.Logger


@Composable
fun rememberZoomImageLogger(
    tag: String = "ZoomImage",
    module: String? = null,
    showThreadName: Boolean = false,
    level: Int = Logger.INFO,
): Logger {
    val logger = remember(tag, module, showThreadName) {
        Logger(tag = tag, module = module, showThreadName = showThreadName)
    }
    logger.level = level
    return logger
}