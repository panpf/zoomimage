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

package com.github.panpf.zoomimage.compose.zoom

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.ScaleFactor
import androidx.compose.ui.layout.isSpecified
import com.github.panpf.zoomimage.compose.util.Origin
import com.github.panpf.zoomimage.compose.util.TopStart
import com.github.panpf.zoomimage.compose.util.div
import com.github.panpf.zoomimage.compose.util.format
import com.github.panpf.zoomimage.compose.util.isEmpty
import com.github.panpf.zoomimage.compose.util.isOrigin
import com.github.panpf.zoomimage.compose.util.times
import com.github.panpf.zoomimage.compose.util.toShortString

/**
 * A simple version of a 2D transformation that includes scale, pan, and rotation
 *
 * @see com.github.panpf.zoomimage.compose.common.test.zoom.TransformTest
 */
@Immutable
data class Transform(
    /**
     * Scale factor
     */
    val scale: ScaleFactor,

    /**
     * Pan position
     */
    val offset: Offset,

    /**
     * The degree of rotation
     */
    val rotation: Float = 0f,

    /**
     * The origin of the scaling operation
     */
    val scaleOrigin: TransformOrigin = TransformOrigin.TopStart,

    /**
     * The origin of the rotation operation
     */
    val rotationOrigin: TransformOrigin = TransformOrigin.TopStart,
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
        val Origin = Transform(
            scale = ScaleFactor(1f, 1f),
            offset = Offset.Zero,
            rotation = 0f,
            scaleOrigin = TransformOrigin.TopStart,
            rotationOrigin = TransformOrigin.TopStart,
        )
    }

    override fun toString(): String {
        return "Transform(" +
                "scale=${scale.toShortString()}, " +
                "offset=${offset.toShortString()}, " +
                "rotation=$rotation, " +
                "scaleOrigin=${scaleOrigin.toShortString()}, " +
                "rotationOrigin=${rotationOrigin.toShortString()}" +
                ")"
    }
}

/**
 * Returns whether the Transform is empty, that is, the scale is 1, the offset is 0, and the rotation is 0
 *
 * @see com.github.panpf.zoomimage.compose.common.test.zoom.TransformTest.testIsEmptyAndIsNotEmpty
 */
fun Transform.isEmpty(): Boolean {
    return scale.isOrigin() && offset.isEmpty() && rotation.format(2) == 0f
}

/**
 * Returns whether the Transform is not empty
 *
 * @see com.github.panpf.zoomimage.compose.common.test.zoom.TransformTest.testIsEmptyAndIsNotEmpty
 */
fun Transform.isNotEmpty(): Boolean = !isEmpty()

/**
 * Return short string descriptions, for example: '(3.45x9.87,10.56x20.56,45.03,0.52x0.52,0.52x0.52)'
 *
 * @see com.github.panpf.zoomimage.compose.common.test.zoom.TransformTest.testToShortString
 */
fun Transform.toShortString(): String =
    "(${scale.toShortString()},${offset.toShortString()},$rotation,${scaleOrigin.toShortString()},${rotationOrigin.toShortString()})"

/**
 * Returns an Transform scaled by multiplying [scaleFactor]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.zoom.TransformTest.testTimes
 */
operator fun Transform.times(scaleFactor: ScaleFactor): Transform {
    return this.copy(
        scale = ScaleFactor(
            scaleX = scale.scaleX * scaleFactor.scaleX,
            scaleY = scale.scaleY * scaleFactor.scaleY,
        ),
        offset = Offset(
            x = offset.x * scaleFactor.scaleX,
            y = offset.y * scaleFactor.scaleY,
        ),
    )
}

/**
 * Returns an Transform scaled by multiplying [scaleFactor]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.zoom.TransformTest.testDiv
 */
operator fun Transform.div(scaleFactor: ScaleFactor): Transform {
    return this.copy(
        scale = ScaleFactor(
            scaleX = scale.scaleX / scaleFactor.scaleX,
            scaleY = scale.scaleY / scaleFactor.scaleY,
        ),
        offset = Offset(
            x = offset.x / scaleFactor.scaleX,
            y = offset.y / scaleFactor.scaleY,
        ),
    )
}

/**
 * Add other Transform to the current Transform, and the scale origin or rotation origin of both must be the same when neither is scaled or rotated equal to the default value
 *
 * @see com.github.panpf.zoomimage.compose.common.test.zoom.TransformTest.testPlus
 */
operator fun Transform.plus(other: Transform): Transform {
    require(
        this.scaleOrigin == other.scaleOrigin
                || this.scale == ScaleFactor.Origin
                || other.scale == ScaleFactor.Origin
    ) {
        "When both this and other Transform's scale are not empty, their scaleOrigin must be the same: " +
                "this.scaleOrigin=${this.scaleOrigin}, other.scaleOrigin=${other.scaleOrigin}"
    }
    require(
        this.rotationOrigin == other.rotationOrigin
                || this.rotation == 0f
                || other.rotation == 0f
    ) {
        "When both this and other Transform's rotation are not zero, their rotationOrigin must be the same: " +
                "this.rotationOrigin=${this.rotationOrigin}, other.rotationOrigin=${other.rotationOrigin}"
    }
    val scaleOrigin = if (
        this.scaleOrigin == other.scaleOrigin
        || other.scale == ScaleFactor.Origin
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
 * Subtract other Transform from the current Transform, and the scale origin or rotation origin of both must be the same when neither is scaled or rotated equal to the default value
 *
 * @see com.github.panpf.zoomimage.compose.common.test.zoom.TransformTest.testMinus
 */
operator fun Transform.minus(other: Transform): Transform {
    require(
        this.scaleOrigin == other.scaleOrigin
                || this.scale == ScaleFactor.Origin
                || other.scale == ScaleFactor.Origin
    ) {
        "When both this and other Transform's scale are not empty, their scaleOrigin must be the same: " +
                "this.scaleOrigin=${this.scaleOrigin}, other.scaleOrigin=${other.scaleOrigin}"
    }
    require(
        this.rotationOrigin == other.rotationOrigin
                || this.rotation == 0f
                || other.rotation == 0f
    ) {
        "When both this and other Transform's rotation are not zero, their rotationOrigin must be the same: " +
                "this.rotationOrigin=${this.rotationOrigin}, other.rotationOrigin=${other.rotationOrigin}"
    }
    val scaleOrigin = if (
        this.scaleOrigin == other.scaleOrigin
        || other.scale == ScaleFactor.Origin
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
 * Linearly interpolate between two Transform.
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
 * @see com.github.panpf.zoomimage.compose.common.test.zoom.TransformTest.testLerp
 */
@Stable
fun lerp(start: Transform, stop: Transform, fraction: Float): Transform {
    require(
        start.scaleOrigin == stop.scaleOrigin
                || start.scale == ScaleFactor.Origin
                || stop.scale == ScaleFactor.Origin
    ) {
        "When both start and stop Transform's scale are not empty, their scaleOrigin must be the same: " +
                "start.scaleOrigin=${start.scaleOrigin}, stop.scaleOrigin=${stop.scaleOrigin}"
    }
    require(
        start.rotationOrigin == stop.rotationOrigin
                || start.rotation == 0f
                || stop.rotation == 0f
    ) {
        "When both start and stop Transform's rotation are not zero, their rotationOrigin must be the same: " +
                "start.rotationOrigin=${start.rotationOrigin}, stop.rotationOrigin=${stop.rotationOrigin}"
    }
    val scaleOrigin = if (
        start.scaleOrigin == stop.scaleOrigin
        || stop.scale == ScaleFactor.Origin
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
        scale = androidx.compose.ui.layout.lerp(start.scale, stop.scale, fraction),
        offset = androidx.compose.ui.geometry.lerp(start.offset, stop.offset, fraction),
        rotation = androidx.compose.ui.util.lerp(start.rotation, stop.rotation, fraction),
        scaleOrigin = scaleOrigin,
        rotationOrigin = rotationOrigin,
    )
}