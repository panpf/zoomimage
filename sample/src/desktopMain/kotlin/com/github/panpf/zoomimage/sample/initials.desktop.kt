package com.github.panpf.zoomimage.sample

import coil3.ImageLoader
import com.github.panpf.sketch.ComponentRegistry
import com.github.panpf.sketch.PlatformContext
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.decode.supportSkiaAnimatedWebp
import com.github.panpf.sketch.decode.supportSkiaGif
import com.github.panpf.zoomimage.sample.image.CoilBlurInterceptor
import com.github.panpf.zoomimage.sample.util.CoilComposeResourceUriFetcher
import com.github.panpf.zoomimage.sample.util.CoilKotlinResourceUriFetcher
import com.github.panpf.zoomimage.sample.util.PexelsCompatibleRequestInterceptor

actual fun Sketch.Builder.platformSketchInitial(context: PlatformContext) {

}

actual fun platformSketchComponents(context: PlatformContext): ComponentRegistry? {
    return ComponentRegistry.Builder().apply {
        supportSkiaGif()
        supportSkiaAnimatedWebp()
        supportSkiaAnimatedWebp()

        addRequestInterceptor(PexelsCompatibleRequestInterceptor())
    }.build()
}

actual fun ImageLoader.Builder.platformCoilInitial(context: coil3.PlatformContext) {
    components {
        add(
            CoilBlurInterceptor(
                Sketch.Builder(PlatformContext.INSTANCE).build(),
                radius = 20,
                maskColor = 0x63000000
            )
        )
        add(CoilComposeResourceUriFetcher.Factory())
        add(CoilKotlinResourceUriFetcher.Factory())
    }
}