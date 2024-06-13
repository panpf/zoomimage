package com.github.panpf.zoomimage.coil

import coil3.BitmapImage
import coil3.annotation.ExperimentalCoilApi
import coil3.memory.MemoryCache
import com.github.panpf.zoomimage.subsampling.CacheTileBitmap
import com.github.panpf.zoomimage.subsampling.DesktopTileBitmap
import org.jetbrains.skiko.toBufferedImage
import java.awt.image.BufferedImage

actual class CoilTileBitmap(
    override val key: String,
    private val cacheValue: MemoryCache.Value
) : CacheTileBitmap, DesktopTileBitmap {

    @OptIn(ExperimentalCoilApi::class)
    override val bufferedImage: BufferedImage by lazy {
        (cacheValue.image as BitmapImage).bitmap.toBufferedImage()
    }

    override val width: Int = bufferedImage.width

    override val height: Int = bufferedImage.height

    override val byteCount: Int = width * height * (bufferedImage.colorModel.pixelSize / 8)

    override val isRecycled: Boolean = false

    override fun recycle() {}

    override fun setIsDisplayed(displayed: Boolean) = Unit
}