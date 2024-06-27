package com.github.panpf.zoomimage.compose.sketch

import com.github.panpf.sketch.ComposeBitmapImage
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.asSketchImage
import com.github.panpf.sketch.cache.ComposeBitmapImageValue
import com.github.panpf.zoomimage.compose.subsampling.ComposeTileBitmap
import com.github.panpf.zoomimage.subsampling.BitmapFrom
import com.github.panpf.zoomimage.subsampling.TileBitmap
import com.github.panpf.zoomimage.subsampling.TileBitmapCache

class SketchComposeTileBitmapCache constructor(
    private val sketch: Sketch,
) : TileBitmapCache {

    override fun get(key: String): TileBitmap? {
        val cacheValue = sketch.memoryCache[key] ?: return null
        cacheValue as ComposeBitmapImageValue
        val composeBitmapImage = cacheValue.image as ComposeBitmapImage
        val bitmap = composeBitmapImage.bitmap
        return ComposeTileBitmap(bitmap, key, BitmapFrom.MEMORY_CACHE)
    }

    override fun put(
        key: String,
        tileBitmap: TileBitmap,
        imageUrl: String,
        imageInfo: com.github.panpf.zoomimage.subsampling.ImageInfo,
        disallowReuseBitmap: Boolean
    ): TileBitmap? {
        tileBitmap as ComposeTileBitmap
        val bitmap = tileBitmap.bitmap
        val cacheValue =
            ComposeBitmapImageValue(bitmap.asSketchImage(), extras = null)
        sketch.memoryCache.put(key, cacheValue)
        return null
    }
}