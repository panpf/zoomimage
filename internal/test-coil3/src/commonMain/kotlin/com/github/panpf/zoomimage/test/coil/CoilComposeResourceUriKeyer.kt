package com.github.panpf.zoomimage.test.coil

import coil3.Uri
import coil3.key.Keyer
import coil3.pathSegments
import coil3.request.Options
import coil3.toUri

class CoilComposeResourceUriKeyer : Keyer<Uri> {

    override fun key(data: Uri, options: Options): String? {
        if (isComposeResourceUri(data.toString().toUri())) {
            return data.toString()
        } else {
            return null
        }
    }
}

/**
 * Check if the uri is a compose resource uri
 *
 * Support 'file:///compose_resource/composeResources/com.github.panpf.sketch.sample.resources/files/huge_china.jpg'
 *
 * @see com.github.panpf.sketch.compose.resources.common.test.fetch.ComposeResourceUriFetcherTest.testIsComposeResourceUri
 */
fun isComposeResourceUri(uri: Uri): Boolean =
    "file".equals(uri.scheme, ignoreCase = true)
            && uri.authority?.takeIf { it.isNotEmpty() } == null
            && "compose_resource".equals(
        uri.pathSegments.firstOrNull(),
        ignoreCase = true
    )