package com.github.panpf.zoomimage.subsampling.internal

import android.graphics.Bitmap
import com.github.panpf.zoomimage.Logger
import com.github.panpf.zoomimage.subsampling.DefaultTileBitmap
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.TileBitmap
import com.github.panpf.zoomimage.subsampling.TileMemoryCache

class TileMemoryCacheHelper(@Suppress("UNUSED_PARAMETER") logger: Logger) {

    var tileMemoryCache: TileMemoryCache? = null
    var disableMemoryCache: Boolean = false

    /**
     * Get the cache of the key
     */
    fun get(key: String): TileBitmap? {
        val tileMemoryCache = tileMemoryCache
        val disableMemoryCache = disableMemoryCache
        if (tileMemoryCache == null || disableMemoryCache) {
            return null
        }
        return tileMemoryCache.get(key)
    }

    fun put(
        key: String,
        bitmap: Bitmap,
        imageKey: String,
        imageInfo: ImageInfo,
        tileBitmapPoolHelper: TileBitmapPoolHelper
    ): TileBitmap {
        val tileMemoryCache = tileMemoryCache
        val disableMemoryCache = disableMemoryCache
        if (tileMemoryCache == null || disableMemoryCache) {
            return DefaultTileBitmap(key, bitmap)
        }
        return tileMemoryCache.put(key, bitmap, imageKey, imageInfo, tileBitmapPoolHelper)
            ?: DefaultTileBitmap(key, bitmap)
    }
}