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

import com.github.panpf.zoomimage.subsampling.TileBitmap
import com.github.panpf.zoomimage.subsampling.TileState
import com.github.panpf.zoomimage.util.IntOffsetCompat
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.toShortString
import kotlinx.coroutines.Job
import kotlin.time.TimeSource

/**
 * A tile of the image, store the region, sample multiplier, Bitmap, load status, and other information of the tile
 *
 * test: desktopTest/com.github.panpf.zoomimage.core.desktop.test.subsampling.internal.TileTest
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
    var tileBitmap: TileBitmap? = null

    /**
     * The state of the tile
     */
    @TileState
    var state: Int = TileState.STATE_NONE

    val animationState = AnimationState()

    fun setTileBitmap(tileBitmap: TileBitmap?, allowAnimate: Boolean) {
        val oldTileBitmap = this.tileBitmap
        if (tileBitmap == oldTileBitmap) return
        oldTileBitmap?.setIsDisplayed(false)
        this.tileBitmap = tileBitmap
        tileBitmap?.setIsDisplayed(true)
        if (tileBitmap != null && allowAnimate) {
            animationState.restart()
        } else {
            animationState.stop()
        }
    }

    fun cleanTileBitmap() {
        setTileBitmap(null, allowAnimate = false)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Tile) return false
        if (srcRect != other.srcRect) return false
        if (sampleSize != other.sampleSize) return false
        if (tileBitmap != other.tileBitmap) return false
        return true
    }

    override fun hashCode(): Int {
        var result = srcRect.hashCode()
        result = 31 * result + sampleSize
        result = 31 * result + (tileBitmap?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Tile(" +
                "coordinate=${coordinate.toShortString()}," +
                "srcRect=${srcRect.toShortString()}," +
                "srcSize=${srcRect.width}x${srcRect.height}," +
                "state=${TileState.name(state)}," +
                "sampleSize=$sampleSize," +
                "bitmap=${tileBitmap})"
    }

    class AnimationState {

        private var startTime = TimeSource.Monotonic.markNow()
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
                val elapsedTime = startTime.elapsedNow().inWholeMilliseconds
                if (elapsedTime >= duration) 1f else elapsedTime / duration.toFloat()
            } else {
                1f
            }
            return running
        }

        fun restart() {
            startTime = TimeSource.Monotonic.markNow()
            progress = 0f
            alpha = 0
        }

        fun stop() {
            if (running) {
                startTime = TimeSource.Monotonic.markNow()
                progress = 1f
                alpha = 255
            }
        }
    }
}