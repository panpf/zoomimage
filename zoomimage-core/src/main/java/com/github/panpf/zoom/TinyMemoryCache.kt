package com.github.panpf.zoom

import android.graphics.Bitmap

interface TinyMemoryCache {
    /**
     * Get the cache of the key
     */
    fun get(key: String): CacheBitmap?

    fun put(
        key: String,
        bitmap: Bitmap,
        imageKey: String,
        imageSize: Size,
        imageMimeType: String,
        imageExifOrientation: Int,
        disallowReuseBitmap: Boolean
    ): CacheBitmap
}

interface CacheBitmap {
    val key: String

    val bitmap: Bitmap?

    fun setIsDisplayed(displayed: Boolean)
}

class DefaultCacheBitmap(override val key: String, override val bitmap: Bitmap?) : CacheBitmap {

    override fun setIsDisplayed(displayed: Boolean) {

    }
}