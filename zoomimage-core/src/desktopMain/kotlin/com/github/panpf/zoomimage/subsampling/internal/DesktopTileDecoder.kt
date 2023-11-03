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

import androidx.annotation.WorkerThread
import com.github.panpf.zoomimage.subsampling.DesktopTileBitmap
import com.github.panpf.zoomimage.subsampling.ExifOrientation
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.TileBitmap
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.util.internal.quietClose
import java.awt.Rectangle
import java.awt.image.BufferedImage
import java.io.Closeable
import java.util.LinkedList
import javax.imageio.ImageIO
import javax.imageio.ImageReader

/**
 * Decode the tile bitmap of the image
 *
 * @see [com.github.panpf.zoomimage.core.test.subsampling.internal.DesktopTileDecoderTest]
 */
class DesktopTileDecoder constructor(
    logger: Logger,
    private val imageSource: ImageSource,
    override val imageInfo: ImageInfo,
    override val exifOrientation: ExifOrientation?
) : TileDecoder {

    private val logger = logger.newLogger(module = "TileDecoder")
    private var destroyed = false
    private val decoderPool = LinkedList<ImageReader>()
//    private val addedImageSize = exifOrientation?.addToSize(imageInfo.size) ?: imageInfo.size

    @WorkerThread
    override fun decode(srcRect: IntRectCompat, sampleSize: Int): TileBitmap? {
//        requiredWorkThread()
        if (destroyed) return null
        val bitmap = useDecoder { decoder ->
            decodeRegion(decoder, srcRect, sampleSize)
        } ?: return null
        return applyExifOrientation(DesktopTileBitmap(bitmap))
    }

    override fun destroy(caller: String) {
//        requiredMainThread()
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
        imageReader: ImageReader,
        srcRect: IntRectCompat,
        inSampleSize: Int
    ): BufferedImage? {
//        requiredWorkThread()
        val imageSize = imageInfo.size
        val newSrcRect = exifOrientation?.applyToRect(srcRect, imageSize, reverse = true) ?: srcRect
        val readParam = imageReader.defaultReadParam.apply {
            sourceRegion = Rectangle(
                /* x = */ newSrcRect.left,
                /* y = */ newSrcRect.top,
                /* width = */ newSrcRect.width,
                /* height = */ newSrcRect.height
            )
            setSourceSubsampling(inSampleSize, inSampleSize, 0, 0)
        }
        return imageReader.read(0, readParam)
    }

    @WorkerThread
    private fun useDecoder(block: (decoder: ImageReader) -> BufferedImage?): BufferedImage? {
        synchronized(decoderPool) {
            if (destroyed) {
                return null
            }
        }

        var imageReader: ImageReader? = synchronized(decoderPool) {
            decoderPool.poll()
        }
        if (imageReader == null) {
            val inputStream = imageSource.openInputStream().getOrNull()?.buffered()
            val imageStream = ImageIO.createImageInputStream(inputStream)
            imageReader = ImageIO.getImageReaders(imageStream).next().apply {
                input = imageStream
            }
        }
        if (imageReader == null) {
            return null
        }

        val bufferedImage = block(imageReader)

        synchronized(decoderPool) {
            if (destroyed) {
                imageReader.recycle()
            } else {
                decoderPool.add(imageReader)
            }
        }

        return bufferedImage
    }

    @WorkerThread
    private fun applyExifOrientation(tileBitmap: TileBitmap): TileBitmap {
//        requiredWorkThread()
        val newBitmap = exifOrientation
            ?.applyToTileBitmap(tileBitmap, reverse = false, null)
            ?: tileBitmap
        return if (newBitmap !== tileBitmap) {
//            if (tileBitmapReuseHelper != null) {
//                tileBitmapReuseHelper.freeTileBitmap(tileBitmap, "applyExifOrientation")
//            } else {
            tileBitmap.recycle()
//            }
            newBitmap
        } else {
            tileBitmap
        }
    }

    private fun ImageReader.recycle() {
        dispose()
        (input as Closeable).quietClose()
    }

    override fun toString(): String {
        return "DesktopTileDecoder(imageSource='${imageSource.key}', imageInfo=$imageInfo, exifOrientation=$exifOrientation)"
    }
}