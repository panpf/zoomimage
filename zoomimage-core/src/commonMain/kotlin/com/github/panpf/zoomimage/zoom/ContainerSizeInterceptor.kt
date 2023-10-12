package com.github.panpf.zoomimage.zoom

import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.Logger

interface ContainerSizeInterceptor {
    fun intercept(logger: Logger, oldContainerSize: IntSizeCompat, newContainerSize: IntSizeCompat): IntSizeCompat
}