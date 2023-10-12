package com.github.panpf.zoomimage.subsampling

import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat

interface ExifOrientation {

    fun name(): String

    fun applyToSize( size: IntSizeCompat): IntSizeCompat

    fun addToSize( size: IntSizeCompat): IntSizeCompat

    fun addToRect(srcRect: IntRectCompat, imageSize: IntSizeCompat): IntRectCompat

    fun applyToTileBitmap(bitmapReuseHelper: TileBitmapReuseHelper?, tileBitmap: TileBitmap): TileBitmap
}

fun ExifOrientation.applyToImageInfo(imageInfo: ImageInfo): ImageInfo {
    val newSize = applyToSize(imageInfo.size)
    return imageInfo.copy(size = newSize)
}