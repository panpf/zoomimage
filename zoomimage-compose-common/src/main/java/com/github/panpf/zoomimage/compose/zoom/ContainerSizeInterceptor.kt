package com.github.panpf.zoomimage.compose.zoom

import androidx.compose.ui.unit.IntSize
import com.github.panpf.zoomimage.Logger

interface ContainerSizeInterceptor {
    fun intercept(logger: Logger, oldContainerSize: IntSize, newContainerSize: IntSize): IntSize
}