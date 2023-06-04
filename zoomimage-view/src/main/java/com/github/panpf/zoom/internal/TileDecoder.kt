/*
 * Copyright (C) 2022 panpf <panpfpanpf@outlook.com>
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
package com.github.panpf.zoom.internal

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.Rect
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import com.github.panpf.zoom.ImageSource
import com.github.panpf.zoom.Size
import com.github.panpf.zoom.freeBitmap
import com.github.panpf.zoom.setInBitmapForRegion
import kotlinx.coroutines.runBlocking
import java.util.LinkedList

internal class TileDecoder internal constructor(
    private val engine: SubsamplingEngine,
    private val imageSource: ImageSource,
    val imageSize: Size,
    val imageMimeType: String,
    val imageExifOrientation: Int,
) {
    private val decoderPool = LinkedList<BitmapRegionDecoder>()
    private val exifOrientationHelper: ExifOrientationHelper =
        ExifOrientationHelper(imageExifOrientation)
    private var _destroyed: Boolean = false
    private val addedImageSize: Size by lazy {
        exifOrientationHelper.addToSize(imageSize)
    }

    val destroyed: Boolean
        get() = _destroyed

    @WorkerThread
    fun decode(tile: Tile): Bitmap? {
        requiredWorkThread()

        if (_destroyed) return null
        return useDecoder { decoder ->
            decodeRegion(decoder, tile.srcRect, tile.inSampleSize)?.let {
                applyExifOrientation(it)
            }
        }
    }

    @WorkerThread
    private fun decodeRegion(
        regionDecoder: BitmapRegionDecoder,
        srcRect: Rect,
        inSampleSize: Int
    ): Bitmap? {
        requiredWorkThread()

        val imageSize = imageSize
        val newSrcRect = exifOrientationHelper.addToRect(srcRect, imageSize)
        val decodeOptions = BitmapFactory.Options().apply {
            this.inSampleSize = inSampleSize
        }
        val tinyBitmapPool = engine.tinyBitmapPool
        tinyBitmapPool?.setInBitmapForRegion(
            logger = engine.logger,
            options = decodeOptions,
            regionSize = Size(newSrcRect.width(), newSrcRect.height()),
            imageMimeType = imageMimeType,
            imageSize = addedImageSize,
            disallowReuseBitmap = engine.disallowReuseBitmap,
            caller = "tile:decodeRegion"
        )
        engine.logger.d(SubsamplingEngine.MODULE) {
            "decodeRegion. inBitmap=${decodeOptions.inBitmap?.logString}. '${imageSource.key}'"
        }

        return try {
            regionDecoder.decodeRegion(newSrcRect, decodeOptions)
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            val inBitmap = decodeOptions.inBitmap
            if (inBitmap != null && isInBitmapError(throwable)) {
                engine.logger.e(SubsamplingEngine.MODULE, throwable) {
                    "decodeRegion. Bitmap region decode inBitmap error. '${imageSource.key}'"
                }

                if (tinyBitmapPool != null) {
                    tinyBitmapPool.freeBitmap(
                        logger = engine.logger,
                        bitmap = inBitmap,
                        disallowReuseBitmap = engine.disallowReuseBitmap,
                        caller = "tile:decodeRegion:error"
                    )
                } else {
                    inBitmap.recycle()
                }
                engine.logger.d(SubsamplingEngine.MODULE) {
                    "decodeRegion. freeBitmap. inBitmap error. bitmap=${inBitmap.logString}. '${imageSource.key}'"
                }

                decodeOptions.inBitmap = null
                try {
                    regionDecoder.decodeRegion(newSrcRect, decodeOptions)
                } catch (throwable1: Throwable) {
                    throwable1.printStackTrace()
                    engine.logger.e(SubsamplingEngine.MODULE, throwable) {
                        "decodeRegion. Bitmap region decode error. srcRect=${newSrcRect}. '${imageSource.key}'"
                    }
                    null
                }
            } else if (isSrcRectError(throwable)) {
                engine.logger.e(SubsamplingEngine.MODULE, throwable) {
                    "decodeRegion. Bitmap region decode srcRect error. imageSize=$imageSize, srcRect=$newSrcRect, inSampleSize=${decodeOptions.inSampleSize}. '${imageSource.key}'"
                }
                null
            } else {
                null
            }
        }
    }

    @WorkerThread
    private fun applyExifOrientation(bitmap: Bitmap): Bitmap {
        requiredWorkThread()

        val newBitmap = exifOrientationHelper.applyToBitmap(
            logger = engine.logger,
            inBitmap = bitmap,
            bitmapPool = engine.tinyBitmapPool,
            disallowReuseBitmap = engine.disallowReuseBitmap
        )
        return if (newBitmap != null && newBitmap != bitmap) {
            val tinyBitmapPool = engine.tinyBitmapPool
            if (tinyBitmapPool != null) {
                tinyBitmapPool.freeBitmap(
                    logger = engine.logger,
                    bitmap = bitmap,
                    disallowReuseBitmap = engine.disallowReuseBitmap,
                    caller = "tile:applyExifOrientation"
                )
            } else {
                bitmap.recycle()
            }
            engine.logger.d(SubsamplingEngine.MODULE) {
                "applyExifOrientation. freeBitmap. bitmap=${bitmap.logString}. '${imageSource.key}'"
            }
            newBitmap
        } else {
            bitmap
        }
    }

    @MainThread
    fun destroy() {
        requiredMainThread()

        synchronized(decoderPool) {
            _destroyed = true
            decoderPool.forEach {
                it.recycle()
            }
            decoderPool.clear()
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
            bitmapRegionDecoder = runBlocking {
                imageSource.openInputStream()
            }.getOrNull()?.buffered()?.use {
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
}