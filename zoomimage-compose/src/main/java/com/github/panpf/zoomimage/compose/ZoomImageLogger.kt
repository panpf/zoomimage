package com.github.panpf.zoomimage.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.github.panpf.zoomimage.Logger


@Composable
fun rememberZoomImageLogger(tag: String = "ZoomImage", level: Int = Logger.INFO): Logger =
    remember { Logger(tag = tag) }.apply { this.level = level }