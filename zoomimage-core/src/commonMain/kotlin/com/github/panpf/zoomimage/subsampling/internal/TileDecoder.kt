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

import com.github.panpf.zoomimage.annotation.WorkerThread
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.TileBitmap
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.Logger
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized

/**
 * Decode the tile bitmap of the image
 *
 * @see com.github.panpf.zoomimage.core.common.test.subsampling.internal.TileDecoderTest
 */
class TileDecoder constructor(
    val logger: Logger,
    val imageSource: ImageSource,
    val rootDecodeHelper: DecodeHelper,
) {

    private var destroyed = false
    private val decoderPool = mutableListOf<DecodeHelper>()
    private val poolSyncLock = SynchronizedObject()

    init {
        logger.d { "TileDecoder. new DecodeHelper. initialization. '${imageSource.key}'" }
        decoderPool.add(rootDecodeHelper)
    }

    fun getImageInfo(): ImageInfo = rootDecodeHelper.imageInfo

    @WorkerThread
    fun decode(key: String, srcRect: IntRectCompat, sampleSize: Int): TileBitmap? {
        return useDecoder { decoder -> decoder.decodeRegion(key, srcRect, sampleSize) }
    }

    @WorkerThread
    private fun useDecoder(
        block: (decoder: DecodeHelper) -> TileBitmap?
    ): TileBitmap? {
        val destroyed = synchronized(poolSyncLock) { destroyed }
        if (destroyed) return null

        var decodeHelper: DecodeHelper? = synchronized(poolSyncLock) {
            if (decoderPool.isNotEmpty()) decoderPool.removeAt(0) else null
        }
        if (decodeHelper == null) {
            logger.d { "TileDecoder. new DecodeHelper. decode. '${imageSource.key}'" }
            decodeHelper = rootDecodeHelper.copy()
        }

        val tileBitmap = block(decodeHelper)

        synchronized(poolSyncLock) {
            if (!destroyed) {
                decoderPool.add(decodeHelper)
            } else {
                decodeHelper.close()
            }
        }

        return tileBitmap
    }

    @WorkerThread
    fun destroy(caller: String) {
        val destroyed = synchronized(poolSyncLock) { this@TileDecoder.destroyed }
        if (!destroyed) {
            this@TileDecoder.destroyed = true
            logger.d { "TileDecoder. destroyDecoder:$caller. '${imageSource.key}'" }
            synchronized(poolSyncLock) {
                decoderPool.forEach { it.close() }
                decoderPool.clear()
            }
        }
    }

    override fun toString(): String {
        return "TileDecoder('${imageSource.key}')"
    }
}