package com.github.panpf.zoomimage

import android.graphics.Bitmap

interface TileMemoryCache {
    /**
     * Get the cache of the key
     */
    fun get(key: String): TileBitmap?

    fun put(
        key: String,
        bitmap: Bitmap,
        imageKey: String,
        imageSize: Size,
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