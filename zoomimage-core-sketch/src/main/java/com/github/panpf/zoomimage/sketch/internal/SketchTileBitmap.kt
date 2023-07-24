package com.github.panpf.zoomimage.sketch.internal

import android.graphics.Bitmap
import com.github.panpf.sketch.cache.MemoryCache
import com.github.panpf.zoomimage.subsampling.TileBitmap

class SketchTileBitmap constructor(
    override val key: String,
    private val cacheValue: MemoryCache.Value,
    private val caller: String,
) : TileBitmap {

    override val bitmap: Bitmap?
        get() = cacheValue.countBitmap.bitmap

    override fun setIsDisplayed(displayed: Boolean) {
        cacheValue.countBitmap.setIsDisplayed(displayed, caller)
    }
}