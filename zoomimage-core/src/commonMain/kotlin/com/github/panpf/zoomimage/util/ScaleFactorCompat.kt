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

/**
 * Constructs a [ScaleFactorCompat] from the given x and y scale values
 *
 * Copy androidx/compose/ui/layout/ScaleFactor.kt
 */
fun ScaleFactorCompat(scaleX: Float, scaleY: Float) = ScaleFactorCompat(packFloats(scaleX, scaleY))

/**
 * Holds 2 dimensional scaling factors for horizontal and vertical axes
 *
 * Copy androidx/compose/ui/layout/ScaleFactor.kt
 */
@JvmInline
value class ScaleFactorCompat internal constructor(@PublishedApi internal val packedValue: Long) {

    /**
     * Returns the scale factor to apply along the horizontal axis
     */
    val scaleX: Float
        get() {
            // Explicitly compare against packed values to avoid
            // auto-boxing of ScaleFactorCompat.Unspecified
            check(this.packedValue != Unspecified.packedValue) {
                "ScaleFactorCompat is unspecified"
            }
            return unpackFloat1(packedValue)
        }

    /**
     * Returns the scale factor to apply along the vertical axis
     */
    val scaleY: Float
        get() {
            // Explicitly compare against packed values to avoid
            // auto-boxing of SizeCompat.Unspecified
            check(this.packedValue != Unspecified.packedValue) {
                "ScaleFactorCompat is unspecified"
            }
            return unpackFloat2(packedValue)
        }

    @Suppress("NOTHING_TO_INLINE", "KotlinRedundantDiagnosticSuppress")
    inline operator fun component1(): Float = scaleX

    @Suppress("NOTHING_TO_INLINE", "KotlinRedundantDiagnosticSuppress")
    inline operator fun component2(): Float = scaleY

    /**
     * Returns a copy of this ScaleFactorCompat instance optionally overriding the
     * scaleX or scaleY parameters
     */
    fun copy(scaleX: Float = this.scaleX, scaleY: Float = this.scaleY) =
        ScaleFactorCompat(scaleX, scaleY)

    /**
     * Multiplication operator.
     *
     * Returns a [ScaleFactorCompat] with scale x and y values multiplied by the operand
     */
    operator fun times(operand: Float) = ScaleFactorCompat(scaleX * operand, scaleY * operand)

    /**
     * Division operator.
     *
     * Returns a [ScaleFactorCompat] with scale x and y values divided by the operand
     */
    operator fun div(operand: Float) = ScaleFactorCompat(scaleX / operand, scaleY / operand)

    override fun toString() =
        if (isSpecified) "ScaleFactorCompat(${scaleX.roundToTenths()}, ${scaleY.roundToTenths()})" else "ScaleFactorCompat.Unspecified"

    companion object {

        /**
         * A ScaleFactorCompat whose [scaleX] and [scaleY] parameters are unspecified. This is a sentinel
         * value used to initialize a non-null parameter.
         * Access to scaleX or scaleY on an unspecified size is not allowed
         */
        val Unspecified = ScaleFactorCompat(Float.NaN, Float.NaN)
    }
}

private fun Float.roundToTenths(): Float {
    val shifted = this * 10
    val decimal = shifted - shifted.toInt()
    // Kotlin's round operator rounds 0.5f down to 0. Manually compare against
    // 0.5f and round up if necessary
    val roundedShifted = if (decimal >= 0.5f) {
        shifted.toInt() + 1
    } else {
        shifted.toInt()
    }
    return roundedShifted.toFloat() / 10
}

/**
 * `false` when this is [ScaleFactorCompat.Unspecified].
 */
inline val ScaleFactorCompat.isSpecified: Boolean
    get() = packedValue != ScaleFactorCompat.Unspecified.packedValue

/**
 * `true` when this is [ScaleFactorCompat.Unspecified].
 */
inline val ScaleFactorCompat.isUnspecified: Boolean
    get() = packedValue == ScaleFactorCompat.Unspecified.packedValue

/**
 * If this [ScaleFactorCompat] [isSpecified] then this is returned, otherwise [block] is executed
 * and its result is returned.
 */
inline fun ScaleFactorCompat.takeOrElse(block: () -> ScaleFactorCompat): ScaleFactorCompat =
    if (isSpecified) this else block()


/**
 * Multiplication operator with [SizeCompat].
 *
 * Return a new [SizeCompat] with the width and height multiplied by the [ScaleFactorCompat.scaleX] and
 * [ScaleFactorCompat.scaleY] respectively
 */
operator fun SizeCompat.times(scaleFactor: ScaleFactorCompat): SizeCompat =
    SizeCompat(this.width * scaleFactor.scaleX, this.height * scaleFactor.scaleY)

/**
 * Multiplication operator with [SizeCompat] with reverse parameter types to maintain
 * commutative properties of multiplication
 *
 * Return a new [SizeCompat] with the width and height multiplied by the [ScaleFactorCompat.scaleX] and
 * [ScaleFactorCompat.scaleY] respectively
 */
operator fun ScaleFactorCompat.times(size: SizeCompat): SizeCompat = size * this

/**
 * Division operator with [SizeCompat]
 *
 * Return a new [SizeCompat] with the width and height divided by [ScaleFactorCompat.scaleX] and
 * [ScaleFactorCompat.scaleY] respectively
 */
operator fun SizeCompat.div(scaleFactor: ScaleFactorCompat): SizeCompat =
    SizeCompat(width / scaleFactor.scaleX, height / scaleFactor.scaleY)

/**
 * Linearly interpolate between two [ScaleFactorCompat] parameters
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
fun lerp(start: ScaleFactorCompat, stop: ScaleFactorCompat, fraction: Float): ScaleFactorCompat {
    return ScaleFactorCompat(
        lerp(start.scaleX, stop.scaleX, fraction),
        lerp(start.scaleY, stop.scaleY, fraction)
    )
}


/* ************************************ Extra-extended functions ******************************** */

/**
 * Returns true if the scaling is 1
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.ScaleFactorCompatTest.testIsOrigin
 */
fun ScaleFactorCompat.isOrigin(): Boolean = scaleX.format(2) == 1f && scaleY.format(2) == 1f

/**
 * Return short string descriptions, for example: '3.45x9.87'
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.ScaleFactorCompatTest.testToShortString
 */
fun ScaleFactorCompat.toShortString(): String = "${scaleX.format(2)}x${scaleY.format(2)}"

/**
 * Create a ScaleFactorCompat, scaleX and scaleY are both [scale]
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.ScaleFactorCompatTest.testCreate
 */
fun ScaleFactorCompat(scale: Float): ScaleFactorCompat = ScaleFactorCompat(scale, scale)

/**
 * The scaling factor that remains the same scale, that is, scaleX and scaleY are both 1f
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.ScaleFactorCompatTest.testOrigin
 */
val ScaleFactorCompat.Companion.Origin: ScaleFactorCompat
    get() = scaleFactorCompatOrigin
private val scaleFactorCompatOrigin by lazy { ScaleFactorCompat(scaleX = 1f, scaleY = 1f) }

/**
 * Returns an ScaleFactorCompat scaled by multiplying [scaleFactor]
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.ScaleFactorCompatTest.testTimes
 */
operator fun ScaleFactorCompat.times(scaleFactor: ScaleFactorCompat) =
    ScaleFactorCompat(scaleX * scaleFactor.scaleX, scaleY * scaleFactor.scaleY)

/**
 * Returns an ScaleFactorCompat scaled by dividing [scaleFactor]
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.ScaleFactorCompatTest.testDiv
 */
operator fun ScaleFactorCompat.div(scaleFactor: ScaleFactorCompat) =
    ScaleFactorCompat(scaleX / scaleFactor.scaleX, scaleY / scaleFactor.scaleY)
