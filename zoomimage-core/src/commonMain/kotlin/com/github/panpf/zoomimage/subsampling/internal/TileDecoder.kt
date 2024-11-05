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

import com.github.panpf.zoomimage.annotation.WorkerThread
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.RegionDecoder
import com.github.panpf.zoomimage.subsampling.TileImage
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.Logger
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized

/**
 * Decode the tile bitmap of the image
 *
 * @see com.github.panpf.zoomimage.core.desktop.test.subsampling.internal.TileDecoderTest
 */
class TileDecoder(
    val logger: Logger,
    private val regionDecoder: RegionDecoder,
) : AutoCloseable {

    private var closed = false
    private val decoderPool = mutableListOf<RegionDecoder>()
    private val poolSyncLock = SynchronizedObject()
    private var regionDecoderCount = 0

    val decoderPoolSize: Int
        get() = decoderPool.size

    val imageInfo: ImageInfo = regionDecoder.imageInfo

    init {
        decoderPool.add(regionDecoder)
        regionDecoderCount++
        logger.d { "TileDecoder. useDecoder. regionDecoderCount=${regionDecoderCount}. ${this.regionDecoder}" }
    }

    @WorkerThread
    fun decode(key: String, srcRect: IntRectCompat, sampleSize: Int): TileImage? {
        val closed = synchronized(poolSyncLock) { closed }
        check(!closed) { "TileDecoder is closed. $regionDecoder" }
        return useDecoder { decoder -> decoder.decodeRegion(key, srcRect, sampleSize) }
    }

    @WorkerThread
    private fun useDecoder(
        block: (decoder: RegionDecoder) -> TileImage?
    ): TileImage? {
        var regionDecoder: RegionDecoder? = synchronized(poolSyncLock) {
            if (decoderPool.isNotEmpty()) decoderPool.removeAt(0) else null
        }
        if (regionDecoder == null) {
            regionDecoderCount++
            logger.d { "TileDecoder. useDecoder. regionDecoderCount=${regionDecoderCount}. ${this.regionDecoder}" }
            regionDecoder = this.regionDecoder.copy()
        }

        val tileImage = block(regionDecoder)

        synchronized(poolSyncLock) {
            if (!closed) {
                decoderPool.add(regionDecoder)
            } else {
                regionDecoder.close()
            }
        }

        return tileImage
    }

    @WorkerThread
    override fun close() {
        val closed = synchronized(poolSyncLock) { this@TileDecoder.closed }
        if (!closed) {
            this@TileDecoder.closed = true
            logger.d { "TileDecoder. close. $regionDecoder" }
            synchronized(poolSyncLock) {
                decoderPool.forEach { it.close() }
                decoderPool.clear()
            }
        }
    }

    override fun toString(): String {
        return "TileDecoder($regionDecoder)"
    }
}