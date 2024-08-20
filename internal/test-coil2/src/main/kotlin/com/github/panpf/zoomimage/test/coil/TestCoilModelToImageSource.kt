package com.github.panpf.zoomimage.test.coil

import android.content.Context
import coil.ImageLoader
import com.github.panpf.zoomimage.coil.CoilModelToImageSource
import com.github.panpf.zoomimage.subsampling.ImageSource

class TestCoilModelToImageSource : CoilModelToImageSource {

    override suspend fun modelToImageSource(
        context: Context,
        imageLoader: ImageLoader,
        model: Any
    ): ImageSource.Factory? {
        return null
    }
}