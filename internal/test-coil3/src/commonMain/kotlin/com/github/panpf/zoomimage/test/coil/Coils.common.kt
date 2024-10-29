package com.github.panpf.zoomimage.test.coil

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader

object Coils {

    init {
        SingletonImageLoader.setSafe {
            newImageLoader(it)
        }
    }

    fun imageLoader(): ImageLoader {
        return SingletonImageLoader.get(platformContext)
    }
}

expect fun newImageLoader(context: PlatformContext): ImageLoader