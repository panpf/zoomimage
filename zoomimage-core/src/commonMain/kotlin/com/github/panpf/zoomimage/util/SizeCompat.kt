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

@file:Suppress("NOTHING_TO_INLINE", "KotlinRedundantDiagnosticSuppress")

package com.github.panpf.zoomimage.util

import com.github.panpf.zoomimage.util.internal.packFloats
import com.github.panpf.zoomimage.util.internal.unpackFloat1
import com.github.panpf.zoomimage.util.internal.unpackFloat2
import kotlin.jvm.JvmInline
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Constructs a [SizeCompat] from the given width and height
 *
 * Copy from androidx/compose/ui/geometry/Size.kt
 */
fun SizeCompat(width: Float, height: Float) = SizeCompat(packFloats(width, height))

/**
 * Holds a 2D floating-point size.
 *
 * You can think of this as an [SizeCompat] from the origin.
 *
 * Copy from androidx/compose/ui/geometry/Size.kt
 */
@JvmInline
value class SizeCompat internal constructor(@PublishedApi internal val packedValue: Long) {

    val width: Float
        get() {
            // Explicitly compare against packed values to avoid auto-boxing of Size.Unspecified
            check(this.packedValue != Unspecified.packedValue) {
                "SizeCompat is unspecified"
            }
            return unpackFloat1(packedValue)
        }

    val height: Float
        get() {
            // Explicitly compare against packed values to avoid auto-boxing of Size.Unspecified
            check(this.packedValue != Unspecified.packedValue) {
                "SizeCompat is unspecified"
            }
            return unpackFloat2(packedValue)
        }

    inline operator fun component1(): Float = width

    inline operator fun component2(): Float = height

    /**
     * Returns a copy of this Size instance optionally overriding the
     * width or height parameter
     */
    fun copy(width: Float = this.width, height: Float = this.height) = SizeCompat(width, height)

    companion object {
        /**
         * An empty size, one with a zero width and a zero height.
         */
        val Zero = SizeCompat(0.0f, 0.0f)

        /**
         * A size whose [width] and [height] are unspecified. This is a sentinel
         * value used to initialize a non-null parameter.
         * Access to width or height on an unspecified size is not allowed.
         */
        val Unspecified = SizeCompat(Float.NaN, Float.NaN)
    }

    fun isEmpty(): Boolean = width <= 0.0f || height <= 0.0f

    /**
     * Multiplication operator.
     *
     * Returns a [SizeCompat] whose dimensions are the dimensions of the left-hand-side
     * operand (a [SizeCompat]) multiplied by the scalar right-hand-side operand (a
     * [Float]).
     */
    operator fun times(operand: Float) = SizeCompat(width * operand, height * operand)

    /**
     * Division operator.
     *
     * Returns a [SizeCompat] whose dimensions are the dimensions of the left-hand-side
     * operand (a [SizeCompat]) divided by the scalar right-hand-side operand (a
     * [Float]).
     */
    operator fun div(operand: Float) = SizeCompat(width / operand, height / operand)

    /**
     * The lesser of the magnitudes of the [width] and the [height].
     */
    val minDimension: Float
        get() = min(width.absoluteValue, height.absoluteValue)

    /**
     * The greater of the magnitudes of the [width] and the [height].
     */
    val maxDimension: Float
        get() = max(width.absoluteValue, height.absoluteValue)

    override fun toString(): String =
        if (isSpecified) {
            "SizeCompat(${width.toStringAsFixed(1)}, ${height.toStringAsFixed(1)})"
        } else {
            // In this case reading the width or height properties will throw, and they don't
            // contain meaningful values as strings anyway.
            "SizeCompat.Unspecified"
        }
}

/**
 * `false` when this is [SizeCompat.Unspecified].
 */
inline val SizeCompat.isSpecified: Boolean
    get() = packedValue != SizeCompat.Unspecified.packedValue

/**
 * `true` when this is [SizeCompat.Unspecified].
 */
inline val SizeCompat.isUnspecified: Boolean
    get() = packedValue == SizeCompat.Unspecified.packedValue

/**
 * If this [SizeCompat]&nbsp;[isSpecified] then this is returned, otherwise [block] is executed
 * and its result is returned.
 */
inline fun SizeCompat.takeOrElse(block: () -> SizeCompat): SizeCompat =
    if (isSpecified) this else block()

/**
 * Linearly interpolate between two sizes
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
fun lerp(start: SizeCompat, stop: SizeCompat, fraction: Float): SizeCompat {
    return SizeCompat(
        lerp(start.width, stop.width, fraction),
        lerp(start.height, stop.height, fraction)
    )
}

/**
 * Returns a [SizeCompat] with [size]'s [SizeCompat.width] and [SizeCompat.height] multiplied by [this]
 */
inline operator fun Int.times(size: SizeCompat) = size * this.toFloat()

/**
 * Returns a [SizeCompat] with [size]'s [SizeCompat.width] and [SizeCompat.height] multiplied by [this]
 */
inline operator fun Double.times(size: SizeCompat) = size * this.toFloat()

/**
 * Convert a [SizeCompat] to a [RectCompat].
 */
fun SizeCompat.toRect(): RectCompat = RectCompat(OffsetCompat.Zero, this)

/**
 * Returns a [SizeCompat] with [size]'s [SizeCompat.width] and [SizeCompat.height] multiplied by [this]
 */
inline operator fun Float.times(size: SizeCompat) = size * this

/**
 * Returns the [OffsetCompat] of the center of the rect from the point of [0, 0]
 * with this [SizeCompat].
 */
val SizeCompat.center: OffsetCompat get() = OffsetCompat(x = width / 2f, y = height / 2f)


/* ************************************ Extra-extended functions ******************************** */

/**
 * Return short string descriptions, for example: '100.56x900.45'
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.SizeCompatTest.testToShortString
 */
fun SizeCompat.toShortString(): String =
    if (isSpecified) "${width.format(2)}x${height.format(2)}" else "Unspecified"

/**
 * Return true if the size is not empty
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.SizeCompatTest.testIsNotEmpty
 */
fun SizeCompat.isNotEmpty(): Boolean = width > 0f && height > 0f

/**
 * Round a [SizeCompat] down to the nearest [Int] coordinates.
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.SizeCompatTest.testRound
 */
fun SizeCompat.round(): IntSizeCompat =
    if (isSpecified) IntSizeCompat(width.roundToInt(), height.roundToInt()) else IntSizeCompat.Zero

/**
 * The size after rotating [rotation] degrees
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.SizeCompatTest.testRotate
 */
fun SizeCompat.rotate(rotation: Int): SizeCompat {
    return if (rotation % 180 == 0) this else SizeCompat(width = height, height = width)
}

/**
 * The size after reverse rotating [rotation] degrees
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.SizeCompatTest.testReverseRotate
 */
fun SizeCompat.reverseRotate(rotation: Int): SizeCompat {
    val reverseRotation = (360 - rotation) % 360
    return rotate(reverseRotation)
}

/**
 * Returns true if the aspect ratio of itself and other is the same
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.SizeCompatTest.testIsSameAspectRatio
 */
fun SizeCompat.isSameAspectRatio(other: SizeCompat, delta: Float = 0f): Boolean {
    val selfScale = this.width / this.height
    val otherScale = other.width / other.height
    if (selfScale.compareTo(otherScale) == 0) {
        return true
    }
    if (delta != 0f && abs(selfScale - otherScale) <= delta) {
        return true
    }
    return false
}