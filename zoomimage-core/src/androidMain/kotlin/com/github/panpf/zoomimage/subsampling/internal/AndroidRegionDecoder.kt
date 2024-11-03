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
import com.github.panpf.zoomimage.subsampling.SubsamplingImage
import com.github.panpf.zoomimage.util.IntRectCompat
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
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
    override val imageSource: ImageSource,
    override val imageInfo: ImageInfo,
    val bitmapRegionDecoder: BitmapRegionDecoder,
    val inputStream: BufferedInputStream,
    val exifOrientationHelper: ExifOrientationHelper,
) : RegionDecoder {

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
        val bitmap = bitmapRegionDecoder.decodeRegion(originalRegion.toAndroidRect(), options)
            ?: throw Exception("Invalid image. region decode return null")
        val tileImage = BitmapTileImage(bitmap, key, fromCache = false)
        val correctedImage = exifOrientationHelper.applyToTileImage(tileImage)
        return correctedImage
    }

    override fun close() {
        bitmapRegionDecoder.recycle()
        inputStream.close()
    }

    override fun copy(): RegionDecoder {
        val inputStream = imageSource.openSource().buffer().inputStream().buffered()
        val bitmapRegionDecoder = runCatching {
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
        return AndroidRegionDecoder(
            imageSource = imageSource,
            imageInfo = imageInfo,
            exifOrientationHelper = exifOrientationHelper,
            inputStream = inputStream,
            bitmapRegionDecoder = bitmapRegionDecoder,
        )
    }

    override fun toString(): String {
        return "BitmapFactoryDecodeHelper(" +
                "imageSource=$imageSource, " +
                "imageInfo=$imageInfo, " +
                "exifOrientationHelper=$exifOrientationHelper)"
    }

    private fun IntRectCompat.toAndroidRect(): Rect {
        return Rect(left, top, right, bottom)
    }

    class Matcher : RegionDecoder.Matcher {
        override suspend fun accept(subsamplingImage: SubsamplingImage): Factory {
            return Factory()
        }
    }

    class Factory : RegionDecoder.Factory {

        private var _exifOrientation: ExifOrientationHelper? = null
        private val exifOrientationSynchronizedObject = SynchronizedObject()

        private fun getOrCreateExifOrientationHelper(imageSource: ImageSource): ExifOrientationHelper {
            return synchronized(exifOrientationSynchronizedObject) {
                @Suppress("ComplexRedundantLet")
                _exifOrientation
                    ?: imageSource
                        .decodeExifOrientation()
                        .let { ExifOrientationHelper(it) }
                        .apply { this@Factory._exifOrientation = this }
            }
        }

        override suspend fun decodeImageInfo(imageSource: ImageSource): ImageInfo {
            val exifOrientationHelper = getOrCreateExifOrientationHelper(imageSource)
            val imageInfo = imageSource.decodeImageInfo()
            val correctedImageInfo = exifOrientationHelper.applyToImageInfo(imageInfo)
            return correctedImageInfo
        }

        override fun checkSupport(mimeType: String): Boolean? = when (mimeType) {
            "image/jpeg", "image/png", "image/webp" -> true
            "image/gif", "image/bmp", "image/svg+xml" -> false
            "image/heic", "image/heif" -> VERSION.SDK_INT >= VERSION_CODES.O_MR1
            "image/avif" -> if (VERSION.SDK_INT <= 34) false else null
            else -> null
        }

        override suspend fun create(
            imageSource: ImageSource,
            imageInfo: ImageInfo
        ): AndroidRegionDecoder {
            val exifOrientationHelper = getOrCreateExifOrientationHelper(imageSource)
            val inputStream = imageSource.openSource().buffer().inputStream().buffered()
            val bitmapRegionDecoder = kotlin.runCatching {
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
            return AndroidRegionDecoder(
                imageSource = imageSource,
                imageInfo = imageInfo,
                exifOrientationHelper = exifOrientationHelper,
                inputStream = inputStream,
                bitmapRegionDecoder = bitmapRegionDecoder,
            )
        }

        override fun close() {

        }
    }
}