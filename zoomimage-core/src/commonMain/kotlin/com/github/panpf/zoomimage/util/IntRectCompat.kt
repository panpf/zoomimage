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

package com.github.panpf.zoomimage.util

import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * An immutable, 2D, axis-aligned, integer bounds rectangle whose coordinates are relative to a given origin.
 *
 * Copy from androidx/compose/ui/unit/IntRect.kt
 */
data class IntRectCompat(
    /**
     * The offset of the left edge of this rectangle from the x axis.
     */
    val left: Int,

    /**
     * The offset of the top edge of this rectangle from the y axis.
     */
    val top: Int,

    /**
     * The offset of the right edge of this rectangle from the x axis.
     */
    val right: Int,

    /**
     * The offset of the bottom edge of this rectangle from the y axis.
     */
    val bottom: Int
) {
    companion object {

        /** A rectangle with left, top, right, and bottom edges all at zero. */
        val Zero: IntRectCompat = IntRectCompat(0, 0, 0, 0)
    }

    /** The distance between the left and right edges of this rectangle. */
    val width: Int
        get() {
            return right - left
        }

    /** The distance between the top and bottom edges of this rectangle. */
    val height: Int
        get() {
            return bottom - top
        }

    /**
     * The distance between the upper-left corner and the lower-right corner of
     * this rectangle.
     */
    val size: IntSizeCompat
        get() = IntSizeCompat(width, height)

    /**
     * Whether this rectangle encloses a non-zero area. Negative areas are
     * considered empty.
     */
    val isEmpty: Boolean
        get() = left >= right || top >= bottom

    /**
     * Returns a new rectangle translated by the given offset.
     *
     * To translate a rectangle by separate x and y components rather than by an
     * [OffsetCompat], consider [translate].
     */
    fun translate(offset: IntOffsetCompat): IntRectCompat {
        return IntRectCompat(left + offset.x, top + offset.y, right + offset.x, bottom + offset.y)
    }

    /**
     * Returns a new rectangle with translateX added to the x components and
     * translateY added to the y components.
     */
    fun translate(translateX: Int, translateY: Int): IntRectCompat {
        return IntRectCompat(
            left + translateX,
            top + translateY,
            right + translateX,
            bottom + translateY
        )
    }

    /** Returns a new rectangle with edges moved outwards by the given delta. */
    fun inflate(delta: Int): IntRectCompat {
        return IntRectCompat(left - delta, top - delta, right + delta, bottom + delta)
    }

    /** Returns a new rectangle with edges moved inwards by the given delta. */
    fun deflate(delta: Int): IntRectCompat = inflate(-delta)

    /**
     * Returns a new rectangle that is the intersection of the given
     * rectangle and this rectangle. The two rectangles must overlap
     * for this to be meaningful. If the two rectangles do not overlap,
     * then the resulting IntRectCompat will have a negative width or height.
     */
    fun intersect(other: IntRectCompat): IntRectCompat {
        return IntRectCompat(
            max(left, other.left),
            max(top, other.top),
            min(right, other.right),
            min(bottom, other.bottom)
        )
    }

    /** Whether `other` has a nonzero area of overlap with this rectangle. */
    fun overlaps(other: IntRectCompat): Boolean {
        if (right <= other.left || other.right <= left)
            return false
        if (bottom <= other.top || other.bottom <= top)
            return false
        return true
    }

    /**
     * The lesser of the magnitudes of the [width] and the [height] of this
     * rectangle.
     */
    val minDimension: Int
        get() = min(width.absoluteValue, height.absoluteValue)

    /**
     * The greater of the magnitudes of the [width] and the [height] of this
     * rectangle.
     */
    val maxDimension: Int
        get() = max(width.absoluteValue, height.absoluteValue)

    /**
     * The offset to the intersection of the top and left edges of this rectangle.
     */
    val topLeft: IntOffsetCompat
        get() = IntOffsetCompat(left, top)

    /**
     * The offset to the center of the top edge of this rectangle.
     */
    val topCenter: IntOffsetCompat
        get() = IntOffsetCompat(left + width / 2, top)

    /**
     * The offset to the intersection of the top and right edges of this rectangle.
     */
    val topRight: IntOffsetCompat
        get() = IntOffsetCompat(right, top)

    /**
     * The offset to the center of the left edge of this rectangle.
     */
    val centerLeft: IntOffsetCompat
        get() = IntOffsetCompat(left, top + height / 2)

    /**
     * The offset to the point halfway between the left and right and the top and
     * bottom edges of this rectangle.
     *
     * See also [IntSizeCompat.center].
     */
    val center: IntOffsetCompat
        get() = IntOffsetCompat(left + width / 2, top + height / 2)

    /**
     * The offset to the center of the right edge of this rectangle.
     */
    val centerRight: IntOffsetCompat
        get() = IntOffsetCompat(right, top + height / 2)

    /**
     * The offset to the intersection of the bottom and left edges of this rectangle.
     */
    val bottomLeft: IntOffsetCompat
        get() = IntOffsetCompat(left, bottom)

    /**
     * The offset to the center of the bottom edge of this rectangle.
     */
    val bottomCenter: IntOffsetCompat
        get() {
            return IntOffsetCompat(left + width / 2, bottom)
        }

    /**
     * The offset to the intersection of the bottom and right edges of this rectangle.
     */
    val bottomRight: IntOffsetCompat
        get() {
            return IntOffsetCompat(right, bottom)
        }

    /**
     * Whether the point specified by the given offset (which is assumed to be
     * relative to the origin) lies between the left and right and the top and
     * bottom edges of this rectangle.
     *
     * Rectangles include their top and left edges but exclude their bottom and
     * right edges.
     */
    fun contains(offset: IntOffsetCompat): Boolean {
        return offset.x >= left && offset.x < right && offset.y >= top && offset.y < bottom
    }

    override fun toString() = "IntRectCompat.fromLTRB(" +
            "$left, " +
            "$top, " +
            "$right, " +
            "$bottom)"
}

/**
 * Construct a rectangle from its left and top edges as well as its width and height.
 * @param offset OffsetCompat to represent the top and left parameters of the RectCompat
 * @param size Size to determine the width and height of this [IntRectCompat].
 * @return RectCompat with [IntRectCompat.left] and [IntRectCompat.top] configured to [IntOffsetCompat.x] and
 * [IntOffsetCompat.y] as [IntRectCompat.right] and [IntRectCompat.bottom] to [IntOffsetCompat.x] + [IntSizeCompat.width] and
 * [IntOffsetCompat.y] + [IntSizeCompat.height] respectively
 */
fun IntRectCompat(offset: IntOffsetCompat, size: IntSizeCompat) =
    IntRectCompat(
        left = offset.x,
        top = offset.y,
        right = offset.x + size.width,
        bottom = offset.y + size.height
    )

/**
 * Construct the smallest rectangle that encloses the given offsets, treating
 * them as vectors from the origin.
 * @param topLeft OffsetCompat representing the left and top edges of the rectangle
 * @param bottomRight OffsetCompat representing the bottom and right edges of the rectangle
 */
fun IntRectCompat(topLeft: IntOffsetCompat, bottomRight: IntOffsetCompat): IntRectCompat =
    IntRectCompat(
        left = topLeft.x,
        top = topLeft.y,
        right = bottomRight.x,
        bottom = bottomRight.y
    )

/**
 * Construct a rectangle that bounds the given circle
 * @param center OffsetCompat that represents the center of the circle
 * @param radius Radius of the circle to enclose
 */
fun IntRectCompat(center: IntOffsetCompat, radius: Int): IntRectCompat =
    IntRectCompat(
        left = center.x - radius,
        top = center.y - radius,
        right = center.x + radius,
        bottom = center.y + radius
    )

/**
 * Linearly interpolate between two rectangles.
 *
 * The [fraction] argument represents position on the timeline, with 0.0 meaning
 * that the interpolation has not started, returning [start] (or something
 * equivalent to [start]), 1.0 meaning that the interpolation has finished,
 * returning [stop] (or something equivalent to [stop]), and values in between
 * meaning that the interpolation is at the relevant point on the timeline
 * between [start] and [stop]. The interpolation can be extrapolated beyond 0.0 and
 * 1.0, so negative values and values greater than 1.0 are valid (and can
 * easily be generated by curves).
 *
 * Values for [fraction] are usually obtained from an [Animation<Float>], such as
 * an `AnimationController`.
 */
fun lerp(start: IntRectCompat, stop: IntRectCompat, fraction: Float): IntRectCompat {
    return IntRectCompat(
        lerp(start.left, stop.left, fraction),
        lerp(start.top, stop.top, fraction),
        lerp(start.right, stop.right, fraction),
        lerp(start.bottom, stop.bottom, fraction)
    )
}

/**
 * Converts an [IntRectCompat] to a [RectCompat]
 */
fun IntRectCompat.toRect(): RectCompat = RectCompat(
    left = left.toFloat(),
    top = top.toFloat(),
    right = right.toFloat(),
    bottom = bottom.toFloat()
)

/**
 * Rounds a [RectCompat] to an [IntRectCompat]
 */
fun RectCompat.roundToIntRect(): IntRectCompat = IntRectCompat(
    left = left.roundToInt(),
    top = top.roundToInt(),
    right = right.roundToInt(),
    bottom = bottom.roundToInt()
)


/* ************************************ Extra-extended functions ******************************** */

/**
 * Return short string descriptions, for example: '[0x0,500x400]'
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.IntRectCompatTest.testToShortString
 */
fun IntRectCompat.toShortString(): String = "[${left}x${top},${right}x${bottom}]"

/**
 * Returns an IntRectCompat scaled by multiplying [scale]
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.IntRectCompatTest.testTimes
 */
operator fun IntRectCompat.times(scale: Float): IntRectCompat =
    IntRectCompat(
        left = (left * scale).roundToInt(),
        top = (top * scale).roundToInt(),
        right = (right * scale).roundToInt(),
        bottom = (bottom * scale).roundToInt(),
    )

/**
 * Returns an IntRectCompat scaled by multiplying [scaleFactor]
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.IntRectCompatTest.testTimes
 */
operator fun IntRectCompat.times(scaleFactor: ScaleFactorCompat): IntRectCompat =
    IntRectCompat(
        left = (left * scaleFactor.scaleX).roundToInt(),
        top = (top * scaleFactor.scaleY).roundToInt(),
        right = (right * scaleFactor.scaleX).roundToInt(),
        bottom = (bottom * scaleFactor.scaleY).roundToInt(),
    )

/**
 * Returns an IntRectCompat scaled by dividing [scale]
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.IntRectCompatTest.testDiv
 */
operator fun IntRectCompat.div(scale: Float): IntRectCompat =
    IntRectCompat(
        left = (left / scale).roundToInt(),
        top = (top / scale).roundToInt(),
        right = (right / scale).roundToInt(),
        bottom = (bottom / scale).roundToInt(),
    )

/**
 * Returns an IntRectCompat scaled by dividing [scaleFactor]
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.IntRectCompatTest.testDiv
 */
operator fun IntRectCompat.div(scaleFactor: ScaleFactorCompat): IntRectCompat =
    IntRectCompat(
        left = (left / scaleFactor.scaleX).roundToInt(),
        top = (top / scaleFactor.scaleY).roundToInt(),
        right = (right / scaleFactor.scaleX).roundToInt(),
        bottom = (bottom / scaleFactor.scaleY).roundToInt(),
    )

/**
 * Limit the offset to the rectangular extent
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.IntRectCompatTest.testLimitToRect
 */
fun IntRectCompat.limitTo(rect: IntRectCompat): IntRectCompat =
    if (this.left < rect.left || this.left > rect.right
        || this.top < rect.top || this.top > rect.bottom
        || this.right < rect.left || this.right > rect.right
        || this.bottom < rect.top || this.bottom > rect.bottom
    ) {
        IntRectCompat(
            left = left.coerceIn(rect.left, rect.right),
            top = top.coerceIn(rect.top, rect.bottom),
            right = right.coerceIn(rect.left, rect.right),
            bottom = bottom.coerceIn(rect.top, rect.bottom),
        )
    } else {
        this
    }

/**
 * Limit Rect to 0 to the range of size
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.IntRectCompatTest.testLimitToSize
 */
fun IntRectCompat.limitTo(size: IntSizeCompat): IntRectCompat =
    limitTo(IntRectCompat(0, 0, size.width, size.height))

/**
 * Rotate the space by [rotation] degrees, and then return the rotated Rect
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.IntRectCompatTest.testRotateInSpace
 */
fun IntRectCompat.rotateInSpace(spaceSize: IntSizeCompat, rotation: Int): IntRectCompat {
    require(rotation % 90 == 0) { "rotation must be a multiple of 90, rotation: $rotation" }
    return when ((rotation % 360).let { if (it < 0) 360 + it else it }) {
        90 -> {
            IntRectCompat(
                left = spaceSize.height - this.bottom,
                top = this.left,
                right = spaceSize.height - this.top,
                bottom = this.right
            )
        }

        180 -> {
            IntRectCompat(
                left = spaceSize.width - this.right,
                top = spaceSize.height - this.bottom,
                right = spaceSize.width - this.left,
                bottom = spaceSize.height - this.top,
            )
        }

        270 -> {
            IntRectCompat(
                left = this.top,
                top = spaceSize.width - this.right,
                right = this.bottom,
                bottom = spaceSize.width - this.left,
            )
        }

        else -> this
    }
}

/**
 * Reverse rotate the space by [rotation] degrees, and then returns the reverse rotated Rect
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.IntRectCompatTest.testRotateInSpace
 */
fun IntRectCompat.reverseRotateInSpace(spaceSize: IntSizeCompat, rotation: Int): IntRectCompat {
    val rotatedSpaceSize = spaceSize.rotate(rotation)
    val reverseRotation = (360 - rotation) % 360
    return rotateInSpace(rotatedSpaceSize, reverseRotation)
}

/**
 * Flip this rect horizontally or vertically within a given container
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.IntRectCompatTest.testFlip
 */
fun IntRectCompat.flip(spaceSize: IntSizeCompat, vertical: Boolean = false): IntRectCompat {
    return if (!vertical) {
        IntRectCompat(
            left = spaceSize.width - right,
            top = top,
            right = spaceSize.width - left,
            bottom = bottom
        )
    } else {
        IntRectCompat(
            left = left,
            top = spaceSize.height - bottom,
            right = right,
            bottom = spaceSize.height - top
        )
    }
}