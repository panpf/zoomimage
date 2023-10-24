package com.github.panpf.zoomimage.subsampling

import android.graphics.Bitmap

interface AndroidTileBitmap : TileBitmap {
    val bitmap: Bitmap?
}

interface AndroidCacheTileBitmap : AndroidTileBitmap, CacheTileBitmap

class DefaultAndroidTileBitmap(override val bitmap: Bitmap) : AndroidTileBitmap {

    override val width: Int = bitmap.width

    override val height: Int = bitmap.height

    override val byteCount: Int = bitmap.byteCount

    override fun recycle() {
        bitmap.recycle()
    }

    override val isRecycled: Boolean
        get() = bitmap.isRecycled
}