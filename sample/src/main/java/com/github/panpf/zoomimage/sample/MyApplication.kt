package com.github.panpf.zoomimage.sample

import androidx.multidex.MultiDexApplication
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.memory.MemoryCache
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.SketchFactory
import com.github.panpf.sketch.cache.internal.LruMemoryCache
import com.github.panpf.sketch.util.Logger
import com.github.panpf.sketch.util.Logger.Level.DEBUG
import com.github.panpf.sketch.util.Logger.Level.INFO
import com.github.panpf.zoomimage.sample.util.getMaxAvailableMemoryCacheBytes
import com.squareup.picasso.LruCache
import com.squareup.picasso.Picasso
import com.tencent.mmkv.MMKV

class MyApplication : MultiDexApplication(), SketchFactory, ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(this)

        Picasso.setSingletonInstance(Picasso.Builder(this).apply {
            memoryCache(LruCache(getMemoryCacheMaxSize().toInt()))
        }.build())

        Glide.init(this, GlideBuilder().setMemoryCache(LruResourceCache(getMemoryCacheMaxSize())))
    }

    override fun createSketch(): Sketch {
        return Sketch.Builder(this)
            .logger(Logger(if (BuildConfig.DEBUG) DEBUG else INFO))
            .memoryCache(LruMemoryCache(maxSize = getMemoryCacheMaxSize()))
            .build()
    }

    private fun getMemoryCacheMaxSize(): Long {
        // 集成了四个图片加载器所以要把内存缓存分成四份
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