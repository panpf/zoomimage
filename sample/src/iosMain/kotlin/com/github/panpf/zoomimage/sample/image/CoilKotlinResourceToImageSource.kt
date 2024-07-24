package com.github.panpf.zoomimage.sample.image

import com.github.panpf.sketch.fetch.KotlinResourceUriFetcher
import com.github.panpf.sketch.util.toUri
import com.github.panpf.zoomimage.coil.CoilModelToImageSource
import com.github.panpf.zoomimage.sample.util.authority
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.fromKotlinResource
import com.github.panpf.zoomimage.subsampling.toFactory
import platform.Foundation.NSURL

class CoilKotlinResourceToImageSource : CoilModelToImageSource {

    override fun dataToImageSource(model: Any): ImageSource.Factory? {
        if (model is String && model.startsWith("${KotlinResourceUriFetcher.SCHEME}://")) {
            val uri = model.toUri()
            val resourcePath = "${uri.authority.orEmpty()}${uri.path.orEmpty()}"
            return ImageSource.fromKotlinResource(resourcePath).toFactory()
        } else if (model is coil3.Uri
            && model.scheme.equals(KotlinResourceUriFetcher.SCHEME, ignoreCase = true)
        ) {
            val resourcePath = "${model.authority.orEmpty()}${model.path.orEmpty()}"
            return ImageSource.fromKotlinResource(resourcePath).toFactory()
        } else if (model is NSURL
            && model.scheme.equals(KotlinResourceUriFetcher.SCHEME, ignoreCase = true)
        ) {
            val resourcePath = "${model.authority.orEmpty()}${model.path.orEmpty()}"
            return ImageSource.fromKotlinResource(resourcePath).toFactory()
        }
        return null
    }
}