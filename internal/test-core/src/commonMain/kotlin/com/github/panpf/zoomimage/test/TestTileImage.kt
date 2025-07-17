package com.github.panpf.zoomimage.test

import com.github.panpf.zoomimage.subsampling.TileImage

@Suppress("RedundantConstructorKeyword")
class TestTileImage constructor(
    override val width: Int = 100,
    override val height: Int = 100
) : TileImage {

    var displayed: Boolean = false

    override val byteCount: Long = width * height * 4L

    override val isRecycled: Boolean = false

    override fun recycle() {}

    override fun setIsDisplayed(displayed: Boolean) {
        this.displayed = displayed
    }
}
