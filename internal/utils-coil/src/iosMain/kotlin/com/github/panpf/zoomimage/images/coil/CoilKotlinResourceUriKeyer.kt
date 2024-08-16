package com.github.panpf.zoomimage.images.coil

import coil3.Uri
import coil3.key.Keyer
import coil3.request.Options
import coil3.toUri

class CoilKotlinResourceUriKeyer : Keyer<Uri> {

    override fun key(data: Uri, options: Options): String? {
        if (isKotlinResourceUri(data.toString().toUri())) {
            return data.toString()
        } else {
            return null
        }
    }
}