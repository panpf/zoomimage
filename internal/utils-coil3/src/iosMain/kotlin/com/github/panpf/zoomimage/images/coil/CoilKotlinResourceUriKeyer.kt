package com.github.panpf.zoomimage.images.coil

import coil3.Uri
import coil3.key.Keyer
import coil3.request.Options
import coil3.toUri

class CoilKotlinResourceUriKeyer : Keyer<Uri> {

    override fun key(data: Uri, options: Options): String? {
        val uriString = data.toString()
        return if (isKotlinResourceUri(uriString.toUri())) uriString else null
    }
}