package com.github.panpf.zoomimage.sample.image

import com.github.panpf.sketch.fetch.isComposeResourceUri
import com.github.panpf.sketch.util.toUri
import com.github.panpf.zoomimage.coil.CoilModelToImageSource
import com.github.panpf.zoomimage.subsampling.ComposeResourceImageSource
import com.github.panpf.zoomimage.subsampling.ImageSource

actual class CoilComposeResourceToImageSource : CoilModelToImageSource {

    actual override fun dataToImageSource(model: Any): ImageSource.Factory? {
        val uri = when (model) {
            is String -> model.toUri()
            is coil3.Uri -> model.toString().toUri()
            is platform.Foundation.NSURL -> model.toString().toUri()
            else -> null
        }
        if (uri != null && isComposeResourceUri(uri)) {
            val resourcePath = uri.pathSegments.drop(1).joinToString("/")
            return ComposeResourceImageSource.Factory(resourcePath)
        }
        return null
    }
}