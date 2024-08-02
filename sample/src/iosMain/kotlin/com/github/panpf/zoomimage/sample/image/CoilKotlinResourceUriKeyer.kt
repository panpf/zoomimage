package com.github.panpf.zoomimage.sample.image

import coil3.Uri
import coil3.key.Keyer
import coil3.request.Options
import com.github.panpf.sketch.fetch.isKotlinResourceUri
import com.github.panpf.sketch.util.toUri

class CoilKotlinResourceUriKeyer : Keyer<Uri> {

    override fun key(data: Uri, options: Options): String? {
        if (isKotlinResourceUri(data.toString().toUri())) {
            return data.toString()
        } else {
            return null
        }
    }
}