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

import com.github.panpf.zoomimage.util.internal.packFloats
import com.github.panpf.zoomimage.util.internal.unpackFloat1
import com.github.panpf.zoomimage.util.internal.unpackFloat2
import kotlin.jvm.JvmInline
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Constructs an Offset from the given relative x and y offsets
 *
 * Copy from androidx/compose/ui/geometry/Offset.kt
 */
fun OffsetCompat(x: Float, y: Float) = OffsetCompat(packFloats(x, y))

/**
 * An immutable 2D floating-point offset.
 *
 * Generally speaking, Offsets can be interpreted in two ways:
 *
 * 1. As representing a point in Cartesian space a specified distance from a
 *    separately-maintained origin. For example, the top-left position of
 *    children in the [RenderBox] protocol is typically represented as an
 *    [Offset] from the top left of the parent box.
 *
 * 2. As a vector that can be applied to coordinates. For example, when
 *    painting a [RenderObject], the parent is passed an [Offset] from the
 *    screen's origin which it can add to the offsets of its children to find
 *    the [Offset] from the screen's origin to each of the children.
 *
 * Because a particular [OffsetCompat] can be interpreted as one sense at one time
 * then as the other sense at a later time, the same class is used for both
 * senses.
 *
 * See also:
 *
 *  * [SizeCompat], which represents a vector describing the size of a rectangle.
 *
 * Creates an offset. The first argument sets [x], the horizontal component,
 * and the second sets [y], the vertical component.
 *
 * Copy from androidx/compose/ui/geometry/Offset.kt
 */
@JvmInline
value class OffsetCompat internal constructor(internal val packedValue: Long) {

    val x: Float
        get() {
            // Explicitly compare against packed values to avoid auto-boxing of Size.Unspecified
            check(this.packedValue != Unspecified.packedValue) {
                "OffsetCompat is unspecified"
            }
            return unpackFloat1(packedValue)
        }

    val y: Float
        get() {
            // Explicitly compare against packed values to avoid auto-boxing of Size.Unspecified
            check(this.packedValue != Unspecified.packedValue) {
                "OffsetCompat is unspecified"
            }
            return unpackFloat2(packedValue)
        }

    operator fun component1(): Float = x

    operator fun component2(): Float = y

    /**
     * Returns a copy of this Offset instance optionally overriding the
     * x or y parameter
     */
    fun copy(x: Float = this.x, y: Float = this.y) = OffsetCompat(x, y)

    companion object {
        /**
         * An offset with zero magnitude.
         *
         * This can be used to represent the origin of a coordinate space.
         */
        val Zero = OffsetCompat(0.0f, 0.0f)

        /**
         * An offset with infinite x and y components.
         *
         * See also:
         *
         *  * [isInfinite], which checks whether either component is infinite.
         *  * [isFinite], which checks whether both components are finite.
         */
        // This is included for completeness, because [Size.infinite] exists.
        val Infinite = OffsetCompat(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)

        /**
         * Represents an unspecified [OffsetCompat] value, usually a replacement for `null`
         * when a primitive value is desired.
         */
        val Unspecified = OffsetCompat(Float.NaN, Float.NaN)
    }

    fun isValid(): Boolean {
        check(!x.isNaN() && !y.isNaN()) {
            "OffsetCompat argument contained a NaN value."
        }
        return true
    }

    /**
     * The magnitude of the offset.
     *
     * If you need this value to compare it to another [OffsetCompat]'s distance,
     * consider using [getDistanceSquared] instead, since it is cheaper to compute.
     */
    fun getDistance() = sqrt(x * x + y * y)

    /**
     * The square of the magnitude of the offset.
     *
     * This is cheaper than computing the [getDistance] itself.
     */
    fun getDistanceSquared() = x * x + y * y

    /**
     * Unary negation operator.
     *
     * Returns an offset with the coordinates negated.
     *
     * If the [OffsetCompat] represents an arrow on a plane, this operator returns the
     * same arrow but pointing in the reverse direction.
     */
    operator fun unaryMinus(): OffsetCompat = OffsetCompat(-x, -y)

    /**
     * Binary subtraction operator.
     *
     * Returns an offset whose [x] value is the left-hand-side operand's [x]
     * minus the right-hand-side operand's [x] and whose [y] value is the
     * left-hand-side operand's [y] minus the right-hand-side operand's [y].
     */
    operator fun minus(other: OffsetCompat): OffsetCompat = OffsetCompat(x - other.x, y - other.y)

    /**
     * Binary addition operator.
     *
     * Returns an offset whose [x] value is the sum of the [x] values of the
     * two operands, and whose [y] value is the sum of the [y] values of the
     * two operands.
     */
    operator fun plus(other: OffsetCompat): OffsetCompat = OffsetCompat(x + other.x, y + other.y)

    /**
     * Multiplication operator.
     *
     * Returns an offset whose coordinates are the coordinates of the
     * left-hand-side operand (an OffsetCompat) multiplied by the scalar
     * right-hand-side operand (a Float).
     */
    operator fun times(operand: Float): OffsetCompat = OffsetCompat(x * operand, y * operand)

    /**
     * Division operator.
     *
     * Returns an offset whose coordinates are the coordinates of the
     * left-hand-side operand (an OffsetCompat) divided by the scalar right-hand-side
     * operand (a Float).
     */
    operator fun div(operand: Float): OffsetCompat = OffsetCompat(x / operand, y / operand)

    /**
     * Modulo (remainder) operator.
     *
     * Returns an offset whose coordinates are the remainder of dividing the
     * coordinates of the left-hand-side operand (an OffsetCompat) by the scalar
     * right-hand-side operand (a Float).
     */
    operator fun rem(operand: Float) = OffsetCompat(x % operand, y % operand)

    override fun toString() = if (isSpecified) {
        "OffsetCompat(${x.toStringAsFixed(1)}, ${y.toStringAsFixed(1)})"
    } else {
        // In this case reading the x or y properties will throw, and they don't contain meaningful
        // values as strings anyway.
        "OffsetCompat.Unspecified"
    }
}

/**
 * Linearly interpolate between two offsets.
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
fun lerp(start: OffsetCompat, stop: OffsetCompat, fraction: Float): OffsetCompat {
    return OffsetCompat(
        lerp(start.x, stop.x, fraction),
        lerp(start.y, stop.y, fraction)
    )
}

/**
 * True if both x and y values of the [OffsetCompat] are finite
 */
val OffsetCompat.isFinite: Boolean get() = x.isFinite() && y.isFinite()

/**
 * `false` when this is [OffsetCompat.Unspecified].
 */
val OffsetCompat.isSpecified: Boolean get() = packedValue != OffsetCompat.Unspecified.packedValue

/**
 * `true` when this is [OffsetCompat.Unspecified].
 */
val OffsetCompat.isUnspecified: Boolean get() = packedValue == OffsetCompat.Unspecified.packedValue

/**
 * If this [OffsetCompat]&nbsp;[isSpecified] then this is returned, otherwise [block] is executed
 * and its result is returned.
 */
inline fun OffsetCompat.takeOrElse(block: () -> OffsetCompat): OffsetCompat =
    if (isSpecified) this else block()


/* ************************************ Extra-extended functions ******************************** */

/**
 * If true is returned, it means Offset is empty
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.OffsetCompatTest.testIsEmpty
 */
fun OffsetCompat.isEmpty() = abs(x).format(2) == 0f && abs(y).format(2) == 0f

/**
 * Return short string descriptions, for example: '10.01x9.03'
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.OffsetCompatTest.testToShortString
 */
fun OffsetCompat.toShortString(): String =
    if (isSpecified) "${x.format(2)}x${y.format(2)}" else "Unspecified"

/**
 * Multiplication operator.
 *
 * Returns an offset whose coordinates are the coordinates of the
 * left-hand-side operand (an OffsetCompat) multiplied by the scalar
 * right-hand-side operand (a Float).
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.OffsetCompatTest.testTimes
 */
operator fun OffsetCompat.times(scaleFactor: ScaleFactorCompat): OffsetCompat =
    OffsetCompat(x = x * scaleFactor.scaleX, y = y * scaleFactor.scaleY)

/**
 * Division operator.
 *
 * Returns an offset whose coordinates are the coordinates of the
 * left-hand-side operand (an OffsetCompat) divided by the scalar right-hand-side
 * operand (a Float).
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.OffsetCompatTest.testDiv
 */
operator fun OffsetCompat.div(scaleFactor: ScaleFactorCompat): OffsetCompat =
    OffsetCompat(x = x / scaleFactor.scaleX, y = y / scaleFactor.scaleY)

/**
 * Rotate the space by [rotation] degrees, and then return the rotated coordinates
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.OffsetCompatTest.testRotateInSpace
 */
fun OffsetCompat.rotateInSpace(spaceSize: SizeCompat, rotation: Int): OffsetCompat {
    require(rotation % 90 == 0) { "rotation must be a multiple of 90, rotation: $rotation" }
    return when ((rotation % 360).let { if (it < 0) 360 + it else it }) {
        90 -> OffsetCompat(x = spaceSize.height - y, y = x)
        180 -> OffsetCompat(x = spaceSize.width - x, y = spaceSize.height - y)
        270 -> OffsetCompat(x = y, y = spaceSize.width - x)
        else -> this
    }
}

/**
 * Reverse rotate the space by [rotation] degrees, and then returns the reverse rotated coordinates
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.OffsetCompatTest.testReverseRotateInSpace
 */
fun OffsetCompat.reverseRotateInSpace(spaceSize: SizeCompat, rotation: Int): OffsetCompat {
    val rotatedSpaceSize = spaceSize.rotate(rotation)
    val reverseRotation = (360 - rotation) % 360
    return rotateInSpace(rotatedSpaceSize, reverseRotation)
}

/**
 * Limit the offset to the rectangular extent
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.OffsetCompatTest.testLimitToRect
 */
fun OffsetCompat.limitTo(rect: RectCompat): OffsetCompat {
    return if (x < rect.left || x > rect.right || y < rect.top || y > rect.bottom) {
        OffsetCompat(
            x = x.coerceIn(rect.left, rect.right),
            y = y.coerceIn(rect.top, rect.bottom),
        )
    } else {
        this
    }
}

/**
 * Limit offset to 0 to the range of size
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.OffsetCompatTest.testLimitToSize
 */
fun OffsetCompat.limitTo(size: SizeCompat): OffsetCompat =
    limitTo(RectCompat(0f, 0f, size.width, size.height))
