package com.github.panpf.zoomimage.sample

import coil3.SingletonImageLoader
import com.github.panpf.sketch.SingletonSketch
import com.github.panpf.zoomimage.sample.util.ignoreFirst
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import coil3.PlatformContext as CoilPlatformContext
import com.github.panpf.sketch.PlatformContext as SketchPlatformContext

fun cleanImageLoaderMemoryCache() {
    GlobalScope.launch {
        val sketchContext = SketchPlatformContext.INSTANCE
        val coilContext = CoilPlatformContext.INSTANCE
        val appSettings = sketchContext.appSettings
        appSettings.composeImageLoader.ignoreFirst().collect { newImageLoader ->
            println("Switch image loader to $newImageLoader")
            composeImageLoaders.asSequence().map { it.name }.forEach { imageLoaderName ->
                if (imageLoaderName != newImageLoader) {
                    when (imageLoaderName) {
                        "Sketch", "Basic" -> {
                            SingletonSketch.get(sketchContext).memoryCache.clear()
                        }

                        "Coil" -> {
                            SingletonImageLoader.get(coilContext).memoryCache?.clear()
                        }

                        else -> throw IllegalArgumentException("Unknown image loader: $newImageLoader")
                    }
                    println("Clean $imageLoaderName memory cache")
                }
            }
        }
    }
}