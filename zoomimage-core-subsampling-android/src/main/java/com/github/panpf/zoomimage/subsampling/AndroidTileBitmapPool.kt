package com.github.panpf.zoomimage.subsampling

import android.graphics.Bitmap

interface AndroidTileBitmapPool : TileBitmapPool {

    /**
     * Puts the specified [bitmap] into the pool.
     *
     * @return If true, it was successfully placed, otherwise call the [bitmap. recycle] method to recycle the [Bitmap].
     */
    fun put(bitmap: Bitmap): Boolean

    /**
     * Get a reusable [Bitmap]. Note that all colors are erased before returning
     */
    fun get(width: Int, height: Int, config: Bitmap.Config): Bitmap?
}