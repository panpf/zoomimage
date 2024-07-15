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
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.TileBitmap
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.util.ioCoroutineDispatcher
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.withContext

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
    private val decoderPool = mutableListOf<DecodeHelper>()
    private val poolSyncLock = SynchronizedObject()

    init {
        decoderPool.add(rootDecodeHelper)
    }

    suspend fun getImageInfo(): ImageInfo {
        return rootDecodeHelper.getImageInfo()
    }

    @WorkerThread
    suspend fun decode(key: String, srcRect: IntRectCompat, sampleSize: Int): TileBitmap? {
        val tileBitmap = useDecoder { decoder ->
            decoder.decodeRegion(key, srcRect, sampleSize)
        } ?: return null
        return tileBitmap
    }

    @WorkerThread
    private suspend fun useDecoder(
        block: suspend (decoder: DecodeHelper) -> TileBitmap?
    ): TileBitmap? = withContext(ioCoroutineDispatcher()) {
        val destroyed = synchronized(poolSyncLock) { destroyed }
        if (!destroyed) {
            var bitmapRegionDecoder: DecodeHelper? = synchronized(poolSyncLock) {
                if (decoderPool.isNotEmpty()) decoderPool.removeAt(0) else null
            }
            if (bitmapRegionDecoder == null) {
                bitmapRegionDecoder = rootDecodeHelper.copy()
            }

            val tileBitmap = block(bitmapRegionDecoder)

            synchronized(poolSyncLock) {
                if (!destroyed) {
                    decoderPool.add(bitmapRegionDecoder)
                } else {
                    bitmapRegionDecoder.close()
                }
            }

            tileBitmap
        } else {
            null
        }
    }

    @MainThread
    suspend fun destroy(caller: String) {
        withContext(ioCoroutineDispatcher()) {
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
    }

    override fun toString(): String {
        return "TileDecoder('${imageSource.key}')"
    }
}