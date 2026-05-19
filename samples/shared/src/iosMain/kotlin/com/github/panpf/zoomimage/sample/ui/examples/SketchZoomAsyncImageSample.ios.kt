package com.github.panpf.zoomimage.sample.ui.examples

import com.github.panpf.sketch.request.ImageOptions
import com.github.panpf.zoomimage.sample.ui.components.MyPhotosAssetFetcher

actual fun getPlatformSketchZoomAsyncImageSampleImageOptions(): ImageOptions = ImageOptions {
    components {
        // TODO After the new version of Sketch is fixed, MyPhotosAssetFetcher is no longer needed.
        add(MyPhotosAssetFetcher.Factory())
    }
}