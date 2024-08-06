package com.github.panpf.zoomimage.sample

import coil3.PlatformContext as CoilPlatformContext
import com.github.panpf.sketch.PlatformContext as SketchPlatformContext
import coil3.SingletonImageLoader
import com.github.panpf.sketch.SingletonSketch
import com.github.panpf.zoomimage.sample.util.ignoreFirst
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class)
fun cleanImageLoaderMemoryCache() {
    GlobalScope.launch {
        val sketchContext = SketchPlatformContext.INSTANCE
        val coilContext = CoilPlatformContext.INSTANCE
        val appSettings = sketchContext.appSettings
        appSettings.composeImageLoader.ignoreFirst().collect { newImageLoader ->
            if (newImageLoader != "Sketch" && newImageLoader != "Basic") {
                SingletonSketch.get(sketchContext).memoryCache.clear()
            }
            if (newImageLoader != "Coil") {
                SingletonImageLoader.get(coilContext).memoryCache?.clear()
            }
        }
    }
}