package com.github.panpf.zoomimage.subsampling

import android.graphics.Bitmap
import com.github.panpf.zoomimage.subsampling.internal.TileBitmapPoolHelper

interface TileMemoryCache {
    /**
     * Get the cache of the key
     */
    fun get(key: String): TileBitmap?

    fun put(
        key: String,
        bitmap: Bitmap,
        imageKey: String,
        imageInfo: ImageInfo,
        tileBitmapPoolHelper: TileBitmapPoolHelper
    ): TileBitmap?
}

interface TileBitmap {
    val key: String

    val bitmap: Bitmap?

    fun setIsDisplayed(displayed: Boolean)
}

class DefaultTileBitmap(override val key: String, override val bitmap: Bitmap?) : TileBitmap {

    override fun setIsDisplayed(displayed: Boolean) {

    }
}