package com.github.panpf.zoomimage.sample

import android.os.Build
import coil3.ImageLoader
import com.github.panpf.sketch.ComponentRegistry
import com.github.panpf.sketch.PlatformContext
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.decode.supportAnimatedGif
import com.github.panpf.sketch.decode.supportAnimatedWebp
import com.github.panpf.sketch.decode.supportMovieGif
import com.github.panpf.zoomimage.sample.image.CoilBlurInterceptor
import com.github.panpf.zoomimage.sample.util.CoilComposeResourceUriFetcher


actual fun Sketch.Builder.platformSketchInitial(context: PlatformContext) {

}

actual fun platformSketchComponents(context: PlatformContext): ComponentRegistry? {
    return ComponentRegistry.Builder().apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            supportAnimatedGif()
        } else {
            supportMovieGif()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            supportAnimatedWebp()
        }
    }.build()
}

actual fun ImageLoader.Builder.platformCoilInitial(context: coil3.PlatformContext) {
    components {
        add(
            CoilBlurInterceptor(
                Sketch.Builder(context).build(),
                radius = 20,
                maskColor = 0x63000000
            )
        )
        add(CoilComposeResourceUriFetcher.Factory())
    }
}