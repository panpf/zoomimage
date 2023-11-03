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

import com.github.panpf.zoomimage.subsampling.CacheTileBitmap
import com.github.panpf.zoomimage.subsampling.TileBitmap
import com.github.panpf.zoomimage.subsampling.TileState
import com.github.panpf.zoomimage.util.IntOffsetCompat
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.toShortString
import kotlinx.coroutines.Job

/**
 * A tile of the image, store the region, sample multiplier, Bitmap, load status, and other information of the tile
 *
 * @see [com.github.panpf.zoomimage.core.test.subsampling.internal.TileTest]
 */
class Tile constructor(
    /**
     * Horizontal and vertical coordinates
     */
    val coordinate: IntOffsetCompat,

    /**
     * The region of Tile in the original image
     */
    val srcRect: IntRectCompat,

    /**
     * The sampling multiplier at load
     */
    val sampleSize: Int
) {

    internal var loadJob: Job? = null

    /**
     * The bitmap of the tile
     */
    var bitmap: TileBitmap? = null

    /**
     * The state of the tile
     */
    @TileState
    var state: Int = TileState.STATE_NONE

    val animationState = AnimationState()

    fun setTileBitmap(tileBitmap: TileBitmap?, fromCache: Boolean) {
        val oldTileBitmap = this.bitmap
        if (tileBitmap == oldTileBitmap) return
        if (oldTileBitmap is CacheTileBitmap) {
            oldTileBitmap.setIsDisplayed(false)
        }
        this.bitmap = tileBitmap
        if (tileBitmap is CacheTileBitmap) {
            tileBitmap.setIsDisplayed(true)
        }
        if (tileBitmap != null && !fromCache) {
            animationState.restart()
        } else {
            animationState.stop()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Tile
        if (srcRect != other.srcRect) return false
        if (sampleSize != other.sampleSize) return false
        if (bitmap != other.bitmap) return false
        return true
    }

    override fun hashCode(): Int {
        var result = srcRect.hashCode()
        result = 31 * result + sampleSize
        result = 31 * result + (bitmap?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Tile(" +
                "coordinate=${coordinate.toShortString()}," +
                "srcRect=${srcRect.toShortString()}," +
                "state=${TileState.name(state)}," +
                "sampleSize=$sampleSize," +
                "bitmap=${bitmap})"
    }

    class AnimationState {

        private var startTime = System.currentTimeMillis()
        private var progress: Float = 1f
            set(value) {
                if (value != field) {
                    field = value
                    alpha = (value * 255).toInt()
                }
            }

        var alpha: Int = 255
            private set
        val running: Boolean
            get() = progress < 1f

        fun calculate(duration: Long): Boolean {
            progress = if (duration > 0) {
                val currentTimeMillis = System.currentTimeMillis()
                val elapsedTime = currentTimeMillis - startTime
                if (elapsedTime >= duration) 1f else elapsedTime / duration.toFloat()
            } else {
                1f
            }
            return running
        }

        fun restart() {
            startTime = System.currentTimeMillis()
            progress = 0f
            alpha = 0
        }

        fun stop() {
            if (running) {
                startTime = 0
                progress = 1f
                alpha = 255
            }
        }
    }
}