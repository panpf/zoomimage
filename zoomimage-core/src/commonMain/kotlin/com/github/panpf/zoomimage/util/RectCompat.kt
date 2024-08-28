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
 * An immutable, 2D, axis-aligned, floating-point rectangle whose coordinates are relative to a given origin.
 *
 * Copy from androidx/compose/ui/geometry/Rect.kt
 */
data class RectCompat(
    /**
     * The offset of the left edge of this rectangle from the x axis.
     */
    val left: Float,

    /**
     * The offset of the top edge of this rectangle from the y axis.
     */
    val top: Float,

    /**
     * The offset of the right edge of this rectangle from the x axis.
     */
    val right: Float,

    /**
     * The offset of the bottom edge of this rectangle from the y axis.
     */
    val bottom: Float
) {

    companion object {

        /** A rectangle with left, top, right, and bottom edges all at zero. */
        val Zero: RectCompat = RectCompat(0.0f, 0.0f, 0.0f, 0.0f)
    }

    /** The distance between the left and right edges of this rectangle. */
    val width: Float
        get() {
            return right - left
        }

    /** The distance between the top and bottom edges of this rectangle. */
    val height: Float
        get() {
            return bottom - top
        }

    /**
     * The distance between the upper-left corner and the lower-right corner of
     * this rectangle.
     */
    val size: SizeCompat
        get() = SizeCompat(width, height)

    /** Whether any of the coordinates of this rectangle are equal to positive infinity. */
    // included for consistency with Offset and Size
    val isInfinite: Boolean
        get() = left >= Float.POSITIVE_INFINITY ||
                top >= Float.POSITIVE_INFINITY ||
                right >= Float.POSITIVE_INFINITY ||
                bottom >= Float.POSITIVE_INFINITY

    /** Whether all coordinates of this rectangle are finite. */
    val isFinite: Boolean
        get() = left.isFinite() &&
                top.isFinite() &&
                right.isFinite() &&
                bottom.isFinite()

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
    fun translate(offset: OffsetCompat): RectCompat {
        return RectCompat(left + offset.x, top + offset.y, right + offset.x, bottom + offset.y)
    }

    /**
     * Returns a new rectangle with translateX added to the x components and
     * translateY added to the y components.
     */
    fun translate(translateX: Float, translateY: Float): RectCompat {
        return RectCompat(
            left + translateX,
            top + translateY,
            right + translateX,
            bottom + translateY
        )
    }

    /** Returns a new rectangle with edges moved outwards by the given delta. */
    fun inflate(delta: Float): RectCompat {
        return RectCompat(left - delta, top - delta, right + delta, bottom + delta)
    }

    /** Returns a new rectangle with edges moved inwards by the given delta. */
    fun deflate(delta: Float): RectCompat = inflate(-delta)

    /**
     * Returns a new rectangle that is the intersection of the given
     * rectangle and this rectangle. The two rectangles must overlap
     * for this to be meaningful. If the two rectangles do not overlap,
     * then the resulting RectCompat will have a negative width or height.
     */
    fun intersect(other: RectCompat): RectCompat {
        return RectCompat(
            max(left, other.left),
            max(top, other.top),
            min(right, other.right),
            min(bottom, other.bottom)
        )
    }

    /** Whether `other` has a nonzero area of overlap with this rectangle. */
    fun overlaps(other: RectCompat): Boolean {
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
    val minDimension: Float
        get() = min(width.absoluteValue, height.absoluteValue)

    /**
     * The greater of the magnitudes of the [width] and the [height] of this
     * rectangle.
     */
    val maxDimension: Float
        get() = max(width.absoluteValue, height.absoluteValue)

    /**
     * The offset to the intersection of the top and left edges of this rectangle.
     */
    val topLeft: OffsetCompat
        get() = OffsetCompat(left, top)

    /**
     * The offset to the center of the top edge of this rectangle.
     */
    val topCenter: OffsetCompat
        get() = OffsetCompat(left + width / 2.0f, top)

    /**
     * The offset to the intersection of the top and right edges of this rectangle.
     */
    val topRight: OffsetCompat
        get() = OffsetCompat(right, top)

    /**
     * The offset to the center of the left edge of this rectangle.
     */
    val centerLeft: OffsetCompat
        get() = OffsetCompat(left, top + height / 2.0f)

    /**
     * The offset to the point halfway between the left and right and the top and
     * bottom edges of this rectangle.
     *
     * See also [SizeCompat.center].
     */
    val center: OffsetCompat
        get() = OffsetCompat(left + width / 2.0f, top + height / 2.0f)

    /**
     * The offset to the center of the right edge of this rectangle.
     */
    val centerRight: OffsetCompat
        get() = OffsetCompat(right, top + height / 2.0f)

    /**
     * The offset to the intersection of the bottom and left edges of this rectangle.
     */
    val bottomLeft: OffsetCompat
        get() = OffsetCompat(left, bottom)

    /**
     * The offset to the center of the bottom edge of this rectangle.
     */
    val bottomCenter: OffsetCompat
        get() {
            return OffsetCompat(left + width / 2.0f, bottom)
        }

    /**
     * The offset to the intersection of the bottom and right edges of this rectangle.
     */
    val bottomRight: OffsetCompat
        get() {
            return OffsetCompat(right, bottom)
        }

    /**
     * Whether the point specified by the given offset (which is assumed to be
     * relative to the origin) lies between the left and right and the top and
     * bottom edges of this rectangle.
     *
     * Rectangles include their top and left edges but exclude their bottom and
     * right edges.
     */
    operator fun contains(offset: OffsetCompat): Boolean {
        return offset.x >= left && offset.x < right && offset.y >= top && offset.y < bottom
    }

    override fun toString() = "RectCompat.fromLTRB(" +
            "${left.toStringAsFixed(1)}, " +
            "${top.toStringAsFixed(1)}, " +
            "${right.toStringAsFixed(1)}, " +
            "${bottom.toStringAsFixed(1)})"
}

/**
 * Construct a rectangle from its left and top edges as well as its width and height.
 * @param offset OffsetCompat to represent the top and left parameters of the RectCompat
 * @param size Size to determine the width and height of this [RectCompat].
 * @return RectCompat with [RectCompat.left] and [RectCompat.top] configured to [OffsetCompat.x] and [OffsetCompat.y] as
 * [RectCompat.right] and [RectCompat.bottom] to [OffsetCompat.x] + [SizeCompat.width] and [OffsetCompat.y] + [SizeCompat.height]
 * respectively
 */
fun RectCompat(offset: OffsetCompat, size: SizeCompat): RectCompat =
    RectCompat(
        offset.x,
        offset.y,
        offset.x + size.width,
        offset.y + size.height
    )

/**
 * Construct the smallest rectangle that encloses the given offsets, treating
 * them as vectors from the origin.
 * @param topLeft OffsetCompat representing the left and top edges of the rectangle
 * @param bottomRight OffsetCompat representing the bottom and right edges of the rectangle
 */
fun RectCompat(topLeft: OffsetCompat, bottomRight: OffsetCompat): RectCompat =
    RectCompat(
        topLeft.x,
        topLeft.y,
        bottomRight.x,
        bottomRight.y
    )

/**
 * Construct a rectangle that bounds the given circle
 * @param center OffsetCompat that represents the center of the circle
 * @param radius Radius of the circle to enclose
 */
fun RectCompat(center: OffsetCompat, radius: Float): RectCompat =
    RectCompat(
        center.x - radius,
        center.y - radius,
        center.x + radius,
        center.y + radius
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
fun lerp(start: RectCompat, stop: RectCompat, fraction: Float): RectCompat {
    return RectCompat(
        lerp(start.left, stop.left, fraction),
        lerp(start.top, stop.top, fraction),
        lerp(start.right, stop.right, fraction),
        lerp(start.bottom, stop.bottom, fraction)
    )
}


/* ************************************ Extra-extended functions ******************************** */

/**
 * Return short string descriptions, for example: '[0.01x0.34,100.67x200.02]'
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.RectCompatTest.testToShortString
 */
fun RectCompat.toShortString(): String =
    "[${left.format(2)}x${top.format(2)},${right.format(2)}x${bottom.format(2)}]"

/**
 * Rounds a [RectCompat] to an [IntRectCompat]
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.RectCompatTest.testRound
 */
fun RectCompat.round(): IntRectCompat = IntRectCompat(
    left = left.roundToInt(),
    top = top.roundToInt(),
    right = right.roundToInt(),
    bottom = bottom.roundToInt()
)

/**
 * Returns an RectCompat scaled by multiplying [scale]
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.RectCompatTest.testTimes
 */
operator fun RectCompat.times(scale: Float): RectCompat =
    RectCompat(
        left = (left * scale),
        top = (top * scale),
        right = (right * scale),
        bottom = (bottom * scale),
    )

/**
 * Returns an RectCompat scaled by multiplying [scaleFactor]
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.RectCompatTest.testTimes
 */
operator fun RectCompat.times(scaleFactor: ScaleFactorCompat): RectCompat =
    RectCompat(
        left = (left * scaleFactor.scaleX),
        top = (top * scaleFactor.scaleY),
        right = (right * scaleFactor.scaleX),
        bottom = (bottom * scaleFactor.scaleY),
    )

/**
 * Returns an RectCompat scaled by dividing [scale]
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.RectCompatTest.testDiv
 */
operator fun RectCompat.div(scale: Float): RectCompat =
    RectCompat(
        left = (left / scale),
        top = (top / scale),
        right = (right / scale),
        bottom = (bottom / scale),
    )

/**
 * Returns an RectCompat scaled by dividing [scaleFactor]
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.RectCompatTest.testDiv
 */
operator fun RectCompat.div(scaleFactor: ScaleFactorCompat): RectCompat =
    RectCompat(
        left = (left / scaleFactor.scaleX),
        top = (top / scaleFactor.scaleY),
        right = (right / scaleFactor.scaleX),
        bottom = (bottom / scaleFactor.scaleY),
    )

/**
 * Limit the offset to the rectangular extent
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.RectCompatTest.testLimitToRect
 */
fun RectCompat.limitTo(rect: RectCompat): RectCompat =
    if (this.left < rect.left || this.left > rect.right
        || this.top < rect.top || this.top > rect.bottom
        || this.right < rect.left || this.right > rect.right
        || this.bottom < rect.top || this.bottom > rect.bottom
    ) {
        RectCompat(
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
 * @see com.github.panpf.zoomimage.core.common.test.util.RectCompatTest.testLimitToSize
 */
fun RectCompat.limitTo(size: SizeCompat): RectCompat =
    limitTo(RectCompat(0f, 0f, size.width, size.height))

/**
 * Rotate the space by [rotation] degrees, and then return the rotated Rect
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.RectCompatTest.testRotateInSpace
 */
fun RectCompat.rotateInSpace(spaceSize: SizeCompat, rotation: Int): RectCompat {
    require(rotation % 90 == 0) { "rotation must be a multiple of 90, rotation: $rotation" }
    return when ((rotation % 360).let { if (it < 0) 360 + it else it }) {
        90 -> {
            RectCompat(
                left = spaceSize.height - this.bottom,
                top = this.left,
                right = spaceSize.height - this.top,
                bottom = this.right
            )
        }

        180 -> {
            RectCompat(
                left = spaceSize.width - this.right,
                top = spaceSize.height - this.bottom,
                right = spaceSize.width - this.left,
                bottom = spaceSize.height - this.top,
            )
        }

        270 -> {
            RectCompat(
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
 * @see com.github.panpf.zoomimage.core.common.test.util.RectCompatTest.testReverseRotateInSpace
 */
fun RectCompat.reverseRotateInSpace(spaceSize: SizeCompat, rotation: Int): RectCompat {
    val rotatedSpaceSize = spaceSize.rotate(rotation)
    val reverseRotation = (360 - rotation) % 360
    return rotateInSpace(rotatedSpaceSize, reverseRotation)
}

/**
 * Flip this rect horizontally or vertically within a given container
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.RectCompatTest.testFlip
 */
fun RectCompat.flip(spaceSize: SizeCompat, vertical: Boolean = false): RectCompat {
    return if (!vertical) {
        RectCompat(
            left = spaceSize.width - right,
            top = top,
            right = spaceSize.width - left,
            bottom = bottom
        )
    } else {
        RectCompat(
            left = left,
            top = spaceSize.height - bottom,
            right = right,
            bottom = spaceSize.height - top
        )
    }
}
