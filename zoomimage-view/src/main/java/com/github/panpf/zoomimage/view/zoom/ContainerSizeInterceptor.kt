package com.github.panpf.zoomimage.view.zoom

import com.github.panpf.zoomimage.Logger
import com.github.panpf.zoomimage.util.IntSizeCompat

interface ContainerSizeInterceptor {
    fun intercept(logger: Logger, oldContainerSize: IntSizeCompat, newContainerSize: IntSizeCompat): IntSizeCompat
}