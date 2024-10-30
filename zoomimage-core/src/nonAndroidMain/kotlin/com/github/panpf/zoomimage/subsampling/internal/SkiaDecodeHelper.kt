/*
 * Copyright (C) 2024 panpf <panpfpanpf@outlook.com>
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

import com.github.panpf.zoomimage.subsampling.BitmapTileImage
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.toSkiaRect
import okio.buffer
import okio.use
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Codec
import org.jetbrains.skia.Data
import org.jetbrains.skia.Image
import org.jetbrains.skia.Rect
import org.jetbrains.skia.impl.use
import kotlin.math.ceil

/**
 * Use [Image] to decode the image
 *
 * *Not thread safe*
 *
 * @see com.github.panpf.zoomimage.core.nonandroid.test.subsampling.internal.SkiaDecoderHelperTest
 */
class SkiaDecodeHelper(
    override val imageSource: ImageSource,
    override val imageInfo: ImageInfo,
    override val supportRegion: Boolean,
    val bytes: ByteArray,
    val image: Image,
) : DecodeHelper {

    override fun decodeRegion(
        key: String,
        region: IntRectCompat,
        sampleSize: Int
    ): BitmapTileImage {
        // Image will parse exif orientation and does not support closing
        val widthValue = region.width / sampleSize.toDouble()
        val heightValue = region.height / sampleSize.toDouble()
        val bitmapWidth: Int = ceil(widthValue).toInt()
        val bitmapHeight: Int = ceil(heightValue).toInt()
        val bitmap = Bitmap().apply {
            allocN32Pixels(bitmapWidth, bitmapHeight)
        }
        val canvas = Canvas(bitmap)
        canvas.drawImageRect(
            image = image,
            src = region.toSkiaRect(),
            dst = Rect.makeWH(bitmapWidth.toFloat(), bitmapHeight.toFloat())
        )
        return BitmapTileImage(bitmap, key, fromCache = false)
    }

    override fun close() {
        image.close()
    }

    override fun copy(): DecodeHelper {
        return SkiaDecodeHelper(
            imageSource = imageSource,
            imageInfo = imageInfo,
            supportRegion = supportRegion,
            bytes = bytes,
            image = Image.makeFromEncoded(bytes)
        )
    }

    override fun toString(): String {
        return "SkiaDecodeHelper(imageSource=$imageSource, imageInfo=$imageInfo, supportRegion=$supportRegion)"
    }

    class Factory : DecodeHelper.Factory {

        override fun checkSupport(mimeType: String): Boolean? = when (mimeType) {
            "image/jpeg", "image/png", "image/webp", "image/bmp" -> true
            "image/svg+xml" -> false
            // TODO Get the skiko version and return false directly.
            //  "image/heic", "image/heif", "image/avif" -> false
            else -> null
        }

        override fun create(imageSource: ImageSource): SkiaDecodeHelper {
            val bytes = imageSource.openSource().buffer().use { it.readByteArray() }
            val image = Image.makeFromEncoded(bytes)
            val imageInfo = readImageInfo(bytes, image)
            val supportRegion = checkSupport(imageInfo.mimeType) ?: true
            return SkiaDecodeHelper(
                imageSource = imageSource,
                imageInfo = imageInfo,
                supportRegion = supportRegion,
                bytes = bytes,
                image = image
            )
        }

        private fun readImageInfo(bytes: ByteArray, image: Image): ImageInfo {
            val encodedImageFormat = Codec.makeFromData(Data.makeFromBytes(bytes)).use {
                it.encodedImageFormat
            }
            val mimeType = "image/${encodedImageFormat.name.lowercase()}"
            return ImageInfo(
                width = image.width,
                height = image.height,
                mimeType = mimeType
            )
        }
    }
}