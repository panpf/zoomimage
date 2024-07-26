/*
 * Copyright (C) 2023 panpf <panpfpanpf@outlook.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.panpf.zoomimage.subsampling.internal

import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.SkiaBitmap
import com.github.panpf.zoomimage.subsampling.SkiaCanvas
import com.github.panpf.zoomimage.subsampling.SkiaImage
import com.github.panpf.zoomimage.subsampling.SkiaRect
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.toSkiaRect
import kotlin.math.ceil

/**
 * Create a [DecodeHelper] instance using [ImageSource], on the non Android platform, [SkiaDecodeHelper] will be used
 *
 * @see com.github.panpf.zoomimage.core.nonandroid.test.subsampling.internal.DecodesNonAndroidTest.testCreateDecodeHelper
 */
internal actual fun createDecodeHelper(imageSource: ImageSource): DecodeHelper {
    return SkiaDecodeHelper.Factory().create(imageSource)
}


/**
 * Checks whether the specified image type supports subsampling, on the non Android platform, it mainly depends on the types supported by SkiaImage.
 *
 * @see com.github.panpf.zoomimage.core.nonandroid.test.subsampling.internal.DecodesNonAndroidTest.testCheckSupportSubsamplingByMimeType
 */
internal actual fun checkSupportSubsamplingByMimeType(mimeType: String): Boolean =
    !"image/gif".equals(mimeType, true)

/**
 * Decode the specified region of the image
 *
 * @see com.github.panpf.zoomimage.core.nonandroid.test.subsampling.internal.DecodesNonAndroidTest.testDecodeRegion
 */
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
 * Calculate the size of the sampled Bitmap
 *
 * @see com.github.panpf.zoomimage.core.nonandroid.test.subsampling.internal.DecodesNonAndroidTest.testCalculateSampledBitmapSize
 */
internal fun calculateSampledBitmapSize(
    imageSize: IntSizeCompat,
    sampleSize: Int,
): IntSizeCompat {
    val widthValue = imageSize.width / sampleSize.toDouble()
    val heightValue = imageSize.height / sampleSize.toDouble()
    val width: Int = ceil(widthValue).toInt()
    val height: Int = ceil(heightValue).toInt()
    return IntSizeCompat(width, height)
}