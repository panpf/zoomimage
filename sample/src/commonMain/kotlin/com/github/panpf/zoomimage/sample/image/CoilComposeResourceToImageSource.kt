package com.github.panpf.zoomimage.sample.image

import coil3.ImageLoader
import coil3.PlatformContext
import com.github.panpf.zoomimage.coil.CoilModelToImageSource
import com.github.panpf.zoomimage.subsampling.ImageSource

expect class CoilComposeResourceToImageSource() : CoilModelToImageSource {

    override fun dataToImageSource(
        context: PlatformContext,
        imageLoader: ImageLoader,
        model: Any
    ): ImageSource.Factory?
}