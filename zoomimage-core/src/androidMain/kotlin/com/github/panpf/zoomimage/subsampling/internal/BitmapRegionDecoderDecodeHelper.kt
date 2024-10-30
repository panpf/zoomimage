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

import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.Rect
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import com.github.panpf.zoomimage.subsampling.BitmapTileImage
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.util.IntRectCompat
import okio.buffer
import java.io.BufferedInputStream

/**
 * Use [BitmapRegionDecoder] to decode the image
 *
 * *Not thread safe*
 *
 * @see com.github.panpf.zoomimage.core.android.test.subsampling.internal.BitmapRegionDecoderDecodeHelperTest
 */
class BitmapRegionDecoderDecodeHelper(
    override val imageSource: ImageSource,
    override val imageInfo: ImageInfo,
    override val supportRegion: Boolean,
    val exifOrientationHelper: ExifOrientationHelper,
) : DecodeHelper {

    private var _decoder: BitmapRegionDecoder? = null
    private var _inputStream: BufferedInputStream? = null

    override fun decodeRegion(
        key: String,
        region: IntRectCompat,
        sampleSize: Int
    ): BitmapTileImage {
        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
        }
        val originalRegion = exifOrientationHelper
            .applyToRect(region, imageInfo.size, reverse = true)
        val decoder = getOrCreateDecoder()
        val bitmap = decoder.decodeRegion(originalRegion.toAndroidRect(), options)
            ?: throw Exception("Invalid image. region decode return null")
        val tileImage = BitmapTileImage(bitmap, key, fromCache = false)
        val correctedImage = exifOrientationHelper.applyToTileImage(tileImage)
        return correctedImage
    }

    private fun getOrCreateDecoder(): BitmapRegionDecoder {
        val decoder = _decoder
        if (decoder != null) {
            return decoder
        }
        val inputStream = imageSource.openSource().buffer().inputStream().buffered()
        val newDecoder = if (VERSION.SDK_INT >= VERSION_CODES.S) {
            BitmapRegionDecoder.newInstance(inputStream)!!
        } else {
            @Suppress("DEPRECATION")
            BitmapRegionDecoder.newInstance(inputStream, false)!!
        }
        this._decoder = newDecoder
        this._inputStream = inputStream
        return newDecoder
    }

    override fun close() {
        _decoder?.recycle()
        _inputStream?.close()
    }

    override fun copy(): DecodeHelper {
        return BitmapRegionDecoderDecodeHelper(
            imageSource = imageSource,
            imageInfo = imageInfo,
            supportRegion = supportRegion,
            exifOrientationHelper = exifOrientationHelper
        )
    }

    override fun toString(): String {
        return "BitmapFactoryDecodeHelper(" +
                "imageSource=$imageSource, " +
                "imageInfo=$imageInfo, " +
                "supportRegion=$supportRegion, " +
                "exifOrientationHelper=$exifOrientationHelper)"
    }

    private fun IntRectCompat.toAndroidRect(): Rect {
        return Rect(left, top, right, bottom)
    }

    class Factory : DecodeHelper.Factory {

        override fun checkSupport(mimeType: String): Boolean? = when (mimeType) {
            "image/jpeg", "image/png", "image/webp" -> true
            "image/gif", "image/bmp", "image/svg+xml" -> false
            "image/heic", "image/heif" -> VERSION.SDK_INT >= VERSION_CODES.O_MR1
            "image/avif" -> if (VERSION.SDK_INT <= 34) false else null
            else -> null
        }

        override fun create(imageSource: ImageSource): BitmapRegionDecoderDecodeHelper {
            val imageInfo = imageSource.decodeImageInfo()
            val exifOrientation = imageSource.decodeExifOrientation()
            val exifOrientationHelper = ExifOrientationHelper(exifOrientation)
            val correctedImageInfo = exifOrientationHelper.applyToImageInfo(imageInfo)
            val supportRegion = checkSupport(imageInfo.mimeType) ?: true
            return BitmapRegionDecoderDecodeHelper(
                imageSource = imageSource,
                imageInfo = correctedImageInfo,
                supportRegion = supportRegion,
                exifOrientationHelper = exifOrientationHelper
            )
        }
    }
}