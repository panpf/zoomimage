package com.github.panpf.zoomimage.sample

import androidx.multidex.MultiDexApplication
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.memory.MemoryCache
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.SketchFactory
import com.github.panpf.sketch.cache.internal.LruMemoryCache
import com.github.panpf.sketch.util.Logger
import com.github.panpf.sketch.util.Logger.Level.DEBUG
import com.github.panpf.sketch.util.Logger.Level.INFO
import com.github.panpf.zoomimage.sample.util.getMaxAvailableMemoryCacheBytes
import com.tencent.mmkv.MMKV

class MyApplication : MultiDexApplication(), SketchFactory, ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(this)
    }

    override fun createSketch(): Sketch {
        return Sketch.Builder(this)
            .logger(Logger(if (BuildConfig.DEBUG) DEBUG else INFO))
            .memoryCache(LruMemoryCache(maxSize = getMemoryCacheMaxSize()))
            .build()
    }

    private fun getMemoryCacheMaxSize(): Long {
        val imageLoaderCount = 4
        return getMaxAvailableMemoryCacheBytes() / imageLoaderCount
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache(
                MemoryCache.Builder(this).apply {
                    maxSizeBytes(getMemoryCacheMaxSize().toInt())
                }.build()
            )
            .build()
    }
}