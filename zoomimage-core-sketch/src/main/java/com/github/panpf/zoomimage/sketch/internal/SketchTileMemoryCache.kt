package com.github.panpf.zoomimage.sketch.internal

import android.graphics.Bitmap
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.cache.CountBitmap
import com.github.panpf.sketch.cache.MemoryCache
import com.github.panpf.sketch.decode.ImageInfo
import com.github.panpf.zoomimage.subsampling.TileBitmap
import com.github.panpf.zoomimage.subsampling.TileBitmapPool
import com.github.panpf.zoomimage.subsampling.TileMemoryCache

class SketchTileMemoryCache(private val sketch: Sketch) : TileMemoryCache {

    override fun get(key: String): TileBitmap? {
        return sketch.memoryCache[key]?.let {
            SketchTileBitmap(key, it)
        }
    }

    override fun put(
        key: String,
        bitmap: Bitmap,
        imageKey: String,
        imageInfo: com.github.panpf.zoomimage.subsampling.ImageInfo,
        tileBitmapPool: TileBitmapPool?
    ): TileBitmap {
        val newCountBitmap = CountBitmap(
            cacheKey = key,
            originBitmap = bitmap,
            bitmapPool = sketch.bitmapPool,
            disallowReuseBitmap = tileBitmapPool == null,
        )
        val newCacheValue = MemoryCache.Value(
            countBitmap = newCountBitmap,
            imageUri = imageKey,
            requestKey = imageKey,
            requestCacheKey = key,
            imageInfo = ImageInfo(
                imageInfo.width,
                imageInfo.height,
                imageInfo.mimeType,
                imageInfo.exifOrientation
            ),
            transformedList = null,
            extras = null,
        )
        sketch.memoryCache.put(key, newCacheValue)
        return SketchTileBitmap(key, newCacheValue)
    }
}