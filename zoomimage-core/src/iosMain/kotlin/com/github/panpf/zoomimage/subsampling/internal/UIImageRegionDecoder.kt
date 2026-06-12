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

import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.RegionDecoder
import com.github.panpf.zoomimage.subsampling.SubsamplingImage
import com.github.panpf.zoomimage.subsampling.toByteArray
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.correctExifOrientation
import com.github.panpf.zoomimage.util.intSizeCompat
import com.github.panpf.zoomimage.util.isAvifFile
import com.github.panpf.zoomimage.util.isBmpFile
import com.github.panpf.zoomimage.util.isGifFile
import com.github.panpf.zoomimage.util.isHeifFile
import com.github.panpf.zoomimage.util.isJpegFile
import com.github.panpf.zoomimage.util.isPngFile
import com.github.panpf.zoomimage.util.isStaticsAvifFile
import com.github.panpf.zoomimage.util.isStaticsHeifFile
import com.github.panpf.zoomimage.util.isVersionAtLeast
import com.github.panpf.zoomimage.util.isWebPFile
import com.github.panpf.zoomimage.util.toBitmap
import com.github.panpf.zoomimage.util.toNSData
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Image
import platform.UIKit.UIImage

/**
 * Use [Image] to decode the image
 *
 * *Not thread safe*
 *
 * @see com.github.panpf.zoomimage.core.ios.test.subsampling.internal.UIImageRegionDecoderTest
 */
class UIImageRegionDecoder(
    val imageSource: ImageSource,
    val mimeType: String,
    imageInfo: ImageInfo? = null,
    bytes: ByteArray? = null,
) : RegionDecoder {

    private val data: ByteArray by lazy { bytes ?: imageSource.toByteArray() }
    private val uiImage: UIImage by lazy {
        val uiImage = UIImage.imageWithData(data.toNSData())
            ?.correctExifOrientation()
        requireNotNull(uiImage) { "Failed to decode image" }
    }
    private var _imageInfo: ImageInfo? = imageInfo
    private val imageInfoLock = SynchronizedObject()

    override fun getImageInfo(): ImageInfo {
        val imageInfo = this._imageInfo
        if (imageInfo != null) return imageInfo

        return synchronized(imageInfoLock) {
            val imageInfo2 = this._imageInfo
            if (imageInfo2 != null) return imageInfo2

            val size = uiImage.intSizeCompat()
            ImageInfo(size, mimeType = mimeType).apply {
                this@UIImageRegionDecoder._imageInfo = this
            }
        }
    }

    override fun prepare() {

    }

    override fun decodeRegion(region: IntRectCompat, sampleSize: Int): Bitmap {
        val bitmap = uiImage.toBitmap(sampleSize = sampleSize, region = region)
        return bitmap
    }

    override fun close() {
    }

    override fun copy(): RegionDecoder {
        return UIImageRegionDecoder(
            imageSource = imageSource,
            mimeType = mimeType,
            imageInfo = _imageInfo,
            bytes = data,
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as UIImageRegionDecoder
        if (imageSource != other.imageSource) return false
        if (mimeType != other.mimeType) return false
        return true
    }

    override fun hashCode(): Int {
        var result = imageSource.hashCode()
        result = 31 * result + mimeType.hashCode()
        return result
    }

    override fun toString(): String {
        return "UIImageRegionDecoder(imageSource=$imageSource, mimeType='$mimeType')"
    }

    class Factory : RegionDecoder.Factory {

        override suspend fun accept(subsamplingImage: SubsamplingImage): Boolean = true

        override fun checkSupport(mimeType: String): Boolean? {
            if (!mimeType.startsWith("image/")) {
                return false
            }
            return when (mimeType) {
                "image/jpeg", "image/png", "image/webp", "image/bmp", "image/gif" -> true
                "image/heic", "image/heif" -> isVersionAtLeast(11)
                "image/avif" -> isVersionAtLeast(16)
                "image/svg+xml" -> false
                else -> null
            }
        }

        private suspend fun isApplicable(subsamplingImage: SubsamplingImage): String {
            if (isJpegFile(subsamplingImage.headerBytes())) {
                return "image/jpeg"
            }
            if (isPngFile(subsamplingImage.headerBytes())) {
                return "image/png"
            }
            if (isWebPFile(subsamplingImage.headerBytes())) {
                return "image/webp"
            }
            if (isBmpFile(subsamplingImage.headerBytes())) {
                return "image/bmp"
            }
            if (isGifFile(subsamplingImage.headerBytes())) {
                return "image/gif"
            }
            if (isVersionAtLeast(11) && isHeifFile(subsamplingImage.headerBytes())) {
                return "image/heif"
            }
            if (isVersionAtLeast(16) && isAvifFile(subsamplingImage.headerBytes())) {
                return "image/avif"
            }
            return subsamplingImage.imageInfo?.mimeType ?: "image/*"
        }

        override suspend fun create(
            subsamplingImage: SubsamplingImage,
            imageSource: ImageSource,
        ): UIImageRegionDecoder {
            val mimeType = isApplicable(subsamplingImage)
            return UIImageRegionDecoder(
                imageSource = imageSource,
                mimeType = mimeType,
            )
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            return other != null && this::class == other::class
        }

        override fun hashCode(): Int {
            return this::class.hashCode()
        }

        override fun toString(): String {
            return "UIImageRegionDecoder"
        }
    }

    class SupplementSkiaFactory : RegionDecoder.Factory {

        override suspend fun accept(subsamplingImage: SubsamplingImage): Boolean =
            isApplicable(subsamplingImage) != null

        override fun checkSupport(mimeType: String): Boolean? {
            if (!mimeType.startsWith("image/")) {
                return false
            }
            return when (mimeType) {
                "image/heic", "image/heif", "image/avif" -> true
                else -> false
            }
        }

        private suspend fun isApplicable(subsamplingImage: SubsamplingImage): String? {
            if (isVersionAtLeast(11) && isStaticsHeifFile(subsamplingImage.headerBytes())) {
                return "image/heif"
            }
            if (isVersionAtLeast(16) && isStaticsAvifFile(subsamplingImage.headerBytes())) {
                return "image/avif"
            }
            return null
        }

        override suspend fun create(
            subsamplingImage: SubsamplingImage,
            imageSource: ImageSource,
        ): UIImageRegionDecoder {
            val mimeType = isApplicable(subsamplingImage)
                ?: throw IllegalArgumentException("Unsupported image format for subsampling: $subsamplingImage")
            return UIImageRegionDecoder(
                imageSource = imageSource,
                mimeType = mimeType,
            )
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            return other != null && this::class == other::class
        }

        override fun hashCode(): Int {
            return this::class.hashCode()
        }

        override fun toString(): String {
            return "SupplementSkiaUIImageRegionDecoder"
        }
    }
}