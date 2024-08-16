package com.github.panpf.zoomimage.test.coil

import coil3.ImageLoader
import coil3.PlatformContext

actual fun newImageLoader(context: PlatformContext): ImageLoader {
    return ImageLoader.Builder(context).build()
}