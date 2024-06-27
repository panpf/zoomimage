package com.github.panpf.zoomimage.subsampling.internal

import com.github.panpf.zoomimage.SkiaBitmap
import com.github.panpf.zoomimage.SkiaCanvas
import com.github.panpf.zoomimage.SkiaImage
import com.github.panpf.zoomimage.SkiaRect
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.toSkiaRect
import kotlin.math.ceil

internal actual fun createDecodeHelper(imageSource: ImageSource): DecodeHelper? {
    return SkiaDecodeHelper(imageSource)
}

internal fun SkiaImage.decodeRegion(srcRect: IntRectCompat, sampleSize: Int): SkiaBitmap {
    val bitmapSize =
        calculateSampledBitmapSize(IntSizeCompat(srcRect.width, srcRect.height), sampleSize)
    val bitmap = SkiaBitmap().apply {
        allocN32Pixels(bitmapSize.width, bitmapSize.height)
    }
    val canvas = SkiaCanvas(bitmap)
    canvas.drawImageRect(
        image = this,
        src = srcRect.toSkiaRect(),
        dst = SkiaRect.makeWH(bitmapSize.width.toFloat(), bitmapSize.height.toFloat())
    )
    return bitmap
}

/**
 * Calculate the size of the sampled Bitmap, support for BitmapFactory or ImageDecoder
 */
internal fun calculateSampledBitmapSize(
    imageSize: IntSizeCompat,
    sampleSize: Int,
    mimeType: String? = null
): IntSizeCompat {
    val widthValue = imageSize.width / sampleSize.toDouble()
    val heightValue = imageSize.height / sampleSize.toDouble()
    val width: Int = ceil(widthValue).toInt()
    val height: Int = ceil(heightValue).toInt()
    return IntSizeCompat(width, height)
}