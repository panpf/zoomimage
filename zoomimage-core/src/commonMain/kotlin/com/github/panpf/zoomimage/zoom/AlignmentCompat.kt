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

package com.github.panpf.zoomimage.zoom

import com.github.panpf.zoomimage.util.IntOffsetCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import kotlin.math.roundToInt

/**
 * An interface to calculate the position of a sized box inside an available space. [AlignmentCompat] is
 * often used to define the alignment of a layout inside a parent layout.
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.AlignmentCompatTest
 */
fun interface AlignmentCompat {
    /**
     * Calculates the position of a box of size [size] relative to the top left corner of an area
     * of size [space]. The returned offset can be negative or larger than `space - size`,
     * meaning that the box will be positioned partially or completely outside the area.
     */
    fun align(size: IntSizeCompat, space: IntSizeCompat, ltrLayout: Boolean): IntOffsetCompat

    /**
     * A collection of common [AlignmentCompat]s aware of layout direction.
     */
    companion object {
        val TopStart: AlignmentCompat = BiasAlignmentCompat(-1f, -1f)
        val TopCenter: AlignmentCompat = BiasAlignmentCompat(0f, -1f)
        val TopEnd: AlignmentCompat = BiasAlignmentCompat(1f, -1f)
        val CenterStart: AlignmentCompat = BiasAlignmentCompat(-1f, 0f)
        val Center: AlignmentCompat = BiasAlignmentCompat(0f, 0f)
        val CenterEnd: AlignmentCompat = BiasAlignmentCompat(1f, 0f)
        val BottomStart: AlignmentCompat = BiasAlignmentCompat(-1f, 1f)
        val BottomCenter: AlignmentCompat = BiasAlignmentCompat(0f, 1f)
        val BottomEnd: AlignmentCompat = BiasAlignmentCompat(1f, 1f)
    }
}

data class BiasAlignmentCompat(
    val horizontalBias: Float,
    val verticalBias: Float
) : AlignmentCompat {
    override fun align(
        size: IntSizeCompat,
        space: IntSizeCompat,
        ltrLayout: Boolean
    ): IntOffsetCompat {
        // Convert to Px first and only round at the end, to avoid rounding twice while calculating
        // the new positions
        val centerX = (space.width - size.width).toFloat() / 2f
        val centerY = (space.height - size.height).toFloat() / 2f
        val resolvedHorizontalBias = if (ltrLayout) {
            horizontalBias
        } else {
            -1 * horizontalBias
        }

        val x = centerX * (1 + resolvedHorizontalBias)
        val y = centerY * (1 + verticalBias)
        return IntOffsetCompat(x.roundToInt(), y.roundToInt())
    }
}

/**
 * Returns the name of [AlignmentCompat], which can also be converted back via the [valueOf] method
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.AlignmentCompatTest.testName
 */
val AlignmentCompat.name: String
    get() = when (this) {
        AlignmentCompat.TopStart -> "TopStart"
        AlignmentCompat.TopCenter -> "TopCenter"
        AlignmentCompat.TopEnd -> "TopEnd"
        AlignmentCompat.CenterStart -> "CenterStart"
        AlignmentCompat.Center -> "Center"
        AlignmentCompat.CenterEnd -> "CenterEnd"
        AlignmentCompat.BottomStart -> "BottomStart"
        AlignmentCompat.BottomCenter -> "BottomCenter"
        AlignmentCompat.BottomEnd -> "BottomEnd"
        else -> "Unknown AlignmentCompat: $this"
    }

/**
 * Returns the [AlignmentCompat] corresponding to the given [name], or throws [IllegalArgumentException]. see [name] property
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.AlignmentCompatTest.testValueOf
 */
fun AlignmentCompat.Companion.valueOf(name: String): AlignmentCompat {
    return when (name) {
        "TopStart" -> TopStart
        "TopCenter" -> TopCenter
        "TopEnd" -> TopEnd
        "CenterStart" -> CenterStart
        "Center" -> Center
        "CenterEnd" -> CenterEnd
        "BottomStart" -> BottomStart
        "BottomCenter" -> BottomCenter
        "BottomEnd" -> BottomEnd
        else -> throw IllegalArgumentException("Unknown alignment name: $name")
    }
}

/**
 * If true is returned, this [AlignmentCompat] is the horizontal starting position
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.AlignmentCompatTest.testIsStart
 */
val AlignmentCompat.isStart: Boolean
    get() = this == AlignmentCompat.TopStart
            || this == AlignmentCompat.CenterStart
            || this == AlignmentCompat.BottomStart

/**
 * If true is returned, this [AlignmentCompat] is the horizontal center position
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.AlignmentCompatTest.testIsHorizontalCenter
 */
val AlignmentCompat.isHorizontalCenter: Boolean
    get() = this == AlignmentCompat.TopCenter
            || this == AlignmentCompat.Center
            || this == AlignmentCompat.BottomCenter

/**
 * If true is returned, this [AlignmentCompat] is the horizontal ending position
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.AlignmentCompatTest.testIsEnd
 */
val AlignmentCompat.isEnd: Boolean
    get() = this == AlignmentCompat.TopEnd
            || this == AlignmentCompat.CenterEnd
            || this == AlignmentCompat.BottomEnd

/**
 * If true is returned, this [AlignmentCompat] is the horizontal and vertical center position
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.AlignmentCompatTest.testIsCenter
 */
val AlignmentCompat.isCenter: Boolean
    get() = this == AlignmentCompat.Center

/**
 * If true is returned, this [AlignmentCompat] is the vertical starting position
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.AlignmentCompatTest.testIsTop
 */
val AlignmentCompat.isTop: Boolean
    get() = this == AlignmentCompat.TopStart
            || this == AlignmentCompat.TopCenter
            || this == AlignmentCompat.TopEnd

/**
 * If true is returned, this [AlignmentCompat] is the vertical center position
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.AlignmentCompatTest.testIsVerticalCenter
 */
val AlignmentCompat.isVerticalCenter: Boolean
    get() = this == AlignmentCompat.CenterStart
            || this == AlignmentCompat.Center
            || this == AlignmentCompat.CenterEnd

/**
 * If true is returned, this [AlignmentCompat] is the vertical ending position
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.AlignmentCompatTest.testIsBottom
 */
val AlignmentCompat.isBottom: Boolean
    get() = this == AlignmentCompat.BottomStart
            || this == AlignmentCompat.BottomCenter
            || this == AlignmentCompat.BottomEnd