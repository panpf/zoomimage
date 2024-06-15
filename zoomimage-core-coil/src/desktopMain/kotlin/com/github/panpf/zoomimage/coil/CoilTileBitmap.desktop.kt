package com.github.panpf.zoomimage.coil

import coil3.BitmapImage
import coil3.annotation.ExperimentalCoilApi
import coil3.memory.MemoryCache
import com.github.panpf.zoomimage.subsampling.CacheTileBitmap
import com.github.panpf.zoomimage.subsampling.DesktopTileBitmap
import org.jetbrains.skia.Bitmap

actual class CoilTileBitmap(
    override val key: String,
    private val cacheValue: MemoryCache.Value
) : CacheTileBitmap, DesktopTileBitmap {

    @OptIn(ExperimentalCoilApi::class)
    override val bitmap: Bitmap by lazy {
        (cacheValue.image as BitmapImage).bitmap
    }

    override val width: Int = bitmap.width

    override val height: Int = bitmap.height

    override val byteCount: Int = bitmap.computeByteSize()

    override val isRecycled: Boolean = false

    override fun recycle() {
    }

    override fun setIsDisplayed(displayed: Boolean) = Unit
}