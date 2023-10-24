package com.github.panpf.zoomimage.subsampling

import java.awt.image.BufferedImage

class DesktopTileBitmap(val bufferedImage: BufferedImage) : TileBitmap {

    override val width: Int = bufferedImage.width

    override val height: Int = bufferedImage.height

    override val byteCount: Int = width * height * (bufferedImage.colorModel.pixelSize / 8)

    override fun recycle() {

    }

    override val isRecycled: Boolean = false
}