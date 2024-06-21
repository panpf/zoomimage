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

import com.github.panpf.zoomimage.annotation.MainThread
import com.github.panpf.zoomimage.annotation.WorkerThread
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.TileBitmap
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.Logger
import java.util.LinkedList

/**
 * Decode the tile bitmap of the image
 *
 * @see [com.github.panpf.zoomimage.core.test.subsampling.internal.TileDecoderTest]
 */
class TileDecoder constructor(
    private val logger: Logger,
    private val imageSource: ImageSource,
    private val rootDecodeHelper: DecodeHelper,
) {

    private var destroyed = false
    private val decoderPool = LinkedList<DecodeHelper>()

    val imageInfo by lazy { rootDecodeHelper.imageInfo }

    init {
        decoderPool.push(rootDecodeHelper)
    }

    @WorkerThread
    fun decode(srcRect: IntRectCompat, sampleSize: Int): TileBitmap? {
        if (destroyed) return null
        val tileBitmap = useDecoder { decoder ->
            decoder.decodeRegion(srcRect, sampleSize)
        } ?: return null
        return tileBitmap
    }

    @WorkerThread
    private fun useDecoder(block: (decoder: DecodeHelper) -> TileBitmap?): TileBitmap? {
        synchronized(decoderPool) {
            if (destroyed) {
                return null
            }
        }

        var bitmapRegionDecoder: DecodeHelper? = synchronized(decoderPool) {
            decoderPool.poll()
        }
        if (bitmapRegionDecoder == null) {
            bitmapRegionDecoder = rootDecodeHelper.copy()
        }

        val tileBitmap = block(bitmapRegionDecoder)

        synchronized(decoderPool) {
            if (destroyed) {
                bitmapRegionDecoder.close()
            } else {
                decoderPool.add(bitmapRegionDecoder)
            }
        }

        return tileBitmap
    }

    @MainThread
    fun destroy(caller: String) {
        if (destroyed) return
        destroyed = true
        synchronized(decoderPool) {
            decoderPool.forEach { it.close() }
            decoderPool.clear()
        }
        logger.d { "destroyDecoder:$caller. '${imageSource.key}'" }
    }

    override fun toString(): String {
        return "TileDecoder('${imageSource.key}')"
    }
}