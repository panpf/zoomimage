package com.github.panpf.zoomimage.test

import com.github.panpf.zoomimage.images.ComposeResImageFile
import com.github.panpf.zoomimage.subsampling.TileBitmap
import com.github.panpf.zoomimage.util.IntSizeCompat
import kotlin.math.ceil

expect suspend fun ComposeResImageFile.decode(): TileBitmap

/**
 * Calculate the size of the sampled Bitmap, support for Skia Image
 *
 * @see com.github.panpf.sketch.core.nonandroid.test.decode.internal.DecodesNonAndroidTest.testCalculateSampledBitmapSize
 */
fun calculateSampledBitmapSize(
    imageSize: IntSizeCompat,
    sampleSize: Int,
): IntSizeCompat {
    val widthValue = imageSize.width / sampleSize.toDouble()
    val heightValue = imageSize.height / sampleSize.toDouble()
    val width: Int = ceil(widthValue).toInt()
    val height: Int = ceil(heightValue).toInt()
    return IntSizeCompat(width, height)
}