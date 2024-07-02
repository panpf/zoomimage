package com.github.panpf.zoomimage.sample

import com.github.panpf.sketch.ComponentRegistry
import com.github.panpf.sketch.PlatformContext
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.decode.supportSkiaAnimatedWebp
import com.github.panpf.sketch.decode.supportSkiaGif

actual fun Sketch.Builder.platformSketchInitial(context: PlatformContext) {

}

actual fun platformSketchComponents(context: PlatformContext): ComponentRegistry? {
    return ComponentRegistry.Builder().apply {
        supportSkiaGif()
        supportSkiaAnimatedWebp()
        supportSkiaAnimatedWebp()
    }.build()
}