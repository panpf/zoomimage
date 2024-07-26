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

import com.github.panpf.zoomimage.subsampling.BitmapFrom
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.SkiaBitmap
import com.github.panpf.zoomimage.subsampling.SkiaCanvas
import com.github.panpf.zoomimage.subsampling.SkiaImage
import com.github.panpf.zoomimage.subsampling.SkiaRect
import com.github.panpf.zoomimage.subsampling.SkiaTileBitmap
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.toSkiaRect
import okio.buffer
import okio.use
import org.jetbrains.skia.Codec
import org.jetbrains.skia.Data
import org.jetbrains.skia.impl.use
import kotlin.math.ceil

/**
 * Use [SkiaImage] to decode the image
 *
 * *Not thread safe*
 *
 * @see com.github.panpf.zoomimage.core.nonandroid.test.subsampling.internal.SkiaDecoderHelperTest
 */
class SkiaDecodeHelper(
    val imageSource: ImageSource,
    override val imageInfo: ImageInfo,
    override val supportRegion: Boolean,
    val bytes: ByteArray,
    val skiaImage: SkiaImage,
) : DecodeHelper {

    override fun decodeRegion(
        key: String,
        region: IntRectCompat,
        sampleSize: Int
    ): SkiaTileBitmap {
        // SkiaImage will parse exif orientation and does not support closing
        val widthValue = region.width / sampleSize.toDouble()
        val heightValue = region.height / sampleSize.toDouble()
        val bitmapWidth: Int = ceil(widthValue).toInt()
        val bitmapHeight: Int = ceil(heightValue).toInt()
        val skiaBitmap = SkiaBitmap().apply {
            allocN32Pixels(bitmapWidth, bitmapHeight)
        }
        val canvas = SkiaCanvas(skiaBitmap)
        canvas.drawImageRect(
            image = skiaImage,
            src = region.toSkiaRect(),
            dst = SkiaRect.makeWH(bitmapWidth.toFloat(), bitmapHeight.toFloat())
        )
        return SkiaTileBitmap(skiaBitmap, key, BitmapFrom.LOCAL)
    }

    override fun close() {
        skiaImage.close()
    }

    override fun copy(): DecodeHelper {
        return SkiaDecodeHelper(
            imageSource = imageSource,
            imageInfo = imageInfo,
            supportRegion = supportRegion,
            bytes = bytes,
            skiaImage = SkiaImage.makeFromEncoded(bytes)
        )
    }

    override fun toString(): String {
        return "SkiaDecodeHelper(imageSource=$imageSource, imageInfo=$imageInfo,)"
    }

    class Factory : DecodeHelper.Factory {

        override fun create(imageSource: ImageSource): SkiaDecodeHelper {
            val bytes = imageSource.openSource().buffer().use { it.readByteArray() }
            val skiaImage = SkiaImage.makeFromEncoded(bytes)
            val imageInfo = readImageInfo(bytes, skiaImage)
            val supportRegion = checkSupportSubsamplingByMimeType(imageInfo.mimeType)
            return SkiaDecodeHelper(
                imageSource = imageSource,
                imageInfo = imageInfo,
                supportRegion = supportRegion,
                bytes = bytes,
                skiaImage = skiaImage
            )
        }

        private fun readImageInfo(bytes: ByteArray, skiaImage: SkiaImage): ImageInfo {
            val encodedImageFormat = Codec.makeFromData(Data.makeFromBytes(bytes)).use {
                it.encodedImageFormat
            }
            val mimeType = "image/${encodedImageFormat.name.lowercase()}"
            return ImageInfo(
                width = skiaImage.width,
                height = skiaImage.height,
                mimeType = mimeType
            )
        }
    }
}