package com.github.panpf.zoomimage.sample

import coil3.ImageLoader
import coil3.SingletonImageLoader
import com.github.panpf.sketch.PlatformContext
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.cache.DiskCache
import com.github.panpf.zoomimage.sample.util.PexelsCompatibleInterceptor
import com.github.panpf.zoomimage.util.AppDirs
import com.github.panpf.zoomimage.util.coil.CoilComposeResourceUriFetcher
import com.github.panpf.zoomimage.util.coil.CoilKotlinResourceUriFetcher
import okio.Path.Companion.toPath
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
    downloadCacheOptions {
        DiskCache.Options(
            appCacheDirectory = AppDirs.getCacheDir(AppInfos.SAMPLE_APP_NAME).toPath()
        )
    }
    resultCacheOptions {
        DiskCache.Options(
            appCacheDirectory = AppDirs.getCacheDir(AppInfos.SAMPLE_APP_NAME).toPath()
        )
    }

    addComponents {
        add(PexelsCompatibleInterceptor())
    }
}

actual fun ImageLoader.Builder.platformCoilInitial(context: coil3.PlatformContext) {
    components {
        add(CoilComposeResourceUriFetcher.Factory())
        add(CoilKotlinResourceUriFetcher.Factory())
    }
}