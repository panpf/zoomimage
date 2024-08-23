package com.github.panpf.zoomimage.sample.ui.examples

import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.request.cacheDecodeTimeoutFrame

actual fun ImageRequest.Builder.SketchZoomAsyncImageSampleImageConfig() {
    cacheDecodeTimeoutFrame()
}