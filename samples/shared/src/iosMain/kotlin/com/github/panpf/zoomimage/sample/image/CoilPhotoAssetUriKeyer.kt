package com.github.panpf.zoomimage.sample.image

import coil3.Uri
import coil3.decode.Decoder
import coil3.key.Keyer
import coil3.request.Options
import com.github.panpf.sketch.fetch.isPhotosAssetUri
import com.github.panpf.sketch.util.toUri

class CoilPhotoAssetUriKeyer : Keyer<Uri> {

    override fun key(data: Uri, options: Options): String? {
        val decoder: Decoder? = null
        val uriString = data.toString()
        return if (isPhotosAssetUri(uriString.toUri())) uriString else null
    }
}