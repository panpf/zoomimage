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

/**
 * A simple version of a 2D transformation that includes scale, pan, and rotation
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.TransformCompatTest
 */
data class TransformCompat(
    /**
     * Scale factor
     */
    val scale: ScaleFactorCompat,

    /**
     * Pan position
     */
    val offset: OffsetCompat,

    /**
     * The degree of rotation
     */
    val rotation: Float = 0f,

    /**
     * The origin of the scaling operation
     */
    val scaleOrigin: TransformOriginCompat = TransformOriginCompat.TopStart,

    /**
     * The origin of the rotation operation
     */
    val rotationOrigin: TransformOriginCompat = TransformOriginCompat.TopStart,
) {

    /**
     * The horizontal scale factor
     */
    val scaleX: Float
        get() = scale.scaleX

    /**
     * The vertical scale factor
     */
    val scaleY: Float
        get() = scale.scaleY

    /**
     * The horizontal offset
     */
    val offsetX: Float
        get() = offset.x

    /**
     * The vertical offset
     */
    val offsetY: Float
        get() = offset.y

    /**
     * The scale horizontal pivot fraction
     */
    val scaleOriginX: Float
        get() = scaleOrigin.pivotFractionX

    /**
     * The scale vertical pivot fraction
     */
    val scaleOriginY: Float
        get() = scaleOrigin.pivotFractionY

    /**
     * The rotation horizontal pivot fraction
     */
    val rotationOriginX: Float
        get() = rotationOrigin.pivotFractionX

    /**
     * The rotation vertical pivot fraction
     */
    val rotationOriginY: Float
        get() = rotationOrigin.pivotFractionY

    init {
        require(scale.isSpecified && offset.isSpecified) {
            "ScaleFactorCompat and OffsetCompat must be specified at the same time"
        }
    }

    companion object {

        /**
         * Transformations that remain unchanged
         */
        val Origin = TransformCompat(
            scale = ScaleFactorCompat(1f, 1f),
            offset = OffsetCompat.Zero,
            rotation = 0f,
            scaleOrigin = TransformOriginCompat.TopStart,
            rotationOrigin = TransformOriginCompat.TopStart,
        )
    }

    override fun toString(): String {
        return "TransformCompat(" +
                "scale=${scale.toShortString()}, " +
                "offset=${offset.toShortString()}, " +
                "rotation=$rotation, " +
                "scaleOrigin=${scaleOrigin.toShortString()}, " +
                "rotationOrigin=${rotationOrigin.toShortString()}" +
                ")"
    }
}

/**
 * If the current TransformCompat is empty, return true
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.TransformCompatTest.testIsEmpty
 */
fun TransformCompat.isEmpty(): Boolean {
    return scale.isOrigin() && offset.isEmpty() && rotation.format(2) == 0f
}

/**
 * If the current TransformCompat is not empty, return true
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.TransformCompatTest.testIsNotEmpty
 */
fun TransformCompat.isNotEmpty(): Boolean = !isEmpty()

/**
 * Return short string descriptions, for example: '(3.45x9.87,10.56x20.56,45.03,0.52x0.52,0.52x0.52)'
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.TransformCompatTest.testToShortString
 */
fun TransformCompat.toShortString(): String =
    "(${scale.toShortString()},${offset.toShortString()},$rotation,${scaleOrigin.toShortString()},${rotationOrigin.toShortString()})"

/**
 * Returns an TransformCompat scaled by multiplying [scaleFactor]
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.TransformCompatTest.testTimes
 */
operator fun TransformCompat.times(scaleFactor: ScaleFactorCompat): TransformCompat {
    return this.copy(
        scale = ScaleFactorCompat(
            scaleX = scale.scaleX * scaleFactor.scaleX,
            scaleY = scale.scaleY * scaleFactor.scaleY,
        ),
        offset = OffsetCompat(
            x = offset.x * scaleFactor.scaleX,
            y = offset.y * scaleFactor.scaleY,
        ),
    )
}

/**
 * Returns an TransformCompat scaled by multiplying [scaleFactor]
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.TransformCompatTest.testDiv
 */
operator fun TransformCompat.div(scaleFactor: ScaleFactorCompat): TransformCompat {
    return this.copy(
        scale = ScaleFactorCompat(
            scaleX = scale.scaleX / scaleFactor.scaleX,
            scaleY = scale.scaleY / scaleFactor.scaleY,
        ),
        offset = OffsetCompat(
            x = offset.x / scaleFactor.scaleX,
            y = offset.y / scaleFactor.scaleY,
        ),
    )
}

/**
 * Add other TransformCompat to the current TransformCompat, and the scale origin or rotation origin
 * of both must be the same when neither is scaled or rotated equal to the default value
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.TransformCompatTest.testPlus
 */
operator fun TransformCompat.plus(other: TransformCompat): TransformCompat {
    require(
        this.scaleOrigin == other.scaleOrigin
                || this.scale == ScaleFactorCompat.Origin
                || other.scale == ScaleFactorCompat.Origin
    ) {
        "When both this and other TransformCompat's scale are not empty, their scaleOrigin must be the same: " +
                "this.scaleOrigin=${this.scaleOrigin}, other.scaleOrigin=${other.scaleOrigin}"
    }
    require(
        this.rotationOrigin == other.rotationOrigin
                || this.rotation == 0f
                || other.rotation == 0f
    ) {
        "When both this and other TransformCompat's rotation are not zero, their rotationOrigin must be the same: " +
                "this.rotationOrigin=${this.rotationOrigin}, other.rotationOrigin=${other.rotationOrigin}"
    }
    val scaleOrigin = if (
        this.scaleOrigin == other.scaleOrigin
        || other.scale == ScaleFactorCompat.Origin
    ) {
        this.scaleOrigin
    } else {
        other.scaleOrigin
    }
    val rotationOrigin = if (
        this.rotationOrigin == other.rotationOrigin
        || other.rotation == 0f
    ) {
        this.rotationOrigin
    } else {
        other.rotationOrigin
    }
    val addScale = other.scale
    return this.copy(
        scale = scale.times(addScale),
        offset = (offset * addScale) + other.offset,
        rotation = rotation + other.rotation,
        scaleOrigin = scaleOrigin,
        rotationOrigin = rotationOrigin,
    )
}

/**
 * Subtract other TransformCompat from the current TransformCompat, and the scale origin or rotation
 * origin of both must be the same when neither is scaled or rotated equal to the default value
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.TransformCompatTest.testMinus
 */
operator fun TransformCompat.minus(other: TransformCompat): TransformCompat {
    require(
        this.scaleOrigin == other.scaleOrigin
                || this.scale == ScaleFactorCompat.Origin
                || other.scale == ScaleFactorCompat.Origin
    ) {
        "When both this and other TransformCompat's scale are not empty, their scaleOrigin must be the same: " +
                "this.scaleOrigin=${this.scaleOrigin}, other.scaleOrigin=${other.scaleOrigin}"
    }
    require(
        this.rotationOrigin == other.rotationOrigin
                || this.rotation == 0f
                || other.rotation == 0f
    ) {
        "When both this and other TransformCompat's rotation are not zero, their rotationOrigin must be the same: " +
                "this.rotationOrigin=${this.rotationOrigin}, other.rotationOrigin=${other.rotationOrigin}"
    }
    val scaleOrigin = if (
        this.scaleOrigin == other.scaleOrigin
        || other.scale == ScaleFactorCompat.Origin
    ) {
        this.scaleOrigin
    } else {
        other.scaleOrigin
    }
    val rotationOrigin = if (
        this.rotationOrigin == other.rotationOrigin
        || other.rotation == 0f
    ) {
        this.rotationOrigin
    } else {
        other.rotationOrigin
    }
    val minusScale = scale.div(other.scale)
    return this.copy(
        scale = minusScale,
        offset = offset - (other.offset * minusScale),
        rotation = rotation - other.rotation,
        scaleOrigin = scaleOrigin,
        rotationOrigin = rotationOrigin,
    )
}

/**
 * Linearly interpolate between two TransformCompat.
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
 * @see com.github.panpf.zoomimage.core.common.test.util.TransformCompatTest.testLerp
 */
fun lerp(start: TransformCompat, stop: TransformCompat, fraction: Float): TransformCompat {
    require(
        start.scaleOrigin == stop.scaleOrigin
                || start.scale == ScaleFactorCompat.Origin
                || stop.scale == ScaleFactorCompat.Origin
    ) {
        "When both start and stop TransformCompat's scale are not empty, their scaleOrigin must be the same: " +
                "start.scaleOrigin=${start.scaleOrigin}, stop.scaleOrigin=${stop.scaleOrigin}"
    }
    require(
        start.rotationOrigin == stop.rotationOrigin
                || start.rotation == 0f
                || stop.rotation == 0f
    ) {
        "When both start and stop TransformCompat's rotation are not zero, their rotationOrigin must be the same: " +
                "start.rotationOrigin=${start.rotationOrigin}, stop.rotationOrigin=${stop.rotationOrigin}"
    }
    val scaleOrigin = if (
        start.scaleOrigin == stop.scaleOrigin
        || stop.scale == ScaleFactorCompat.Origin
    ) {
        start.scaleOrigin
    } else {
        stop.scaleOrigin
    }
    val rotationOrigin = if (
        start.rotationOrigin == stop.rotationOrigin
        || stop.rotation == 0f
    ) {
        start.rotationOrigin
    } else {
        stop.rotationOrigin
    }
    return start.copy(
        scale = lerp(start = start.scale, stop = stop.scale, fraction = fraction),
        offset = lerp(start = start.offset, stop = stop.offset, fraction = fraction),
        rotation = lerp(start = start.rotation, stop = stop.rotation, fraction = fraction),
        scaleOrigin = scaleOrigin,
        rotationOrigin = rotationOrigin,
    )
}