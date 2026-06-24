package com.github.panpf.zoomimage.test.coil

import coil3.ImageLoader
import coil3.PlatformContext
import com.github.panpf.zoomimage.util.coil.CoilComposeResourceUriFetcher

actual fun newImageLoader(context: PlatformContext): ImageLoader {
    return ImageLoader.Builder(context).apply {
        components {
            add(CoilComposeResourceUriFetcher.Factory())
        }
    }.build()
}