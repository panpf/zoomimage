package com.github.panpf.zoomimage.compose.sketch

import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.asSketchImage
import com.github.panpf.zoomimage.subsampling.BitmapFrom
import com.github.panpf.zoomimage.subsampling.SkiaTileBitmap
import com.github.panpf.zoomimage.subsampling.TileBitmap
import com.github.panpf.zoomimage.subsampling.TileBitmapCache

class SketchSkiaTileBitmapCache constructor(
    private val sketch: Sketch,
) : TileBitmapCache {

    override fun get(key: String): TileBitmap? {
        val cacheValue = sketch.memoryCache[key] ?: return null
        cacheValue as SkiaBitmapImageValue
        val skiaBitmapImage = cacheValue.image
        val skiaBitmap = skiaBitmapImage.bitmap
        return SkiaTileBitmap(skiaBitmap, key, BitmapFrom.MEMORY_CACHE)
    }

    override fun put(
        key: String,
        tileBitmap: TileBitmap,
        imageUrl: String,
        imageInfo: com.github.panpf.zoomimage.subsampling.ImageInfo,
        disallowReuseBitmap: Boolean
    ): TileBitmap? {
        tileBitmap as SkiaTileBitmap
        val bitmap = tileBitmap.bitmap
        val cacheValue =
            SkiaBitmapImageValue(bitmap.asSketchImage(), extras = null)
        sketch.memoryCache.put(key, cacheValue)
        return null
    }
}