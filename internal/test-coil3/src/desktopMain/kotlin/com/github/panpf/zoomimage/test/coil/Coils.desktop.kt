package com.github.panpf.zoomimage.test.coil

import coil3.ImageLoader
import coil3.PlatformContext
import com.github.panpf.zoomimage.images.coil.CoilKotlinResourceUriFetcher
import com.github.panpf.zoomimage.images.coil.CoilKotlinResourceUriKeyer

actual fun newImageLoader(context: PlatformContext): ImageLoader {
    return ImageLoader.Builder(context).apply {
        components {
            add(CoilKotlinResourceUriFetcher.Factory())
            add(CoilKotlinResourceUriKeyer())
        }
    }.build()
}