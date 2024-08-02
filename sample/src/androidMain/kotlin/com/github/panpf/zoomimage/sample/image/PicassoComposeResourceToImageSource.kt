package com.github.panpf.zoomimage.sample.image

import android.net.Uri
import com.github.panpf.sketch.fetch.isComposeResourceUri
import com.github.panpf.sketch.util.toUri
import com.github.panpf.zoomimage.picasso.PicassoDataToImageSource
import com.github.panpf.zoomimage.subsampling.ComposeResourceImageSource
import com.github.panpf.zoomimage.subsampling.ImageSource

class PicassoComposeResourceToImageSource : PicassoDataToImageSource {

    override fun dataToImageSource(data: Any): ImageSource.Factory? {
        if (data is Uri && isComposeResourceUri(data.toString().toUri())) {
            val resourcePath = data.pathSegments.drop(1).joinToString("/")
            return ComposeResourceImageSource.Factory(resourcePath)
        }
        return null
    }
}