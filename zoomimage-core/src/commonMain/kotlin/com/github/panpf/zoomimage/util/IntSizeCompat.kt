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

import com.github.panpf.zoomimage.util.internal.packInts
import com.github.panpf.zoomimage.util.internal.unpackInt1
import com.github.panpf.zoomimage.util.internal.unpackInt2
import kotlin.jvm.JvmInline
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Constructs an [IntSizeCompat] from width and height [Int] values.
 *
 * Copy from androidx/compose/ui/unit/IntSize.kt
 */
fun IntSizeCompat(width: Int, height: Int): IntSizeCompat = IntSizeCompat(packInts(width, height))

/**
 * A two-dimensional size class used for measuring in [Int] pixels.
 *
 * Copy from androidx/compose/ui/unit/IntSize.kt
 */
@JvmInline
value class IntSizeCompat internal constructor(@PublishedApi internal val packedValue: Long) {

    /**
     * The horizontal aspect of the size in [Int] pixels.
     */
    val width: Int
        get() = unpackInt1(packedValue)

    /**
     * The vertical aspect of the size in [Int] pixels.
     */
    val height: Int
        get() = unpackInt2(packedValue)

    @Suppress("NOTHING_TO_INLINE", "KotlinRedundantDiagnosticSuppress")
    inline operator fun component1(): Int = width

    @Suppress("NOTHING_TO_INLINE", "KotlinRedundantDiagnosticSuppress")
    inline operator fun component2(): Int = height

    /**
     * Returns an IntSizeCompat scaled by multiplying [width] and [height] by [other]
     */
    operator fun times(other: Int): IntSizeCompat =
        IntSizeCompat(width = width * other, height = height * other)

    /**
     * Returns an IntSizeCompat scaled by dividing [width] and [height] by [other]
     */
    operator fun div(other: Int): IntSizeCompat =
        IntSizeCompat(width = width / other, height = height / other)

    override fun toString(): String = "$width x $height"

    companion object {
        /**
         * IntSize with a zero (0) width and height.
         */
        val Zero = IntSizeCompat(0, 0)
    }
}

/**
 * Returns an [IntSizeCompat] with [size]'s [IntSizeCompat.width] and [IntSizeCompat.height]
 * multiplied by [this].
 */
operator fun Int.times(size: IntSizeCompat) = size * this

/**
 * Convert a [IntSizeCompat] to a [IntRectCompat].
 */
fun IntSizeCompat.toIntRect(): IntRectCompat {
    return IntRectCompat(IntOffsetCompat.Zero, this)
}

/**
 * Returns the [IntOffsetCompat] of the center of the rect from the point of [0, 0]
 * with this [IntSizeCompat].
 */
val IntSizeCompat.center: IntOffsetCompat
    get() = IntOffsetCompat(x = width / 2, y = height / 2)

// temporary while PxSize is transitioned to Size
fun IntSizeCompat.toSize(): SizeCompat = SizeCompat(width.toFloat(), height.toFloat())


/* ************************************ Extra-extended functions ******************************** */

/**
 * Return short string descriptions, for example: '100x200'
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.IntSizeCompatTest.testToShortString
 */
fun IntSizeCompat.toShortString(): String = "${width}x$height"

/**
 * Return true if the size is empty
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.IntSizeCompatTest.testIsEmpty
 */
fun IntSizeCompat.isEmpty(): Boolean = width <= 0 || height <= 0

/**
 * Return true if the size is not empty
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.IntSizeCompatTest.testIsNotEmpty
 */
fun IntSizeCompat.isNotEmpty(): Boolean = width > 0 && height > 0

/**
 * Returns an IntSizeCompat scaled by multiplying [this] by [scaleFactor]
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.IntSizeCompatTest.testTimes
 */
operator fun IntSizeCompat.times(scaleFactor: ScaleFactorCompat): IntSizeCompat {
    return IntSizeCompat(
        width = (width * scaleFactor.scaleX).roundToInt(),
        height = (height * scaleFactor.scaleY).roundToInt()
    )
}

/**
 * Returns an IntSizeCompat scaled by multiplying [this] by [scale]
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.IntSizeCompatTest.testTimes
 */
operator fun IntSizeCompat.times(scale: Float): IntSizeCompat =
    IntSizeCompat(
        width = (this.width * scale).roundToInt(),
        height = (this.height * scale).roundToInt()
    )

/**
 * Returns an IntSizeCompat scaled by dividing [this] by [scaleFactor]
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.IntSizeCompatTest.testDiv
 */
operator fun IntSizeCompat.div(scaleFactor: ScaleFactorCompat): IntSizeCompat {
    return IntSizeCompat(
        width = (width / scaleFactor.scaleX).roundToInt(),
        height = (height / scaleFactor.scaleY).roundToInt()
    )
}

/**
 * Returns an IntSizeCompat scaled by dividing [this] by [scale]
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.IntSizeCompatTest.testDiv
 */
operator fun IntSizeCompat.div(scale: Float): IntSizeCompat =
    IntSizeCompat(
        width = (this.width / scale).roundToInt(),
        height = (this.height / scale).roundToInt()
    )

/**
 * The size after rotating [rotation] degrees
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.IntSizeCompatTest.testRotate
 */
fun IntSizeCompat.rotate(rotation: Int): IntSizeCompat {
    return if (rotation % 180 == 0) this else IntSizeCompat(width = height, height = width)
}

/**
 * The size after reverse rotating [rotation] degrees
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.IntSizeCompatTest.testReverseRotate
 */
fun IntSizeCompat.reverseRotate(rotation: Int): IntSizeCompat {
    val reverseRotation = (360 - rotation) % 360
    return rotate(reverseRotation)
}

/**
 * Returns true if the aspect ratio of itself and other is the same
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.IntSizeCompatTest.testIsSameAspectRatio
 */
fun IntSizeCompat.isSameAspectRatio(other: IntSizeCompat, delta: Float = 0f): Boolean {
    val selfScale = this.width / this.height.toFloat()
    val otherScale = other.width / other.height.toFloat()
    if (selfScale.compareTo(otherScale) == 0) {
        return true
    }
    if (delta != 0f && abs(selfScale - otherScale) <= delta) {
        return true
    }
    return false
}

/**
 * Linearly interpolate between two [IntSizeCompat]s.
 *
 * The [fraction] argument represents position on the timeline, with 0.0 meaning
 * that the interpolation has not started, returning [start] (or something
 * equivalent to [start]), 1.0 meaning that the interpolation has finished,
 * returning [stop] (or something equivalent to [stop]), and values in between
 * meaning that the interpolation is at the relevant point on the timeline
 * between [start] and [stop]. The interpolation can be extrapolated beyond 0.0 and
 * 1.0, so negative values and values greater than 1.0 are valid.
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.IntSizeCompatTest.testLerp
 */
fun lerp(start: IntSizeCompat, stop: IntSizeCompat, fraction: Float): IntSizeCompat =
    IntSizeCompat(
        lerp(start.width, stop.width, fraction),
        lerp(start.height, stop.height, fraction)
    )

/**
 * Returns a copy of this IntOffset instance optionally overriding the
 * x or y parameter
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.IntSizeCompatTest.testCopy
 */
fun IntSizeCompat.copy(width: Int = this.width, height: Int = this.height) =
    IntSizeCompat(width = width, height = height)