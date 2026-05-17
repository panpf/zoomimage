package com.github.panpf.zoomimage.sample

import coil3.ImageLoader
import com.github.panpf.sketch.PlatformContext
import com.github.panpf.sketch.Sketch
import com.github.panpf.zoomimage.sample.image.CoilComposeResourceUriFetcher
import com.github.panpf.zoomimage.sample.image.CoilComposeResourceUriKeyer
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

actual fun initialApp(context: PlatformContext, koinAppDeclaration: KoinAppDeclaration?) {
    startKoin {
        androidContext(context)
        modules(commonModule(context))
        modules(platformModule(context))
        koinAppDeclaration?.invoke(this)
    }
}

actual fun platformModule(context: PlatformContext): Module = module {

}

actual fun Sketch.Builder.platformSketchInitial(context: PlatformContext) {

}

actual fun ImageLoader.Builder.platformCoilInitial(context: coil3.PlatformContext) {
    components {
        add(CoilComposeResourceUriFetcher.Factory())
        add(CoilComposeResourceUriKeyer())
    }
}