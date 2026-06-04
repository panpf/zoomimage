package com.github.panpf.zoomimage.sample.image

import coil3.Uri
import coil3.key.Keyer
import coil3.request.Options
import com.github.panpf.sketch.fetch.isComposeResourceUri
import com.github.panpf.sketch.util.toUri

class CoilComposeResourceUriKeyer : Keyer<Uri> {

    override fun key(data: Uri, options: Options): String? {
        val uriString = data.toString()
        return if (isComposeResourceUri(uriString.toUri())) uriString else null
    }
}