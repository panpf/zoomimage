package com.github.panpf.zoomimage.coil

import com.github.panpf.zoomimage.subsampling.ImageSource as ZoomImageImageSource
import coil3.ImageLoader
import coil3.PlatformContext

interface CoilModelToImageSource {
    fun dataToImageSource(model: Any): ZoomImageImageSource.Factory?
}

expect class CoilModelToImageSourceImpl(context: PlatformContext, imageLoader: ImageLoader) :
    CoilModelToImageSource {

    override fun dataToImageSource(model: Any): ZoomImageImageSource.Factory?
}