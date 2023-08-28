package com.github.panpf.zoomimage.subsampling

import android.graphics.Bitmap
import com.github.panpf.zoomimage.subsampling.internal.TileBitmapPoolHelper

/**
 * Tile memory cache container interface, you need to implement it to customize tile memory cache
 */
interface TileMemoryCache {

    /**
     * Get the cache of the key
     */
    fun get(key: String): TileBitmap?

    /**
     * Put the cache of the key
     *
     * @param key Cache key
     * @param bitmap Tile bitmap
     * @param imageKey Image key
     * @param imageInfo Image information
     * @param tileBitmapPoolHelper The helper class used to access [TileBitmapPool]
     */
    fun put(
        key: String,
        bitmap: Bitmap,
        imageKey: String,
        imageInfo: ImageInfo,
        tileBitmapPoolHelper: TileBitmapPoolHelper
    ): TileBitmap?
}

/**
 * Tile bitmap interface, you need to implement it to customize tile bitmap
 */
interface TileBitmap {

    /**
     * Cache key
     */
    val key: String

    /**
     * Tile bitmap
     */
    val bitmap: Bitmap?

    /**
     * TileBitmap displays callbacks when the state changes
     */
    fun setIsDisplayed(displayed: Boolean)
}

class DefaultTileBitmap(override val key: String, override val bitmap: Bitmap?) : TileBitmap {

    override fun setIsDisplayed(displayed: Boolean) {

    }
}