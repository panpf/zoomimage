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
import kotlin.math.roundToInt

/**
 * Constructs a [TransformOriginCompat] from the given fractional values from the Layer's
 * width and height
 *
 * Copy from androidx/compose/ui/graphics/TransformOrigin.kt
 */
fun TransformOriginCompat(pivotFractionX: Float, pivotFractionY: Float): TransformOriginCompat =
    TransformOriginCompat(packFloats(pivotFractionX, pivotFractionY))

/**
 * A two-dimensional position represented as a fraction of the Layer's width and height
 *
 * Copy from androidx/compose/ui/graphics/TransformOrigin.kt
 */
@JvmInline
value class TransformOriginCompat internal constructor(@PublishedApi internal val packedValue: Long) {

    /**
     * Return the position along the x-axis that should be used as the
     * origin for rotation and scale transformations. This is represented as a fraction
     * of the width of the content. A value of 0.5f represents the midpoint between the left
     * and right bounds of the content
     */
    val pivotFractionX: Float
        get() = unpackFloat1(packedValue)

    /**
     * Return the position along the y-axis that should be used as the
     * origin for rotation and scale transformations. This is represented as a fraction
     * of the height of the content. A value of 0.5f represents the midpoint between the top
     * and bottom bounds of the content
     */
    val pivotFractionY: Float
        get() = unpackFloat2(packedValue)

    @Suppress("NOTHING_TO_INLINE", "KotlinRedundantDiagnosticSuppress")
    inline operator fun component1(): Float = pivotFractionX

    @Suppress("NOTHING_TO_INLINE", "KotlinRedundantDiagnosticSuppress")
    inline operator fun component2(): Float = pivotFractionY

    /**
     * Returns a copy of this TransformOriginCompat instance optionally overriding the
     * pivotFractionX or pivotFractionY parameter
     */
    fun copy(
        pivotFractionX: Float = this.pivotFractionX,
        pivotFractionY: Float = this.pivotFractionY
    ) = TransformOriginCompat(pivotFractionX, pivotFractionY)

    override fun toString() =
        "TransformOriginCompat(${pivotFractionX.format(2)}, ${pivotFractionY.format(2)}))"

    companion object {

        /**
         * [TransformOriginCompat] constant to indicate that the center of the content should
         * be used for rotation and scale transformations
         */
        val Center = TransformOriginCompat(0.5f, 0.5f)
    }
}


/* ************************************ Extra-extended functions ******************************** */

/**
 * Return short string descriptions, for example: '0.52x0.52'
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.TransformOriginCompatTest.testToShortString
 */
fun TransformOriginCompat.toShortString(): String =
    "${pivotFractionX.format(2)}x${pivotFractionY.format(2)}"

/**
 * [TransformOriginCompat] constant to indicate that the top start of the content should
 * be used for rotation and scale transformations
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.TransformOriginCompatTest.testTopStart
 */
val TransformOriginCompat.Companion.TopStart
    get() = transformOriginCompatTopStart
private val transformOriginCompatTopStart by lazy {
    TransformOriginCompat(pivotFractionX = 0f, pivotFractionY = 0f)
}

/**
 * Return a new [TransformOriginCompat] with the width and height multiplied by the [operand]
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.TransformOriginCompatTest.testTimes
 */
operator fun TransformOriginCompat.times(operand: Float) =
    TransformOriginCompat(pivotFractionX * operand, pivotFractionY * operand)

/**
 * Multiplication operator with [IntSizeCompat].
 *
 * Return a new [IntSizeCompat] with the width and height multiplied by the [TransformOriginCompat.pivotFractionX] and
 * [TransformOriginCompat.pivotFractionY] respectively
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.TransformOriginCompatTest.testTimes
 */
operator fun IntSizeCompat.times(origin: TransformOriginCompat): IntSizeCompat =
    IntSizeCompat(
        width = (this.width * origin.pivotFractionX).roundToInt(),
        height = (this.height * origin.pivotFractionY).roundToInt()
    )

/**
 * Multiplication operator with [SizeCompat].
 *
 * Return a new [SizeCompat] with the width and height multiplied by the [TransformOriginCompat.pivotFractionX] and
 * [TransformOriginCompat.pivotFractionY] respectively
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.TransformOriginCompatTest.testTimes
 */
operator fun SizeCompat.times(origin: TransformOriginCompat): SizeCompat =
    SizeCompat(
        width = this.width * origin.pivotFractionX,
        height = this.height * origin.pivotFractionY
    )

/**
 * Return a new [TransformOriginCompat] with the width and height dividing by the [operand]
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.TransformOriginCompatTest.testDiv
 */
operator fun TransformOriginCompat.div(operand: Float) =
    TransformOriginCompat(pivotFractionX / operand, pivotFractionY / operand)

/**
 * Linearly interpolate between two [TransformOriginCompat] parameters
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
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.TransformOriginCompatTest.testLerp
 */
fun lerp(
    start: TransformOriginCompat,
    stop: TransformOriginCompat,
    fraction: Float
): TransformOriginCompat {
    return TransformOriginCompat(
        pivotFractionX = lerp(start.pivotFractionX, stop.pivotFractionX, fraction),
        pivotFractionY = lerp(start.pivotFractionY, stop.pivotFractionY, fraction)
    )
}