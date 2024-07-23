package com.github.panpf.zoomimage.sample.image

import com.github.panpf.zoomimage.coil.CoilModelToImageSource
import com.github.panpf.zoomimage.subsampling.ImageSource

expect class CoilComposeResourceToImageSource() : CoilModelToImageSource {

    override fun dataToImageSource(model: Any): ImageSource.Factory?
}