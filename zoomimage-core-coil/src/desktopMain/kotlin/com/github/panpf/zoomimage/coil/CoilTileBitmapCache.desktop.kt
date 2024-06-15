package com.github.panpf.zoomimage.coil

import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.asCoilImage
import coil3.memory.MemoryCache
import com.github.panpf.zoomimage.subsampling.CacheTileBitmap
import com.github.panpf.zoomimage.subsampling.DesktopTileBitmap
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.TileBitmap
import com.github.panpf.zoomimage.subsampling.TileBitmapCache

actual class CoilTileBitmapCache actual constructor(
    private val imageLoader: ImageLoader
) : TileBitmapCache {
    override fun get(key: String): CacheTileBitmap? {
        return imageLoader.memoryCache
            ?.get(MemoryCache.Key(key))
            ?.let { CoilTileBitmap(key, it) }
    }

    @OptIn(ExperimentalCoilApi::class)
    override fun put(
        key: String,
        tileBitmap: TileBitmap,
        imageUrl: String,
        imageInfo: ImageInfo,
        disallowReuseBitmap: Boolean
    ): CacheTileBitmap? {
        val bitmap = (tileBitmap as DesktopTileBitmap).bitmap ?: return null
        val newCacheValue = MemoryCache.Value(bitmap.asCoilImage())
        val memoryCache = imageLoader.memoryCache ?: return null
        memoryCache[MemoryCache.Key(key)] = newCacheValue
        return CoilTileBitmap(key, newCacheValue)
    }
}