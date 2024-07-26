package com.github.panpf.zoomimage.test

import com.github.panpf.zoomimage.subsampling.BitmapFrom
import com.github.panpf.zoomimage.subsampling.TileBitmap

class TestTileBitmap(override val key: String, bitmapWidth: Int = 100, bitmapHeight: Int = 100) :
    TileBitmap {

    var displayed: Boolean = false

    override val width: Int = bitmapWidth

    override val height: Int = bitmapHeight

    override val byteCount: Long = bitmapWidth * bitmapHeight * 4L

    override val isRecycled: Boolean
        get() = false

    override val bitmapFrom: BitmapFrom
        get() = BitmapFrom.LOCAL

    override fun recycle() {}

    override fun setIsDisplayed(displayed: Boolean) {
        this.displayed = displayed
    }
}
