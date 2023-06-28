/*
 * Copyright (C) 2022 panpf <panpfpanpf@outlook.com>
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
package com.github.panpf.zoomimage.internal

import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.Rect
import android.widget.ImageView.ScaleType
import com.github.panpf.zoomimage.Size
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private val matrixValuesLocal = ThreadLocal<FloatArray>()
private val Matrix.localValues: FloatArray
    get() {
        val values = matrixValuesLocal.get()
            ?: FloatArray(9).apply { matrixValuesLocal.set(this) }
        getValues(values)
        return values
    }

internal fun Matrix.getScale(): ScaleFactor {
    val values = localValues
    return ScaleFactor(
        scaleX = values[Matrix.MSCALE_X],
        scaleY = values[Matrix.MSCALE_Y]
    )
}

internal fun Matrix.getTranslation(): Translation {
    val values = localValues
    return Translation(
        translationX = values[Matrix.MTRANS_X],
        translationY = values[Matrix.MTRANS_Y]
    )
}

internal fun Matrix.getRotateDegrees(): Int {
    val values = localValues
    val skewX: Float = values[Matrix.MSKEW_X]
    val scaleX: Float = values[Matrix.MSCALE_X]
    val degrees = (atan2(skewX.toDouble(), scaleX.toDouble()) * (180 / Math.PI)).roundToInt()
    return when {
        degrees < 0 -> abs(degrees)
        degrees > 0 -> 360 - degrees
        else -> 0
    }
}

internal fun reverseRotateRect(rect: Rect, rotateDegrees: Int, drawableSize: Size) {
    require(rotateDegrees % 90 == 0) {
        "rotateDegrees must be an integer multiple of 90"
    }
    when (rotateDegrees) {
        90 -> {
            val bottom = rect.bottom
            rect.bottom = rect.left
            rect.left = rect.top
            rect.top = rect.right
            rect.right = bottom
            rect.top = drawableSize.height - rect.top
            rect.bottom = drawableSize.height - rect.bottom
        }

        180 -> {
            var right = rect.right
            rect.right = rect.left
            rect.left = right
            right = rect.bottom
            rect.bottom = rect.top
            rect.top = right
            rect.top = drawableSize.height - rect.top
            rect.bottom = drawableSize.height - rect.bottom
            rect.left = drawableSize.width - rect.left
            rect.right = drawableSize.width - rect.right
        }

        270 -> {
            val bottom = rect.bottom
            rect.bottom = rect.right
            rect.right = rect.top
            rect.top = rect.left
            rect.left = bottom
            rect.left = drawableSize.width - rect.left
            rect.right = drawableSize.width - rect.right
        }
    }
}

internal fun rotatePoint(point: PointF, rotateDegrees: Int, drawableSize: Size) {
    require(rotateDegrees % 90 == 0) {
        "rotateDegrees must be an integer multiple of 90"
    }
    when (rotateDegrees) {
        90 -> {
            point.x = drawableSize.height - point.y
            point.y = point.x
        }

        180 -> {
            point.x = drawableSize.width - point.x
            point.y = drawableSize.height - point.y
        }

        270 -> {
            point.x = point.y
            point.y = drawableSize.width - point.x
        }
    }
}

internal fun ScaleType.computeTransform(srcSize: Size, dstSize: Size): Transform {
    val scaleFactor = this.computeScaleFactor(srcSize, dstSize)
    val translation = this.computeScaleTranslation(srcSize, dstSize)
    return Transform(scaleFactor, translation)
}

internal fun ScaleType.computeScaleFactor(srcSize: Size, dstSize: Size): ScaleFactor {
    val widthScale = dstSize.width / srcSize.width.toFloat()
    val heightScale = dstSize.height / srcSize.height.toFloat()
    val fillMaxDimension = max(widthScale, heightScale)
    val fillMinDimension = min(widthScale, heightScale)
    return when (this) {
        ScaleType.CENTER -> ScaleFactor(scaleX = 1.0f, scaleY = 1.0f)

        ScaleType.CENTER_CROP -> {
            ScaleFactor(scaleX = fillMaxDimension, scaleY = fillMaxDimension)
        }

        ScaleType.CENTER_INSIDE -> {
            if (srcSize.width <= dstSize.width && srcSize.height <= dstSize.height) {
                ScaleFactor(scaleX = 1.0f, scaleY = 1.0f)
            } else {
                ScaleFactor(scaleX = fillMinDimension, scaleY = fillMinDimension)
            }
        }

        ScaleType.FIT_START,
        ScaleType.FIT_CENTER,
        ScaleType.FIT_END -> {
            ScaleFactor(scaleX = fillMinDimension, scaleY = fillMinDimension)
        }

        ScaleType.FIT_XY -> {
            ScaleFactor(scaleX = widthScale, scaleY = heightScale)
        }

        ScaleType.MATRIX -> ScaleFactor(1.0f, 1.0f)
        else -> ScaleFactor(scaleX = 1.0f, scaleY = 1.0f)
    }
}

internal fun ScaleType.computeScaleTranslation(
    srcSize: Size,
    dstSize: Size
): Translation {
    val scaleFactor = this.computeScaleFactor(srcSize = srcSize, dstSize = dstSize)
    val scaledSrcSize = srcSize.times(scaleFactor)
    return when (this) {
        ScaleType.CENTER -> Translation(
            translationX = (dstSize.width - scaledSrcSize.width) / 2.0f,
            translationY = (dstSize.height - scaledSrcSize.height) / 2.0f
        )

        ScaleType.CENTER_CROP -> Translation(
            translationX = (dstSize.width - scaledSrcSize.width) / 2.0f,
            translationY = (dstSize.height - scaledSrcSize.height) / 2.0f
        )

        ScaleType.CENTER_INSIDE -> Translation(
            translationX = (dstSize.width - scaledSrcSize.width) / 2.0f,
            translationY = (dstSize.height - scaledSrcSize.height) / 2.0f
        )

        ScaleType.FIT_START -> Translation(
            translationX = 0.0f,
            translationY = 0.0f
        )

        ScaleType.FIT_CENTER -> Translation(
            translationX = (dstSize.width - scaledSrcSize.width) / 2.0f,
            translationY = (dstSize.height - scaledSrcSize.height) / 2.0f
        )

        ScaleType.FIT_END -> Translation(
            translationX = dstSize.width - scaledSrcSize.width.toFloat(),
            translationY = dstSize.height - scaledSrcSize.height.toFloat()
        )

        ScaleType.FIT_XY -> Translation(
            translationX = 0.0f,
            translationY = 0.0f
        )

        ScaleType.MATRIX -> Translation(
            translationX = 0.0f,
            translationY = 0.0f
        )

        else -> Translation(
            translationX = 0.0f,
            translationY = 0.0f
        )
    }
}

internal fun ScaleType.supportReadMode(): Boolean = this != ScaleType.FIT_XY

internal fun computeReadModeTransform(
    scaleType: ScaleType,
    srcSize: Size,
    dstSize: Size
): Transform {
    val widthScale = dstSize.width / srcSize.width.toFloat()
    val heightScale = dstSize.height / srcSize.height.toFloat()
    val fillMaxDimension = max(widthScale, heightScale)
    @Suppress("UnnecessaryVariable") val scaleX = fillMaxDimension
    @Suppress("UnnecessaryVariable") val scaleY = fillMaxDimension
    val baseTransform = scaleType.computeTransform(srcSize = srcSize, dstSize = dstSize)
    val translateX =
        if (baseTransform.translationX < 0) baseTransform.translationX * -1 * scaleX else 0.0f
    val translateY =
        if (baseTransform.translationY < 0) baseTransform.translationY * -1 * scaleY else 0.0f
    return Transform(
        scaleX = scaleX,
        scaleY = scaleY,
        translationX = translateX,
        translationY = translateY
    )
}

fun ScaleType.toScaleMode(): ScaleMode = when (this) {
    ScaleType.CENTER -> ScaleMode.NONE
    ScaleType.CENTER_CROP -> ScaleMode.CROP
    ScaleType.CENTER_INSIDE -> ScaleMode.INSIDE
    ScaleType.FIT_START -> ScaleMode.FIT
    ScaleType.FIT_CENTER -> ScaleMode.FIT
    ScaleType.FIT_END -> ScaleMode.FIT
    ScaleType.FIT_XY -> ScaleMode.FILL_BOUNDS
    ScaleType.MATRIX -> ScaleMode.NONE
    else -> ScaleMode.NONE
}