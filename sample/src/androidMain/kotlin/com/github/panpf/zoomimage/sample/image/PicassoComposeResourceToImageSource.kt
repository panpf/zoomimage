package com.github.panpf.zoomimage.sample.image

import android.net.Uri
import com.github.panpf.sketch.fetch.ComposeResourceUriFetcher
import com.github.panpf.zoomimage.picasso.PicassoDataToImageSource
import com.github.panpf.zoomimage.subsampling.ComposeResourceImageSource
import com.github.panpf.zoomimage.subsampling.ImageSource

class PicassoComposeResourceToImageSource : PicassoDataToImageSource {

    override fun dataToImageSource(data: Any): ImageSource.Factory? {
        if (data is Uri
            && data.scheme.equals(ComposeResourceUriFetcher.SCHEME, ignoreCase = true)
        ) {
            val resourcePath = "${data.authority.orEmpty()}${data.path.orEmpty()}"
            return ComposeResourceImageSource.Factory(resourcePath)
        }
        return null
    }
}