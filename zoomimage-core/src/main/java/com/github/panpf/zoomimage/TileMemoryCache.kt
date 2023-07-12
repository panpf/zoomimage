package com.github.panpf.zoomimage

import android.graphics.Bitmap
import com.github.panpf.zoomimage.core.IntSizeCompat

interface TileMemoryCache {
    /**
     * Get the cache of the key
     */
    fun get(key: String): TileBitmap?

    fun put(
        key: String,
        bitmap: Bitmap,
        imageKey: String,
        imageSize: IntSizeCompat,
        imageMimeType: String,
        imageExifOrientation: Int,
        disallowReuseBitmap: Boolean
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