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
import com.github.panpf.zoomimage.subsampling.RegionDecoder
import com.github.panpf.zoomimage.subsampling.SubsamplingImage
import com.github.panpf.zoomimage.util.IntRectCompat
import okio.buffer
import java.io.BufferedInputStream

/**
 * Use [BitmapRegionDecoder] to decode the image
 *
 * *Not thread safe*
 *
 * @see com.github.panpf.zoomimage.core.android.test.subsampling.internal.AndroidRegionDecoderTest
 */
class AndroidRegionDecoder(
    override val subsamplingImage: SubsamplingImage,
    val imageSource: ImageSource,
    imageInfo: ImageInfo? = subsamplingImage.imageInfo,
) : RegionDecoder {

    private val exifOrientationHelper: ExifOrientationHelper by lazy {
        val exifOrientation = imageSource.decodeExifOrientation()
        ExifOrientationHelper(exifOrientation)
    }
    private var inputStream: BufferedInputStream? = null
    private var bitmapRegionDecoder: BitmapRegionDecoder? = null

    override val imageInfo: ImageInfo by lazy { imageInfo ?: decodeImageInfo() }

    private fun decodeImageInfo(): ImageInfo {
        val imageInfo = imageSource.decodeImageInfo()
        val correctedImageInfo = exifOrientationHelper.applyToImageInfo(imageInfo)
        return correctedImageInfo
    }

    override fun prepare() {
        if (inputStream != null && bitmapRegionDecoder != null) return

        val inputStream = imageSource.openSource().buffer().inputStream().buffered().apply {
            this@AndroidRegionDecoder.inputStream = this
        }
        bitmapRegionDecoder = kotlin.runCatching {
            if (VERSION.SDK_INT >= VERSION_CODES.S) {
                BitmapRegionDecoder.newInstance(inputStream)!!
            } else {
                @Suppress("DEPRECATION")
                BitmapRegionDecoder.newInstance(inputStream, false)!!
            }
        }.apply {
            if (isFailure) {
                inputStream.close()
                throw exceptionOrNull()!!
            }
        }.getOrThrow()
    }

    override fun decodeRegion(
        key: String,
        region: IntRectCompat,
        sampleSize: Int
    ): BitmapTileImage {
        prepare()
        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
        }
        val originalRegion = exifOrientationHelper
            .applyToRect(region, imageInfo.size, reverse = true)
        val bitmap = bitmapRegionDecoder!!.decodeRegion(originalRegion.toAndroidRect(), options)
            ?: throw Exception("Invalid image. region decode return null")
        val tileImage = BitmapTileImage(bitmap, key, fromCache = false)
        val correctedImage = exifOrientationHelper.applyToTileImage(tileImage)
        return correctedImage
    }

    override fun close() {
        bitmapRegionDecoder?.recycle()
        inputStream?.close()
    }

    override fun copy(): RegionDecoder {
        return AndroidRegionDecoder(
            subsamplingImage = subsamplingImage,
            imageSource = imageSource,
            imageInfo = imageInfo,
        )
    }

    private fun IntRectCompat.toAndroidRect(): Rect {
        return Rect(left, top, right, bottom)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as AndroidRegionDecoder
        if (subsamplingImage != other.subsamplingImage) return false
        if (imageSource != other.imageSource) return false
        return true
    }

    override fun hashCode(): Int {
        var result = subsamplingImage.hashCode()
        result = 31 * result + imageSource.hashCode()
        return result
    }

    override fun toString(): String {
        return "AndroidRegionDecoder(subsamplingImage=$subsamplingImage, imageSource=$imageSource)"
    }

    class Factory : RegionDecoder.Factory {

        override suspend fun accept(subsamplingImage: SubsamplingImage): Boolean = true

        override fun checkSupport(mimeType: String): Boolean? {
            if (!mimeType.startsWith("image/")) {
                return false
            }
            return when (mimeType) {
                "image/jpeg", "image/png", "image/webp" -> true
                "image/gif", "image/bmp", "image/svg+xml" -> false
                "image/heic", "image/heif" -> VERSION.SDK_INT >= VERSION_CODES.O_MR1
                "image/avif" -> if (VERSION.SDK_INT <= 35) false else null
                else -> null
            }
        }

        override fun create(
            subsamplingImage: SubsamplingImage,
            imageSource: ImageSource,
        ): AndroidRegionDecoder = AndroidRegionDecoder(
            subsamplingImage = subsamplingImage,
            imageSource = imageSource,
        )

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            return other != null && this::class == other::class
        }

        override fun hashCode(): Int {
            return this::class.hashCode()
        }

        override fun toString(): String {
            return "AndroidRegionDecoder"
        }
    }
}