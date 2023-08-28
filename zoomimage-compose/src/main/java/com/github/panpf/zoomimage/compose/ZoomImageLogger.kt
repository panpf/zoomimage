package com.github.panpf.zoomimage.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.github.panpf.zoomimage.Logger


/**
 * Creates and remember a [Logger]
 *
 * @param tag The tag of the log
 * @param module The module of the log
 * @param showThreadName Whether to show the thread name in the log
 * @param level The level of the log
 */
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