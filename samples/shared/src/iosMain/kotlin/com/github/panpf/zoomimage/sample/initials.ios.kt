package com.github.panpf.zoomimage.sample

import coil3.ImageLoader
import coil3.SingletonImageLoader
import com.github.panpf.sketch.PlatformContext
import com.github.panpf.sketch.Sketch
import com.github.panpf.zoomimage.images.coil.CoilKotlinResourceUriFetcher
import com.github.panpf.zoomimage.images.coil.CoilKotlinResourceUriKeyer
import com.github.panpf.zoomimage.sample.image.CoilComposeResourceUriFetcher
import com.github.panpf.zoomimage.sample.image.CoilComposeResourceUriKeyer
import com.github.panpf.zoomimage.sample.image.CoilPhotoAssetUriFetcher
import com.github.panpf.zoomimage.sample.image.CoilPhotoAssetUriKeyer
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

actual fun initialApp(context: PlatformContext, koinAppDeclaration: KoinAppDeclaration?) {
    startKoin {
        modules(commonModule(context))
        modules(platformModule(context))
        koinAppDeclaration?.invoke(this)
    }
    SingletonImageLoader.setSafe { newCoil(it) }
    cleanImageLoaderMemoryCache()
}

actual fun platformModule(context: PlatformContext): Module = module {

}

actual fun Sketch.Builder.platformSketchInitial(context: PlatformContext) {

}

actual fun ImageLoader.Builder.platformCoilInitial(context: coil3.PlatformContext) {
    // TODO The ios platform coil supports avif heif format images
    components {
        add(CoilComposeResourceUriFetcher.Factory())
        add(CoilComposeResourceUriKeyer())
        add(CoilPhotoAssetUriFetcher.Factory())
        add(CoilPhotoAssetUriKeyer())
        add(CoilKotlinResourceUriFetcher.Factory())
        add(CoilKotlinResourceUriKeyer())
    }
}