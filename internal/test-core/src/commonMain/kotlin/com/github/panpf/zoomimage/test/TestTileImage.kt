package com.github.panpf.zoomimage.test

import com.github.panpf.zoomimage.subsampling.TileImage

class TestTileImage(override val key: String, bitmapWidth: Int = 100, bitmapHeight: Int = 100) :
    TileImage {

    var displayed: Boolean = false

    override val width: Int = bitmapWidth

    override val height: Int = bitmapHeight

    override val byteCount: Long = bitmapWidth * bitmapHeight * 4L

    override val isRecycled: Boolean = false

    override val fromCache: Boolean = false

    override fun recycle() {}

    override fun setIsDisplayed(displayed: Boolean) {
        this.displayed = displayed
    }
}
