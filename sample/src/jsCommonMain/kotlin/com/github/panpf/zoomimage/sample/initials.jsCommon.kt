package com.github.panpf.zoomimage.sample

import coil3.ImageLoader
import com.github.panpf.sketch.ComponentRegistry
import com.github.panpf.sketch.PlatformContext
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.decode.supportSkiaAnimatedWebp
import com.github.panpf.sketch.decode.supportSkiaGif
import com.github.panpf.zoomimage.sample.util.CoilComposeResourceUriFetcher

actual fun Sketch.Builder.platformSketchInitial(context: PlatformContext) {

}

actual fun platformSketchComponents(context: PlatformContext): ComponentRegistry? {
    return ComponentRegistry.Builder().apply {
        supportSkiaGif()
        supportSkiaAnimatedWebp()
        supportSkiaAnimatedWebp()
    }.build()
}

actual fun ImageLoader.Builder.platformCoilInitial(context: coil3.PlatformContext) {
    components {
        add(CoilComposeResourceUriFetcher.Factory())
    }
}