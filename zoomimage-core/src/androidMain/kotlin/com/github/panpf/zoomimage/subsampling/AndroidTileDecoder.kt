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

package com.github.panpf.zoomimage.subsampling

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.Rect
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import com.github.panpf.zoomimage.subsampling.internal.isInBitmapError
import com.github.panpf.zoomimage.subsampling.internal.isSrcRectError
import com.github.panpf.zoomimage.subsampling.internal.requiredMainThread
import com.github.panpf.zoomimage.subsampling.internal.requiredWorkThread
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.util.toShortString
import java.util.LinkedList

/**
 * Decode the tile bitmap of the image
 *
 * @see [com.github.panpf.zoomimage.core.test.subsampling.AndroidTileDecoderTest]
 */
class AndroidTileDecoder constructor(
    logger: Logger,
    private val imageSource: ImageSource,
    override val imageInfo: ImageInfo,
    override val exifOrientation: ExifOrientation?,
    private val tileBitmapReuseHelper: AndroidTileBitmapReuseHelper?,
) : TileDecoder {

    private val logger = logger.newLogger(module = "TileDecoder")
    private var destroyed = false
    private val decoderPool = LinkedList<BitmapRegionDecoder>()
    private val addedImageSize =
        exifOrientation?.applyToSize(imageInfo.size, reverse = true) ?: imageInfo.size

    @WorkerThread
    override fun decode(srcRect: IntRectCompat, sampleSize: Int): TileBitmap? {
        requiredWorkThread()
        if (destroyed) return null
        val bitmap = useDecoder { decoder ->
            decodeRegion(decoder, srcRect, sampleSize)
        } ?: return null
        return applyExifOrientation(DefaultAndroidTileBitmap(bitmap))
    }

    @MainThread
    override fun destroy(caller: String) {
        requiredMainThread()
        if (destroyed) return
        destroyed = true
        logger.d { "destroy:$caller. '${imageSource.key}'" }
        synchronized(decoderPool) {
            decoderPool.forEach {
                it.recycle()
            }
            decoderPool.clear()
        }
    }

    @WorkerThread
    private fun decodeRegion(
        regionDecoder: BitmapRegionDecoder,
        srcRect: IntRectCompat,
        inSampleSize: Int
    ): Bitmap? {
        requiredWorkThread()
        val imageSize = imageInfo.size
        val newSrcRect = exifOrientation?.applyToRect(srcRect, imageSize, reverse = true) ?: srcRect
        val decodeOptions = BitmapFactory.Options().apply {
            this.inSampleSize = inSampleSize
        }
        tileBitmapReuseHelper?.setInBitmapForRegion(
            options = decodeOptions,
            regionSize = IntSizeCompat(newSrcRect.width, newSrcRect.height),
            imageMimeType = imageInfo.mimeType,
            imageSize = addedImageSize,
            caller = "decodeRegion"
        )

        return try {
            regionDecoder.decodeRegion(newSrcRect.toAndroidRect(), decodeOptions)
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            val inBitmap = decodeOptions.inBitmap
            if (inBitmap != null && isInBitmapError(throwable)) {
                logger.e("decodeRegion. Bitmap region decode inBitmap error. '${imageSource.key}'")

                if (tileBitmapReuseHelper != null) {
                    tileBitmapReuseHelper.freeBitmap(inBitmap, "decodeRegion:error")
                } else {
                    inBitmap.recycle()
                }

                decodeOptions.inBitmap = null
                try {
                    regionDecoder.decodeRegion(newSrcRect.toAndroidRect(), decodeOptions)
                } catch (throwable1: Throwable) {
                    throwable1.printStackTrace()
                    logger.e(throwable) {
                        "decodeRegion. Bitmap region decode error. srcRect=${newSrcRect}. '${imageSource.key}'"
                    }
                    null
                }
            } else if (isSrcRectError(throwable)) {
                logger.e(throwable) {
                    "decodeRegion. Bitmap region decode srcRect error. " +
                            "imageSize=${imageSize.toShortString()}, " +
                            "srcRect=${newSrcRect.toShortString()}, " +
                            "inSampleSize=${decodeOptions.inSampleSize}. " +
                            "'${imageSource.key}'"
                }
                null
            } else {
                null
            }
        }
    }

    @WorkerThread
    private fun useDecoder(block: (decoder: BitmapRegionDecoder) -> Bitmap?): Bitmap? {
        requiredWorkThread()
        synchronized(decoderPool) {
            if (destroyed) {
                return null
            }
        }

        var bitmapRegionDecoder: BitmapRegionDecoder? = synchronized(decoderPool) {
            decoderPool.poll()
        }
        if (bitmapRegionDecoder == null) {
            bitmapRegionDecoder = imageSource.openInputStream().getOrNull()?.buffered()?.use {
                if (VERSION.SDK_INT >= VERSION_CODES.S) {
                    BitmapRegionDecoder.newInstance(it)
                } else {
                    @Suppress("DEPRECATION")
                    BitmapRegionDecoder.newInstance(it, false)
                }
            }
        }
        if (bitmapRegionDecoder == null) {
            return null
        }

        val bitmap = block(bitmapRegionDecoder)

        synchronized(decoderPool) {
            if (destroyed) {
                bitmapRegionDecoder.recycle()
            } else {
                decoderPool.add(bitmapRegionDecoder)
            }
        }

        return bitmap
    }

    @WorkerThread
    private fun applyExifOrientation(tileBitmap: TileBitmap): TileBitmap {
        requiredWorkThread()
        val newBitmap = exifOrientation
            ?.applyToTileBitmap(tileBitmap, reverse = false, tileBitmapReuseHelper)
            ?: tileBitmap
        return if (newBitmap !== tileBitmap) {
            if (tileBitmapReuseHelper != null) {
                tileBitmapReuseHelper.freeTileBitmap(tileBitmap, "applyExifOrientation")
            } else {
                tileBitmap.recycle()
            }
            newBitmap
        } else {
            tileBitmap
        }
    }

    private fun IntRectCompat.toAndroidRect(): Rect {
        return Rect(left, top, right, bottom)
    }

    override fun toString(): String {
        return "AndroidTileDecoder(imageSource='${imageSource.key}', imageInfo=$imageInfo, exifOrientation=$exifOrientation)"
    }
}