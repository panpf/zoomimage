package com.github.panpf.zoomimage.sample.image

import coil3.ImageLoader
import coil3.PlatformContext
import com.github.panpf.sketch.fetch.isKotlinResourceUri
import com.github.panpf.sketch.util.toUri
import com.github.panpf.zoomimage.coil.CoilModelToImageSource
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.fromKotlinResource
import com.github.panpf.zoomimage.subsampling.toFactory

class CoilKotlinResourceToImageSource : CoilModelToImageSource {

    override fun dataToImageSource(
        context: PlatformContext,
        imageLoader: ImageLoader,
        model: Any
    ): ImageSource.Factory? {
        val uri = when (model) {
            is String -> model.toUri()
            is coil3.Uri -> model.toString().toUri()
            else -> null
        }
        if (uri != null && isKotlinResourceUri(uri)) {
            val resourcePath = uri.pathSegments.drop(1).joinToString("/")
            return ImageSource.fromKotlinResource(resourcePath).toFactory()
        }
        return null
    }
}