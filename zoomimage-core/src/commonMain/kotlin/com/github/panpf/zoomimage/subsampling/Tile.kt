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

package com.github.panpf.zoomimage.subsampling

import com.github.panpf.zoomimage.util.IntOffsetCompat
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.toShortString
import kotlinx.coroutines.Job
import kotlin.time.TimeSource

/**
 * A tile of the image, store the region, sample multiplier, Bitmap, load status, and other information of the tile
 *
 * @see com.github.panpf.zoomimage.core.desktop.test.subsampling.TileTest
 */
class Tile(
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
    var tileImage: TileImage? = null

    /**
     * The state of the tile
     */
    @TileState
    var state: Int = TileState.STATE_NONE

    val animationState = AnimationState()

    fun setTileImage(tileImage: TileImage?, allowAnimate: Boolean) {
        val oldTileImage = this.tileImage
        if (tileImage == oldTileImage) return
        oldTileImage?.setIsDisplayed(false)
        this.tileImage = tileImage
        tileImage?.setIsDisplayed(true)
        if (tileImage != null && allowAnimate) {
            animationState.restart()
        } else {
            animationState.stop()
        }
    }

    fun cleanTileImage() {
        setTileImage(null, allowAnimate = false)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as Tile
        if (coordinate != other.coordinate) return false
        if (srcRect != other.srcRect) return false
        if (sampleSize != other.sampleSize) return false
        return true
    }

    override fun hashCode(): Int {
        var result = coordinate.hashCode()
        result = 31 * result + srcRect.hashCode()
        result = 31 * result + sampleSize
        return result
    }

    override fun toString(): String {
        return "Tile(" +
                "coordinate=${coordinate.toShortString()}," +
                "srcRect=${srcRect.toShortString()}," +
                "srcSize=${srcRect.width}x${srcRect.height}," +
                "state=${TileState.name(state)}," +
                "sampleSize=$sampleSize," +
                "bitmap=${tileImage})"
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