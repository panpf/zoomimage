package com.github.panpf.zoomimage.sample.ui.examples

import com.github.panpf.sketch.request.ImageOptions
import com.github.panpf.sketch.request.cacheDecodeTimeoutFrame

actual fun getPlatformSketchZoomAsyncImageSampleImageOptions(): ImageOptions = ImageOptions {
    cacheDecodeTimeoutFrame()
}