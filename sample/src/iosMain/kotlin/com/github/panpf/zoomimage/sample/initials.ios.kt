package com.github.panpf.zoomimage.sample

import coil3.ImageLoader
import com.github.panpf.sketch.PlatformContext
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.decode.supportSkiaAnimatedWebp
import com.github.panpf.sketch.decode.supportSkiaGif
import com.github.panpf.zoomimage.sample.image.CoilComposeResourceUriFetcher
import com.github.panpf.zoomimage.sample.image.CoilComposeResourceUriKeyer
import com.github.panpf.zoomimage.sample.image.CoilKotlinResourceUriKeyer
import com.github.panpf.zoomimage.sample.util.CoilKotlinResourceUriFetcher

actual fun Sketch.Builder.platformSketchInitial(context: PlatformContext) {
    addComponents {
        supportSkiaGif()
        supportSkiaAnimatedWebp()
        supportSkiaAnimatedWebp()
    }
}

actual fun ImageLoader.Builder.platformCoilInitial(context: coil3.PlatformContext) {
    components {
        add(CoilComposeResourceUriFetcher.Factory())
        add(CoilComposeResourceUriKeyer())
        add(CoilKotlinResourceUriFetcher.Factory())
        add(CoilKotlinResourceUriKeyer())
    }
}