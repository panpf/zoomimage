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

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.Rect
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.RegionDecoder
import com.github.panpf.zoomimage.subsampling.SubsamplingImage
import com.github.panpf.zoomimage.subsampling.read
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.closeQuietly
import com.github.panpf.zoomimage.util.isAnimatedWebPFile
import com.github.panpf.zoomimage.util.isAvifFile
import com.github.panpf.zoomimage.util.isHeifFile
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import okio.IOException
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
    val imageSource: ImageSource,
    imageInfo: ImageInfo? = null,
) : RegionDecoder {

    private val exifOrientationHelper: ExifOrientationHelper by lazy {
        val exifOrientation = imageSource.decodeExifOrientation()
        ExifOrientationHelper(exifOrientation)
    }
    private var inputStream: BufferedInputStream? = null
    private var bitmapRegionDecoder: BitmapRegionDecoder? = null
    private var _imageInfo: ImageInfo? = imageInfo
    private val imageInfoLock = SynchronizedObject()

    override fun getImageInfo(): ImageInfo {
        val imageInfo = this._imageInfo
        if (imageInfo != null) return imageInfo

        return synchronized(imageInfoLock) {
            val imageInfo2 = this._imageInfo
            if (imageInfo2 != null) return imageInfo2

            // Consistent with the format supported by BitmapRegionDecoder
            val headerBytes =
                imageSource.read(100) ?: throw IOException("Unable to read image header")
            val yes = when {
                isAnimatedWebPFile(headerBytes) -> VERSION.SDK_INT >= 26
                isHeifFile(headerBytes) -> VERSION.SDK_INT >= 27
                isAvifFile(headerBytes) -> VERSION.SDK_INT >= 37
                else -> true
            }
            if (!yes) {
                throw Exception("Unsupported image format")
            }
            val imageInfo = imageSource.decodeImageInfo()
            exifOrientationHelper.applyToImageInfo(imageInfo).apply {
                this@AndroidRegionDecoder._imageInfo = this
            }
        }
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
                inputStream.closeQuietly()
                throw exceptionOrNull()!!
            }
        }.getOrThrow()
    }

    override fun decodeRegion(region: IntRectCompat, sampleSize: Int): Bitmap {
        prepare()
        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
        }
        val imageInfo = getImageInfo()
        val originalRegion = exifOrientationHelper
            .applyToRect(region, imageInfo.size, reverse = true)
        val bitmap = bitmapRegionDecoder!!.decodeRegion(originalRegion.toAndroidRect(), options)
            ?: throw Exception("Invalid image. region decode return null")
        val correctedBitmap = exifOrientationHelper.applyToBitmap(bitmap)
        return correctedBitmap
    }

    override fun close() {
        bitmapRegionDecoder?.recycle()
        inputStream?.closeQuietly()
    }

    override fun copy(): RegionDecoder {
        return AndroidRegionDecoder(
            imageSource = imageSource,
            imageInfo = _imageInfo,
        )
    }

    private fun IntRectCompat.toAndroidRect(): Rect {
        return Rect(left, top, right, bottom)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as AndroidRegionDecoder
        if (imageSource != other.imageSource) return false
        return true
    }

    override fun hashCode(): Int {
        return imageSource.hashCode()
    }

    override fun toString(): String {
        return "AndroidRegionDecoder(imageSource=$imageSource)"
    }

    class Factory : RegionDecoder.Factory {

        override suspend fun accept(subsamplingImage: SubsamplingImage): Boolean {
            val headerBytes = subsamplingImage.headerBytes()
            return when {
                isAnimatedWebPFile(headerBytes) -> VERSION.SDK_INT >= 26
                isHeifFile(headerBytes) -> VERSION.SDK_INT >= 27
                isAvifFile(headerBytes) -> VERSION.SDK_INT >= 37
                else -> true
            }
        }

        override fun checkSupport(mimeType: String): Boolean? {
            if (!mimeType.startsWith("image/")) {
                return false
            }
            return when (mimeType) {
                "image/jpeg", "image/png", "image/webp" -> true
                "image/gif", "image/bmp", "image/svg+xml" -> false
                "image/heic", "image/heif" -> VERSION.SDK_INT >= 27
                "image/avif" -> VERSION.SDK_INT >= 37
                else -> null
            }
        }

        override suspend fun create(
            subsamplingImage: SubsamplingImage,
            imageSource: ImageSource,
        ): AndroidRegionDecoder = AndroidRegionDecoder(imageSource = imageSource)

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